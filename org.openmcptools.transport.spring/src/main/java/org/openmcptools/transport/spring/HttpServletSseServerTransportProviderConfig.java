package org.openmcptools.transport.spring;

import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import jakarta.servlet.http.HttpServletRequest;

public class HttpServletSseServerTransportProviderConfig {

	public static final String SERVER_TRANSPORT_FACTORY_NAME = "HttpServletSseServerTransportProviderFactory";
	public static final String SERVER_CF_TARGET = "(component.factory=" + SERVER_TRANSPORT_FACTORY_NAME + ")";

	public static final String SERVER_TRANSPORT_FACTORY_CONFIG = HttpServletSseServerTransportProviderConfig.class
			+ ".config";

	private McpJsonMapper jsonMapper;
	private String baseUrl = "";
	private String messageEndpoint;
	private String sseEndpoint = "/sse";
	private McpTransportContextExtractor<HttpServletRequest> contextExtractor = (
			serverRequest) -> McpTransportContext.EMPTY;
	private Duration keepAliveInterval;
	private ServerTransportSecurityValidator securityValidator = ServerTransportSecurityValidator.NOOP;

	public HttpServletSseServerTransportProviderConfig(McpJsonMapper jsonMapper, String messageEndpoint) {
		Objects.requireNonNull(jsonMapper, "jsonMapper must not be null");
		this.jsonMapper = jsonMapper;
		Objects.requireNonNull(messageEndpoint, "messageEndpoint must not be null");
		this.messageEndpoint = messageEndpoint;
	}

	public HttpServletSseServerTransportProviderConfig setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public HttpServletSseServerTransportProviderConfig setSseEndpoint(String sseEndpoint) {
		this.sseEndpoint = sseEndpoint;
		return this;
	}

	public HttpServletSseServerTransportProviderConfig setContextExtractor(
			McpTransportContextExtractor<HttpServletRequest> contextExtractor) {
		this.contextExtractor = contextExtractor;
		return this;
	}

	public HttpServletSseServerTransportProviderConfig setKeepAliveInterval(Duration keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
		return this;
	}

	public HttpServletSseServerTransportProviderConfig setSecurityValidator(
			ServerTransportSecurityValidator securityValidator) {
		this.securityValidator = securityValidator;
		return this;
	}

	public Dictionary<String, Object> asProperties() {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(SERVER_TRANSPORT_FACTORY_CONFIG, this);
		return props;
	}

	public static HttpServletSseServerTransportProviderConfig fromProperties(Map<String, Object> properties) {
		return (HttpServletSseServerTransportProviderConfig) properties.get(SERVER_TRANSPORT_FACTORY_CONFIG);
	}

	public McpJsonMapper getJsonMapper() {
		return jsonMapper;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getMessageEndpoint() {
		return messageEndpoint;
	}

	public String getSseEndpoint() {
		return sseEndpoint;
	}

	public McpTransportContextExtractor<HttpServletRequest> getContextExtractor() {
		return contextExtractor;
	}

	public Duration getKeepAliveInterval() {
		return keepAliveInterval;
	}

	public ServerTransportSecurityValidator getSecurityValidator() {
		return securityValidator;
	}

}
