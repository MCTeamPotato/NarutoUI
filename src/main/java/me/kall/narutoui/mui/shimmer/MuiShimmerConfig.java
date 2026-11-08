package me.kall.narutoui.mui.shimmer;

import me.kall.narutoui.mui.color.MuiColorSchemes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Global settings and color ramp consumed by the generic shimmer engine.
 * <p>
 * The API ships a neutral default; consumers install their own implementation through
 * {@link MuiShimmer#configure(MuiShimmerConfig)}. This interface only controls whether shimmer
 * exists at all and how it looks; whether an individual component shimmers is decided by its
 * {@link MuiShimmerable} marker.
 */
public interface MuiShimmerConfig {
    /**
     * Returns whether shimmer is enabled globally.
     *
     * @return the master switch
     */
    boolean enabled();

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
     * <p>
     * The default is the Naruto scheme's own ramp, so there is one definition of the signature
     * white/gold/orange/red wave instead of a second copy here.
     *
     * @return an array of nine ARGB colors
     */
    default int[] ramp() {
        return MuiColorSchemes.NARUTO.shimmerRamp();
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
            public boolean enabled() {
                return true;
            }
        };
    }
}