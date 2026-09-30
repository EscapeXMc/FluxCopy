# FluxCopy

![Paper](https://img.shields.io/badge/Paper-1.21.11-green)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

**Just do copy while Flexing, Only with Flux**

FluxCopy is a lightweight Paper 1.21.11 plugin that lets you copy the MOTD and server icon from any Minecraft server with a single command and a simple confirmation GUI.

---

## Features

- **One-Command Copy**: `/copy <server-ip> [port]` fetches MOTD and icon from any server
- **Smart GUI**: 4-button confirmation screen with separate Yes/No options for MOTD and icon
- **Live SLP Ping**: Real-time server status ping to fetch the latest MOTD and favicon
- **Instant Apply**: Automatically updates `server.properties` and saves `server-icon.png`
- **Port Support**: Works with both direct IPs and IP:port combos
- **Clean Tagline**: *Just do copy while Flexing, Only with Flux*

---

## Installation

1. Download the latest `FluxCopy-1.0.0.jar`
2. Place it in your Paper server's `plugins/` folder
3. Restart the server

---

## Usage

### Command

```
/copy <server-ip> [port]
```

**Examples:**
```
/copy play.applemc
/copy play.applemc 25565
/copy mc.hypixel.net 25565
```

### Confirmation GUI

After pinging the target server, a GUI opens with 4 buttons:

| Slot | Button | Action |
|------|--------|--------|
| 2 | **Yes - Copy MOTD** | Copies the MOTD from the target server |
| 3 | **No - Skip MOTD** | Leaves your current MOTD unchanged |
| 5 | **Yes - Copy Icon** | Downloads and applies the server icon |
| 6 | **No - Skip Icon** | Leaves your current icon unchanged |

> **Note**: You must confirm at least one option for changes to apply.

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `fluxcopy.copy` | Allows using `/copy` command | `op` |

---

## Configuration

The plugin generates `plugins/FluxCopy/config.yml` on first run:

```yaml
default-port: 25565
connection-timeout: 5000
read-timeout: 5000

messages:
  prefix: "§8[§bFluxCopy§8] §r"
  pinging: "§7Pinging §f{server}§7..."
  ping-success: "§aSuccessfully connected to §f{server}"
  ping-failure: "§cFailed to ping server: §f{error}"
  usage: "§cUsage: /copy <server-ip> [port]"
  example: "§7Example: /copy play.applemc 25565"
  not-a-player: "§cOnly players can use this command."
  invalid-port: "§cInvalid port number."
  session-expired: "§cSession expired. Please run /copy again."
  no-changes: "§8No changes applied."
  motd-updated: "§aMOTD updated successfully"
  icon-saved: "§aServer icon saved"
  no-permission: "§cYou don't have permission to use this command."
```

---

## Building from Source

```bash
git clone https://github.com/EscapeXMc/FluxCopy.git
cd FluxCopy
mvn clean package
```

The shaded JAR will be at `target/FluxCopy-1.0.0.jar`.

---

## Technical Details

- **Target**: Paper 1.21.11-R0.1-SNAPSHOT
- **Java**: 21
- **Networking**: Raw SLP (Server List Ping) protocol
- **Dependencies**: Gson 2.10.1 (shaded)
- **Data Packs**: Version 94 compatible

---

## License

MIT License — feel free to use, modify, and distribute.

---

*Made with ❤️ by Flux*