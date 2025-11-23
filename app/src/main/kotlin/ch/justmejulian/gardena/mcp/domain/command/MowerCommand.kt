/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.Command as GeneratedCommand
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.MowerCommand as GeneratedMowerCommand
import com.gardena.smartgarden.service.iapi.generated.model.MowerCommandAttributes

object MowerCommand {
  private fun createCommand(
    commandKey: String,
    commandEnum: MowerCommandAttributes.CommandEnum,
    defaultSeconds: Int? = null,
  ) =
    Command(
      type = "MOWER_CONTROL",
      key = commandKey,
      toRequest = { seconds ->
        CommandRequest()
          .data(
            GeneratedCommand(
              GeneratedMowerCommand()
                .type(GeneratedMowerCommand.TypeEnum.MOWER_CONTROL)
                .attributes(
                  MowerCommandAttributes().command(commandEnum).apply {
                    if (defaultSeconds != null) seconds(seconds ?: defaultSeconds)
                  }
                )
            )
          )
      },
    )

  val commands: Map<String, Command> =
    mapOf(
      "START_SECONDS_TO_OVERRIDE" to
        createCommand(
          "START_SECONDS_TO_OVERRIDE",
          MowerCommandAttributes.CommandEnum.START_SECONDS_TO_OVERRIDE,
          1800,
        ),
      "START_DONT_OVERRIDE" to
        createCommand(
          "START_DONT_OVERRIDE",
          MowerCommandAttributes.CommandEnum.START_DONT_OVERRIDE,
        ),
      "PARK_UNTIL_NEXT_TASK" to
        createCommand(
          "PARK_UNTIL_NEXT_TASK",
          MowerCommandAttributes.CommandEnum.PARK_UNTIL_NEXT_TASK,
        ),
      "PARK_UNTIL_FURTHER_NOTICE" to
        createCommand(
          "PARK_UNTIL_FURTHER_NOTICE",
          MowerCommandAttributes.CommandEnum.PARK_UNTIL_FURTHER_NOTICE,
        ),
    )
}
