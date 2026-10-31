# NarutoUI 实战教程：把 NarutoOverlay 的配置界面拆给你看（EXAMPLE-BASED-TUTOR）

> 本文不是 API 字典。查参数去 [API-TUTOR.md](API-TUTOR.md)，第一次上手去 [EASY_START.md](EASY_START.md)。
>
> 本文只做一件事：**拿 NarutoOverlay 真实在跑的配置界面当样本，一行一行拆开讲**。你看到的每一段代码都来自仓库里的真实文件，不是凭空编的示例；每个“为什么”也都对应真实代码里的写法。

- 样本代码：`../../NarutoOverlay/src/main/java/me/kall/narutooverlay/config/ui`
- NarutoUI 版本：1.4.0（`../gradle.properties` 里的 `mod_version`）
- 环境：Minecraft 1.20.1 / Forge 47.x / Java 17 / Modern UI 3.12.0
- 阅读顺序建议：第 1～3 章必读；之后按你手头要做的东西跳读；第 12 章是“照抄清单”，第 13 章是“别踩这些坑”。

**代码块约定**

- `// ←` 开头的注释是本文加的讲解，原文件里没有。
- `// ...` 表示省略了与当前话题无关的行。
- 变量名、方法名、参数顺序保持原样，方便你直接打开源文件对照。
- 文中出现的 `RenderArgs`、`Card`、`Palette` 等是 NarutoOverlay 自己的类，不是 NarutoUI API；这正是本文要教你的“适配层”写法。

---

## 目录

- [1. 先看成品：三个页面、一个 rail、两个浮层](#1-先看成品三个页面一个-rail两个浮层)
- [2. 入口：可选依赖、反射门与“打开配置界面”](#2-入口可选依赖反射门与打开配置界面)
- [3. 组合根：ConfigUi.create 逐行拆解](#3-组合根configuicreate-逐行拆解)
- [4. 高层字段：Appearance 页教会你的四件事](#4-高层字段appearance-页教会你的四件事)
- [5. 自定义行：主题卡片与颜色选择](#5-自定义行主题卡片与颜色选择)
- [6. 数据驱动：Cards 页与可折叠卡片](#6-数据驱动cards-页与可折叠卡片)
- [7. 只读信息页：About 页的“展示型”写法](#7-只读信息页about-页的展示型写法)
- [8. 视觉层：主题、控件工厂、流光](#8-视觉层主题控件工厂流光)
- [9. 背景：可配置背景是怎么接进来的](#9-背景可配置背景是怎么接进来的)
- [10. 本地化：为什么到处是 MuiLocalization.get](#10-本地化为什么到处是-muilocalizationget)
- [11. 保存、重建与恢复](#11-保存重建与恢复)
- [12. 抄作业：给新模组的最小移植清单](#12-抄作业给新模组的最小移植清单)
- [13. 避坑清单（都来自真实代码）](#13-避坑清单都来自真实代码)
- [14. 对照表与结语](#14-对照表与结语)

---

## 1. 先看成品：三个页面、一个 rail、两个浮层

### 1.1 玩家看到的东西

打开 NarutoOverlay 的配置界面，实际结构是这样的：

```text
Screen（Modern UI）
├─ 背景（可配置：单图 / 文件夹轮播 / 裁剪 / 调暗）
├─ 左侧 rail
│  ├─ railHeader：圆形 "N" 标志（MuiLogo，带流光）
│  ├─ 三个页面项：Cards / Appearance / About
│  └─ railFooter："ESC" 提示胶囊（MuiHintBadge）
└─ 内容区（每页可滚动、居中、限宽）
   ├─ Cards 页：每张 HUD 卡片一个可折叠卡片 + 位置输入 + 字段开关 + 重置
   ├─ Appearance 页：主题卡片、显示开关、流光滑条、边框、性能
   └─ About 页：版本、背景状态、配置文件路径、操作说明
```

外加两个浮层：

- 颜色选择器（`MuiColorPickerOverlay`）——点“Pick color”弹在全屏最上层；
- 图片裁剪框（`MuiCropOverlay`）——选完背景图后弹出来让你框选可见区域。

注意：**这些都不是 NarutoOverlay 自己画的布局**。NarutoOverlay 只写了“要哪些页面、每页有哪些字段、字段读写哪个配置项”，剩下的导航、卡片、分隔线、滚动、主题、浮层位置，全部由 NarutoUI 提供。

### 1.2 文件地图：谁负责什么

| NarutoOverlay 文件 | 职责 | 本文对应 |
|---|---|---|
| `NarutoUiCompat` | 可选依赖的反射门：NarutoUI / Modern UI 不在时不炸 | 第 2 章 |
| `ConfigUi` | 组合根：创建 Screen、宿主、页面、背景工厂 | 第 3、9 章 |
| `ConfigUi.ConfigScreenHost` | 把 `MuiScreenHost` 接到当前 fragment 上 | 第 3.4 节 |
| `ConfigAppearancePage` | 高层字段页（toggle / slider / 自定义颜色行） | 第 4、5 章 |
| `ConfigCardsPage` | 数据驱动页（可折叠卡片、数字框、chip） | 第 6 章 |
| `ConfigAboutPage` | 展示型页面（value / text / description row / 文本操作） | 第 7 章 |
| `NarutoShimmerWidgetFactory` | 控件工厂适配器：换强调色，其余委托给库 | 第 8 章 |
| `NarutoShimmerConfig` | 流光参数适配器：把配置项喂给 profile | 第 8 章 |
| `ConfigUi.ConfigBackgroundFactory` | 可配置背景 + 标签 + 裁剪浮层工厂 | 第 9 章 |
| `ConfigSaver` | 防抖保存（400ms 合并写盘） | 第 11 章 |

记住一句话：**NarutoOverlay 的 UI 代码几乎全是“声明”和“适配”，没有“画界面”。** 这也是你用 NarutoUI 时应该保持的姿势。

### 1.3 三个心智模型

在看代码前，先把三个概念对齐，后面会反复用到：

1. **Screen / Page / Section**：Screen 是整屏，Page 是左侧导航切出来的一页，Section 是页面里的一张卡片（自动带标题、圆角、边框、字段间分隔线）。
2. **Host**：屏幕级服务。页面拿不到“当前 fragment”，但能通过 `MuiScreenHost` 要求上下文、弹浮层、重建当前页。NarutoOverlay 用 `ConfigScreenHost` 做了一层间接。
3. **Profile / Factory**：视觉层。`MuiShimmerProfile` 决定流光长什么样，`MuiWidgetFactory` 决定开关/滑条/按钮长什么样。两者都是“创建控件时捕获”，不是全局设置。

---

## 2. 入口：可选依赖、反射门与“打开配置界面”

### 2.1 为什么需要反射门

NarutoOverlay 把 NarutoUI 当**可选依赖**：装了就有好看的配置界面，没装也要能正常启动。问题在于：`ConfigUi` 这个类里直接 `import me.kall.narutoui...`，一旦 JVM 加载它，而 NarutoUI 不在，就会抛 `NoClassDefFoundError`。

所以 NarutoOverlay 拆了一个“只碰字符串和反射”的壳：

```java
// 文件：config/ui/NarutoUiCompat.java
/**
 * Reflective gate for the optional NarutoUI-backed configuration screen.
 * <p>This class must not reference NarutoUI or Modern UI types directly. It is safe to load when
 * either optional mod is missing, while {@link ConfigUi} is only loaded after both are present.</p>
 */
public final class NarutoUiCompat {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");
    // ← 注意：这里只有类名字符串。编译期不产生对 ConfigUi 的符号引用。
    private static final String CONFIG_UI = "me.kall.narutooverlay.config.ui.ConfigUi";

    public static void integrate(@NotNull FMLJavaModLoadingContext context) {
        // ← 只有真的调用到这里，才会去加载 ConfigUi；
        //    如果 NarutoUI/Modern UI 缺失，Class.forName 抛异常，被下面 catch 住，游戏继续跑。
        invoke("install", new Class<?>[]{FMLJavaModLoadingContext.class}, context);
    }

    public static void migrateBundledDefaults() {
        invoke("migrateBundledDefaults", new Class<?>[0]);
    }

    private static void invoke(@NotNull String method, @NotNull Class<?>[] parameterTypes, @NotNull Object... arguments) {
        try {
            Class.forName(CONFIG_UI).getMethod(method, parameterTypes).invoke(null, arguments);
        } catch (ReflectiveOperationException | LinkageError error) {
            // ← LinkageError 是关键：NoClassDefFoundError 属于它，而不是 ReflectiveOperationException。
            LOGGER.warn("NarutoUI configuration screen is unavailable", error);
        }
    }
}
```

调用方在 `NarutoOverlay` 主类里先判断 mod 是否加载：

```java
// 文件：NarutoOverlay.java（节选）
if (ModList.get().isLoaded("narutoui")) {
    NarutoUiCompat.integrate(context);   // ← 装了 NarutoUI 才尝试接线
}
```

> **为什么这样写**
>
> 可选依赖不是“在 `mods.toml` 里写 optional 就完事”。只要你的类路径里有对可选库的符号引用，类加载就可能失败。正确姿势是：**入口类零引用，用反射或独立的桥接类延迟加载**。`NarutoUiCompat` 就是那个“零引用的桥”。

### 2.2 注册“模组列表里的 Config 按钮”

```java
// 文件：config/ui/ConfigUi.java（节选）
public static void install(FMLJavaModLoadingContext context) {
    if (installed) return;              // ← 幂等：重复调用不会注册两遍
    installed = true;

    BundledBackgrounds.install();       // ← 把 jar 里的内置背景解压到游戏目录（第 9 章）

    // ← Forge 的扩展点：模组列表里的 "Config" 按钮会打开这里返回的 Screen。
    //    第二个参数是上一屏，ESC 时回到它。
    context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (minecraft, previous) -> create(previous)));

    // ← 额外入口：在聊天栏右键点中一张 HUD 卡片，也能直接打开配置（第 3.5 节）。
    MinecraftForge.EVENT_BUS.addListener(ConfigUi::onMouseButton);
}
```

到这一步，入口就有了。真正开始“搭界面”的是 `ConfigUi.create`。

---

## 3. 组合根：ConfigUi.create 逐行拆解

### 3.1 完整代码（带讲解注释）

```java
// 文件：config/ui/ConfigUi.java
public static @NotNull Screen create(@Nullable Screen previous) {
    return create(previous, 0);         // ← 默认打开第 0 页（Cards）
}

public static @NotNull Screen create(@Nullable Screen previous, int page) {
    // ← 1) 先造宿主。它持有“可配置背景”，也是页面回调里唯一的屏幕服务入口。
    ConfigScreenHost host = new ConfigScreenHost();

    // ← 2) 页签下标做 clamp。外部入口（比如右键卡片）可能传进越界值，
    //       而 builder.initialPage 不做范围检查。
    int initialPage = Math.max(0, Math.min(2, page));

    MuiSettingsScreen.Builder builder = MuiSettingsScreen.builder("NarutoOverlay")
            // ← 本模组的控件外观：带流光的 ShimmerTextView / Switch / SeekBar / Button。
            //    不传就用库默认的普通 Modern UI 控件。
            .widgetFactory(new NarutoShimmerWidgetFactory())
            // ← 本模组独立的流光参数（速度、密度、强度、颜色 ramp、fps）。
            //    关键点：每个 screen 可以不一样，不会互相覆盖。
            .shimmerProfile(MuiShimmerProfile.create(new NarutoShimmerConfig()))
            // ← 背景由宿主持有：配置页、About 页、裁剪流程共用同一份状态。
            .background(host.background())
            // ← 打开时停在哪一页（0 开始）。
            .initialPage(initialPage)
            // ← 左侧导航顶部：一个圆形 N。工厂在每次建屏时调用。
            .railHeader(ConfigUi::logo)
            // ← 左侧导航底部：ESC 提示胶囊。
            .railFooter(ConfigUi::hint)
            // ← fragment 建好视图后回调，把宿主接到当前 fragment。
            .onCreated(host::attach)
            // ← fragment 销毁前回调，断开引用，避免宿主继续持有已销毁的视图。
            .onDestroyed(fragment -> host.attach(null))
            // ← 四个参数：导航文字 / 页面标题 / 页面副标题 / 页面构建回调。
            //    导航文字和标题分开，是因为 rail 很窄，标题可以更长。
            .page(MuiLocalization.get("narutooverlay.config.nav.cards"),
                    MuiLocalization.get("narutooverlay.config.cards.title"),
                    MuiLocalization.get("narutooverlay.config.cards.subtitle"),
                    cardsPage -> ConfigCardsPage.populate(cardsPage, host))
            .page(MuiLocalization.get("narutooverlay.config.nav.appearance"),
                    MuiLocalization.get("narutooverlay.config.appearance.title"),
                    MuiLocalization.get("narutooverlay.config.appearance.subtitle"),
                    form -> ConfigAppearancePage.populate(form, host))
            .page(MuiLocalization.get("narutooverlay.config.nav.about"),
                    MuiLocalization.get("narutooverlay.config.about.title"),
                    MuiLocalization.get("narutooverlay.config.about.subtitle"),
                    aboutPage -> ConfigAboutPage.populate(aboutPage, host.background()));

    // ← 真正的创建动作。previous 传回去，ESC 才能回到原来那屏。
    return builder.create(previous);
}
```

### 3.2 每一行到底发生了什么

按执行顺序看：

1. `new ConfigScreenHost()`：立刻创建背景控制器（`ConfigBackgroundFactory.create()`），并从配置里 `load()` 一份 `MuiBackgroundState`。
2. `builder.page(...)`：**只是把页面定义（一个 record）塞进列表**，页面回调此刻不执行。真正的执行发生在 fragment 的 `onCreateView` 里（第 11 章会再讲）。
3. `builder.create(previous)`：交给 `MuiConfigScreen` 创建 Screen，注册一个进程内 UUID，把 fragment 塞进 screen。
4. fragment 建视图时，才逐个执行页面回调——这时 `ConfigCardsPage.populate(...)` 才会跑，才会 `new CardsView(...)`、才会创建控件。
5. 视图建完后，`onViewCreated` 触发 `host.attach(fragment)`。**所以页面构建期间 `host.fragment` 还是 null**。

> **这是最容易踩的坑**
>
> 页面回调执行时，`host` 还没 attach。所以页面构建代码里**不能**调用 `host.requireContext()`、`host.showOverlay(...)`、`host.reloadCurrentPage()`。NarutoOverlay 的页面只在“点击/选色/重置”这类**回调**里用 host，构建阶段只用传进来的 `page.context()` / `page.theme()`。
>
> 第 3.4 节的宿主实现会对 null 做保护：`showOverlay` / `reloadCurrentPage` 静默忽略，`requireContext` 直接抛 `IllegalStateException`。

### 3.3 为什么标题用 `MuiLocalization.get` 而不是直接写字符串

- `MuiLocalization.get(key)`：立即解析成当前语言的字符串，找不到就返回 key 本身。
- 也可以直接把翻译键传给 `.page(...)`，库会在构建时通过 `MuiLocalization.resolve` 解析。
- NarutoOverlay 选择先 `get`，是因为同一个 key 还会被用于别处（例如 `ConfigUi.hint`、About 页），提前解析成 `String` 更好传。

两种写法都行，别混着用出“有的地方解析了、有的地方没解析”的错觉即可。

### 3.4 宿主：为什么不直接让页面拿 fragment

```java
// 文件：config/ui/ConfigUi.java（节选）
static final class ConfigScreenHost implements MuiScreenHost {
    // ← 背景控制器在构造时就建好：它不依赖 fragment，只依赖配置和文件系统。
    private final MuiConfigurableBackground background = ConfigBackgroundFactory.create();
    private @Nullable MuiConfigFragment fragment;   // ← 建屏前是 null

    public @NotNull MuiConfigurableBackground background() {
        return this.background;
    }

    public void attach(@Nullable MuiConfigFragment fragment) {
        this.fragment = fragment;                   // ← onCreated 时接上，onDestroyed 时置空
    }

    @Override
    public @NotNull Context requireContext() {
        MuiConfigFragment current = this.fragment;
        // ← 故意抛异常而不是返回 null：Fluent 代码里静默 null 更难查。
        if (current == null) throw new IllegalStateException("Config screen is not attached");
        return current.requireContext();
    }

    @Override
    public void showOverlay(@NotNull View overlay) {
        MuiConfigFragment current = this.fragment;
        if (current != null) current.showOverlay(overlay);   // ← 未 attach 时静默忽略
    }

    @Override
    public void removeOverlay(@NotNull View overlay) {
        MuiConfigFragment current = this.fragment;
        if (current != null) current.removeOverlay(overlay);
    }

    @Override
    public void reloadCurrentPage() {
        MuiConfigFragment current = this.fragment;
        if (current != null) current.reloadCurrentPage();
    }
}
```

这层间接带来两个好处：

1. **页面不依赖 fragment 类型**。`ConfigCardsPage.populate(page, host)` 只认识 `MuiScreenHost`，单测或换宿主更容易。
2. **宿主可以持有“屏幕之外”的东西**：背景控制器。页面通过 `host.background()` 就能驱动背景，不需要知道它怎么存、怎么画。

`MuiScreenHost` 只有四个方法，外加一个默认的 `onThemeChanged()`：

```java
// 文件：NarutoUI/.../screen/MuiScreenHost.java
public interface MuiScreenHost {
    @NotNull Context requireContext();
    void showOverlay(@NotNull View overlay);
    void removeOverlay(@NotNull View overlay);
    void reloadCurrentPage();

    default void onThemeChanged() {
        reloadCurrentPage();   // ← 默认实现直接重建当前页，让颜色重新解析
    }
}
```

### 3.5 rail 头部与底部

```java
// 文件：config/ui/ConfigUi.java（节选）
private static @NotNull View logo(@NotNull Context context) {
    // ← MuiLogo：圆形色块 + 居中的文字，背景是 shimmer target。
    //    color() 是基础色，流光关闭时用它；textColor() 是字形颜色。
    return MuiLogo.create(context, MuiTheme.modern(context), "N")
            .color(Accent.primary())
            .textColor(Accent.onPrimary());
}

private static @NotNull View hint(@NotNull Context context) {
    // ← MuiHintBadge：带 shimmer 描边的胶囊，适合放快捷键提示。
    return MuiHintBadge.create(context, MuiTheme.modern(context),
                    MuiLocalization.get("narutooverlay.config.footer.escHint"))
            .color(Accent.primary());
}
```

两个方法都是 `Function<Context, View>`，在每次建屏时调用一次。注意它们都自己 `MuiTheme.modern(context)`，因为 rail 的构建发生在**没有页面上下文**的地方，拿不到 `page.theme()`。

### 3.6 聊天栏右键的小入口

```java
// 文件：config/ui/ConfigUi.java（节选）
private static void onMouseButton(InputEvent.MouseButton.Pre event) {
    if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
    if (event.getAction() != GLFW.GLFW_PRESS) return;

    Minecraft minecraft = Minecraft.getInstance();
    if (!(minecraft.screen instanceof ChatScreen)) return;   // ← 只在聊天界面里生效

    // ← 用当前鼠标位置去问 HUD 卡片系统：点到哪张卡了？
    Card<?> card = TextRelocation.pick(TextRelocation.mouseX(), TextRelocation.mouseY());
    if (card == null) return;

    event.setCanceled(true);                                  // ← 吃掉这次右键，避免插入聊天符号
    minecraft.setScreen(create(minecraft.screen));            // ← 打开配置，ESC 回到聊天
}
```

这是“业务入口”的写法，和 NarutoUI 无关，但值得学：**入口可以有很多个，界面只写一次**。

---

## 4. 高层字段：Appearance 页教会你的四件事

Appearance 页是“高层 DSL”的集中示范。它只用 `page.section(...)` 和 `section.toggle/slider/custom`，没有手动布局。我们挑四段来看。

### 4.1 开关：supplier + consumer 的正确姿势

```java
// 文件：config/ui/page/ConfigAppearancePage.java（节选）
page.section(MuiLocalization.get(GROUP + "appearance.display"), section -> section
        // ← 四参数 toggle：标题、说明、当前值、变化回调。
        //    第二个参数可以为 null，表示不要说明行。
        .toggle(MuiLocalization.get(GROUP + "appearance.dropShadow"),
                MuiLocalization.get(GROUP + "appearance.dropShadow.desc"),
                // ← BooleanSupplier：库每 100ms 轮询一次，读的是“配置的当前值”。
                //    这样外部（快捷键、别的界面）改了配置，开关也会跟着动。
                RenderArgs.DROP_SHADOW::get,
                // ← BooleanConsumer：用户拨动开关时执行。只改状态，不负责“界面同步”，
                //    同步由上面的 supplier 完成。
                value -> {
                    RenderArgs.DROP_SHADOW.set(value);
                    ConfigSaver.changed();   // ← 标脏，交给 400ms 防抖保存
                })
        // ← 中间还有两个同款开关（equalWidth / drawBackplane），写法完全一样，这里省略。
        // ...
        .toggle(MuiLocalization.get(GROUP + "appearance.textShimmer"),
                MuiLocalization.get(GROUP + "appearance.textShimmer.desc"),
                RenderArgs.TEXT_SHIMMER::get,
                value -> {
                    RenderArgs.TEXT_SHIMMER.set(value);
                    ConfigSaver.changed();
                    // ← 流光开关变了，要通知引擎重新判断“现在还有没有需要动画的目标”。
                    //    不调用它，关闭后可能还在跑，或重新打开后不恢复。
                    MuiShimmer.refresh();
                }));
```

这三行就是 NarutoUI 开关的全部心智模型：

| 你提供 | 库负责 |
|---|---|
| 一个“读当前值”的 supplier | 每 100ms 轮询，把 UI 同步到真实状态 |
| 一个“写新值”的 consumer | 用户操作时调用，带上防重入保护 |
| 标题 / 可选说明 | 行布局、开关样式、说明字号、间距 |

> **静态版什么时候用？**
>
> `toggle(title, booleanValue, consumer)` 只在创建时读一次初值。适合“这个开关只在这里改、别处不会变”的场景。NarutoOverlay 几乎全部用 supplier 版，因为它还有快捷键和 HUD 拖拽会改配置。

### 4.2 滑条：整数 + 缩放 + 格式化

NarutoUI 的滑条只支持 `int`（底层是 `SeekBar`）。但配置里的速度是 `double`（0.2x～3.0x）。NarutoOverlay 的处理是**显示层放大 10 倍，存储层除回去**：

```java
// 文件：config/ui/page/ConfigAppearancePage.java（节选）
.slider(MuiLocalization.get(GROUP + "appearance.shimmerSpeed"),
        2, 30,                                              // ← 滑条整数范围 2..30
        // ← 读：0.2x..3.0x 乘以 10 再四舍五入，变成整数
        () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_SPEED.get() * 10.0D),
        // ← 写：滑条值除以 10，存回 double
        value -> {
            RenderArgs.TEXT_SHIMMER_SPEED.set(value / 10.0D);
            ConfigSaver.changed();
        },
        // ← 显示：不显示 "15"，显示 "1.5x"
        value -> String.format(Locale.ROOT, "%.1fx", value / 10.0D))
```

同样的套路在 Appearance 页出现了四次（速度、密度、强度、边框宽度），About 页的背景淡入也是同一套路：

| 配置项 | 存储类型 | 滑条范围 | 读 | 写 | 显示 |
|---|---|---|---|---|---|
| 流光速度 | double | 2..30 | `×10` | `÷10` | `%.1fx` |
| 流光密度 | double | 2..30 | `×10` | `÷10` | `%.1fx` |
| 流光强度 | double | 0..100 | `×100` | `÷100` | `%` |
| 边框宽度 | double | 0..400 | `×100` | `÷100` | `%.2f` |
| 背景淡入 | double（秒） | 0..300 | `×100` | `÷100` | `%.1f` |

> **为什么不用现成的 double 滑条？**
>
> 库没有提供。与其为了一个滑块引入新的控件体系，不如把“精度”编码进整数：0.01 的精度 → 乘 100；0.1 的精度 → 乘 10。这是很常见的做法，代价是每个字段都要记得读和写用同一个倍数。

### 4.3 滑条的“自己改自己”问题

你可能会担心：supplier 每 100ms 读配置，而滑条拖动时又在写配置，会不会打架？

NarutoUI 在 `MuiControls.sliderRow` 里做了两层保护：

1. `onProgressChanged` 只在 `fromUser == true` 时回调你的 consumer，程序化 `setProgress` 不会触发写回；
2. 轮询时如果正在拖动（`tracking[0]`）或正在同步（`syncing[0]`），就跳过这一轮。

所以你可以放心地让 supplier 读“滑条自己写进去的值”。NarutoOverlay 的所有滑条都是这么写的。

### 4.4 分组与自动分隔线

```java
// 文件：config/ui/page/ConfigAppearancePage.java（节选）
private static void performance(@NotNull MuiSettingsPage page) {
    page.section(MuiLocalization.get(GROUP + "appearance.performance"), section -> section
            .slider(MuiLocalization.get(GROUP + "appearance.fpsInterval"), 1, 200,
                    RenderArgs.FPS_COOLDOWN::get,
                    value -> {
                        RenderArgs.FPS_COOLDOWN.set(value);
                        ConfigSaver.changed();
                    })
            .slider(MuiLocalization.get(GROUP + "appearance.gcInterval"), 1, 200,
                    RenderArgs.GC_COOLDOWN::get,
                    value -> {
                        RenderArgs.GC_COOLDOWN.set(value);
                        ConfigSaver.changed();
                    })
            .slider(MuiLocalization.get(GROUP + "appearance.ramInterval"), 1, 200,
                    RenderArgs.RAM_COOLDOWN::get,
                    value -> {
                        RenderArgs.RAM_COOLDOWN.set(value);
                        ConfigSaver.changed();
                    })
            .slider(MuiLocalization.get(GROUP + "appearance.fpsWindow"), 10, 1000,
                    RenderArgs.FPS_WINDOW::get,
                    value -> {
                        RenderArgs.FPS_WINDOW.set(value);
                        ConfigSaver.changed();
                    }));
}
```

`section` 回调里的每个字段都会自动：

- 生成一行（标题 + 控件）；
- 在第一行之后、每一行之前插入一条分隔线；
- 使用统一的 44dp 最小行高和左右 2dp / 上下 6dp 的内边距。

所以**不要在 section 里自己加卡片背景**，否则会出现“卡片套卡片”的双层圆角。NarutoOverlay 的 Appearance 页没有任何手动背景，全部交给 section。

### 4.5 一行里放两个控件：`customLayout` 和 `custom`

高层字段不够用时，有两个入口：

```java
// 方式一：customLayout 拿到一个可链式的 MuiLayout（空行），自己往里加
//        （示意代码，section 来自 .section("...", section -> ...) 的回调参数）
.section("Rendering", section -> section
        .customLayout(row -> row
                .spacing(6)
                .add(section.page().valueRow("Version", "1.0.0"))))

// 方式二：custom(View) 把一整行视图交给库，库负责分隔线和整行宽度
.section("Rendering", section -> section
        .custom(section.page().valueRow("Version", "1.0.0")))
```

两者都会**参与自动分隔线**：第一个字段不加线，后面的加。Appearance 页的边框颜色行用的就是 `customLayout`（第 5.2 节）。

---

## 5. 自定义行：主题卡片与颜色选择

这一章的两个例子，是“高层 DSL 搞不定时”的标准解法。

### 5.1 主题卡片：给选择器提供数据，而不是提供布局

`MuiThemePicker` 是通用的两列卡片选择器。它不认识 `Theme` 枚举，只认识 `Option` 接口。NarutoOverlay 用一个 `record` 做适配：

> 当前 API 还提供 `MuiThemePicker.create(context, theme, options, inUse, changed)`，返回一个 `MuiLayout`，可以嵌进已有 section；`add(...)` 仍然自带 section，并返回页面以便继续链式调用。

```java
// 文件：config/ui/page/ConfigAppearancePage.java（节选）
private static void themes(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
    List<MuiThemePicker.Option> options = new ArrayList<>();
    for (Theme theme : Theme.values()) options.add(new ThemeOption(theme));

    // ← 注意：MuiThemePicker.add 内部会自己调用 page.section(...)，
    //    所以这里不要再包一层 page.section，否则会出现空卡片 + 嵌套卡片。
    MuiThemePicker.add(page,
            MuiLocalization.get(GROUP + "appearance.theme"),
            options,
            MuiLocalization.get(GROUP + "appearance.inUse"),
            // ← 选中后：标脏 + 让宿主重建当前页（颜色在构建时解析，不重建不会变）。
            () -> {
                ConfigSaver.changed();
                host.onThemeChanged();
            });
}

private record ThemeOption(Theme theme) implements MuiThemePicker.Option {
    @Override
    public @NotNull @Unmodifiable CharSequence title() {
        // ← 显示名也用翻译键拼出来：narutooverlay.config.theme.naruto 之类。
        return MuiLocalization.get(GROUP + "theme." + this.theme.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean selected() {
        // ← 当前选中的判断：直接读配置，不缓存。
        return RenderArgs.THEME.get() == this.theme;
    }

    @Override
    public void select() {
        // ← 只写配置。保存和界面刷新由外面的 changed 回调负责。
        RenderArgs.THEME.set(this.theme);
    }

    @Override
    public int accent() {
        // ← 没有强调色的主题（例如 CLASSIC）退回到普通色，避免透明色块。
        Palette palette = this.theme.palette();
        return palette.hasAccent() ? palette.accent() : palette.normal();
    }

    @Override
    public int surface() {
        return this.theme.palette().backplane();
    }

    @Override
    public @NotNull View preview(@NotNull Context context, @NotNull MuiTheme theme) {
        // ← 用 modern 工厂而不是当前屏幕的 shimmer 工厂：
        //    预览要“静态、准确”，不希望流光把颜色晃来晃去。
        return MuiControls.withFactory(MuiWidgetFactory.modern(), () -> {
            Palette palette = this.theme.palette();

            FrameLayout preview = new FrameLayout(context);
            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(context, 8));
            background.setColor(palette.backplane());
            background.setStroke(Math.max(1, MuiControls.dp(context, 1)), opaque(palette.border()));
            preview.setBackground(background);
            preview.setMinimumHeight(MuiControls.dp(context, 58));

            LinearLayout lines = MuiControls.column(context);
            lines.setPadding(MuiControls.dp(context, 7), MuiControls.dp(context, 7),
                    MuiControls.dp(context, 7), MuiControls.dp(context, 7));
            // ← 用真实的 HUD 文案做预览：FPS / RAM / GC 三行，玩家一眼就知道主题长什么样。
            lines.addView(previewLine(context, "FPS ", "144", palette), MuiControls.fill());
            lines.addView(previewLine(context, "RAM ", "8192/16384", palette), MuiControls.fill());
            lines.addView(previewLine(context, "GC ", "12 ms", palette), MuiControls.fill());

            preview.addView(lines, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));
            return preview;
        });
    }

    private static @NotNull LinearLayout previewLine(@NotNull Context context, @NotNull String label,
                                                     @NotNull String value, @NotNull Palette palette) {
        LinearLayout line = MuiControls.row(context);
        line.setMinimumHeight(0);      // ← 预览行要紧凑，覆盖 row() 默认的 44dp
        line.setPadding(0, 0, 0, 0);   // ← 去掉 row() 默认内边距
        MuiTheme theme = MuiTheme.modern(context);
        // ← opaque()：把调色板里可能带 alpha 的颜色强制不透明，否则预览会透出卡片背景。
        line.addView(MuiControls.text(context, theme, label, 10, opaque(palette.label())), MuiControls.wrap());
        line.addView(MuiControls.text(context, theme, value, 10, opaque(palette.value(ValueLevel.NORMAL))), MuiControls.wrap());
        return line;
    }
}
```

这段代码有几个非常实战的点：

1. **`Option` 是“数据 + 行为”，不是视图**。库负责生成卡片、选中描边、“In use”标签、两列排布。
2. **`preview()` 返回 `null` 时用默认色块预览**（一条 accent + 一块 surface）。NarutoOverlay 选择自定义，因为“主题长什么样”用三行 HUD 文案表达最直观。
3. **自定义预览里要自己处理紧凑度**。`MuiControls.row` 默认 44dp 高、带内边距，适合表单行，不适合 58dp 的预览框。
4. **在预览里切换工厂**。`withFactory` 是线程本地栈，套在 lambda 里，退出自动恢复，不会污染外面的 shimmer 控件。

### 5.2 颜色选择：字段只负责显示，浮层负责选择

`MuiColorField` 只画“色块 + 标签 + 按钮”，点击弹什么、选完怎么存，全由调用方决定。NarutoOverlay 把它接在 Appearance 页的“边框”分区：

```java
// 文件：config/ui/page/ConfigAppearancePage.java（节选）
private static void backplane(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
    page.section(MuiLocalization.get(GROUP + "appearance.backplane"), section -> section
            .slider(MuiLocalization.get(GROUP + "appearance.borderWidth"), 0, 400,
                    () -> (int) Math.round(RenderArgs.BACKPLANE_BORDER_WIDTH.get() * 100.0D),
                    value -> {
                        RenderArgs.BACKPLANE_BORDER_WIDTH.set(value / 100.0D);
                        ConfigSaver.changed();
                    },
                    value -> String.format(Locale.ROOT, "%.2f", value / 100.0D))
            .toggle(MuiLocalization.get(GROUP + "appearance.borderOverride"),
                    MuiLocalization.get(GROUP + "appearance.borderOverride.desc"),
                    RenderArgs.BACKPLANE_BORDER_OVERRIDE::get,
                    value -> {
                        RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(value);
                        ConfigSaver.changed();
                    })
            // ← 自定义一行：这一行里放 ColorField。
            .customLayout(row -> addColorPicker(row, host)));
}

private static void addColorPicker(@NotNull MuiLayout row, @NotNull MuiScreenHost host) {
    Context context = row.getContext();
    MuiTheme theme = MuiTheme.modern(context);

    MuiColorField field = MuiColorField.create(context, theme,
                    MuiLocalization.get(GROUP + "appearance.borderColor"), RenderArgs.borderColor())
            .buttonLabel(MuiLocalization.get(GROUP + "appearance.pickColor"))
            .onPick(() -> {
                // ← 浮层要在“页面构建之外”创建，所以它不会自动继承屏幕的 profile / factory。
                //    这里手动包一层，保证浮层里的按钮、滑条和主界面风格一致。
                Context pickerContext = host.requireContext();
                MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () ->
                        MuiControls.withFactory(new NarutoShimmerWidgetFactory(), () ->
                                host.showOverlay(new MuiColorPickerOverlay(pickerContext,
                                        MuiTheme.modern(pickerContext).withPrimary(Accent.primary()),
                                        RenderArgs.borderColor(),
                                        new MuiColorPickerOverlay.Labels(
                                                MuiLocalization.get(GROUP + "color.title"),
                                                MuiLocalization.get(GROUP + "color.brightness"),
                                                MuiLocalization.get(GROUP + "color.alpha"),
                                                MuiLocalization.get(GROUP + "color.apply"),
                                                MuiLocalization.get(GROUP + "color.cancel")),
                                        argb -> {
                                            // ← ARGB 拆成四个 Forge 配置项。
                                            //    注意 >>> 24：无符号右移，alpha 不会变成负数。
                                            RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(true);
                                            RenderArgs.BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
                                            RenderArgs.BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
                                            RenderArgs.BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
                                            RenderArgs.BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);
                                            ConfigSaver.changed();
                                            // ← 色块显示的是构建时的初始颜色，改完必须重建页面才会变。
                                            host.reloadCurrentPage();
                                        }))));
            });

    // ← customLayout 给的是一个空的 MuiLayout，必须自己给 LayoutParams。
    //    fill() = 宽度填满、高度包裹。
    row.add(field, MuiControls.fill());
}
```

> **为什么选择颜色还要“强制开启 override”？**
>
> 因为 `RenderArgs.borderColor()` 的逻辑是：override 为 true 才用自定义 RGBA，否则跟随主题调色板。用户既然手动选了颜色，就必须把 override 打开，否则选完还是显示主题色。这是业务语义，不是 UI 库的事——但它发生在 UI 回调里，所以写在这里。

> **`withCurrent` / `withFactory` 为什么是嵌套的？**
>
> 两者都是“线程本地的栈”。`withCurrent(profile, () -> withFactory(factory, () -> ...))` 表示：在这段代码里创建的所有控件，既使用这个 profile，也使用这个 factory。退出内层恢复 factory，退出外层恢复 profile。它们不影响屏幕其余部分。

---

## 6. 数据驱动：Cards 页与可折叠卡片

Cards 页是三个页面里最“像应用”的一个：卡片数量、字段数量都来自数据，不是写死的。它的做法值得完整学一遍。

### 6.1 页面只做一件事：把控制权交给视图模型

```java
// 文件：config/ui/page/ConfigCardsPage.java（节选）
public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
    // ← page.custom：不要 section 卡片，整页内容自己排。
    //    因为每张 HUD 卡片本身就是一张卡片，再套 section 会变成双层卡片。
    page.custom(ui -> new CardsView(ui, host).render());
}
```

`MuiPageContext` 能给你四样东西：`context()`、`theme()`、`content()`、`contentLayout()`。Cards 页把 context、theme 和可链式的内容列存起来：

```java
private static final class CardsView {
    private final Context context;
    private final MuiTheme theme;
    private final MuiLayout content;
    private final MuiScreenHost host;
    private boolean first = true;   // ← 只有第一张卡需要顶部外边距

    CardsView(@NotNull MuiPageContext page, @NotNull MuiScreenHost host) {
        this.context = page.context();
        this.theme = page.theme();
        this.content = page.contentLayout();   // ← 页面的纵向内容列（可链式）
        this.host = host;
    }

    void render() {
        // ← 数据驱动：卡片注册表里有几张，就建几个 CardView。
        for (Card<?> card : CardRegistry.all()) {
            CardView view = new CardView(this, card);
            this.content.add(view.view(), cardParams());
        }

        // ← 全部卡片的“重置”放在最下面，左对齐，上方留 20dp。
        MuiTextAction resetAll = MuiTextAction.create(this.context, this.theme,
                loc("resetAll"), this::resetAll);
        this.content.add(resetAll, resetAllParams());
    }

    void changed() {
        ConfigSaver.changed();      // ← 所有子视图共享同一个“标脏”入口
    }

    void rebuild() {
        this.host.reloadCurrentPage();
    }

    private void resetAll() {
        CardRegistry.resetAll();
        this.changed();
        this.rebuild();             // ← 重置改的是“构建时读到的初值”，必须重建
    }

    private @NotNull LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                MuiControls.MATCH, MuiControls.WRAP);
        // ← 第一张卡多 18dp 顶距（CARD_TOP_MARGIN），其余卡片之间靠 12dp 底距隔开。
        params.setMargins(0, this.first ? dp(this.context, 18) : 0, 0,
                dp(this.context, 12));
        this.first = false;
        return params;
    }
}
```

> **为什么拆成 `CardsView` / `CardView` / `FieldView` 三个类？**
>
> 如果全写成静态方法 + 一堆捕获变量，回调里会出现 `card`、`box`、`x`、`y`、`config`、`body` 六个自由变量，改一个字段要往上翻 200 行。拆成视图模型后：
>
> - `CardsView` 管“页面级”的事（列表、间距、全部重置）；
> - `CardView` 管“一张卡”的事（标题、位置、字段、单卡重置）；
> - `FieldView` 管“一行字段”的事（标题、chip、开关）。
>
> 每个回调只捕获 `this`，代码短、职责清楚。这是真人项目里比 API 更值得学的东西。

### 6.2 一张卡片：可折叠 + 位置编辑 + 预设

```java
// 文件：config/ui/page/ConfigCardsPage.java（节选）
private static final class CardView {
    private final CardsView parent;
    private final Context context;
    private final MuiTheme theme;
    private final Card<?> card;
    private final MuiCollapsibleCard box;   // ← 可折叠卡片本体
    private final MuiLayout body;           // ← 折叠区域（MuiLayout，可链式）
    private final MuiNumberField x;
    private final MuiNumberField y;

    CardView(@NotNull CardsView parent, @NotNull Card<?> card) {
        this.parent = parent;
        this.context = parent.context;
        this.theme = parent.theme;
        this.card = card;

        // ← 参数：context / theme / 标题 / 摘要 / 初始是否展开。
        //    false = 默认收起，先给玩家一个紧凑的全局视图。
        this.box = MuiCollapsibleCard.card(this.context, this.theme,
                cardName(card), summary(card), false);
        this.body = this.box.body();

        // ← 数字输入框：初值、范围、居中。
        //    bounds(0, MAX) 表示不允许负数；gravity(CENTER) 让数字居中好看。
        this.x = MuiNumberField.create(this.context, this.theme, card.leftValue().get())
                .bounds(0, Integer.MAX_VALUE)
                .gravity(Gravity.CENTER);
        this.y = MuiNumberField.create(this.context, this.theme, card.topValue().get())
                .bounds(0, Integer.MAX_VALUE)
                .gravity(Gravity.CENTER);

        this.renderHeader();
        this.renderPosition();
        this.body.divider();
        this.renderFields();   // ← 遍历字段、逐行插入分隔线，正文省略
        this.renderReset();    // ← 单卡“重置”操作，正文省略
    }

    private void renderHeader() {
        // ← 标题栏右侧的启用开关。trailing 只是“塞进标题栏右边”，语义由调用方决定。
        Switch toggle = MuiControls.switchControl(this.context, this.theme);
        toggle.setChecked(this.card.enabledValue().get());
        toggle.setContentDescription(loc("cards.enabled"));   // ← 无障碍描述
        toggle.setOnCheckedChangeListener((view, checked) -> this.setEnabled(checked));
        this.box.trailing(toggle);
    }

    private void renderPosition() {
        this.x.onChange(this::setX);   // ← 用户输入才触发；程序化 value() 不触发
        this.y.onChange(this::setY);

        // ← 一行放：X 标签、X 输入框、Y 标签、Y 输入框、弹性空白、重置操作。
        //    flexibleSpace() 把“重置”推到最右边。
        MuiLayout row = MuiLayout.row(this.context, this.theme).padding(0)
                .add(MuiControls.text(this.context, this.theme, "X", 12, this.theme.onSurfaceVariant()))
                .add(this.x, fieldParams(this.context))
                .add(MuiControls.text(this.context, this.theme, "Y", 12, this.theme.onSurfaceVariant()), yLabelParams(this.context))
                .add(this.y, fieldParams(this.context))
                .flexibleSpace()
                .add(MuiTextAction.create(this.context, this.theme,
                        loc("cards.resetPosition"), this::resetPosition));
        this.body.add(row);

        // ← 四个角落预设按钮，等宽平分。
        MuiLayout presets = MuiLayout.row(this.context, this.theme).padding(0).spacing(6);
        presets.addWeighted(this.presetButton("cards.topLeft", 0, 0), 1.0F)
                .addWeighted(this.presetButton("cards.topRight", 1, 0), 1.0F)
                .addWeighted(this.presetButton("cards.bottomLeft", 0, 1), 1.0F)
                .addWeighted(this.presetButton("cards.bottomRight", 1, 1), 1.0F);

        LinearLayout.LayoutParams presetParams =
                new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP);
        presetParams.topMargin = dp(this.context, PRESET_TOP_MARGIN);
        this.body.add(presets, presetParams);
    }
}
```

几个细节，值得单独说：

**为什么 `MuiNumberField` 用 `bounds(0, Integer.MAX_VALUE)`？**

因为 HUD 坐标不允许负数（会被画到屏幕外）。`MuiNumberField` 会在用户输入时 clamp，并且把越界的输入“回弹”成合法值。程序化 `value(int)` 也会 clamp，但**不会触发 `onChange`**——这正是 `syncPosition()` 能安全调用 `x.value(...)` 而不造成回调风暴的原因。

**预设位置怎么算？**

```java
private void preset(int horizontal, int vertical) {
    Minecraft minecraft = Minecraft.getInstance();
    int width = this.card.lineWidth();
    if (width <= 0) width = 160;                       // ← 还没渲染过时的兜底宽度
    int height = minecraft.font.lineHeight;            // ← HUD 一行的高度
    int margin = 8;
    // ← 用 GUI 缩放后的尺寸，不是物理像素。HUD 坐标就是这套坐标系。
    int left = horizontal == 0 ? margin
            : minecraft.getWindow().getGuiScaledWidth() - width - margin;
    int top = vertical == 0 ? margin
            : minecraft.getWindow().getGuiScaledHeight() - height - margin;
    this.card.moveTo(left, top);                       // ← 写配置
    this.changed();                                    // ← 标脏
    this.syncPosition();                               // ← 把新坐标写回两个输入框
}
```

**摘要怎么刷新？**

`MuiCollapsibleCard.summary(text)` 只改标题栏那一行字，不重建卡片。所以位置变化时调用它，开销很小：

```java
private void refreshSummary() {
    this.box.summary(summary(this.card));   // ← 例如 "3 / 4 字段 · (10, 5)"
}
```

**为什么“重置单卡”要重建页面，而“重置位置”不用？**

- 重置位置只改 `x/y` 两个值，`syncPosition()` 可以直接把控件值写回去；
- 重置单卡会把所有字段开关、位置、启用状态全部恢复默认，而**开关和 chip 的初值是在构建时读的**。不重建页面，界面就会和配置对不上。所以 `resetCard()` 走 `parent.rebuild()`。

### 6.3 一行字段：标题 + chip + 开关

```java
// 文件：config/ui/page/ConfigCardsPage.java（节选）
private static final class FieldView {
    private final CardView parent;
    private final Field<?> field;
    private final FieldConfig config;
    private final MuiLayout row;

    FieldView(@NotNull CardView parent, @NotNull Field<?> field) {
        this.parent = parent;
        this.field = field;
        this.config = parent.card.fieldConfig(field.id());
        this.row = this.buildRow();
    }

    private @NotNull MuiLayout buildRow() {
        MuiLayout row = MuiLayout.row(this.parent.context, this.parent.theme).padding(0);
        // ← 标题占满剩余宽度，把后面的控件挤到右边。
        row.add(MuiControls.text(this.parent.context, this.parent.theme,
                fieldName(this.field), 13, this.parent.theme.onSurface()),
                MuiControls.weight());

        // ← 只有这个字段“有标签/单位”时，才显示对应 chip。
        //    否则会出现一个点了没反应的 chip。
        if (this.field.hasLabel()) {
            row.add(MuiChip.create(this.parent.context, this.parent.theme,
                            loc("chip.label"), this.config.showLabel())
                    .onChanged(this::setLabel), chipParams(this.parent.context));
        }
        if (this.field.hasUnit()) {
            row.add(MuiChip.create(this.parent.context, this.parent.theme,
                            loc("chip.unit"), this.config.showUnit())
                    .onChanged(this::setUnit), chipParams(this.parent.context));
        }

        Switch toggle = MuiControls.switchControl(this.parent.context, this.parent.theme);
        toggle.setChecked(this.config.enabled());
        toggle.setOnCheckedChangeListener((view, checked) -> this.setEnabled(checked));
        LinearLayout.LayoutParams toggleParams = MuiControls.wrap();
        toggleParams.setMargins(dp(this.parent.context, 10), 0, 0, 0);
        row.add(toggle, toggleParams);
        return row;
    }

    private void setLabel(boolean checked) {
        // ← FieldConfig 的 showLabelValue() 可能为 null（这个字段不支持标签）。
        //    虽然上面已经用 hasLabel() 过滤过，但这里再判一次，防止未来改动漏掉。
        if (this.config.showLabelValue() != null) this.config.showLabelValue().set(checked);
        this.parent.changed();
    }

    private void setEnabled(boolean enabled) {
        this.config.enabledValue().set(enabled);
        this.parent.changed();
        this.parent.refreshSummary();   // ← 启用数变了，摘要里的 "x / y 字段" 要更新
    }
}
```

`MuiChip` 有两个容易忽略的行为：

- 点击会自己切换选中状态，然后才回调 `onChanged`；
- 它的背景是 shimmer target，进窗口自动注册、出窗口自动注销，**不需要你管生命周期**。

---

## 7. 只读信息页：About 页的“展示型”写法

About 页没有开关和滑条（除了背景参数），全是“展示 + 操作”。它示范了 `value`、`text`、`MuiDescriptionRow`、`MuiTextAction` 的组合。

### 7.1 信息卡片

```java
// 文件：config/ui/page/ConfigAboutPage.java（节选）
public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiBackgroundActions background) {
    // ← 构建时读一次快照。注意：后续背景参数变化时，页面会被 controller 重建。
    MuiBackgroundState state = background.state();

    page.section(MuiLocalization.get(GROUP + "about.info"), section -> section
            .value(MuiLocalization.get(GROUP + "about.version"), version())
            // ← value 链式调用：每次返回 section，所以可以一直点下去。
            .value(MuiLocalization.get(GROUP + "about.environment"),
                    NarutoOverlay.ENV + " \u00B7 "
                            + SharedConstants.getCurrentVersion().getName() + " \u00B7 "
                            + MuiLocalization.get(GROUP + "about.clientOnly")));

    page.section(MuiLocalization.get(GROUP + "about.background"), section -> {
        String folder = state.folder();
        int count = background.images().size();   // ← 读文件夹里实际有多少张图

        section.value(MuiLocalization.get(GROUP + "background.folder"),
                folder.isEmpty()
                        ? MuiLocalization.get(GROUP + "background.folder.none")
                        : folder);
        section.text(MuiLocalization.get(GROUP + "background.folder.hint"));
        section.customLayout(row -> addFolderButtons(row, background, folder));
        section.value(MuiLocalization.get(GROUP + "background.images"),
                count == 0 ? MuiLocalization.get(GROUP + "background.images.empty")
                        : String.valueOf(count));

        // ← 关键：这里用 supplier 版本，而不是把 state 的值算死。
        //    因为 setBackgroundInterval 会替换整个 state，supplier 每次都能读到新值。
        section.slider(MuiLocalization.get(GROUP + "background.interval"), 2, 300,
                () -> (int) Math.round(background.state().interval()),
                background::setBackgroundInterval,
                value -> MuiLocalization.get(GROUP + "background.interval.value", value));
        section.slider(MuiLocalization.get(GROUP + "background.fade"), 0, 300,
                () -> (int) Math.round(background.state().fade() * 100.0D),
                value -> background.setBackgroundFade(value / 100.0D),
                value -> MuiLocalization.get(GROUP + "background.fade.value",
                        String.format(Locale.ROOT, "%.1f", value / 100.0D)));
        section.slider(MuiLocalization.get(GROUP + "background.dim"), 0, 95,
                () -> (int) Math.round(background.state().dim() * 100.0D),
                value -> background.setBackgroundDim(value / 100.0D),
                value -> value + "%");
    });
    // ...
}
```

**这里有一个值得注意的“混搭”：**

- 文件夹名、图片数量是**构建时读一次**的静态值；
- 三个滑条是**每次轮询都读**的 supplier。

为什么能混？因为背景 controller 在“选文件夹 / 清空文件夹 / 应用裁剪”之后会调用 `reloadCurrentPage()`（见第 9 章），页面会被重建，静态值自然更新。而滑条不需要重建，所以用 supplier 实时读。

> **你写自己的页面时**
>
> 如果一个值会被别处改变、而且改变时不会重建页面 → 用 supplier；
> 如果改变时你愿意/必须重建页面 → 静态值也可以。
> 判断标准是“界面会不会自己刷新”，不是“值是不是动态的”。

### 7.2 说明行：比 text 更像“设置项”的只读行

```java
page.section(MuiLocalization.get(GROUP + "about.usage"), section -> {
    Context context = section.page().context();
    MuiTheme theme = section.page().theme();
    // ← custom(View) 把整行交给库：库负责分隔线和整行宽度。
    //    MuiDescriptionRow 是“标题 + 可选说明”的纵向两行。
    section.custom(MuiDescriptionRow.create(context, theme,
            MuiLocalization.get(GROUP + "about.hint.drag"),
            MuiLocalization.get(GROUP + "about.hint.drag.desc")));
    // ← 其余四条（merge / split / open / reset）写法完全一样，省略。
});
```

`MuiDescriptionRow` 和 `section.text(...)` 的区别：

- `text`：一段说明文字，字号 12、次要色，适合卡片内的解释段落；
- `MuiDescriptionRow`：标题 13、说明 11，带纵向留白，更像“一条设置项的说明”，适合操作指南。

### 7.3 文本操作：复制、打开文件夹

```java
private static void addPathActions(@NotNull MuiLayout row, @NotNull Path path) {
    Context context = row.getContext();
    MuiTheme theme = MuiTheme.modern(context);
    MuiLayout actions = MuiLayout.row(context, theme).padding(0);

    // ← 先创建不带回调的 action，因为回调里要引用 action 自己（改文案）。
    MuiTextAction copy = MuiTextAction.create(context, theme,
            MuiLocalization.get(GROUP + "about.copyPath"));
    copy.onClick(() -> {
        Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
        copy.text(MuiLocalization.get(GROUP + "about.copied"));   // ← 即时反馈
    });

    MuiTextAction open = MuiTextAction.create(context, theme,
            MuiLocalization.get(GROUP + "about.openFolder"), () -> reveal(path));

    actions.add(copy, MuiControls.weight());   // ← 复制占满左边
    LinearLayout.LayoutParams openParams = MuiControls.wrap();
    openParams.leftMargin = MuiControls.dp(context, 12);
    actions.add(open, openParams);
    row.add(actions, MuiControls.fill());
}
```

`MuiTextAction` 的三个实战点：

1. 它是 `FrameLayout` 包一个 `TextView`，点击区域比纯文字大，不容易点空；
2. 支持后改文案（`copy.text(...)`），适合“复制 → 已复制”这种反馈；
3. 标签是从当前 widget factory 创建的，所以它**也会参与流光**（如果工厂是 shimmer）。

`reveal(path)` 是纯平台代码（Windows 用 `explorer /select,`，macOS 用 `open -R`，其他退回 `Util.getPlatform().openFile(parent)`），和 NarutoUI 无关，这里不展开。

### 7.4 页脚：直接用低层内容列

```java
page.custom(context -> {
    // ← context 是 MuiPageContext，content() 是页面的纵向内容列。
    TextView footer = MuiControls.text(context.context(), context.theme(),
            MuiLocalization.get(GROUP + "about.footer"), 11,
            context.theme().onSurfaceVariant());
    footer.setGravity(Gravity.CENTER_HORIZONTAL);
    LinearLayout.LayoutParams params = MuiControls.fill();
    params.topMargin = MuiControls.dp(context.context(), 28);
    context.content().addView(footer, params);   // ← 不想要自动分隔线，所以直接 addView
});
```

这里没有用 `section.text(...)`，因为页脚不该被包在卡片里。`page.custom` 是“页面级逃生口”，适合这种一次性布局。

---

## 8. 视觉层：主题、控件工厂、流光

### 8.1 控件工厂：只改颜色，不改实现

NarutoUI 自带一套 shimmer 控件。NarutoOverlay 的需求是“用这套控件，但强调色跟随自己的主题调色板”。做法是写一个**适配器**，而不是复制一套控件：

```java
// 文件：config/ui/widget/NarutoShimmerWidgetFactory.java（节选）
public final class NarutoShimmerWidgetFactory implements MuiWidgetFactory {
    // ← 复用库里的 shimmer 实现，不重新造轮子。
    private static final MuiWidgetFactory SHIMMER = MuiWidgetFactory.shimmer();

    private static @NotNull MuiTheme naruto(@NotNull MuiTheme theme) {
        // ← 只覆盖两个 token：强调色和“强调色上的文字色”。
        //    其余 token（surface / outline / onSurface…）沿用 Modern UI 主题。
        return theme.withPrimary(Accent.primary()).withOnPrimary(0xFF000000);
    }

    @Override
    public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
        // ← 文本不需要换色：颜色是调用方逐处传进来的。
        return SHIMMER.text(context, text, size, color);
    }

    @Override
    public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
        return SHIMMER.switchControl(context, naruto(theme));
    }

    @Override
    public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
        return SHIMMER.slider(context, naruto(theme), max, progress);
    }

    @Override
    public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label,
                                  boolean outlined, @NotNull View.OnClickListener onClick) {
        return SHIMMER.button(context, naruto(theme), label, outlined, onClick);
    }
}
```

> **为什么不让库直接支持“换色”？**
>
> 因为“换色”是调用方的品牌决策，不是库的职责。库提供 `withPrimary`、`withSurface`、`withOutline`、`withCornerRadius` 这一整套 token 级覆盖，已经足够；再往上抽象（比如“Naruto 主题”）就把业务塞进库了。

### 8.2 强调色从哪来：Accent

```java
// 文件：config/ui/widget/Accent.java（节选）
public final class Accent {
    private static @NotNull Palette palette() {
        return RenderArgs.palette();   // ← 跟随玩家选的 HUD 主题
    }

    public static int primary() {
        Palette palette = palette();
        // ← 有强调色就用边框色（并强制不透明）；没有就用白色。
        return palette.hasAccent() ? palette.border() | 0xFF000000 : 0xFFFFFFFF;
    }

    public static int onPrimary() {
        // ← 强调色上的文字色。有强调色用深棕，无强调色用近黑。
        return palette().hasAccent() ? 0xFF2A1400 : 0xFF16171C;
    }

    public static int danger() {
        // ← 破坏性操作用 critical 色（目前 NarutoOverlay 的配置页还没用到）。
        return palette().critical() | 0xFF000000;
    }
}
```

这是一个很好的示范：**不要把颜色写死在 UI 代码里**。UI 只问 `Accent.primary()`，具体值由当前主题决定；换主题时只要重建页面，颜色自然跟着变。

### 8.3 流光参数：把配置项喂给 profile

```java
// 文件：config/ui/NarutoShimmerConfig.java（节选）
public final class NarutoShimmerConfig implements MuiShimmerConfig {
    // ← 这是 NarutoOverlay 自己的 HUD profile。
    //    它不是库的 default profile，所以别的模组可以独立配置自己的流光，互不干扰。
    private static final MuiShimmerProfile HUD_PROFILE =
            MuiShimmerProfile.create(new NarutoShimmerConfig());

    public static @NotNull MuiShimmerProfile profile() {
        return HUD_PROFILE;
    }

    @Override
    public boolean text() {
        // ← HUD 文字是否流光（总开关）。
        return RenderArgs.TEXT_SHIMMER.get();
    }

    @Override
    public boolean configText() {
        // ← 配置界面文字是否流光。
        return RenderArgs.CONFIG_TEXT_SHIMMER.get();
    }

    @Override
    public boolean controls() {
        // ← 配置界面控件是否流光。
        return RenderArgs.CONTROL_SHIMMER.get();
    }

    @Override
    public boolean hasAccent() {
        // ← 没有强调色的主题（CLASSIC）直接不流光，避免白光乱闪。
        return RenderArgs.palette().hasAccent();
    }

    @Override
    public int fps() {
        return RenderArgs.CONFIG_SHIMMER_FPS.get();
    }

    @Override
    public double speed() {
        return RenderArgs.TEXT_SHIMMER_SPEED.get();
    }

    @Override
    public double scale() {
        return RenderArgs.TEXT_SHIMMER_SCALE.get();
    }

    @Override
    public double strength() {
        return RenderArgs.TEXT_SHIMMER_STRENGTH.get();
    }

    @Override
    public int @NotNull [] ramp() {
        // ← 9 个颜色，对应绘制时的 9 个色标（0.00, 0.12, 0.25, 0.40, 0.55, 0.70, 0.82, 0.92, 1.00）。
        //    白色 → 警告色 → 强调色 → 标签色 → 危险色 → 再回来，形成一个循环色带。
        Palette palette = RenderArgs.palette();
        return new int[]{
                0xFFFFFFFF,
                palette.warn() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.critical() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.warn() | 0xFF000000,
                0xFFFFFFFF
        };
    }
}
```

几个关键事实（来自库的实现）：

- 流光是**两道波干涉**，按控件在窗口中的绝对位置采样，所以相邻文字不会同时变成同一个颜色；
- 只有 `text()` 和 `hasAccent()` 同时为 true，某个目标才会真正动画；
- 引擎默认最低 30fps，实际取所有活跃目标里最大的 `fps()`；
- 每个 profile 有自己的时钟，速度/密度的平滑过渡互不影响。

### 8.4 什么时候必须手动 `withCurrent`

屏幕构建时，`MuiConfigFragment` 会自动把 builder 里的 profile 设为“当前”：

```java
// 文件：NarutoUI/.../screen/MuiConfigFragment.java（节选）
private @NotNull View createPage(@NotNull Context context, @NotNull MuiConfigScreen.PageSpec spec) {
    // ← 建页面前压入 profile + factory，建完弹出。
    //    所以页面里的控件自动“继承”屏幕配置，你什么都不用做。
    return MuiShimmerProfile.withCurrent(this.model.shimmerProfile(), () ->
            MuiControls.withFactory(this.model.widgetFactory(), () -> {
                if (spec.viewFactory() != null) return spec.viewFactory().apply(context);
                return new MuiPage(context, this.theme, this.model.contentWidth(),
                        spec.title(), spec.subtitle(), spec.body()).root();
            }));
}
```

但**浮层**不在这个保护范围内：颜色选择器、裁剪框是在点击回调里 `new` 出来的。任何你在“页面构建阶段之外”手动创建的控件都一样。

作为对照：`railHeader` / `railFooter` 在 rail 构建时调用，而 rail 构建本身被 `withCurrent` + `withFactory` 包着（`MuiConfigFragment` 里 `buildRail(...)` 的调用点），所以它们是自动继承的。

所以 NarutoOverlay 在创建颜色浮层时手动包了 `withCurrent` + `withFactory`（第 5.2 节）。这是“看起来多余、少了就出问题”的典型代码。

### 8.5 改完流光设置为什么要 `refresh()`

```java
// 文件：NarutoUI/.../shimmer/MuiShimmer.java（节选）
public static void refresh() {
    boolean active = false;
    for (ShimmerTarget target : TARGETS) {
        if (target.shimmerEnabled()) { active = true; break; }
    }
    if (!active) {
        restore();   // ← 全部恢复基础色
        stop();      // ← 停掉动画器
        return;
    }
    start();         // ← 需要时启动
}
```

`refresh()` 不会自动被配置变化触发——库不知道你的配置什么时候变。所以：

- 关掉/打开“文字流光”“配置流光”“控件流光”时，调 `MuiShimmer.refresh()`；
- 改速度/密度/强度不需要调，每次刷新都会从 config 读；
- 改颜色 ramp 也不需要调，下一次刷新就用新色带。

---

## 9. 背景：可配置背景是怎么接进来的

### 9.1 一行 `background(...)` 背后的四个部件

`MuiConfigurableBackground` 把“选图 / 选文件夹 / 裁剪 / 轮播 / 持久化”打包在一起。它需要四个东西：

1. `MuiBackgroundSettings`：怎么读、怎么写你的配置；
2. `MuiBackgroundLabels`：所有按钮和提示的文案；
3. `MuiBackgroundFiles`：文件放哪、怎么列、怎么拷贝；
4. `MuiCropOverlayFactory`（可选）：裁剪框用什么主题和控件。

NarutoOverlay 的组装：

```java
// 文件：config/ui/ConfigUi.java（节选）
static final class ConfigBackgroundFactory {
    private static final String GROUP = "narutooverlay.config.background.";
    private static final String BASE_NAME = "narutooverlay-config-bg";   // ← 存图时的固定前缀

    static @NotNull MuiConfigurableBackground create() {
        return new MuiConfigurableBackground(
                new ConfigBackgroundSettings(),                        // ← 持久化适配器
                labels(),                                              // ← 文案
                new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), BASE_NAME), // ← 游戏目录 + 前缀
                cropFactory());                                        // ← 裁剪浮层工厂
    }

    private static @NotNull MuiBackgroundLabels labels() {
        // ← 构造顺序（8 个参数）：
        //    选图标题、图片过滤器名、选图失败、选文件夹标题、选文件夹失败、
        //    读取/保存失败、保存成功、裁剪框的 4 个标签。
        return new MuiBackgroundLabels(
                MuiLocalization.get(GROUP + "picker"),
                MuiLocalization.get(GROUP + "filter"),
                MuiLocalization.get(GROUP + "pickerFailed"),
                MuiLocalization.get(GROUP + "folder.picker"),
                MuiLocalization.get(GROUP + "folder.failed"),
                MuiLocalization.get(GROUP + "failed"),
                MuiLocalization.get(GROUP + "saved"),
                new MuiCropOverlay.Labels(
                        MuiLocalization.get(GROUP + "crop.title"),
                        MuiLocalization.get(GROUP + "crop.hint"),
                        MuiLocalization.get(GROUP + "crop.confirm"),
                        MuiLocalization.get(GROUP + "crop.cancel")));
    }

    private static @NotNull MuiCropOverlayFactory cropFactory() {
        // ← 裁剪框是“点击后才创建”的浮层，同样需要手动继承 profile + factory。
        return (context, image, width, height, target, cropLabels, listener) ->
                MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () ->
                        MuiControls.withFactory(new NarutoShimmerWidgetFactory(), () ->
                                new MuiCropOverlay(context,
                                        MuiTheme.modern(context).withPrimary(Accent.primary()),
                                        image, width, height, target, cropLabels, listener)));
    }
}
```

### 9.2 持久化适配器：把通用 state 映射到 Forge 配置

```java
// 文件：config/ui/ConfigUi.java（节选）
static final class ConfigBackgroundSettings implements MuiBackgroundSettings {
    @Override
    public @NotNull MuiBackgroundState load() {
        List<String> order = new ArrayList<>();
        for (String name : RenderArgs.BACKGROUND_ORDER.get()) {
            // ← Forge 的 List 配置可能含 null（例如手改配置文件），过滤掉再交给 state。
            if (name != null) order.add(name);
        }

        // ← 参数顺序：image, folder, order, interval, fade, dim, crop。
        return new MuiBackgroundState(
                RenderArgs.BACKGROUND_IMAGE.get(),
                RenderArgs.BACKGROUND_FOLDER.get(),
                order,
                RenderArgs.BACKGROUND_INTERVAL.get(),
                RenderArgs.BACKGROUND_FADE.get(),
                RenderArgs.BACKGROUND_DIM.get(),
                new MuiCrop(
                        RenderArgs.BACKGROUND_CROP_X.get(),
                        RenderArgs.BACKGROUND_CROP_Y.get(),
                        RenderArgs.BACKGROUND_CROP_WIDTH.get(),
                        RenderArgs.BACKGROUND_CROP_HEIGHT.get()));
    }

    @Override
    public void save(@NotNull MuiBackgroundState state) {
        // ← 逐个写回。state 是不可变 record，不会出现“改一半”的中间态。
        RenderArgs.BACKGROUND_IMAGE.set(state.image());
        RenderArgs.BACKGROUND_FOLDER.set(state.folder());
        RenderArgs.BACKGROUND_ORDER.set(new ArrayList<>(state.order()));   // ← 复制，避免外部改列表
        RenderArgs.BACKGROUND_INTERVAL.set(state.interval());
        RenderArgs.BACKGROUND_FADE.set(state.fade());
        RenderArgs.BACKGROUND_DIM.set(state.dim());

        MuiCrop crop = state.crop();
        RenderArgs.BACKGROUND_CROP_X.set(crop.x());
        RenderArgs.BACKGROUND_CROP_Y.set(crop.y());
        RenderArgs.BACKGROUND_CROP_WIDTH.set(crop.width());
        RenderArgs.BACKGROUND_CROP_HEIGHT.set(crop.height());

        ConfigSaver.changed();   // ← 背景控制器不知道你的保存策略，必须由适配器通知
    }
}
```

> **为什么 `save` 里要调 `ConfigSaver.changed()`？**
>
> 库的契约是“我调你的 `save`，你负责真正持久化”。NarutoOverlay 的持久化是防抖的，所以 `save` 只写内存 + 标脏，400ms 后统一 `CONFIG.save()`。如果你不用防抖，也可以在这里直接写盘——这是你的选择，不是库的。

### 9.3 文件助手做了什么

`MuiBackgroundFiles` 不碰任何 UI 和配置，只做四件事：

| 方法 | 行为 | 为什么重要 |
|---|---|---|
| `file(name)` | 把相对名解析到游戏目录 | 配置里存相对路径，换整合包目录也能用 |
| `folder(configured)` | 绝对路径直接用，相对路径拼游戏目录 | 允许玩家把手动文件夹放到任意位置 |
| `images(folder, order)` | 列出图片，`order` 里的优先，其余按字母序 | 轮播顺序可控，新增图片不会打乱 |
| `copyInto(source)` | 以固定前缀 + 原扩展名拷进游戏目录 | 原图不动，配置里只存一个稳定文件名 |

NarutoOverlay 还额外做了“内置背景解压”：

```java
// 文件：config/ui/background/BundledBackgrounds.java（节选）
public static void install() {
    Path folder = folder();
    try {
        Files.createDirectories(folder);
    } catch (IOException error) {
        LOGGER.error("Could not create the bundled background folder {}", folder, error);
        return;
    }
    for (Asset asset : ASSETS) {
        copy(asset, folder.resolve(asset.name()));   // ← 已存在就跳过，绝不覆盖玩家改过的图
    }
}
```

`migrateLegacyDefault()` 只迁移“完全等于老默认值”的配置：

```java
if (!RenderArgs.BACKGROUND_FOLDER.get().isBlank()) return;
if (!"narutooverlay-config-bg.png".equals(RenderArgs.BACKGROUND_IMAGE.get())) return;
// ← 只有同时满足这两个条件，才认为是老默认配置，才切到内置轮播文件夹。
```

这种“只动精确匹配的旧默认值”的迁移策略，比“看到旧字段就覆盖”安全得多。你自己的模组换默认背景时，可以直接抄这个思路。

### 9.4 控制器内部：先轮播，再单图

`MuiConfigurableBackground.refresh()` 的选择顺序值得知道：

```text
有图片列表（文件夹里有图） → 轮播
否则有文件夹但没图         → 纯色兜底
否则有单图                 → 单图（带裁剪和调暗）
否则                       → 纯色兜底 + 异步解码单图
```

另外两个生命周期细节：

- `attach(host, root)` 时重新 `settings.load()`，所以外部改了配置文件，重新打开界面就能看到；
- `detach()` 会停轮播、关图片、清 token，避免后台计时器继续跑。

选图完成后如果图片比例和窗口比例差得不超过 1%，会跳过裁剪直接保存；否则才弹 `MuiCropOverlay`。这是“别让玩家做无意义操作”的细节。

---

## 10. 本地化：为什么到处是 MuiLocalization.get

### 10.1 解析规则

`MuiLocalization.resolve(text)` 的逻辑很简单：

```java
public static @NotNull String resolve(@NotNull CharSequence text) {
    String key = text.toString();
    if (key.isEmpty()) return key;
    try {
        return I18n.exists(key) ? I18n.get(key) : key;   // ← 有翻译用翻译，没有就用原文
    } catch (Throwable ignored) {
        return key;                                       // ← 语言文件坏掉也不崩
    }
}
```

所以所有公开的标签参数都支持两种写法：

```java
// 写法一：直接写人话（适合示例、临时界面、单语言模组）
.toggle("Enable HUD", MyConfig::enabled, MyConfig::setEnabled)

// 写法二：写翻译键（推荐正式模组）
.toggle("mymod.config.enabled", MyConfig::enabled, MyConfig::setEnabled)
```

### 10.2 NarutoOverlay 的两种调用方式

| 场景 | 用法 | 原因 |
|---|---|---|
| 传给 builder / section / 字段 | `MuiLocalization.get(key)` | 提前解析成 `String`，方便复用、拼接 |
| 卡片名 / 字段名这种“可选翻译” | `I18n.exists(key) ? I18n.get(key) : id` | 没有翻译时退回技术 ID，至少还能用 |
| 带参数文案 | `MuiLocalization.get(key, args)` | 对应语言文件里的 `%s` |

卡片名的写法：

```java
private static @NotNull String cardName(@NotNull Card<?> card) {
    String key = GROUP + "card." + card.id().toLowerCase(Locale.ROOT);
    // ← 有翻译用翻译，没有就显示 "FPS" 这种 ID。不会出现 "narutooverlay.config.card.fps" 这种长键。
    return I18n.exists(key) ? I18n.get(key) : card.id();
}
```

### 10.3 什么时候解析？语言切换怎么办？

- 大多数标签在**构建界面时**解析；
- `MuiBackgroundLabels` 在**构造时**解析（record 的紧凑构造器里逐个 `resolve`）；
- 所以切换语言后，已经打开的界面不会自动变。

处理方式：重新打开界面，或调用 `reloadCurrentPage()`。NarutoOverlay 没有做“运行时切语言”的特殊处理，因为 Minecraft 切语言本来就会重建大部分界面。

> **你的模组如果支持运行时切语言**
>
> 在语言加载事件里调用 `host.reloadCurrentPage()`；如果背景文案也要变，记得重建 `MuiBackgroundLabels`（也就是重新 `create` 宿主/屏幕）。

---

## 11. 保存、重建与恢复

### 11.1 NarutoUI 不保存配置，NarutoOverlay 自己保存

这是最重要的一条边界。库的 setter 只改你的状态；保存逻辑完全由调用方决定。NarutoOverlay 的做法是“标脏 + 防抖”：

```java
// 文件：config/ConfigSaver.java（全文）
public final class ConfigSaver {
    private static final long SAVE_DELAY_MS = 400L;   // ← 最后一次改动后 400ms 才写盘

    private static boolean dirty;
    private static long lastChange;

    public static void changed() {
        ConfigVersion.bump();          // ← 配置版本号 +1（用于兼容/迁移判断）
        dirty = true;
        lastChange = Util.getMillis();
    }

    public static void tick() {
        // ← 每个客户端 tick 检查一次：还在 dirty 且已过静默期，就 flush。
        if (dirty && Util.getMillis() - lastChange >= SAVE_DELAY_MS) {
            flush();
        }
    }

    public static void flush() {
        if (!dirty) return;
        dirty = false;
        RenderArgs.CONFIG.save();      // ← 真正的 Forge 配置写盘
    }
}
```

接入点在客户端 tick：

```java
// 文件：config/RenderArgs.java（节选）
public static void tick(TickEvent.@NotNull ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;   // ← 只在 tick 结束阶段做
    // ...
    ConfigSaver.tick();
}
```

为什么用防抖而不是“每次改动立刻写盘”：

- 拖一次滑条会产生几十次 `onProgressChanged`，每次都写盘会卡；
- 400ms 的静默期足以合并连续操作，又不会让玩家觉得“怎么还没保存”；
- 即使游戏崩溃，正常情况下也最多丢一个防抖窗口内的改动。

> **注意**
>
> 这个方案依赖客户端 tick 持续运行。如果玩家在 400ms 内直接杀进程，最后一次改动会丢。要更稳，可以在 `Screen` 关闭时显式 `ConfigSaver.flush()`（NarutoOverlay 目前没这么做，你可以补）。

### 11.2 什么时候必须重建页面

先看库的重建实现：

```java
// 文件：NarutoUI/.../screen/MuiConfigFragment.java（节选）
@Override
public void reloadCurrentPage() {
    if (this.model == null || this.selected < 0 || this.selected >= this.pages.size()) return;

    int index = this.selected;
    // ← 用原始页面定义重新构建一次，替换列表里的旧视图，再显示。
    this.pages.set(index, this.createPage(requireContext(), this.model.pages().get(index)));
    this.showPage(index);
}
```

它做的是“整页重来”，不是“局部刷新”。所以判断标准很简单：

| 变化的东西 | 需要重建吗 | 原因 |
|---|---|---|
| 开关状态、滑条数值 | 不需要 | supplier 每 100ms 同步，或控件自己处理 |
| 数字框里的坐标 | 不需要 | `value(int)` 直接写控件 |
| 卡片摘要文字 | 不需要 | `summary(text)` 只改文本 |
| 卡片启用数、字段启用数 | 不需要（但要刷摘要） | 摘要不是 supplier |
| 主题、强调色、颜色初值 | **需要** | 颜色在构建时解析 |
| 重置单卡 / 全部重置 | **需要** | 开关、chip 的初值都是构建时读的 |
| 背景文件夹 / 裁剪结果 | **需要** | 页面上的文件夹名、图片数、色块都是静态值 |
| 语言切换 | **需要** | 文案在构建时解析 |

### 11.3 主题切换的已知取舍

`ConfigAppearancePage.themes` 在选中主题后调用 `host.onThemeChanged()`，默认实现是 `reloadCurrentPage()`。效果是：

- 当前页（Appearance）重建，控件颜色、主题卡片描边、“In use”标签都更新；
- **左侧 rail 不会重建**，所以 `MuiLogo` 的颜色和 `MuiHintBadge` 的强调色不会立即变，要重新打开界面。

这是当前实现下真实存在的限制，不是文档漏写。如果你的模组在意这点，可以在 `onThemeChanged` 里自己重建 rail（需要超出 `MuiScreenHost` 的能力），或者接受“重新打开后一致”。

### 11.4 恢复：进程内 registry 与 stable recipe

默认 `MuiSettingsScreen.builder(...).create(parent)` 会在进程内注册一个 UUID 模型。`MuiConfigFragment` 有公开无参构造，`onCreate` 时用 `ARG_MODEL` 从 registry 取回模型，因此 FragmentManager 重建时能恢复当前页和滚动位置。

如果你想跨进程重启恢复，可以注册 stable recipe：

```java
MuiSettingsScreen.register("mymod:config", "My Mod", builder -> builder
        .page("General", page -> page
                .section("Rendering", section -> section
                        .toggle("mymod.config.enabled", MyConfig::enabled, MyConfig::setEnabled))));

Screen screen = MuiSettingsScreen.createRegistered("mymod:config", parent);
```

要点：

- 注册必须在客户端初始化时执行；
- recipe 里不能依赖一次性局部变量，所有状态都要能从配置重建；
- registry 不是配置存储，进程重启后必须重新注册。

NarutoOverlay 没走 stable recipe，因为它每次都从 `ConfigUi.create` 现场构建，模型不跨进程保存；这对“模组列表 Config 按钮”场景够用了。

---

## 12. 抄作业：给新模组的最小移植清单

如果你已经看懂上面的例子，下面这套最小实现可以让你在半小时内做出一个“像样”的配置界面。

### 12.1 依赖

```gradle
dependencies {
    implementation "icyllis.modernui:ModernUI-Core:3.12.0"
    modImplementation("icyllis.modernui:ModernUI-Forge:1.20.1-3.12.0.+")
    modImplementation files("libs/narutoui-1.20.1-1.4.0.jar")   // 或 Maven 坐标
}
```

运行时 `mods/` 里要同时有 NarutoUI 和 Modern UI。别把配置界面类放在 common 端初始化路径里——Modern UI 是客户端库。

### 12.2 配置持有类（你的，不是库的）

```java
public final class MyConfig {
    private static boolean enabled = true;
    private static int scale = 100;

    public static boolean enabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; save(); }

    public static int scale() { return scale; }
    public static void setScale(int value) { scale = value; save(); }

    public static void save() {
        // ← 写你的 ForgeConfigSpec / JSON / 其他存储。
        //    库不会替你保存。
    }
}
```

### 12.3 配置界面（组合根）

```java
public final class MyConfigScreen {
    public static Screen create(@Nullable Screen parent) {
        return MuiSettingsScreen.builder("My Mod")
                .subtitle("Client settings")
                // ← 可选：换成你自己的控件工厂 / 流光 profile
                // .widgetFactory(MuiWidgetFactory.shimmer())
                // .shimmerProfile(MuiShimmerProfile.create(new MyShimmerConfig()))
                .background(MuiBackground.scrim())
                .page("General", page -> page
                        .section("Rendering", section -> section
                                .toggle("Enable HUD", "Draw the overlay",
                                        MyConfig::enabled, MyConfig::setEnabled)
                                .slider("Scale", 50, 200,
                                        MyConfig::scale, MyConfig::setScale,
                                        value -> value + "%")))
                .page("About", page -> page
                        .section("Information", section -> section
                                .value("Version", "1.0.0")
                                .text("Thanks for using My Mod.")))
                .create(parent);
    }
}
```

### 12.4 入口（可选依赖安全版）

```java
// 零引用桥：类里只有字符串
public final class MyUiBridge {
    private static final Logger LOGGER = LogManager.getLogger("MyMod");
    private static final String CONFIG_UI = "com.example.mymod.client.MyConfigUi";

    public static void install(FMLJavaModLoadingContext context) {
        try {
            Class.forName(CONFIG_UI)
                    .getMethod("install", FMLJavaModLoadingContext.class)
                    .invoke(null, context);
        } catch (ReflectiveOperationException | LinkageError error) {
            LOGGER.warn("NarutoUI config screen unavailable", error);
        }
    }
}
```

```java
// 真正引用 NarutoUI 的类：只在装了 NarutoUI 时被加载
public final class MyConfigUi {
    public static void install(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, previous) -> MyConfigScreen.create(previous)));
    }
}
```

主类里：

```java
if (ModList.get().isLoaded("narutoui")) {
    MyUiBridge.install(context);
}
```

### 12.5 想要更强：照搬 NarutoOverlay 的哪部分

| 你想要 | 抄这个文件 | 需要额外写的 |
|---|---|---|
| 自己的强调色 | `NarutoShimmerWidgetFactory` + `Accent` | 一个读你主题色的类 |
| 自己的流光参数 | `NarutoShimmerConfig` | 一个 `MuiShimmerConfig` 实现 |
| 可折叠卡片列表 | `ConfigCardsPage` | 你的数据源和每张卡的字段模型 |
| 可配置背景 | `ConfigBackgroundFactory` | `MuiBackgroundSettings` + `MuiBackgroundFiles` |
| 防抖保存 | `ConfigSaver` | 你的 `CONFIG.save()` |
| 自定义行 | `ConfigAppearancePage.addColorPicker` | 你的浮层/控件 |
| 只读信息页 | `ConfigAboutPage` | 你的版本号和路径 |

---

## 13. 避坑清单（都来自真实代码）

下面每一条，都能在上面的例子里找到对应写法。按“症状 → 原因 → 解法”列。

### 13.1 重启后设置丢了

- **症状**：界面里改了，游戏里也生效，重启回到默认。
- **原因**：只写了配置对象，没触发保存。
- **解法**：每个 consumer 里调用你的保存入口（NarutoOverlay 是 `ConfigSaver.changed()`）。库不负责保存。

### 13.2 滑条拖到某个值又跳回去

- **症状**：拖动时数值来回跳。
- **原因**：supplier 读的值和你写回去的值不是同一套（常见于 double 缩放倍数不一致），或者你在 `onProgressChanged` 里又 `setProgress`。
- **解法**：读和写用同一个倍数（第 4.2 节）；不要在 consumer 里手动动滑条。

### 13.3 开关在别处改了，界面不更新

- **症状**：快捷键改了配置，界面里的开关还是旧的。
- **原因**：用了静态初值版 `toggle(title, boolean, consumer)`。
- **解法**：换成 supplier 版 `toggle(title, BooleanSupplier, BooleanConsumer)`。

### 13.4 重置后界面和配置对不上

- **症状**：点了重置，配置已经恢复默认，但开关/chip 还是旧状态。
- **原因**：控件的初值在构建时读取，运行中不会自己重读。
- **解法**：`host.reloadCurrentPage()`（NarutoOverlay 的 `resetAll` / `resetCard` 都这么做）。

### 13.5 主题换了，颜色没变

- **症状**：主题卡片选中状态变了，但控件还是旧强调色。
- **原因**：颜色在构建页面时解析进控件。
- **解法**：`host.onThemeChanged()` 或 `reloadCurrentPage()`。注意 rail 不在重建范围内（第 11.3 节）。

### 13.6 浮层里的控件没有流光 / 颜色不对

- **症状**：主界面有流光，弹出的颜色选择器没有。
- **原因**：浮层在页面构建之外创建，不会自动继承 profile/factory。
- **解法**：`MuiShimmerProfile.withCurrent(profile, () -> MuiControls.withFactory(factory, () -> new Overlay(...)))`。

### 13.7 在页面构建时调用 host 炸了

- **症状**：打开界面时 `IllegalStateException: Config screen is not attached`。
- **原因**：页面回调执行时 fragment 还没 attach（第 3.2 节）。
- **解法**：构建阶段只用 `page.context()` / `page.theme()`；host 的方法只在用户操作回调里调用。

### 13.8 界面里多了一层卡片

- **症状**：section 里又出现圆角卡片，边框叠边框。
- **原因**：在 `section(...)` 里又加了 `MuiLayout.card(...)`，或者给 `MuiThemePicker.add` 外面又包了 `page.section(...)`。
- **解法**：section 已经是卡片；`MuiThemePicker.add` 自带 section（第 5.1 节）。

### 13.9 自定义行宽度不对

- **症状**：`customLayout(row -> ...)` 里的控件只占一半，或者没有填满。
- **原因**：`customLayout` 给你的是一个空的 `MuiLayout`，默认布局参数不一定是你想要的。
- **解法**：自己 `add(view, MuiControls.fill() / weight() / wrap())`。

### 13.10 可选依赖没装时启动崩溃

- **症状**：没装 NarutoUI 的客户端启动报 `NoClassDefFoundError`。
- **原因**：入口类直接引用了 NarutoUI 类型。
- **解法**：反射桥隔离（第 2.1 节）；`LinkageError` 也要 catch。

### 13.11 页面加了一页，`initialPage` 打开越界

- **症状**：从某个入口打开，直接白屏/异常。
- **原因**：`ConfigUi.create` 里 `Math.min(2, page)` 是写死的页数上限。
- **解法**：加页时同步改这个上限；更稳妥的是用一个常量（如 `PAGE_COUNT`）维护页数，所有入口只传 `0..PAGE_COUNT-1`。`MuiSettingsScreen` 没有公开页数，别指望它替你校验。

### 13.12 切语言后界面还是旧文案

- **症状**：语言改了，配置界面文字没变。
- **原因**：标签在构建时解析。
- **解法**：重新打开界面，或 `reloadCurrentPage()`；背景标签需要重新构造（第 10.3 节）。

### 13.13 卡片摘要不更新

- **症状**：切换字段开关后，标题栏的 “3 / 4 字段” 没变。
- **原因**：摘要不是 supplier，只是普通文本。
- **解法**：改完状态后调用 `card.summary(newText)`（NarutoOverlay 的 `refreshSummary`）。

### 13.14 页面很多时打开很慢

- **症状**：第一次打开要卡一下。
- **原因**：所有页面在 `onCreateView` 时一次性构建（库的设计）。`customPage` 也一样，它的工厂是在建页时立即调用的，**不能用来延迟构建**。
- **解法**：页面多时考虑拆成多个 screen，或者把重 IO 移出页面构建（例如先显示占位、异步加载完再 `reloadCurrentPage()`）。

---

## 14. 对照表与结语

### 14.1 NarutoOverlay 写法 → NarutoUI API

| NarutoOverlay 里的东西 | 实际调用的 NarutoUI API | 说明 |
|---|---|---|
| `MuiSettingsScreen.builder(...)` | `screen/MuiSettingsScreen` | 高层入口 |
| `.widgetFactory(new NarutoShimmerWidgetFactory())` | `theme/MuiWidgetFactory` | 每屏控件实现 |
| `.shimmerProfile(MuiShimmerProfile.create(...))` | `shimmer/MuiShimmerProfile` | 每屏流光参数 |
| `.background(host.background())` | `background/MuiConfigurableBackground` | 可配置背景 |
| `.railHeader(ConfigUi::logo)` | `widget/MuiLogo` | 导航头部 |
| `.railFooter(ConfigUi::hint)` | `widget/MuiHintBadge` | 导航底部 |
| `.page(nav, title, subtitle, body)` | `screen/MuiSettingsScreen.Builder` | 页面定义 |
| `section.toggle(...)` | `screen/MuiSettingsSection#toggle` | 开关行 |
| `section.slider(...)` | `screen/MuiSettingsSection#slider` | 滑条行 |
| `section.value(...)` | `screen/MuiSettingsSection#value` | 只读值行 |
| `section.customLayout(row -> ...)` | `screen/MuiSettingsSection#customLayout` | 自定义行 |
| `MuiThemePicker.add(...)` | `widget/MuiThemePicker` | 主题卡片选择器 |
| `MuiColorField` + `MuiColorPickerOverlay` | `widget/MuiColorField`、`overlay/MuiColorPickerOverlay` | 颜色字段与浮层 |
| `MuiCollapsibleCard.card(...)` | `widget/MuiCollapsibleCard` | 可折叠卡片 |
| `MuiNumberField` / `MuiChip` / `MuiTextAction` | `widget/*` | 通用控件 |
| `MuiDescriptionRow` | `widget/MuiDescriptionRow` | 说明行 |
| `MuiLayout.row/column` | `widget/MuiLayout` | 链式布局 |
| `MuiControls.fill/wrap/weight/dp` | `widget/MuiControls` | 布局参数与单位 |
| `MuiLocalization.get(...)` | `util/MuiLocalization` | 翻译 |
| `host.showOverlay(...)` / `reloadCurrentPage()` | `screen/MuiScreenHost` | 屏幕服务 |
| `MuiShimmer.refresh()` | `shimmer/MuiShimmer` | 重新判断是否动画 |

### 14.2 你读完应该能做到

- [ ] 说出 Screen / Page / Section / Host / Profile / Factory 各自负责什么；
- [ ] 用 `MuiSettingsScreen.builder` 搭一个三页配置界面；
- [ ] 用 supplier 版 toggle/slider 让界面跟外部状态同步；
- [ ] 用 `custom` / `customLayout` 放一个自定义行；
- [ ] 用 `MuiCollapsibleCard` + `MuiNumberField` + `MuiChip` 做一个数据驱动列表；
- [ ] 用 `MuiThemePicker.Option` 接一个主题选择器；
- [ ] 用 `MuiColorField` + `MuiColorPickerOverlay` 做颜色选择；
- [ ] 用 `MuiConfigurableBackground` 接背景，并写一个 `MuiBackgroundSettings`；
- [ ] 写出自己的 `MuiWidgetFactory` / `MuiShimmerConfig` 适配器；
- [ ] 知道什么时候必须 `reloadCurrentPage()`；
- [ ] 知道为什么保存逻辑必须自己写。

### 14.3 最后一句

NarutoUI 的 API 不难，难的是“边界”：

- **界面归库，状态归你**；
- **构建时读初值，运行中靠 supplier 或重建**；
- **浮层和延迟创建的东西，要手动继承 profile/factory**；
- **可选依赖要用反射桥隔离**。

把 NarutoOverlay 这几个文件当成模板改，比从零读 API 快得多。等你改完第一版，再回头读 [API-TUTOR.md](API-TUTOR.md)，会发现那些参数名突然都“有画面”了。

---

## 参考

- NarutoOverlay 源码：[`../../NarutoOverlay/src/main/java/me/kall/narutooverlay/config/ui`](../NarutoOverlay/src/main/java/me/kall/narutooverlay/config/ui/)
- NarutoUI 源码：[`../src/main/java/me/kall/narutoui/mui`](src/main/java/me/kall/narutoui/mui/)
- 入门教程：[EASY_START.md](EASY_START.md)
- API 参考：[API-TUTOR.md](API-TUTOR.md)
- 变更记录：[CHANGELOG.md](../CHANGELOG.md)