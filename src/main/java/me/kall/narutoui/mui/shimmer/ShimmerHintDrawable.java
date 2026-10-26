package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.MuiControls;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded outline behind the bottom-left ESC hint; its outline follows the shimmer.
 */
public final class ShimmerHintDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private final MuiShimmerProfile profile;
    private final int strokeWidth;
    private int baseColor;

    /**
     * Creates a shimmering hint outline.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     */
    public ShimmerHintDrawable(@NotNull View host, int baseColor) {
        this.host = host;
        this.profile = MuiShimmerProfile.current();
        this.baseColor = baseColor;
        this.strokeWidth = Math.max(1, MuiControls.dp(host.getContext(), 1));
        this.applyColor(baseColor);
    }

    /**
     * Updates the color restored when shimmer is disabled.
     *
     * @param color base ARGB color
     */
    public void setBaseColor(int color) {
        this.baseColor = color;
        if (!MuiShimmer.isRunning()) {
            this.applyColor(color);
            invalidateSelf();
        }
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        this.applyColor(color);
        invalidateSelf();
    }

    @Override
    public void applyBaseColor() {
        this.applyColor(this.baseColor);
        invalidateSelf();
    }

    private void applyColor(int color) {
        setColor(ColorStateList.modulateColor(color, 0.14F));
        setStroke(this.strokeWidth, ColorStateList.modulateColor(color, 0.7F));
    }

    @Override
    public boolean shimmerEnabled() {
        return this.host.isAttachedToWindow() && this.profile.config().text()
                && this.profile.config().controls();
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