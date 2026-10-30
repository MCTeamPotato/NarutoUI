package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A small primary-colored text action. The label is created through {@link MuiControls}, so it uses
 * the active widget factory and participates in shimmer exactly like other text controls.
 * <p>
 * The action is a small container instead of a raw {@link TextView}; this keeps the click target
 * stable and allows the label to be replaced after the action is created.
 */
public final class MuiTextAction extends FrameLayout {
    /** Label view created by the active widget factory. */
    private final TextView text;

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
    public MuiTextAction(@NotNull Context context, @NotNull MuiTheme theme,
                         @NotNull CharSequence label, @NotNull Runnable action) {
        super(context);
        this.text = MuiControls.text(context, theme, label, 12, theme.primary());
        this.text.setPadding(MuiControls.dp(context, 8), MuiControls.dp(context, 4),
                MuiControls.dp(context, 8), MuiControls.dp(context, 4));
        addView(this.text, new FrameLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP));
        setOnClickListener(view -> action.run());
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
    public static @NotNull MuiTextAction create(@NotNull Context context, @NotNull MuiTheme theme,
                                                @NotNull CharSequence label, @NotNull Runnable action) {
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
    public static @NotNull MuiTextAction create(@NotNull Context context, @NotNull MuiTheme theme,
                                                @NotNull CharSequence label) {
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
     * Sets the click callback.
     *
     * @param action click callback
     * @return this action, for chaining
     */
    public @NotNull MuiTextAction onClick(@NotNull Runnable action) {
        setOnClickListener(view -> action.run());
        return this;
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