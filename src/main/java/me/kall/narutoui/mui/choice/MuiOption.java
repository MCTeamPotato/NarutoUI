package me.kall.narutoui.mui.choice;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One value in a selection control: a value, its label, and optional presentation metadata.
 * <p>
 * The same model is used by {@link MuiDropdown} and {@link MuiChoiceGrid}, so an enum or config
 * value can switch presentation without rebuilding its option list. The preview is optional; a
 * dropdown ignores it, while a grid uses it as the card artwork.
 *
 * @param value       option value
 * @param label       label or translation key
 * @param description optional description or translation key
 * @param enabled     whether the option can be selected
 * @param badge       optional short badge such as "Default" or "Restart", or {@code null}
 * @param preview     optional card preview, or {@code null}
 * @param <T>         value type
 */
public record MuiOption<T>(@NotNull T value, @NotNull CharSequence label, @Nullable CharSequence description, boolean enabled, @Nullable CharSequence badge, @Nullable Preview preview) {
    /**
     * Builds a card preview. A preview receives the active API theme; theme presets can use the
     * candidate theme they are previewing instead.
     */
    @FunctionalInterface
    public interface Preview {
        /**
         * Builds the preview view.
         *
         * @param context the screen context
         * @param theme   active API theme
         * @return the preview view
         */
        @NotNull View create(@NotNull Context context, @NotNull MuiTheme theme);
    }

    /**
     * Creates an enabled option without description, badge or preview.
     *
     * @param value option value
     * @param label label or translation key
     * @param <T>   value type
     * @return the option
     */
    public static <T> @NotNull MuiOption<T> of(@NotNull T value, @NotNull CharSequence label) {
        return new MuiOption<>(value, label, null, true, null, null);
    }

    /**
     * Creates an enabled option with a description.
     *
     * @param value       option value
     * @param label       label or translation key
     * @param description optional description or translation key
     * @param <T>         value type
     * @return the option
     */
    public static <T> @NotNull MuiOption<T> of(@NotNull T value, @NotNull CharSequence label, @Nullable CharSequence description) {
        return new MuiOption<>(value, label, description, true, null, null);
    }

    /**
     * Returns a copy with a description.
     *
     * @param description description or translation key, or {@code null}
     * @return the new option
     */
    public @NotNull MuiOption<T> description(@Nullable CharSequence description) {
        return new MuiOption<>(this.value, this.label, description, this.enabled, this.badge, this.preview);
    }

    /**
     * Returns a copy with a different enabled state.
     *
     * @param enabled {@code false} to disable selection
     * @return the new option
     */
    public @NotNull MuiOption<T> enabled(boolean enabled) {
        return new MuiOption<>(this.value, this.label, this.description, enabled, this.badge, this.preview);
    }

    /**
     * Returns a copy with a badge.
     *
     * @param badge badge text or translation key, or {@code null}
     * @return the new option
     */
    public @NotNull MuiOption<T> badge(@Nullable CharSequence badge) {
        return new MuiOption<>(this.value, this.label, this.description, this.enabled, badge, this.preview);
    }

    /**
     * Returns a copy with a preview.
     *
     * @param preview preview factory, or {@code null}
     * @return the new option
     */
    public @NotNull MuiOption<T> preview(@Nullable Preview preview) {
        return new MuiOption<>(this.value, this.label, this.description, this.enabled, this.badge, preview);
    }
}