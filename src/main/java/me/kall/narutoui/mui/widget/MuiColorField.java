package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A compact color field: rounded swatch, label, and a pick action.
 * <p>
 * The field only renders and exposes the chosen color; showing a picker overlay and persisting the
 * result stays with the caller. This keeps the component usable outside the configuration shell.
 */
public final class MuiColorField extends LinearLayout {
    /** Color preview square. */
    private final View swatch;
    /** Pick button. */
    private final Button pick;

    /**
     * Creates a color field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   field label or translation key
     * @param color   initial ARGB color
     */
    public MuiColorField(@NotNull Context context, @NotNull MuiTheme theme,
                         @NotNull CharSequence label, int color) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        this.swatch = new View(context);
        setSwatchColor(context, color);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(
                MuiControls.dp(context, 26), MuiControls.dp(context, 26));
        swatchParams.setMargins(0, 0, MuiControls.dp(context, 10), 0);
        addView(this.swatch, swatchParams);

        addView(MuiControls.text(context, theme, label, 13, theme.onSurface()), MuiControls.weight());

        this.pick = MuiControls.button(context, theme, "Pick color", true, view -> {});
        addView(this.pick, MuiControls.wrap());
    }

    /**
     * Creates a color field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   field label or translation key
     * @param color   initial ARGB color
     * @return the field
     */
    public static @NotNull MuiColorField create(@NotNull Context context, @NotNull MuiTheme theme,
                                                @NotNull CharSequence label, int color) {
        return new MuiColorField(context, theme, label, color);
    }

    /**
     * Updates the preview color.
     *
     * @param color the new ARGB color
     * @return this field, for chaining
     */
    public @NotNull MuiColorField color(int color) {
        setSwatchColor(getContext(), color);
        return this;
    }

    /**
     * Updates the pick button label.
     *
     * @param label label or translation key
     * @return this field, for chaining
     */
    public @NotNull MuiColorField buttonLabel(@NotNull CharSequence label) {
        this.pick.setText(MuiLocalization.resolve(label));
        return this;
    }

    /**
     * Sets the pick callback.
     *
     * @param action pick action
     * @return this field, for chaining
     */
    public @NotNull MuiColorField onPick(@NotNull Runnable action) {
        this.pick.setOnClickListener(view -> action.run());
        return this;
    }

    /**
     * Applies the rounded swatch background.
     *
     * @param context the screen context
     * @param color   the ARGB color
     */
    private void setSwatchColor(@NotNull Context context, int color) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setCornerRadius(MuiControls.dp(context, 6));
        drawable.setColor(color);
        drawable.setStroke(Math.max(1, MuiControls.dp(context, 1)), 0x40FFFFFF);
        this.swatch.setBackground(drawable);
    }
}