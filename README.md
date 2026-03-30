# GardenaSmartSystem-MCP

**Note**: This is a proof of concept MCP server implementation.

This Model Context Protocol (MCP) server provides integration with the GARDENA smart system API, allowing you to monitor and control your smart garden devices including mowers, irrigation systems, sensors, and more.

This implementation follows the guidance from the [Model Context Protocol documentation for connecting local servers](https://modelcontextprotocol.io/docs/develop/connect-local-servers#none-of-this-is-working-what-do-i-do).

### GARDENA smart system API

This API allows you to monitor and control your smart garden devices.

[View Documentation](https://developer.husqvarnagroup.cloud/apis/gardena-smart-system-api)

## Prerequisites

- Java 21
- Gradle 9.2.0 (included via wrapper)
- A valid OAuth bearer token for the GARDENA API and your API key (Client ID)

### Verify Java Installation

```bash
java -version
# Expected output:
openjdk version "21.0.8" 2025-07-15 LTS
OpenJDK Runtime Environment Microsoft-11933201 (build 21.0.8+9-LTS)
OpenJDK 64-Bit Server VM Microsoft-11933201 (build 21.0.8+9-LTS, mixed mode, sharing)
```

### Getting API Credentials

1. Visit https://developer.husqvarnagroup.cloud/
2. Sign up or log in
3. Go to "Applications" → "Create Application"
4. Fill in the application details
5. Complete the OAuth flow externally to obtain a **Bearer Token**
6. Copy your **Client ID** — this is used as the API key (`X-Api-Key` header)

## Configuration

### Required Environment Variables

- `GARDENA_CLIENT_ID` - Your GARDENA API Client ID (used as the `X-Api-Key` header; will be replaced by a dedicated API key later)
- `GARDENA_BEARER_TOKEN` - Your OAuth bearer token (obtained externally via the OAuth flow)

### Optional Environment Variables

You can override the default API endpoints for testing or development:

- `GARDENA_API_BASE_URL` - GARDENA Smart System API base URL

#### HTTP transport / OAuth (only relevant when running with `--transport sse`)

- `MCP_RESOURCE_BASE_URL` - Canonical public base URL of this MCP server (e.g. `https://mcp.example.com`). When set, the server exposes the OAuth protected resource metadata endpoint and requires an `Authorization` header on all requests except that endpoint. Must match exactly what clients will use as the token audience — no trailing slash.
- `MCP_AUTH_SERVER_URL` - Authorization server issuer URL (e.g. `https://auth.example.com`). Included in the protected resource metadata so clients know where to obtain tokens.

## Building the Project

```bash
# Build the project
./gradlew build

# Clean build artifacts
./gradlew clean
```

### Code Formatting

This project uses [ktfmt](https://facebook.github.io/ktfmt/) with Google style for consistent Kotlin code formatting.

```bash
# Format all Kotlin source files
./gradlew ktfmtFormat

# Check if code is properly formatted
./gradlew ktfmtCheck
```
### Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests AppTest
```

## Running the MCP Server

The server supports two transports selected with the `--transport` flag. Defaults to `stdio`.

### stdio (default)

Compatible with AI assistants like Claude Desktop that spawn the server as a local process.

```bash
GARDENA_CLIENT_ID=your_client_id GARDENA_BEARER_TOKEN=your_bearer_token ./gradlew run
```

### HTTP

Starts an embedded HTTP server. The MCP endpoint is at `/mcp` (streamable HTTP transport).

```bash
GARDENA_CLIENT_ID=your_client_id \
  GARDENA_BEARER_TOKEN=your_bearer_token \
  ./gradlew run --args="--transport sse --port 3000"
```

Use `--port` to override the default port of `3000`.

#### Running with OAuth protection

Set `MCP_RESOURCE_BASE_URL` to enable OAuth protected resource metadata. The server will then:

1. Serve `GET /.well-known/oauth-protected-resource` (publicly, no auth required) — returns the RFC 9728 metadata document pointing at your authorization server.
2. Reject every other request that has no `Authorization` header with `401 Unauthorized` and a `WWW-Authenticate` challenge header.

```bash
GARDENA_CLIENT_ID=your_client_id \
  GARDENA_BEARER_TOKEN=your_bearer_token \
  MCP_RESOURCE_BASE_URL=https://mcp.example.com \
  MCP_AUTH_SERVER_URL=https://auth.example.com \
  java -jar app/build/libs/app-all.jar --transport sse --port 3000
```

Example metadata response:

```json
{
  "resource": "https://mcp.example.com",
  "authorization_servers": ["https://auth.example.com"]
}
```

> **HTTPS required** — deploy behind TLS in production. The canonical `MCP_RESOURCE_BASE_URL` must match exactly what clients use as the token audience; even a trailing slash difference will cause validation failures.

### Claude Desktop Configuration

To use this MCP server with Claude Desktop, you first need to build the standalone JAR:

```bash
./gradlew build
```

This creates a standalone JAR at `app/build/libs/app-all.jar` that includes all dependencies.

Then add the following configuration to your Claude Desktop config file:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`  
**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "gardena-smart-system": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/GardenaSmartSystem-MCP/app/build/libs/app-all.jar"
      ],
      "env": {
        "GARDENA_CLIENT_ID": "your_client_id_here",
        "GARDENA_BEARER_TOKEN": "your_bearer_token_here"
      }
    }
  }
}
```

Replace `/absolute/path/to/GardenaSmartSystem-MCP` with the actual path to this repository on your system, and replace the bearer token and client ID with your GARDENA API credentials.

**Note**: Using the standalone JAR provides instant startup compared to running via Gradle, which is important for Claude Desktop's connection timeout.
