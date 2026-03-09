package org.openmcptools.transport.uds.flow;

import java.nio.channels.Selector;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.openmcptools.transport.uds.UDSClientTransportConfig;

public class FlowUDSMcpClientTransportConfig extends UDSClientTransportConfig {

	public static final String CLIENT_TRANSPORT_FACTORY_NAME = "FlowUDSMcpClientTransportConfig";
	
	public FlowUDSMcpClientTransportConfig(Map<String, ?> properties) {
		super(properties);
	}

	public FlowUDSMcpClientTransportConfig(Path targetSocketPath, int incomingBufferSize, Selector selector,
			ExecutorService executorService) {
		super(targetSocketPath, incomingBufferSize, selector, executorService);
	}

	public FlowUDSMcpClientTransportConfig(Path targetSocketPath) {
		super(targetSocketPath);
	}

}
