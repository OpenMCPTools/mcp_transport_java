package org.openmcptools.transport.uds.spring;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Selector;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.ecf.ai.mcp.transports.UDSClientStringChannel;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Component(factory = UDSMcpTransportConfig.CLIENT_TRANSPORT_FACTORY_NAME)
public class UDSMcpClientTransportFactory implements McpClientTransport {

	private static final Logger logger = LoggerFactory.getLogger(UDSMcpClientTransportFactory.class);

	private final Sinks.Many<JSONRPCMessage> inboundSink;

	private final Sinks.Many<JSONRPCMessage> outboundSink;

	// Must be set/non-null
	private SpringJsonObjectMapper objectMapper;

	private Path targetAddress;

	private int incomingBufferSize = 4096;

	private Selector selector;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private UDSClientStringChannel clientChannel;

	private Scheduler outboundScheduler;

	private volatile boolean isClosing = false;

	public UDSMcpClientTransportFactory() {
		this.inboundSink = Sinks.many().unicast().onBackpressureBuffer();
		this.outboundSink = Sinks.many().unicast().onBackpressureBuffer();
	}

	@Reference
	void setMcpJsonMapper(SpringJsonObjectMapper jsonMapper) {
		this.objectMapper = jsonMapper;
	}
	
	@Activate
	void activate(Map<String, Object> properties) throws Exception {
		UDSMcpClientTransportConfig clientConfig = new UDSMcpClientTransportConfig(properties);
		this.targetAddress = clientConfig.getTargetSocketPath();
		this.selector = clientConfig.getSelector();
		this.executorService = clientConfig.getExecutorService();

		this.outboundScheduler = Schedulers.fromExecutorService(this.executorService, "outbound");
		this.clientChannel = new UDSClientStringChannel(this.selector, this.incomingBufferSize);
	}

	@Override
	public Mono<Void> connect(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
		return Mono.<Void>fromRunnable(() -> {
			handleIncomingMessages(handler);
			try {
				this.clientChannel.connect(UnixDomainSocketAddress.of(targetAddress), (client) -> {
					logger.info("CONNECTED to targetAddress=" + targetAddress);
				}, (data) -> {
					JSONRPCMessage json = deserializeJsonRpcMessage(this.objectMapper, data);
					if (!this.inboundSink.tryEmitNext(json).isSuccess()) {
						if (!isClosing) {
							logger.error("Failed to enqueue inbound message: {}", json);
						}
					}
				});
			} catch (IOException e) {
				this.clientChannel.close();
				throw new RuntimeException(
						"Connect to address=" + targetAddress + " failed message: " + e.getMessage());
			}
			startOutboundProcessing();
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private JSONRPCMessage deserializeJsonRpcMessage(SpringJsonObjectMapper objectMapper, String data)
			throws IOException {
		return objectMapper.deserializeJsonRpcMessage(data);
	}

	private void handleIncomingMessages(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> inboundMessageHandler) {
		this.inboundSink.asFlux().flatMap(message -> Mono.just(message).transform(inboundMessageHandler)
				.contextWrite(ctx -> ctx.put("observation", "myObservation"))).subscribe();
	}

	@Override
	public Mono<Void> sendMessage(JSONRPCMessage message) {
		if (this.outboundSink.tryEmitNext(message).isSuccess()) {
			return Mono.empty();
		} else {
			return Mono.error(new RuntimeException("Failed to enqueue message"));
		}
	}

	private void startOutboundProcessing() {
		this.handleOutbound(messages -> messages.publishOn(outboundScheduler).handle((message, s) -> {
			if (message != null && !isClosing) {
				try {
					this.clientChannel.writeMessage(objectMapper.writeValueAsString(message));
					s.next(message);
				} catch (IOException e) {
					s.error(new RuntimeException(e));
				}
			}
		}));
	}

	protected void handleOutbound(Function<Flux<JSONRPCMessage>, Flux<JSONRPCMessage>> outboundConsumer) {
		outboundConsumer.apply(outboundSink.asFlux()).doOnComplete(() -> {
			isClosing = true;
			outboundSink.tryEmitComplete();
		}).doOnError(e -> {
			if (!isClosing) {
				logger.error("Error in outbound processing", e);
				isClosing = true;
				outboundSink.tryEmitComplete();
			}
		}).subscribe();
	}

	@Override
	public Mono<Void> closeGracefully() {
		return Mono.fromRunnable(() -> {
			isClosing = true;
			logger.debug("Initiating graceful shutdown");
		}).then(Mono.<Void>defer(() -> {
			inboundSink.tryEmitComplete();
			outboundSink.tryEmitComplete();
			return Mono.delay(Duration.ofMillis(100)).then();
		})).then(Mono.defer(() -> {
			// Close clientChannel
			if (this.clientChannel != null) {
				this.clientChannel.close();
				this.clientChannel = null;
			}
			return Mono.empty();
		})).doOnNext(o -> {
			logger.info("channel closed");
		}).then(Mono.fromRunnable(() -> {
			try {
				outboundScheduler.dispose();
				logger.debug("Graceful shutdown completed");
			} catch (Exception e) {
				logger.error("Error during graceful shutdown", e);
			}
		})).then().subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return this.objectMapper.unmarshalFrom(data, typeRef);
	}

}