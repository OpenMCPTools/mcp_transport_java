package org.openmcptools.transport.spring;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openmcptools.transport.server.MCPServerSessionFactory;
import org.openmcptools.transport.server.MCPServerTransportProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerSession.Factory;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import reactor.core.publisher.Mono;

@Component(factory = StdioServerTransportProviderConfig.SERVER_TRANSPORT_FACTORY_NAME, service = { MCPServerTransportProvider.class, McpServerTransportProvider.class })
public class StdioServerTransportProviderFactory implements MCPServerTransportProvider<Mono<Void>, Mono<?>, JSONRPCMessage>, McpServerTransportProvider {

	private JsonObjectMapper jsonMapper;
	private MCPStdioServerTransportProvider impl;
	
	@Reference
	void setJsonObjectMapper(JsonObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	class MCPStdioServerTransportProvider extends StdioServerTransportProvider implements org.openmcptools.transport.server.MCPServerTransportProvider<Mono<Void>, Mono<?>, JSONRPCMessage> {

		public MCPStdioServerTransportProvider(StdioServerTransportProviderConfig config) {
			super(jsonMapper.getMcpJsonMapper(), config.getInputStream(), config.getOutputStream());
		}
		@Override
		public void close() {
			super.close();
		}

		class MCPStdioTransport extends StdioMcpSessionTransport {
		    @Override
		    protected void initProcessing() {
		    	super.initProcessing();
		    }
		}

		@Override
		public void initServerSessionFactory(MCPServerSessionFactory<Mono<Void>, Mono<?>, JSONRPCMessage> factory) {
			MCPStdioTransport t = new MCPStdioTransport();
			this.session = (McpServerSession) factory.create(new MCPServerTransportImpl(t));
			t.initProcessing();
		}
	}
	
	@Activate
	void activate(Map<String, Object> properties) {
		StdioServerTransportProviderConfig config = (StdioServerTransportProviderConfig) properties.get(StdioServerTransportProviderConfig.SERVER_TRANSPORT_FACTORY_CONFIG);
		Objects.requireNonNull(config, "StdioServerTransportProviderConfig must not be null");
	    this.impl = new MCPStdioServerTransportProvider(config);
	}
	
	@Override
	public List<String> protocolVersions() {
		return impl.protocolVersions();
	}

	@Override
	public Mono<Void> notifyClients(String method, Object params) {
		return this.impl.notifyClients(method, params);
	}

	@Override
	public Mono<Void> closeGracefully() {
		return this.impl.closeGracefully();
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
	public void close() {
		this.impl.close();
	}

}
