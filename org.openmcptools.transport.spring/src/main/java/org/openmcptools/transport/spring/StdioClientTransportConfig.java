package org.openmcptools.transport.spring;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

import io.modelcontextprotocol.client.transport.ServerParameters;

public class StdioClientTransportConfig {

	public static final String CLIENT_TRANSPORT_FACTORY_NAME = "StdioClientTransportFactory";
	public static final String CLIENT_CF_TARGET = "(component.factory=" + CLIENT_TRANSPORT_FACTORY_NAME + ")";

	public static final String CLIENT_TRANSPORT_FACTORY_CONFIG = StdioClientTransportConfig.class + ".config";

	private final ServerParameters serverParams;

	public StdioClientTransportConfig(ServerParameters serverParameters) {
		Objects.requireNonNull(serverParameters, "serverParameters must not be null");
		this.serverParams = serverParameters;
	}

	public ServerParameters getServerParameters() {
		return serverParams;
	}

	public Dictionary<String, Object> asProperties() {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(CLIENT_TRANSPORT_FACTORY_CONFIG, this);
		return props;
	}
}
