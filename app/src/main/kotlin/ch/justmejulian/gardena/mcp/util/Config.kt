package ch.justmejulian.gardena.mcp.util

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
          "Missing required environment variable(s): ${missingVars.joinToString(", ")}")
    }

    return result
  }
}
