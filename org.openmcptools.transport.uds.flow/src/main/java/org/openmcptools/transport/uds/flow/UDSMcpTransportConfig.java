package org.openmcptools.transport.uds.flow;

import java.nio.channels.Selector;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.openmcptools.transport.uds.UDSClientTransportConfig;

public class UDSMcpTransportConfig extends UDSClientTransportConfig {

	public UDSMcpTransportConfig(Map<String, ?> properties) {
		super(properties);
	}

	public UDSMcpTransportConfig(Path targetSocketPath, int incomingBufferSize, Selector selector,
			ExecutorService executorService) {
		super(targetSocketPath, incomingBufferSize, selector, executorService);
	}

	public UDSMcpTransportConfig(Path targetSocketPath) {
		super(targetSocketPath);
	}

}
