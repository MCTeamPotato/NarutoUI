package me.kall.narutoui.mui.field;

import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.widget.EditText;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * A themed numeric input that understands integer, long and decimal configuration values.
 * <p>
 * The field keeps an exact {@link BigDecimal} internally, so it can represent a full 64-bit long or
 * a decimal value without the precision loss of a {@code double}. It accepts a leading minus sign
 * and, in {@link Mode#DECIMAL}, a decimal point. Decimal fields can either keep the entered
 * precision or round to a fixed scale. Text is validated while the player types; an out-of-range or
 * unparsable value is outlined with the theme error color and is only corrected (clamped) when the
 * edit is committed with Enter or focus loss.
 * <p>
 * The listener must match the mode: use {@link #onChangeInt(IntConsumer)} or
 * {@link #onChangeLong(LongConsumer)} for {@link Mode#INTEGER}, {@link #onChangeLong(LongConsumer)}
 * for {@link Mode#LONG}, and {@link #onChangeDouble(DoubleConsumer)} for {@link Mode#DECIMAL}.
 */
public final class MuiNumberField extends EditText {
    /** Context used for density conversion. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Rounded input background. */
    private final ShapeDrawable background;
    /** Numeric interpretation of the text. */
    private final Mode mode;
    /** Inclusive lower bound, or {@code null} for no bound. */
    private @Nullable BigDecimal min;
    /** Inclusive upper bound, or {@code null} for no bound. */
    private @Nullable BigDecimal max;
    /** Number of fraction digits used in {@link Mode#DECIMAL}. */
    private int scale;
    /** Last valid value. */
    private BigDecimal current;
    /** Guards programmatic text updates. */
    private boolean guard;
    /** Last applied validity, used to avoid redundant stroke updates. */
    private boolean valid = true;
    /** Optional integer edit listener. */
    private @Nullable IntConsumer intListener;
    /** Optional integer or long edit listener. */
    private @Nullable LongConsumer longListener;
    /** Optional decimal edit listener. */
    private @Nullable DoubleConsumer doubleListener;

    /**
     * Creates an integer field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     */
    public MuiNumberField(@NotNull Context context, @NotNull MuiTheme theme, int value) {
        this(context, theme, BigDecimal.valueOf(value), Mode.INTEGER, 0);
    }

    /**
     * Creates an integer or long field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @param mode    {@link Mode#INTEGER} or {@link Mode#LONG}
     */
    public MuiNumberField(@NotNull Context context, @NotNull MuiTheme theme, long value, @NotNull Mode mode) {
        this(context, theme, BigDecimal.valueOf(value), integerOrLong(mode), 0);
    }

    /**
     * Creates a decimal field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @param scale   number of fraction digits, or a negative value to keep the entered precision
     */
    private MuiNumberField(@NotNull Context context, @NotNull MuiTheme theme, double value, int scale) {
        this(context, theme, BigDecimal.valueOf(value), Mode.DECIMAL, scale);
    }

    /**
     * Creates a field for any mode.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @param mode    numeric interpretation
     * @param scale   fraction digits for decimals
     */
    private MuiNumberField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull BigDecimal value, @NotNull Mode mode, int scale) {
        super(context);
        if (mode != Mode.DECIMAL && scale != 0) throw new IllegalArgumentException("scale only applies to decimals");

        this.context = context;
        this.theme = theme;
        this.mode = mode;
        this.scale = mode == Mode.DECIMAL ? (scale < 0 ? -1 : scale) : 0;
        this.current = this.normalize(value);
        this.min = mode == Mode.INTEGER ? BigDecimal.valueOf(Integer.MIN_VALUE) : mode == Mode.LONG ? BigDecimal.valueOf(Long.MIN_VALUE) : null;
        this.max = mode == Mode.INTEGER ? BigDecimal.valueOf(Integer.MAX_VALUE) : mode == Mode.LONG ? BigDecimal.valueOf(Long.MAX_VALUE) : null;

        this.background = MuiInputSupport.style(this, context, theme, MuiControls.dp(context, 6));
        this.setText(this.format(this.current));
        this.installListeners();
    }

    private static @NotNull Mode integerOrLong(@NotNull Mode mode) {
        if (mode == Mode.DECIMAL) throw new IllegalArgumentException("use create(context, theme, value, scale) for decimals");
        return mode;
    }

    /**
     * Creates an integer field.
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
     * Creates a long field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @return the field
     */
    public static @NotNull MuiNumberField create(@NotNull Context context, @NotNull MuiTheme theme, long value) {
        return new MuiNumberField(context, theme, value, Mode.LONG);
    }

    /**
     * Creates a decimal field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @param scale   number of fraction digits, or a negative value to keep the entered precision
     * @return the field
     */
    public static @NotNull MuiNumberField create(@NotNull Context context, @NotNull MuiTheme theme, double value, int scale) {
        return new MuiNumberField(context, theme, value, scale);
    }

    /**
     * Installs the text, focus and key listeners shared by all modes.
     */
    private void installListeners() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (MuiNumberField.this.guard) return;

                String text = s.toString();
                if (text.isEmpty() || text.equals("-") || text.equals("-.") || text.endsWith(".")) {
                    MuiNumberField.this.setValid(true);
                    return;
                }

                BigDecimal parsed;
                try {
                    parsed = new BigDecimal(text);
                } catch (NumberFormatException ignored) {
                    MuiNumberField.this.setValid(false);
                    return;
                }

                if (!MuiNumberField.this.inBounds(parsed)) {
                    MuiNumberField.this.setValid(false);
                    return;
                }

                MuiNumberField.this.setValid(true);
                BigDecimal next = MuiNumberField.this.normalize(parsed);
                if (next.compareTo(MuiNumberField.this.current) != 0) {
                    MuiNumberField.this.current = next;
                    MuiNumberField.this.fire(next);
                }
            }
        });

        MuiInputSupport.installCommit(this, () -> true, this::commit);
    }

    /**
     * Enables or disables the field. A disabled field dims its text and background and no longer
     * accepts focus.
     *
     * @param enabled {@code false} to disable
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField enabled(boolean enabled) {
        MuiInputSupport.applyEnabled(this, this.background, this.theme, enabled);
        return this;
    }

    /**
     * Sets the inclusive integer bounds and clamps the current value.
     *
     * @param min lower bound
     * @param max upper bound
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField bounds(int min, int max) {
        return this.bounds(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
    }

    /**
     * Sets the inclusive long bounds and clamps the current value.
     *
     * @param min lower bound
     * @param max upper bound
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField bounds(long min, long max) {
        return this.bounds(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
    }

    /**
     * Sets the inclusive decimal bounds and clamps the current value.
     *
     * @param min lower bound
     * @param max upper bound
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField bounds(double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max)) return this;
        return this.bounds(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
    }

    /**
     * Sets the inclusive bounds and clamps the current value.
     *
     * @param min lower bound, or {@code null} for no bound
     * @param max upper bound, or {@code null} for no bound
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField bounds(@Nullable BigDecimal min, @Nullable BigDecimal max) {
        this.min = min;
        this.max = max != null && min != null && max.compareTo(min) < 0 ? min : max;
        this.value(this.current);
        return this;
    }

    /**
     * Sets the number of fraction digits for {@link Mode#DECIMAL}. A negative value keeps the
     * entered precision instead of rounding to a fixed scale.
     *
     * @param scale fraction digits, or a negative value for unlimited precision
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField scale(int scale) {
        this.scale = scale < 0 ? -1 : scale;
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
        MuiInputSupport.applyWidth(this, this.context, dp);
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
     * Registers the integer edit listener. This is the convenience overload for
     * {@link Mode#INTEGER} fields and is not fired for {@link Mode#LONG} or {@link Mode#DECIMAL}.
     * Programmatic {@link #value(long)} updates do not fire it.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField onChangeInt(@Nullable IntConsumer listener) {
        this.intListener = listener;
        return this;
    }

    /**
     * Registers the integer or long edit listener. Programmatic {@link #value(long)} updates do not
     * fire it.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField onChangeLong(@Nullable LongConsumer listener) {
        this.longListener = listener;
        return this;
    }

    /**
     * Registers the decimal edit listener. Programmatic {@link #value(double)} updates do not fire
     * it.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField onChangeDouble(@Nullable DoubleConsumer listener) {
        this.doubleListener = listener;
        return this;
    }

    /**
     * Returns the current value as a long.
     *
     * @return the current value
     */
    public long longValue() {
        return this.current.longValue();
    }

    /**
     * Returns the current value as a double.
     *
     * @return the current value
     */
    public double doubleValue() {
        return this.current.doubleValue();
    }

    /**
     * Returns the current value exactly.
     *
     * @return the current value
     */
    public @NotNull BigDecimal decimalValue() {
        return this.current;
    }

    /**
     * Updates the value without firing the edit listener.
     *
     * @param value the new value, clamped to the configured bounds
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField value(long value) {
        return this.value(BigDecimal.valueOf(value));
    }

    /**
     * Updates the value without firing the edit listener.
     *
     * @param value the new value, clamped to the configured bounds
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField value(double value) {
        if (!Double.isFinite(value)) return this;
        return this.value(BigDecimal.valueOf(value));
    }

    /**
     * Updates the value without firing the edit listener.
     *
     * @param value the new value, clamped to the configured bounds
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField value(@NotNull BigDecimal value) {
        this.current = this.clamp(this.normalize(value));
        this.setTextSilently(this.format(this.current));
        this.setValid(true);
        return this;
    }

    /**
     * Commits the current text. An empty, unparsable or out-of-range value is corrected to the last
     * valid value, or clamped into range when it is a valid number.
     *
     * @return this field, for chaining
     */
    public @NotNull MuiNumberField commit() {
        String text = this.getText().toString();
        BigDecimal parsed = null;
        try {
            parsed = text.isEmpty() ? null : new BigDecimal(text);
        } catch (NumberFormatException ignored) {
        }

        BigDecimal next = parsed == null ? this.current : this.clamp(this.normalize(parsed));
        if (next.compareTo(this.current) != 0) {
            this.current = next;
            this.fire(next);
        }

        this.setTextSilently(this.format(this.current));
        this.setValid(true);
        return this;
    }

    /**
     * Normalizes a value for the current mode.
     *
     * @param value the value
     * @return the normalized value
     */
    private @NotNull BigDecimal normalize(@NotNull BigDecimal value) {
        if (this.mode != Mode.DECIMAL) return value.setScale(0, RoundingMode.HALF_UP);
        return this.scale < 0 ? value.stripTrailingZeros() : value.setScale(this.scale, RoundingMode.HALF_UP);
    }

    /**
     * Clamps a value into the configured bounds.
     *
     * @param value the value
     * @return the clamped value
     */
    private @NotNull BigDecimal clamp(@NotNull BigDecimal value) {
        BigDecimal result = value;
        if (this.min != null && result.compareTo(this.min) < 0) result = this.min;
        if (this.max != null && result.compareTo(this.max) > 0) result = this.max;
        return result;
    }

    /**
     * Returns whether a value is inside the configured bounds.
     *
     * @param value the value
     * @return {@code true} when the value is in range
     */
    private boolean inBounds(@NotNull BigDecimal value) {
        return (this.min == null || value.compareTo(this.min) >= 0) && (this.max == null || value.compareTo(this.max) <= 0);
    }

    /**
     * Formats a value for display.
     *
     * @param value the value
     * @return the display text
     */
    private @NotNull String format(@NotNull BigDecimal value) {
        return this.mode == Mode.DECIMAL ? value.stripTrailingZeros().toPlainString() : value.toBigInteger().toString();
    }

    /**
     * Updates the text without notifying the edit listeners.
     *
     * @param text the new text
     */
    private void setTextSilently(@NotNull CharSequence text) {
        if (this.getText().toString().contentEquals(text)) return;
        this.guard = true;
        this.setText(text);
        this.guard = false;
    }

    /**
     * Notifies the listeners for the current mode.
     *
     * @param value the new value
     */
    private void fire(@NotNull BigDecimal value) {
        if (this.mode == Mode.DECIMAL) {
            if (this.doubleListener != null) this.doubleListener.accept(value.doubleValue());
        } else if (this.mode == Mode.INTEGER) {
            if (this.intListener != null) this.intListener.accept(value.intValue());
            else if (this.longListener != null) this.longListener.accept(value.longValue());
        } else if (this.longListener != null) {
            this.longListener.accept(value.longValue());
        }
    }

    /**
     * Commits a pending edit when the field leaves the window, so closing or switching a page does
     * not silently drop typed text.
     */
    @Override
    protected void onDetachedFromWindow() {
        this.commit();
        super.onDetachedFromWindow();
    }

    /**
     * Applies the outline color for the current validity.
     *
     * @param valid whether the current text is a valid in-range number
     */
    private void setValid(boolean valid) {
        this.valid = MuiInputSupport.applyValid(this.background, this.context, this.theme, valid, this.valid);
    }

    /**
     * Numeric interpretation of the field text.
     */
    public enum Mode {
        /** 32-bit integer. */
        INTEGER,
        /** 64-bit integer. */
        LONG,
        /** Decimal with a configurable number of fraction digits. */
        DECIMAL
    }
}