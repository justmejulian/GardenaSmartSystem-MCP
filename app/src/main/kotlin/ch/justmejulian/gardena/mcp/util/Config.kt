package ch.justmejulian.gardena.mcp.util

data class GardenaCredentials(val clientId: String, val clientSecret: String)

data class ApiConfig(val authBaseUrl: String)

enum class EnvVar(val key: String) {
  GARDENA_CLIENT_ID("GARDENA_CLIENT_ID"),
  GARDENA_CLIENT_SECRET("GARDENA_CLIENT_SECRET"),
  GARDENA_AUTH_BASE_URL("GARDENA_AUTH_BASE_URL"),
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
    )
  }
}
