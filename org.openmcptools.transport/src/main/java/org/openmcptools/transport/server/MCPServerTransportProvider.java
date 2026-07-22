/*
 * Copyright 2024-2025 the original author or authors.
 */

package org.openmcptools.transport.server;

import java.util.List;

/**
 * Provider for server-side transport management.
 *
 * @param <AV> Asynchronous Void Type
 * @param <AR> Asynchronous Result Type
 * @param <M> Message Type
 */
public interface MCPServerTransportProvider<AV, AR, M> {
	
	/**
	 * Initializes the factory used to create server sessions.
	 *
	 * @param factory the session factory
	 */
	void initServerSessionFactory(MCPServerSessionFactory<AV, AR, M> factory);

	/**
	 * Sends a notification to all connected clients.
	 *
	 * @param method the method name
	 * @param params the parameters for the notification
	 * @return an asynchronous void result
	 */
	AV notifyClientsAsync(String method, Object params);

	/**
	 * Closes the transport provider immediately.
	 */
	void closeSync();

	/**
	 * Initiates a graceful shutdown of the transport provider.
	 *
	 * @return an asynchronous void result
	 */
	AV closeAsync();

	/**
	 * Returns the supported protocol versions.
	 *
	 * @return a list of protocol versions
	 */
	List<String> protocolVersions();
}
