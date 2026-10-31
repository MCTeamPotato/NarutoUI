package me.kall.narutoui.mui.choice;

import me.kall.narutoui.mui.layout.MuiControls;

import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Builds {@link MuiOption} lists from enum constants.
 * <p>
 * Labels use the conventional {@code <prefix>.<constant>} translation key and fall back to a
 * humanized constant name. Constants are values, never ordinals, so a config file that stores
 * {@code FAST} keeps working when constants are reordered.
 */
public final class MuiEnumOptions {
    private MuiEnumOptions() {}

    /**
     * Builds options for every enum constant.
     *
     * @param type      enum class
     * @param keyPrefix translation key prefix, usually {@code modid.config.<path>}
     * @param <E>       enum type
     * @return the options
     */
    public static <E extends Enum<E>> @NotNull List<MuiOption<E>> of(@NotNull Class<E> type, @NotNull String keyPrefix) {
        return of(type, constant -> {
            String key = keyPrefix + "." + constant.name().toLowerCase(Locale.ROOT);
            String resolved = MuiLocalization.resolve(key);
            return resolved.equals(key) ? humanize(constant.name()) : resolved;
        });
    }

    /**
     * Builds options for every enum constant with a custom labeler.
     *
     * @param type    enum class
     * @param labeler label factory
     * @param <E>     enum type
     * @return the options
     */
    public static <E extends Enum<E>> @NotNull List<MuiOption<E>> of(@NotNull Class<E> type, @NotNull Function<E, CharSequence> labeler) {
        E[] constants = type.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException(type + " is not an enum");

        List<MuiOption<E>> options = new ArrayList<>(constants.length);
        for (E constant : constants) options.add(MuiOption.of(constant, labeler.apply(constant)));
        return options;
    }

    /**
     * Turns {@code SOME_NAME} into {@code Some Name}.
     *
     * @param raw constant name
     * @return readable label
     */
    private static @NotNull String humanize(@NotNull String raw) {
        if (raw.isEmpty()) return raw;

        StringBuilder out = new StringBuilder(raw.length() + 8);
        boolean upperNext = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '_' || c == '-' || c == '.') {
                if (!out.isEmpty() && out.charAt(out.length() - 1) != ' ') out.append(' ');
                upperNext = true;
                continue;
            }

            if (upperNext) {
                out.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}