package org.openmcptools.transport.uds.spring;

import java.nio.channels.Selector;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.openmcptools.transport.uds.UDSTransportConfig;

public class UDSClientTransportConfig extends UDSTransportConfig {

	public static final String CLIENT_TRANSPORT_FACTORY_NAME = "UDSClientTransportProviderFactory";
	public static final String CLIENT_CF_TARGET = "(component.factory=" + CLIENT_TRANSPORT_FACTORY_NAME + ")";

	public UDSClientTransportConfig(Path targetSocketPath, int incomingBufferSize, Selector selector,
			ExecutorService executorService) {
		super(targetSocketPath, incomingBufferSize, selector, executorService);
	}

	public UDSClientTransportConfig(Map<String, ?> properties) {
		super(properties);
	}

	public UDSClientTransportConfig(Path targetSocketPath) {
		super(targetSocketPath);
	}

}
