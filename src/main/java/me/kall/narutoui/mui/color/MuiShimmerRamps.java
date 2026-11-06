package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * Builders for rich nine-stop shimmer ramps.
 * <p>
 * A flat two-color band is readable but visually thin: the wave only travels between two tones.
 * These helpers keep the same mirrored shape as the hand-tuned Naruto ramp while adding light tints
 * and a same-family deep stop, so other palettes get comparable depth:
 * <pre>
 * light2 → light1 → accentLight → accent → deep → accent → accentLight → light1 → light2
 * </pre>
 */
public final class MuiShimmerRamps {
    private static final float[] STOPS = {0.0F, 0.12F, 0.25F, 0.40F, 0.55F, 0.70F, 0.82F, 0.92F, 1.0F};

    private MuiShimmerRamps() {}

    /**
     * Builds a rich ramp from one accent, deriving a deeper same-family stop.
     *
     * @param accent core accent color
     * @return nine ARGB stops
     */
    public static int @NotNull [] rich(int accent) {
        return rich(accent, mix(accent, 0xFF000000, 0.40D));
    }

    /**
     * Builds a rich ramp from an accent and a deeper same-family color.
     *
     * @param accent core accent color
     * @param deep   deeper auxiliary color
     * @return nine ARGB stops
     */
    public static int @NotNull [] rich(int accent, int deep) {
        int core = MuiColorBands.opaque(accent);
        int shadow = MuiColorBands.opaque(deep);
        int light2 = mix(core, 0xFFFFFFFF, 0.75D);
        int light1 = mix(core, 0xFFFFFFFF, 0.45D);
        int accentLight = mix(core, 0xFFFFFFFF, 0.20D);

        return new int[]{
                light2,
                light1,
                accentLight,
                core,
                shadow,
                core,
                accentLight,
                light1,
                light2
        };
    }

    /**
     * Builds a neutral white-to-gray ramp for themes without an accent.
     *
     * @return nine ARGB stops
     */
    public static int @NotNull [] neutral() {
        return new int[]{
                0xFFFFFFFF, 0xFFE6E6E6, 0xFFD0D0D0, 0xFFB8B8B8, 0xFF8E8E8E,
                0xFFB8B8B8, 0xFFD0D0D0, 0xFFE6E6E6, 0xFFFFFFFF
        };
    }

    /**
     * Samples a nine-stop ramp at a wave position.
     *
     * @param ramp     nine ARGB stops
     * @param position wave position in {@code 0..1}
     * @return the interpolated color
     */
    public static int sample(int @NotNull [] ramp, float position) {
        float value = Math.max(0.0F, Math.min(1.0F, position));
        for (int i = 1; i < STOPS.length; i++) {
            if (value <= STOPS[i]) {
                float span = STOPS[i] - STOPS[i - 1];
                float fraction = span <= 0.0F ? 0.0F : (value - STOPS[i - 1]) / span;
                return mix(ramp[i - 1], ramp[i], fraction);
            }
        }
        return ramp[ramp.length - 1];
    }

    private static int mix(int from, int to, double amount) {
        double a = Math.max(0.0D, Math.min(1.0D, amount));
        int alpha = (int) Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * a);
        int red = (int) Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * a);
        int green = (int) Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * a);
        int blue = (int) Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * a);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}