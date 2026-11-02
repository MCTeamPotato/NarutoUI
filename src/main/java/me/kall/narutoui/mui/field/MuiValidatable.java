package me.kall.narutoui.mui.field;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A field that reports whether its current text is valid and lets callers observe validity changes.
 * <p>
 * This is the contract used by {@link me.kall.narutoui.mui.widget.MuiValidationHint}. Both
 * {@link MuiTextField} and {@link MuiNumberField} implement it, so a hint can be attached to either
 * without knowing the concrete field type.
 */
public interface MuiValidatable {
    /**
     * Returns whether the text currently shown in the field is valid.
     *
     * @return {@code true} when the current text passes the field's checks
     */
    boolean isValid();

    /**
     * Registers a listener invoked whenever the field's validity changes. The listener is invoked
     * immediately with the current state, then on every change.
     *
     * @param listener validity listener
     */
    void onValidityChanged(@NotNull Consumer<Boolean> listener);
}