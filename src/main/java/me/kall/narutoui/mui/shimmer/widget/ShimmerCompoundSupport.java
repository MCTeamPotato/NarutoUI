package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.widget.CompoundButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Shared shimmer state for {@link CompoundButton} wrappers such as checkbox and radio button.
 * <p>
 * The wrappers extend different concrete classes, so this support object owns the base colors,
 * marker and attach state while the wrapper only forwards its color setters and lifecycle.
 */
final class ShimmerCompoundSupport {
    private final CompoundButton host;
    private final MuiShimmerProfile profile;
    private final Consumer<ColorStateList> textApplier;
    private final Consumer<ColorStateList> buttonTintApplier;
    private @Nullable ColorStateList baseTextColors;
    private @Nullable ColorStateList baseButtonTint;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private boolean bound;

    ShimmerCompoundSupport(
            @NotNull CompoundButton host,
            @NotNull Consumer<ColorStateList> textApplier,
            @NotNull Consumer<ColorStateList> buttonTintApplier
    ) {
        this.host = host;
        this.profile = MuiShimmerProfile.current();
        this.textApplier = textApplier;
        this.buttonTintApplier = buttonTintApplier;
    }

    void bind(@Nullable ColorStateList buttonTint, @NotNull ColorStateList textColors) {
        this.baseButtonTint = buttonTint;
        this.baseTextColors = textColors;
        this.bound = true;
        if (!MuiShimmer.isRunning()) this.applyBaseColor();
    }

    void textColors(@NotNull ColorStateList colors) {
        this.baseTextColors = colors;
        if (!MuiShimmer.isRunning()) this.textApplier.accept(colors);
    }

    void buttonTint(@Nullable ColorStateList tint) {
        this.baseButtonTint = tint;
        if (!MuiShimmer.isRunning()) this.buttonTintApplier.accept(tint);
    }

    void applyShimmerColor(int color) {
        if (!this.bound) return;
        this.textApplier.accept(ColorStateList.valueOf(color));
        this.buttonTintApplier.accept(ColorStateList.valueOf(color));
    }

    void applyBaseColor() {
        if (!this.bound) return;
        if (this.baseTextColors != null) this.textApplier.accept(this.baseTextColors);
        this.buttonTintApplier.accept(this.baseButtonTint);
    }

    boolean shimmerEnabled() {
        return this.marker.shimmer() && this.bound && this.host.isAttachedToWindow() && this.host.isEnabled();
    }

    void setShimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
    }

    @NotNull MuiShimmerable shimmerable() {
        return this.marker;
    }

    int baseColor() {
        return this.baseTextColors == null ? 0xFFFFFFFF : this.baseTextColors.getDefaultColor();
    }

    @NotNull MuiShimmerProfile profile() {
        return this.profile;
    }

    @NotNull CompoundButton host() {
        return this.host;
    }
}