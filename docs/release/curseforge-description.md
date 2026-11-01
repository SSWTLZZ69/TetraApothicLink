# Tetra Apothic Link

Tetra and Apotheosis both reward equipment that grows over time, but their systems do not fully understand each other. Tetra Apothic Link makes Apotheosis evaluate Tetra equipment through its actual modular context, while giving high-level enchanting a progression path that respects Tetra's magic-capacity and integrity systems. Tetrawear is an optional integration: you can omit it and still use the Tetra tool, ranged-weapon, affix, gem, reforging, inscription, and Ascension Table features.

## Features

- Places modular Tetra tools, bows, crossbows, shields, and Tetrawear armor into appropriate Apotheosis loot categories.
- Classifies modular armor as light or heavy from the structural weight of its installed modules.
- Adds light- and heavy-armor affixes, gem behavior, and special abilities designed around Tetrawear's energy mechanics.
- Shows incompatible affixes and gems as dormant instead of silently applying an invalid effect.
- Adds four Arcane Capacity Inscription tiers for equipment intended to carry Apotheosis-level enchantments.
- Unlocks those inscriptions through staged Apotheosis infusion setups rather than adding more levels above Tetra's original Gild improvement.
- Provides server configuration and datapack-driven rules for armor weight, applicability, tool categories, gem patches, and special effects.
- Adds an experimental Ascension Table for equipment-bound integrity ascension, material-value tuning, unstable overlimit, and stabilization.

## Experimental Ascension Table — 0.2.0-alpha.1

The new table reuses Apotheosis' enchanting-screen layout, bookshelf statistics, experience costs, and native Quanta/rectification/Arcana selection algorithms. The screen retains the existing Eterna, Quanta, and Arcana bars; rectification still participates in the background without adding another control.

Place one assembled Tetra item in the equipment slot and a medium in the other slot. Hover the three options to inspect the result and cost; the first, second, and third options consume one, two, and three media respectively. Processing without an improvement does not charge you.

| Medium | Purpose |
| --- | --- |
| Structural Ascension | Permanently raises the whole item's integrity supply. |
| Material Exaltation | Amplifies material primary, secondary, and tertiary contributions. |
| Material Attenuation | Reduces those contributions for structures that benefit from lower values. |
| Unstable Overlimit | A cheaper unstable integrity boost and permission for supported handheld equipment to exceed its integrity budget, at the cost of native Tetra destabilization. |
| Stabilizing | Converts previously obtained unstable integrity into stable integrity and resolves eligible whole-item destabilization once its source is removed. |

Ascension belongs to the complete item, not to a removable blade, handle, or other module. Supported replacements and Apotheosis reforging preserve that state; salvaged components do not inherit it. Material tuning recalculates the material-derived contributions rather than multiplying all final weapon statistics. Negative coefficients keep their sign, so increasing every value is not automatically better for every structure.

**Alpha limitations:** unstable overlimit is currently **handheld-only**, allows up to 60 integrity deficit, and uses native Tetra Winded, Vexing, and Infested effects. Stable ascension can also support the optional Tetrawear armor integration. Tiers, multipliers, costs, and limits are experimental; visuals currently reuse vanilla models and textures. Back up your world before testing. The table requires Apotheosis' enchantment module, and its crafting recipes also require the adventure module.

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
- Java 17
- Tetra 6.17.x
- Apotheosis 7.4.8
- Placebo 8.6.3+
- Apothic Attributes / AttributesLib 1.3.7+

The addon must be installed on both the client and server.

## Optional and recommended mods

- **Tetrawear 1.0.x** — optional modular armor classification, armor affixes/gems, and energy-related effects. **It is not required; omit it if your pack does not use it.**
- **Create: Enchantment Industry 2.5.x** — optional enchanting-machine integration.
- **Tetracelium 1.3.x** — optional compatibility support.
- **Tetra Insight 0.1.6+** — recommended client companion for detailed Holosphere material and schematic information.

---

## 中文说明

Tetra Apothic Link 将 Tetra 的模块化装备接入神化的战利品类别、词缀、宝石和重铸体系。**Tetrawear 已改为可选依赖，可以不安装**；不安装时仍可使用 Tetra 工具、弓弩、词缀、宝石、重铸、魔力刻印与升格台。安装 Tetrawear 后，额外启用模块护甲轻重分类、护甲专属词缀与宝石、精力相关效果。

### 0.2.0-alpha.1 实验功能：升格台

升格台沿用神化附魔界面布局、书架属性、经验成本和量子化／校准／阿卡那算法，只保留原有的位阶、量子化、阿卡那三条属性栏，校准在后台参与计算。

新增五种媒介：**结构升格、材质升华、材质压缩、不稳超限、稳定化**。它们分别用于提高完整度供给、放大或压缩材料主值／副值／第三值、用原生失稳换取超限，以及后续把不稳定力量转为稳定力量。左槽放一件已组装的 Tetra 装备，右槽放媒介；悬停查看结果，第 1／2／3 档消耗 1／2／3 个媒介，无有效提升不收费。

强化数据绑定整件装备，不绑定可拆模块；正常受支持的换件、转换和重铸保留强化，拆下来的部件不带走收益。材料升华只重算材料来源贡献，不会把神化词缀或所有最终属性重复放大；负系数仍保留原来的方向。

**超限目前仅支持手持装备**，最多超支 60 完整度，并承担 Tetra 原生的疲乏、恼鬼、虫蚀失稳效果。数值、成本和上限仍属实验设计，模型与纹理暂时复用原版资源，请先备份世界测试。升格台需要启用神化附魔模块，合成配方还需要冒险模块。

本模组新增四档独立的“魔力刻印”，分别提供 60、120、200、300 点魔力容量。它不会覆盖原版镀金，也不会在镀金之上增加新等级。第一档要求部件已经拥有镀金 V，后续档位依次升级上一档刻印。

刻印卷轴需要经过四个阶段的神化灌注：

1. 主世界阶段：使用宝石粉与不常见重铸材料安装第一档刻印。
2. 下界或海洋阶段：选择高量子或高奥秘灌注路线，使用稀有重铸材料。
3. 深暗之域阶段：完成平衡型灌注，使用史诗重铸材料。
4. 末地阶段：完成龙息末地灌注，使用神话重铸材料。

安装 Create: Enchantment Industry 后，还可获得卷轴打印、烈焰附魔器魔力容量限制、超载去稳化和拒绝超载等可选联动。服务器可以通过配置限制或关闭这些机制。
