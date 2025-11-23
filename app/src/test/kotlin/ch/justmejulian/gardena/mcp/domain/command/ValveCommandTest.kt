/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.ValveCommand as GeneratedValveCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ValveCommandTest {

  @Test
  fun `commands map contains all expected commands`() {
    val expectedCommands =
      setOf("START_SECONDS_TO_OVERRIDE", "STOP_UNTIL_NEXT_TASK", "PAUSE", "UNPAUSE")

    assertEquals(expectedCommands, ValveCommand.commands.keys)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE command has correct properties`() {
    val command = ValveCommand.commands["START_SECONDS_TO_OVERRIDE"]

    assertNotNull(command)
    assertEquals("VALVE_CONTROL", command.type)
    assertEquals("START_SECONDS_TO_OVERRIDE", command.key)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with default seconds`() {
    val command = ValveCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(null)

    assertNotNull(request.data)
    val valveCommand = request.data.actualInstance as GeneratedValveCommand
    assertEquals(GeneratedValveCommand.TypeEnum.VALVE_CONTROL, valveCommand.type)
    assertEquals(3600, valveCommand.attributes?.seconds)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with custom seconds`() {
    val command = ValveCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(7200)

    assertNotNull(request.data)
    val valveCommand = request.data.actualInstance as GeneratedValveCommand
    assertEquals(7200, valveCommand.attributes?.seconds)
  }

  @Test
  fun `STOP_UNTIL_NEXT_TASK command has correct properties`() {
    val command = ValveCommand.commands["STOP_UNTIL_NEXT_TASK"]

    assertNotNull(command)
    assertEquals("VALVE_CONTROL", command.type)
    assertEquals("STOP_UNTIL_NEXT_TASK", command.key)
  }

  @Test
  fun `PAUSE command has correct properties`() {
    val command = ValveCommand.commands["PAUSE"]

    assertNotNull(command)
    assertEquals("VALVE_CONTROL", command.type)
    assertEquals("PAUSE", command.key)
  }

  @Test
  fun `UNPAUSE command has correct properties`() {
    val command = ValveCommand.commands["UNPAUSE"]

    assertNotNull(command)
    assertEquals("VALVE_CONTROL", command.type)
    assertEquals("UNPAUSE", command.key)
  }
}
