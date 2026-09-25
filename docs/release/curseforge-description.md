# Tetra Apothic Link

Tetra and Apotheosis both reward equipment that grows over time, but their systems do not fully understand each other. Tetra Apothic Link makes Apotheosis evaluate Tetra and Tetrawear equipment through its actual modular context, while giving high-level enchanting a progression path that respects Tetra's magic-capacity and integrity systems.

## Features

- Places modular Tetra tools, bows, crossbows, shields, and Tetrawear armor into appropriate Apotheosis loot categories.
- Classifies modular armor as light or heavy from the structural weight of its installed modules.
- Adds light- and heavy-armor affixes, gem behavior, and special abilities designed around Tetrawear's energy mechanics.
- Shows incompatible affixes and gems as dormant instead of silently applying an invalid effect.
- Adds four Arcane Capacity Inscription tiers for equipment intended to carry Apotheosis-level enchantments.
- Unlocks those inscriptions through staged Apotheosis infusion setups rather than adding more levels above Tetra's original Gild improvement.
- Provides server configuration and datapack-driven rules for armor weight, applicability, tool categories, gem patches, and special effects.

## Arcane Capacity Inscription

Arcane Capacity Inscription is a separate Tetra improvement. It does not replace Gild and does not add any new Gild levels. Tier I requires an existing Gild V module; later inscriptions upgrade the previous tier rather than stacking all four bonuses together.

| Tier | Magic capacity | Integrity | Progression stage | Workbench materials |
| --- | ---: | ---: | --- | --- |
| I | +60 | 0 | Overworld infusion | Gem Dust + Uncommon Material |
| II | +120 | -1 | Nether Quanta or Ocean Arcana infusion | Gem Dust + Rare Material |
| III | +200 | -1 | Deep Dark balanced infusion | Gem Dust + Epic Material |
| IV | +300 | -2 | End draconic infusion | Gem Dust + Mythic Material |

Each tier begins as a prepared Tetra scroll and must be completed through Apotheosis enchanting infusion. Tier II supports two equivalent routes: a Quanta-focused Nether setup or an Arcana-focused ocean setup.

## Optional Create: Enchantment Industry integration

When Create: Enchantment Industry is installed, Tetra scrolls can participate in its printing workflow. Blaze Enchanters also gain explicit handling for results that exceed Tetra's magic capacity:

- **Capacity Limit** lowers only enchantments newly added by the current operation until the result fits.
- **Destabilize Overload** keeps the complete result and resolves the newly added overload through Tetra destabilization effects.
- **Reject Overload** refuses an operation that would introduce additional overload.

The Enchantment Tuner item changes the saved mode of an individual Blaze Enchanter. Servers can restrict the available modes or disable this integration entirely.

## Requirements

- Minecraft 1.20.1
- Forge 47.4.16 or newer in the 47.x line
- Tetra 6.17.x
- Tetrawear 1.0.x
- Apotheosis 7.4.8
- Placebo 8.6.3+
- Apothic Attributes / AttributesLib 1.3.7+

The addon must be installed on both the client and server.

## Optional and recommended mods

- **Create: Enchantment Industry 2.5.x** — optional enchanting-machine integration.
- **Tetracelium 1.3.x** — optional compatibility support.
- **Tetra Insight 0.1.6+** — recommended client companion for detailed Holosphere material and schematic information.

---

## 中文说明

Tetra Apothic Link 将 Tetra 与 Tetrawear 的模块化装备接入神化的战利品类别、词缀、宝石和重铸体系。模组会根据装备的真实模块结构识别工具类别，并依据结构重量区分轻甲与重甲，让不同类型的神化效果作用于合适的装备。

本模组新增四档独立的“魔力刻印”，分别提供 60、120、200、300 点魔力容量。它不会覆盖原版镀金，也不会在镀金之上增加新等级。第一档要求部件已经拥有镀金 V，后续档位依次升级上一档刻印。

刻印卷轴需要经过四个阶段的神化灌注：

1. 主世界阶段：使用宝石粉与不常见重铸材料安装第一档刻印。
2. 下界或海洋阶段：选择高量子或高奥秘灌注路线，使用稀有重铸材料。
3. 深暗之域阶段：完成平衡型灌注，使用史诗重铸材料。
4. 末地阶段：完成龙息末地灌注，使用神话重铸材料。

安装 Create: Enchantment Industry 后，还可获得卷轴打印、烈焰附魔器魔力容量限制、超载去稳化和拒绝超载等可选联动。服务器可以通过配置限制或关闭这些机制。
