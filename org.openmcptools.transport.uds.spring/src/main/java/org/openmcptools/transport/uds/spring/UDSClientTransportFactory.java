package org.openmcptools.transport.uds.spring;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import reactor.core.publisher.Mono;

import org.openmcptools.transport.client.McpClientTransport;

@Component(factory = "UDSClientTransportFactory")
public class UDSClientTransportFactory
		implements McpClientTransport<Mono<Void>, JSONRPCMessage, Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> {

	private UDSMcpClientTransportFactory impl;

	public UDSClientTransportFactory() {

	}

	@Activate
	void activate(Map<String, Object> properties) throws Exception {
		impl = new UDSMcpClientTransportFactory();
		impl.activate(properties);
	}

	@Override
	public void close() {
		this.impl.close();
	}

	@Override
	public Mono<Void> closeGracefully() {
		return this.impl.closeGracefully();
	}

	@Override
	public <T> T unmarshalFrom(Object data, Type type) {
		return this.impl.unmarshalFrom(data, new TypeRef<T>(type) {
		});
	}

	@Override
	public List<String> protocolVersions() {
		return this.impl.protocolVersions();
	}

	@Override
	public Mono<Void> sendMessage(JSONRPCMessage message) {
		return this.impl.sendMessage(message);
	}

	@Override
	public Mono<Void> connect(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
		return this.impl.connect(handler);
	}

}
