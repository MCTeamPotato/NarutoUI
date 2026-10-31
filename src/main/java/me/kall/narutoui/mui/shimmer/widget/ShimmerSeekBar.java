package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.SeekBar;
import org.jetbrains.annotations.NotNull;

/**
 * A seek bar whose progress line and thumb follow the API shimmer.
 */
public final class ShimmerSeekBar extends SeekBar implements ShimmerTarget {
    private int baseColor = 0xFFFFFFFF;
    private boolean bound;
    private final MuiShimmerProfile profile;
    private MuiShimmerable marker = MuiShimmerable.NEVER;

    /**
     * Creates a shimmer seek bar.
     *
     * @param context the screen context
     */
    public ShimmerSeekBar(@NotNull Context context) {
        super(context);
        this.profile = MuiShimmerProfile.current();
    }

    /**
     * Binds the base progress color.
     *
     * @param baseColor base ARGB color
     * @return this seek bar, for chaining
     */
    public @NotNull ShimmerSeekBar bind(int baseColor) {
        this.baseColor = baseColor;
        this.bound = true;
        if (this.isAttachedToWindow()) MuiShimmer.attach(this);
        return this;
    }

    /**
     * Replaces the per-instance marker.
     *
     * @param marker the marker
     * @return this seek bar, for chaining
     */
    public @NotNull ShimmerSeekBar shimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
        return this;
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        this.setProgressTintList(ColorStateList.valueOf(color));
        this.setThumbTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void applyBaseColor() {
        this.applyShimmerColor(this.baseColor);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.bound && this.isEnabled();
    }

    @Override
    public @NotNull MuiShimmerProfile shimmerProfile() {
        return this.profile;
    }

    @Override
    public @NotNull View shimmerHost() {
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.bound) MuiShimmer.attach(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        MuiShimmer.detach(this);
        super.onDetachedFromWindow();
    }
}