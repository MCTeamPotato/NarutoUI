package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Settings and color ramp consumed by the generic shimmer engine.
 * <p>
 * The API ships a neutral default; consumers install their own implementation through
 * {@link MuiShimmer#configure(MuiShimmerConfig)}.
 */
public interface MuiShimmerConfig {
    /**
     * Returns whether shimmer is enabled globally.
     *
     * @return the master switch
     */
    boolean text();

    /**
     * Returns whether configuration-screen text should shimmer.
     *
     * @return the config-text switch
     */
    boolean configText();

    /**
     * Returns whether controls and decorations should shimmer.
     *
     * @return the controls switch
     */
    boolean controls();

    /**
     * Returns whether the active palette has an accent color. Shimmer is skipped when it does not.
     *
     * @return {@code true} when a palette accent exists
     */
    default boolean hasAccent() {
        return true;
    }

    /**
     * Returns the configuration shimmer update rate.
     *
     * @return frames per second
     */
    default int fps() {
        return 30;
    }

    /**
     * Returns the shimmer animation speed.
     *
     * @return speed multiplier
     */
    default double speed() {
        return 1.0D;
    }

    /**
     * Returns the spatial density multiplier.
     *
     * @return density multiplier
     */
    default double scale() {
        return 1.0D;
    }

    /**
     * Returns the blend strength.
     *
     * @return strength in {@code 0..1}
     */
    default double strength() {
        return 1.0D;
    }

    /**
     * Returns the nine-stop color ramp used by the wave.
     *
     * @return an array of nine ARGB colors
     */
    default int[] ramp() {
        return new int[]{
                0xFFFFFFFF,
                0xFFFFD24A,
                0xFFFFA726,
                0xFFFF7043,
                0xFFFF5252,
                0xFFFF7043,
                0xFFFFA726,
                0xFFFFD24A,
                0xFFFFFFFF
        };
    }

    /**
     * Returns the neutral default configuration.
     *
     * @return the default config
     */
    @Contract(value = " -> new", pure = true)
    static @NotNull MuiShimmerConfig defaults() {
        return new MuiShimmerConfig() {
            @Override
            public boolean text() {
                return true;
            }

            @Override
            public boolean configText() {
                return true;
            }

            @Override
            public boolean controls() {
                return true;
            }
        };
    }
}