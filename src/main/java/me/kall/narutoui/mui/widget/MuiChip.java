package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.TextView;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.ShimmerCircleDrawable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A small toggleable chip. The fill is a shimmer target: selected chips use the theme accent,
 * unselected chips use a light neutral, and the label stays black so it remains readable while the
 * background wave animates.
 */
public final class MuiChip extends TextView {
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

        setText(MuiLocalization.resolve(label));
        setTextSize(11);
        setTextColor(LABEL_COLOR);
        setGravity(Gravity.CENTER);
        setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 4),
                MuiControls.dp(context, 10), MuiControls.dp(context, 4));

        this.fill = new ShimmerCircleDrawable(this, fillColor());
        this.fill.setCornerRadius(MuiControls.dp(context, 14));
        setBackground(this.fill);

        setOnClickListener(view -> {
            this.checked = !this.checked;
            applyStyle();
            if (this.onChanged != null) this.onChanged.accept(this.checked);
        });
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
    public static @NotNull MuiChip create(@NotNull Context context, @NotNull MuiTheme theme,
                                          @NotNull CharSequence label, boolean checked) {
        return new MuiChip(context, theme, label, checked);
    }

    /**
     * Updates the selected state without firing the listener.
     *
     * @param checked the new state
     * @return this chip, for chaining
     */
    public @NotNull MuiChip checked(boolean checked) {
        this.checked = checked;
        applyStyle();
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
        this.fill.setBaseColor(fillColor());
        setTextColor(LABEL_COLOR);
    }

    /**
     * Returns the base fill color for the current selected state.
     *
     * @return an ARGB color
     */
    private int fillColor() {
        return this.checked ? this.theme.primary() : this.theme.onSurfaceVariant();
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