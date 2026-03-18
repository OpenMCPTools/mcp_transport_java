package org.openmcptools.transport.server;

import org.openmcptools.transport.MCPTransport;

/**
 * Interface for server-side MCP transport.
 *
 * @param <AV> Asynchronous Void Type
 * @param <M> Message Type
 */
public interface MCPServerTransport<AV, M> extends MCPTransport<AV, M> {
}
