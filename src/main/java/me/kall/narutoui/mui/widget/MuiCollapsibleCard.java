package me.kall.narutoui.mui.widget;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MeasureSpec;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.screen.MuiSettingsSection;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A card with a clickable header and an animated expand/collapse body.
 * <p>
 * The header always stays visible and contains a {@code +}/{@code -} chevron, a title, an optional
 * summary and an optional trailing view (for example an enabled switch). Clicking the header
 * animates the body between zero height and its measured height. The body is a regular
 * {@link MuiLayout}, so callers can compose it with {@link MuiControls} or with an internal
 * {@link MuiSettingsSection}.
 * <p>
 * This is the generic card used directly by NarutoOverlay's Cards page; it is also useful for any
 * long settings page that should start with a compact overview.
 */
public final class MuiCollapsibleCard extends LinearLayout {
    /** Expand/collapse animation duration in milliseconds. */
    private static final long ANIMATION_DURATION = 190L;

    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Header row that stays visible. */
    private final LinearLayout header;
    /** Plus/minus indicator at the start of the header. */
    private final TextView chevron;
    /** Optional summary below the title. */
    private final TextView summaryView;
    /** Collapsible body. */
    private final MuiLayout body;
    /** Fixed-height wrapper that animates the body. */
    private final RevealLayout reveal;

    /** Whether the body is currently expanded. */
    private boolean expanded;
    /** Optional expansion listener. */
    private @Nullable BooleanConsumer onExpandedChanged;
    /** Currently running reveal animation. */
    private @Nullable ValueAnimator animator;
    /** Guards callbacks from animations that were superseded or cancelled. */
    private int animationToken;

    /**
     * Creates a collapsible card.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    card title
     * @param summary  optional summary; empty hides the summary line
     * @param expanded whether the body starts expanded
     */
    public MuiCollapsibleCard(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title,
                              @Nullable CharSequence summary, boolean expanded) {
        super(context);
        this.theme = theme;
        this.expanded = expanded;
        setOrientation(VERTICAL);
        MuiLayout.applyCardStyle(this, context, theme);

        this.header = new LinearLayout(context);
        this.header.setOrientation(HORIZONTAL);
        this.header.setGravity(Gravity.CENTER_VERTICAL);
        this.header.setPadding(dp(context, 2), dp(context, 8), dp(context, 2), dp(context, 8));

        this.chevron = MuiControls.text(context, theme, expanded ? "-" : "+", 16, theme.onSurfaceVariant());
        this.chevron.setGravity(Gravity.CENTER);
        this.chevron.setMinimumWidth(dp(context, 20));
        this.header.addView(this.chevron, MuiControls.wrap());

        MuiLayout texts = MuiLayout.column(context, theme);
        texts.add(MuiControls.heading(context, theme, title, 15, theme.primary()));
        this.summaryView = MuiControls.text(context, theme,
                summary == null ? "" : summary, 11, theme.onSurfaceVariant());
        this.summaryView.setVisibility(summary == null || summary.length() == 0 ? GONE : VISIBLE);
        texts.add(this.summaryView);

        LinearLayout.LayoutParams textParams = MuiControls.weight();
        textParams.setMargins(dp(context, 10), 0, 0, 0);
        this.header.addView(texts, textParams);
        this.header.setOnClickListener(view -> this.toggle());

        this.body = MuiLayout.column(context, theme).padding(0, 4, 0, 0);
        this.reveal = new RevealLayout(context);
        this.reveal.setClipChildren(true);
        this.reveal.addView(this.body, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
        this.reveal.setVisibility(expanded ? VISIBLE : GONE);

        addView(this.header, new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
        addView(this.reveal, new LinearLayout.LayoutParams(MuiControls.MATCH,
                expanded ? MuiControls.WRAP : 0));
    }

    /**
     * Creates a collapsible card.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    card title
     * @param summary  optional summary; empty hides the summary line
     * @param expanded whether the body starts expanded
     * @return the card
     */
    public static @NotNull MuiCollapsibleCard card(@NotNull Context context, @NotNull MuiTheme theme,
                                                   @NotNull CharSequence title, @Nullable CharSequence summary,
                                                   boolean expanded) {
        return new MuiCollapsibleCard(context, theme, title, summary, expanded);
    }

    /**
     * Returns the active theme.
     *
     * @return the theme
     */
    public @NotNull MuiTheme theme() {
        return this.theme;
    }

    /**
     * Returns the collapsible body. Add rows and controls here.
     *
     * @return the body layout
     */
    public @NotNull MuiLayout body() {
        return this.body;
    }

    /**
     * Configures the collapsible body and returns this card.
     *
     * @param content body callback
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard body(@NotNull Consumer<MuiLayout> content) {
        content.accept(this.body);
        return this;
    }

    /**
     * Adds a view to the right side of the header. Calling it more than once creates a trailing
     * group, which is useful for a switch plus a small action.
     *
     * @param view the trailing view, for example an enabled switch
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard trailing(@NotNull View view) {
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(dp(getContext(), 12), 0, 0, 0);
        this.header.addView(view, params);
        return this;
    }

    /**
     * Replaces the summary line.
     *
     * @param text summary text or translation key; empty hides the line
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard summary(@NotNull CharSequence text) {
        String value = MuiLocalization.resolve(text);
        this.summaryView.setText(value);
        this.summaryView.setVisibility(value.isEmpty() ? GONE : VISIBLE);
        return this;
    }

    /**
     * Expands or collapses the body with the default animation.
     *
     * @param expanded {@code true} to expand
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard expanded(boolean expanded) {
        setExpanded(expanded);
        return this;
    }

    /**
     * Toggles the body with the default animation.
     *
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard toggle() {
        setExpanded(!this.expanded);
        return this;
    }

    /**
     * Expands or collapses the body with the default animation.
     *
     * @param expanded {@code true} to expand
     */
    public void setExpanded(boolean expanded) {
        setExpanded(expanded, true);
    }

    /**
     * Expands or collapses the body.
     *
     * @param expanded {@code true} to expand
     * @param animate  {@code false} to apply the target height immediately
     */
    public void setExpanded(boolean expanded, boolean animate) {
        if (this.expanded == expanded) return;

        this.expanded = expanded;
        this.chevron.setText(expanded ? "-" : "+");
        if (this.onExpandedChanged != null) this.onExpandedChanged.accept(expanded);
        animateReveal(expanded, animate);
    }

    /**
     * Returns whether the body is expanded.
     *
     * @return {@code true} when expanded
     */
    public boolean isExpanded() {
        return this.expanded;
    }

    /**
     * Sets a listener invoked whenever the expansion state changes.
     *
     * @param listener the listener, or {@code null} to clear it
     * @return this card, for chaining
     */
    public @NotNull MuiCollapsibleCard onExpandedChanged(@Nullable BooleanConsumer listener) {
        this.onExpandedChanged = listener;
        return this;
    }

    /**
     * Stops a running animation when the card leaves the window.
     */
    @Override
    protected void onDetachedFromWindow() {
        this.animationToken++;
        if (this.animator != null) {
            this.animator.cancel();
            this.animator = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * Animates the reveal wrapper between zero and the measured body height.
     *
     * @param expand  target state
     * @param animate whether to animate
     */
    private void animateReveal(boolean expand, boolean animate) {
        int token = ++this.animationToken;
        if (this.animator != null) {
            this.animator.cancel();
            this.animator = null;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.reveal.getLayoutParams();
        if (!animate) {
            this.reveal.setVisibility(expand ? VISIBLE : GONE);
            params.height = expand ? MuiControls.WRAP : 0;
            this.reveal.setLayoutParams(params);
            return;
        }

        int from = this.reveal.getVisibility() == VISIBLE ? this.reveal.getHeight() : 0;
        int target = 0;

        if (expand) {
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            if (width <= 0) width = this.reveal.getWidth();
            if (width <= 0) {
                this.reveal.setVisibility(VISIBLE);
                params.height = MuiControls.WRAP;
                this.reveal.setLayoutParams(params);
                return;
            }

            params.height = MuiControls.WRAP;
            this.reveal.setLayoutParams(params);
            this.reveal.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            target = this.reveal.getMeasuredHeight();

            this.reveal.setVisibility(VISIBLE);
            params.height = 0;
            this.reveal.setLayoutParams(params);
        }

        ValueAnimator animator = ValueAnimator.ofInt(from, target);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            if (token != this.animationToken) return;
            params.height = (Integer) animation.getAnimatedValue();
            this.reveal.requestLayout();
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (token != MuiCollapsibleCard.this.animationToken) return;
                MuiCollapsibleCard.this.animator = null;
                if (expand) {
                    params.height = MuiControls.WRAP;
                } else {
                    params.height = 0;
                    MuiCollapsibleCard.this.reveal.setVisibility(GONE);
                }
                MuiCollapsibleCard.this.reveal.requestLayout();
            }
        });
        this.animator = animator;
        animator.start();
    }

    /**
     * Converts a DP value to pixels.
     *
     * @param context the screen context
     * @param value   value in DP
     * @return value in pixels
     */
    private static int dp(@NotNull Context context, int value) {
        return MuiControls.dp(context, value);
    }

    /**
     * Reveal wrapper that measures its child without a height constraint, so the body can report
     * its natural height while the wrapper is still animating from zero.
     */
    private static final class RevealLayout extends FrameLayout {
        /**
         * Creates a reveal wrapper.
         *
         * @param context the screen context
         */
        RevealLayout(@NotNull Context context) {
            super(context);
        }

        /**
         * Measures the child with an unspecified height.
         *
         * @param child                the body view
         * @param parentWidthMeasureSpec parent width spec
         * @param widthUsed            horizontal space already used
         * @param parentHeightMeasureSpec parent height spec
         * @param heightUsed           vertical space already used
         */
        @Override
        protected void measureChildWithMargins(@NotNull View child, int parentWidthMeasureSpec, int widthUsed,
                                               int parentHeightMeasureSpec, int heightUsed) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + params.leftMargin + params.rightMargin + widthUsed,
                    params.width);
            child.measure(childWidthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
    }
}