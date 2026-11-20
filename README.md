# Modpack Helper

> modid: `modpackhelper`

Minecraft Java 版 **1.20.1 / Forge 47.4.21** 的客户端辅助模组，
为整合包提供帮助文档、常见问题、旧存档导入与 DeepSeek 峰谷提示。

本文件写给「想改代码 / 想自己打包」的人。只想玩的玩家看 [安装](#二安装玩家) 一节即可。

---

## 一、这是什么

这些功能原本长在 [Verity Plus](https://github.com/CreeperBoy-CB/VerityPlus)（modid: `verityconfig`）里，
后来按「和 Verity 本体无关的周边功能」拆出来独立成模组。

### 1. 帮助文档（HelperScreen）

按主题分 Windows / 手机两套正文，正文里可以嵌告警、提示、可点击链接、章节标题。

渲染器本身支持插图（按文件头先算版面、滚到可见区域才异步解码），
但目前资源文件中暂未包含任何插图。

### 2. 常见问题（QandAScreen）

按「Verity / 其他模组 / 其他」三栏分类，逐条折叠展开。

### 3. 旧存档导入（LegacySaveImportScreen）

扫描 `.minecraft/versions/` 下其它整合包实例的存档，复制导入到当前实例，源存档保留。

### 4. 模组管理说明（ModsRemovedScreen）

说明「模组管理功能已移除」，并链接到「模组安装」「模组管理」两份教程。

### 5. DeepSeek 峰谷提示（DeepSeekHintHandler / DeepSeekPricing）

进入存档时提示当前时段，峰谷切换时再提示一次。节假日按「非周末」判断，
数据从网络接口拉取；拉取失败时退回本地周末判断。

### 6. 聊天引导（ChatGuideFilter）

把 Verity 的「Need help setting up this mod?」换成自己的引导，
并隐藏 `AI setup issue? Tutorial:` 这类只会刷屏的链接。

---

## 二、安装（玩家）

**必需**

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x（开发与测试使用 47.4.21） |
| Verity Plus（`verityconfig`） | 2.9.9 及以上 |
| Verity JE | 6.0.0 及以上 |

把 jar 放进 `mods` 目录即可，客户端专用。

> **重要**：本模组把 Verity Plus 声明为**强制依赖**。
> 只装本模组、不装 Verity Plus，游戏会在启动时直接拒绝加载。
>
> 反过来只装 Verity Plus 是能正常玩的，只是没有帮助 / 常见问题 / 峰谷提示
> 这几项功能，以及 `/vc help`、`/vc qanda`、`/vc dshint` 三条指令。

---

## 三、指令

| 指令 | 作用 |
|---|---|
| `/mph` | 打开常见问题 |
| `/mph help` | 打开帮助（默认「内存不足」） |
| `/mph help <主题>` | 打开指定主题，支持中文名，如 `/mph help 光影安装` |
| `/mph qanda` | 打开常见问题 |

装了两个模组时，Verity Plus 还会额外注册：

| 指令 | 作用 |
|---|---|
| `/vc help [主题]` | 帮助文档 |
| `/vc qanda` | 常见问题 |
| `/vc dshint [on\|off]` | 峰谷提示开关 |
| `/vc dshint date [日期 时间]` | 指定时刻查询 |

**帮助主题**

| 英文名 | 中文名 |
|---|---|
| `low_memory` | 内存不足 |
| `shader_install` | 光影安装 |
| `mod_install` | 模组安装 |
| `mod_manage` | 模组管理（教程尚未编写，只有占位说明） |
| `tc_use` | TouchController使用教程 |

---

## 四、帮助文档怎么写

正文放在 `src/main/resources/assets/modpackhelper/help/` 下，命名规则：

| 文件 | 说明 |
|---|---|
| `<主题>_pc.txt` | Windows 端正文 |
| `<主题>_mobile.txt` | 手机端正文 |

两份可以内容不同（菜单路径不一样），但主题名必须一致。

> 只有其中一份时，另一个平台打开会看到空页面，所以 `/mph help <主题>`
> 会把这种半缺状态也算作「主题不存在」并直接报错。

### 行内标记

| 标记 | 效果 |
|---|---|
| `/warn/文字/warn/` | 红色告警 |
| `/hint/文字/hint/` | 黄色提示 |
| `/url/显示文字\|https://.../url/` | 可点击链接（只接受 http/https） |
| `§1:"章节标题"` | 章节标题，可用「上一章 / 下一章」跳转 |

### 插图（可选）

渲染器本身支持插图：需要时单独一行写文件名，如 `<xxx.png>`，放在：

```
assets/modpackhelper/help/pngres/<主题>/
```

目录 PC 与手机共用。找不到对应文件时该行会被静默跳过，不占版面，
也不会显示「图片加载失败」的空框。

> ⚠️ 目前资源文件中**暂未包含任何插图**，帮助文档全部是纯文本。
> 如果要补图，请使用自己绘制、自己截取并遵守对应 EULA 的示意图，
> 或经授权取得的素材——Minecraft 游戏画面、PCL2 / FCL 启动器界面
> 与模组素材都属于他人版权，直接放进仓库有侵权风险。

### 新增主题

还要在 `HelperScreen` 的 `TOPIC_TITLES` 里登记一个中文名，
否则 `/mph help` 的补全里看不到它。

### 常见问题

写在 `assets/modpackhelper/help/qanda.txt`：

```
## 分类名
Q:问题
A:答案行一
A:答案行二
```

---

## 五、目录结构与代码导航

```
src/main/java/com/cb2495/modpackhelper/
├── ModPackHelper.java              模组入口，/mph 指令注册
├── client/                         聊天与峰谷提示
│   ├── ChatGuideFilter.java        引导类消息的拦截与替换
│   ├── ChatLinks.java              可点击的指令 / 链接文本
│   ├── DeepSeekHintHandler.java    何时发峰谷提示
│   ├── DeepSeekPricing.java        峰谷时段计算与节假日数据
│   └── VerityPlusConfigBridge.java 反射读 VerityPlus 的配置
├── screen/                         界面
│   ├── HelperScreen.java           帮助文档（章节跳转、可选插图）
│   ├── QandAScreen.java            常见问题
│   ├── LegacySaveImportScreen.java 旧存档导入
│   ├── ModsRemovedScreen.java      模组管理说明
│   └── TopBarScreen.java           两个滚动页共用的顶栏骨架
├── save/                           旧存档扫描与缩略图缓存
├── help/                           HelpText 标记解析、已读记录
├── util/HelpImages.java            包装 VerityPlus 的图片头解析与后台线程
└── widget/                         顶栏按钮栏与滚动区域

src/main/resources/assets/modpackhelper/help/
├── 各主题的 _pc.txt / _mobile.txt
└── qanda.txt
```

---

## 六、开发环境

| 项目 | 版本 / 说明 |
|---|---|
| **IDE** | IntelliJ IDEA |
| **Gradle** | 8.8 · 用自带的 `gradlew` |
| **Forge** | 47.4.21 |
| **Mappings** | official / 1.20.1 |
| **Java** | 17 |

> **本仓库是 Verity Plus 的 Gradle 子工程，不能单独构建。**

必须把两个仓库放在同一级目录：

```
任意路径/
├── VerityConfig/      父工程（Verity Plus）
└── ModPackHelper/     本仓库
```

然后把 IDEA 打开在 `VerityConfig` 目录，或者在那里跑命令：

```bash
cd VerityConfig
gradlew.bat build                      # Windows
./gradlew build                        # Linux / macOS
gradlew.bat :modpackhelper:runClient   # 只跑客户端
gradlew.bat :modpackhelper:build       # 只打本模组的包
```

产物在 `ModpackHelper/build/libs/modpackhelper-<版本>.jar`。

---

## 七、与 Verity Plus 的依赖关系

依赖是单向的：

```
ModpackHelper ──编译期依赖──▶ VerityPlus
VerityPlus    ──反射调用────▶ ModpackHelper
```

本模组直接 `import` VerityPlus 的类（日志、后台线程、图片头解析），
所以需要在同一个 Gradle 构建里拿到它的 userdev classpath。父工程用
`settings.gradle` 把本目录 include 成子工程：

```groovy
include 'modpackhelper'
project(':modpackhelper').projectDir = file('../ModPackHelper')
```

反过来，VerityPlus 从不 `import` 本模组的类，全部通过
`ModpackHelperBridge` 反射调用。这样做是为了避免循环编译依赖，
同时保证 VerityPlus 单独安装也能正常启动。

构建脚本会检查父工程名，不在 VerityPlus 工程里会直接报错并说明原因。
这是有意的：把 reobf 后的 SRG 名 jar 塞进 `runClient` 的 userdev 环境，
会以 `NoSuchMethodError` 的形式在启动瞬间崩溃，比构建期报错难查得多。

---

## 八、开发时容易踩的坑

- **`Screen.init()` 在窗口缩放时会重跑**。顶栏按钮必须每次先 `clear()` 再重建，
  否则缩放几次就会多出一堆重复按钮。
- **顶栏按钮不进 `Screen.renderables`**。`Screen.render` 会在遮罩之后重绘控件表，
  普通按钮会被半透明遮罩压暗成「鬼影」，所以由 `ScreenTopBar` 在遮罩之后手动绘制。
- **已读帮助记录**存在 `.cache/modpack_helper/read_help_message.json`。
- **图片解码**跑在 VerityPlus 的后台单线程上；界面关闭后靠「加载代次」
  丢弃迟到的结果，避免纹理泄漏。
- **资源里有中文**，`processResources` 必须设 `filteringCharset = 'UTF-8'`，
  否则中文 Windows 上会按 GBK 展开 `mods.toml`，产出非法 UTF-8。

---

## 九、发布时提醒玩家

- **必须同时安装 Verity Plus**，否则游戏拒绝加载
- 两个 jar 都要分发，缺哪个都会出问题

---

## 十、免责声明与授权说明

> **本节是本项目最重要的一节，请在使用或分发前完整阅读。**

### 1. 本项目与 Verity JE 作者无隶属关系

Modpack Helper 是**第三方社区项目**，由 CreeperBoy-CB 独立开发，
与 Verity JE 的作者 **Varmite、BlockMaster29、JJack、ErrorCezar**
（以及 Verity 形象原作者 ThatMob）没有任何隶属、合作或背书关系。

### 2. Verity JE 采用 All Rights Reserved

Verity JE 的 `mods.toml` 中明确声明 `license="All Rights Reserved"`。

**All Rights Reserved 意味着版权方保留全部权利**。本项目的 MIT 许可证
只覆盖 CreeperBoy-CB 自己编写的代码，不能、也不会改变 Verity JE 的授权状态。

### 3. 本项目的资源全部自制

| 内容 | 来源 |
|---|---|
| `help/*_pc.txt`、`help/*_mobile.txt` | 本项目自行撰写 |
| `help/qanda.txt` | 本项目自行整理 |
| `help/pngres/` | 暂未包含任何插图 |

本项目**不包含** Verity JE 的任何代码、贴图、模型、音效或原始资源。

### 4. 关于聊天文本的处理

`ChatGuideFilter` 会在运行时识别 Verity 发出的固定字符串
（例如 `Need help setting up this mod?`），命中后**把这条消息隐藏掉**，
换成本项目自己的引导文字。

被匹配的字符串只用于**比较**，不会被打包进本项目。
本项目也不修改 Verity 的任何 class 文件或资源。

### 5. 帮助文档暂未包含插图

早期版本的帮助文档配有 49 张操作截图，现已全部移除，因此当前资源文件中没有插图。

| 原截图内容 | 版权归属 |
|---|---|
| Minecraft 游戏画面（`mc_*.png`） | Mojang。EULA 不允许把游戏内画面用于再分发 |
| PCL2 / FCL 启动器界面（`pcl_*.png` / `fcl_*.png`） | 启动器作者 |
| 画面中出现的模组素材（Verity 的笑脸等） | Verity JE 或对应模组作者 |

这些图既不是本项目原创，也无法取得授权，放进 Git 仓库属于再分发。
即使只是「操作教程的配图」，也不构成合理使用的充分理由。

移除后的做法：

- 帮助文档全部改为纯文本，靠**路径与菜单名**描述操作步骤
- 渲染器本身支持插图，但资源文件中暂未包含任何插图；缺图时静默跳过，不占版面
- 如果你的整合包需要配图，请使用自己绘制、自己截取并遵守对应 EULA 的示意图，
  或经授权取得的素材

### 6. 责任限制

- 本项目按「现状」提供，不附带任何明示或暗示的担保
- 作者不对使用本项目造成的任何直接或间接损失负责
- 若 Verity JE 作者提出异议，本项目会配合调整或停止分发

### 7. 如果你是 Verity JE 的作者

如果你认为本项目侵犯了你的权利，请通过仓库的 Issue 联系。
作者承诺会在核实后**及时移除相关内容或停止分发**，无需法律程序。

---

## 十一、许可证

本项目采用 **MIT 许可证**，全文见仓库根目录的 [LICENSE.txt](LICENSE.txt)。

```
Copyright (c) 2026 CreeperBoy-CB
```

你可以自由使用、修改、再发布本项目的代码，只需在副本中保留上述版权声明与
许可声明。

> 注意本项目的**依赖**各有各的许可证，不在 MIT 覆盖范围内：

| 依赖 | 许可证 |
|---|---|
| Forge | LGPL 2.1 |
| Yet Another Config Lib | LGPL 3.0-or-later |
| Verity Plus | MIT |
| Verity JE | **All Rights Reserved** |

本项目**没有**获得 Verity JE 作者的授权，与它的交互方式见「十、免责声明」。