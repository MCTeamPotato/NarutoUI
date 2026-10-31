package me.kall.narutoui.mui.field;

import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.EditText;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A themed single-line or multiline text input with a fluent configuration API.
 * <p>
 * The field is the generic primitive behind string-valued configuration entries. It reports text
 * through {@link #onChange(Consumer)} while the player types and through
 * {@link #onCommit(Consumer)} when the edit is finished (Enter in single-line mode, or focus loss).
 * Configuration screens should normally write through {@code onCommit}, so a half-typed path never
 * reaches the config object.
 * <p>
 * An optional {@link #validator(Predicate)} marks invalid input with an error outline. Invalid input
 * never fires {@code onChange} or {@code onCommit}; the last valid text remains available through
 * {@link #text()}.
 */
public final class MuiTextField extends EditText {
    /** Context used for density conversion. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Rounded input background. */
    private final ShapeDrawable background;
    /** Whether the field currently accepts multiple lines. */
    private boolean multiline;
    /** Guards programmatic text updates. */
    private boolean guard;
    /** Last applied validity, used to avoid redundant stroke updates. */
    private boolean valid = true;
    /** Text at the last commit, used to suppress duplicate commit callbacks. */
    private String committed;
    /** Optional per-keystroke edit listener. */
    private @Nullable Consumer<String> onChange;
    /** Optional edit-finished listener. */
    private @Nullable Consumer<String> onCommit;
    /** Optional validity predicate. */
    private @Nullable Predicate<String> validator;

    /**
     * Creates a text field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial text
     */
    public MuiTextField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence value) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.committed = value.toString();
        this.background = MuiInputSupport.style(this, context, theme, MuiControls.dp(context, 7));
        this.setText(value);

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (MuiTextField.this.guard) return;

                String text = s.toString();
                boolean accepted = MuiTextField.this.validator == null || MuiTextField.this.validator.test(text);
                MuiTextField.this.setValid(accepted);
                if (accepted && MuiTextField.this.onChange != null) MuiTextField.this.onChange.accept(text);
            }
        });

        MuiInputSupport.installCommit(this, () -> !this.multiline, this::commit);
    }

    /**
     * Creates a text field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial text
     * @return the field
     */
    public static @NotNull MuiTextField create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence value) {
        return new MuiTextField(context, theme, value);
    }

    /**
     * Sets the placeholder hint. The value is resolved through {@link MuiLocalization}.
     *
     * @param hint hint text or translation key
     * @return this field, for chaining
     */
    public @NotNull MuiTextField hint(@NotNull CharSequence hint) {
        this.setHint(MuiLocalization.resolve(hint));
        return this;
    }

    /**
     * Enables or disables the field. A disabled field dims its text and background and no longer
     * accepts focus.
     *
     * @param enabled {@code false} to disable
     * @return this field, for chaining
     */
    public @NotNull MuiTextField enabled(boolean enabled) {
        MuiInputSupport.applyEnabled(this, this.background, this.theme, enabled);
        return this;
    }

    /**
     * Switches between single-line and multiline editing. Multiline fields do not commit on Enter;
     * they commit when focus is lost.
     *
     * @param multiline {@code true} for a multiline field
     * @return this field, for chaining
     */
    public @NotNull MuiTextField multiline(boolean multiline) {
        this.multiline = multiline;
        this.setSingleLine(!multiline);
        if (multiline) {
            this.setMinLines(2);
            this.setMaxLines(6);
            this.setGravity(Gravity.TOP | Gravity.START);
        } else {
            this.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        }
        return this;
    }

    /**
     * Limits the text length.
     *
     * @param maxLength maximum number of characters
     * @return this field, for chaining
     */
    public @NotNull MuiTextField maxLength(int maxLength) {
        this.setFilters(new InputFilter.LengthFilter(Math.max(0, maxLength)));
        return this;
    }

    /**
     * Sets the validation predicate. Invalid text is outlined with the theme error color and is not
     * reported through the edit callbacks.
     *
     * @param validator predicate returning {@code true} for acceptable text, or {@code null}
     * @return this field, for chaining
     */
    public @NotNull MuiTextField validator(@Nullable Predicate<String> validator) {
        this.validator = validator;
        this.setValid(validator == null || validator.test(this.getText().toString()));
        return this;
    }

    /**
     * Sets a fixed width in DP. The width is stored on {@code LinearLayout.LayoutParams}, which is
     * also used when the field is later added to a {@link MuiLayout}.
     *
     * @param dp width in density-independent pixels
     * @return this field, for chaining
     */
    public @NotNull MuiTextField width(int dp) {
        MuiInputSupport.applyWidth(this, this.context, dp);
        return this;
    }

    /**
     * Sets the text gravity.
     *
     * @param gravity a {@code Gravity} value
     * @return this field, for chaining
     */
    public @NotNull MuiTextField gravity(int gravity) {
        this.setGravity(gravity);
        return this;
    }

    /**
     * Registers the per-keystroke edit listener. Programmatic {@link MuiTextField#text(CharSequence)} updates do not
     * fire it.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiTextField onChange(@Nullable Consumer<String> listener) {
        this.onChange = listener;
        return this;
    }

    /**
     * Registers the edit-finished listener. It fires on Enter in single-line mode and on focus loss,
     * and only when the text differs from the last committed value.
     *
     * @param listener the listener
     * @return this field, for chaining
     */
    public @NotNull MuiTextField onCommit(@Nullable Consumer<String> listener) {
        this.onCommit = listener;
        return this;
    }

    /**
     * Returns the current text.
     *
     * @return the text
     */
    public @NotNull String text() {
        return this.getText().toString();
    }

    /**
     * Updates the text without firing the edit listeners.
     *
     * @param value the new text
     * @return this field, for chaining
     */
    public @NotNull MuiTextField text(@NotNull CharSequence value) {
        this.guard = true;
        this.setText(value);
        this.guard = false;
        this.committed = value.toString();
        this.setValid(this.validator == null || this.validator.test(this.committed));
        return this;
    }

    /**
     * Commits the current text if it is valid and differs from the last committed value.
     *
     * @return this field, for chaining
     */
    public @NotNull MuiTextField commit() {
        String text = this.getText().toString();
        if (this.validator != null && !this.validator.test(text)) {
            this.setValid(false);
            return this;
        }

        this.setValid(true);
        if (!text.equals(this.committed)) {
            this.committed = text;
            if (this.onCommit != null) this.onCommit.accept(text);
        }
        return this;
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
     * @param valid whether the current text passes validation
     */
    private void setValid(boolean valid) {
        this.valid = MuiInputSupport.applyValid(this.background, this.context, this.theme, valid, this.valid);
    }
}