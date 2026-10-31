package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;

/**
 * A small integer input with a themed rounded background and a fluent configuration API.
 * <p>
 * The field accepts digits only, clamps values to the configured range and reports every valid
 * edit through {@link #onChange(IntConsumer)}. It is intentionally independent from any editor
 * model, so it can be dropped into {@link MuiLayout} chains or custom rows.
 */
public final class MuiNumberField extends EditText {
    /** Context used for density conversion. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Rounded input background. */
    private final ShapeDrawable background;
    /** Inclusive lower bound. */
    private int min = 0;
    /** Inclusive upper bound. */
    private int max = Integer.MAX_VALUE;
    /** Last valid value. */
    private int current;
    /** Guards programmatic text updates. */
    private boolean guard;
    /** Optional edit listener. */
    private @Nullable IntConsumer onChange;

    /**
     * Creates a number field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     */
    public MuiNumberField(@NotNull Context context, @NotNull MuiTheme theme, int value) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.current = value;

        this.setSingleLine();
        this.setTextSize(14);
        this.setTextColor(theme.onSurface());
        this.setText(String.valueOf(value));

        this.background = new ShapeDrawable();
        this.background.setCornerRadius(MuiControls.dp(context, 8));
        this.background.setColor(theme.surfaceHigh());
        this.setBackground(this.background);
        this.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 6), MuiControls.dp(context, 10), MuiControls.dp(context, 6));

        this.setFilters((source, start, end, dest, dStart, dEnd) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) return "";
            }
            return null;
        });

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (MuiNumberField.this.guard || s.isEmpty()) return;

                int parsed;
                try {
                    parsed = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignored) {
                    return;
                }

                int clamped = MuiNumberField.this.clamp(parsed);
                MuiNumberField.this.current = clamped;
                if (MuiNumberField.this.onChange != null) {
                    MuiNumberField.this.onChange.accept(clamped);
                }

                if (clamped != parsed) {
                    MuiNumberField.this.guard = true;
                    MuiNumberField.this.post(() -> {
                        MuiNumberField.this.setText(String.valueOf(clamped));
                        MuiNumberField.this.guard = false;
                    });
                }
            }
        });
    }

    /**
     * Creates a number field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @return the field
     */
    public static @NotNull MuiNumberField create(@NotNull Context context, @NotNull MuiTheme theme, int value) {
        return new MuiNumberField(context, theme, value);
    }

    /**
     * Enables or disables the field. A disabled field dims its text and background and no longer
     * accepts focus.
     *
     * @param enabled {@code false} to disable
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField enabled(boolean enabled) {
        this.setEnabled(enabled);
        this.setFocusable(enabled);
        this.setFocusableInTouchMode(enabled);
        this.setTextColor(enabled ? this.theme.onSurface() : MuiControls.alpha(this.theme.onSurface(), 0.38F));
        this.background.setColor(enabled ? this.theme.surfaceHigh() : MuiControls.alpha(this.theme.surfaceHigh(), 0.5F));
        return this;
    }

    /**
     * Sets the inclusive bounds and clamps the current value.
     *
     * @param min lower bound
     * @param max upper bound
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField bounds(int min, int max) {
        this.min = min;
        this.max = Math.max(min, max);
        this.value(this.current);
        return this;
    }

    /**
     * Sets a fixed width in DP. The width is stored on {@code LinearLayout.LayoutParams}, which is
     * also used when the field is later added to a {@link MuiLayout}.
     *
     * @param dp width in density-independent pixels
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField width(int dp) {
        ViewGroup.LayoutParams currentParams = this.getLayoutParams();
        LinearLayout.LayoutParams params;
        if (currentParams instanceof LinearLayout.LayoutParams linear) {
            params = linear;
        } else if (currentParams instanceof ViewGroup.MarginLayoutParams margins) {
            params = new LinearLayout.LayoutParams(margins);
        } else if (currentParams != null) {
            params = new LinearLayout.LayoutParams(currentParams);
        } else {
            params = new LinearLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP);
        }
        params.width = MuiControls.dp(this.context, dp);
        this.setLayoutParams(params);
        return this;
    }

    /**
     * Sets the text gravity.
     *
     * @param gravity a {@code Gravity} value
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField gravity(int gravity) {
        this.setGravity(gravity);
        return this;
    }

    /**
     * Registers the edit listener. Programmatic {@link #value(int)} updates do not fire it.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField onChange(@NotNull IntConsumer listener) {
        this.onChange = listener;
        return this;
    }

    /**
     * Returns the last valid value.
     *
     * @return the value
     */
    public int value() {
        return this.current;
    }

    /**
     * Updates the value without notifying the edit listener.
     *
     * @param value the new value, clamped to the configured bounds
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField value(int value) {
        int clamped = this.clamp(value);
        this.current = clamped;
        if (!this.getText().toString().equals(String.valueOf(clamped))) {
            this.guard = true;
            this.setText(String.valueOf(clamped));
            this.guard = false;
        }
        return this;
    }

    /**
     * Clamps a value to the configured range.
     *
     * @param value the input
     * @return the clamped value
     */
    private int clamp(int value) {
        return Math.max(this.min, Math.min(this.max, value));
    }
}