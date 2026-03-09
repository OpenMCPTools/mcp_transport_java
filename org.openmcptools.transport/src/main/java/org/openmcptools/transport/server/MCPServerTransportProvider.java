/*
 * Copyright 2024-2025 the original author or authors.
 */

package org.openmcptools.transport.server;

import java.util.List;

public interface MCPServerTransportProvider<AV, AR, M> {
	
	void initServerSessionFactory(MCPServerSessionFactory<AV, AR, M> factory);

	AV notifyClients(String method, Object params);

	void close();

	AV closeGracefully();

	List<String> protocolVersions();

}
