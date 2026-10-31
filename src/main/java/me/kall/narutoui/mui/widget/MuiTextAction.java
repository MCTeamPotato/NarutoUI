package me.kall.narutoui.mui.widget;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A small primary-colored text action. The label is created through {@link MuiControls}, so it uses
 * the active widget factory and participates in shimmer exactly like other text controls.
 * <p>
 * The action is a small container instead of a raw {@link TextView}; this keeps the click target
 * stable and allows the label to be replaced after the action is created.
 */
public final class MuiTextAction extends FrameLayout {
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Label view created by the active widget factory. */
    private final TextView text;
    /** Pending flash restore callback, or {@code null} when no flash is running. */
    private @Nullable Runnable pendingFlash;
    /** Text to restore when the running flash ends, or {@code null} when no flash is running. */
    private @Nullable CharSequence restoreLabel;

    /**
     * Creates a text action without a click callback. Use {@link #onClick(Runnable)} to attach one
     * after construction, which is useful when the callback needs to reference the action itself.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   action label or translation key
     */
    public MuiTextAction(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label) {
        this(context, theme, label, () -> {});
    }

    /**
     * Creates a text action.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   action label or translation key
     * @param action  click callback
     */
    public MuiTextAction(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, @NotNull Runnable action) {
        super(context);
        this.theme = theme;
        this.text = MuiControls.text(context, theme, label, 12, theme.primary());
        this.text.setPadding(MuiControls.dp(context, 8), MuiControls.dp(context, 4), MuiControls.dp(context, 8), MuiControls.dp(context, 4));
        this.addView(this.text, new FrameLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP));
        this.setOnClickListener(view -> {
            if (this.isEnabled()) action.run();
        });
    }

    /**
     * Creates a text action.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   action label or translation key
     * @param action  click callback
     * @return the action view
     */
    public static @NotNull MuiTextAction create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, @NotNull Runnable action) {
        return new MuiTextAction(context, theme, label, action);
    }

    /**
     * Creates a text action without a click callback.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   action label or translation key
     * @return the action view
     */
    public static @NotNull MuiTextAction create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label) {
        return new MuiTextAction(context, theme, label);
    }

    /**
     * Replaces the label.
     *
     * @param label label or translation key
     * @return this action, for chaining
     */
    public @NotNull MuiTextAction text(@NotNull CharSequence label) {
        this.text.setText(MuiLocalization.resolve(label));
        return this;
    }

    /**
     * Enables or disables the action. A disabled action dims its label and ignores clicks.
     *
     * @param enabled {@code false} to disable
     * @return this action, for chaining
     */
    public @NotNull MuiTextAction enabled(boolean enabled) {
        this.setEnabled(enabled);
        this.text.setTextColor(enabled ? this.theme.primary() : MuiControls.alpha(this.theme.primary(), 0.38F));
        return this;
    }

    /**
     * Returns the current label.
     *
     * @return the label
     */
    public @NotNull CharSequence label() {
        return this.text.getText();
    }

    /**
     * Sets the click callback.
     *
     * @param action click callback
     * @return this action, for chaining
     */
    public @NotNull MuiTextAction onClick(@NotNull Runnable action) {
        this.setOnClickListener(view -> {
            if (this.isEnabled()) action.run();
        });
        return this;
    }

    /**
     * Temporarily replaces the label and restores the previous text after {@code millis}. Intended
     * for click feedback such as "Copied", where the label changes immediately and reverts on its
     * own. Must be called on the UI thread.
     * <p>
     * Calling this again while a flash is running restarts the countdown and keeps the text that
     * was visible before the first call, so repeated clicks never pin the temporary label.
     *
     * @param label  temporary label or translation key
     * @param millis how long the temporary label stays visible, in milliseconds
     * @return this action, for chaining
     */
    public @NotNull MuiTextAction flash(@NotNull CharSequence label, int millis) {
        if (this.restoreLabel == null) this.restoreLabel = this.label();
        this.text(label);

        if (this.pendingFlash != null) this.removeCallbacks(this.pendingFlash);

        Runnable restore = () -> {
            this.pendingFlash = null;
            CharSequence previous = this.restoreLabel;
            this.restoreLabel = null;
            if (previous != null) this.text.setText(previous);
        };
        this.pendingFlash = restore;
        this.postDelayed(restore, Math.max(1, millis));
        return this;
    }

    /**
     * Cancels a pending {@link #flash(CharSequence, int)} restore.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (this.pendingFlash != null) {
            this.removeCallbacks(this.pendingFlash);
            this.pendingFlash = null;
        }
        this.restoreLabel = null;
        super.onDetachedFromWindow();
    }

    /**
     * Returns the label view, for callers that need to style it further.
     *
     * @return the label view
     */
    public @NotNull TextView textView() {
        return this.text;
    }
}