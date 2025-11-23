package ch.justmejulian.gardena.mcp.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigTest {
  @Test
  fun `loadEnvVariables returns map of existing variables`() {
    // PATH should exist on all systems
    val result = Config.loadEnvVariables(listOf("PATH"))
    assertEquals(System.getenv("PATH"), result["PATH"])
  }

  @Test
  fun `loadEnvVariables throws when variable is missing`() {
    val exception =
      assertFailsWith<IllegalStateException> { Config.loadEnvVariables(listOf("NONEXISTENT_VAR")) }
    assertEquals("Missing required environment variable(s): NONEXISTENT_VAR", exception.message)
  }

  @Test
  fun `loadEnvVariables throws with all missing variables`() {
    val exception =
      assertFailsWith<IllegalStateException> {
        Config.loadEnvVariables(listOf("MISSING_VAR_1", "MISSING_VAR_2"))
      }
    assertEquals(
      "Missing required environment variable(s): MISSING_VAR_1, MISSING_VAR_2",
      exception.message,
    )
  }
}
