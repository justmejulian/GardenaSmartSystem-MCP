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
- GARDENA API credentials (Client ID and Client Secret)

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
5. Copy your **Client ID** and **Client Secret**

## Configuration

### Required Environment Variables

- `GARDENA_CLIENT_ID` - Your GARDENA API Client ID
- `GARDENA_CLIENT_SECRET` - Your GARDENA API Client Secret

### Optional Environment Variables

You can override the default API endpoints for testing or development:

- `GARDENA_AUTH_BASE_URL` - Authentication API base URL

- `GARDENA_API_BASE_URL` - GARDENA Smart System API base URL

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

The application runs as an MCP (Model Context Protocol) server using stdio transport, making it compatible with AI assistants like Claude.

```bash
# Run with your API credentials
GARDENA_CLIENT_ID=your_client_id GARDENA_CLIENT_SECRET=your_client_secret ./gradlew run
```

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
        "GARDENA_CLIENT_SECRET": "your_client_secret_here"
      }
    }
  }
}
```

Replace `/absolute/path/to/GardenaSmartSystem-MCP` with the actual path to this repository on your system, and replace the client ID and secret with your GARDENA API credentials.

**Note**: Using the standalone JAR provides instant startup compared to running via Gradle, which is important for Claude Desktop's connection timeout.
