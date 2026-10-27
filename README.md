# NarutoUI

NarutoUI 是一个面向 Minecraft 客户端模组的 **Modern UI 配置界面库**。

它不提供某一套固定的配置内容，而是提供一整套界面骨架：导航栏、页面、分区、字段、主题、背景、本地化、流光、文件选择、颜色选择和图片裁剪。其他模组只需要声明“要显示哪些页面、每个页面有哪些字段”，不需要重复编写布局和控件代码。

## 特性

- 声明式页面 / 分区 / 字段 DSL
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
import me.kall.narutoui.mui.MuiSettingsScreen;
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

## 主题与背景

```java
MuiTheme theme = MuiTheme.modern(context)
        .withPrimary(0xFFE0A64A)
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
├── MuiConfigScreen.java
├── MuiSettingsScreen.java
├── MuiSettingsPage.java
├── MuiSettingsSection.java
├── MuiTheme.java
├── MuiWidgetFactory.java
├── MuiLocalization.java
├── MuiFilePicker.java
├── background/
├── shimmer/
└── widget/
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