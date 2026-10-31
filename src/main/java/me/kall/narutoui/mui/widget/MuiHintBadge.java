package me.kall.narutoui.mui.widget;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.drawable.ShimmerHintDrawable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A small outlined hint badge. Both the text and the outline can participate in the shared shimmer,
 * so the badge matches the navigation logo without the caller having to register shimmer targets
 * manually.
 */
public final class MuiHintBadge extends FrameLayout implements MuiShimmerContainer {
    /** Hint text. */
    private final TextView text;
    /** Shimmering outline. */
    private final ShimmerHintDrawable outline;
    /** Marker applied to the text and outline. */
    private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

    /**
     * Creates a hint badge.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param content hint text or translation key
     */
    public MuiHintBadge(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence content) {
        super(context);

        this.text = MuiControls.text(context, theme, content, 9, theme.onSurfaceVariant());
        this.text.setGravity(Gravity.CENTER);
        this.addView(this.text, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));

        this.setPadding(MuiControls.dp(context, 4), MuiControls.dp(context, 4), MuiControls.dp(context, 4), MuiControls.dp(context, 4));

        this.outline = new ShimmerHintDrawable(this.text, theme.primary());
        this.outline.setCornerRadius(MuiControls.dp(context, 8));
        this.setBackground(this.outline);
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.shimmerable = marker;
        MuiShimmerScope.apply(this.text, marker);
        this.outline.setShimmerable(marker);
        MuiShimmer.refresh();
    }

    /**
     * Creates a hint badge.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param content hint text or translation key
     * @return the badge
     */
    public static @NotNull MuiHintBadge create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence content) {
        return new MuiHintBadge(context, theme, content);
    }

    /**
     * Replaces the hint text.
     *
     * @param content hint text or translation key
     * @return this badge, for chaining
     */
    public @NotNull MuiHintBadge text(@NotNull CharSequence content) {
        this.text.setText(MuiLocalization.resolve(content));
        return this;
    }

    /**
     * Returns the current hint text.
     *
     * @return the hint text
     */
    public @NotNull CharSequence text() {
        return this.text.getText();
    }

    /**
     * Sets the outline color restored when shimmer is disabled.
     *
     * @param color ARGB color
     * @return this badge, for chaining
     */
    public @NotNull MuiHintBadge color(int color) {
        this.outline.setBaseColor(color);
        return this;
    }

    /**
     * Sets the text size.
     *
     * @param sp size in SP
     * @return this badge, for chaining
     */
    public @NotNull MuiHintBadge textSize(float sp) {
        this.text.setTextSize(sp);
        return this;
    }

    /**
     * Registers the outline when the badge enters the window.
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MuiShimmer.attach(this.outline);
    }

    /**
     * Unregisters the outline when the badge leaves the window.
     */
    @Override
    protected void onDetachedFromWindow() {
        MuiShimmer.detach(this.outline);
        super.onDetachedFromWindow();
    }
}