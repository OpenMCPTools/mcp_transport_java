package org.openmcptools.transport;

import java.util.List;

public interface McpTransport<A, M> {

	void close();

	A closeGracefully();

	A sendMessage(M message);

	<T> T unmarshalFrom(Object data, GenericTypeRef<T> typeRef);

	List<String> protocolVersions();
}
