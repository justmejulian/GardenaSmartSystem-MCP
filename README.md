# GardenaSmartSystem-MCP

This Model Context Protocol (MCP) server provides integration with the GARDENA smart system API, allowing you to monitor and control your smart garden devices including mowers, irrigation systems, sensors, and more.

### GARDENA smart system AP

This API allows you to monitor and control your smart garden devices.

[View Documentation](https://developer.husqvarnagroup.cloud/apis/gardena-smart-system-api)

## Prerequisites

- Java 25 or higher
- Gradle 9.2.0 (included via wrapper)

### Verify Java Installation

```bash
java -version
# Expected output:
# openjdk version "25.0.1" 2025-10-21
# OpenJDK Runtime Environment (build 25.0.1+8-27)
# OpenJDK 64-Bit Server VM (build 25.0.1+8-27, mixed mode, sharing)
```

## Building the Project

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```
