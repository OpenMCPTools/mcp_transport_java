package org.openmcptools.transport;

import org.openmcptools.transport.util.GenericTypeRef;

/**
 * Represents an MCP session for communication between components.
 *
 * @param <AV> Asynchronous Void Type
 * @param <AR> Asynchronous Result Type
 */
public interface MCPSession<AV, AR> {
	
	/**
	 * Sends an asynchronous request.
	 *
	 * @param <T> the expected response type
	 * @param method the method name to invoke
	 * @param requestParams the parameters for the request
	 * @param responseTypeRef the type reference for the response
	 * @return an asynchronous result containing the response
	 */
	<T> AR sendRequestAsync(String method, Object requestParams, GenericTypeRef<T> responseTypeRef);

	/**
	 * Sends an asynchronous notification.
	 *
	 * @param method the method name to invoke
	 * @param params the parameters for the notification
	 * @return an asynchronous void result
	 */
	AV sendNotificationAsync(String method, Object params);

	/**
	 * Initiates a graceful shutdown of the session.
	 *
	 * @return an asynchronous void result
	 */
	AV closeAsync();

	/**
	 * Closes the session immediately.
	 */
	void closeSync();
}
