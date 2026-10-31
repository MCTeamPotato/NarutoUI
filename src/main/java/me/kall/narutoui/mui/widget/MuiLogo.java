package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.ShimmerCircleDrawable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A compact brand mark for a navigation rail or a custom header.
 * <p>
 * The glyph sits on a rounded shimmering surface and is centered horizontally, so the logo can be
 * added to a full-width rail header with normal fill params. Shimmer registration is owned by the
 * logo itself; callers no longer need to track it through a screen host.
 */
public final class MuiLogo extends FrameLayout {
    /** Default size in DP. */
    private static final int DEFAULT_SIZE = 36;

    /** Glyph view. */
    private final TextView glyph;
    /** Shimmering background. */
    private final ShimmerCircleDrawable circle;
    /** Current size in DP. */
    private int size = DEFAULT_SIZE;

    /**
     * Creates a logo.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    glyph text, usually one character
     */
    public MuiLogo(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
        super(context);

        this.glyph = MuiControls.heading(context, theme, text, 16, theme.onPrimary());
        this.glyph.setGravity(Gravity.CENTER);

        this.circle = new ShimmerCircleDrawable(this.glyph, theme.primary());
        this.circle.setCornerRadius(MuiControls.dp(context, (float) DEFAULT_SIZE / 2));
        this.glyph.setBackground(this.circle);

        this.addView(this.glyph, new FrameLayout.LayoutParams(MuiControls.dp(context, DEFAULT_SIZE), MuiControls.dp(context, DEFAULT_SIZE), Gravity.CENTER_HORIZONTAL));
    }

    /**
     * Creates a logo.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    glyph text, usually one character
     * @return the logo
     */
    public static @NotNull MuiLogo create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
        return new MuiLogo(context, theme, text);
    }

    /**
     * Replaces the glyph.
     *
     * @param glyph glyph text or translation key
     * @return this logo, for chaining
     */
    public @NotNull MuiLogo glyph(@NotNull CharSequence glyph) {
        this.glyph.setText(MuiLocalization.resolve(glyph));
        return this;
    }

    /**
     * Returns the current glyph.
     *
     * @return the glyph text
     */
    public @NotNull CharSequence glyph() {
        return this.glyph.getText();
    }

    /**
     * Sets the square size.
     *
     * @param dp size in density-independent pixels
     * @return this logo, for chaining
     */
    public @NotNull MuiLogo size(int dp) {
        this.size = dp;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.glyph.getLayoutParams();
        params.width = MuiControls.dp(this.getContext(), dp);
        params.height = MuiControls.dp(this.getContext(), dp);
        this.glyph.setLayoutParams(params);
        this.circle.setCornerRadius(MuiControls.dp(this.getContext(), (float) dp / 2));
        return this;
    }

    /**
     * Sets the background color restored when shimmer is disabled.
     *
     * @param color ARGB color
     * @return this logo, for chaining
     */
    public @NotNull MuiLogo color(int color) {
        this.circle.setBaseColor(color);
        return this;
    }

    /**
     * Sets the glyph color.
     *
     * @param color ARGB color
     * @return this logo, for chaining
     */
    public @NotNull MuiLogo textColor(int color) {
        this.glyph.setTextColor(color);
        return this;
    }

    /**
     * Returns the current size in DP.
     *
     * @return the size
     */
    public int size() {
        return this.size;
    }

    /**
     * Registers the shimmer target when the logo enters the window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MuiShimmer.attach(this.circle);
    }

    /**
     * Unregisters the shimmer target when the logo leaves the window.
     */
    @Override
    protected void onDetachedFromWindow() {
        MuiShimmer.detach(this.circle);
        super.onDetachedFromWindow();
    }
}