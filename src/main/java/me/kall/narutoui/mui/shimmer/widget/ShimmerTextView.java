package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.TextView;
import org.jetbrains.annotations.NotNull;

/**
 * A text view that participates in the API shimmer. The base color is remembered so the ripple can
 * always be blended from it.
 */
public final class ShimmerTextView extends TextView implements ShimmerTarget {
    private int baseColor = 0xFFFFFFFF;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private final MuiShimmerProfile profile;

    /**
     * Creates a shimmer text view with shimmer disabled by default.
     *
     * @param context the screen context
     */
    public ShimmerTextView(@NotNull Context context) {
        this(context, MuiShimmerable.NEVER);
    }

    /**
     * Creates a shimmer text view.
     *
     * @param context the screen context
     * @param marker  per-instance marker
     */
    public ShimmerTextView(@NotNull Context context, @NotNull MuiShimmerable marker) {
        super(context);
        this.profile = MuiShimmerProfile.current();
        this.marker = marker;
    }

    /**
     * Replaces the per-instance marker.
     *
     * @param marker the marker
     * @return this text view, for chaining
     */
    public @NotNull ShimmerTextView shimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
        return this;
    }

    /**
     * Enables or disables shimmer for this individual view.
     *
     * @param enabled {@code false} to keep the base color
     * @return this text view, for chaining
     */
    public @NotNull ShimmerTextView setShimmerEnabled(boolean enabled) {
        return this.shimmerable(MuiShimmerable.of(enabled));
    }

    @Override
    public void setTextColor(int color) {
        this.baseColor = color;
        // During TextView's own constructor mTextColor is still null; it must be initialised,
        // otherwise the subsequent setHintTextColor() call inside the constructor NPEs.
        if (this.getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(color);
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        this.baseColor = colors.getDefaultColor();
        if (this.getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(colors);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        super.setTextColor(color);
    }

    @Override
    public void applyBaseColor() {
        super.setTextColor(this.baseColor);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.isEnabled();
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