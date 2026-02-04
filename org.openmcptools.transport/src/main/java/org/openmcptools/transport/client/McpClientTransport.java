package org.openmcptools.transport.client;

import java.util.function.Consumer;
import java.util.function.Function;

import org.openmcptools.transport.McpTransport;

public interface McpClientTransport<A, M, T, R> extends McpTransport<A, M> {

	A connect(Function<T, R> handler);

	default void setExceptionHandler(Consumer<Throwable> handler) {
	}

}
