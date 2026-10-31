package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.RadioButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A radio button whose text and button tint can participate in shimmer.
 */
public final class ShimmerRadioButton extends RadioButton implements ShimmerTarget {
    private final ShimmerCompoundSupport support;

    /**
     * Creates a shimmer radio button.
     *
     * @param context the screen context
     */
    public ShimmerRadioButton(@NotNull Context context) {
        super(context);
        this.support = new ShimmerCompoundSupport(this, this::applyTextColors, this::applyButtonTint);
        ColorStateList text = this.getTextColors() == null ? ColorStateList.valueOf(0xFFFFFFFF) : this.getTextColors();
        this.support.bind(this.getButtonTintList(), text);
    }

    /**
     * Binds the base colors used for restore.
     *
     * @param buttonTint base button tint
     * @param textColors base text colors
     * @return this radio button, for chaining
     */
    public @NotNull ShimmerRadioButton bind(@Nullable ColorStateList buttonTint, @NotNull ColorStateList textColors) {
        this.support.bind(buttonTint, textColors);
        return this;
    }

    /**
     * Replaces the per-instance marker.
     *
     * @param marker the marker
     * @return this radio button, for chaining
     */
    public @NotNull ShimmerRadioButton shimmerable(@NotNull MuiShimmerable marker) {
        this.support.setShimmerable(marker);
        return this;
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.support.setShimmerable(marker);
    }

    @Override
    public void setTextColor(int color) {
        if (this.support == null) {
            super.setTextColor(color);
        } else {
            this.support.textColors(ColorStateList.valueOf(color));
        }
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        if (this.support == null) {
            super.setTextColor(colors);
        } else {
            this.support.textColors(colors);
        }
    }

    @Override
    public void setButtonTintList(@Nullable ColorStateList tint) {
        if (this.support == null) {
            super.setButtonTintList(tint);
        } else {
            this.support.buttonTint(tint);
        }
    }

    private void applyTextColors(@NotNull ColorStateList colors) {
        super.setTextColor(colors);
    }

    private void applyButtonTint(@Nullable ColorStateList tint) {
        super.setButtonTintList(tint);
    }

    @Override
    public int shimmerBaseColor() {
        return this.support.baseColor();
    }

    @Override
    public void applyShimmerColor(int color) {
        this.support.applyShimmerColor(color);
    }

    @Override
    public void applyBaseColor() {
        this.support.applyBaseColor();
    }

    @Override
    public boolean shimmerEnabled() {
        return this.support.shimmerEnabled();
    }

    @Override
    public @NotNull MuiShimmerProfile shimmerProfile() {
        return this.support.profile();
    }

    @Override
    public @NotNull View shimmerHost() {
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MuiShimmer.attach(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        MuiShimmer.detach(this);
        super.onDetachedFromWindow();
    }
}