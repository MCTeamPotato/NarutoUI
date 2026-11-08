package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * OKLab color math for two-color bands.
 * <p>
 * Interpolating in sRGB darkens gradients between complementary colors and makes the midpoint look
 * muddy. OKLab is a perceptual space, so the same two endpoints produce an even, clean band.
 * <p>
 * The generated ramp is mirrored: the first half runs {@code first -> second}, the second half runs
 * {@code second -> first}. The shimmer engine samples its ramp with an oscillating wave, so a
 * mirrored band keeps both chosen colors visible instead of leaving them at the rarely reached ends
 * of the wave.
 * <p>
 * The conversion itself lives in {@link MuiOklch}; this class only exposes the band-shaped API.
 */
public final class MuiColorBands {
    private MuiColorBands() {}

    /**
     * Forces a color opaque.
     *
     * @param color ARGB color
     * @return the color with an alpha channel of {@code 0xFF}
     */
    public static int opaque(int color) {
        return MuiColorMath.opaque(color);
    }

    /**
     * Builds a mirrored OKLab band between two colors.
     *
     * @param first  first color
     * @param second second color
     * @param stops  number of stops, at least 2
     * @return the generated ARGB stops
     */
    public static int @NotNull [] oklabBand(int first, int second, int stops) {
        int count = Math.max(2, stops);
        MuiOklch start = MuiOklch.fromArgb(opaque(first));
        MuiOklch end = MuiOklch.fromArgb(opaque(second));
        int[] colors = new int[count];

        for (int i = 0; i < count; i++) {
            double position = (double) i / (double) (count - 1);
            double amount = 1.0D - Math.abs(position * 2.0D - 1.0D);
            colors[i] = MuiOklch.mix(start, end, amount).toArgb();
        }
        return colors;
    }

    /**
     * Converts an opaque ARGB color to OKLab.
     *
     * @param color ARGB color
     * @return {@code {L, a, b}}
     */
    public static double @NotNull [] toOklab(int color) {
        MuiOklch value = MuiOklch.fromArgb(color);
        double radians = Math.toRadians(value.hue());
        return new double[]{
                value.lightness(),
                value.chroma() * Math.cos(radians),
                value.chroma() * Math.sin(radians)
        };
    }

    /**
     * Converts OKLab back to an opaque ARGB color.
     *
     * @param lightness L component
     * @param a         a component
     * @param b         b component
     * @return the ARGB color
     */
    public static int toArgb(double lightness, double a, double b) {
        double hue = Math.toDegrees(Math.atan2(b, a));
        if (hue < 0.0D) {
            hue += 360.0D;
        }
        return new MuiOklch(lightness, Math.sqrt(a * a + b * b), hue).toArgb();
    }

    /**
     * Formats an opaque color for labels and config text.
     *
     * @param color ARGB color
     * @return {@code #RRGGBB}
     */
    public static @NotNull String toHex(int color) {
        return MuiColorMath.toHex(color);
    }
}
