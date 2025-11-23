/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

import ch.justmejulian.gardena.mcp.domain.command.Command

/**
 * Base sealed interface for all device types in the Gardena Smart System. Each device type combines
 * multiple services to provide a unified view of the device.
 */
sealed interface Device {
  val id: String
  val name: String?
  val batteryLevel: Int?
  val batteryState: String?
  val rfLinkLevel: Int?
  val rfLinkState: String?
  val serial: String?
  val modelType: String?
  val supportedCommands: Map<String, Command>
}
