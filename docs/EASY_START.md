# NarutoUI 新手上路（EASY_START）

> 这是一份写给第一次接触 NarutoUI 的开发者的“说明书”，不是完整 API 字典。
>
> 读完之后，你应该能独立做出：
>
> - 一个能打开的普通配置界面
> - 单页设置、顶部导航、多页配置
> - 自定义行 / 列布局
> - 自己的主题、背景图片 / 轮播背景
> - 图片选择、颜色选择、图片裁剪
> - 带流光的控件
> - 跨界面重建的稳定 ID
>
> 想查每个方法的完整参数，请看同目录的 [API-TUTOR.md](API-TUTOR.md)。本文会把常用部分讲得更慢、更细。

> **不用一次读完。**
>
> - 只想先看到界面：跳到 [1.3](#13-先写一个最小的配置对象) 和 [1.4](#14-写第一个界面)；
> - 想做普通配置页：看 [第 3 节](#3-高层-dsl只描述有哪些字段)；
> - 想自定义行/列：看 [第 4 节](#4-低层-dsl需要自定义布局时)；
> - 想做背景/流光/选图：看 [第 5～8 节](#5-主题控件工厂与流光)；
> - 遇到报错：直接看 [第 11 节](#11-常见问题与避坑)。
>
> 示例为了易读会省略 `import` 和你自己的辅助类（例如 `MyMetrics`、`MyConfig` 的 getter/setter）；核心 API 调用是完整的。

---

## 目录

- [0. 先用一句话理解它](#0-先用一句话理解它)
- [1. 把 NarutoUI 接进你的模组](#1-把-narutoui-接进你的模组)
- [2. 三个层次：先选对工具](#2-三个层次先选对工具)
- [3. 高层 DSL：只描述“有哪些字段”](#3-高层-dsl只描述有哪些字段)
- [4. 低层 DSL：需要自定义布局时](#4-低层-dsl需要自定义布局时)
- [5. 主题、控件工厂与流光](#5-主题控件工厂与流光)
- [6. 背景：纯色、图片、轮播、可配置背景](#6-背景纯色图片轮播可配置背景)
- [7. 本地化：标签也可以直接写翻译键](#7-本地化标签也可以直接写翻译键)
- [8. 文件选择、颜色选择与裁剪](#8-文件选择颜色选择与裁剪)
- [9. 恢复与稳定 ID](#9-恢复与稳定-id)
- [10. 进阶组件：主题选择器和卡片编辑器](#10-进阶组件主题选择器和卡片编辑器)
- [11. 常见问题与避坑](#11-常见问题与避坑)
- [12. 构建与产物](#12-构建与产物)
- [13. 速查：哪个类负责什么](#13-速查哪个类负责什么)

---

## 0. 先用一句话理解它

NarutoUI 是 **Modern UI 的配置界面骨架**。

你告诉它：

1. 这个界面叫什么；
2. 有哪些页面；
3. 每个页面有哪些分区；
4. 每个分区里放哪些字段（开关、滑条、文字、按钮……）。

它负责：

- 左侧导航 / 顶部导航；
- 可滚动、居中、限制宽度的页面；
- 卡片、圆角、边框、分隔线、行间距；
- 开关、滑条、按钮、数值行的默认样式；
- 主题、背景、本地化、流光；
- 屏幕重建时恢复当前页和滚动位置。

它**不负责**：

- 保存你的配置；
- 决定你的配置存在 ForgeConfigSpec、JSON 还是别的什么地方；
- 在你的配置变化时自动写盘。

所以字段回调只做一件事：**修改你的状态**。保存要在你的 setter 或回调里自己做。

### 0.1 它长什么样

一个典型界面大概是这样：

```text
Screen
├─ 背景
├─ 导航（左侧竖栏 / 顶部横栏）
└─ 内容区
   └─ Page
      ├─ 页面标题 / 副标题
      └─ Section 卡片
         ├─ toggle 行（标题 + 开关）
         ├─ slider 行（标题 + 滑条 + 数值）
         ├─ value 行（标题 + 只读数值）
         ├─ text 段落
         └─ button 按钮
```

### 0.2 适合谁

- 使用 **Forge 1.20.1 / Java 17** 的客户端模组；
- 已经安装 **Modern UI 3.12.0 或更高版本**；
- 想要一个“看起来像 Modern UI 原生设置页”的配置界面；
- 不想自己写导航栏、卡片、滚动、主题和字段行。

> **重要：NarutoUI 是客户端库。**
>
> 不要在 dedicated server（专用服务器）初始化路径里创建界面或加载界面类。服务端只需要你的配置数据，不需要 Modern UI。

---

## 1. 把 NarutoUI 接进你的模组

### 1.1 当前版本与依赖

当前仓库默认构建的是：

- Minecraft **1.20.1**
- Forge **47.x**
- Java **17**
- Modern UI **3.12.0+**
- NarutoUI **1.4.0**

`../build.gradle` 里也保留了 1.21.1 / NeoForge 的版本配置，但本文示例以 **1.20.1 Forge** 为准。

NarutoUI 依赖 Modern UI。你的开发环境里至少要能编译到：

```gradle
dependencies {
    implementation "icyllis.modernui:ModernUI-Core:3.12.0"
    modImplementation("icyllis.modernui:ModernUI-Forge:1.20.1-3.12.0.+")

    // 本地开发时，把 NarutoUI 的 jar 放进 libs/ 再这样引用
    modImplementation files("libs/narutoui-1.20.1-1.4.0.jar")
}
```

> 如果 NarutoUI 已经发布到你使用的 Maven，把 `files(...)` 换成对应的 Maven 坐标即可。
>
> 运行时，你的 `mods/` 目录里需要同时有 **NarutoUI** 和 **Modern UI**。

常用包：

```text
me.kall.narutoui.mui.screen       入口、Fragment、页面、section、MuiScreenHost
me.kall.narutoui.mui.widget       MuiLayout、MuiControls、卡片、数字框、chip、logo、hint、颜色/说明行
me.kall.narutoui.mui.theme        MuiTheme、MuiWidgetFactory、MuiShimmerWidgetFactory
me.kall.narutoui.mui.background   背景、可配置背景、MuiCrop、MuiBackgroundFiles
me.kall.narutoui.mui.shimmer      流光 profile、配置、ShimmerTarget
me.kall.narutoui.mui.overlay      颜色选择浮层、裁剪浮层
me.kall.narutoui.mui.util         MuiLocalization、MuiFilePicker
```

如果你要在自己的 `mods.toml` 里声明依赖，可以加类似下面这段（版本范围按你的实际发布版本调整）：

```toml
[[dependencies.mymod]]
modId = "narutoui"
mandatory = true
versionRange = "[1.20.1-1.4.0,)"
ordering = "AFTER"
side = "CLIENT"
```

### 1.2 注册“打开配置界面”的入口

Forge 1.20.1 常见做法是通过 `ConfigScreenHandler.ConfigScreenFactory` 注册：

```java
package com.example.mymod.client;

import com.example.mymod.MyConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientSetup {
    private ClientSetup() {}

    public static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, previous) -> MyConfigScreen.create(previous)));
    }
}
```

然后在主模组类里，只在客户端调用它：

```java
package com.example.mymod;

import com.example.mymod.client.ClientSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(MyMod.MOD_ID)
public final class MyMod {
    public static final String MOD_ID = "mymod";

    public MyMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.register(FMLJavaModLoadingContext.get());
        }
    }
}
```

> **建议：**正式项目里把 `ClientSetup` 放到单独的 client-only 类中，避免 dedicated server 意外加载客户端类。上面这种写法已经用 `FMLEnvironment.dist == Dist.CLIENT` 做了判断，但大型项目更推荐用 `@Mod.EventBusSubscriber(Dist.CLIENT)` 或独立的客户端入口。

### 1.3 先写一个最小的配置对象

NarutoUI 不规定配置怎么存，所以先用一个最简单的静态配置类演示：

```java
package com.example.mymod;

public final class MyConfig {
    private static boolean enabled = true;
    private static int scale = 100;

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static int scale() {
        return scale;
    }

    public static void setScale(int value) {
        scale = value;
        save();
    }

    public static void reset() {
        enabled = true;
        scale = 100;
        save();
    }

    public static void save() {
        // TODO: 写 ForgeConfigSpec，或者写你自己的 JSON / 文件
        // 这个方法不会被 NarutoUI 自动调用，需要你在 setter 里调用。
    }
}
```

### 1.4 写第一个界面

```java
package com.example.mymod;

import me.kall.narutoui.mui.screen.MuiSettingsScreen;
import net.minecraft.client.gui.screens.Screen;

public final class MyConfigScreen {
    private MyConfigScreen() {}

    public static Screen create(Screen parent) {
        return MuiSettingsScreen.builder("Example Mod")
                .subtitle("Client settings")
                .page("General", page -> page
                        .section("Rendering", section -> section
                                .toggle("Enable HUD",
                                        MyConfig::enabled,
                                        MyConfig::setEnabled)
                                .slider("Scale", 50, 200,
                                        MyConfig::scale,
                                        MyConfig::setScale,
                                        value -> value + "%")
                                .value("Version", "1.0.0")
                                .text("Changes apply immediately.")
                                .button("Reset", MyConfig::reset)))
                .create(parent);
    }
}
```

打开后你会得到：

- 左侧导航；
- 一个居中的滚动页面；
- “Rendering”卡片；
- 一个带说明空间的开关行；
- 一个带 `%` 数值的滑条；
- 一行只读版本号；
- 一段说明文字；
- 一个 Reset 按钮。

这就是最常用的工作流：**builder → page → section → 字段**。

---

## 2. 三个层次：先选对工具

NarutoUI 的 API 大致分三层。不要一上来就用最底层，先判断自己要什么。

| 层次 | 入口 | 适合的场景 | 代码量 |
|---|---|---|---|
| 高层 DSL | `MuiSettingsScreen` + `MuiSettingsPage` + `MuiSettingsSection` | 普通开关、滑条、数值、按钮、说明文字 | 最少 |
| 低层 DSL | `MuiConfigScreen` + `MuiPageContext` | 需要自定义行、列、权重、特殊卡片内容 | 中等 |
| 手动组合 | `MuiLayout` + `MuiControls` | 需要完全控制布局、动态增删控件、嵌入已有页面 | 最多 |

可以把它想成点餐：

- **高层 DSL**：套餐，告诉服务员“我要一个开关、一个滑条”，厨房自动摆盘；
- **低层 DSL**：半自助，你拿到卡片和页面容器，可以自己往里面加东西；
- **手动组合**：自己下厨，锅碗瓢盆都给你，但摆盘也自己负责。

**建议：默认从高层 DSL 开始。** 只有当某个布局高层表达不了时，再下潜到低层。

---

## 3. 高层 DSL：只描述“有哪些字段”

### 3.1 页面和分区

```java
MuiSettingsScreen.builder("My Mod")
        .subtitle("Client settings")
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)))
        .create(parent);
```

`page("General", page -> ...)` 里：

- 第一个参数 `"General"` 同时作为导航标签和页面标题；
- 第二个参数是页面构建回调，拿到 `MuiSettingsPage`；
- `.section("Rendering", section -> ...)` 会自动加分区标题和卡片；
- `section` 是 `MuiSettingsSection`，所有字段都往卡片里加。

如果你想导航标签和页面标题不同：

```java
.page("mymod.nav.general", "General", "Everyday options", page -> ...)
```

三个字符串依次是：**导航标签、页面标题、页面副标题**。副标题可以为 `null`。

### 3.2 字段速查

| 方法 | 作用 | 示例 |
|---|---|---|
| `toggle(title, BooleanSupplier, BooleanConsumer)` | 实时开关 | `.toggle("HUD", MyConfig::enabled, MyConfig::setEnabled)` |
| `toggle(title, description, supplier, consumer)` | 带说明的开关 | `.toggle("HUD", "Draw it", ...)` |
| `toggle(title, boolean, consumer)` | 静态初值开关 | `.toggle("HUD", true, value -> ...)` |
| `slider(title, min, max, IntSupplier, IntConsumer)` | 整数滑条 | `.slider("Scale", 50, 200, ...)` |
| `slider(..., IntFunction<CharSequence>)` | 自定义数值文字 | `.slider(..., value -> value + "%")` |
| `value(title, CharSequence)` | 只读文字 | `.value("Version", "1.0.0")` |
| `value(title, IntSupplier)` | 实时整数 | `.value("FPS", MyMetrics::fps)` |
| `value(title, formatter, IntSupplier)` | 自定义格式的实时值 | `.value("Memory", value -> value + " MB", ...)` |
| `text(CharSequence)` | 卡片内段落 | `.text("Changes apply immediately.")` |
| `button(label, Runnable)` | 按钮 | `.button("Reset", MyConfig::reset)` |
| `divider()` | 显式分隔线 | `.divider()` |
| `custom(View)` | 自定义一行 | `.custom(myView)` |
| `customLayout(Consumer<MuiLayout>)` | 链式自定义行容器 | `.customLayout(row -> row.text("Hi"))` |
| `cardLayout()` | 拿到卡片本体 | `section.cardLayout().spacing(8)` |

`toggle` 的第二个参数是 **setter / 回调**，不是 getter。写成 `MyConfig::enabled` 两次通常在编译期就会暴露问题。

### 3.3 “实时值”和“用户操作”是两件事

这是新手最容易混淆的地方。

```java
.toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
//                    ↑ getter          ↑ 用户点击时调用
```

- **getter / supplier**：界面每隔约 100 ms 调用一次，用来把外部状态同步到控件。
- **setter / consumer**：用户拖动滑条、点击开关时调用，用来改你的状态。

所以：

- 配置值适合用 supplier；
- 高频变化的游戏数据（每帧变化）**不要**用 supplier 轮询，应该在事件回调里主动更新控件；
- supplier 不是事件推送，它只是“隔一会儿问一次”。

### 3.4 保存配置：API 不会替你做

所有 setter 只负责改状态。保存要你自己做：

```java
public static void setScale(int value) {
    scale = value;
    save(); // 你的保存逻辑
}
```

如果你不想每次拖动都写盘，也可以只在界面关闭或点“保存”按钮时写：

```java
.button("Save", MyConfig::save)
```

> 拖动滑条会连续触发很多次回调。频繁写盘时，建议做防抖，或者只在 `onDestroyed` / 按钮里保存。

### 3.5 常用 builder 选项

```java
MuiSettingsScreen.builder("My Mod")
        .subtitle("Client settings")
        .footer("Changes are saved automatically")
        .contentWidth(820)
        .initialPage(1)
        .singlePage()                 // 不要左侧导航，只显示一个页面
        .topNavigation()              // 导航放到顶部
        .theme(theme)                 // 自定义主题，见第 5 节
        .background(MuiBackground.scrim())
        .widgetFactory(MuiWidgetFactory.modern())
        .shimmerProfile(MuiShimmerProfile.defaultProfile())
        .onCreated(fragment -> { })
        .onDestroyed(fragment -> { })
        .page(...)
        .create(parent);
```

说明：

- `singlePage()`：必须**恰好一个页面**，否则构建时抛异常；
- `topNavigation()` / `leftNavigation()` / `navigation(MuiNavigation.TOP)`：导航位置；
- `contentWidth(dp)`：页面内容最大宽度，`<= 0` 表示不限制；
- `initialPage(index)`：打开时显示第几页，索引从 0 开始；
- `theme(theme)`：传入你自己的 `MuiTheme`；传 `null` 表示使用 Modern UI 当前主题；
- `background(...)`：背景，默认是暗色遮罩；
- `widgetFactory(...)`：这一屏用哪套控件；
- `shimmerProfile(...)`：这一屏的流光配置；
- `onCreated`：界面外壳创建完成后回调，参数是 `MuiConfigFragment`，它实现了 `MuiScreenHost`；
- `onDestroyed`：界面销毁前回调。

### 3.6 单页设置示例

如果配置项很少，不需要左侧导航：

```java
return MuiSettingsScreen.builder("Example Mod")
        .singlePage()
        .page("", page -> page
                .section("Rendering", section -> section
                        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
                        .slider("Scale", 50, 200,
                                MyConfig::scale,
                                MyConfig::setScale,
                                value -> value + "%")))
        .create(parent);
```

单页模式下：

- 没有左侧导航；
- 只允许一个页面；
- `subtitle` / `footer` / `railHeader` / `railFooter` 不显示；
- 页面可以用 `.page("", ...)` 省略标题。

### 3.7 多页示例

```java
return MuiSettingsScreen.builder("Example Mod")
        .subtitle("Client settings")
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)))
        .page("About", page -> page
                .section("Information", section -> section
                        .text("A small example configuration screen.")
                        .value("Version", "1.0.0")))
        .create(parent);
```

---

## 4. 低层 DSL：需要自定义布局时

高层 DSL 的 `section` 已经把“标题 + 卡片 + 分隔线”都包好了。如果你需要：

- 多列布局；
- 自定义一行里的多个控件；
- 把已有页面嵌进来；
- 在卡片里放特殊控件；

就用 `MuiConfigScreen`。

### 4.1 `MuiConfigScreen` + `MuiPageContext`

```java
MuiConfigScreen.builder("My Mod")
        .page("General", "General", "Everyday options", ui -> {
            LinearLayout card = ui.section("Rendering");
            card.addView(ui.switchRow("Enable HUD", null, true, value -> { }),
                    MuiControls.fill());
        })
        .create(parent);
```

页面回调拿到 `MuiPageContext`，常用方法：

| 方法 | 作用 |
|---|---|
| `context()` | Modern UI `Context`，创建控件、算 dp 用 |
| `theme()` | 当前 `MuiTheme` |
| `content()` | 页面内容列（`LinearLayout`） |
| `contentLayout()` | 同一列的链式版本（`MuiLayout`） |
| `header(title, subtitle)` | 手动加页面标题和副标题 |
| `section(title)` | 加分区标题 + 卡片，返回卡片 `LinearLayout` |
| `sectionLayout(title)` | 同上，但返回链式卡片 `MuiLayout` |
| `add(view)` | 往页面内容列加一个全宽 view |
| `switchRow(...)` | 创建开关行 |
| `valueRow(...)` | 创建数值行 |
| `sliderRow(...)` | 创建滑条行 |

> 高层 `MuiSettingsPage` 已经自动调用过 `header`，所以低层页面里一般不需要再手动加标题，除非你确实想要两个标题。

### 4.2 `MuiLayout`：给 `LinearLayout` 加一层链式封装

`MuiLayout` 是 `LinearLayout` 的子类。它**仍然是 `LinearLayout`**，所以可以传给任何接受 `View` 或 `LinearLayout` 的 API；同时所有配置方法都返回自己，方便连续写。

```java
LinearLayout card = MuiLayout.card(context, theme)
        .margin(0, 12, 0, 0)
        .spacing(8)
        .padding(12)
        .heading("Appearance")
        .text("Changes apply immediately.")
        .button("Reset", MyConfig::reset);
```

常用方法：

| 分类 | 方法 |
|---|---|
| 创建 | `column(context, theme)`、`row(context, theme)`、`card(context, theme)` |
| 方向 / 对齐 | `horizontal()`、`vertical()`、`gravity(int)`、`center()`、`centerVertical()` |
| 间距 | `padding(dp)`、`padding(l, t, r, b)`、`margin(...)`、`spacing(dp)` |
| 尺寸 | `minHeight(dp)` |
| 背景 | `background(int color)`、`background(Drawable)` |
| 加内容 | `add(view)`、`add(view, params)`、`addWeighted(view, weight)`、`addAll(view...)` |
| 占位 | `space(dp)`、`flexibleSpace()` |
| 常用控件 | `text(...)`、`heading(...)`、`divider()`、`button(...)` |

几个约定：

- 所有尺寸参数都是 **DP**；`text` / `heading` 的字号是 **SP**。
- `add(view)` 使用 `LinearLayout` 的默认布局参数：纵向布局让子项填满宽度，横向布局包裹内容。
- `margin(...)` 会**记住**数值，等父容器分配 `LayoutParams` 时再应用，所以可以先 `margin` 再 `add`。
- `spacing(dp)` 用 `LinearLayout` 的 divider 槽位实现“子项之间的空隙”，不会改子项 margin。它的缺点是：如果你之后手动 `setDividerDrawable(...)`，这个间距会被替换。

示例：一行“文字 + 开关”：

```java
Switch toggle = MuiControls.switchControl(ui.context(), ui.theme());
toggle.setChecked(MyConfig.enabled());
toggle.setOnCheckedChangeListener((view, checked) -> MyConfig.setEnabled(checked));

ui.contentLayout().add(
        MuiLayout.row(ui.context(), ui.theme())
                .add(MuiControls.text(ui.context(), ui.theme(),
                                "Enable HUD", 14, ui.theme().onSurface()),
                        MuiControls.weight())
                .add(toggle));
```

### 4.3 `MuiControls`：手动创建单个控件

`MuiControls` 是静态工具类，适合在 `MuiLayout` 或自定义页面里创建控件。

常用方法：

| 方法 | 说明 |
|---|---|
| `text(context, theme, text, size, color)` | 普通文字 |
| `heading(context, theme, text, size, color)` | 加粗标题 |
| `switchControl(context, theme)` | 开关 |
| `switchRow(context, theme, ...)` | 完整开关行 |
| `slider(context, theme, max, progress)` | 滑条 |
| `sliderRow(context, theme, ...)` | 完整滑条行 |
| `valueRow(context, theme, ...)` | 数值行 |
| `button(context, theme, label, outlined, onClick)` | 按钮 |
| `divider(context, theme)` | 1px 分隔线 |
| `column(context)` / `row(context)` / `card(context, theme)` | 普通布局 |
| `dp(context, value)` | DP 转像素 |
| `alpha(color, factor)` | 调整透明度 |
| `fill()` / `wrap()` / `weight()` | 常用 `LayoutParams` |

示例：手动拼一个按钮行：

```java
Button cancel = MuiControls.button(ui.context(), ui.theme(), "Cancel", true, view -> { });
Button apply = MuiControls.button(ui.context(), ui.theme(), "Apply", false, view -> { });

ui.add(MuiLayout.row(ui.context(), ui.theme())
        .add(cancel)
        .space(8)
        .add(apply));
```

### 4.4 自定义整页

如果整页都要自己画，可以用 `customPage`：

```java
MuiConfigScreen.builder("My Mod")
        .customPage("Custom", context -> {
            ScrollView scroll = new ScrollView(context);
            LinearLayout column = MuiControls.column(context);
            scroll.addView(column, new FrameLayout.LayoutParams(
                    MuiControls.MATCH, MuiControls.WRAP));
            // ... 自己加内容 ...
            return scroll;
        })
        .create(parent);
```

注意：`customPage` 的返回值应该是**完整的、可滚动的页面根视图**。外壳不会再帮你包一层滚动容器。

### 4.5 什么时候用 `content()`，什么时候用 `contentLayout()`

- 你已经有 `MuiPageContext`，想按原有写法 `content().addView(view, params)`：用 `content()`；
- 你想链式写：用 `contentLayout()`。

两者指向同一个内容列，不会创建两个容器。

---

## 5. 主题、控件工厂与流光

### 5.1 `MuiTheme`

`MuiTheme` 是一组视觉 token，不可变（record）。常用 token：

```text
primary / onPrimary        主色和主色上的文字色
surface / surfaceLow       屏幕背景、卡片背景
surfaceHigh                控件背景
onSurface                  主要文字
onSurfaceVariant           次要文字
outline                    边框、分隔线
error                      错误色
cornerRadius               圆角（DP）
spacing                    页面/内容间距（DP）
```

你可以从 Modern UI 当前主题解析：

```java
MuiTheme theme = MuiTheme.modern(context)
        .withPrimary(0xFFE0A64A)
        .withOnPrimary(0xFF1A1208)
        .withSpacing(18)
        .withCornerRadius(14);
```

注意 `context` 是 Modern UI 的 `Context`，不是 Minecraft 的 `Minecraft` 对象。通常在页面里用：

```java
.page("General", "General", null, ui -> {
    MuiTheme theme = ui.theme(); // 当前屏解析好的主题
    // ...
})
```

如果你在 builder 阶段已经有一个 `MuiTheme`，可以：

```java
.theme(theme)
```

> 如果你没有现成的 `MuiTheme`，最省事的做法是**不传** `.theme(...)`，让 API 自己从 Modern UI 解析。进页面后可以用 `ui.theme()` 做自绘控件。
>
> 颜色格式是 **ARGB**，例如 `0xFFE0A64A`：前两位是 Alpha，后面是 R、G、B。

### 5.2 `MuiWidgetFactory`

控件工厂决定“开关、滑条、按钮、文字实际用什么类”。

内置两种：

```java
MuiWidgetFactory.modern()   // 普通 Modern UI 控件
MuiWidgetFactory.shimmer()  // 带流光的控件
```

每屏指定：

```java
MuiSettingsScreen.builder("My Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .page(...)
        .create(parent);
```

全局兜底：

```java
MuiControls.setWidgetFactory(MuiWidgetFactory.shimmer());
```

> 优先用“每屏指定”。全局设置适合只有一个模组使用这套 API 的场景，否则可能影响别的界面。

自定义工厂需要实现：

```java
TextView text(Context context, CharSequence text, float size, int color)
Switch switchControl(Context context, MuiTheme theme)
SeekBar slider(Context context, MuiTheme theme, int max, int progress)
Button button(Context context, MuiTheme theme, CharSequence label,
              boolean outlined, View.OnClickListener onClick)
```

### 5.3 Shimmer 流光

Shimmer 是“颜色波”效果。要让它出现，需要两件事：

1. 用 **shimmer 控件工厂**创建控件；
2. 控件有 **可用的 shimmer profile**。

只设置 `.shimmerProfile(...)` 但用 `MuiWidgetFactory.modern()`，是不会有流光的，因为普通控件不实现 `ShimmerTarget`。

最简用法：

```java
MuiSettingsScreen.builder("My Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .shimmerProfile(MuiShimmerProfile.defaultProfile())
        .page(...)
        .create(parent);
```

自定义配置：

```java
public final class MyShimmerConfig implements MuiShimmerConfig {
    @Override
    public boolean text() {
        return MyConfig.shimmer();
    }

    @Override
    public boolean configText() {
        return MyConfig.configTextShimmer();
    }

    @Override
    public boolean controls() {
        return MyConfig.controlShimmer();
    }

    @Override
    public boolean hasAccent() {
        return true;
    }

    @Override
    public int fps() {
        return 30;
    }

    @Override
    public double speed() {
        return 1.0D;
    }

    @Override
    public double scale() {
        return 1.0D;
    }

    @Override
    public double strength() {
        return 0.8D;
    }

    @Override
    public int[] ramp() {
        // 必须是 9 个 ARGB 颜色
        return new int[]{
                0xFFFFFFFF, 0xFFFFD24A, 0xFFFFA726,
                0xFFFF7043, 0xFFFF5252, 0xFFFF7043,
                0xFFFFA726, 0xFFFFD24A, 0xFFFFFFFF
        };
    }
}
```

每屏 profile：

```java
MuiShimmerProfile profile = MuiShimmerProfile.create(new MyShimmerConfig());

MuiSettingsScreen.builder("My Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .shimmerProfile(profile)
        .page(...)
        .create(parent);
```

默认 profile：

```java
MuiShimmerSettings.configure(new MyShimmerConfig()); // 改默认配置
MuiShimmer.configure(new MyShimmerConfig());         // 改默认配置并刷新引擎
```

`MuiShimmerConfig` 的 `hasAccent`、`fps`、`speed`、`scale`、`strength`、`ramp` 都有默认值，最少只需要实现 `text()`、`configText()`、`controls()`。

自定义控件想参与流光，实现 `ShimmerTarget`，然后在 attach / detach 时调用：

```java
MuiShimmer.attach(myView);
MuiShimmer.detach(myView);
```

---

## 6. 背景：纯色、图片、轮播、可配置背景

### 6.1 内置背景预设

```java
.background(MuiBackground.transparent())     // 完全透明
.background(MuiBackground.black())           // 不透明黑
.background(MuiBackground.color(0xCC101116)) // 纯色（ARGB）
.background(MuiBackground.scrim())           // 默认暗色遮罩
.background(MuiBackground.image(path, 0.35D))
.background(MuiBackground.image(path, 0.0D, 0.0D, 1.0D, 1.0D, 0.35D))
.background(MuiBackground.slideshow(files, 5.0D, 0.8D, 0.35D))
```

参数说明：

- `path` 是 `java.nio.file.Path`；
- `dim` 是 `0.0 ~ 1.0` 的暗化程度，`0` 是原图，`1` 接近全黑；
- `image` 的五个数字是裁剪区域：`cropX, cropY, cropWidth, cropHeight`，都是 `0..1` 的比例；
- `slideshow(files, interval, fade, dim)`：`interval` 秒换一张，`fade` 秒交叉淡入淡出。

> 单图和轮播都是**异步解码**的。刚打开时可能先显示黑色或默认色，解码完成后才换成图片。
>
> 相对路径容易受工作目录影响，建议用 `FMLPaths.GAMEDIR.get().resolve("config/mymod/bg.png")` 得到绝对路径。

### 6.2 可配置背景：玩家自己选图、选文件夹、裁剪

如果你希望玩家在配置界面里：

- 选择一张背景图；
- 选择整个文件夹做轮播；
- 调整轮播间隔、淡入时长、暗化；
- 自动裁剪成当前窗口比例；
- 记住这些设置；

就用 `MuiConfigurableBackground`。

它需要三个东西：

1. `MuiBackgroundFiles`：负责路径、复制文件、列出图片、解码；
2. `MuiBackgroundSettings`：负责从你的配置读、写；
3. `MuiBackgroundLabels`：所有提示文字。

#### 第一步：文件助手

```java
private static final MuiBackgroundFiles BG_FILES =
        new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), "mymod-background");
```

玩家选中的图片会被复制到游戏目录，文件名是 `mymod-background` + 原扩展名。文件夹里的图片会按文件名排序，支持 `.png`、`.jpg`、`.jpeg`、`.bmp`、`.gif`。

#### 第二步：持久化适配器

`MuiConfigurableBackground` 不关心你用什么配置系统，你只要实现两个方法：

```java
public final class MyBackgroundSettings implements MuiBackgroundSettings {
    @Override
    public MuiBackgroundState load() {
        return new MuiBackgroundState(
                MyConfig.bgImage(),       // String，游戏目录下的相对文件名，可为 ""
                MyConfig.bgFolder(),      // String，文件夹，可为 ""
                MyConfig.bgOrder(),       // List<String>，轮播顺序偏好
                MyConfig.bgInterval(),    // double，秒
                MyConfig.bgFade(),        // double，秒
                MyConfig.bgDim(),         // double，0..1
                new MuiCrop(
                        MyConfig.cropX(),
                        MyConfig.cropY(),
                        MyConfig.cropWidth(),
                        MyConfig.cropHeight()));
    }

    @Override
    public void save(MuiBackgroundState state) {
        MyConfig.setBgImage(state.image());
        MyConfig.setBgFolder(state.folder());
        MyConfig.setBgOrder(state.order());
        MyConfig.setBgInterval(state.interval());
        MyConfig.setBgFade(state.fade());
        MyConfig.setBgDim(state.dim());
        MyConfig.setCrop(state.crop().x(), state.crop().y(),
                state.crop().width(), state.crop().height());
        MyConfig.save();
    }
}
```

> 上面的 `MyConfig.bgImage()`、`MyConfig.setBgImage(...)` 等方法只是示意，需要按你自己的配置对象补全。`save()` 在每次用户操作后调用，适合在这里标记配置 dirty。

`MuiBackgroundState` 是不可变快照，字段含义：

| 字段 | 含义 |
|---|---|
| `image` | 单图文件名，相对于游戏目录 |
| `folder` | 轮播文件夹，可为相对或绝对路径 |
| `order` | 优先播放的文件名列表，其余按字母序 |
| `interval` | 每张图显示秒数 |
| `fade` | 交叉淡入秒数 |
| `dim` | 暗化程度 `0..1` |
| `crop` | 单图裁剪比例 |

默认值是：`image=""`、`folder=""`、`interval=12`、`fade=0.6`、`dim=0.55`、`crop=FULL`。

#### 第三步：文字标签

```java
private static final MuiBackgroundLabels BG_LABELS = new MuiBackgroundLabels(
        "mymod.background.pick",           // 图片选择标题
        "Images",                          // 图片过滤器标签
        "mymod.background.pick.failed",    // 图片选择失败
        "mymod.background.folder",         // 文件夹选择标题
        "mymod.background.folder.failed",  // 文件夹选择失败
        "mymod.background.failed",         // 解码/保存失败
        "mymod.background.saved",          // 保存成功
        new MuiCropOverlay.Labels(
                "mymod.crop.title",
                "mymod.crop.hint",
                "mymod.crop.apply",
                "mymod.crop.cancel"));
```

这些字符串既可以写普通文字，也可以写翻译键。`MuiBackgroundLabels` 在创建时就会通过 `MuiLocalization` 解析它们。

#### 第四步：创建控制器并挂到 screen

```java
public static Screen create(Screen parent) {
    MuiConfigurableBackground background = new MuiConfigurableBackground(
            new MyBackgroundSettings(), BG_LABELS, BG_FILES);

    return MuiSettingsScreen.builder("Example Mod")
            .background(background)
            .page("Appearance", page -> page
                    .section("Background", section -> section
                            .button("Choose image", background::chooseBackgroundImage)
                            .button("Choose folder", background::chooseBackgroundFolder)
                            .button("Clear image", background::clearBackgroundImage)
                            .button("Clear folder", background::clearBackgroundFolder)
                            .slider("Interval", 2, 30,
                                    () -> (int) background.state().interval(),
                                    value -> background.setBackgroundInterval(value),
                                    value -> value + " s")
                            .slider("Dim", 0, 100,
                                    () -> (int) Math.round(background.state().dim() * 100),
                                    value -> background.setBackgroundDim(value / 100.0D),
                                    value -> value + "%")))
            .create(parent);
}
```

它会自动：

- 打开系统图片/文件夹选择器；
- 把选中的图片复制进游戏目录；
- 如果图片比例和窗口不一致，弹出裁剪框；
- 保存裁剪比例；
- 单图模式显示选中的图；
- 有文件夹时优先显示文件夹轮播；
- 通过 `MuiBackgroundSettings.save` 持久化。

#### 第五步：读取当前状态

`MuiConfigurableBackground` 实现了 `MuiBackgroundActions`，可以读：

```java
background.state();   // 当前设置
background.images();  // 文件夹中按顺序找到的图片
```

也提供：

```java
background.reload();    // 重新从 settings.load() 读，并刷新背景
background.invalidate(); // 丢弃缓存的图片，下次重新解码
```

> 页面按钮点击时，背景必须已经 attach 到界面上。`MuiConfigurableBackground` 会在界面创建时自动 attach；如果你手动 `new` 了一个却没有 `.background(controller)`，选择类操作会直接返回，其他设置也可能只改状态而不会真正刷新背景。

### 6.3 完全自定义背景

背景接口只有三个关键方法：

```java
public interface MuiBackground {
    Drawable create(Context context);

    default void attach(FrameLayout root) {
        root.setBackground(create(root.getContext()));
    }

    default void attach(MuiScreenHost host, FrameLayout root) {
        attach(root);
    }

    default void detach() {}
}
```

简单背景只需要：

```java
MuiBackground custom = MuiBackground.of(context -> new ColorDrawable(0xAA000000));
```

如果背景需要定时器、图片资源、动画，就实现 `attach` / `detach` 自己管理生命周期。`detach` 会在界面视图销毁时调用。

---

## 7. 本地化：标签也可以直接写翻译键

所有公开标签参数都可以是：

- 普通文本：`"Enable HUD"`；
- 翻译键：`"mymod.config.enabled"`。

解析规则：

- `I18n.exists(key)` 为真：使用翻译值；
- 否则：原样返回字符串；
- 空字符串保持为空。

```java
.page("mymod.config.general", page -> page
        .section("mymod.config.rendering", section -> section
                .toggle("mymod.config.enabled",
                        "mymod.config.enabled.desc",
                        MyConfig::enabled,
                        MyConfig::setEnabled)))
```

带参数的文本：

```java
.value("mymod.config.memory",
        MuiLocalization.translatable("mymod.config.megabytes", 8192))
```

`MuiLocalization` 提供：

```java
MuiLocalization.resolve(text)                 // 解析单个标签
MuiLocalization.resolveNullable(text)         // null 安全
MuiLocalization.get(key, args...)             // 带参数
MuiLocalization.translatable(key, args...)    // 返回 CharSequence，可直接给标签参数
```

> 翻译是在**构建界面时**解析的。切换语言后，重新打开界面，或调用 `host.reloadCurrentPage()` 重建当前页。

---

## 8. 文件选择、颜色选择与裁剪

### 8.1 文件 / 文件夹选择

```java
MuiFilePicker.openImage("Choose an image", "Images", new MuiFilePicker.Callback() {
    @Override
    public void onPicked(Path path) {
        // 选中文件，回调在 UI 线程
    }

    @Override
    public void onFailed() {
        // 对话框失败
    }
});

MuiFilePicker.openFolder(initialPath, "Choose a folder", callback);
```

要点：

- 回调在 UI 线程执行；
- 用户点“取消”时**不会**触发任何回调（不是失败）；
- `onFailed` 只有对话框无法打开或出错时才调用；
- 原生对话框优先，失败时会回退到 AWT / Swing。

### 8.2 颜色选择器

`MuiColorPickerOverlay` 是一个覆盖整个界面外壳的 HSV 颜色选择器：

```java
MuiColorPickerOverlay overlay = new MuiColorPickerOverlay(
        host.requireContext(),
        theme,
        MyConfig.accent(),
        new MuiColorPickerOverlay.Labels(
                "mymod.color.title",
                "mymod.color.brightness",
                "mymod.color.alpha",
                "mymod.color.apply",
                "mymod.color.cancel"),
        argb -> {
            MyConfig.setAccent(argb);
            host.reloadCurrentPage(); // 如果主题/强调色参与页面构建
        });

host.showOverlay(overlay);
```

- `argb` 是带 Alpha 的整数；
- 点 Apply 会先关闭浮层，再回调；
- 点 Cancel 只关闭，不回调；
- `Labels` 里的文字同样可以是翻译键。

### 8.3 得到 `MuiScreenHost`

页面回调里没有直接给你 host，所以用 `onCreated` 存起来：

```java
private static MuiScreenHost host;

return MuiSettingsScreen.builder("My Mod")
        .onCreated(fragment -> host = fragment)
        .page("Appearance", "Appearance", null, ui -> {
            MuiTheme theme = ui.theme();

            ui.section("Accent", section -> section
                    .button("Pick color", () -> {
                        if (host == null) return;

                        host.showOverlay(new MuiColorPickerOverlay(
                                host.requireContext(),
                                theme,
                                MyConfig.accent(),
                                new MuiColorPickerOverlay.Labels(
                                        "Title", "Brightness", "Alpha", "Apply", "Cancel"),
                                argb -> MyConfig.setAccent(argb)));
                    }));
        })
        .create(parent);
```

`MuiScreenHost` 是通用的宿主服务接口：

```java
Context requireContext();
void showOverlay(View overlay);
void removeOverlay(View overlay);
void reloadCurrentPage();
default void onThemeChanged() { reloadCurrentPage(); }
```

`onThemeChanged()` 默认重建当前页；需要额外主题状态的宿主可以覆盖它。NarutoOverlay 不再需要单独的 `ConfigUiHost`，页面直接依赖 `MuiScreenHost`。

### 8.4 图片裁剪

`MuiCropOverlay` 是一个固定宽高比的裁剪框：

```java
MuiCropOverlay overlay = new MuiCropOverlay(
        context,
        theme,
        previewImage,
        imageWidth,
        imageHeight,
        targetAspect,
        new MuiCropOverlay.Labels(
                "mymod.crop.title",
                "mymod.crop.hint",
                "mymod.crop.apply",
                "mymod.crop.cancel"),
        new MuiCropOverlay.Listener() {
            @Override
            public void onCrop(int x, int y, int width, int height) {
                // 结果是以源图像像素为单位的区域
                host.removeOverlay(overlay); // 需要自己移除
            }

            @Override
            public void onCancel() {
                host.removeOverlay(overlay);
            }
        });

host.showOverlay(overlay);
```

> 如果你只是想做“配置背景图裁剪”，直接用第 6 节的 `MuiConfigurableBackground` 更省事。它已经处理了预览图创建、比例判断、裁剪框移除和结果保存。

---

## 9. 恢复与稳定 ID

### 9.1 普通创建：进程内恢复

```java
MuiSettingsScreen.builder("My Mod")
        .page("General", page -> ...)
        .create(parent);
```

每次调用 `builder().create(parent)` 都会生成一个 UUID，把界面模型放进进程内 registry。FragmentManager 重建界面时，`MuiConfigFragment` 会从 registry 取回模型，恢复当前页和滚动位置。

这适合“本次游戏进程内”的恢复，不保证游戏重启后还能恢复。

### 9.2 稳定 ID：跨进程重启恢复

如果你希望界面在游戏重启后、FragmentManager 重新创建时还能根据 ID 重建，注册一个 stable recipe：

```java
public final class MyConfigScreen {
    private static final String ID = "mymod:config";

    public static void register() {
        MuiSettingsScreen.register(ID, "My Mod", builder -> builder
                .subtitle("Client settings")
                .page("General", page -> page
                        .section("Rendering", section -> section
                                .toggle("mymod.config.enabled",
                                        MyConfig::enabled,
                                        MyConfig::setEnabled))));
    }

    public static Screen create(Screen parent) {
        return MuiSettingsScreen.createRegistered(ID, parent);
    }
}
```

然后在客户端初始化时调用一次 `MyConfigScreen.register()`。

低层版本：

```java
MuiConfigScreen.register(ID, "My Mod", builder -> builder
        .page("General", "General", null, ui -> { ... }));

Screen screen = MuiConfigScreen.createRegistered(ID, parent);
```

要求：

- recipe 必须是“可重建”的，不要依赖一次性的局部变量；
- 所有状态都应该来自能重新读取的配置对象；
- 同一个 id 再次注册会替换旧 recipe；
- `createRegistered` 找不到 id 会抛异常。

### 9.3 `MuiConfigFragmentFactory`

标准 `MuiModApi.createScreen` 路径不需要它。只有你的宿主允许设置 `FragmentManager#setFragmentFactory` 时，才可能需要：

```java
manager.setFragmentFactory(new MuiConfigFragmentFactory());
```

`MuiConfigFragment` 本身有 public 无参构造，所以大多数情况下不用管。

---

## 10. 进阶组件：主题选择器和卡片编辑器

这两个不是“必须用”的组件，但如果你要做“主题选择”“HUD 卡片布局编辑”，可以少写很多布局代码。

### 10.1 主题选择器

```java
MuiThemePicker.add(page, "Theme", options, () -> {
    MyConfig.save();
    // 如果主题影响整个 screen，可以在这里调用 host.reloadCurrentPage()
}).text("Changes apply immediately.");
```

`add(...)` 返回页面，可以继续链式添加内容；如果想把选择器嵌进已有 section，用 `MuiThemePicker.create(context, theme, options, inUse, changed)`，它返回一个 `MuiLayout`。

每个选项实现 `MuiThemePicker.Option`：

```java
public final class MyThemeOption implements MuiThemePicker.Option {
    @Override
    public CharSequence title() {
        return "Dark";
    }

    @Override
    public boolean selected() {
        return MyConfig.theme().equals("dark");
    }

    @Override
    public void select() {
        MyConfig.setTheme("dark");
    }

    @Override
    public int accent() {
        return 0xFFE0A64A;
    }

    @Override
    public int surface() {
        return 0xFF101116;
    }
}
```

选择器会把选项排成两列卡片；`select()` 会在 `changed` 之前调用。`preview(...)` 可以返回自定义预览控件，返回 `null` 则使用默认的 accent / surface 色块。

### 10.2 卡片与通用组件

旧版 `MuiCardEditor` 的 `Model` / `Field` 适配层把“卡片、位置、字段、重置”绑在一起，不够通用，已经移除。现在直接组合 NarutoUI 的通用组件：

```java
MuiCollapsibleCard.card(context, theme, "Health Bar",
                "1 / 2 字段 · (10, 20)", false)
        .trailing(enabledSwitch)
        .body(body -> body
                .add(MuiLayout.row(context, theme)
                        .spacing(6)
                        .add(MuiNumberField.create(context, theme, 10)
                                .bounds(0, 1920)
                                .width(64)
                                .onChange(value -> { }))
                        .add(MuiChip.create(context, theme, "Label", true)
                                .onChanged(checked -> { }))
                        .add(MuiTextAction.create(context, theme, "Reset", () -> { }))));
```

- `MuiCollapsibleCard`：常驻标题、摘要、`+` / `-` 和可动画 body。
- `MuiNumberField`：数字输入、范围 clamp、`onChange`、链式 `value` / `bounds` / `width` / `gravity`。
- `MuiChip`：背景参与 shimmer 的小开关 chip，文字固定黑色。
- `MuiTextAction`：紧凑的 primary 文本操作。
- `MuiLogo`：圆形 shimmer 标志，适合 rail 头部。
- `MuiHintBadge`：带 shimmer 描边的提示胶囊，适合 ESC 提示 / 底部说明。
- `MuiColorField`：色块 + 标签 + pick 按钮，适合边框色 / 强调色。
- `MuiDescriptionRow`：标题 + 可选说明，适合用法提示、只读说明行。

NarutoOverlay 的 Cards、About、Appearance 页面以及 rail 的 logo/hint 现在都是这样组装的：业务适配留在 NarutoOverlay，NarutoUI 只提供通用、可复用的链式组件。

---

## 11. 常见问题与避坑

### 11.1 界面打不开 / 没有 Config 按钮

检查：

- Modern UI 是否安装；
- NarutoUI 是否在 `mods/`；
- 注册代码是否只在客户端执行；
- `ConfigScreenHandler.ConfigScreenFactory` 是否注册成功；
- 日志里是否有 `NoClassDefFoundError` / `MuiModApi` 相关错误。

### 11.2 改了开关，配置没保存

NarutoUI 不保存配置。你必须在 setter 或回调里调用自己的保存逻辑。

### 11.3 开关/滑条显示的值和实际状态不同步

- supplier 大约每 100 ms 轮询一次，不是实时推送；
- 高频数据请用事件回调主动更新控件；
- 如果外部状态在别的线程变化，确保最终在 UI 线程更新。

### 11.4 静态滑条的数值文字不更新

`sliderRow` 的静态 `CharSequence value` 只设置初始文字。需要实时读数时，用 supplier + formatter 版本：

```java
.slider("Scale", 50, 200, MyConfig::scale, MyConfig::setScale, value -> value + "%")
```

### 11.5 单页模式报错

`singlePage()` 要求**恰好一个页面**。检查是否加了多个 `.page(...)`。

### 11.6 背景第一帧是黑的

单图和轮播都是异步解码。解码完成前会显示加载色 / 回退色，这是正常的。

### 11.7 背景相对路径找不到

用绝对路径：

```java
Path path = FMLPaths.GAMEDIR.get().resolve("config/mymod/bg.png");
```

### 11.8 `spacing()` 的间距突然没了

`MuiLayout.spacing()` 使用布局的 divider 槽位。如果你之后手动调用 `setDividerDrawable(...)`，间距会被替换。

### 11.9 shimmer 不亮

确认：

- 用了 `MuiWidgetFactory.shimmer()`；
- profile 的 `text()` / `configText()` / `controls()` 是 `true`；
- `hasAccent()` 是 `true`；
- 控件确实是通过工厂创建的 `Shimmer*` 控件。

### 11.10 颜色选择器 / 裁剪框不显示

它们需要通过 `MuiScreenHost.showOverlay(...)` 显示。确认：

- 通过 `onCreated(fragment -> host = fragment)` 拿到了 host；
- 点击按钮时 host 不为 `null`；
- 裁剪框的 listener 里自己调用了 `removeOverlay`（颜色选择器会自己关闭，裁剪框不会）。

### 11.11 文件选择取消后没有回调

这是设计行为：取消不是失败，不会调用 `onPicked` 或 `onFailed`。

### 11.12 翻译没生效

- 检查 `lang` 文件里有没有对应 key；
- 标签是在构建界面时解析的，切换语言后需要重开界面或 `reloadCurrentPage()`；
- `MuiBackgroundLabels` 在创建时就会解析，不要在语言加载前过早构造。

### 11.13 专用服务器崩溃

NarutoUI 是客户端库。不要在 common / server 初始化路径里引用界面类。把客户端注册放到单独的 client-only 类中。

### 11.14 页面内容改了但界面没变

页面是创建时构建并缓存的。需要重新构建当前页时：

```java
host.reloadCurrentPage();
```

或者重新打开界面。

### 11.15 `customPage` 不能滚动

`customPage` 不会帮你包 `ScrollView`。返回的根视图要自己负责滚动。

---

## 12. 构建与产物

在项目根目录执行：

```text
gradlew build
```

Windows 下也可以：

```text
.\gradlew.bat build
```

产物在：

```text
build/libs/narutoui-<minecraft版本>-<mod版本>.jar
```

例如：

```text
build/libs/narutoui-1.20.1-1.4.0.jar
```

同时会生成 sources jar 和 javadoc jar。构建脚本默认使用 Java 17 编译 1.20.1 版本。

---

## 13. 速查：哪个类负责什么

| 类 / 接口 | 作用 | 你什么时候会用到 |
|---|---|---|
| `MuiSettingsScreen` | 高层配置界面 builder | 大多数普通配置页 |
| `MuiSettingsPage` | 高层页面 | `.page(...)` 回调里 |
| `MuiSettingsSection` | 高层分区 / 字段 | `.section(...)` 回调里 |
| `MuiConfigScreen` | 低层配置界面 builder | 自定义页面、布局 |
| `MuiPageContext` | 低层页面上下文 | 低层页面回调里 |
| `MuiLayout` | 链式 `LinearLayout` | 自定义行 / 列 / 卡片 |
| `MuiCollapsibleCard` | 带 `+` / `-` 的可折叠卡片 | 紧凑列表、长设置分组 |
| `MuiNumberField` | 链式数字输入框 | 坐标、数量、范围输入 |
| `MuiChip` | 链式小开关 chip | 标签 / 单位开关 |
| `MuiTextAction` | 紧凑 primary 文本操作 | 卡片内的 Reset / Clear |
| `MuiLogo` | 圆形 shimmer 标志 | rail / header |
| `MuiHintBadge` | shimmer 描边提示胶囊 | ESC 提示、底部说明 |
| `MuiColorField` | 色块 + pick 按钮 | 边框色 / 强调色 |
| `MuiDescriptionRow` | 标题 + 说明 | 用法提示、只读说明行 |
| `MuiControls` | 手动控件工厂 | 低层布局、自定义行 |
| `MuiTheme` | 视觉 token | 改主色、圆角、间距 |
| `MuiWidgetFactory` | 控件实现工厂 | 普通控件 / shimmer 控件 |
| `MuiBackground` | 背景接口和预设 | 纯色、图片、轮播 |
| `MuiConfigurableBackground` | 可配置背景控制器 | 让玩家选图/文件夹/裁剪 |
| `MuiBackgroundFiles` | 背景文件助手 | 可配置背景 |
| `MuiBackgroundSettings` | 背景持久化适配器 | 可配置背景 |
| `MuiBackgroundLabels` | 背景文字 | 可配置背景 |
| `MuiShimmerProfile` | 单屏流光配置 + 时钟 | 每屏独立流光 |
| `MuiShimmerConfig` | 流光参数 | 速度、颜色、开关 |
| `MuiShimmer` | 流光引擎 | 自定义控件接入 |
| `MuiFilePicker` | 系统文件/文件夹选择 | 选图、选文件夹 |
| `MuiColorPickerOverlay` | ARGB 颜色选择浮层 | 主题色、边框色 |
| `MuiCropOverlay` | 固定比例裁剪浮层 | 背景裁剪、头像裁剪 |
| `MuiThemePicker` | 主题卡片选择器 | 主题切换页 |
| `MuiScreenHost` | 界面宿主服务 | 浮层、刷新页面、拿 Context |
| `MuiConfigFragment` | 实际承载界面的 Fragment | `onCreated` 参数 |
| `MuiConfigFragmentFactory` | 可选 Fragment 工厂 | 特殊宿主 |

---

## 最后：推荐阅读顺序

1. 先用第 1 节跑通一个最小界面；
2. 用第 3 节把常用字段写熟；
3. 遇到“默认布局放不下”时看第 4 节；
4. 需要视觉定制时看第 5、6 节；
5. 需要玩家选文件/颜色/裁剪时看第 8 节；
6. 需要跨重启恢复时看第 9 节；
7. 需要逐方法查参数时看 [API-TUTOR.md](API-TUTOR.md)。

如果你只想复制一个能跑的起点，直接用第 1.3 + 1.4 节的代码，把 `MyConfig` 的 `save()` 换成你自己的保存逻辑即可。