package org.openmcptools.transport;

import java.util.List;

import org.openmcptools.transport.util.GenericTypeRef;

public interface MCPTransport<AV, M> {

	List<String> protocolVersions();
	
	AV sendMessage(M message);

	<T> T unmarshalFrom(Object data, GenericTypeRef<T> unmarshalledTypeRef);

	AV closeGracefully();

	void close();

}
