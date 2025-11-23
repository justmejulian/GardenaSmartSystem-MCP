/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.MowerCommand as GeneratedMowerCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MowerCommandTest {

  @Test
  fun `commands map contains all expected commands`() {
    val expectedCommands =
      setOf(
        "START_SECONDS_TO_OVERRIDE",
        "START_DONT_OVERRIDE",
        "PARK_UNTIL_NEXT_TASK",
        "PARK_UNTIL_FURTHER_NOTICE",
      )

    assertEquals(expectedCommands, MowerCommand.commands.keys)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE command has correct properties`() {
    val command = MowerCommand.commands["START_SECONDS_TO_OVERRIDE"]

    assertNotNull(command)
    assertEquals("MOWER_CONTROL", command.type)
    assertEquals("START_SECONDS_TO_OVERRIDE", command.key)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with default seconds`() {
    val command = MowerCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(null)

    assertNotNull(request.data)
    val mowerCommand = request.data.actualInstance as GeneratedMowerCommand
    assertEquals(GeneratedMowerCommand.TypeEnum.MOWER_CONTROL, mowerCommand.type)
    assertEquals(1800, mowerCommand.attributes?.seconds)
  }

  @Test
  fun `START_SECONDS_TO_OVERRIDE creates request with custom seconds`() {
    val command = MowerCommand.commands["START_SECONDS_TO_OVERRIDE"]!!
    val request = command.toRequest(3600)

    assertNotNull(request.data)
    val mowerCommand = request.data.actualInstance as GeneratedMowerCommand
    assertEquals(3600, mowerCommand.attributes?.seconds)
  }

  @Test
  fun `START_DONT_OVERRIDE command has correct properties`() {
    val command = MowerCommand.commands["START_DONT_OVERRIDE"]

    assertNotNull(command)
    assertEquals("MOWER_CONTROL", command.type)
    assertEquals("START_DONT_OVERRIDE", command.key)
  }

  @Test
  fun `PARK_UNTIL_NEXT_TASK command has correct properties`() {
    val command = MowerCommand.commands["PARK_UNTIL_NEXT_TASK"]

    assertNotNull(command)
    assertEquals("MOWER_CONTROL", command.type)
    assertEquals("PARK_UNTIL_NEXT_TASK", command.key)
  }

  @Test
  fun `PARK_UNTIL_FURTHER_NOTICE command has correct properties`() {
    val command = MowerCommand.commands["PARK_UNTIL_FURTHER_NOTICE"]

    assertNotNull(command)
    assertEquals("MOWER_CONTROL", command.type)
    assertEquals("PARK_UNTIL_FURTHER_NOTICE", command.key)
  }
}
