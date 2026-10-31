package me.kall.narutoui.mui.widget;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
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
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Color preview square. */
    private final View swatch;
    /** Field label. */
    private final TextView labelView;
    /** Pick button. */
    private final Button pick;
    /** Current ARGB color. */
    private int color;

    /**
     * Creates a color field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   field label or translation key
     * @param color   initial ARGB color
     */
    public MuiColorField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, int color) {
        super(context);
        this.theme = theme;
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);

        this.color = color;
        this.swatch = new View(context);
        this.setSwatchColor(context, color);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(MuiControls.dp(context, 26), MuiControls.dp(context, 26));
        swatchParams.setMargins(0, 0, MuiControls.dp(context, 10), 0);
        this.addView(this.swatch, swatchParams);

        this.labelView = MuiControls.text(context, theme, label, 13, theme.onSurface());
        this.addView(this.labelView, MuiControls.weight());

        this.pick = MuiControls.button(context, theme, "Pick color", true, view -> {});
        this.addView(this.pick, MuiControls.wrap());
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
    public static @NotNull MuiColorField create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, int color) {
        return new MuiColorField(context, theme, label, color);
    }

    /**
     * Enables or disables the field. A disabled field dims its label and swatch and disables the
     * pick button.
     *
     * @param enabled {@code false} to disable
     * @return this field, for chaining
     */
    public @NotNull MuiColorField enabled(boolean enabled) {
        this.setEnabled(enabled);
        this.pick.setEnabled(enabled);
        this.labelView.setTextColor(enabled ? this.theme.onSurface() : MuiControls.alpha(this.theme.onSurface(), 0.38F));
        this.setSwatchColor(this.getContext(), this.color);
        return this;
    }

    /**
     * Updates the preview color.
     *
     * @param color the new ARGB color
     * @return this field, for chaining
     */
    public @NotNull MuiColorField color(int color) {
        this.color = color;
        this.setSwatchColor(this.getContext(), color);
        return this;
    }

    /**
     * Returns the current preview color.
     *
     * @return the ARGB color
     */
    public int color() {
        return this.color;
    }

    /**
     * Replaces the field label.
     *
     * @param label label or translation key
     * @return this field, for chaining
     */
    public @NotNull MuiColorField label(@NotNull CharSequence label) {
        this.labelView.setText(MuiLocalization.resolve(label));
        return this;
    }

    /**
     * Returns the current field label.
     *
     * @return the label
     */
    public @NotNull CharSequence label() {
        return this.labelView.getText();
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
        drawable.setColor(this.isEnabled() ? color : MuiControls.alpha(color, 0.38F));
        drawable.setStroke(Math.max(1, MuiControls.dp(context, 1)), 0x40FFFFFF);
        this.swatch.setBackground(drawable);
    }
}