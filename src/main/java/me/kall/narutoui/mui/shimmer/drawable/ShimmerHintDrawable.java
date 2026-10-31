package me.kall.narutoui.mui.shimmer.drawable;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded outline behind the bottom-left ESC hint; its outline follows the shimmer.
 */
public final class ShimmerHintDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private final MuiShimmerProfile profile;
    private final int strokeWidth;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private int baseColor;

    /**
     * Creates a shimmering hint outline with shimmer disabled by default.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     */
    public ShimmerHintDrawable(@NotNull View host, int baseColor) {
        this(host, baseColor, MuiShimmerable.NEVER);
    }

    /**
     * Creates a shimmering hint outline.
     *
     * @param host      the view used to sample the wave position
     * @param baseColor the base ARGB color
     * @param marker    per-instance marker
     */
    public ShimmerHintDrawable(@NotNull View host, int baseColor, @NotNull MuiShimmerable marker) {
        this.host = host;
        this.profile = MuiShimmerProfile.current();
        this.marker = marker;
        this.baseColor = baseColor;
        this.strokeWidth = Math.max(1, MuiControls.dp(host.getContext(), 1));
        this.applyColor(baseColor);
    }

    /**
     * Updates the color restored when shimmer is disabled.
     *
     * @param color base ARGB color
     * @return this drawable, for chaining
     */
    public @NotNull ShimmerHintDrawable setBaseColor(int color) {
        this.baseColor = color;
        if (!MuiShimmer.isRunning()) {
            this.applyColor(color);
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
        this.applyColor(color);
        this.invalidateSelf();
    }

    @Override
    public void applyBaseColor() {
        this.applyColor(this.baseColor);
        this.invalidateSelf();
    }

    private void applyColor(int color) {
        this.setColor(ColorStateList.modulateColor(color, 0.14F));
        this.setStroke(this.strokeWidth, ColorStateList.modulateColor(color, 0.7F));
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.host.isAttachedToWindow();
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