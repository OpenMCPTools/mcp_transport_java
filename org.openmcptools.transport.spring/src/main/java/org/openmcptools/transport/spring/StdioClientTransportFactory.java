package org.openmcptools.transport.spring;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.openmcptools.transport.client.MCPClientTransport;
import org.openmcptools.transport.util.GenericTypeRef;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import reactor.core.publisher.Mono;

@Component(factory = StdioClientTransportConfig.CLIENT_TRANSPORT_FACTORY_NAME, service = { MCPClientTransport.class,
		McpClientTransport.class })
public class StdioClientTransportFactory implements
		MCPClientTransport<Mono<Void>, Mono<JSONRPCMessage>, Mono<JSONRPCMessage>, JSONRPCMessage>, McpClientTransport {

	private JsonObjectMapper jsonMapper;

	@Reference
	void setJsonObjectMapper(JsonObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	private StdioClientTransport impl;

	@Activate
	void activate(Map<String, Object> properties) {
		ServerParameters p = ((StdioClientTransportConfig) properties
				.get(StdioClientTransportConfig.CLIENT_TRANSPORT_FACTORY_CONFIG)).getServerParameters();
		this.impl = new StdioClientTransport(p, jsonMapper.getMcpJsonMapper());
	}

	@Override
	public List<String> protocolVersions() {
		return impl.protocolVersions();
	}

	@Override
	public Mono<Void> closeGracefully() {
		return closeAsync();
	}

	@Override
	public Mono<Void> connect(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
		return connectAsync(handler, null);
	}

	@Override
	public Mono<Void> sendMessage(JSONRPCMessage message) {
		return sendMessageAsync(message);
	}

	@Override
	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return unmarshall(data, new GenericTypeRef<T>(typeRef.getType()));
	}

	// impl of MCPClientTransport
	public <T> T unmarshall(Object data, GenericTypeRef<T> unmarshalledTypeRef) {
		return impl.unmarshalFrom(data, new TypeRefAdapter<T>(unmarshalledTypeRef));
	}

	public Mono<Void> closeAsync() {
		return impl.closeGracefully();
	}

	public void closeSync() {
		impl.close();
	}

	public Mono<Void> connectAsync(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> requestResponseHandler,
			Consumer<Throwable> exceptionHandler) {
		if (exceptionHandler != null) {
			this.impl.setExceptionHandler(exceptionHandler);
		}
		return this.impl.connect(requestResponseHandler);
	}

	@Override
	public Mono<Void> sendMessageAsync(JSONRPCMessage message) {
		return impl.sendMessage(message);
	}

}
