/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.command

import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest

/**
 * Represents a command that can be sent to a device.
 *
 * @property type The command type (e.g., POWER_SOCKET_CONTROL)
 * @property key The command key (e.g., START_SECONDS_TO_OVERRIDE)
 * @property toRequest Function that creates a CommandRequest with optional seconds parameter
 */
data class Command(
  val type: String,
  val key: String,
  val toRequest: (seconds: Int?) -> CommandRequest,
)
