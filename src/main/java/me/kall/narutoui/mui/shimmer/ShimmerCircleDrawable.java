package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded background of the navigation logo; it ripples with the surrounding text.
 */
public final class ShimmerCircleDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private final MuiShimmerProfile profile;
    private int baseColor;

    /**
     * Creates a shimmering circle background.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     */
    public ShimmerCircleDrawable(@NotNull View host, int baseColor) {
        this.host = host;
        this.profile = MuiShimmerProfile.current();
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
        return this.host.isAttachedToWindow() && this.host.isEnabled() && this.profile.config().text() && this.profile.config().controls();
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