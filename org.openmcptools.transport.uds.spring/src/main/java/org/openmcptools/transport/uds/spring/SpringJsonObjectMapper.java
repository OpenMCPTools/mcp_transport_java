package org.openmcptools.transport.uds.spring;

import java.io.IOException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;

@Component(immediate=true, service=SpringJsonObjectMapper.class)
public class SpringJsonObjectMapper {

	private final McpJsonMapper mcpJsonMapper;
	
	@Activate
	public SpringJsonObjectMapper() {
		this.mcpJsonMapper = McpJsonDefaults.getDefaultMcpJsonMapper();
	}
	
	public <T> T readValue(String content, Class<T> type) throws IOException {
		return mcpJsonMapper.readValue(content, type);
	}

	public String writeValueAsString(Object value) throws IOException {
		return mcpJsonMapper.writeValueAsString(value);
	}

	public JSONRPCMessage deserializeJsonRpcMessage(String data) throws IOException {
		return McpSchema.deserializeJsonRpcMessage(this.mcpJsonMapper, data);
	}

	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return mcpJsonMapper.convertValue(data, typeRef);
	}

}
