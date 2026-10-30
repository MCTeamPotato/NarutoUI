# MUI 配置界面 API 教程

本教程介绍 `me.kall.narutoui.mui` 的配置界面 API。它不是某一套配置文件的规范，而是一层构建在 Modern UI 之上的界面框架：你只需要描述“有哪些页面、每个页面有哪些字段”，布局、控件样式、导航、背景和生命周期由 API 负责。

API 按职责分包：

- `me.kall.narutoui.mui.screen`：入口、Fragment、页面、section、宿主接口
- `me.kall.narutoui.mui.widget`：链式布局与通用控件
- `me.kall.narutoui.mui.theme`：主题与控件工厂
- `me.kall.narutoui.mui.background`：背景与可配置背景
- `me.kall.narutoui.mui.shimmer`：流光系统
- `me.kall.narutoui.mui.overlay`：颜色选择、裁剪浮层
- `me.kall.narutoui.mui.util`：本地化与文件选择

教程面向已经能编写 Forge / NeoForge 客户端模组的开发者。示例默认使用 Java 17，Minecraft 1.20.1 + Forge，Modern UI 3.12.0。

> 第一次接触这个库？先看更通俗的 [EASY_START.md](EASY_START.md)。本文更适合查参数和进阶细节。

## 目录

- [0 写在前面](#0-写在前面)
- [1 它能做什么](#1-它能做什么)
- [2 接入准备](#2-接入准备)
- [3 第一个配置界面](#3-第一个配置界面)
- [4 页面、分区与字段](#4-页面分区与字段)
- [5 低层 API](#5-低层-api)
- [6 主题与控件](#6-主题与控件)
- [7 背景预设](#7-背景预设)
- [8 本地化](#8-本地化)
- [9 Shimmer 流光](#9-shimmer-流光)
- [10 文件选择、颜色选择与裁剪](#10-文件选择颜色选择与裁剪)
- [11 屏幕恢复与稳定 ID](#11-屏幕恢复与稳定-id)
- [12 完整示例](#12-完整示例)
- [13 限制与常见问题](#13-限制与常见问题)
- [14 API 速查](#14-api-速查)
- [15 参考](#15-参考)

---

## 0 写在前面

一个配置界面通常包含这些重复工作：

- 左侧导航栏和当前页面高亮；
- 可滚动、居中、限制宽度的内容区；
- section 标题、卡片背景、圆角、边框；
- 行布局、标签、说明文字、分隔线；
- switch、slider、按钮、数值显示；
- 主题颜色、背景图片、本地化、动画；
- 页面重建、窗口缩放、资源释放。

`me.kall.narutoui.mui` 把这些工作集中到 API 中。调用方只需要提供：

1. 页面和导航标题；
2. 每个分区里放什么字段；
3. 字段读写哪个配置对象。

API 不负责保存配置。`toggle`、`slider` 的回调假设你自己维护配置对象或 Forge 配置值。

---

## 1 它能做什么

### 1.1 高层 DSL

```java
Screen screen = MuiSettingsScreen.builder("Example Mod")
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
                        .slider("Scale", 50, 200,
                                MyConfig::scale, MyConfig::setScale,
                                value -> value + "%")))
        .create(parent);
```

### 1.2 低层组合

需要自定义布局时，仍然可以使用 `MuiConfigScreen`、`MuiPageContext` 和 `MuiControls`：

```java
MuiConfigScreen.builder("Example Mod")
        .page("General", "General", "Everyday options", ui -> {
            LinearLayout card = ui.section("Rendering");
            card.addView(ui.switchRow("Enable HUD", null, true, value -> {}),
                    MuiControls.fill());
        })
        .create(parent);
```

### 1.3 可替换的视觉层

- `MuiTheme`：每次 screen 可以不同主题；
- `MuiWidgetFactory`：每次 screen 可以不同控件实现；
- `MuiShimmerProfile`：每次 screen 可以独立 shimmer 配置；
- `MuiBackground`：透明、黑屏、单图、轮播，或自定义 provider。

---

## 2 接入准备

### 2.1 依赖

API 依赖 Modern UI：

```gradle
implementation "icyllis.modernui:ModernUI-Core:${modernui_version}"
modImplementation("icyllis.modernui:ModernUI-Forge:${minecraft_version}-${modernui_version}.+")
```

当前 API 与 NarutoOverlay 同工程，包名 `me.kall.narutoui.mui`。未来计划拆成独立库模组；拆出后，消费方只需声明该库和 Modern UI 的依赖。

### 2.2 客户端限定

`MuiConfigScreen` 使用 `MuiModApi.createScreen(...)`，只能在客户端加载。不要在 common 端或 dedicated server 初始化路径中触发配置界面。

### 2.3 入口位置

Forge 常见入口：

```java
public final class MyMod {
    public MyMod(FMLJavaModLoadingContext context) {
        registerConfigScreen(context);
    }

    private static void registerConfigScreen(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, previous) -> MyConfigScreen.create(previous)));
    }
}
```

NeoForge 使用对应的 config screen 注册方式。

---

## 3 第一个配置界面

先写一个最小的配置对象：

```java
public final class MyConfig {
    private static boolean enabled = true;
    private static int scale = 100;

    public static boolean enabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static int scale() { return scale; }
    public static void setScale(int value) { scale = value; }
}
```

然后创建 screen：

```java
public final class MyConfigScreen {
    public static Screen create(Screen parent) {
        return MuiSettingsScreen.builder("Example Mod")
                .subtitle("Client settings")
                .page("General", "General", "Everyday options", page -> page
                        .section("Rendering", section -> section
                                .toggle("Enable HUD",
                                        MyConfig::enabled,
                                        MyConfig::setEnabled)
                                .slider("Scale", 50, 200,
                                        MyConfig::scale,
                                        MyConfig::setScale,
                                        value -> value + "%")))
                .create(parent);
    }
}
```

打开后你会得到：

- 左侧导航；
- 居中、可滚动的页面；
- 自动生成的分区卡片和分隔线；
- 带说明文字空间的 switch 行；
- 带数值标签的 slider 行。

注意 `toggle` 的第二个参数是 setter，而不是 getter。写成 `MyConfig::enabled` 两次通常在编译期就会暴露问题。

---

## 4 页面、分区与字段

### 4.1 页面

```java
.page("mymod.nav.general", "mymod.page.general", "mymod.page.general.desc", page -> ...)
```

四个参数依次为：

1. 导航项文字；
2. 页面标题；
3. 页面副标题，可为 `null`；
4. 页面构建回调。

只给导航标签时：

```java
.page("General", page -> ...)
```

页面标题会使用导航标签。

### 4.2 分区

```java
.section("Rendering", section -> section
        .toggle(...)
        .slider(...))
```

分区会自动生成标题和卡片，并在字段之间插入分隔线。不要自己再添加卡片背景，否则会出现双层卡片。

### 4.3 字段

`MuiSettingsSection` 提供的常用字段：

| 方法 | 用途 |
|---|---|
| `toggle(title, BooleanSupplier, BooleanConsumer)` | 实时状态开关 |
| `toggle(title, description, BooleanSupplier, BooleanConsumer)` | 带说明的开关 |
| `toggle(title, boolean, BooleanConsumer)` | 静态初值开关 |
| `slider(title, min, max, IntSupplier, IntConsumer)` | 整数滑条 |
| `slider(title, min, max, IntSupplier, IntConsumer, IntFunction<CharSequence>)` | 自定义数值文字 |
| `value(title, CharSequence)` | 静态值行 |
| `value(title, IntSupplier)` | 实时整数值行 |
| `value(title, IntFunction<CharSequence>, IntSupplier)` | 自定义格式的实时值 |
| `text(CharSequence)` | 卡片内段落 |
| `button(label, Runnable)` | 按钮 |
| `divider()` | 显式分隔线 |
| `custom(View)` | 自定义一行，仍自动分隔 |
| `custom(Consumer<LinearLayout>)` | 自定义行容器 |

Supplier 版本每 100 ms 轮询一次。它适合“外部状态偶尔变化”的场景，不适合高频事件；高频数据请直接使用事件回调并在回调中更新控件。

### 4.4 页面级自定义

```java
page.text("A paragraph outside a card.");
page.custom(context -> {
    context.content().addView(myView, MuiControls.fill());
});
```

`MuiSettingsPage.custom` 会直接拿到低层 `MuiPageContext`，适合一小段特殊布局。

---

## 5 低层 API

### 5.1 `MuiConfigScreen`

```java
MuiConfigScreen.builder("Title")
        .subtitle("Subtitle")
        .contentWidth(760)
        .page("General", "General", "Desc", ui -> {
            LinearLayout card = ui.section("Rendering");
            card.addView(ui.switchRow("Enabled", null, true, value -> {}),
                    MuiControls.fill());
        })
        .create(parent);
```

`MuiPageContext` 提供：

- `context()`：Modern UI `Context`；
- `theme()`：当前 `MuiTheme`；
- `content()`：页面内容列；
- `contentLayout()`：同上，但返回可链式构建的 `MuiLayout`；
- `header(title, subtitle)`；
- `section(title)`：添加标题和卡片，返回卡片 body；
- `sectionLayout(title)`：同上，但返回可链式构建的 `MuiLayout`；
- `add(view)`；
- `switchRow(...)`；
- `valueRow(...)`；
- `sliderRow(...)`。

`toggle` 和低层 `switchRow` 的布尔回调都使用 `it.unimi.dsi.fastutil.booleans.BooleanConsumer`；方法引用和 `value -> { }` 形式的 lambda 都可以直接传入。

### 5.2 何时使用低层 API

- 需要多列布局；
- 需要自定义卡片内容；
- 需要把已有页面作为 custom page 嵌入；
- 需要在 section 内直接操作 view。

如果只是普通表单字段，优先使用高层 DSL。

### 5.3 `MuiLayout` 链式布局

`MuiLayout` 是 `LinearLayout` 的子类。它可以直接传给任何接受 `View` 或 `LinearLayout` 的 API，同时把所有常用配置方法变成返回自身的链式调用：

```java
MuiLayout card = MuiLayout.card(context, theme)
        .margin(0, 12, 0, 0)
        .spacing(8)
        .heading("Appearance")
        .text("Changes apply immediately.")
        .button("Reset", MyConfig::reset);
```

构建方法：

```text
MuiLayout.column(context, theme)
MuiLayout.row(context, theme)
MuiLayout.card(context, theme)

horizontal() / vertical()
gravity(int) / center() / centerVertical()
padding(dp) / padding(left, top, right, bottom)
margin(dp) / margin(left, top, right, bottom)
spacing(dp)
minHeight(dp)
background(int color) / background(Drawable)

add(view) / add(view, params)
addWeighted(view, weight) / addAll(view...)
space(dp) / flexibleSpace()

text(text) / text(text, size, color)
heading(text) / heading(text, size, color)
divider()
button(label, action) / button(label, outlined, action)
```

约定：

- 尺寸参数都以 DP 为单位；`text` 的 `size` 以 SP 为单位。
- `add(view)` 使用 `LinearLayout` 的默认布局参数：纵向布局让子项填满宽度，横向布局包裹内容。
- `spacing(dp)` 使用布局自身的 divider 槽位实现子项间距。它不会修改子项的 margin，因此可以和外层间距同时使用；如果之后手动设置 `setDividerDrawable`，间距会被替换。
- `margin(...)` 会记住数值，并在父容器分配 `LayoutParams` 时应用，所以可以在 `add` 之前调用。

低层页面和分区已经在内部使用 `MuiLayout`，对应的入口是：

```java
page.custom(ui -> {
    ui.contentLayout()
            .spacing(8)
            .add(ui.switchRow("Enable HUD", null, true, value -> {}));

    MuiLayout card = ui.sectionLayout("Rendering")
            .add(ui.valueRow("Version", "1.0.0"));
});
```

`MuiSettingsSection` 提供 `cardLayout()`（返回卡片本体）和 `customLayout(Consumer<MuiLayout>)`：

```java
.section("Rendering", section -> section
        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
        .customLayout(row -> row
                .spacing(6)
                .add(section.page().valueRow("Version", "1.0.0"))))
```

### 5.4 `MuiCollapsibleCard` 可折叠卡片

`MuiCollapsibleCard` 是带常驻标题栏和可动画 body 的卡片。标题栏包含：

- 左侧 `+` / `-` 指示；
- 标题；
- 可选摘要；
- 可选 trailing 控件（例如启用开关）。

点击标题栏即可展开/收起 body：

```java
MuiCollapsibleCard.card(context, theme, "FPS", "1 / 4 字段 · (10, 5)", false)
        .trailing(enabledSwitch)
        .summary("1 / 4 字段 · (10, 5)")
        .body(body -> body.add(
                MuiControls.text(context, theme, "Content", 12, theme.onSurfaceVariant())))
        .onExpandedChanged(expanded -> { ... });
```

可用方法：

```text
MuiCollapsibleCard.card(context, theme, title, summary, expanded)
body()
body(Consumer<MuiLayout>)
trailing(view)
summary(text)
expanded(boolean) / setExpanded(boolean) / setExpanded(boolean, animate)
toggle()
isExpanded()
onExpandedChanged(listener)
```

### 5.5 通用链式控件

这些组件原本散落在页面/卡片编辑器的私有实现里，现在都已拆成可独立组合的链式控件：

```java
MuiLayout.row(context, theme)
        .spacing(6)
        .add(MuiNumberField.create(context, theme, 10)
                .bounds(0, 100)
                .width(64)
                .gravity(Gravity.CENTER)
                .onChange(value -> { ... }))
        .add(MuiChip.create(context, theme, "Label", true)
                .onChanged(checked -> { ... }))
        .add(MuiTextAction.create(context, theme, "Reset", () -> { ... }));
```

- `MuiNumberField`：`bounds(min, max)`、`width(dp)`、`gravity(int)`、`onChange(IntConsumer)`、`value()` / `value(int)`；用户输入会 clamp，程序化 `value(int)` 不触发回调。
- `MuiChip`：`checked(boolean)`、`isChecked()`、`onChanged(BooleanConsumer)`；背景是 shimmer target（选中 accent、未选中浅中性色），文字固定黑色。
- `MuiTextAction`：`create(context, theme, label, action)` 或 `create(context, theme, label).onClick(action)`；紧凑的 primary 文本操作。
- `MuiLogo`：圆形 shimmer 标志，`size(dp)` / `color(argb)` / `textColor(argb)`；自动注册和注销 shimmer。
- `MuiHintBadge`：带 shimmer 描边的提示胶囊，`color(argb)` / `textSize(sp)`；文字和描边都可以参与流光。
- `MuiColorField`：色块 + 标签 + pick 按钮，`color(argb)` / `buttonLabel(...)` / `onPick(...)`。
- `MuiDescriptionRow`：标题 + 可选说明，适合用法提示、只读说明行。

```java
MuiLogo.create(context, theme, "N").color(accent).textColor(onAccent);
MuiHintBadge.create(context, theme, "ESC").color(accent);
MuiColorField.create(context, theme, "Border color", argb)
        .buttonLabel("Pick")
        .onPick(() -> { });
MuiDescriptionRow.create(context, theme, "Drag", "Drag a card to move it.");
```

NarutoOverlay 的 Cards、About、Appearance 页面以及 rail 的 logo/hint 现在都直接组合这些组件，而不是依赖页面私有的 UI 代码。

> `MuiCardEditor` 已移除。卡片、位置输入、chip、文本操作和重置逻辑都应该由调用方组合上面的通用组件完成。

---

## 6 主题与控件

### 6.1 `MuiTheme`

```java
MuiTheme theme = MuiTheme.modern(context)
        .withPrimary(0xFFE0A64A)
        .withSpacing(18)
        .withCornerRadius(14);
```

可用 token：

```text
primary, onPrimary,
surface, surfaceLow, surfaceHigh,
onSurface, onSurfaceVariant,
outline, error,
cornerRadius, spacing
```

`MuiTheme.modern(context)` 从 Modern UI 属性解析，解析不到时使用回退色。

### 6.2 全局默认控件工厂

```java
MuiControls.setWidgetFactory(new MyWidgetFactory());
```

这会影响之后创建的控件。

### 6.3 每屏控件工厂

```java
MuiSettingsScreen.builder("Title")
        .widgetFactory(myFactory)
        .page(...)
        .create(parent);
```

screen 构建页面、导航、header/footer 时会临时切换工厂（`MuiControls.withFactory`），因此两个模组可以同时使用不同控件实现。

`MuiWidgetFactory` 负责：

```java
text(context, text, size, color)
switchControl(context, theme)
slider(context, theme, max, progress)
button(context, theme, label, outlined, listener)
```

如果你的控件有动画、自绘或额外生命周期，实现这个接口即可，API 不需要知道具体类。

---

## 7 背景预设

`MuiBackground` 提供常见实现：

```java
.background(MuiBackground.transparent())
.background(MuiBackground.black())
.background(MuiBackground.color(0xCC101116))
.background(MuiBackground.scrim())
.background(MuiBackground.image(path, 0.35D))
.background(MuiBackground.image(path, cropX, cropY, cropWidth, cropHeight, 0.35D))
.background(MuiBackground.slideshow(files, 5.0D, 0.8D, 0.35D))
```

含义：

- `transparent()`：完全透明；
- `black()`：不透明黑；
- `color(argb)`：纯色；
- `scrim()`：默认暗色遮罩；
- `image(...)`：单图，异步解码，可裁剪；
- `slideshow(...)`：图片列表轮播，支持间隔、交叉淡入淡出和调暗。

生命周期：

- 页面 attach 时调用 `attach(root)`；
- 页面 view 销毁时调用 `detach()`；
- 轮播的 timer、淡入动画、`Image` 都由 provider 释放。

自定义背景只需要实现 `MuiBackground`：

```java
public final class MyBackground implements MuiBackground {
    public Drawable create(Context context) { ... }
    public void attach(FrameLayout root) { ... }
    public void detach() { ... }
}
```

如果背景没有额外状态，只实现 `create` 即可。

### 7.1 可配置背景（`MuiConfigurableBackground`）

固定图片或轮播用上面的预设即可。如果希望玩家在配置界面里选择图片/文件夹、裁剪、调整轮播参数，可以用 `MuiConfigurableBackground`。它把文件选择、裁剪、轮播和持久化组合起来，但不直接依赖任何模组的配置类：

```java
MuiBackgroundFiles files = new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), "mymod-config-bg");

MuiConfigurableBackground background = new MuiConfigurableBackground(
        new MyBackgroundSettings(),
        new MuiBackgroundLabels(
                "选择背景图片", "图片文件", "无法打开图片选择器",
                "选择背景文件夹", "无法打开文件夹选择器",
                "无法读取或保存图片", "背景已保存",
                new MuiCropOverlay.Labels("裁剪背景", "拖动选择可见区域", "应用", "取消")),
        files);

Screen screen = MuiSettingsScreen.builder("My Mod")
        .background(background)
        .page("外观", page -> page
                .section("背景", section -> section
                        .button("选择背景图片", background::chooseBackgroundImage)
                        .button("选择背景文件夹", background::chooseBackgroundFolder)))
        .create(parent);
```

`MuiConfigurableBackground` 实现两个接口：

- `MuiBackground`：`create` / `attach` / `detach`，由 `MuiConfigFragment` 自动调用；
- `MuiBackgroundActions`：选择/清除背景、设置间隔/淡入/调暗，以及读取 `state()` 和 `images()`（当前设置与文件夹中的图片列表）。

持久化由 `MuiBackgroundSettings` 负责，这样 API 不绑定 Forge 配置：

```java
public final class MyBackgroundSettings implements MuiBackgroundSettings {
    @Override
    public MuiBackgroundState load() {
        return new MuiBackgroundState(image, folder, order, interval, fade, dim, crop);
    }

    @Override
    public void save(MuiBackgroundState state) {
        // 写回配置并标记 dirty
    }
}
```

裁剪浮层默认使用 `MuiTheme.modern(context)`。需要自定义主题、强调色或控件工厂时，传入 `MuiCropOverlayFactory`：

```java
new MuiConfigurableBackground(settings, labels, files,
        (context, image, w, h, aspect, cropLabels, listener) ->
                new MuiCropOverlay(context, MuiTheme.modern(context).withPrimary(0xFFFFC42E),
                        image, w, h, aspect, cropLabels, listener));
```

`MuiConfigFragment` 在 attach 时会把自身作为 `MuiScreenHost` 传给背景；自定义背景如果需要宿主服务，重写 `attach(MuiScreenHost, FrameLayout)` 即可。`MuiConfigFragment` 本身实现了 `MuiScreenHost`，所以 `onCreated` 拿到的 fragment 可以直接当宿主使用。

---

## 8 本地化

所有公开的标签参数都接受“普通文本”或“翻译键”。

```java
.page("mymod.config.general", page -> page
        .section("mymod.config.rendering", section -> section
                .toggle("mymod.config.enabled", "mymod.config.enabled.desc",
                        MyConfig::enabled, MyConfig::setEnabled)
                .button("mymod.config.reset", MyConfig::reset)))
```

解析规则：

- `I18n.exists(key)` 为真：使用翻译；
- 否则：使用原始字符串；
- 空字符串保持为空。

带参数：

```java
.value("mymod.config.memory",
        MuiLocalization.translatable("mymod.config.megabytes", 8192))
```

会被解析的标签包括：

- 导航、页面标题、副标题；
- section 标题、段落；
- switch 标题和描述；
- slider 标题；
- value row 的标题（不包含值本身）；
- 按钮；
- 主题选择器；
- 卡片编辑器标签；
- 颜色选择器和裁剪框标签。

翻译在构建界面时解析。切换语言后，重新打开界面或调用 `reloadCurrentPage()` 重建页面。

---

## 9 Shimmer 流光

### 9.1 安装配置

```java
MuiShimmerSettings.configure(new MyShimmerConfig());
```

`MyShimmerConfig` 实现 `MuiShimmerConfig`：

```java
public final class MyShimmerConfig implements MuiShimmerConfig {
    public boolean text() { return MyConfig.shimmer(); }
    public boolean configText() { return MyConfig.configTextShimmer(); }
    public boolean controls() { return MyConfig.controlShimmer(); }
    public boolean hasAccent() { return MyConfig.hasAccent(); }
    public int fps() { return MyConfig.shimmerFps(); }
    public double speed() { return MyConfig.speed(); }
    public double scale() { return MyConfig.scale(); }
    public double strength() { return MyConfig.strength(); }
    public int[] ramp() { return MyConfig.ramp(); }
}
```

`MuiShimmerSettings` 配置默认 profile（通常是 HUD）。

### 9.2 每屏 profile

```java
MuiShimmerProfile profile = MuiShimmerProfile.create(new MyShimmerConfig());

MuiSettingsScreen.builder("Title")
        .shimmerProfile(profile)
        .page(...)
        .create(parent);
```

控件在构建时捕获当前 profile。两个 screen 可以使用不同速度、密度、颜色 ramp。

### 9.3 自定义控件参与

实现 `ShimmerTarget`：

```java
public final class MyShimmerWidget extends TextView implements ShimmerTarget {
    private int baseColor = 0xFFFFFFFF;

    public int shimmerBaseColor() { return baseColor; }
    public void applyShimmerColor(int color) { setTextColor(color); }
    public void applyBaseColor() { setTextColor(baseColor); }
    public boolean shimmerEnabled() { return isAttachedToWindow(); }
    public View shimmerHost() { return this; }
}
```

然后在 attach/detach 时调用：

```java
MuiShimmer.attach(this);
MuiShimmer.detach(this);
```

默认 `shimmerProfile()` 返回当前 profile；需要指定 profile 时覆盖它。

### 9.4 时钟

`MuiShimmerClock` 是 profile 内部的实例：

```java
MuiShimmerProfile profile = ...;
profile.clock().update();
double time = profile.clock().time();
```

不要自己创建全局时钟；每个 profile 独立维护 speed/scale/strength 的平滑过渡。

---

## 10 文件选择、颜色选择与裁剪

### 10.1 `MuiFilePicker`

```java
MuiFilePicker.openImage(
        "Choose an image",
        "Images",
        new MuiFilePicker.Callback() {
            public void onPicked(Path path) { ... }
            public void onFailed() { ... }
        });

MuiFilePicker.openFolder(initialPath, "Choose a folder", callback);
```

回调在 UI 线程执行。标题、过滤器由调用方提供，API 不绑定任何模组的翻译键。

### 10.2 `MuiColorPickerOverlay`

```java
host.showOverlay(new MuiColorPickerOverlay(
        context,
        theme,
        initialArgb,
        new MuiColorPickerOverlay.Labels(
                "mymod.color.title",
                "mymod.color.brightness",
                "mymod.color.alpha",
                "mymod.color.apply",
                "mymod.color.cancel"),
        argb -> { ... }));
```

`showOverlay` 由宿主决定；NarutoOverlay 使用 `MuiConfigFragment.showOverlay`，其他模组可以放在自己的 FrameLayout 上。

### 10.3 `MuiCropOverlay`

```java
new MuiCropOverlay(
        context,
        theme,
        previewImage,
        imageWidth,
        imageHeight,
        targetAspect,
        new MuiCropOverlay.Labels(
                "mymod.crop.title",
                "mymod.crop.hint",
                "mymod.crop.confirm",
                "mymod.crop.cancel"),
        listener);
```

裁剪结果是以源图像像素为单位的 `(x, y, width, height)`。

---

## 11 屏幕恢复与稳定 ID

### 11.1 进程内恢复

```java
MuiConfigScreen.builder("Title")
        .page(...)
        .create(parent)
```

每次调用 `builder().create(parent)` 会创建一个 UUID，把 model 放进进程内 registry，并通过 `MuiConfigFragment.ARG_MODEL` 传给 fragment。

`MuiConfigFragment`：

- 有 public no-arg 构造；
- 在 `onCreate` 中从 registry 取 model；
- 保存当前页和每页滚动位置；
- 取不到 model 时显示 recovery 页面而不是崩溃。

### 11.2 稳定 ID

需要跨进程重启恢复时：

```java
MuiSettingsScreen.register("mymod:config", "My Mod", builder -> builder
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("mymod.config.enabled",
                                MyConfig::enabled, MyConfig::setEnabled))));

Screen screen = MuiSettingsScreen.createRegistered("mymod:config", parent);
```

注册必须在客户端初始化时完成。recipe 不能依赖一次性局部变量；所有状态都应来自可重建的配置对象。

低层等价：

```java
MuiConfigScreen.register(...);
MuiConfigScreen.createRegistered(...);
```

### 11.3 `MuiConfigFragmentFactory`

如果宿主允许设置 `FragmentManager#setFragmentFactory`，可以使用：

```java
manager.setFragmentFactory(new MuiConfigFragmentFactory());
```

标准 `MuiModApi.createScreen` 路径不需要它。

---

## 12 完整示例

```java
public final class ExampleScreen {
    private static final String ID = "example:config";

    public static void register() {
        MuiSettingsScreen.register(ID, "Example Mod", ExampleScreen::build);
    }

    public static Screen create(Screen parent) {
        return MuiSettingsScreen.createRegistered(ID, parent);
    }

    private static void build(MuiSettingsScreen.Builder builder) {
        builder.subtitle("Client settings")
                .contentWidth(820)
                .background(MuiBackground.scrim())
                .page("General", "General", "Everyday options", page -> page
                        .section("Rendering", section -> section
                                .toggle("Enable HUD",
                                        MyConfig::enabled,
                                        MyConfig::setEnabled)
                                .slider("Scale", 50, 200,
                                        MyConfig::scale,
                                        MyConfig::setScale,
                                        value -> value + "%")
                                .value("FPS", MyMetrics::fps))
                        .section("Diagnostics", section -> section
                                .value("Session", "1h 20m")
                                .button("Reset", MyConfig::reset)))
                .page("About", "About", "Example Mod", page -> page
                        .section("Information", section -> section
                                .text("A small example configuration screen.")
                                .value("Version", "1.0.0")));
    }
}
```

客户端初始化：

```java
ExampleScreen.register();
```

Forge 配置入口：

```java
context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
        () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, previous) -> ExampleScreen.create(previous)));
```

---

## 13 限制与常见问题

### 13.1 API 不保存配置

所有 setter 只负责改你的状态。需要保存时，在回调里调用你自己的配置保存逻辑：

```java
.toggle("Enabled", MyConfig::enabled, value -> {
    MyConfig.setEnabled(value);
    MyConfig.save();
})
```

### 13.2 页面是预先构建的

所有页面在 screen 创建时构建。页面很多时，创建会变慢。需要重新构建当前页时调用 `reloadCurrentPage()`。

### 13.3 Supplier 不是事件推送

supplier 控件每 100 ms 轮询。高频变化的数据应该使用事件回调，而不是 supplier。

### 13.4 Slider 的静态值不会自动更新

带 `CharSequence value` 的 `sliderRow` 只设置初始文字。需要实时读数时使用 supplier + formatter 版本。

### 13.5 Modern UI 是硬依赖

`MuiConfigScreen` 直接调用 `MuiModApi.createScreen`。如果消费方没有 Modern UI，配置界面无法创建。

### 13.6 稳定 recipe 不是持久化配置

`register` 只注册界面构建配方，不保存用户配置。它必须在每次客户端初始化时重新执行。

### 13.7 不要在 recovery 页面里调用业务回调

model 缺失时，API 只能显示 recovery view。不要在自定义 background 或页面构造里假设 model 一定存在。

---

## 14 API 速查

### 高层入口

```text
MuiSettingsScreen.builder(title)
    .subtitle(text)
    .footer(text)
    .theme(theme)
    .widgetFactory(factory)
    .shimmerProfile(profile)
    .contentWidth(dp)
    .background(background)
    .initialPage(index)
    .railHeader(factory)
    .railFooter(factory)
    .onCreated(callback)
    .onDestroyed(callback)
    .page(nav, body)
    .page(nav, title, subtitle, body)
    .customPage(nav, factory)
    .create(parent)
```

### 页面/分区

```text
page.section(title, section -> ...)
page.text(text)
page.custom(context -> ...)

section.toggle(...)
section.slider(...)
section.value(...)
section.text(...)
section.button(...)
section.divider()
section.custom(...)
section.customLayout(row -> ...)
section.cardLayout()
```

### 链式布局

```text
MuiLayout.column(context, theme)
MuiLayout.row(context, theme)
MuiLayout.card(context, theme)
layout.add(view) / add(view, params)
layout.addWeighted(view, weight) / addAll(view...)
layout.space(dp) / flexibleSpace()
layout.text(...) / heading(...) / divider() / button(...)
layout.padding(...) / margin(...) / spacing(...)
layout.horizontal() / vertical() / gravity(...)
layout.minHeight(...) / background(...)
page.contentLayout()
page.sectionLayout(title)
```

### 可折叠卡片 / 通用控件

```text
MuiCollapsibleCard.card(context, theme, title, summary, expanded)
card.trailing(view) / summary(text) / body() / body(consumer)
card.expanded(boolean) / toggle() / isExpanded() / onExpandedChanged(listener)

MuiNumberField.create(context, theme, value)
field.bounds(min, max) / width(dp) / gravity(int) / onChange(IntConsumer)
field.value() / value(int)

MuiChip.create(context, theme, label, checked)
chip.checked(boolean) / isChecked() / onChanged(BooleanConsumer)

MuiTextAction.create(context, theme, label, action)
MuiTextAction.create(context, theme, label).onClick(action)

MuiLogo.create(context, theme, glyph)
logo.size(dp) / color(argb) / textColor(argb)

MuiHintBadge.create(context, theme, text)
badge.color(argb) / textSize(sp)

MuiColorField.create(context, theme, label, argb)
field.color(argb) / buttonLabel(label) / onPick(action)

MuiDescriptionRow.create(context, theme, title, description)
```

### 主题

```text
MuiTheme.modern(context)
theme.withPrimary(argb)
theme.withSpacing(dp)
theme.withCornerRadius(dp)
```

### 背景

```text
MuiBackground.transparent()
MuiBackground.black()
MuiBackground.color(argb)
MuiBackground.scrim()
MuiBackground.image(path, dim)
MuiBackground.image(path, cropX, cropY, cropWidth, cropHeight, dim)
MuiBackground.slideshow(files, interval, fade, dim)
MuiConfigurableBackground(settings, labels, files)
MuiBackgroundSettings.load() / save(state)
MuiBackgroundLabels(...)
MuiBackgroundFiles(gameDir, baseName)
MuiScreenHost（MuiConfigFragment 实现）
```

### 恢复

```text
MuiConfigScreen.register(id, title, recipe)
MuiConfigScreen.createRegistered(id, parent)
MuiSettingsScreen.register(id, title, recipe)
MuiSettingsScreen.createRegistered(id, parent)
MuiConfigFragment.ARG_MODEL
MuiConfigFragmentFactory
```

### 工具

```text
MuiLocalization.resolve(text)
MuiLocalization.translatable(key, args...)
MuiFilePicker.openImage(title, filter, callback)
MuiFilePicker.openFolder(initial, title, callback)
MuiColorPickerOverlay
MuiCropOverlay
MuiShimmerProfile
MuiShimmer
MuiShimmerClock
```
