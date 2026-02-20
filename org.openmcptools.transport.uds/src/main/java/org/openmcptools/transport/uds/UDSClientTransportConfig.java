package org.openmcptools.transport.uds;

import java.nio.channels.Selector;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class UDSClientTransportConfig extends UDSTransportConfig {

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
