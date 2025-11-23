/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.Command as GeneratedCommand
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.ValveCommand as GeneratedValveCommand
import com.gardena.smartgarden.service.iapi.generated.model.ValveCommandAttributes

object ValveCommand {
  private const val TYPE = "VALVE_CONTROL"

  private fun buildRequest(
    commandEnum: ValveCommandAttributes.CommandEnum,
    sec: Int?,
  ): CommandRequest =
    CommandRequest()
      .data(
        GeneratedCommand(
          GeneratedValveCommand()
            .type(GeneratedValveCommand.TypeEnum.VALVE_CONTROL)
            .attributes(
              ValveCommandAttributes().command(commandEnum).apply { sec?.let { this.seconds(it) } }
            )
        )
      )

  private fun createCommand(
    key: String,
    commandEnum: ValveCommandAttributes.CommandEnum,
    defaultSeconds: Int? = null,
  ) = Command(TYPE, key) { seconds -> buildRequest(commandEnum, seconds ?: defaultSeconds) }

  val commands: Map<String, Command> =
    mapOf(
      "START_SECONDS_TO_OVERRIDE" to
        createCommand(
          "START_SECONDS_TO_OVERRIDE",
          ValveCommandAttributes.CommandEnum.START_SECONDS_TO_OVERRIDE,
          3600,
        ),
      "STOP_UNTIL_NEXT_TASK" to
        createCommand(
          "STOP_UNTIL_NEXT_TASK",
          ValveCommandAttributes.CommandEnum.STOP_UNTIL_NEXT_TASK,
        ),
      "PAUSE" to createCommand("PAUSE", ValveCommandAttributes.CommandEnum.PAUSE),
      "UNPAUSE" to createCommand("UNPAUSE", ValveCommandAttributes.CommandEnum.UNPAUSE),
    )
}
