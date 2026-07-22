package org.openmcptools.transport.spring;

import java.util.List;

import org.openmcptools.transport.server.MCPServerTransport;
import org.openmcptools.transport.util.GenericTypeRef;

import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerTransport;
import reactor.core.publisher.Mono;

public class MCPServerTransportImpl implements MCPServerTransport<Mono<Void>, JSONRPCMessage>, McpServerTransport {

	private McpServerTransport delegate;

	public MCPServerTransportImpl(McpServerTransport delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<String> protocolVersions() {
		return delegate.protocolVersions();
	}

	@Override
	public <T> T unmarshall(Object data, GenericTypeRef<T> unmarshalledTypeRef) {
		return delegate.unmarshalFrom(data, new TypeRefAdapter<>(unmarshalledTypeRef));
	}

	@Override
	public Mono<Void> closeAsync() {
		return delegate.closeGracefully();
	}

	@Override
	public void closeSync() {
		delegate.close();
	}

	@Override
	public Mono<Void> sendMessage(JSONRPCMessage message) {
		return delegate.sendMessage(message);
	}

	@Override
	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return delegate.unmarshalFrom(data, typeRef);
	}

	@Override
	public Mono<Void> sendMessageAsync(JSONRPCMessage message) {
		return sendMessage(message);
	}

	@Override
	public Mono<Void> closeGracefully() {
		return closeGracefully();
	}
}
