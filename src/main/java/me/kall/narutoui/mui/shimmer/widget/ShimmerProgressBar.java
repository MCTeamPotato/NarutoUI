package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.ProgressBar;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A progress bar whose progress, secondary progress and indeterminate tints can shimmer.
 * <p>
 * The base tint lists are remembered so stopping shimmer restores the normal progress colors.
 */
public final class ShimmerProgressBar extends ProgressBar implements ShimmerTarget {
    private final MuiShimmerProfile profile;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private @Nullable ColorStateList baseProgressTint;
    private @Nullable ColorStateList baseBackgroundTint;
    private @Nullable ColorStateList baseSecondaryTint;
    private @Nullable ColorStateList baseIndeterminateTint;
    private int baseColor = 0xFFFFFFFF;
    private boolean bound;

    /**
     * Creates a shimmer progress bar.
     *
     * @param context the screen context
     */
    public ShimmerProgressBar(@NotNull Context context) {
        super(context);
        this.profile = MuiShimmerProfile.current();
        this.bind(
                this.getProgressTintList(),
                this.getProgressBackgroundTintList(),
                this.getSecondaryProgressTintList(),
                this.getIndeterminateTintList());
    }

    /**
     * Binds the base tints used for restore.
     *
     * @param progressTint      progress tint
     * @param backgroundTint    progress background tint
     * @param secondaryTint     secondary progress tint
     * @param indeterminateTint indeterminate tint
     * @return this bar, for chaining
     */
    public @NotNull ShimmerProgressBar bind(
            @Nullable ColorStateList progressTint,
            @Nullable ColorStateList backgroundTint,
            @Nullable ColorStateList secondaryTint,
            @Nullable ColorStateList indeterminateTint
    ) {
        this.baseProgressTint = progressTint;
        this.baseBackgroundTint = backgroundTint;
        this.baseSecondaryTint = secondaryTint;
        this.baseIndeterminateTint = indeterminateTint;
        this.baseColor = progressTint == null ? 0xFFFFFFFF : progressTint.getDefaultColor();
        this.bound = true;
        if (!MuiShimmer.isRunning()) this.applyBaseColor();
        return this;
    }

    /**
     * Replaces the per-instance marker.
     *
     * @param marker the marker
     * @return this bar, for chaining
     */
    public @NotNull ShimmerProgressBar shimmerable(@NotNull MuiShimmerable marker) {
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
        this.setProgressBackgroundTintList(ColorStateList.valueOf(MuiControls.alpha(color, 0.28F)));
        this.setSecondaryProgressTintList(ColorStateList.valueOf(MuiControls.alpha(color, 0.5F)));
        this.setIndeterminateTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void applyBaseColor() {
        this.setProgressTintList(this.baseProgressTint);
        this.setProgressBackgroundTintList(this.baseBackgroundTint);
        this.setSecondaryProgressTintList(this.baseSecondaryTint);
        this.setIndeterminateTintList(this.baseIndeterminateTint);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.bound && this.isAttachedToWindow() && this.isEnabled();
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