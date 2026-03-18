package org.openmcptools.transport.server;

import org.openmcptools.transport.MCPSession;

/**
 * Represents a server-side MCP session.
 *
 * @param <AV> Asynchronous Void Type
 * @param <AR> Asynchronous Result Type
 */
public interface MCPServerSession<AV, AR> extends MCPSession<AV, AR> {
	/**
	 * Returns the unique identifier for this session.
	 *
	 * @return the session ID
	 */
	String getSessionId();
}

