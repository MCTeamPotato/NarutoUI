package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.screen.MuiSettingsPage;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Generic theme-card selector. The caller supplies title, subtitle and two accent colors per
 * option; the API builds the tiles, selected outline and click handling.
 */
public final class MuiThemePicker {
    private MuiThemePicker() {}

    /**
     * One selectable theme card.
     */
    public interface Option {
        /**
         * Returns the visible theme name.
         *
         * @return the theme title
         */
        @NotNull CharSequence title();

        /**
         * Returns an optional secondary line.
         *
         * @return the subtitle, or {@code null}
         */
        default @Nullable CharSequence subtitle() {
            return null;
        }

        /**
         * Returns whether this option is currently selected.
         *
         * @return the selected state
         */
        boolean selected();

        /**
         * Applies this option. The picker calls this before {@code changed}.
         */
        void select();

        /**
         * Returns the accent color used in the preview.
         *
         * @return an ARGB accent color
         */
        int accent();

        /**
         * Returns the surface color used in the preview.
         *
         * @return an ARGB surface color
         */
        int surface();

        /**
         * Builds optional rich preview content. Return {@code null} to use the default
         * accent/surface swatch.
         *
         * @param context the screen context
         * @param theme   active API theme
         * @return the preview view, or {@code null}
         */
        default @Nullable View preview(@NotNull Context context, @NotNull MuiTheme theme) {
            return null;
        }
    }

    /**
     * Adds a two-column theme picker using the default {@code "In use"} label.
     *
     * @param page         page builder
     * @param sectionTitle section heading
     * @param options      selectable options
     * @param changed      callback invoked after an option is selected
     * @return the page, for chaining
     */
    public static @NotNull MuiSettingsPage add(@NotNull MuiSettingsPage page, @NotNull CharSequence sectionTitle, @NotNull List<? extends Option> options, @NotNull Runnable changed) {
        return add(page, sectionTitle, options, "In use", changed);
    }

    /**
     * Adds a two-column theme picker.
     *
     * @param page         page builder
     * @param sectionTitle section heading
     * @param options      selectable options
     * @param inUse        label shown on the selected option
     * @param changed      callback invoked after an option is selected
     * @return the page, for chaining
     */
    public static @NotNull MuiSettingsPage add(@NotNull MuiSettingsPage page, @NotNull CharSequence sectionTitle, @NotNull List<? extends Option> options, @NotNull CharSequence inUse, @NotNull Runnable changed) {
        page.section(sectionTitle, section -> section.custom(create(section.page().context(), section.page().theme(), options, inUse, changed)));
        return page;
    }

    /**
     * Builds a two-column theme picker as a standalone layout.
     * <p>
     * Use this overload when the picker should be embedded in an existing section instead of
     * owning a section of its own. The returned layout can be passed to
     * {@link me.kall.narutoui.mui.screen.MuiSettingsSection#custom(icyllis.modernui.view.View)}.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options selectable options
     * @param changed callback invoked after an option is selected
     * @return the picker layout
     */
    public static @NotNull MuiLayout create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<? extends Option> options, @NotNull Runnable changed) {
        return create(context, theme, options, "In use", changed);
    }

    /**
     * Builds a two-column theme picker as a standalone layout.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options selectable options
     * @param inUse   label shown on the selected option
     * @param changed callback invoked after an option is selected
     * @return the picker layout
     */
    public static @NotNull MuiLayout create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<? extends Option> options, @NotNull CharSequence inUse, @NotNull Runnable changed) {
        MuiLayout column = MuiLayout.column(context, theme);

        for (int i = 0; i < options.size(); i += 2) {
            MuiLayout row = MuiLayout.row(context, theme).padding(0);

            Option left = options.get(i);
            row.add(tile(context, theme, left, inUse, changed), tileParams(context, i + 1 < options.size()));

            if (i + 1 < options.size()) {
                row.add(tile(context, theme, options.get(i + 1), inUse, changed), tileParams(context, false));
            }

            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = i == 0 ? 0 : MuiControls.dp(context, 8);
            column.add(row, params);
        }

        return column;
    }

    /**
     * Builds one selectable theme tile.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param option  option model
     * @param inUse   selected label
     * @param changed selection callback
     * @return the tile view
     */
    private static @NotNull View tile(@NotNull Context context, @NotNull MuiTheme theme, @NotNull Option option, @NotNull CharSequence inUse, @NotNull Runnable changed) {
        LinearLayout tile = MuiControls.column(context);
        tile.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 10), MuiControls.dp(context, 10), MuiControls.dp(context, 10));

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, theme.cornerRadius()));
        background.setColor(theme.surfaceHigh());
        background.setStroke(Math.max(1, MuiControls.dp(context, 1)), option.selected() ? theme.primary() : theme.outline());
        tile.setBackground(background);

        LinearLayout previewBox = MuiControls.column(context);

        View customPreview = option.preview(context, theme);
        if (customPreview != null) {
            previewBox.addView(customPreview, MuiControls.fill());
        } else {
            View accent = new View(context);
            ShapeDrawable accentBackground = new ShapeDrawable();
            accentBackground.setCornerRadius(MuiControls.dp(context, 3));
            accentBackground.setColor(option.accent());
            accent.setBackground(accentBackground);
            previewBox.addView(accent, new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.dp(context, 8)));

            View body = new View(context);
            ShapeDrawable bodyBackground = new ShapeDrawable();
            bodyBackground.setCornerRadius(MuiControls.dp(context, 5));
            bodyBackground.setColor(option.surface());
            body.setBackground(bodyBackground);
            LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.dp(context, 42));
            bodyParams.topMargin = MuiControls.dp(context, 4);
            previewBox.addView(body, bodyParams);
        }

        tile.addView(previewBox, MuiControls.fill());

        TextView title = MuiControls.text(context, theme, option.title(), 13, option.selected() ? theme.primary() : theme.onSurface());
        LinearLayout.LayoutParams titleParams = MuiControls.fill();
        titleParams.topMargin = MuiControls.dp(context, 8);
        tile.addView(title, titleParams);

        CharSequence subtitle = option.subtitle();
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = MuiControls.text(context, theme, subtitle, 10, theme.onSurfaceVariant());
            tile.addView(subtitleView, MuiControls.fill());
        }

        if (option.selected()) {
            TextView current = MuiControls.text(context, theme, inUse, 10, theme.primary());
            current.setGravity(Gravity.END);
            tile.addView(current, MuiControls.fill());
        }

        tile.setOnClickListener(view -> {
            if (option.selected()) return;
            option.select();
            changed.run();
        });
        return tile;
    }

    /**
     * Returns equal-width layout params for one tile.
     *
     * @param context     the screen context
     * @param trailingGap whether to leave a right gap
     * @return tile layout params
     */
    private static @NotNull LinearLayout.LayoutParams tileParams(@NotNull Context context, boolean trailingGap) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F);
        if (trailingGap) params.setMargins(0, 0, MuiControls.dp(context, 8), 0);
        return params;
    }
}