package me.kall.narutoui.mui;

import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Small localization helper used by the API. Public labels accept either literal text or a
 * translation key; keys are resolved at build time with a literal fallback.
 */
public final class MuiLocalization {
    private MuiLocalization() {}

    /**
     * Resolves a translation key, or returns the original text when no translation exists.
     *
     * @param text literal text or translation key
     * @return localized text
     */
    public static @NotNull String resolve(@NotNull CharSequence text) {
        String key = text.toString();
        if (key.isEmpty()) return key;
        try {
            return I18n.exists(key) ? I18n.get(key) : key;
        } catch (Throwable ignored) {
            return key;
        }
    }

    /**
     * Resolves an optional label.
     *
     * @param text literal text or translation key, or {@code null}
     * @return localized text, or {@code null}
     */
    public static @Nullable String resolveNullable(@Nullable CharSequence text) {
        return text == null ? null : resolve(text);
    }

    /**
     * Resolves a translation key with arguments, falling back to the key itself.
     *
     * @param key  translation key
     * @param args format arguments
     * @return localized text
     */
    public static @NotNull String get(@NotNull String key, @NotNull Object... args) {
        try {
            return I18n.exists(key) ? I18n.get(key, args) : key;
        } catch (Throwable ignored) {
            return key;
        }
    }

    /**
     * Returns a resolved translation value suitable for any label parameter.
     *
     * @param key  translation key
     * @param args format arguments
     * @return localized text
     */
    public static @NotNull @Unmodifiable CharSequence translatable(@NotNull String key, @NotNull Object... args) {
        return get(key, args);
    }
}