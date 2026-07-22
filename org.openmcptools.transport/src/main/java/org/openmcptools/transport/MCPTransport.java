package org.openmcptools.transport;

import org.openmcptools.transport.util.GenericTypeRef;

/**
 * Base interface for MCP transport implementations.
 *
 * @param <AV> Asynchronous Void Type
 * @param <M> Message Type
 */
public interface MCPTransport<AV, M> {

	/**
	 * Sends a message through the transport.
	 *
	 * @param message the message to send
	 * @return an asynchronous void result
	 */
	AV sendMessageAsync(M message);

	/**
	 * Unmarshals data from a transport-specific format to a typed object.
	 *
	 * @param <T> the target type
	 * @param data the raw data to unmarshal
	 * @param unmarshalledTypeRef the type reference for unmarshalling
	 * @return the unmarshalled object
	 */
	<T> T unmarshall(Object data, GenericTypeRef<T> unmarshalledTypeRef);

	/**
	 * Initiates a graceful shutdown of the transport.
	 *
	 * @return an asynchronous void result
	 */
	AV closeAsync();

	/**
	 * Closes the transport immediately.
	 */
	void closeSync();
}

