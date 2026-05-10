# LootrTeams Addon

![Status](https://img.shields.io/badge/Status-In%20Development-orange)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-blue)
![Forge](https://img.shields.io/badge/Forge-47.x-red)
![License](https://img.shields.io/badge/License-MIT-green)

**LootrTeams Addon** is a Forge mod for Minecraft 1.20.1 that seamlessly bridges [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr) and [FTB Teams](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge).

Normally, Lootr gives every single player their own independent loot from chests. While great for large servers, this creates a disconnected and overpowered experience for players in the same team or party. **LootrTeams Addon** fixes this: it changes the Lootr storage mechanics so that **loot is shared per team**. 

## 🚀 Key Features

### 🤝 True Team-Shared Loot
- **Team Inventories:** When a player in an FTB Team opens a Lootr chest, that chest's loot is generated and shared for the entire team. If another teammate opens it, they see the same inventory (already looted).
- **Solo Player Support (Ghost Teams):** If a player is not in a team (or FTB Teams is not installed), they still get their own isolated loot, preserving the classic vanilla Lootr experience.
- **Dynamic Team Transitions:** The system is fully exploit-proof. Leaving a team will revert you to your solo inventory state without generating "double loot".

### 🛡️ Full Backwards Compatibility & Safety
- **Legacy Migration:** Installing this mod on an existing world will NOT delete your old loot. The built-in `LegacyMigrator` automatically ports existing player-based loot into the new team-based architecture.
- **Native Lootr Commands:** Advanced background synchronization ensures that vanilla commands like `/lootr clear <player>` still work perfectly, allowing admins to manage loot easily.
- **Robust Cache Management:** Fully optimized for server performance, avoiding excessive disk reads through smart RAM caching.

## ✅ Tested Versions
The mod has been extensively tested with the following environment:
- **Forge:** 47.2.20
- **Minecraft:** 1.20.1
- **Lootr:** 1.20.1-0.7.30.73
- **FTB Teams:** 1.20.1-2001.2.2

## 🤝 Contributing
Contributions are welcome! Whether it's reporting bugs, suggesting features, or submitting Pull Requests, your help is appreciated. 
- **Report Issues:** Please use the [GitHub Issues](https://github.com/FranWDev/lootr-teamsAddon/issues) tracker.
- **Developers:** Feel free to fork the repo and submit PRs. A comprehensive test suite (`GameTest Framework` and `JUnit 5`) is included to ensure stability.

## 📜 Credits
- **Lootr:** [Noobanidus](https://www.curseforge.com/members/noobanidus)
- **FTB Teams:** [FTB Team](https://www.curseforge.com/members/ftb)
- **Mod developed by:** [FranWDev](https://github.com/FranWDev)

## ⚖️ License
Licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.
