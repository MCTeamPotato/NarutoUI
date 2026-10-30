package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.R;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Switch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A switch whose checked track follows the API shimmer.
 */
public final class ShimmerSwitch extends Switch implements ShimmerTarget {
    private static final int[][] STATES = new int[][]{
            new int[]{-R.attr.state_enabled, -R.attr.state_checked},
            new int[]{-R.attr.state_enabled, R.attr.state_checked},
            new int[]{R.attr.state_checked},
            StateSet.WILD_CARD
    };

    private @Nullable ShapeDrawable track;
    private int baseColor = 0xFFFFFFFF;
    private int offColor = 0xFF000000;
    private int offDisabledColor;
    private boolean bound;
    private final MuiShimmerProfile profile;

    /**
     * Creates a shimmer switch.
     *
     * @param context the screen context
     */
    public ShimmerSwitch(@NotNull Context context) {
        super(context);
        this.profile = MuiShimmerProfile.current();
    }

    /**
     * Binds the track colors used for the shimmer.
     *
     * @param track     the track drawable
     * @param baseColor checked track color
     * @param offColor  unchecked track color
     */
    public void bind(@NotNull ShapeDrawable track, int baseColor, int offColor) {
        this.track = track;
        this.baseColor = baseColor;
        this.offColor = offColor;
        this.offDisabledColor = ColorStateList.modulateColor(offColor, 0.12F);
        this.bound = true;
        this.applyBaseColor();

        if (isAttachedToWindow()) MuiShimmer.attach(this);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        this.applyColor(color);
    }

    @Override
    public void applyBaseColor() {
        this.applyColor(this.baseColor);
    }

    private void applyColor(int color) {
        ShapeDrawable track = this.track;
        if (track == null) return;

        track.setColor(new ColorStateList(STATES, new int[]{
                this.offDisabledColor,
                ColorStateList.modulateColor(color, 0.38F),
                color,
                this.offColor
        }));
        track.invalidateSelf();
    }

    @Override
    public boolean shimmerEnabled() {
        return this.bound && isEnabled() && this.profile.config().text() && this.profile.config().controls();
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