package me.kall.narutoui.mui.shimmer.widget;

import me.kall.narutoui.mui.shimmer.ShimmerColor;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.resources.ResourceId;
import icyllis.modernui.util.AttributeSet;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A button that participates in the API shimmer. Both its text and its filled background can follow
 * the ripple.
 */
public final class ShimmerButton extends Button implements ShimmerTarget {
    private ColorStateList baseColors = ColorStateList.valueOf(0xFFFFFFFF);
    private int baseColor = 0xFFFFFFFF;
    private @Nullable ShapeDrawable backgroundShape;
    private int backgroundBaseColor;
    private boolean shimmerText = true;
    private MuiShimmerable marker = MuiShimmerable.NEVER;
    private final MuiShimmerProfile profile;

    /**
     * Creates a shimmer button without an attribute set or default style.
     *
     * @param context the screen context
     */
    public ShimmerButton(@NotNull Context context) {
        this(context, null, null);
    }

    /**
     * Creates a shimmer button.
     *
     * @param context      the screen context
     * @param attrs        optional attribute set
     * @param defStyleAttr optional default style
     */
    public ShimmerButton(@NotNull Context context, @Nullable AttributeSet attrs, @Nullable ResourceId defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.profile = MuiShimmerProfile.current();
    }

    /**
     * Makes the filled background participate in the same shimmer as the text.
     *
     * @param shape          the background shape used by the button
     * @param baseBackground the normal filled background color
     * @return this button, for chaining
     */
    public @NotNull ShimmerButton bindBackground(@NotNull ShapeDrawable shape, int baseBackground) {
        this.backgroundShape = shape;
        this.backgroundBaseColor = baseBackground;
        return this;
    }

    /**
     * Controls whether the label itself shimmers. Filled buttons often keep a static label color
     * while only the background follows the ripple.
     *
     * @param shimmerText {@code false} to keep the base text color
     * @return this button, for chaining
     */
    public @NotNull ShimmerButton setTextShimmer(boolean shimmerText) {
        this.shimmerText = shimmerText;
        return this;
    }

    /**
     * Enables or disables shimmer for this individual button.
     *
     * @param shimmer {@code false} to keep the base colors
     * @return this button, for chaining
     */
    public @NotNull ShimmerButton setShimmerEnabled(boolean shimmer) {
        return this.shimmerable(MuiShimmerable.of(shimmer));
    }

    /**
     * Replaces the per-instance marker.
     *
     * @param marker the marker
     * @return this button, for chaining
     */
    public @NotNull ShimmerButton shimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
        return this;
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.marker = marker;
        MuiShimmer.refresh();
    }

    private void applyBackground(int color) {
        if (this.backgroundShape == null) return;
        this.backgroundShape.setColor(color);
        this.invalidate();
    }

    @Override
    public void setTextColor(int color) {
        this.baseColor = color;
        this.baseColors = ColorStateList.valueOf(color);
        if (this.getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(color);
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        this.baseColors = colors;
        this.baseColor = colors.getDefaultColor();
        if (this.getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(colors);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColors.getColorForState(this.getDrawableState(), this.baseColor);
    }

    @Override
    public void applyShimmerColor(int color) {
        if (this.shimmerText) {
            super.setTextColor(color);
        } else {
            super.setTextColor(this.baseColors);
        }
        this.applyBackground(ShimmerColor.blend(this.backgroundBaseColor, color, 0.62F));
    }

    @Override
    public void applyBaseColor() {
        super.setTextColor(this.baseColors);
        this.applyBackground(this.backgroundBaseColor);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.marker.shimmer() && this.isAttachedToWindow();
    }

    @Override
    public @NotNull MuiShimmerProfile shimmerProfile() {
        return this.profile;
    }

    @Override
    public float shimmerStrength() {
        return this.isEnabled() ? 0.65F : 0.3F;
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