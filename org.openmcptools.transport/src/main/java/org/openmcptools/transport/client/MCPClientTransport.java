package org.openmcptools.transport.client;

import java.util.function.Consumer;
import java.util.function.Function;

import org.openmcptools.transport.MCPTransport;

public interface MCPClientTransport<AV, ARQ, ARS, M> extends MCPTransport<AV, M> {

	AV connect(Function<ARQ, ARS> requestResponseHandler, Consumer<Throwable> exceptionHandler);

	default AV connect(Function<ARQ, ARS> requestResponseHandler) {
		return connect(requestResponseHandler, null);
	}

}
