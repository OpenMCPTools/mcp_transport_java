package org.openmcptools.transport.server;

@FunctionalInterface
public interface MCPServerSessionFactory<AV, AR, M> {

	MCPServerSession<AV, AR> create(MCPServerTransport<AV, M> serverTransport);
}
