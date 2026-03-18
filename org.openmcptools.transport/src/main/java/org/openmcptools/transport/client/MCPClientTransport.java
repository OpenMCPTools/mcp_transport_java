package org.openmcptools.transport.client;

import java.util.function.Consumer;
import java.util.function.Function;
import org.openmcptools.transport.MCPTransport;

/**
 * Interface for client-side MCP transport.
 *
 * @param <AV> Asynchronous Void Type
 * @param <ARQ> Asynchronous Request Type
 * @param <ARS> Asynchronous Result Type
 * @param <M> Message Type
 */
public interface MCPClientTransport<AV, ARQ, ARS, M> extends MCPTransport<AV, M> {

	/**
	 * Connects the transport and sets up handlers for requests and exceptions.
	 *
	 * @param requestResponseHandler the handler for processing requests and producing results
	 * @param exceptionHandler the handler for processing errors
	 * @return an asynchronous void result indicating connection status
	 */
	AV connect(Function<ARQ, ARS> requestResponseHandler, Consumer<Throwable> exceptionHandler);

	/**
	 * Connects the transport with a request handler.
	 *
	 * @param requestResponseHandler the handler for processing requests and producing results
	 * @return an asynchronous void result indicating connection status
	 */
	default AV connect(Function<ARQ, ARS> requestResponseHandler) {
		return connect(requestResponseHandler, null);
	}
}
