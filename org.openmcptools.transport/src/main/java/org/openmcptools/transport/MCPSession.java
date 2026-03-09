package org.openmcptools.transport;

import org.openmcptools.transport.util.GenericTypeRef;

public interface MCPSession<AV, AR> {
	
	<T> AR sendRequest(String method, Object requestParams, GenericTypeRef<T> responseTypeRef);

	AV sendNotification(String method, Object params);

	AV closeGracefully();

	void close();
}
