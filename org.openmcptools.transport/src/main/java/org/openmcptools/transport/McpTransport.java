package org.openmcptools.transport;

import java.lang.reflect.Type;
import java.util.List;

public interface McpTransport<A, M> {

	void close();

	A closeGracefully();

	A sendMessage(M message);

	<T> T unmarshalFrom(Object data, Type type);

	List<String> protocolVersions();
}
