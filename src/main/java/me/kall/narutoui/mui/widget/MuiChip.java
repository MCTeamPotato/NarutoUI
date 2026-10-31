package me.kall.narutoui.mui.widget;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.TextView;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.drawable.ShimmerCircleDrawable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A small toggleable chip. The fill is a shimmer target: selected chips use the theme accent,
 * unselected chips use a light neutral, and the label stays black so it remains readable while the
 * background wave animates.
 */
public final class MuiChip extends TextView implements MuiShimmerContainer {
    /** Black label color. */
    private static final int LABEL_COLOR = 0xFF000000;

    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Shimmering fill. */
    private final ShimmerCircleDrawable fill;
    /** Current selected state. */
    private boolean checked;
    /** Optional selection listener. */
    private @Nullable BooleanConsumer onChanged;
    /** Marker applied to the fill. */
    private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

    /**
     * Creates a chip.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    chip label or translation key
     * @param checked  initial selected state
     */
    public MuiChip(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean checked) {
        super(context);
        this.theme = theme;
        this.checked = checked;

        this.setText(MuiLocalization.resolve(label));
        this.setTextSize(11);
        this.setTextColor(LABEL_COLOR);
        this.setGravity(Gravity.CENTER);
        this.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 4), MuiControls.dp(context, 10), MuiControls.dp(context, 4));

        this.fill = new ShimmerCircleDrawable(this, this.fillColor());
        this.fill.setCornerRadius(MuiControls.dp(context, 14));
        this.setBackground(this.fill);

        this.setOnClickListener(view -> {
            if (!this.isEnabled()) return;
            this.checked = !this.checked;
            this.applyStyle();
            if (this.onChanged != null) this.onChanged.accept(this.checked);
        });
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.shimmerable = marker;
        this.fill.setShimmerable(marker);
        MuiShimmer.refresh();
    }

    /**
     * Creates a chip.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    chip label or translation key
     * @param checked  initial selected state
     * @return the chip
     */
    public static @NotNull MuiChip create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean checked) {
        return new MuiChip(context, theme, label, checked);
    }

    /**
     * Enables or disables the chip. A disabled chip keeps its layout but dims its fill and label.
     *
     * @param enabled {@code false} to disable
     * @return this chip, for chaining
     */
    public @NotNull MuiChip enabled(boolean enabled) {
        this.setEnabled(enabled);
        this.applyStyle();
        return this;
    }

    /**
     * Replaces the chip label.
     *
     * @param label label or translation key
     * @return this chip, for chaining
     */
    public @NotNull MuiChip label(@NotNull CharSequence label) {
        this.setText(MuiLocalization.resolve(label));
        return this;
    }

    /**
     * Returns the current chip label.
     *
     * @return the label
     */
    public @NotNull CharSequence label() {
        return this.getText();
    }

    /**
     * Updates the selected state without firing the listener.
     *
     * @param checked the new state
     * @return this chip, for chaining
     */
    public @NotNull MuiChip checked(boolean checked) {
        this.checked = checked;
        this.applyStyle();
        return this;
    }

    /**
     * Returns whether the chip is selected.
     *
     * @return the selected state
     */
    public boolean isChecked() {
        return this.checked;
    }

    /**
     * Registers the selection listener.
     *
     * @param listener the listener
     * @return this chip, for chaining
     */
    public @NotNull MuiChip onChanged(@Nullable BooleanConsumer listener) {
        this.onChanged = listener;
        return this;
    }

    /**
     * Applies the themed fill and label colors.
     */
    private void applyStyle() {
        this.fill.setBaseColor(this.fillColor());
        this.setTextColor(this.isEnabled() ? LABEL_COLOR : MuiControls.alpha(LABEL_COLOR, 0.38F));
    }

    /**
     * Returns the base fill color for the current selected and enabled state.
     *
     * @return an ARGB color
     */
    private int fillColor() {
        int color = this.checked ? this.theme.primary() : this.theme.onSurfaceVariant();
        return this.isEnabled() ? color : MuiControls.alpha(color, 0.38F);
    }

    /**
     * Registers the shimmering fill when the chip enters the window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MuiShimmer.attach(this.fill);
    }

    /**
     * Unregisters the shimmering fill when the chip leaves the window.
     */
    @Override
    protected void onDetachedFromWindow() {
        MuiShimmer.detach(this.fill);
        super.onDetachedFromWindow();
    }
}