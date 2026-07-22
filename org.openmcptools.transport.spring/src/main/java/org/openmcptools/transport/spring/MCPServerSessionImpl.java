package org.openmcptools.transport.spring;

import java.time.Duration;
import java.util.Map;

import org.openmcptools.transport.server.MCPServerSession;
import org.openmcptools.transport.server.MCPServerTransport;
import org.openmcptools.transport.util.GenericTypeRef;

import io.modelcontextprotocol.server.McpInitRequestHandler;
import io.modelcontextprotocol.server.McpNotificationHandler;
import io.modelcontextprotocol.server.McpRequestHandler;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import reactor.core.publisher.Mono;

public class MCPServerSessionImpl extends McpServerSession implements MCPServerSession<Mono<Void>, Mono<?>> {

	public MCPServerSessionImpl(String id, Duration requestTimeout,
			MCPServerTransport<Mono<Void>, JSONRPCMessage> transport, McpInitRequestHandler initHandler,
			Map<String, McpRequestHandler<?>> requestHandlers,
			Map<String, McpNotificationHandler> notificationHandlers) {
		super(id, requestTimeout, (McpServerTransport) transport, initHandler, requestHandlers, notificationHandlers);
	}

	@Override
	public <T> Mono<T> sendRequestAsync(String method, Object requestParams, GenericTypeRef<T> responseTypeRef) {
		return super.sendRequest(method, requestParams, new TypeRefAdapter<T>(responseTypeRef));
	}

	public String getSessionId() {
		return getId();
	}

	@Override
	public Mono<Void> sendNotificationAsync(String method, Object params) {
		return super.sendNotification(method, params);
	}

	@Override
	public Mono<Void> closeAsync() {
		return super.closeGracefully();
	}

	@Override
	public void closeSync() {
		super.close();
	}
}
