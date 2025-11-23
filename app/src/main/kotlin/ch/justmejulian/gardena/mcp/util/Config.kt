package ch.justmejulian.gardena.mcp.util

data class GardenaCredentials(val clientId: String, val clientSecret: String)

enum class EnvVar(val key: String) {
  GARDENA_CLIENT_ID("GARDENA_CLIENT_ID"),
  GARDENA_CLIENT_SECRET("GARDENA_CLIENT_SECRET"),
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
   * Loads Gardena API credentials from environment variables.
   *
   * @return GardenaCredentials
   * @throws IllegalStateException if credentials are not set
   */
  fun loadGardenaCredentials(): GardenaCredentials {
    val envVars = loadEnvVariables(EnvVar.entries.map { it.key })

    return GardenaCredentials(
      clientId = envVars.getValue(EnvVar.GARDENA_CLIENT_ID.key),
      clientSecret = envVars.getValue(EnvVar.GARDENA_CLIENT_SECRET.key),
    )
  }
}
