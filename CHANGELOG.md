# 1.0.0
- Initial release.
# 1.1.0
- Allow settings screens to render one page without a navigation rail 
- Allow to place multi-page navigation in a horizontal, scrollable top bar.
# 1.2.0
- Provide shimmer-enabled text, switch, slider, and button controls through `MuiWidgetFactory.shimmer()`. 
- Add `MuiTheme.withOnPrimary()` for customizing filled-button text color and document the new factory.
# 1.3.0
- Add a configurable background controller with image and folder pickers, slideshow settings, crop support, and pluggable persistence.
- Expose screen-host services for overlays and page refreshes.
# 1.4.0
- Huge refactor and re-design. 
# 1.4.1
- Add page-wide shimmer defaults for headers and content, plus supplier-backed sliders with descriptions. 
- Update chip theming and separate the logo glyph from its shimmering background
# 1.5.0
- Add fixed action bars, guarded close handling, lazy page construction, scrollable navigation, and searchable rows.
- Add field validation for text, numbers, and list entries, with validation hints, and document the new APIs.
# 1.5.1
- Add dialogs, collapsible sections, tooltips, dynamic navigation sizing, and runtime row enablement. 
- Parse legacy text formatting and improve screen lifecycle, resize handling, and list-editor commits.
# 1.6.0
- Add description-free overloads for section number, long number, decimal, dropdown, choice grid, list and text field rows, mirrored in `MuiControls` and `MuiPageContext`.
- Add three-argument page overloads and navigation-label based initial-page selection to the high-level and low-level screen builders.
- Add default-marker checkbox, radio button and progress bar factories, a filled button overload, fluent padding, margin, text and heading helpers, and decimal/long value rows.
- Add expanded-by-default collapsible section helpers, default image/slideshow background factories, shorter file-picker calls, and a decimal number field factory without an explicit scale. 1.7.
# 1.7.0
- Add page-switch transitions through `MuiPageTransition` (`FADE` default, `SLIDE_UP`, `SLIDE_DOWN`, `SLIDE_AUTO`) and the matching `pageTransition(...)` / `pageTransitionDuration(...)` builder methods on both screen builders. Use `pageTransitionDuration(0)` for an instant switch.
- Skip transitions on the initial page, page reloads and resize rebuilds, and cancel a running transition when another switch starts or the view is destroyed.
# 1.7.1
- Add `MuiColorBand` and `MuiColorBands`, a public OKLab two-color band API with a nine-stop mirrored ramp.
- Add `MuiColorBandField` and `MuiColorBandPreview` widgets: live ramp preview, two color fields, picker hooks and per-field labels.
- Document the band API in the API tutorial.
# 1.7.2
- Add `themeProvider(Function<Context, MuiTheme>)` to both screen builders for context-aware, live themes.
- Add `MuiConfigFragment.reloadTheme()`: rebuild the rail, navigation highlight, headings and widgets while keeping the selected page.
# 1.7.3
- Add `MuiShimmerRamps`, a public light/core/deep nine-stop ramp generator with sampling.
- Add `MuiColorBandField.rampProvider(...)` so core/deep band fields can preview a richer ramp than the default two-color band.
# 1.8.0
- Add `MuiColorScheme`, `MuiThemePalette`, `MuiHudPalette` and `MuiColorSchemes`, a shared catalog of UI + HUD color schemes with a registry.
- Add built-in Naruto, Classic, Jade, Azure, Sakura and Graphite schemes with translated names.
- A scheme registered by NarutoUI or another mod automatically appears in every consumer that iterates the catalog.
