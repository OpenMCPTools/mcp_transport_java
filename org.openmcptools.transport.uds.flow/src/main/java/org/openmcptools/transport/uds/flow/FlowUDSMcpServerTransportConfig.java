package org.openmcptools.transport.uds.flow;

import java.nio.channels.Selector;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.openmcptools.transport.uds.UDSServerTransportConfig;

public class FlowUDSMcpServerTransportConfig extends UDSServerTransportConfig {

	public static final String SERVER_TRANSPORT_FACTORY_NAME = "FlowUDSMcpServerTransportProvider";

	public FlowUDSMcpServerTransportConfig(Map<String, Object> properties) {
		super(properties);
	}

	public FlowUDSMcpServerTransportConfig(Path targetSocketPath, boolean autoRestartSession) {
		super(targetSocketPath, autoRestartSession);
	}

	public FlowUDSMcpServerTransportConfig(Path targetSocketPath, int incomingBufferSize, Selector selector,
			ExecutorService executorService, boolean autoRestartSession) {
		super(targetSocketPath, incomingBufferSize, selector, executorService, autoRestartSession);
	}

	public FlowUDSMcpServerTransportConfig(Path targetSocketPath) {
		super(targetSocketPath);
	}
	
	
}
