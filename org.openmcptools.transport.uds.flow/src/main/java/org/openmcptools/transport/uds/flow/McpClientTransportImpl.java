package org.openmcptools.transport.uds.flow;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Selector;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.ecf.ai.mcp.transports.UDSClientStringChannel;
import org.openmcptools.transport.client.McpClientTransport;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;

@Component(factory = FlowUDSMcpClientTransportConfig.CLIENT_TRANSPORT_FACTORY_NAME)
public class McpClientTransportImpl implements McpClientTransport<Uni<Void>, JSONRPCMessage, Uni<JSONRPCMessage>, Uni<JSONRPCMessage>> {

	private static final Logger logger = LoggerFactory.getLogger(McpClientTransportImpl.class);

	private final UnicastProcessor<JSONRPCMessage> inboundSink;

	private final UnicastProcessor<JSONRPCMessage> outboundSink;

	// Must be set/non-null
	private UDSJsonObjectMapper objectMapper;

	private Path targetAddress;

	private int incomingBufferSize = 4096;

	private Selector selector;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private UDSClientStringChannel clientChannel;

	private volatile boolean isClosing = false;

	public McpClientTransportImpl() {
		this.inboundSink = UnicastProcessor.create();
		this.outboundSink = UnicastProcessor.create();
	}

	@Reference
	void setMcpJsonMapper(UDSJsonObjectMapper jsonMapper) {
		this.objectMapper = jsonMapper;
	}
	
	@Activate
	void activate(Map<String, Object> properties) throws Exception {
		FlowUDSMcpClientTransportConfig clientConfig = new FlowUDSMcpClientTransportConfig(properties);
		this.targetAddress = clientConfig.getTargetSocketPath();
		this.selector = clientConfig.getSelector();
		this.executorService = clientConfig.getExecutorService();

		this.clientChannel = new UDSClientStringChannel(this.selector, this.incomingBufferSize);
	}

	@Override
	public Uni<Void> connect(Function<Uni<JSONRPCMessage>, Uni<JSONRPCMessage>> handler) {
		return Uni.createFrom().item(() -> {
			handleIncomingMessages(handler);
			try {
				this.clientChannel.connect(UnixDomainSocketAddress.of(targetAddress), (client) -> {
					logger.info("CONNECTED to targetAddress=" + targetAddress);
				}, (data) -> {
					try {
						JSONRPCMessage json = deserializeJsonRpcMessage(this.objectMapper, data);
						this.inboundSink.onNext(json);
					} catch (Exception e) {
						if (!isClosing) {
							logger.error("Failed to enqueue inbound message: {}", data, e);
						}
					}
				});
			} catch (IOException e) {
				this.clientChannel.close();
				throw new RuntimeException(
						"Connect to address=" + targetAddress + " failed message: " + e.getMessage());
			}
			startOutboundProcessing();
			return null;
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool()).replaceWithVoid();
	}

	private JSONRPCMessage deserializeJsonRpcMessage(UDSJsonObjectMapper objectMapper, String data)
			throws IOException {
		return objectMapper.deserializeJsonRpcMessage(data);
	}

	private void handleIncomingMessages(Function<Uni<JSONRPCMessage>, Uni<JSONRPCMessage>> inboundMessageHandler) {
		this.inboundSink.onItem().transformToUniAndMerge(message -> 
				inboundMessageHandler.apply(Uni.createFrom().item(message))
						// Context management in Mutiny is via Uni.withContext/Multi.withContext
						// Note: Context propagation usually requires specific integration (e.g. Quarkus)
						// but here we manually set a context-like attribute if required by the handler
						.onItem().invoke(item -> { /* placeholder for observation if needed */ })
		).subscribe().with(item -> {});
	}

	@Override
	public Uni<Void> sendMessage(JSONRPCMessage message) {
		try {
			this.outboundSink.onNext(message);
			return Uni.createFrom().voidItem();
		} catch (Exception e) {
			return Uni.createFrom().failure(new RuntimeException("Failed to enqueue message", e));
		}
	}

	private void startOutboundProcessing() {
		this.handleOutbound(messages -> messages.emitOn(executorService).onItem().transformToUniAndConcatenate(message -> {
			if (message != null && !isClosing) {
				try {
					this.clientChannel.writeMessage(objectMapper.writeValueAsString(message));
					return Uni.createFrom().item(message);
				} catch (IOException e) {
					return Uni.createFrom().failure(new RuntimeException(e));
				}
			}
			return Uni.createFrom().nullItem();
		}));
	}

	protected void handleOutbound(Function<Multi<JSONRPCMessage>, Multi<JSONRPCMessage>> outboundConsumer) {
		outboundConsumer.apply(this.outboundSink).onCompletion().invoke(() -> {
			isClosing = true;
			outboundSink.onComplete();
		}).onFailure().invoke(e -> {
			if (!isClosing) {
				logger.error("Error in outbound processing", e);
				isClosing = true;
				outboundSink.onComplete();
			}
		}).subscribe().with(item -> {});
	}

	@Override
	public Uni<Void> closeGracefully() {
		return Uni.createFrom().item(() -> {
			isClosing = true;
			logger.debug("Initiating graceful shutdown");
			return null;
		}).chain(() -> {
			inboundSink.onComplete();
			outboundSink.onComplete();
			return Uni.createFrom().voidItem().onItem().delayIt().by(Duration.ofMillis(100));
		}).chain(() -> {
			// Close clientChannel
			if (this.clientChannel != null) {
				this.clientChannel.close();
				this.clientChannel = null;
			}
			logger.info("channel closed");
			return Uni.createFrom().voidItem();
		}).invoke(() -> {
			try {
				// Mutiny handles its own threading, but if the executor was specifically managed:
				// (Original code disposed of a scheduler derived from this executor)
				logger.debug("Graceful shutdown completed");
			} catch (Exception e) {
				logger.error("Error during graceful shutdown", e);
			}
		}).runSubscriptionOn(Infrastructure.getDefaultWorkerPool()).replaceWithVoid();
	}

	@Override
	public <T> T unmarshalFrom(Object data, Type type) {
		return this.objectMapper.unmarshalFrom(data, new TypeRef<>(type) {});
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> protocolVersions() {
		// TODO Auto-generated method stub
		return null;
	}

}
