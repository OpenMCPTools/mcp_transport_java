package org.openmcptools.transport.server;

public interface McpServerSessionFactory<S, T> {

	S create(T sessionTransport);

}
