# NarutoUI API 教程

这份教程按“我想做什么”来组织。你不需要先读完所有类，找到最接近的需求，抄代码、改字段就行。

完整类列表看 [README](../README.md)，所有方法的细节看 javadoc。

---

## 0. 先记住三件事

1. **NarutoUI 只负责界面，不负责保存配置。**
   字段的 `Supplier` / `Consumer` 都指向你自己的配置对象；什么时候写盘、什么时候 Apply，由你决定。

2. **默认工厂是 `MuiWidgetFactory.modern()`，没有 shimmer。**
   想要流光，必须显式换 `MuiWidgetFactory.shimmer()`，再用 `shimmerNext()` 或标记打开。

3. **所有东西都是 Modern UI 的 View。**
   遇到 DSL 覆盖不到的情况，可以随时拿 `MuiControls` / `MuiLayout` 自己拼，再塞进 `custom(...)`。

---

## 1. 我想先做一个能打开的配置界面

最小版本：

```java
public static Screen createConfig(Screen parent) {
    return MuiSettingsScreen.builder("My Mod")
            .subtitle("Client settings")
            .page("General", page -> page
                    .section("Rendering", section -> section
                            .toggle("Enable HUD",
                                    MyConfig::enabled,
                                    MyConfig::setEnabled)
                            .slider("HUD scale", 50, 200,
                                    MyConfig::scale,
                                    MyConfig::setScale,
                                    value -> value + "%")
                            .button("Apply", MyConfig::apply)))
            .create(parent);
}
```

对应的方法签名大致是：

```java
.toggle(title, BooleanSupplier, BooleanConsumer)
.slider(title, min, max, IntSupplier, IntConsumer, IntFunction<CharSequence>)
.button(label, Runnable)
```

`MuiSettingsScreen.builder(...).create(parent)` 会生成一个完整的 Modern UI Screen，里面已经有：

- 左侧导航（或者单页模式）
- 可滚动的居中页面
- section 卡片、标题、分隔线、行布局
- 主题和控件样式

你只写“有哪些字段”。

---

## 2. 我想分多页 / 不要左侧导航

### 多页

```java
MuiSettingsScreen.builder("My Mod")
        .subtitle("Client settings")
        .footer("v1.0")
        .page("General", page -> page
                .section("Gameplay", section -> section
                        .toggle("Enabled", MyConfig::enabled, MyConfig::setEnabled)))
        .page("Visuals", "Visuals", "Colors and effects", page -> page
                .section("Appearance", section -> section
                        .slider("Opacity", 0, 100, MyConfig::opacity, MyConfig::setOpacity)))
        .create(parent);
```

`page(navLabel, body)` 的 `navLabel` 同时也是页面标题。
`page(navLabel, title, body)` 只给页面单独标题，不显示副标题。
`page(navLabel, title, subtitle, body)` 可以给页面单独的标题和副标题。

### 单页

```java
MuiSettingsScreen.builder("My Mod")
        .singlePage()
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("Enabled", MyConfig::enabled, MyConfig::setEnabled)))
        .create(parent);
```

单页模式：

- 不生成导航栏
- 只允许一个页面，多了会抛 `IllegalStateException`
- 页面直接占满内容区
- `subtitle` / `footer` / `railHeader` / `railFooter` 不再显示；`actionBar` 仍然显示

### 顶部导航

```java
.topNavigation()
```

等价于 `.navigation(MuiNavigation.TOP)`。默认是左侧导航。

### 初始页 / 内容宽度

```java
.initialPage(1)          // 按索引
.initialPage("General")  // 按导航标签，页面重排后仍然指向同一页
.contentWidth(720)       // DP；<= 0 表示不限制
```

`initialPage(int)` 和 `initialPage(String)` 后调用的生效；标签在 `create()` 时解析，找不到会抛 `IllegalArgumentException`。

### 页面切换过渡

```java
MuiSettingsScreen.builder("My Mod")
        // 不写这一行也可以：默认就是 FADE
        .pageTransition(MuiPageTransition.SLIDE_AUTO)
        .pageTransitionDuration(220) // 可选，毫秒
        .page(...)
```

- `FADE`：旧页淡出、新页淡入（默认）
- `SLIDE_UP` / `SLIDE_DOWN`：旧页向上/下移出，新页从另一侧移入
- `SLIDE_AUTO`：按页序决定方向，往后翻向上、往前翻向下

想要瞬间切换（不播动画）时用 `.pageTransitionDuration(0)`；首次打开、`reloadCurrentPage()` 和窗口尺寸重建本来就不做动画。连续点导航会取消上一次动画、从当前状态重新开始。单页模式没有页面切换，这个设置不会生效。

---

## 3. 我想做开关、滑杆、按钮

所有行级方法都提供不传 `description` 的重载；`description` 是可选的标题 tooltip，不需要时直接省略。

### 开关

```java
.toggle("Enabled", MyConfig::enabled, MyConfig::setEnabled)
.toggle("Debug", "Show debug information", MyConfig::debug, MyConfig::setDebug)
```

如果值不是 supplier，而是当前快照：

```java
.toggle("Static option", true, value -> {
    // value 是新的 boolean
})
```

### 滑杆

整数：

```java
.slider("Scale", 50, 200, MyConfig::scale, MyConfig::setScale)
.slider("Opacity", 0, 100,
        MyConfig::opacity,
        MyConfig::setOpacity,
        value -> value + "%")
```

小数：

```java
.slider("Multiplier", 0.5D, 2.0D, 0.05D,
        MyConfig::multiplier,
        MyConfig::setMultiplier,
        value -> String.format("%.2f", value))
```

第三个参数是步长。小数滑杆的精度最终由内部数值处理决定；需要精确输入时，旁边通常配一个 `decimal(...)` 输入框。

### 按钮

```java
.button("Apply", MyConfig::apply)
.button("Close", () -> Minecraft.getInstance().setScreen(parent))
```

按钮没有返回值；点击时执行 `Runnable`。

### 只读的值

```java
.value("Version", "1.0.0")
.value("Loaded chunks", MyWorld::loadedChunks)
.value("TPS", value -> String.format("%.1f", value / 20.0F), MyWorld::tps)
```

`value(..., IntSupplier)` 会定期刷新；`value(..., format, supplier)` 可以自定义整数显示。`DoubleSupplier` / `LongSupplier` 也有默认格式化的重载。

---

## 4. 我想输入文本、数字、列表

### 文本

supplier 版本：

```java
.textField("Display name", "Shown in the HUD", MyConfig::name, MyConfig::setName)
```

静态初始值版本：

```java
.textField("Alias", "default", value -> {
    MyConfig.setAlias(value);
})
```

文本字段在 **Enter** 或 **失焦** 时提交，不是每次按键都写配置。

需要把校验失败原因显示出来时，把 `MuiValidationHint` 放在字段下面：

```java
MuiTextField field = MuiControls.textField(ctx, theme, MyConfig.name())
        .validator(value -> !value.isBlank());
MuiValidationHint hint = MuiValidationHint.create(
        ctx, theme, field, "名字不能为空");
```

`MuiValidationHint` 观察 `MuiValidatable`（`MuiTextField` / `MuiNumberField` 都实现了它），字段有效时自动隐藏。

### 数字

```java
.number("Width", 1, 1920, MyConfig::width, MyConfig::setWidth)
.longNumber("Seed", Long.MIN_VALUE, Long.MAX_VALUE, MyConfig::seed, MyConfig::setSeed)
.decimal("Multiplier", 0.0D, 10.0D, MyConfig::factor, MyConfig::setFactor)
```

- `number` 是 `int`
- `longNumber` 是 `long`
- `decimal` 是 `double`
- 都带上下限；越界/非法输入会按控件规则处理

需要 DSL 之外的校验（例如只允许偶数、只允许某个倍数）时，直接拿 `MuiNumberField`：

```java
MuiNumberField field = MuiControls.numberField(ctx, theme, MyConfig.width())
        .bounds(1, 1920)
        .validator(value -> value.intValue() % 2 == 0)
        .onChangeInt(MyConfig::setWidth);
```

`validator` 在解析和上下限之后执行；不通过时输入框显示主题错误色，且不会触发 `onChange`。提交时非法文本会回到上一个有效值。

### 字符串列表

```java
.list("Whitelist", "One entry per line",
        MyConfig.whitelist(),
        MyConfig::setWhitelist)
```

列表编辑器自己维护一份副本，用户增删改后通过 `onChanged` 把完整 `List<String>` 给你。

需要逐项校验（数字列表、枚举列表、非空字符串）时：

```java
MuiControls.listEditor(ctx, theme, MyConfig.ids())
        .validator(id -> id.matches("[a-z0-9_]+"))
        .onChanged(values -> MyConfig.setIds(values));
```

不通过的项会保持红框，且不会通过 `onChanged` 上报；解析成目标类型（例如 `Integer`/枚举）也放在 `onChanged` 或 `validator` 里做。

### 下拉框

```java
List<MuiOption<Mode>> modes = MuiEnumOptions.of(Mode.class, "mymod.config.mode");

// ...
.dropdown("Mode", modes, MyConfig.mode(), MyConfig::setMode)
```

如果是 supplier 驱动：

```java
.dropdownBound("Mode", modes, MyConfig::mode, MyConfig::setMode)
```

`MuiEnumOptions.of(...)` 会为每个枚举常量生成选项，并按 `keyPrefix + "." + 小写常量名` 找翻译；找不到就自动把 `SOME_NAME` 变成 `Some Name`。

### 卡片选择

```java
List<MuiOption<Integer>> quality = List.of(
        MuiOption.of(0, "Low").description("Fastest"),
        MuiOption.of(1, "High").badge("Recommended"));

// ...
.choiceGrid("Quality", quality, MyConfig.quality(), MyConfig::setQuality)
```

supplier 版本叫 `choiceGridBound(...)`。

`MuiOption` 可以带：

- `description(...)`：卡片下方说明
- `badge(...)`：右上角小标签
- `preview(...)`：自定义预览 View
- `enabled(false)`：不可选

### 主题选择

```java
.section("Appearance", section -> {
    var context = section.page().context();
    MuiTheme dark = MuiTheme.modern(context).withPrimary(0xFFE0A64A);
    MuiTheme light = MuiTheme.modern(context).withPrimary(0xFF3B82F6);

    section.themePicker(List.of(
            MuiThemePicker.of(dark, "Dark"),
            MuiThemePicker.of(light, "Light")),
            selected -> MyConfig.setThemeId(selected.equals(dark) ? "dark" : "light"));
})
```

注意：`themePicker` 只是“选一个主题对象”，**不会自动重建当前屏幕**。你可以在选择后保存主题 id，然后重新创建 screen，或者在自己的 host 里处理主题变更。

---

## 5. 我想把这些字段接到自己的配置

这是最常见的问题。NarutoUI 不规定配置格式，下面几种写法都可以。

### 直接读写静态配置

```java
.toggle("Enabled",
        MyConfig::enabled,
        MyConfig::setEnabled)
```

前提是 `MyConfig.enabled()` 是 `boolean`，`setEnabled(boolean)` 是 `void`。

### 读写实例配置

```java
MyConfig config = ...;

.section("General", section -> section
        .toggle("Enabled",
                config::enabled,
                config::setEnabled)
        .slider("Scale", 50, 200,
                config::scale,
                config::setScale))
```

### 草稿 / Apply / Cancel

DSL 不替你决定草稿/保存语义。简单场景可以放在页面里，正式配置屏更推荐用下面的 `actionBar` 维护草稿：

```java
MyConfig draft = MyConfig.copyOf(MyConfig.current());

MuiSettingsScreen.builder("My Mod")
        .page("General", page -> page
                .section("General", section -> section
                        .toggle("Enabled", draft::enabled, draft::setEnabled)
                        .button("Apply", () -> {
                            MyConfig.replaceWith(draft);
                            MyConfig.save();
                        })
                        .button("Cancel", () -> {
                            // 关闭 screen，或重新载入当前配置
                        })))
        .create(parent);
```

### 固定保存栏与未保存确认

`actionBar` 在页面下方放一个不随内容滚动的固定栏；`onCloseRequested` 在玩家按 ESC/返回时执行：

```java
MuiScreenHost[] host = new MuiScreenHost[1];
boolean[] dirty = {false};

MuiSettingsScreen.builder("My Mod")
        .onCreated(fragment -> host[0] = fragment)
        .onCloseRequested(() -> {
            if (!dirty[0]) return true;
            // 在这里 showOverlay 一个确认框；返回 false 先把界面留住
            return false;
        })
        .actionBar(ctx -> {
            MuiTheme theme = MuiTheme.modern(ctx);
            LinearLayout bar = MuiControls.row(ctx);
            bar.setGravity(Gravity.CENTER_VERTICAL);
            bar.addView(MuiControls.button(ctx, theme, "Save", false, view -> {
                MyConfig.save();
                host[0].close();          // 确认后关闭，绕过守卫
            }), MuiControls.wrap());
            bar.addView(MuiControls.button(ctx, theme, "Cancel", true,
                    view -> host[0].requestClose()), MuiControls.wrap());
            return bar;
        })
        .page("General", page -> page
                .section("General", section -> section
                        .toggle("Enabled", MyConfig::enabled, MyConfig::setEnabled)))
        .create(parent);
```

- `MuiScreenHost#requestClose()` 走正常返回路径，会执行 `onCloseRequested`；
- `MuiScreenHost#close()` 直接关闭，用于确认框里“放弃修改”；
- 守卫运行在 UI 线程，可以安全调用 `showOverlay`。

### supplier 的刷新

`toggle` / `slider` / `value` 的 supplier 默认每 **100 ms** 轮询一次，不是事件推送。

文本输入框在保持焦点时会暂停轮询，避免覆盖正在输入的内容；提交后恢复。

---

## 6. 我想自定义一行 / 一块 / 一整页

### 自定义一行

```java
.section("Custom", section -> {
    var context = section.page().context();
    var theme = section.page().theme();

    section.custom(MuiLayout.row(context, theme)
            .add(MuiControls.text(context, theme, "Hello", 12, theme.onSurface()))
            .add(MuiControls.button(context, theme, "Click", false, view -> {
                // do something
            })));
})
```

`section.custom(View)` 会像普通字段行一样插入分隔线。

### 直接往 section 卡片里加

```java
.section("Custom", section -> section
        .customLayout(body -> body
                .add(MuiControls.text(
                        section.page().context(),
                        section.page().theme(),
                        "Anything here",
                        12,
                        section.page().theme().onSurfaceVariant()))))
```

`customLayout` 拿到的是 section 的卡片内容列。

### 自定义页面内容

```java
.page("Custom", page -> page
        .custom(ui -> ui.content().addView(
                MuiControls.heading(
                        ui.context(),
                        ui.theme(),
                        "Custom page",
                        24,
                        ui.theme().onSurface()))))
```

`ui.content()` 是页面的内容列，`ui.contentLayout()` 是同一列的 `MuiLayout` 包装。

### 搜索 / 过滤

`MuiPageContext#searchable(text, row)` 把一行登记为可过滤项，`filter(query)` 按关键字显示/隐藏：

```java
.page("General", page -> page.custom(ui -> {
    MuiTextField search = MuiControls.textField(ui.context(), ui.theme(), "")
            .hint("Search");
    ui.content().addView(search, MuiControls.fill());

    LinearLayout row = ui.switchRow(
            "Enabled", "Shows the HUD",
            MyConfig::enabled, MyConfig::setEnabled);
    ui.add(row);
    ui.searchable("Enabled HUD", row);

    search.onChange(ui::filter);
}))
```

这是低层原语：只隐藏登记过的行，section 标题和卡片不会自动收起；需要“无结果”提示或整卡折叠时，自己在 `filter` 回调旁边处理。

### 完全自定义一个页面

```java
MuiSettingsScreen.builder("My Mod")
        .customPage("Custom", context -> {
            // 返回你自己的 View
            return myCustomRoot;
        })
        .create(parent);
```

`customPage` 的 root 也会在当前的 widget factory / shimmer profile 下创建，所以里面用 `MuiControls` 创建的控件仍然遵循当前 screen 的工厂和 profile。

---

## 7. 共享配色方案

`MuiColorSchemes` 是 UI 和 HUD 共用的配色目录。不要在自己的模组里再维护一份主题枚举：

```java
for (MuiColorScheme scheme : MuiColorSchemes.all()) {
    String id = scheme.id();              // 存进配置
    String name = I18n.get(scheme.nameKey());
    MuiTheme theme = scheme.theme().toTheme(radius, spacing);
    // HUD: scheme.hud() + scheme.shimmerRamp()
}
```

- 内置 `NARUTO`、`CLASSIC`、`JADE`、`AZURE`、`SAKURA`、`GRAPHITE`
- 需要新增方案时调用 `MuiColorSchemes.register(...)`；所有遍历 `all()` 的界面会自动多一个选项
- `AUTO` / `CUSTOM` 属于消费方自己的特殊项，不放进共享目录
- `MuiColorScheme` 同时携带 `MuiThemePalette`（UI）和 `MuiHudPalette`（HUD），一份方案可以同时服务设置界面和覆盖层

## 8. 我想改主题

默认会自动解析 Modern UI 主题，一般不用设置。

### 固定主题

如果你已经有 Modern UI `Context`：

```java
MuiTheme theme = MuiTheme.modern(context)
        .withPrimary(0xFFE0A64A)
        .withOnPrimary(0xFF000000)
        .withSurface(0xCC101116)
        .withCornerRadius(14)
        .withSpacing(18);

MuiSettingsScreen.builder("My Mod")
        .theme(theme)
        .page(...)
        .create(parent);
```

### 跟随实时配置的主题

`.theme(...)` 在屏幕创建时就固定了；如果强调色来自配置，用 `.themeProvider(...)`：

```java
var host = new MuiConfigFragment[1];

MuiSettingsScreen.builder("My Mod")
        .themeProvider(context -> MuiTheme.modern(context)
                .withPrimary(MyConfig.accent())
                .withOnPrimary(MyConfig.onAccent()))
        .onCreated(fragment -> host[0] = fragment)
        .page("General", ...)
        .create(parent);

// 配置改变后，重建外壳并保留当前页
if (host[0] != null) host[0].reloadTheme();
```

`themeProvider` 在每次外壳重建时重新求值，因此导航高亮、页面标题、区块标题和控件会一起换色，不会只换一半。

没有 `Context` 又想要固定配色时，可以直接构造 record：

```java
MuiTheme theme = new MuiTheme(
        0xFFE0A64A, // primary
        0xFF000000, // onPrimary
        0xFF101116, // surface
        0xFF1F2027, // surfaceLow
        0xFF2A2B34, // surfaceHigh
        0xFFEDEFF3, // onSurface
        0xFFA6AAB4, // onSurfaceVariant
        0xFF3A3C48, // outline
        0xFFFF6B6B, // error
        12,          // cornerRadius
        20           // spacing
);
```

---

## 9. 我想换背景

默认是接近黑色的半透明遮罩。

### 纯色 / 透明 / 黑色

```java
.background(MuiBackground.color(0xCC101116))
.background(MuiBackground.transparent())
.background(MuiBackground.black())
```

### 单张图片

```java
.background(MuiBackground.image(
        Path.of("config/mymod/background.png")))   // dim 默认 0.55

.background(MuiBackground.image(
        Path.of("config/mymod/background.png"),
        0.7D))   // 显式 dim，0..1
```

### 带裁剪的图片

```java
.background(MuiBackground.image(
        path,
        0.1D, 0.1D, 0.8D, 0.8D, // crop x, y, width, height
        0.55D))
```

### 幻灯片

```java
.background(MuiBackground.slideshow(files)) // 默认 8s / 1.2s / 0.55

.background(MuiBackground.slideshow(
        files,
        8.0D,   // 每张图停留秒数
        1.2D,   // 交叉淡入秒数
        0.55D)) // dim
```

如果需要“用户在界面里选图 / 选文件夹 / 裁剪 / 保存”的完整背景控制器，可以看 `MuiConfigurableBackground`，它把 `MuiFilePicker`、`MuiCropOverlay` 和状态保存串起来了；普通需求直接用上面的 `MuiBackground` 工厂就够了。

---

## 10. 我想让界面流光

所有 shimmer 默认关闭。要开启，必须同时满足：

1. screen 使用 `MuiWidgetFactory.shimmer()`
2. 目标控件或行被 `shimmerNext()` / `.shimmerable(...)` 标记为 `ALWAYS`

### 最小开启

```java
MuiSettingsScreen.builder("My Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .page("General", page -> page
                .shimmerNext()
                .section("Rendering", section -> section
                        .shimmerNext()
                        .toggle("Enabled", MyConfig::enabled, MyConfig::setEnabled)
                        .slider("Scale", 50, 200, MyConfig::scale, MyConfig::setScale)))
        .create(parent);
```

注意：默认 factory 是 `modern()`，它没有 shimmer target；不换工厂的话，`shimmerNext()` 不会有效果。

### 行级

```java
.section("Rendering", section -> section
        .shimmerNext()
        .toggle("Enabled", ...)       // 这一行闪
        .slider("Scale", ...)         // 这一行不闪
        .shimmerNext(MuiShimmerable.NEVER)
        .button("Reset", ...))        // 明确关闭
```

`shimmerNext()` 只影响紧接着的那一行，不会影响后面的行。

### 页面级

```java
.page("General", page -> page
        .shimmerHeader()     // 页面标题 + 副标题
        .shimmerNext()       // 下一个 section / text / custom
        .section("Rendering", section -> ...))
```

页面级 `shimmerNext()` 作用于下一个页面块：

- `section(...)`：section 标题 + 整个 card（所有行）
- `text(...)`：该段落
- `custom(...)` / `customLayout(...)`：回调中新加的子视图

如果整页都要闪，可以在页面开头直接：

```java
.page("About", page -> page
        .shimmerAll()
        .section("Info", section -> section.value(...))
        .section("Usage", section -> section.note(...)))
```

`shimmerAll()` 会带上页面 header，并让之后添加的所有 section / text / custom 都继承这个 marker。单独某一块不想闪时，再用 `shimmerNext(MuiShimmerable.NEVER)` 覆盖。

如果只想让 section 里某一行闪，用 section 自己的 `shimmerNext()`，不要用页面级 `shimmerNext()`。

### 导航栏和外壳标题

```java
MuiSettingsScreen.builder("My Mod")
        .widgetFactory(MuiWidgetFactory.shimmer())
        .shimmerNavigation()   // 导航栏所有页面标签
        .shimmerTitles()       // rail/top bar 的标题、副标题、footer
        .page(...)
        .create(parent);
```

也支持自定义 marker：

```java
.shimmerNavigation(MuiShimmerable.when(MyConfig::navigationGlow))
.shimmerTitles(MuiShimmerable.NEVER)
```

### 自定义 shimmer 条件

```java
MuiShimmerable.ALWAYS
MuiShimmerable.NEVER
MuiShimmerable.of(boolean)
MuiShimmerable.when(MyConfig::glowEnabled)
```

`when(...)` 每帧求值。如果引擎已经停止，条件从 false 变 true 时需要调用一次 `MuiShimmer.refresh()` 重新启动。

### 自定义控件怎么响应

如果 custom View 里有自己的 target：

- 实现 `ShimmerTarget`，在 `setShimmerable(...)` 里保存 marker
- 如果内部有 drawable 或延迟创建的子控件，实现 `MuiShimmerContainer`，把 marker 转发给它们

否则 `MuiShimmerScope` 只能沿 View 树找到普通子控件，找不到不可见的内部 target。

### 输入框和 MuiTextAction

`MuiTextField`、`MuiNumberField` 和 `MuiTextAction` 现在都实现了 `ShimmerTarget`，默认 `NEVER`。给所在行加 `shimmerNext()` 后，输入框里的文字、列表编辑器的删除文字都会跟着流动。

`MuiListEditor` 同时是 `MuiShimmerContainer`，所以之后点击 Add 新建的行也会继承当前行的 marker，不会只让初始行闪光。

---

## 11. 我想选文件、选颜色、裁剪图片

### 文件 / 文件夹

```java
MuiFilePicker.openImage("Choose background", "Images", new MuiFilePicker.Callback() {
    @Override
    public void onPicked(@NotNull Path path) {
        // path 已经是选中的文件
    }

    @Override
    public void onFailed() {
        // 用户取消或对话框失败
    }
});

MuiFilePicker.openFolder(Path.of("config/mymod"), "Choose folder", callback);

// 短版本：图片 filter 默认 "Images"；文件夹默认从游戏目录开始
MuiFilePicker.openImage("Choose background", callback);
MuiFilePicker.openFolder("Choose folder", callback);
```

回调会在 UI 线程执行。

### 颜色选择

```java
.onCreated(fragment -> {
    var context = fragment.requireContext();
    var labels = new MuiColorPickerOverlay.Labels(
            "Color", "Brightness", "Alpha", "Apply", "Cancel");

    var picker = new MuiColorPickerOverlay(
            context,
            MuiTheme.modern(context),
            0xFFFF5252,
            labels,
            argb -> {
                // 用户点了 Apply
            });

    fragment.showOverlay(picker);
})
```

### 双色带

双色带行由 `MuiColorBandField` 提供：标题、实时色带预览、两个颜色字段和两个取色回调。它只负责显示和更新颜色，取色弹层由调用方决定，和 `MuiColorField` 的模式一致。

```java
MuiColorBandField band = MuiColorBandField.create(
                context, theme, "Glow band", 0xFFFFD24A, 0xFFFF5252)
        .labels("Start", "End", "Pick")
        .onPickFirst(() -> showPicker(band.firstColor(), argb -> band.first(argb)))
        .onPickSecond(() -> showPicker(band.secondColor(), argb -> band.second(argb)));

row.add(band, MuiControls.fill());
```

- `band.band()` 返回 `MuiColorBand`，`band.ramp()` 返回九段 OKLab 色带；色带是镜像的，引擎往复采样时两端和中间都能看到选中的颜色。
- 只需要画色带时用 `MuiColorBandPreview.create(context, colors, 28)`。
- 如果两个颜色是“核心色 + 深色辅助”，用 `MuiShimmerRamps.rich(core, deep)` 生成带浅色、核心、深色的九段带，再用 `.rampProvider(band -> MuiShimmerRamps.rich(band.first(), band.second()))` 让预览也使用它。
- `MuiColorBandField.enabled(false)` 会同时禁用两个颜色字段。

### 裁剪

`MuiCropOverlay` 是独立 View，构造参数包含图片、尺寸、目标宽高比、Labels 和回调；`MuiCropOverlayFactory` 可以自定义主题。一般不需要直接用，`MuiConfigurableBackground` 已经处理了“选图 → 裁剪 → 保存”的流程。

---

## 12. 我想注册一个稳定的配置界面

```java
public static void initClient() {
    MuiSettingsScreen.register("mymod:config", "My Mod", builder -> builder
            .page("General", page -> page
                    .section("General", section -> section
                            .toggle("Enabled",
                                    MyConfig::enabled,
                                    MyConfig::setEnabled))));
}

public static Screen open(Screen parent) {
    return MuiSettingsScreen.createRegistered("mymod:config", parent);
}
```

注意：

- recipe 必须在客户端初始化时重新注册
- recipe 不能依赖一次性局部状态
- 注册的是“怎么构建界面”，不是配置值本身
- 进程重启后仍然需要重新注册

---

## 13. 我想让别的模组 shadow 这套 UI

构建：

```text
gradlew commonJar
```

产物：

```text
build/libs/narutoui-xxx-xxx-common.jar
```

它只包含 `me/kall/narutoui/mui/**`，不包含：

- `NarutoUI` 入口点
- `META-INF/mods.toml` / `neoforge.mods.toml`
- 其他模组元数据

使用方把它 shade 进自己的 jar 即可，不要再额外依赖完整的 NarutoUI。

注意：common jar 仍然依赖 Minecraft client 和 Modern UI；`screen` / `background` / `file picker` 还依赖 `icyllis.modernui.mc.MuiModApi`。它已经 remap，和当前 Forge/NeoForge + Minecraft 版本绑定。

---

## 14. 常见坑

### 我调用了 `shimmerNext()`，但没有流光

检查是不是还在用默认的 `MuiWidgetFactory.modern()`。

```java
.widgetFactory(MuiWidgetFactory.shimmer())
```

### 文本框改了，但配置没立刻变

正常。`textField` 在 Enter 或失焦时提交。

### supplier 的值变了，界面没更新

supplier 默认 100 ms 轮询；文本输入框聚焦时会暂停。不是事件总线。

### 页面内容不会自动刷新

页面在第一次切换到时构建，之后保留。需要重新构建当前页面时：

```java
.onCreated(fragment -> ...) // 保存 fragment 引用
// 之后
fragment.reloadCurrentPage();
```

### `shimmerNext()` 影响到了后面的行

它不会。检查是不是用了页面级 `shimmerNext()`；页面级作用于整个下一个块，包括 section 的所有行。

### 列表编辑器的值没有写回

检查 `onChanged` 是否真的把完整列表写回配置。列表编辑器内部是副本，不会直接改你传入的 `List`。

### 配置保存 / Apply 是谁做的

是你。NarutoUI 只提供字段回调，不碰持久化。

### 自定义 `MuiWidgetFactory`

`MuiWidgetFactory` 的所有方法都是 abstract，不再提供“返回默认组件”的 fallback。自定义工厂需要实现完整接口；如果只是想换某几种控件，可以写一个委托包装器，把大部分方法转发给 `MuiWidgetFactory.modern()` 或 `MuiWidgetFactory.shimmer()` 的实例，再覆盖需要改的方法。

---

## 需要完整 API 时

- 包结构和能力总览：[README](../README.md)
- 所有 public 方法：[javadoc](../build/docs/javadoc/index.html)
- 背景控制器、shimmer 自定义控件等高级用法：直接看对应类的 javadoc 和接口注释