package me.kall.narutoui.mui.choice;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Preset that turns a set of candidate themes into a {@link MuiChoiceGrid} of preview cards.
 * <p>
 * The picker no longer owns selection state or a {@code select} callback: the returned grid is a
 * normal value control, so callers can bind it to a supplier or a plain callback.
 * Each choice carries its own theme, so the preview uses the candidate's colors instead of the
 * active theme's colors.
 */
public final class MuiThemePicker {
    private MuiThemePicker() {}

    /**
     * One candidate theme and its card text.
     *
     * @param theme    the candidate theme
     * @param title    visible theme name or translation key
     * @param subtitle optional secondary line
     * @param preview  optional custom preview; {@code null} uses the default swatch
     */
    public record Choice(@NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence subtitle, @Nullable MuiOption.Preview preview) {
        /**
         * Creates a choice with the default swatch.
         *
         * @param theme the candidate theme
         * @param title visible theme name or translation key
         * @return the choice
         */
        public static @NotNull Choice of(@NotNull MuiTheme theme, @NotNull CharSequence title) {
            return new Choice(theme, title, null, null);
        }

        /**
         * Creates a choice with a subtitle and the default swatch.
         *
         * @param theme    the candidate theme
         * @param title    visible theme name or translation key
         * @param subtitle optional secondary line
         * @return the choice
         */
        public static @NotNull Choice of(@NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence subtitle) {
            return new Choice(theme, title, subtitle, null);
        }

        /**
         * Returns a copy with a custom preview.
         *
         * @param preview preview factory
         * @return the new choice
         */
        public @NotNull Choice preview(@NotNull MuiOption.Preview preview) {
            return new Choice(this.theme, this.title, this.subtitle, preview);
        }
    }

    /**
     * Builds a theme picker with the default {@code "In use"} label.
     *
     * @param context the screen context
     * @param active  currently active theme
     * @param choices candidate themes
     * @return a stateful choice grid
     */
    public static @NotNull MuiChoiceGrid<Choice> create(@NotNull Context context, @NotNull MuiTheme active, @NotNull List<Choice> choices) {
        return create(context, active, choices, "In use");
    }

    /**
     * Builds a theme picker.
     *
     * @param context the screen context
     * @param active  currently active theme
     * @param choices candidate themes
     * @param inUse   label shown on the selected card
     * @return a stateful choice grid
     */
    public static @NotNull MuiChoiceGrid<Choice> create(@NotNull Context context, @NotNull MuiTheme active, @NotNull List<Choice> choices, @NotNull CharSequence inUse) {
        List<MuiOption<Choice>> options = new ArrayList<>(choices.size());
        Choice selected = null;

        for (Choice choice : choices) {
            MuiOption.Preview preview = choice.preview() != null ? choice.preview() : (previewContext, ignored) -> swatch(previewContext, choice.theme());
            options.add(new MuiOption<>(choice, choice.title(), choice.subtitle(), true, null, preview));
            if (selected == null && choice.theme().equals(active)) selected = choice;
        }

        return MuiChoiceGrid.create(context, active, options).selected(selected).selectedLabel(inUse);
    }

    /**
     * Builds the default theme swatch: an accent bar above a surface block.
     *
     * @param context the screen context
     * @param candidate the previewed theme
     * @return the preview view
     */
    private static @NotNull View swatch(@NotNull Context context, @NotNull MuiTheme candidate) {
        LinearLayout preview = MuiControls.column(context);

        View accent = new View(context);
        ShapeDrawable accentBackground = new ShapeDrawable();
        accentBackground.setCornerRadius(MuiControls.dp(context, 3));
        accentBackground.setColor(candidate.primary());
        accent.setBackground(accentBackground);
        preview.addView(accent, new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.dp(context, 8)));

        View body = new View(context);
        ShapeDrawable bodyBackground = new ShapeDrawable();
        bodyBackground.setCornerRadius(MuiControls.dp(context, 5));
        bodyBackground.setColor(candidate.surface());
        body.setBackground(bodyBackground);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.dp(context, 42));
        bodyParams.topMargin = MuiControls.dp(context, 4);
        preview.addView(body, bodyParams);
        return preview;
    }
}