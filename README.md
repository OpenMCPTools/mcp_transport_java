# mcp_transport_java - MCP Java Transport API

The current [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) has a [transport API](https://java.sdk.modelcontextprotocol.io/latest-snapshot/#architecture), allowing servers and clients to replace
one of the default transports (stdio, SSE, etc) with their own, alternative transports.

The API in this repo provides a generatlization of the SDK's transport API.  This API uses Java Generics to allow the use of alternative libraries for asynchronous communication at the transport layer. 

This API could be contributed back to the Java SDK in the form of super interfaces/classes.

This repo also has an implementation based upon Unix Domain Sockets (UDS).  This provides a local-only transport for secure communication between client and server.

## Projects

### API

[org.openmcptools.transport](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport.uds) - The OpenMCPTools transport API

### Unix Domain Sockets

[org.openmcptool.transport.uds](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport.uds) - Unix Domain Socket (UDS) Support Classes

[org.openmcptools.transport.uds.spring](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport.uds.spring) - Unix Domain Socket Implementation for [Java SDK](https://github.com/modelcontextprotocol/java-sdk)


