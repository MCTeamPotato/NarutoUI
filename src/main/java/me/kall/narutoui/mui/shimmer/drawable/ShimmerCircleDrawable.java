package me.kall.narutoui.mui.shimmer.drawable;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded background of the navigation logo; it ripples with the surrounding text.
 */
public final class ShimmerCircleDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private final MuiShimmerProfile profile;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private int baseColor;

    /**
     * Creates a shimmering circle background with shimmer disabled by default.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     */
    public ShimmerCircleDrawable(@NotNull View host, int baseColor) {
        this(host, baseColor, MuiShimmerable.NEVER);
    }

    /**
     * Creates a shimmering circle background.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     * @param marker    per-instance marker
     */
    public ShimmerCircleDrawable(@NotNull View host, int baseColor, @NotNull MuiShimmerable marker) {
        this.host = host;
        this.profile = MuiShimmerProfile.current();
        this.marker = marker;
        this.baseColor = baseColor;
        this.setColor(baseColor);
    }

    /**
     * Updates the color restored when shimmer is disabled.
     *
     * @param color base ARGB color
     * @return this drawable, for chaining
     */
    public @NotNull ShimmerCircleDrawable setBaseColor(int color) {
        this.baseColor = color;
        if (!MuiShimmer.isRunning()) {
            this.setColor(color);
            this.invalidateSelf();
        }
        return this;
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        this.setColor(color);
        this.invalidateSelf();
    }

    @Override
    public void applyBaseColor() {
        this.setColor(this.baseColor);
        this.invalidateSelf();
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.host.isAttachedToWindow() && this.host.isEnabled();
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
    }

    @Override
    public @NotNull MuiShimmerProfile shimmerProfile() {
        return this.profile;
    }

    @Override
    public @NotNull View shimmerHost() {
        return this.host;
    }
}