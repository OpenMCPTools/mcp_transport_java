# MCP Transport

A lightweight, transport-agnostic Java abstraction for the **Model Context Protocol (MCP)**.

## Overview

Model Context Protocol (MCP) is an open standard for connecting AI models to external tools and data. This project provides the core Java interfaces and utilities required to implement MCP Clients and Servers. It is designed to be highly flexible, supporting various asynchronous frameworks (Project Reactor, CompletableFuture, Project Mutiny) and transport mechanisms (Stdio, SSE, WebSockets).

## Package Structure

- `org.openmcptools.transport`: Core interfacesAPI  (`MCPTransport`, `MCPSession`).
- `org.openmcptools.transport.client`: Client-specific connection logic.
- `org.openmcptools.transport.server`: Server-specific session management and factories.
- `org.openmcptools.transport.util`: Utilities for type-safe data unmarshalling.

## Key Features

- **Protocol Agnostic**: Build tools that work over Standard I/O, HTTP/SSE, or any custom stream.
- **Async & Reactive Framework Agnostic**: Fully generic design compatible with `java.util.concurrent`, `io.projectreactor`, `io.smallrye.mutiny` or other reactive frameworks.
- **OSGi Ready**: Includes metadata for OSGi container environments.

## Implementations

MCP Java SDK (Spring) - In org.openmcptools.transport.spring and (UDS) org.openmcptools.transport.spring.uds

## License

Copyright 2024-2025 the original author or authors. Licensed under the Apache License, Version 2.0.

