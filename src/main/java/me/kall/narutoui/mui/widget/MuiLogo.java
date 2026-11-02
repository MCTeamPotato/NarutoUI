package me.kall.narutoui.mui.widget;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.drawable.ShimmerCircleDrawable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A compact brand mark for a navigation rail or a custom header.
 * <p>
 * The glyph is a separate, static text layer above a shimmering disc, so the letter always stays
 * readable while the background follows the accent wave.
 */
public final class MuiLogo extends FrameLayout implements MuiShimmerContainer {
    /** Default size in DP. */
    private static final int DEFAULT_SIZE = 36;

    /** Shimmering disc behind the glyph. */
    private final View disc;
    /** Static glyph above the disc. */
    private final TextView glyph;
    /** Shimmering background. */
    private final ShimmerCircleDrawable circle;
    /** Current size in DP. */
    private int size = DEFAULT_SIZE;
    /** Marker applied to the disc. */
    private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

    /**
     * Creates a logo.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    glyph text, usually one character
     */
    public MuiLogo(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
        super(context);

        this.disc = new View(context);
        this.circle = new ShimmerCircleDrawable(this.disc, theme.primary());
        this.circle.setCornerRadius(MuiControls.dp(context, (float) DEFAULT_SIZE / 2));
        this.disc.setBackground(this.circle);
        this.addView(this.disc, this.logoParams(context, DEFAULT_SIZE));

        this.glyph = new TextView(context);
        this.glyph.setText(MuiLocalization.resolve(text));
        this.glyph.setTextSize(16);
        this.glyph.setTextColor(theme.onPrimary());
        this.glyph.setTextStyle(TextPaint.BOLD);
        this.glyph.setGravity(Gravity.CENTER);
        this.addView(this.glyph, this.logoParams(context, DEFAULT_SIZE));
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.shimmerable = marker;
        this.circle.setShimmerable(marker);
        MuiShimmer.refresh();
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
        this.disc.setLayoutParams(this.logoParams(this.getContext(), dp));
        this.glyph.setLayoutParams(this.logoParams(this.getContext(), dp));
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

    private @NotNull FrameLayout.LayoutParams logoParams(@NotNull Context context, int dp) {
        return new FrameLayout.LayoutParams(MuiControls.dp(context, dp), MuiControls.dp(context, dp), Gravity.CENTER_HORIZONTAL);
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