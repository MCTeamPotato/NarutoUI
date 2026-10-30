package me.kall.narutoui.mui.shimmer;

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
    private final MuiShimmerProfile profile;

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
     */
    public void bindBackground(@NotNull ShapeDrawable shape, int baseBackground) {
        this.backgroundShape = shape;
        this.backgroundBaseColor = baseBackground;
    }

    /**
     * Controls whether the label itself shimmers. Filled buttons often keep a static label color
     * while only the background follows the ripple.
     *
     * @param shimmerText {@code false} to keep the base text color
     */
    public void setTextShimmer(boolean shimmerText) {
        this.shimmerText = shimmerText;
    }

    private void applyBackground(int color) {
        if (this.backgroundShape == null) return;
        this.backgroundShape.setColor(color);
        invalidate();
    }

    @Override
    public void setTextColor(int color) {
        this.baseColor = color;
        this.baseColors = ColorStateList.valueOf(color);
        if (getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(color);
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        this.baseColors = colors;
        this.baseColor = colors.getDefaultColor();
        if (getTextColors() == null || !MuiShimmer.isRunning()) super.setTextColor(colors);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColors.getColorForState(getDrawableState(), this.baseColor);
    }

    @Override
    public void applyShimmerColor(int color) {
        if (this.shimmerText) {
            super.setTextColor(color);
        } else {
            super.setTextColor(this.baseColors);
        }
        this.applyBackground(blend(this.backgroundBaseColor, color, 0.62F));
    }

    @Override
    public void applyBaseColor() {
        super.setTextColor(this.baseColors);
        this.applyBackground(this.backgroundBaseColor);
    }

    @SuppressWarnings("SameParameterValue")
    private static int blend(int from, int to, float amount) {
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * amount);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * amount);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * amount);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean shimmerEnabled() {
        return isAttachedToWindow() && this.profile.config().text() && this.profile.config().configText();
    }

    @Override
    public @NotNull MuiShimmerProfile shimmerProfile() {
        return this.profile;
    }

    @Override
    public float shimmerStrength() {
        return isEnabled() ? 0.65F : 0.3F;
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