package org.openmcptools.transport.server;

import org.openmcptools.transport.MCPSession;

public interface MCPServerSession<AV, AR> extends MCPSession<AV, AR> {
	String getSessionId();
}
