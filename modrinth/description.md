## **🐑 DisguisePlugin**

Transform into **86 different mobs** — from a humble sheep to the Ender Dragon itself! Pick a disguise from a beautiful GUI menu and become any creature of Minecraft with its **realistic mob mechanics**, **authentic movement speed**, and **unique interactions**.

> **Works on Minecraft 1.21.x** — Paper, Spigot and Purpur servers.

---

### ✨ **Features**

#### **🎭 86 Mobs in One Menu**

- **Animals** — sheep, pig, cow, fox, panda, axolotl, frog...
- **Mounts & Fast Mobs** — horse, camel, dolphin, bee, allay...
- **Monsters** — zombie, skeleton, creeper, enderman, warden...
- **Elite & Bosses** — shulker, **Ender Dragon**, **Wither**, **Happy Ghast**...

Every mob is spawned as a **real entity** — other players see the actual mob with its authentic hitbox, sounds and animations. No fake packets.

#### **🍼 Baby & Size Variants**

- Disguise as a **baby** version of any mob (baby chicken, baby zombie...)
- Choose **slime / magma cube sizes** (1–4)
- Pick **sheep colors** (16 wool colors) and **axolotl variants** (5 types)

#### **🐔 Realistic Mob Mechanics**

Each disguise behaves like the real mob:

| Mob | Interaction |
| :--- | :--- |
| 🐔 Chicken | lays eggs over time |
| 🐫 Camel | sits down / stands up |
| 🦔 Armadillo | curls up and drops scutes |
| 🌪️ Breeze | shoots wind charges |
| 🐚 Shulker | teleports while sneaking |
| 💥 Creeper | explodes with a fuse timer |
| 👁️ Warden | fires sonic boom at attackers |
| 🐉 Ender Dragon / Wither | giant flying boss with hover animation |

Plus: realistic **walk speeds** for every mob, knockback resistance, fall immunity (cats, iron golems...), eating grass animation and more.

#### **💰 Optional Economy System (Vault)**

- **3 modes** in config: `admin` (admins only) / `all` (everyone) / `paid` (disguises cost money)
- **Per-mob pricing**: animals 100 coins, mounts 200, strong mobs 300, elite 500, bosses 1000 — fully adjustable
- **Per-mob disguise duration**: default 30 minutes, auto-expires when time runs out
- Players can **cancel their disguise anytime for free** via the menu (BARRIER button)

#### **🌍 Multi-Language Support**

- Ships with **简体中文 (zh_cn)** and **English (en_us)**
- **Custom languages**: copy any language file, rename it, translate — done!
- All chat messages, action bars, menus and console logs follow the selected language

---

### 📥 **Installation**

1. Download the jar
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. Run `/dp` to open the disguise menu!

**Requirements:**

- Paper, Spigot or Purpur — **Minecraft 1.21+**
- Java 21
- **No hard dependencies** — works out of the box. [Vault](https://www.spigotmc.org/resources/vault.34315/) is optional, only needed for `paid` mode
- **Server-side only** — no client mod or resource pack required

> New mobs from newer versions (1.21.9 / 1.21.11) are **hidden automatically** on older servers — no errors, the menu simply shows fewer mobs.

---

### 🎮 **Commands**

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/dp` | Open the disguise menu | `disguise.use` |
| `/dp reload` | Reload config, language, economy | `disguise.reload` |
| `/dp debug` | Show debug information | `disguise.admin` |
| `/dp mode` | Show current usage mode | `disguise.use` |

**Permissions:**

| Permission | Default | Description |
| :--- | :--- | :--- |
| `disguise.use` | `true` | Use the disguise menu |
| `disguise.reload` | `op` | Reload the plugin |
| `disguise.admin` | `op` | Admin mode access |

---

### ⚙️ **Configuration**

**`config.yml`** — choose the language and the usage mode:

```yaml
# Language file inside the lang/ folder
language: zh_cn

# /dp mode: admin / all / paid
dp-mode: paid
```

**Per-mob pricing & duration** (paid mode) — every mob is configurable:

```yaml
mobs:
  sheep: { price: 100, duration: 1800 }    # 100 coins, 30 minutes
  ender_dragon: { price: 1000, duration: 1800 }
  slime: { price: 300, duration: 0 }       # 0 duration = unlimited
```

---

### ❓ **FAQ**

**Q: Does it work without Vault?**

**A:** Yes — the plugin starts fine, logs a warning and automatically falls back to `all` mode.

**Q: Can I make my own language?**

**A:** Yes! Copy `lang/en_us.yml` in the plugin folder, rename it (e.g. `fr_fr.yml`), translate the values, then set `language: fr_fr` in config.

**Q: Why do I see fewer mobs than 86?**

**A:** Mobs added in newer Minecraft versions (1.21.9+) are hidden on older servers automatically.

---

### 🐛 **Support & Feedback**

Found a bug or have a suggestion? Open an issue on [GitHub](https://github.com/GaryGray001/disguise-plugin) or leave a review below!

**Have fun disguising! 🎭**
