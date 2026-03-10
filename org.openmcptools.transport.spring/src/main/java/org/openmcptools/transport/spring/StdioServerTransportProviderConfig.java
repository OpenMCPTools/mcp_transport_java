package org.openmcptools.transport.spring;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

public class StdioServerTransportProviderConfig {

	public static final String SERVER_TRANSPORT_FACTORY_NAME = "StdioServerTransportProviderFactory";
	public static final String SERVER_CF_TARGET = "(component.factory=" + SERVER_TRANSPORT_FACTORY_NAME + ")";

	public static final String SERVER_TRANSPORT_FACTORY_CONFIG = StdioServerTransportProviderConfig.class + ".config";
	
	private final InputStream ins;
	private final OutputStream outs;
	
	public StdioServerTransportProviderConfig(InputStream ins, OutputStream outs) {
		Objects.requireNonNull(ins, "InputStream must not be null");
		Objects.requireNonNull(outs, "OutputStream must not be null");
		this.ins = ins;
		this.outs = outs;
	}
	
	public InputStream getInputStream() {
		return ins;
	}
	
	public OutputStream getOutputStream() {
		return outs;
	}
	
	public Dictionary<String, Object> asProperties() {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(SERVER_TRANSPORT_FACTORY_CONFIG, this);
		return props;
	}
}
