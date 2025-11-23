/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.Command as GeneratedCommand
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.ValveSetCommand as GeneratedValveSetCommand
import com.gardena.smartgarden.service.iapi.generated.model.ValveSetCommandAttributes

object ValveSetCommand {
  private const val TYPE = "VALVE_SET_CONTROL"

  private fun buildRequest(
    commandEnum: ValveSetCommandAttributes.CommandEnum,
    sec: Int?,
  ): CommandRequest =
    CommandRequest()
      .data(
        GeneratedCommand(
          GeneratedValveSetCommand()
            .type(GeneratedValveSetCommand.TypeEnum.VALVE_SET_CONTROL)
            .attributes(ValveSetCommandAttributes().command(commandEnum))
        )
      )

  private fun createCommand(
    key: String,
    commandEnum: ValveSetCommandAttributes.CommandEnum,
    defaultSeconds: Int? = null,
  ) = Command(TYPE, key) { seconds -> buildRequest(commandEnum, seconds ?: defaultSeconds) }

  val commands: Map<String, Command> =
    mapOf(
      "STOP_UNTIL_NEXT_TASK" to
        createCommand(
          "STOP_UNTIL_NEXT_TASK",
          ValveSetCommandAttributes.CommandEnum.STOP_UNTIL_NEXT_TASK,
        )
    )
}
