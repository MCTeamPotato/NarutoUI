package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * Plain ARGB helpers shared by the palette generator, the shimmer ramp and the widget factories.
 * <p>
 * Everything works on packed ARGB integers so results are identical on every platform and no
 * {@code java.awt} class is required. Contrast follows WCAG 2.1 relative luminance.
 */
public final class MuiColorMath {
    /** Opaque alpha. */
    public static final int OPAQUE = 0xFF000000;

    private MuiColorMath() {}

    /**
     * Forces a color opaque.
     *
     * @param color ARGB color
     * @return the color with an alpha channel of {@code 0xFF}
     */
    public static int opaque(int color) {
        return color | OPAQUE;
    }

    /**
     * Replaces the alpha channel.
     *
     * @param color ARGB color
     * @param alpha alpha in {@code 0..255}
     * @return the color with the given alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (color & 0xFFFFFF);
    }

    /**
     * Returns the WCAG relative luminance of a color.
     *
     * @param color ARGB color
     * @return the relative luminance, ignoring alpha
     */
    public static double luminance(int color) {
        return 0.2126D * toLinear((color >> 16) & 0xFF)
                + 0.7152D * toLinear((color >> 8) & 0xFF)
                + 0.0722D * toLinear(color & 0xFF);
    }

    /**
     * WCAG contrast ratio between two colors.
     *
     * @param first  first color
     * @param second second color
     * @return the ratio in {@code 1..21}
     */
    public static double contrast(int first, int second) {
        double a = luminance(first);
        double b = luminance(second);
        return (Math.max(a, b) + 0.05D) / (Math.min(a, b) + 0.05D);
    }

    /**
     * Perceptual distance in OKLab.
     *
     * @param first  first color
     * @param second second color
     * @return the distance; {@code 0.02} is roughly one just-noticeable difference
     */
    public static double deltaE(int first, int second) {
        MuiOklch a = MuiOklch.fromArgb(first);
        MuiOklch b = MuiOklch.fromArgb(second);
        double aA = a.chroma() * Math.cos(Math.toRadians(a.hue()));
        double aB = a.chroma() * Math.sin(Math.toRadians(a.hue()));
        double bA = b.chroma() * Math.cos(Math.toRadians(b.hue()));
        double bB = b.chroma() * Math.sin(Math.toRadians(b.hue()));
        double deltaL = a.lightness() - b.lightness();
        double deltaA = aA - bA;
        double deltaB = aB - bB;
        return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    }

    /**
     * Interpolates two colors in OKLab.
     *
     * @param from   start color
     * @param to     end color
     * @param amount blend amount, clamped to {@code 0..1}
     * @return the interpolated color
     */
    public static int mix(int from, int to, double amount) {
        return MuiOklch.mix(MuiOklch.fromArgb(from), MuiOklch.fromArgb(to), amount).toArgb();
    }

    /**
     * Moves a foreground color away from a background until the pair reaches a contrast target.
     * <p>
     * Lightness only moves in the direction it already leans, so the hue and the color's role are
     * preserved and a surface that already passes is returned unchanged.
     *
     * @param foreground the color to adjust
     * @param background the color it is read against
     * @param ratio      minimum contrast ratio
     * @return the adjusted color
     */
    public static int enforceContrast(int foreground, int background, double ratio) {
        if (contrast(foreground, background) >= ratio) {
            return foreground;
        }

        MuiOklch color = MuiOklch.fromArgb(foreground);
        double direction = color.lightness() >= MuiOklch.fromArgb(background).lightness() ? 1.0D : -1.0D;
        double lightness = color.lightness();

        for (int i = 0; i < 120; i++) {
            lightness = clamp(lightness + direction * 0.01D, 0.02D, 0.995D);
            int candidate = color.withLightness(lightness).toArgb();
            if (contrast(candidate, background) >= ratio) {
                return candidate;
            }
            if (lightness <= 0.02D || lightness >= 0.995D) {
                break;
            }
        }
        return color.withLightness(lightness).toArgb();
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

    /**
     * Signed shortest distance from one hue to another.
     *
     * @param from source hue in degrees
     * @param to   target hue in degrees
     * @return the delta in {@code -180..180}
     */
    public static double shortestHueDelta(double from, double to) {
        return ((to - from) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    /**
     * Wraps a hue into {@code 0..360}.
     *
     * @param hue hue in degrees
     * @return the normalized hue
     */
    public static double normalizeHue(double hue) {
        double value = hue % 360.0D;
        return value < 0.0D ? value + 360.0D : value;
    }

    /**
     * Converts an 8-bit sRGB channel to linear light.
     *
     * @param channel channel value in {@code 0..255}
     * @return the linear-light value in {@code 0..1}
     */
    public static double toLinear(int channel) {
        double value = channel / 255.0D;
        return value <= 0.04045D ? value / 12.92D : Math.pow((value + 0.055D) / 1.055D, 2.4D);
    }

    /**
     * Converts a linear-light channel back to 8-bit sRGB.
     *
     * @param linear linear-light channel
     * @return the 8-bit sRGB channel
     */
    public static int encode(double linear) {
        double value = clamp(linear, 0.0D, 1.0D);
        double encoded = value <= 0.0031308D ? value * 12.92D : 1.055D * Math.pow(value, 1.0D / 2.4D) - 0.055D;
        return (int) Math.round(clamp(encoded, 0.0D, 1.0D) * 255.0D);
    }

    /**
     * Clamps a value into a range.
     *
     * @param value input value
     * @param low   lower bound
     * @param high  upper bound
     * @return the value clamped to {@code [low, high]}
     */
    public static double clamp(double value, double low, double high) {
        return value < low ? low : (value > high ? high : value);
    }

    /**
     * Clamps a value into a range.
     *
     * @param value input value
     * @param low   lower bound
     * @param high  upper bound
     * @return the value clamped to {@code [low, high]}
     */
    public static int clamp(int value, int low, int high) {
        return value < low ? low : (value > high ? high : value);
    }
}
