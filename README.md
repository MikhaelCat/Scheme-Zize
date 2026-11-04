<div align="center">

# 🧩 Scheme-Zize

**Modern Mindustry mod featuring 512×512 schematic sizing, headless-safe dedicated server administration, and powerful client QoL tools.**

[![Mindustry Version](https://img.shields.io/badge/Mindustry-156--159%2B-6c5ce7.svg?style=for-the-badge&logo=game-and-watch)](https://github.com/Anuken/Mindustry)
[![Platform](https://img.shields.io/badge/platform-Desktop%20%7C%20Mobile-blue.svg?style=for-the-badge&logo=android)]()
[![Java Target](https://img.shields.io/badge/Java-17%2B-ED8B00.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-green.svg?style=for-the-badge)](LICENSE)

*A unified, crash-safe single JAR build designed for both players and dedicated server operators.*

[Features](#-key-features) • [Installation](#-installation) • [Server Setup (Scheme Net)](#-dedicated-server-setup-scheme-net) • [Architecture](#-architecture) • [Building](#-building-from-source)

---

</div>

## 🌟 Key Features

### 📐 Extreme Schematic Sizing (Up to 512×512)
* Increases maximum schematic dimensions up to **512×512**, allowing massive base layouts, multi-factory setups, and complex logic circuits without game-imposed clipping.
* Fully integrated with desktop and mobile input handlers (`ModedDesktopInput`, `ModedMobileInput`, `ModedSchematics`).

### 🛡️ Headless-Safe Server Administration (Scheme Net)
* **Single JAR Port:** Ships client QoL features and server-side RPC handlers in one unified build.
* **Dedicated Server Safety:** Completely eliminates headless crashes (`schematics null`, `netClient null`, `main.js` loader failures) with isolated, server-only initialization routines.
* **Remote Admin Tools:** Authorized server administrators (`player.admin`) can execute full admin tasks (item filling, unit spawning, team rules, teleports) across dedicated remote servers without requiring local hosting.

### 🤖 Automation & Integrated AI Helpers
* **GammaAI & NetMinerAI:** Automated mining, construction assistance, and worker coordination.
* **Renderer & Building Tools:** High-performance caching (`BuildsCache`, `UnitsCache`) with real-time entity tracking.

### 🎨 Modernized HUD & Multilingual Support
* Hexadecimal color picker (`HexBar`, `HexSelection`), interactive text sliders, and customizable HUD fragments.
* Full localization out of the box: **English, Russian, German, French, Polish, Ukrainian, Japanese, Chinese, Vietnamese, Indonesian**.

---

## 📦 Installation

### Client (Desktop / Mobile)
1. Download the latest `Scheme-Zize.jar` from the [Releases](https://github.com/MikhaelCat/Scheme-Zize/releases) page.
2. Place the JAR into your game mods folder:
   * **Linux:** `~/.local/share/Mindustry/mods/`
   * **Windows:** `%appdata%/Mindustry/mods/`
   * **Android:** In-game menu → **Mods** → **Import Mod** → select `Scheme-Zize.jar`.
3. Restart Mindustry.

### 🖥️ Dedicated Server Setup (Scheme Net)
To enable networked admin controls on your dedicated server:
1. Place the exact same `Scheme-Zize.jar` into your server's `config/mods/` directory.
2. Restart the server.
3. On the client, go to **Admin Tools → Auto** or **Scheme Net**.
4. Authenticated admins can immediately use remote commands and management utilities over the network.

---

## 🏗️ Architecture

```
                 ┌────────────────────────────────┐
                 │       Mindustry Client         │
                 │   (512×512 Schematics, UI, AI) │
                 └───────────────┬────────────────┘
                                 │
                   Scheme Net Network Protocol
                   (Authenticated with player.admin)
                                 │
                                 ▼
                 ┌────────────────────────────────┐
                 │     Dedicated Headless Server  │
                 │   - Safe server-only init path │
                 │   - Remote admin RPC handlers  │
                 │   - No client UI/loader crash  │
                 └────────────────────────────────┘
```

---

## 🛠️ Building from Source

Requirements: **JDK 17+**

```bash
git clone https://github.com/MikhaelCat/Scheme-Zize.git
cd Scheme-Zize
chmod +x gradlew build-all.sh
./build-all.sh
```

The compiled output will be generated at:
```text
dist/Scheme-Zize.jar
```

---

## 📄 License & Attribution

- Maintained and updated by [MikhaelCat](https://github.com/MikhaelCat).
- Originally authored as Scheme-Zize by Fallendragon and community contributors.
- Licensed under the [MIT License](LICENSE).
