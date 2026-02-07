package org.openmcptools.transport.uds.spring;

import java.util.List;
import java.util.Map;

import org.openmcptools.transport.server.McpServerTransportProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerSession.Factory;
import reactor.core.publisher.Mono;

@Component(factory = "UDSServerTransportProviderFactory")
public class UDSServerTransportProviderFactory
		implements McpServerTransportProvider<Mono<Void>, JSONRPCMessage, McpServerSession.Factory> {

	private UDSMcpServerTransportProviderFactory impl;

	@Activate
	protected void activate(Map<String, Object> properties) {
		this.impl = new UDSMcpServerTransportProviderFactory();
		this.impl.activate(properties);
	}

	@Deactivate
	protected void deactivate() {
		if (this.impl != null) {
			this.impl.close();
			this.impl = null;
		}
	}

	@Override
	public Mono<Void> notifyClients(String method, Object params) {
		return this.impl.notifyClients(method, params);
	}

	@Override
	public void setSessionFactory(Factory factory) {
		this.setSessionFactory(factory);
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
	public List<String> protocolVersions() {
		return this.impl.protocolVersions();
	}

}