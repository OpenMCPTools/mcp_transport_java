package org.openmcptools.transport.uds.flow;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDSMcpTransportConfig {

	public static final int DEFAULT_INCOMING_BUFFER_SIZE = 4096;

	public static final String TARGET_SOCKET_PATH_PROP = "udsTargetSocketPath";
	public static final String INCOMING_BUFFER_SIZE_PROP = "udsIncomingBufferSize";
	public static final String SELECTOR_PROP = "udsSelector";
	public static final String EXECUTOR_SERVICE_PROP = "udsExecutorService";

	public static final String SERVER_TRANSPORT_FACTORY_NAME = "UDSMcpServerTransportProviderFactory";

	public static final String CLIENT_TRANSPORT_FACTORY_NAME = "UDSMcpClientTransportProviderFactory";

	public static final String SERVER_CF_TARGET = "(component.factory=" + SERVER_TRANSPORT_FACTORY_NAME + ")";
	public static final String CLIENT_CF_TARGET = "(component.factory=" + CLIENT_TRANSPORT_FACTORY_NAME + ")";

	public static Path getCurrentDir() {
		return Paths.get("");
	}

	public static Path getParentDir() {
		return Paths.get("..");
	}

	public static void deleteIfExists(File f) {
		if (f.exists()) {
			f.delete();
		}
	}

	public static void deleteOnExit(File f) {
		f.deleteOnExit();
	}

	public static Path getFilePath(Path dir, String fileName) {
		return dir.resolve(fileName);
	}

	protected Path targetSocketPath;
	protected int incomingBufferSize = DEFAULT_INCOMING_BUFFER_SIZE;
	protected Selector selector;
	protected ExecutorService executorService;

	public UDSMcpTransportConfig(Map<String, ?> properties) {
		Path path = (Path) properties.get(TARGET_SOCKET_PATH_PROP);
		this.targetSocketPath = path;
		Integer bufSize = (Integer) properties.get(INCOMING_BUFFER_SIZE_PROP);
		if (bufSize != null) {
			this.incomingBufferSize = bufSize;
		}
		this.selector = (Selector) properties.get(SELECTOR_PROP);
		this.executorService = (ExecutorService) properties.get(EXECUTOR_SERVICE_PROP);
	}

	public UDSMcpTransportConfig(Path targetSocketPath, int incomingBufferSize, Selector selector,
			ExecutorService executorService) {
		this.targetSocketPath = targetSocketPath;
		this.incomingBufferSize = incomingBufferSize;
		this.selector = selector;
		this.executorService = executorService;
	}

	public UDSMcpTransportConfig(Path targetSocketPath) {
		this(targetSocketPath, DEFAULT_INCOMING_BUFFER_SIZE, null, null);
	}

	public int getIncomingBufferSize() {
		return incomingBufferSize;
	}

	public ExecutorService getExecutorService() {
		return this.executorService == null ? Executors.newCachedThreadPool() : this.executorService;
	}

	public Selector getSelector() {
		try {
			return this.selector == null ? Selector.open() : this.selector;
		} catch (IOException e) {
			throw new RuntimeException("Cannot open new selector", e);
		}
	}

	public Path getTargetSocketPath() {
		return targetSocketPath;
	}

	public Dictionary<String, Object> asProperties() {
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(TARGET_SOCKET_PATH_PROP, getTargetSocketPath());
		properties.put(INCOMING_BUFFER_SIZE_PROP, getIncomingBufferSize());
		properties.put(SELECTOR_PROP, getSelector());
		properties.put(EXECUTOR_SERVICE_PROP, getExecutorService());
		return properties;
	}

}
