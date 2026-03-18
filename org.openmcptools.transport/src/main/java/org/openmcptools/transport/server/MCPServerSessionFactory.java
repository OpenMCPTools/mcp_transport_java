package org.openmcptools.transport.server;

/**
 * Factory interface for creating {@link MCPServerSession} instances.
 *
 * @param <AV> Asynchronous Void Type
 * @param <AR> Asynchronous Result Type
 * @param <M> Message Type
 */
@FunctionalInterface
public interface MCPServerSessionFactory<AV, AR, M> {

	/**
	 * Creates a new server session using the provided transport.
	 *
	 * @param serverTransport the server transport to use for the session
	 * @return a new server session instance
	 */
	MCPServerSession<AV, AR> create(MCPServerTransport<AV, M> serverTransport);
}
