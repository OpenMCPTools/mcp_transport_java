# MCP Transport

A lightweight, transport-agnostic Java abstraction for the **Model Context Protocol (MCP)**.

## Overview

Model Context Protocol (MCP) is an open standard for connecting AI models to external tools and data. This project provides the core Java interfaces and utilities required to implement MCP Clients and Servers. It is designed to be highly flexible, supporting various asynchronous frameworks (Project Reactor, CompletableFuture) and transport mechanisms (Stdio, SSE, WebSockets).

## Project Structure

- `org.openmcptools.transport`: Core interfaces (`MCPTransport`, `MCPSession`).
- `org.openmcptools.transport.client`: Client-specific connection logic.
- `org.openmcptools.transport.server`: Server-specific session management and factories.
- `org.openmcptools.transport.util`: Utilities for type-safe data unmarshalling.

## Key Features

- **Protocol Agnostic**: Build tools that work over Standard I/O, HTTP/SSE, or any custom stream.
- **Async & Reactive**: Fully generic design compatible with `java.util.concurrent` or `io.projectreactor`.
- **Type Safe**: Integrated unmarshalling support using `GenericTypeRef` to preserve complex generic types.
- **OSGi Ready**: Includes metadata for OSGi container environments.

## Usage Example

### 1. Define a Transport
You would typically use an implementation like a `StdioTransport` (not shown here) which implements the `MCPClientTransport` interface.

### 2. Create a Session
```java
// AV = Mono<Void>, AR = Mono
GenericTypeRef<List<String>> listType = new GenericTypeRef<>() {};

MCPSession<Mono<Void>, Mono<?>> session = ...;

// Sending a notification
session.sendNotification("initialized", null);

// Sending a request and unmarshalling the response
Mono<List<String>> tools = session.sendRequest("list_tools", null, listType);
```

## License

Copyright 2024-2025 the original author or authors. Licensed under the Apache License, Version 2.0.

