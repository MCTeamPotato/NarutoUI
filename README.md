# NarutoUI

NarutoUI 是一个面向 Minecraft 客户端模组的 **Modern UI 配置界面库**。

它不规定你的配置内容，而是提供一套可直接使用的界面骨架：导航栏、页面、section 卡片、常用字段、主题、背景、本地化、流光控件、文件选择、颜色选择和图片裁剪。接入方只需要声明“有哪些页面、每个页面有哪些字段”，不用重复写布局和控件代码。

> 想按需求找用法？读 [API 教程](docs/API-TUTOR.md)。完整 public API 见 javadoc。

## 提供什么

- 声明式页面 / section / 字段 DSL
- 可链式构建的 `MuiLayout` 与 `MuiControls`
- 常用控件：开关、整数/小数滑杆、文本输入、整数/长整数/小数输入、下拉框、卡片选择、列表编辑、按钮、只读值
- 外壳能力：固定底部 action bar、关闭守卫（未保存确认）、可滚动导航栏、按需构建页面、`MuiPageContext#searchable` 过滤原语
- 页面切换过渡：默认淡入淡出，另有上滑 / 下滑 / 按页序自动；`pageTransitionDuration(0)` 可关闭，时长可调
- 输入校验：数值字段支持外部 `Predicate<BigDecimal>`，列表编辑器支持逐项 `Predicate<String>`，`MuiValidationHint` 显示错误文案
- 通用组件：`MuiChip`、`MuiTextAction`、`MuiLogo`、`MuiHintBadge`、`MuiValidationHint`、`MuiColorField`、`MuiColorBandField`、`MuiColorBandPreview`、`MuiDescriptionRow`、`MuiCollapsibleCard`
- 双色带取色：`MuiColorBand` / `MuiColorBands` 用 OKLab 在多色标之间生成平滑色带
- 可配置主题：强调色、背景色、圆角、间距、内容宽度
- 背景：纯色、透明、黑色、单图、裁剪图、幻灯片
- 实例级 shimmer：`MuiShimmerable` + `shimmerNext()`，按行 / 页面 / 导航栏 / 标题开启；输入框和 `MuiTextAction` 也可参与
- 文件/文件夹选择、颜色选择、图片裁剪
- 每屏独立的 `MuiWidgetFactory` 与 `MuiShimmerProfile`
- 进程内页面恢复与 stable recipe 注册
- 可单独构建的 `commonJar`，方便其他模组 shadow

NarutoUI 是界面库，**不负责配置持久化**。字段回调只修改你的配置对象；保存、草稿、Apply / Revert 都由调用方实现。

## 文档

- [API 教程](docs/API-TUTOR.md)：按“我想做什么”组织
- javadoc：`gradlew javadoc` 后查看 `build/docs/javadoc/index.html`
- 包结构与能力总览：见下方目录

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
- 仍需要 Minecraft client 和 Modern UI；其中 screen / background / file picker 使用 `icyllis.modernui.mc.MuiModApi`
- 已 remap，和当前 Forge/NeoForge + Minecraft 版本绑定

不要把 `commonJar` 当独立 mod 加载；也不要同时依赖完整 NarutoUI 和 common jar，否则会重复类。

## 设计边界

- 页面在第一次切换到时构建，之后保留；页面 body 在首次打开时执行，不要依赖它在 screen 创建时产生副作用。需要重建当前页时使用 `MuiScreenHost.reloadCurrentPage()`。
- `toggle` / `slider` / `value` 的 supplier 每 100 ms 轮询一次，不是事件推送。
- 文本输入保持焦点时暂停 supplier 轮询；`textField` 只在 Enter 或失焦时提交。
- `MuiDropdown` 是页面内展开面板，不是弹窗；选项很多时建议改用独立搜索页面。
- `MuiWidgetFactory` 的方法全部是 abstract；自定义工厂需要完整实现，内置 `modern()` / `shimmer()` 已提供完整实现。
- `MuiListEditor` 没有虚拟化或内部滚动；很长的列表建议使用独立页面。
- 数值滑杆最多映射 100,000 个进度步；更大范围由数值输入框补足精度。
- stable recipe 不是配置持久化；进程重启后需要在客户端初始化时重新注册。

## 目录

```text
src/main/java/me/kall/narutoui/mui/
├── screen/       入口、Fragment、页面、section、MuiScreenHost
├── layout/       链式布局 MuiLayout 与 MuiControls 门面
├── field/        文本 / 数字 / 列表输入控件
├── choice/       MuiOption、dropdown、choice grid、枚举选项、主题选择
├── color/        OKLab 双色带、色带值对象
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

## License

见 [LICENSE](LICENSE)。