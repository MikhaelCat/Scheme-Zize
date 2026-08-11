# Scheme-Zize

Scheme-Zize is a Mindustry client/server mod with schematic-size support and admin tools.

## What’s different here

### Server-side admin tools

In earlier versions, most admin actions (fill, items, units, teleport, rules, …) only worked if you were the local host. Remote clients on a dedicated server were stuck.

This build ships **server handlers in the same jar**. Put it in `config/mods` on the server and on the client. Admins can use **Scheme Net** over the network without hosting locally. Actions require real admin rank (`player.admin`).

Without the server jar, it still works as a normal client QoL mod.

### Headless / dedicated server

Older Scheme-Zize jars crash when loaded on a dedicated server (`schematics` null, `netClient` null, `main.js` touching a null loader). This port is safe on headless: server-only init path, no client UI/scripts on the server.

### One jar, modern builds

Single `Scheme-Zize.jar` for Mindustry **156–159** (desktop + mobile).

## Install

1. Download `Scheme-Zize.jar` from the repository Releases page
2. Client: `mods/`
3. For networked admin tools: same jar in server `config/mods/`
4. Enable Admin Tools → Auto or Scheme Net (admin only)



## Build

```bash
./build-all.sh
```

Output: `dist/Scheme-Zize.jar`

## Credit

- Maintained as Scheme-Zize by Fallendragon.
- Maintained as Scheme-Zize by Fallendragon.

