/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.ValveSetCommand as GeneratedValveSetCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ValveSetCommandTest {

  @Test
  fun `commands map contains all expected commands`() {
    val expectedCommands = setOf("STOP_UNTIL_NEXT_TASK")

    assertEquals(expectedCommands, ValveSetCommand.commands.keys)
  }

  @Test
  fun `STOP_UNTIL_NEXT_TASK command has correct properties`() {
    val command = ValveSetCommand.commands["STOP_UNTIL_NEXT_TASK"]

    assertNotNull(command)
    assertEquals("VALVE_SET_CONTROL", command.type)
    assertEquals("STOP_UNTIL_NEXT_TASK", command.key)
  }

  @Test
  fun `STOP_UNTIL_NEXT_TASK creates valid request`() {
    val command = ValveSetCommand.commands["STOP_UNTIL_NEXT_TASK"]!!
    val request = command.toRequest(null)

    assertNotNull(request.data)
    val valveSetCommand = request.data.actualInstance as GeneratedValveSetCommand
    assertEquals(GeneratedValveSetCommand.TypeEnum.VALVE_SET_CONTROL, valveSetCommand.type)
  }
}
