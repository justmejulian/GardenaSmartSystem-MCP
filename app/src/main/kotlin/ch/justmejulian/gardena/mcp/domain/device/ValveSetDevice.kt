/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

import ch.justmejulian.gardena.mcp.domain.command.Command
import ch.justmejulian.gardena.mcp.domain.command.ValveCommand
import ch.justmejulian.gardena.mcp.domain.command.ValveSetCommand

/**
 * Valve device representing a single valve in a valve set. Contains valve-specific attributes like
 * state.
 */
data class Valve(
  val id: String,
  val name: String? = null,
  val state: String? = null,
  val activity: String? = null,
) {
  val supportedCommands: Map<String, Command>
    get() = ValveCommand.commands
}

/**
 * Valve set device combining VALVE_SET, COMMON, and multiple VALVE services. Manages irrigation
 * control with multiple individual valves.
 */
data class ValveSetDevice(
  override val id: String,
  override val name: String? = null,
  override val batteryLevel: Int? = null,
  override val batteryState: String? = null,
  override val rfLinkLevel: Int? = null,
  override val rfLinkState: String? = null,
  override val serial: String? = null,
  override val modelType: String? = null,
  // VALVE_SET service attributes
  val valveSetState: String? = null,
  // List of individual valves
  val valves: List<Valve> = emptyList(),
) : Device {
  val supportedCommands: Map<String, Command>
    get() = ValveSetCommand.commands
}
