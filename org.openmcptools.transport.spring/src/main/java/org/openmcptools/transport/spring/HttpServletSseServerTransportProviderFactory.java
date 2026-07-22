package org.openmcptools.transport.spring;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openmcptools.transport.server.MCPServerSessionFactory;
import org.openmcptools.transport.server.MCPServerTransportProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerSession.Factory;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

@Component(factory = HttpServletSseServerTransportProviderConfig.SERVER_TRANSPORT_FACTORY_NAME, service = {
		MCPServerTransportProvider.class, McpServerTransportProvider.class })
public class HttpServletSseServerTransportProviderFactory
		implements MCPServerTransportProvider<Mono<Void>, Mono<?>, JSONRPCMessage>, McpServerTransportProvider {

	private JsonObjectMapper jsonMapper;
	private MCPHttpServletSseServerTransportProvider impl;

	@Reference
	void setJsonObjectMapper(JsonObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	class MCPHttpServletSseServerTransportProvider extends HttpServletSseServerTransportProvider implements
			org.openmcptools.transport.server.MCPServerTransportProvider<Mono<Void>, Mono<?>, JSONRPCMessage> {

		private static final long serialVersionUID = 7695330167683607291L;

		MCPHttpServletSseServerTransportProvider(String baseUrl, String messageEndpoint, String sseEndpoint,
				Duration keepAliveInterval, McpTransportContextExtractor<HttpServletRequest> contextExtractor,
				ServerTransportSecurityValidator securityValidator) {
			super(jsonMapper.getMcpJsonMapper(), baseUrl, messageEndpoint, sseEndpoint, keepAliveInterval,
					contextExtractor, securityValidator);
		}

		@Override
		public void initServerSessionFactory(MCPServerSessionFactory<Mono<Void>, Mono<?>, JSONRPCMessage> factory) {
			impl.setSessionFactory(
					transport -> ((McpServerSession) factory.create(new MCPServerTransportImpl(transport))));
		}

		@Override
		public void closeSync() {
			super.close();
		}

		@Override
		public Mono<Void> notifyClientsAsync(String method, Object params) {
			return notifyClients(method, params);
		}

		@Override
		public Mono<Void> closeAsync() {
			return closeGracefully();
		}
	}

	@Activate
	void activate(Map<String, Object> properties) {
		HttpServletSseServerTransportProviderConfig config = HttpServletSseServerTransportProviderConfig
				.fromProperties(properties);
		Objects.requireNonNull(config, "StdioServerTransportProviderConfig must not be null");
		this.impl = new MCPHttpServletSseServerTransportProvider(config.getBaseUrl(), config.getMessageEndpoint(),
				config.getSseEndpoint(), config.getKeepAliveInterval(), config.getContextExtractor(),
				config.getSecurityValidator());
	}

	@Override
	public List<String> protocolVersions() {
		return impl.protocolVersions();
	}

	@Override
	public Mono<Void> notifyClientsAsync(String method, Object params) {
		return this.impl.notifyClientsAsync(method, params);
	}

	@Override
	public Mono<Void> closeAsync() {
		return this.impl.closeAsync();
	}

	@Override
	public void setSessionFactory(Factory sessionFactory) {
		this.impl.setSessionFactory(sessionFactory);
	}

	@Override
	public void initServerSessionFactory(MCPServerSessionFactory<Mono<Void>, Mono<?>, JSONRPCMessage> factory) {
		this.impl.initServerSessionFactory(factory);
	}

	@Override
	public void closeSync() {
		this.impl.closeSync();
	}

	@Override
	public Mono<Void> notifyClients(String method, Object params) {
		return impl.notifyClients(method, params);
	}

	@Override
	public Mono<Void> closeGracefully() {
		return impl.closeGracefully();
	}

}
