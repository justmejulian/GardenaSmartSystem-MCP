/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.Command as GeneratedCommand
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketCommand as GeneratedPowerSocketCommand
import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketCommandAttributes

object PowerSocketCommand {
  private fun createCommand(
    commandKey: String,
    commandEnum: PowerSocketCommandAttributes.CommandEnum,
    defaultSeconds: Int? = null,
  ) =
    Command(
      type = "POWER_SOCKET_CONTROL",
      key = commandKey,
      toRequest = { seconds ->
        CommandRequest()
          .data(
            GeneratedCommand(
              GeneratedPowerSocketCommand()
                .type(GeneratedPowerSocketCommand.TypeEnum.POWER_SOCKET_CONTROL)
                .attributes(
                  PowerSocketCommandAttributes().command(commandEnum).apply {
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
          PowerSocketCommandAttributes.CommandEnum.START_SECONDS_TO_OVERRIDE,
          1800,
        ),
      "START_OVERRIDE" to
        createCommand("START_OVERRIDE", PowerSocketCommandAttributes.CommandEnum.START_OVERRIDE),
      "STOP_UNTIL_NEXT_TASK" to
        createCommand(
          "STOP_UNTIL_NEXT_TASK",
          PowerSocketCommandAttributes.CommandEnum.STOP_UNTIL_NEXT_TASK,
        ),
      "PAUSE" to createCommand("PAUSE", PowerSocketCommandAttributes.CommandEnum.PAUSE),
      "UNPAUSE" to createCommand("UNPAUSE", PowerSocketCommandAttributes.CommandEnum.UNPAUSE),
    )
}
