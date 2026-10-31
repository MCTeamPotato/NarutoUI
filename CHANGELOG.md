# Unreleased
- Make `MuiCollapsibleCard.expanded(...)` / `setExpanded(...)` chainable and add `expanded(boolean, boolean animate)`.
- Add `MuiThemePicker.create(...)` returning a standalone `MuiLayout`; `MuiThemePicker.add(...)` now returns the page.
- Add the missing `MuiTheme.with*` methods for surface, text, outline and error tokens.
- Add chainable text updates: `MuiChip.label`, `MuiHintBadge.text`, `MuiColorField.label`, `MuiLogo.glyph`, `MuiDescriptionRow.title` / `description`.
- Add `enabled(boolean)` to `MuiChip`, `MuiTextAction`, `MuiNumberField` and `MuiColorField`, including dimmed disabled states.
- Make shimmer control bind methods chainable; add `ShimmerButton(Context)`.
- Add `MuiSettingsPage.customLayout(...)`; remove `MuiSettingsSection.custom(Consumer<LinearLayout>)` in favor of `customLayout`.
- Change `MuiControls.card(...)` to return `MuiLayout`.

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
- Expose screen-host services for overlays and page refreshes, and document the new API.
# 1.4.0
- Add `MuiLayout`, a fluent `LinearLayout` subclass with chainable orientation, padding, margins, spacing, and content helpers.
- Expose fluent layouts through `MuiPageContext.contentLayout()` / `sectionLayout()` and `MuiSettingsSection.cardLayout()` / `customLayout()`.
- Add `MuiCollapsibleCard`, a reusable card with a clickable `+`/`-` header and animated expand/collapse body.
- Add `MuiNumberField`, `MuiChip`, `MuiTextAction`, `MuiLogo`, `MuiHintBadge`, `MuiColorField` and `MuiDescriptionRow` as general chainable components.
- Add `MuiCollapsibleCard.body(Consumer<MuiLayout>)` for fluent body composition.
- Remove `MuiCardEditor`; compose the generic components directly instead.
- Expand `MuiScreenHost` with a default `onThemeChanged()` and document it as the single host contract for pages, overlays and shell collaborators.
- Split the flat `me.kall.narutoui.mui` package into `screen`, `widget`, `theme`, `background`, `shimmer`, `overlay` and `util`; entry points such as `MuiSettingsScreen` now live in `screen`.