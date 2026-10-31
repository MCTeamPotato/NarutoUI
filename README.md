# NarutoUI

NarutoUI 是一个面向 Minecraft 客户端模组的 **Modern UI 配置界面库**。

它不提供某一套固定的配置内容，而是提供一整套界面骨架：导航栏、页面、分区、字段、主题、背景、本地化、流光、文件选择、颜色选择和图片裁剪。其他模组只需要声明“要显示哪些页面、每个页面有哪些字段”，不需要重复编写布局和控件代码。

> 第一次使用？建议先读 [EASY_START.md](docs/EASY_START.md)，它按“接入 → 最小界面 → 自定义布局 → 背景/流光”的顺序讲解。完整 API 参考见 [API-TUTOR.md](docs/API-TUTOR.md)。想看 NarutoOverlay 的真实代码怎么用这套 API，读 [EXAMPLE-BASED-TUTOR.md](docs/EXAMPLE-BASED-TUTOR.md)。

## 特性

- 声明式页面 / 分区 / 字段 DSL
- 可链式构建的 `MuiLayout`（`LinearLayout` 子类）
- 可折叠卡片 `MuiCollapsibleCard`（加号/减号展开，带动画）
- 通用控件 `MuiNumberField` / `MuiChip` / `MuiTextAction` / `MuiLogo` / `MuiHintBadge` / `MuiColorField` / `MuiDescriptionRow`
- 可滚动、居中、限制宽度的页面
- 自动生成 section 卡片、行布局和分隔线
- switch、slider、value row、button、text 等常用字段
- 可配置主题、圆角、间距、内容宽度
- 透明、黑屏、纯色、单图、轮播背景
- 翻译键直接作为标签使用
- 独立的 shimmer 流光系统
- 颜色选择器、图片裁剪框、原生文件/文件夹选择器
- 每屏独立的控件工厂和 shimmer profile
- 进程内页面恢复与 stable recipe 注册

> NarutoUI 是界面库，不负责保存配置。字段回调只负责修改你的配置对象，保存逻辑由调用方实现。

## 快速开始

```java
import me.kall.narutoui.mui.screen.MuiSettingsScreen;
import net.minecraft.client.gui.screens.Screen;

public final class MyConfigScreen {
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
                                        value -> value + "%")))
                .create(parent);
    }
}
```

页面的 header、卡片、字段行、分隔线和控件样式都由 API 生成。

### 单页模式

如果配置内容很少，不需要左侧页面导航，可以启用单页模式：

```java
Screen screen = MuiSettingsScreen.builder("Example Mod")
        .singlePage()
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
                        .slider("Scale", 50, 200,
                                MyConfig::scale, MyConfig::setScale,
                                value -> value + "%")))
        .create(parent);
```

单页模式下：

- 不创建左侧导航栏
- 只允许添加一个页面
- 页面直接占满内容区
- `railHeader` / `railFooter` / `subtitle` / `footer` 不再显示
- 页面可以通过 `.page("", ...)` 省略标题

如果添加了多个页面，构建时会抛出 `IllegalStateException`。

### 顶部导航

多页界面默认使用左侧竖向导航，也可以改成顶部横向导航：

```java
MuiSettingsScreen.builder("Example Mod")
        .topNavigation()
        .page("General", page -> ...)
        .page("About", page -> ...)
        .create(parent);
```

也可以显式指定：

```java
.navigation(MuiNavigation.TOP)
.navigation(MuiNavigation.LEFT)
```

顶部导航会显示标题、副标题和横向排列的页面项；页面较多时导航区域可以横向滚动。

### Shimmer 控件

API 自带一套 shimmer 控件工厂：

```java
MuiSettingsScreen.builder("Example Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .theme(MuiTheme.modern(context)
                .withPrimary(0xFFE0A64A)
                .withOnPrimary(0xFF000000))
        .page("General", page -> ...)
        .create(parent);
```

它会统一创建：

- `ShimmerTextView`
- `ShimmerSwitch`
- `ShimmerSeekBar`
- `ShimmerButton`

实心按钮默认使用 `theme.onPrimary()`，文字不参与流光；描边按钮文字继续流光。调用方只需要提供主题颜色，不需要自己创建这些控件。

## 页面与字段

```java
.section("Rendering", section -> section
        .toggle("Enable HUD", "Draw the overlay", MyConfig::enabled, MyConfig::setEnabled)
        .slider("Scale", 50, 200, MyConfig::scale, MyConfig::setScale, value -> value + "%")
        .value("FPS", MyMetrics::fps)
        .text("Changes apply immediately.")
        .button("Reset", MyConfig::reset))
```

常用字段：

- `toggle(title, BooleanSupplier, BooleanConsumer)`
- `toggle(title, description, BooleanSupplier, BooleanConsumer)`
- `slider(title, min, max, IntSupplier, IntConsumer)`
- `slider(..., IntFunction<CharSequence> formatter)`
- `value(title, CharSequence / IntSupplier)`
- `button(label, Runnable)`
- `text(CharSequence)`
- `custom(View / Consumer<LinearLayout>)`

## 链式布局

`MuiLayout` 是 `LinearLayout` 的链式封装。它本身仍然是 `LinearLayout`，可以直接传给任何接受 `View` 或 `LinearLayout` 的 API；所有配置方法都返回自身：

```java
MuiLayout card = MuiLayout.card(context, theme)
        .margin(0, 12, 0, 0)
        .spacing(8)
        .heading("Appearance")
        .text("Changes apply immediately.")
        .button("Reset", MyConfig::reset);
```

可用的构建方法：

- `column(Context, MuiTheme)` / `row(Context, MuiTheme)` / `card(Context, MuiTheme)`
- `horizontal()` / `vertical()` / `gravity(int)` / `center()` / `centerVertical()`
- `padding(dp)` / `padding(left, top, right, bottom)`
- `margin(dp)` / `margin(left, top, right, bottom)`（在加入父容器后仍然生效）
- `spacing(dp)`（使用布局的 divider 槽位实现子项间距）
- `minHeight(dp)` / `background(color / Drawable)`
- `add(View)` / `add(View, LayoutParams)` / `addWeighted(View, weight)` / `addAll(View...)`
- `space(dp)` / `flexibleSpace()`
- `text(...)` / `heading(...)` / `divider()` / `button(...)`

低层页面已经提供了对应的入口：

```java
page.custom(ui -> {
    ui.contentLayout()
            .spacing(8)
            .add(ui.switchRow("Enable HUD", null, true, value -> {}));

    MuiLayout card = ui.sectionLayout("Rendering")
            .add(ui.valueRow("Version", "1.0.0"));
});
```

`MuiSettingsSection` 还提供 `cardLayout()` 和 `customLayout(Consumer<MuiLayout>)`：

```java
.section("Rendering", section -> section
        .toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)
        .customLayout(row -> row
                .spacing(6)
                .add(section.page().valueRow("Version", "1.0.0"))))
```

## 可折叠卡片

`MuiCollapsibleCard` 把“常驻标题栏 + 可展开内容”封装成一个卡片。标题栏左侧是 `+` / `-`，点击标题栏会以动画展开或收起 body：

```java
// enabledSwitch 是你自己的开关控件，例如 MuiControls.switchControl(...)
MuiCollapsibleCard.card(context, theme, "FPS", "1 / 4 字段 · (10, 5)", false)
        .trailing(enabledSwitch)
        .summary("1 / 4 字段 · (10, 5)")
        .body(body -> body
                .add(MuiControls.text(context, theme, "Content", 12, theme.onSurfaceVariant())));
```

## 通用控件

卡片、输入框、chip 和文本操作是彼此独立的组件，可以像 `MuiLayout` 一样自由组合：

```java
MuiLayout.row(context, theme)
        .spacing(6)
        .add(MuiNumberField.create(context, theme, 10)
                .bounds(0, 100)
                .width(64)
                .onChange(value -> { }))
        .add(MuiChip.create(context, theme, "Label", true)
                .onChanged(checked -> { }))
        .add(MuiTextAction.create(context, theme, "Reset", () -> { }));
```

- `MuiNumberField`：只接受数字、支持范围 clamp、`onChange` 回调和链式 `value` / `bounds` / `width` / `gravity` / `enabled`
- `MuiChip`：背景参与 shimmer 的小开关 chip，文字固定黑色；支持 `label` / `checked` / `isChecked` / `enabled` / `onChanged`
- `MuiTextAction`：紧凑的 primary 文本操作；支持 `text` / `label` / `enabled`
- `MuiLogo`：带圆形 shimmer 背景的 rail / header 标志；支持 `glyph` / `size` / `color`，自动管理 shimmer 注册
- `MuiHintBadge`：带 shimmer 描边的提示胶囊；支持 `text` / `color` / `textSize`，文字和描边都可参与流光
- `MuiColorField`：色块 + 标签 + pick 按钮，适合边框色 / 强调色；支持 `label` / `color` / `enabled`
- `MuiDescriptionRow`：标题 + 可选说明，适合用法提示和只读说明行；支持 `title` / `description`

```java
MuiLogo.create(context, theme, "N").color(accent).textColor(onAccent);
MuiHintBadge.create(context, theme, "ESC").color(accent).text("ESC");
MuiColorField.create(context, theme, "Border color", argb)
        .label("Border color")
        .buttonLabel("Pick")
        .onPick(() -> { });
MuiDescriptionRow.create(context, theme, "Drag", "Drag a card to move it.")
        .description("Changed");
```

> `MuiCardEditor` 已从 API 中移除，原来的 `Model` / `Field` 适配层不再需要。卡片直接组合 `MuiCollapsibleCard` + 这些通用控件；NarutoOverlay 的 Cards 页面就是这样实现的。

## 主题与背景

```java
MuiTheme theme = MuiTheme.modern(context)
        .withPrimary(0xFFE0A64A)
        .withSurface(0xCC101116)
        .withCornerRadius(14);

MuiBackground background = MuiBackground.slideshow(files, 5.0D, 0.8D, 0.35D);
```

可用背景：

```java
MuiBackground.transparent()
MuiBackground.black()
MuiBackground.color(0xCC101116)
MuiBackground.scrim()
MuiBackground.image(path, dim)
MuiBackground.image(path, cropX, cropY, cropWidth, cropHeight, dim)
MuiBackground.slideshow(files, interval, fade, dim)
```

## 本地化

所有公开标签参数都可以直接写翻译键：

```java
.page("mymod.config.general", page -> page
        .section("mymod.config.rendering", section -> section
                .toggle("mymod.config.enabled",
                        "mymod.config.enabled.desc",
                        MyConfig::enabled,
                        MyConfig::setEnabled)))
```

标签会通过 `MuiLocalization` 解析；不存在对应键时回退到原始字符串。

带参数的文本：

```java
.value("mymod.config.memory",
        MuiLocalization.translatable("mymod.config.megabytes", 8192))
```

## Shimmer 流光

```java
MuiShimmerProfile profile = MuiShimmerProfile.create(new MyShimmerConfig());

MuiSettingsScreen.builder("Title")
        .shimmerProfile(profile)
        .page(...)
        .create(parent);
```

每个 profile 拥有独立的时间、速度、密度、强度和颜色 ramp。`MuiShimmerSettings` 用于配置默认 HUD profile。

自定义控件实现 `ShimmerTarget` 后，通过 `MuiShimmer.attach/detach` 接入即可。

## 文件、颜色与裁剪

```java
MuiFilePicker.openImage("Choose an image", "Images", callback);
MuiFilePicker.openFolder(initialPath, "Choose a folder", callback);
```

- `MuiColorPickerOverlay`：HSV 色轮、亮度、Alpha、HEX 和 Apply/Cancel
- `MuiCropOverlay`：固定宽高比的图片裁剪框

两者的标签都通过 `Labels` 传入，支持翻译键。

## 屏幕恢复

默认的 `MuiConfigScreen.create(parent)` 使用进程内 registry。`MuiConfigFragment` 有 public 无参构造，可以在 FragmentManager 重建时恢复当前页和滚动位置。

需要跨进程重启恢复时，注册 stable recipe：

```java
MuiSettingsScreen.register("mymod:config", "My Mod", builder -> builder
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("mymod.config.enabled",
                                MyConfig::enabled,
                                MyConfig::setEnabled))));

Screen screen = MuiSettingsScreen.createRegistered("mymod:config", parent);
```

recipe 必须在客户端初始化时重新注册，且不能依赖一次性局部状态。

## 构建

```text
gradlew build
```

产物：

```text
build/libs/narutoui-xxx-xxx.jar
```

## 目录

```text
src/main/java/me/kall/narutoui/mui/
├── screen/       入口、Fragment、页面、section、MuiScreenHost
├── widget/       链式布局与通用控件
├── theme/        MuiTheme、控件工厂
├── background/   静态/图片/幻灯片背景、可配置背景
├── shimmer/      流光 profile、绘制原语、流光控件
├── overlay/      颜色选择、裁剪浮层
└── util/         本地化、文件/文件夹选择
```

## 限制

- Modern UI 是硬依赖，配置界面只能在客户端创建。
- API 不负责配置持久化。
- 页面在 screen 创建时预先构建；页面很多时创建会变慢。
- Supplier 字段每 100 ms 轮询，不是事件推送。
- 静态 slider 数值标签不会自动刷新，需要实时读数时使用 supplier + formatter 版本。
- stable recipe 不是配置文件持久化；进程重启后需要在客户端初始化时重新注册。

## License

LGPL-v3，作者 Kasualix 与 DeepSeek-V4.1-Flash-1017。