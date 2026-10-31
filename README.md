# NarutoUI

NarutoUI 是一个面向 Minecraft 客户端模组的 **Modern UI 配置界面库**。

它不提供某一套固定的配置内容，而是提供一整套界面骨架：导航栏、页面、分区、字段、主题、背景、本地化、流光、文件选择、颜色选择和图片裁剪。其他模组只需要声明“要显示哪些页面、每个页面有哪些字段”，不需要重复编写布局和控件代码。

> 第一次使用？建议先读 [EASY_START.md](docs/EASY_START.md)，它按“接入 → 最小界面 → 自定义布局 → 背景/流光”的顺序讲解。完整 API 参考见 [API-TUTOR.md](docs/API-TUTOR.md)。想看 NarutoOverlay 的真实代码怎么用这套 API，读 [EXAMPLE-BASED-TUTOR.md](docs/EXAMPLE-BASED-TUTOR.md)。

## 特性

- 声明式页面 / 分区 / 字段 DSL
- 可链式构建的 `MuiLayout`（`LinearLayout` 子类）
- 可折叠卡片 `MuiCollapsibleCard`（加号/减号展开，带动画）
- 通用控件 `MuiNumberField` / `MuiTextField` / `MuiDropdown` / `MuiChoiceGrid` / `MuiListEditor` / `MuiChip` / `MuiTextAction` / `MuiLogo` / `MuiHintBadge` / `MuiColorField` / `MuiDescriptionRow`
- 可滚动、居中、限制宽度的页面
- 自动生成 section 卡片、行布局和分隔线
- switch、int/double slider、number、text、dropdown、value row、button、text 等常用字段
- 整数 / 长整数 / 小数输入，支持负数、范围校验和编辑提交
- 字符串列表编辑器、枚举选项与卡片选择网格
- 纯组件设计：配置持久化、草稿 / Apply 由调用方维护
- 可配置主题、圆角、间距、内容宽度
- 透明、黑屏、纯色、单图、轮播背景
- 翻译键直接作为标签使用
- 实例级 `MuiShimmerable` 标记，可在构造/链式代码或 `shimmerNext(...)` 中决定单个组件是否流光
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
- `ShimmerCheckBox`
- `ShimmerRadioButton`
- `ShimmerProgressBar`

实心按钮默认使用 `theme.onPrimary()`，文字不参与流光；描边按钮文字继续流光。每个控件创建时都带一个 `MuiShimmerable` 标记，默认 `NEVER`；可以用构造器、`.shimmerable(...)` 或 DSL 的 `shimmerNext()` / `shimmerNext(marker)` 单独开启。工厂还提供 `checkBox(...)`、`radioButton(...)`、`progressBar(...)` 三个 shimmer 版本。

## 页面与字段

```java
.section("Rendering", section -> section
        .toggle("Enable HUD", "Draw the overlay", MyConfig::enabled, MyConfig::setEnabled)
        .slider("Scale", 50, 200, MyConfig::scale, MyConfig::setScale, value -> value + "%")
        .slider("Opacity", 0.2D, 1.0D, 0.05D, MyConfig::opacity, MyConfig::setOpacity, value -> Math.round(value * 100) + "%")
        .number("Max entities", null, 1, 4096, MyConfig::maxEntities, MyConfig::setMaxEntities)
        .decimal("Gravity", null, 0.0D, 2.0D, MyConfig::gravity, MyConfig::setGravity)
        .textField("Display name", "Shown in the HUD", MyConfig::displayName, MyConfig::setDisplayName)
        .dropdownBound("Mode", null, modes, MyConfig::mode, MyConfig::setMode)
        .value("FPS", MyMetrics::fps)
        .text("Changes apply immediately.")
        .button("Reset", MyConfig::reset))
```

常用字段：

- `toggle(title, BooleanSupplier, BooleanConsumer)`
- `toggle(title, description, BooleanSupplier, BooleanConsumer)`
- `slider(title, min, max, IntSupplier, IntConsumer)`
- `slider(title, min, max, IntSupplier, IntConsumer, IntFunction<CharSequence> formatter)`
- `slider(title, min, max, step, DoubleSupplier, DoubleConsumer, DoubleFunction<CharSequence> formatter)`
- `number(title, description, min, max, IntSupplier, IntConsumer)`
- `longNumber(title, description, min, max, LongSupplier, LongConsumer)`
- `decimal(title, description, min, max, DoubleSupplier, DoubleConsumer)`
- `textField(title, description, String, Consumer<String> onCommit)`
- `textField(title, description, Supplier<String>, Consumer<String> onCommit)`
- `dropdown(title, description, List<MuiOption<T>>, T, Consumer<T>)`
- `dropdownBound(title, description, List<MuiOption<T>>, Supplier<T>, Consumer<T>)`
- `choiceGrid(title, description, List<MuiOption<T>>, T, Consumer<T>)`
- `choiceGridBound(title, description, List<MuiOption<T>>, Supplier<T>, Consumer<T>)`
- `value(title, CharSequence / IntSupplier)`
- `button(label, Runnable)`
- `text(CharSequence)`
- `description(title)` / `description(title, description)`
- `note(Note)` / `note(title)` / `note(title, description)`
- `custom(View)` / `customLayout(Consumer<MuiLayout>)`

`textField` 只在按 Enter 或失焦时提交，不会把输入到一半的字符串写进配置；需要实时反馈时可以直接使用 `MuiTextField.onChange(...)`。`dropdown` 适合选项较多或需要紧凑排列的场合；`choiceGrid` 是有状态的卡片网格，适合 enum、主题和带说明的可视化选项：

```java
List<MuiOption<Mode>> modes = List.of(
        MuiOption.of(Mode.OFF, "mymod.mode.off"),
        MuiOption.of(Mode.SIMPLE, "mymod.mode.simple"),
        MuiOption.of(Mode.FULL, "mymod.mode.full").description("Full effects"));

.section("Rendering", section -> section
        .choiceGridBound("Mode", "Render mode", modes, MyConfig::mode, MyConfig::setMode))
```

`MuiOption` 同时被 `MuiDropdown` 和 `MuiChoiceGrid` 使用；`MuiEnumOptions.of(Mode.class, "mymod.config.mode")` 可以从枚举常量直接生成选项（翻译键缺失时回退到 humanize 后的常量名）。

`description` 加一行“标题 + 可选说明”的只读行；连续的 `note` 会把多行说明包进同一个块，内部不再插分隔线，遇到其他行自动结束：

```java
.section("Usage", section -> section
        .description("Drag", "Drag a card to move it.")
        .note("Merge", "Drop one card onto another.")
        .note("Split", "Double-click a group."))
```

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
                .onChangeLong(value -> { }))
        .add(MuiChip.create(context, theme, "Label", true)
                .onChanged(checked -> { }))
        .add(MuiTextAction.create(context, theme, "Reset", () -> { }));
```

- `MuiNumberField`：整数 / 长整数 / 小数输入；支持负数、范围 clamp、`onChangeInt` / `onChangeLong` / `onChangeDouble` 回调和链式 `value` / `bounds` / `scale` / `width` / `gravity` / `enabled`
- `MuiTextField`：通用文本输入；支持 `hint` / `maxLength` / `multiline` / `validator` / `onChange` / `onCommit` / `text` / `enabled`
- `MuiDropdown`：内嵌展开的 select 控件；支持 `Option` 列表、`selected` / `selectedIndex` / `onSelected` / `placeholder` / `enabled`
- `MuiChip`：背景参与 shimmer 的小开关 chip，文字固定黑色；支持 `label` / `checked` / `isChecked` / `enabled` / `onChanged`
- `MuiTextAction`：紧凑的 primary 文本操作；支持 `text` / `label` / `enabled` / `flash`
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

## 列表编辑

`MuiListEditor` 用于字符串列表配置（对应 Forge 的 `defineList` / `defineListAllowEmpty`）。每条记录是一个 `MuiTextField`，失焦或按 Enter 时提交；加号/减号会先提交当前输入再重建行，不会丢掉正在编辑的内容：

```java
.section("Filters", section -> section
        .list("Allowed names", "One entry per line", MyConfig.allowedNames,
                MyConfig::setAllowedNames))
```

也可以直接组合控件：

```java
MuiListEditor.create(context, theme, List.of("minecraft", "forge"))
        .addLabel("Add entry")
        .itemPlaceholder("mod id")
        .maxEntries(32)
        .onChanged(values -> { });
```

## 直接绑定 getter / setter

组件不负责配置持久化，也不绑定任何 loader 的 Config API。字段只接收 `Supplier` / `Consumer`，所以可以直接接 Forge / NeoForge 的 `ConfigValue`：

```java
.toggle("Enabled", MyConfig.ENABLED::get, MyConfig.ENABLED::set)
.number("Count", null, 1, 10, MyConfig.COUNT::get, MyConfig.COUNT::set)
.decimal("Ratio", null, 0.0D, 1.0D, MyConfig.RATIO::get, MyConfig.RATIO::set)
.textField("Label", null, MyConfig.LABEL::get, MyConfig.LABEL::set)
```

`BooleanValue` / `IntValue` / `LongValue` / `DoubleValue` 本身就能当对应的 supplier / consumer 使用，方法引用会自动拆装箱。列表因为 `ConfigValue<List<? extends String>>` 和 `List<String>` 不是同一类型，需要调用方包一层：

```java
.list("Names", null, MyConfig.NAMES.get(), names -> MyConfig.NAMES.set(names))
```

写回时机、配置文件保存、`ModConfigEvent` 刷新、server 配置同步都由调用方决定；库只负责把控件值交回给你的 setter。

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

`MuiThemePicker` 现在只是 `MuiChoiceGrid` 的主题预设，不再持有 `select` 回调或 `selected` 快照。候选主题是 `Choice`，选择结果通过 `Consumer<MuiTheme>` 返回：

```java
List<MuiThemePicker.Choice> choices = List.of(
        MuiThemePicker.Choice.of(darkTheme, "mymod.theme.dark", "Dark"),
        MuiThemePicker.Choice.of(lightTheme, "mymod.theme.light", "Light"));

.section("Appearance", section -> section
        .themePicker(choices, MyConfig::setTheme))
```

卡片网格自身保存选中状态，点击后立即更新高亮；当前选中值不在选项里时会显示 “Unknown value” 卡片，而不是静默清空。

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
        .widgetFactory(MuiWidgetFactory.shimmer())
        .shimmerProfile(profile)
        .page(...)
        .create(parent);
```

默认工厂是 `MuiWidgetFactory.modern()`，它创建普通控件，没有 shimmer target；要使用 `shimmerNext()` 必须先切到 `MuiWidgetFactory.shimmer()`。

每个 profile 拥有独立的时间、速度、密度、强度和颜色 ramp。所有 shimmer 默认关闭，单个组件是否参与由它自己的 `MuiShimmerable` 标记决定：

```java
new ShimmerTextView(context);                       // 默认 NEVER
new ShimmerTextView(context, MuiShimmerable.ALWAYS); // 显式开启
new ShimmerButton(context)
        .shimmerable(MuiShimmerable.when(MyConfig::controlsShimmer));
```

`MuiShimmerable.when(BooleanSupplier)` 适合动态判定，条件每帧求值；条件从 false 变 true 且引擎已停止时，需要调用 `MuiShimmer.refresh()` 重新启动。`MuiShimmerable.of(boolean)` 是固定开关。全局 `MuiShimmerConfig.enabled()` 仍然是总开关。

在 DSL 中，用无参 `shimmerNext()` 开启紧接着的那一行，或用 `shimmerNext(marker)` 传入自定义标记：

```java
.section("Rendering", section -> section
        .shimmerNext()                 // 这一行开启 shimmer
        .toggle("Enabled", ...)
        .shimmerNext(MuiShimmerable.NEVER)
        .slider("Scale", ...)          // 这一行关闭
        .button("Reset", ...))         // 后续行默认关闭
```

页面级和外壳级也提供了对应的入口：

```java
MuiSettingsScreen.builder("Title")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .shimmerNavigation()          // 导航栏所有页面标签
        .shimmerTitles()              // rail/top bar 的标题、副标题、footer
        .page("General", page -> page
                .shimmerHeader()      // 页面标题 + 副标题
                .shimmerNext()        // 下一个 section / text / custom
                .section("Rendering", section -> ...))
        .create(parent);
```

`page.shimmerNext()` 作用于下一个页面块：`section` 会让该 section 的标题和所有行一起开启；`text` 只作用于该段落；`custom` / `customLayout` 只作用于回调中新加的子视图。页面标题在页面 body 之前创建，所以用独立的 `shimmerHeader()` 而不是 `shimmerNext()`。

标记会递归应用到该行/该块创建的 label、控件和 drawable（包括 dropdown / choice grid 之后重建的行），且不会影响后续行。

自定义控件实现 `ShimmerTarget` 后，通过 `MuiShimmer.attach/detach` 接入，并实现 `setShimmerable(...)` 保存标记；`shimmerEnabled()` 只报告本地状态（attached / enabled / bound / marker），不要再读取全局 config。

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
build/libs/narutoui-xxx-xxx.jar         完整模组（含 NarutoUI 入口点）
build/libs/narutoui-xxx-xxx-common.jar  仅 me/kall/narutoui/mui/**
```

`commonJar` 是给其他模组 shadow 用的：

- 不包含 `NarutoUI` 入口点
- 不包含 `META-INF/mods.toml` / `neoforge.mods.toml` 或其他模组元数据
- 只包含 `me/kall/narutoui/mui/**` 的 remapped 类
- 仍然需要 Minecraft client 和 Modern UI；其中 screen / background / file picker 使用 `icyllis.modernui.mc.MuiModApi`，纯控件部分只使用 Modern UI Core
- 已经过 remap，和当前 Forge/NeoForge + Minecraft 版本绑定；跨 loader 或跨版本需要分别构建

不要直接把 `commonJar` 当独立 mod 加载；它没有入口点和元数据，设计上是给别的模组 shade 进自己的 jar。使用方不要再额外依赖完整的 NarutoUI jar，否则会出现重复类。

## 目录

```text
src/main/java/me/kall/narutoui/mui/
├── screen/       入口、Fragment、页面、section、MuiScreenHost
├── layout/       链式布局 MuiLayout 与 MuiControls 门面
├── field/        文本 / 数字 / 列表输入控件
├── choice/       MuiOption、dropdown、choice grid、枚举选项、主题选择
├── widget/       chip、logo、hint、折叠卡片等通用视觉组件
├── factory/      MuiWidgetFactory / MuiShimmerFactory
├── theme/        MuiTheme
├── shimmer/      引擎、profile、marker、scope
│   ├── widget/   shimmer 版 text / button / switch / slider / checkbox / radio / progress
│   └── drawable/ shimmer 圆形与描边 drawable
├── background/   静态/图片/幻灯片背景、可配置背景
├── overlay/      颜色选择、裁剪浮层
└── util/         本地化、文件/文件夹选择
```

## 限制

- Modern UI 是硬依赖，配置界面只能在客户端创建。
- 库不负责配置持久化，也不提供草稿 / Apply / Revert；字段回调直接写调用方状态，需要延迟提交时由调用方自己缓存。
- 页面在 screen 创建时预先构建；页面很多时创建会变慢。
- Supplier 字段每 100 ms 轮询，不是事件推送。
- 文本输入保持焦点时会暂停 supplier 轮询，避免覆盖正在输入的内容；`textField` 只在 Enter 或失焦时提交。
- `MuiDropdown` 是内嵌展开面板，不是弹窗；选项很多（几十项以上）时应改用带搜索的独立页面。
- `MuiListEditor` 会随内容增高，没有自己的滚动或虚拟化；很长的列表应使用独立页面。
- 数值滑杆最多映射 100,000 个进度步；更大的整数或浮点范围仍能滑，但单步精度由数值输入框补足。
- 静态 slider 数值标签不会自动刷新，需要实时读数时使用 supplier + formatter 版本。
- stable recipe 不是配置文件持久化；进程重启后需要在客户端初始化时重新注册。

## License

LGPL-v3，作者 Kasualix 与 DeepSeek-V4.1-Flash-1017。