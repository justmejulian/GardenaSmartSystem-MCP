package ch.justmejulian.gardena.mcp.util

data class GardenaCredentials(val clientId: String, val clientSecret: String)

data class ApiConfig(val authBaseUrl: String, val apiBaseUrl: String)

/**
 * OAuth protected resource configuration (RFC 9728).
 *
 * @param resourceBaseUrl canonical public base URL of this MCP server — must match exactly in
 *   token audience claims and the protected resource metadata document.
 * @param authServerUrls list of authorization server issuer URLs that are trusted to issue tokens
 *   for this resource.
 */
data class OAuthConfig(val resourceBaseUrl: String, val authServerUrls: List<String>)

enum class EnvVar(val key: String) {
  GARDENA_CLIENT_ID("GARDENA_CLIENT_ID"),
  GARDENA_CLIENT_SECRET("GARDENA_CLIENT_SECRET"),
  GARDENA_AUTH_BASE_URL("GARDENA_AUTH_BASE_URL"),
  GARDENA_API_BASE_URL("GARDENA_API_BASE_URL"),
  MCP_RESOURCE_BASE_URL("MCP_RESOURCE_BASE_URL"),
  MCP_AUTH_SERVER_URL("MCP_AUTH_SERVER_URL"),
}

object Config {
  /**
   * Loads environment variables by name.
   *
   * @param varNames List of environment variable names to load
   * @return Map of variable names to their values
   * @throws IllegalStateException if any variable is not set or is blank
   */
  fun loadEnvVariables(varNames: List<String>): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val missingVars = mutableListOf<String>()

    for (varName in varNames) {
      val value = System.getenv(varName)

      if (value.isNullOrBlank()) {
        missingVars.add(varName)
      } else {
        result[varName] = value
      }
    }

    if (missingVars.isNotEmpty()) {
      throw IllegalStateException(
        "Missing required environment variable(s): ${missingVars.joinToString(", ")}"
      )
    }

    return result
  }

  /**
   * Loads an optional environment variable.
   *
   * @param varName Name of the environment variable
   * @param defaultValue Default value if the variable is not set
   * @return The environment variable value or the default value
   */
  fun loadOptionalEnvVariable(varName: String, defaultValue: String): String {
    return System.getenv(varName)?.takeIf { it.isNotBlank() } ?: defaultValue
  }

  /**
   * Loads Gardena API credentials from environment variables.
   *
   * @return GardenaCredentials
   * @throws IllegalStateException if credentials are not set
   */
  fun loadGardenaCredentials(): GardenaCredentials {
    val envVars =
      loadEnvVariables(listOf(EnvVar.GARDENA_CLIENT_ID.key, EnvVar.GARDENA_CLIENT_SECRET.key))

    return GardenaCredentials(
      clientId = envVars.getValue(EnvVar.GARDENA_CLIENT_ID.key),
      clientSecret = envVars.getValue(EnvVar.GARDENA_CLIENT_SECRET.key),
    )
  }

  /**
   * Loads OAuth protected resource configuration from environment variables.
   *
   * Both variables are optional — returns null when [MCP_RESOURCE_BASE_URL] is not set, which
   * means OAuth metadata endpoints will not be served (appropriate for stdio / local use).
   *
   * @return OAuthConfig, or null if [MCP_RESOURCE_BASE_URL] is not configured
   */
  fun loadOAuthConfig(): OAuthConfig? {
    val resourceBaseUrl = System.getenv(EnvVar.MCP_RESOURCE_BASE_URL.key)?.takeIf { it.isNotBlank() } ?: return null
    val authServerUrl = System.getenv(EnvVar.MCP_AUTH_SERVER_URL.key)?.takeIf { it.isNotBlank() }
    return OAuthConfig(
      resourceBaseUrl = resourceBaseUrl.trimEnd('/'),
      authServerUrls = listOfNotNull(authServerUrl),
    )
  }

  /**
   * Loads API configuration from environment variables with defaults.
   *
   * @return ApiConfig
   */
  fun loadApiConfig(): ApiConfig {
    return ApiConfig(
      authBaseUrl =
        loadOptionalEnvVariable(
          EnvVar.GARDENA_AUTH_BASE_URL.key,
          "https://api.authentication.husqvarnagroup.dev/v1",
        ),
      apiBaseUrl =
        loadOptionalEnvVariable(EnvVar.GARDENA_API_BASE_URL.key, "https://api.smart.gardena.dev/v2"),
    )
  }
}
