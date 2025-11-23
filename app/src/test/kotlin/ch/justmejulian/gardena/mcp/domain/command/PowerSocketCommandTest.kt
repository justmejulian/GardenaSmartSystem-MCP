/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketCommand as GeneratedPowerSocketCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PowerSocketCommandTest {

  @Test
  fun `commands map contains all expected commands`() {
    val expectedCommands =
      setOf(
        "START_SECONDS_TO_OVERRIDE",
        "START_OVERRIDE",
        "STOP_UNTIL_NEXT_TASK",
        "PAUSE",
        "UNPAUSE",
      )

    assertEquals(expectedCommands, PowerSocketCommand.commands.keys)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE command has correct properties`() {
    val command = PowerSocketCommand.commands["START_SECONDS_TO_OVERRIDE"]

    assertNotNull(command)
    assertEquals("POWER_SOCKET_CONTROL", command.type)
    assertEquals("START_SECONDS_TO_OVERRIDE", command.key)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with default seconds`() {
    val command = PowerSocketCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(null)

    assertNotNull(request.data)
    val powerSocketCommand = request.data.actualInstance as GeneratedPowerSocketCommand
    assertEquals(GeneratedPowerSocketCommand.TypeEnum.POWER_SOCKET_CONTROL, powerSocketCommand.type)
    assertEquals(1800, powerSocketCommand.attributes?.seconds)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with custom seconds`() {
    val command = PowerSocketCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(3600)

    assertNotNull(request.data)
    val powerSocketCommand = request.data.actualInstance as GeneratedPowerSocketCommand
    assertEquals(3600, powerSocketCommand.attributes?.seconds)
  }

  @Test
  fun `START_OVERRIDE command has correct properties`() {
    val command = PowerSocketCommand.commands["START_OVERRIDE"]

    assertNotNull(command)
    assertEquals("POWER_SOCKET_CONTROL", command.type)
    assertEquals("START_OVERRIDE", command.key)
  }

  @Test
  fun `STOP_UNTIL_NEXT_TASK command has correct properties`() {
    val command = PowerSocketCommand.commands["STOP_UNTIL_NEXT_TASK"]

    assertNotNull(command)
    assertEquals("POWER_SOCKET_CONTROL", command.type)
    assertEquals("STOP_UNTIL_NEXT_TASK", command.key)
  }

  @Test
  fun `PAUSE command has correct properties`() {
    val command = PowerSocketCommand.commands["PAUSE"]

    assertNotNull(command)
    assertEquals("POWER_SOCKET_CONTROL", command.type)
    assertEquals("PAUSE", command.key)
  }

  @Test
  fun `UNPAUSE command has correct properties`() {
    val command = PowerSocketCommand.commands["UNPAUSE"]

    assertNotNull(command)
    assertEquals("POWER_SOCKET_CONTROL", command.type)
    assertEquals("UNPAUSE", command.key)
  }
}
