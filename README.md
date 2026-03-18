# mcp_transport_java - MCP Java Transport API

The current [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) has a [transport API](https://java.sdk.modelcontextprotocol.io/latest-snapshot/#architecture), allowing servers and clients to replace
one of the default transports (stdio, SSE, etc) with their own, alternative transports.

The API in this repo provides a generatlization of the Java SDK's transport API.  This API uses Java Generics to allow the use of alternative reactive libraries (e.g. Mutiny) for asynchronous communication at the transport layer. 

This repo also has a implementation based upon Unix Domain Sockets (UDS).  This provides a local-only transport for secure communication between client and server and is implemented for the [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) version 0.18.1 and above.

## Projects

### Transport API

[org.openmcptools.transport](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport) - The OpenMCPTools transport API

### Unix Domain Sockets Implementation (local only)

This UDS implementation supports long-running servers, allowing clients to disconnect and reconnect without restarting the server (as MCP stdio currently requires).  This ability for long-running makes it easier to run, test and debug long-running MCP servers.

[org.openmcptool.transport.uds](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport.uds) - Unix Domain Socket (UDS) Support Classes

[org.openmcptools.transport.uds.spring](https://github.com/OpenMCPTools/mcp_transport_java/tree/main/org.openmcptools.transport.uds.spring) - Unix Domain Socket Implementation for [Java SDK](https://github.com/modelcontextprotocol/java-sdk)


