/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

import ch.justmejulian.gardena.mcp.domain.command.Command
import ch.justmejulian.gardena.mcp.domain.command.PowerSocketCommand

/**
 * Power socket device combining COMMON and POWER_SOCKET services. Controls power outlets and tracks
 * duration of operation.
 */
data class PowerSocketDevice(
  override val id: String,
  override val name: String? = null,
  override val batteryLevel: Int? = null,
  override val batteryState: String? = null,
  override val rfLinkLevel: Int? = null,
  override val rfLinkState: String? = null,
  override val serial: String? = null,
  override val modelType: String? = null,
  // POWER_SOCKET service attributes
  val state: String? = null,
  val duration: Int? = null,
) : Device {
  val supportedCommands: Map<String, Command>
    get() = PowerSocketCommand.commands
}
