package org.openmcptools.transport.uds.flow;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openmcptools.transport.uds.server.UDSServerStringChannel;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.mutiny.subscription.BackPressureStrategy;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.smallrye.mutiny.tuples.Tuple2;

import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.reactivestreams.Publisher;

@Component(factory = FlowUDSMcpServerTransportConfig.SERVER_TRANSPORT_FACTORY_NAME)
public class UDSMcpServerTransportProvider implements McpServerTransportProvider {

	private static final Logger logger = LoggerFactory.getLogger(UDSMcpServerTransportProvider.class);

	private JsonObjectMapper objectMapper;
	// Required Path for UnixDomainSocket creation
	private Path targetAddress;

	private int incomingBufferSize;

	private boolean restartSession;

	private Selector selector;

	private ExecutorService executorService;

	// Created/set in setSessionFactory
	private McpServerSession serverSession;

	// Created/set in setSessionFactory
	private UDSMcpSessionTransport sessionTransport;

	public UDSMcpServerTransportProvider() {

	}

	@Reference 
	void setObjectMapper(JsonObjectMapper jsonMapper) {
		this.objectMapper = jsonMapper;
	}
	
	@Activate
	protected void activate(Map<String, Object> properties) {
		FlowUDSMcpServerTransportConfig serverConfig = new FlowUDSMcpServerTransportConfig(properties);
		this.targetAddress = serverConfig.getTargetSocketPath();
		this.incomingBufferSize = serverConfig.getIncomingBufferSize();
		this.restartSession = serverConfig.autoRestartSession();
		this.selector = serverConfig.getSelector();
		this.executorService = serverConfig.getExecutorService();
	}

	@Deactivate
	protected void deactivate() {
		if (this.serverSession != null) {
			this.serverSession.close();
			this.serverSession = null;
		}
		if (this.targetAddress != null) {
			this.targetAddress.toFile().delete();
			this.targetAddress = null;
		}
	}

	@Override
	public List<String> protocolVersions() {
		return List.of(ProtocolVersions.MCP_2024_11_05);
	}

	@Override
	public void setSessionFactory(McpServerSession.Factory sessionFactory) {
		this.sessionTransport = new UDSMcpSessionTransport();
		this.serverSession = sessionFactory.create(sessionTransport);
		this.sessionTransport.initProcessing();
	}

	@Override
	public Uni<Void> notifyClients(String method, Object params) {
		if (this.serverSession == null) {
			return Uni.createFrom().failure(McpError.builder(-1).message("No uds acceptedClient to use for notifyClients").build());
		}
		return Uni.createFrom().publisher();
		return serverSession.sendNotification(method, params).onFailure().invoke(e -> logger.error("Failed to send notification: {}", e.getMessage()))
	}

	@Override
	public Uni<Void> closeGracefully() {
		if (this.serverSession == null) {
			return Uni.createFrom().voidItem();
		}
		this.restartSession = false;
		return this.serverSession.closeGracefully();
	}

	public boolean isClientConnected() {
		return (this.sessionTransport != null) ? this.sessionTransport.isClientConnected() : false;
	}

	private class UDSMcpSessionTransport implements McpServerTransport {

		private AtomicBoolean isClosing;

		private Sinks.Many<JSONRPCMessage> inboundSink;

		private Sinks.Many<JSONRPCMessage> outboundSink;

		private AtomicBoolean isStarted;

		private Sinks.One<Void> inboundReady;

		private Sinks.One<Void> outboundReady;

		private UDSServerStringChannel serverSocketChannel;

		private synchronized void initialize() {
			isClosing = new AtomicBoolean(false);
			isStarted = new AtomicBoolean(false);
			// Refined Mutiny Sink initialization
			outboundReady = Sinks.one().;
			inboundReady = Sinks.one();
			this.inboundSink = Sinks.many().unicast().onBackpressureBuffer();
			this.outboundSink = Sinks.many().unicast().onBackpressureBuffer();
		}

		public UDSMcpSessionTransport() {
			initialize();
		}

		public synchronized void handleMessage(McpSchema.JSONRPCMessage json) throws IOException {
			try {
				if (this.inboundSink.tryEmitNext(json).isFailure()) {
					throw new Exception("Failed to enqueue message");
				}
			} catch (Exception e) {
				logIfNotClosing("Error processing inbound message", e);
				throw new IOException("Error in processing inbound message", e);
			}
		}

		@Override
		public synchronized Uni<Void> sendMessage(McpSchema.JSONRPCMessage message) {
			return Uni.combine().all().unis(inboundReady.asUni(), outboundReady.asUni()).discardItems().chain(() -> {
				if (outboundSink.tryEmitNext(message).isSuccess()) {
					return Uni.createFrom().voidItem();
				} else {
					return Uni.createFrom().failure(new RuntimeException("Failed to enqueue message"));
				}
			});
		}

		@Override
		public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
			return objectMapper.unmarshalFrom(data, typeRef);
		}

		@Override
		public Uni<Void> closeGracefully() {
			return Uni.createFrom().item(() -> {
				isClosing.set(true);
				logger.debug("Session transport closing gracefully");
				inboundSink.tryEmitComplete();
				return null;
			});
		}

		@Override
		public synchronized void close() {
			if (logger.isDebugEnabled()) {
				logger.debug("Session transport closing");
			}
			isClosing.set(true);
			serverSocketChannel.close();
			if (logger.isDebugEnabled()) {
				logger.debug("Session transport closed");
			}
		}

		public synchronized boolean isClientConnected() {
			return isClosing.get() ? false : this.serverSocketChannel.isClientConnected();
		}

		private void initProcessing() {
			this.inboundSink.asMulti().onItem().transformToUniAndMerge(message1 -> serverSession.handle(message1)).onTermination().invoke(() -> {
				this.outboundSink.tryEmitComplete();
			}).subscribe().with(unused -> {});

			if (isStarted.compareAndSet(false, true)) {
				inboundReady.tryEmitNext(null);
			}

			try {
				this.serverSocketChannel = new UDSServerStringChannel(selector == null ? Selector.open() : selector,
						incomingBufferSize, executorService) {
					public void start(UnixDomainSocketAddress address, IOConsumer<SocketChannel> acceptHandler,
							IOConsumer<String> readHandler) throws IOException {
						super.start(StandardProtocolFamily.UNIX, address, acceptHandler, readHandler);
					}

					public boolean isClientConnected() {
						return this.acceptedClient != null;
					}

					@Override
					protected void handleException(SelectionKey key, Throwable e) {
						// Do this with existing executor
						if (restartSession) {
							executor.execute(() -> {
								try {
									synchronized (UDSMcpSessionTransport.this) {
										UDSMcpSessionTransport.this.close();
										// Delete the file underneath the UDS socket
										Files.deleteIfExists(targetAddress);
										if (logger.isDebugEnabled()) {
											logger.debug("Session transport restarting");
										}
										initialize();
										initProcessing();
										if (logger.isDebugEnabled()) {
											logger.debug("Session transport restarted");
										}
									}
								} catch (IOException e1) {
									logger.error("Could not restart server session", e1);
								}
							});
						}
					}
				};
				this.serverSocketChannel.start(UnixDomainSocketAddress.of(targetAddress), (clientChannel) -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Accepted connect from clientChannel=" + clientChannel);
					}
					startOutboundProcessing();
				}, (dataLine) -> {
					String message = (String) dataLine;
					if (logger.isDebugEnabled()) {
						logger.debug("Received message line=" + message);
					}
					try {
						handleMessage(deserializeJsonRpcMessage(objectMapper, message.trim()));
					} catch (IOException e) {
						this.serverSocketChannel.close();
					}
				});
			} catch (IOException e) {
				this.serverSocketChannel.close();
				throw new RuntimeException("accepterNonBlockSocketChannel could not be started", e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Session transport initProcessing completed");
			}
		}

		private JSONRPCMessage deserializeJsonRpcMessage(JsonObjectMapper objectMapper, String data)
				throws IOException {
			return objectMapper.deserializeJsonRpcMessage(data);
		}

		private void startOutboundProcessing() {
			outboundSink.asMulti()
				 .onSubscription().invoke(() -> outboundReady.tryEmitNext(null))
				 .emitOn(executorService)
				 .onItem().transformToUniAndConcatenate(message -> {
					 if (message != null && !isClosing.get()) {
						 try {
							 serverSocketChannel.writeMessage(objectMapper.writeValueAsString(message));
							 return Uni.createFrom().item(message);
						 }
						 catch (IOException e) {
							 if (!isClosing.get()) {
								 logger.error("Error writing message", e);
								 return Uni.createFrom().failure(new RuntimeException(e));
							 }
							 else {
								 logger.debug("Stream closed during shutdown", e);
								 return Uni.createFrom().nothing();
							 }
						 }
					 }
					 else if (isClosing.get()) {
						 return Uni.createFrom().nothing();
					 }
					 return Uni.createFrom().nothing();
				 })
				 .onCompletion().invoke(() -> {
					 isClosing.set(true);
				 })
				 .onFailure().invoke(e -> {
					 if (!isClosing.get()) {
						 logger.error("Error in outbound processing", e);
						 isClosing.set(true);
					 }
				 })
				 .subscribe().with(msg -> {}, err -> {});
		 } 

		private void logIfNotClosing(String message, Exception e) {
			if (!isClosing.get()) {
				logger.error(message, e);
			}
		}

	}

}
