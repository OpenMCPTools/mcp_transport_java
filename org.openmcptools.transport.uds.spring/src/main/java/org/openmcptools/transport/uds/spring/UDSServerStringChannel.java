package org.openmcptools.transport.uds.spring;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import org.eclipse.ecf.ai.mcp.transports.ServerStringChannel;

public class UDSServerStringChannel extends ServerStringChannel {

	public UDSServerStringChannel(Selector selector, int incomingBufferSize, ExecutorService executor) {
		super(selector, incomingBufferSize, executor);
	}

	public void start(UnixDomainSocketAddress address, IOConsumer<SocketChannel> acceptHandler,
			IOConsumer<String> readHandler) throws IOException {
		super.start(StandardProtocolFamily.UNIX, address, acceptHandler, readHandler);
	}

	public boolean isClientConnected() {
		return this.acceptedClient != null;
	}

}
