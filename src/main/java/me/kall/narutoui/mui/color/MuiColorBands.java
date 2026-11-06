package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * OKLab color math for two-color bands.
 * <p>
 * Interpolating in sRGB darkens gradients between complementary colors and makes the midpoint look
 * muddy. OKLab is a perceptual space, so the same two endpoints produce an even, clean band. The
 * conversion follows Björn Ottosson's OKLab reference matrices.
 * <p>
 * The generated ramp is mirrored: the first half runs {@code first -> second}, the second half runs
 * {@code second -> first}. The shimmer engine samples its ramp with an oscillating wave, so a
 * mirrored band keeps both chosen colors visible instead of leaving them at the rarely reached ends
 * of the wave.
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
        return color | 0xFF000000;
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
        double[] start = toOklab(opaque(first));
        double[] end = toOklab(opaque(second));
        int[] colors = new int[count];

        for (int i = 0; i < count; i++) {
            double position = (double) i / (double) (count - 1);
            double t = 1.0D - Math.abs(position * 2.0D - 1.0D);
            colors[i] = opaque(toArgb(
                    mix(start[0], end[0], t),
                    mix(start[1], end[1], t),
                    mix(start[2], end[2], t)));
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
        double red = linear((color >> 16) & 0xFF);
        double green = linear((color >> 8) & 0xFF);
        double blue = linear(color & 0xFF);

        double l = 0.4122214708D * red + 0.5363325363D * green + 0.0514459929D * blue;
        double m = 0.2119034982D * red + 0.6806995451D * green + 0.1073969566D * blue;
        double s = 0.0883024619D * red + 0.2817188376D * green + 0.6299787005D * blue;

        double lRoot = Math.cbrt(l);
        double mRoot = Math.cbrt(m);
        double sRoot = Math.cbrt(s);

        return new double[]{
                0.2104542553D * lRoot + 0.7936177850D * mRoot - 0.0040720468D * sRoot,
                1.9779984951D * lRoot - 2.4285922050D * mRoot + 0.4505937099D * sRoot,
                0.0259040371D * lRoot + 0.7827717662D * mRoot - 0.8086757660D * sRoot
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
        double lRoot = lightness + 0.3963377774D * a + 0.2158037573D * b;
        double mRoot = lightness - 0.1055613458D * a - 0.0638541728D * b;
        double sRoot = lightness - 0.0894841775D * a - 1.2914855480D * b;

        double l = lRoot * lRoot * lRoot;
        double m = mRoot * mRoot * mRoot;
        double s = sRoot * sRoot * sRoot;

        double red = 4.0767416621D * l - 3.3077115913D * m + 0.2309699292D * s;
        double green = -1.2684380046D * l + 2.6097574011D * m - 0.3413193965D * s;
        double blue = -0.0041960863D * l - 0.7034186147D * m + 1.7076147010D * s;

        return 0xFF000000 | (encode(red) << 16) | (encode(green) << 8) | encode(blue);
    }

    /**
     * Formats an opaque color for labels and config text.
     *
     * @param color ARGB color
     * @return {@code #RRGGBB}
     */
    public static @NotNull String toHex(int color) {
        return String.format("#%06X", opaque(color) & 0xFFFFFF);
    }

    private static int encode(double linear) {
        double value = Math.max(0.0D, Math.min(1.0D, linear));
        double srgb = value <= 0.0031308D
                ? value * 12.92D
                : 1.055D * Math.pow(value, 1.0D / 2.4D) - 0.055D;
        return (int) Math.round(Math.max(0.0D, Math.min(1.0D, srgb)) * 255.0D);
    }

    private static double linear(int channel) {
        double value = channel / 255.0D;
        return value <= 0.04045D ? value / 12.92D : Math.pow((value + 0.055D) / 1.055D, 2.4D);
    }

    private static double mix(double from, double to, double amount) {
        return from + (to - from) * amount;
    }
}