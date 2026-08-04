# DisguisePlugin — 完整介绍 / Full Introduction

> 英文版可直接复制到 Modrinth Description；中文版用于中文社区宣传（MCBBS 等）或 README。

---

## English Version

## 🐑 DisguisePlugin

Transform into **86 different mobs** — from a humble sheep to the Ender Dragon itself! Pick a disguise from a beautiful GUI menu and become any creature of Minecraft with its **realistic mob mechanics**, **authentic movement speed**, and **unique interactions**.

> **Works on Minecraft 1.21.x** — Paper, Spigot and Purpur servers.

---

### ✨ Features

#### 🎭 86 Mobs in One Menu

- **Animals** — sheep, pig, cow, fox, panda, axolotl, frog...
- **Mounts & Fast Mobs** — horse, camel, dolphin, bee, allay...
- **Monsters** — zombie, skeleton, creeper, enderman, warden...
- **Elite & Bosses** — shulker, **Ender Dragon**, **Wither**, **Happy Ghast**...

Every mob is spawned as a **real entity** — other players see the actual mob with its authentic hitbox, sounds and animations. No fake packets.

#### 🍼 Baby & Size Variants

- Disguise as a **baby** version of any mob (baby chicken, baby zombie...)
- Choose **slime / magma cube sizes** (1–4)
- Pick **sheep colors** (16 wool colors) and **axolotl variants** (5 types)

#### 🐔 Realistic Mob Mechanics

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

#### 💰 Optional Economy System (Vault)

- **3 modes** in config: `admin` (admins only) / `all` (everyone) / `paid` (disguises cost money)
- **Per-mob pricing**: animals 100 coins, mounts 200, strong mobs 300, elite 500, bosses 1000 — fully adjustable
- **Per-mob disguise duration**: default 30 minutes, auto-expires when time runs out
- Players can **cancel their disguise anytime for free** via the menu (BARRIER button)

#### 🌍 Multi-Language Support

- Ships with **简体中文 (zh_cn)** and **English (en_us)**
- **Custom languages**: copy any language file, rename it, translate — done!
- All chat messages, action bars, menus and console logs follow the selected language

---

### 📥 Installation

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

### 🎮 Commands

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

### ⚙️ Configuration

**`config.yml`** — choose the language and the usage mode:

```yaml
# Language file inside the lang/ folder
language: zh_cn

# /dp mode: admin / all / paid
dp-mode: paid

# Disable the locator bar (player positions on the XP bar, 1.21.6+)
disable-locator-bar: true
```

**Per-mob pricing & duration** (paid mode) — every mob is configurable:

```yaml
mobs:
  sheep: { price: 100, duration: 1800 }    # 100 coins, 30 minutes
  ender_dragon: { price: 1000, duration: 1800 }
  slime: { price: 300, duration: 0 }       # 0 duration = unlimited
```

---

### ❓ FAQ

**Q: Does it work without Vault?**

**A:** Yes — the plugin starts fine, logs a warning and automatically falls back to `all` mode.

**Q: Can I make my own language?**

**A:** Yes! Copy `lang/en_us.yml` in the plugin folder, rename it (e.g. `fr_fr.yml`), translate the values, then set `language: fr_fr` in config.

**Q: Why do I see fewer mobs than 86?**

**A:** Mobs added in newer Minecraft versions (1.21.9+) are hidden on older servers automatically.

---

### 🐛 Support & Feedback

Found a bug or have a suggestion? Open an issue on [GitHub](https://github.com/GaryGray001/disguise-plugin) or leave a review below!

**Have fun disguising! 🎭**

---

## 中文版

## 🐑 DisguisePlugin 变身插件

一键变身 **86 种生物**——从小羊羔到末影龙！通过精美的 GUI 菜单选择变身，成为 Minecraft 中的任何一种生物，体验**真实的生物机制**、**原版移动速度**和**独一无二的互动玩法**。

> **支持 Minecraft 1.21.x** — Paper、Spigot、Purpur 服务器

---

### ✨ 功能特性

#### 🎭 一个菜单 86 种生物

- **动物** — 羊、猪、牛、狐狸、熊猫、美西螈、青蛙……
- **坐骑与快速生物** — 马、骆驼、海豚、蜜蜂、悦灵……
- **怪物** — 僵尸、骷髅、苦力怕、末影人、循声守卫……
- **精英与 BOSS** — 潜影贝、**末影龙**、**凋灵**、**快乐恶魂**……

所有变身都是**真实实体**——其他玩家看到的就是真实的生物本体，拥有原版碰撞箱、音效和动画，绝非假模型。

#### 🍼 幼年与体型变体

- 变身成任意生物的**幼年形态**（幼年鸡、幼年僵尸……）
- 选择**史莱姆 / 岩浆怪的体型**（1–4 级）
- 选择**绵羊毛色**（16 种羊毛色）和**美西螈品种**（5 种）

#### 🐔 真实的生物机制

每种变身都还原了原版行为：

| 生物 | 互动效果 |
| :--- | :--- |
| 🐔 鸡 | 随时间下蛋 |
| 🐫 骆驼 | 坐下 / 站起 |
| 🦔 犰狳 | 缩成球并掉落鳞甲 |
| 🌪️ 旋风人 | 发射风弹 |
| 🐚 潜影贝 | 潜行时传送 |
| 💥 苦力怕 | 引线爆炸 |
| 👁️ 循声守卫 | 对攻击者发射声波 |
| 🐉 末影龙 / 凋灵 | 巨型飞行 BOSS，悬浮动画 |

还有：每种生物**真实的移动速度**、击退抗性、摔落免疫（猫、铁傀儡等）、吃草动画等细节。

#### 💰 可选经济系统（Vault）

- **3 种模式**（配置可选）：`admin`（仅管理员）/ `all`（所有玩家）/ `paid`（付费使用）
- **每个生物独立价格**：普通动物 100 金币、坐骑 200、强怪 300、精英 500、BOSS 1000——全部可调
- **每个生物独立的变身时长**：默认 30 分钟，时间到**自动解除变身**
- 玩家可以随时通过菜单（BARRIER 按钮）**免费主动取消变身**

#### 🌍 多语言支持

- 自带**简体中文 (zh_cn)** 和 **English (en_us)**
- **自定义语言**：复制任意语言文件 → 重命名 → 翻译即可！
- 所有聊天消息、ActionBar、菜单和控制台日志都跟随所选语言

---

### 📥 安装方法

1. 下载 jar 文件
2. 放入服务器的 `plugins/` 文件夹
3. 重启服务器
4. 输入 `/dp` 打开变身菜单！

**环境要求：**

- Paper、Spigot 或 Purpur — **Minecraft 1.21+**
- Java 21
- **无硬依赖** — 开箱即用，[Vault](https://www.spigotmc.org/resources/vault.34315/) 仅 `paid` 付费模式需要（可选）
- **纯服务端插件** — 无需安装任何客户端 Mod 或资源包

> 新版本（1.21.9 / 1.21.11）加入的生物在旧版本服务器上会**自动隐藏**——不会报错，菜单只显示当前版本支持的生物。

---

### 🎮 指令说明

| 指令 | 说明 | 权限 |
| :--- | :--- | :--- |
| `/dp` | 打开变身菜单 | `disguise.use` |
| `/dp reload` | 重载配置、语言、经济设置 | `disguise.reload` |
| `/dp debug` | 显示调试信息 | `disguise.admin` |
| `/dp mode` | 查看当前使用模式 | `disguise.use` |

**权限节点：**

| 权限 | 默认 | 说明 |
| :--- | :--- | :--- |
| `disguise.use` | `true` | 使用变身菜单 |
| `disguise.reload` | `op` | 重载插件 |
| `disguise.admin` | `op` | 管理员模式权限 |

---

### ⚙️ 配置说明

**`config.yml`** — 选择语言和使用模式：

```yaml
# 使用的语言文件（lang 文件夹内）
language: zh_cn

# /dp 使用模式：admin / all / paid
dp-mode: paid

# 关闭经验条上的玩家位置显示（1.21.6+ 功能）
disable-locator-bar: true
```

**每个生物的价格与时长**（paid 模式生效）——所有生物均可独立配置：

```yaml
mobs:
  sheep: { price: 100, duration: 1800 }    # 100 金币，30 分钟
  ender_dragon: { price: 1000, duration: 1800 }
  slime: { price: 300, duration: 0 }       # 时长为 0 = 不限时
```

---

### ❓ 常见问题

**问：没有安装 Vault 能正常运行吗？**

**答：** 能。插件正常启动，控制台会输出警告，并自动回退到 `all` 模式。

**问：可以自己制作语言吗？**

**答：** 可以！复制插件目录下 `lang/en_us.yml`，重命名为你需要的语言代码（如 `fr_fr.yml`），翻译内容后，在 config 中设置 `language: fr_fr` 即可。

**问：为什么我看到的生物少于 86 种？**

**答：** 较新版本（1.21.9+）加入的生物在旧版本服务器上会自动隐藏。

---

### 🐛 支持与反馈

发现 Bug 或有建议？在 [GitHub](https://github.com/GaryGray001/disguise-plugin) 提交 Issue，或在 Modrinth 评论区留言！

**祝你变身愉快！🎭**
