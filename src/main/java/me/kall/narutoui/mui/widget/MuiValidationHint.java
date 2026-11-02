package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.field.MuiValidatable;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * A small error label that follows a {@link MuiValidatable} field.
 * <p>
 * The hint is hidden while the field is valid and becomes visible as soon as the field reports an
 * invalid value. It is intentionally a separate view so callers can place it exactly where the row
 * needs it, including below a custom field row.
 */
public final class MuiValidationHint extends TextView {
    /** Field whose validity controls this label. */
    private final MuiValidatable field;

    /**
     * Creates a validation hint.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param field   field to observe
     * @param message message shown while the field is invalid
     */
    public MuiValidationHint(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiValidatable field, @NotNull CharSequence message) {
        super(context);
        this.field = field;
        this.setTextSize(10);
        this.setTextColor(theme.error());
        this.setPadding(MuiControls.dp(context, 2), MuiControls.dp(context, 2), MuiControls.dp(context, 2), 0);
        this.message(message);
        this.setVisibility(field.isValid() ? View.GONE : View.VISIBLE);
        field.onValidityChanged(valid -> this.setVisibility(valid ? View.GONE : View.VISIBLE));
    }

    /**
     * Creates a validation hint.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param field   field to observe
     * @param message message shown while the field is invalid
     * @return the hint
     */
    public static @NotNull MuiValidationHint create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiValidatable field, @NotNull CharSequence message) {
        return new MuiValidationHint(context, theme, field, message);
    }

    /**
     * Replaces the message shown while the field is invalid.
     *
     * @param message message text or translation key
     * @return this hint, for chaining
     */
    public @NotNull MuiValidationHint message(@NotNull CharSequence message) {
        this.setText(MuiLocalization.resolve(message));
        return this;
    }

    /**
     * Returns the observed field.
     *
     * @return the field
     */
    public @NotNull MuiValidatable field() {
        return this.field;
    }
}