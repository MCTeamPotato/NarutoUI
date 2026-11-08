package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * A color in OKLCH: perceptual lightness, chroma and hue.
 * <p>
 * OKLab is perceptually uniform, so interpolating or comparing two colors here gives results that
 * match what the eye sees; sRGB interpolation between distant hues goes through a muddy midpoint.
 * The conversion follows Björn Ottosson's OKLab reference matrices.
 * <p>
 * {@link #toArgb()} holds lightness and hue and reduces chroma until the color fits sRGB, so a
 * generated color never clips a channel and shifts hue.
 *
 * @param lightness perceptual lightness in {@code 0..1}
 * @param chroma    chroma, {@code 0} for neutral
 * @param hue       hue in degrees, {@code 0..360}
 */
public record MuiOklch(double lightness, double chroma, double hue) {

    /**
     * Converts an opaque ARGB color to OKLCH.
     *
     * @param argb ARGB color; alpha is ignored
     * @return the OKLCH value
     */
    public static @NotNull MuiOklch fromArgb(int argb) {
        double red = MuiColorMath.toLinear((argb >> 16) & 0xFF);
        double green = MuiColorMath.toLinear((argb >> 8) & 0xFF);
        double blue = MuiColorMath.toLinear(argb & 0xFF);

        double l = 0.4122214708D * red + 0.5363325363D * green + 0.0514459929D * blue;
        double m = 0.2119034982D * red + 0.6806995451D * green + 0.1073969566D * blue;
        double s = 0.0883024619D * red + 0.2817188376D * green + 0.6299787005D * blue;

        double lRoot = Math.cbrt(l);
        double mRoot = Math.cbrt(m);
        double sRoot = Math.cbrt(s);

        double lightness = 0.2104542553D * lRoot + 0.7936177850D * mRoot - 0.0040720468D * sRoot;
        double a = 1.9779984951D * lRoot - 2.4285922050D * mRoot + 0.4505937099D * sRoot;
        double b = 0.0259040371D * lRoot + 0.7827717662D * mRoot - 0.8086757660D * sRoot;

        double chroma = Math.sqrt(a * a + b * b);
        double hue = Math.toDegrees(Math.atan2(b, a));
        if (hue < 0.0D) {
            hue += 360.0D;
        }
        return new MuiOklch(lightness, chroma, hue);
    }

    /**
     * Converts this value to an opaque ARGB color, reducing chroma until it fits sRGB.
     *
     * @return the opaque ARGB color
     */
    public int toArgb() {
        MuiOklch fitted = this.fit();
        double radians = Math.toRadians(fitted.hue);
        double a = fitted.chroma * Math.cos(radians);
        double b = fitted.chroma * Math.sin(radians);

        double lRoot = fitted.lightness + 0.3963377774D * a + 0.2158037573D * b;
        double mRoot = fitted.lightness - 0.1055613458D * a - 0.0638541728D * b;
        double sRoot = fitted.lightness - 0.0894841775D * a - 1.2914855480D * b;

        double l = lRoot * lRoot * lRoot;
        double m = mRoot * mRoot * mRoot;
        double s = sRoot * sRoot * sRoot;

        int red = MuiColorMath.encode(4.0767416621D * l - 3.3077115913D * m + 0.2309699292D * s);
        int green = MuiColorMath.encode(-1.2684380046D * l + 2.6097574011D * m - 0.3413193965D * s);
        int blue = MuiColorMath.encode(-0.0041960863D * l - 0.7034186147D * m + 1.7076147010D * s);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    /**
     * Holds lightness and hue and binary-searches chroma down until the color fits sRGB.
     *
     * @return an in-gamut copy
     */
    public @NotNull MuiOklch fit() {
        double lightness = MuiColorMath.clamp(this.lightness, 0.0D, 1.0D);
        MuiOklch base = new MuiOklch(lightness, Math.max(0.0D, this.chroma), this.hue);
        if (base.inGamut()) {
            return base;
        }

        double low = 0.0D;
        double high = base.chroma;
        for (int i = 0; i < 40; i++) {
            double middle = (low + high) * 0.5D;
            if (new MuiOklch(lightness, middle, this.hue).inGamut()) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return new MuiOklch(lightness, low, this.hue);
    }

    /**
     * Returns the widest chroma this lightness and hue can hold in sRGB.
     *
     * @return the largest in-gamut chroma
     */
    public double maxChroma() {
        double lightness = MuiColorMath.clamp(this.lightness, 0.0D, 1.0D);
        double low = 0.0D;
        double high = 0.42D;
        for (int i = 0; i < 40; i++) {
            double middle = (low + high) * 0.5D;
            if (new MuiOklch(lightness, middle, this.hue).inGamut()) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    /**
     * Returns a copy at a different lightness.
     *
     * @param value new lightness
     * @return a copy with the given lightness
     */
    public @NotNull MuiOklch withLightness(double value) {
        return new MuiOklch(value, this.chroma, this.hue);
    }

    /**
     * Returns a copy at a different chroma.
     *
     * @param value new chroma
     * @return a copy with the given chroma
     */
    public @NotNull MuiOklch withChroma(double value) {
        return new MuiOklch(this.lightness, value, this.hue);
    }

    /**
     * Returns a copy at a different hue.
     *
     * @param value new hue in degrees
     * @return a copy with the given hue
     */
    public @NotNull MuiOklch withHue(double value) {
        return new MuiOklch(this.lightness, this.chroma, value);
    }

    /**
     * Interpolates two colors in OKLCH.
     *
     * @param from   start color
     * @param to     end color
     * @param amount blend amount, clamped to {@code 0..1}
     * @return the interpolated color
     */
    public static @NotNull MuiOklch mix(@NotNull MuiOklch from, @NotNull MuiOklch to, double amount) {
        double t = MuiColorMath.clamp(amount, 0.0D, 1.0D);
        double fromA = from.chroma * Math.cos(Math.toRadians(from.hue));
        double fromB = from.chroma * Math.sin(Math.toRadians(from.hue));
        double toA = to.chroma * Math.cos(Math.toRadians(to.hue));
        double toB = to.chroma * Math.sin(Math.toRadians(to.hue));

        double a = fromA + (toA - fromA) * t;
        double b = fromB + (toB - fromB) * t;
        double hue = Math.toDegrees(Math.atan2(b, a));
        if (hue < 0.0D) {
            hue += 360.0D;
        }
        return new MuiOklch(
                from.lightness + (to.lightness - from.lightness) * t,
                Math.sqrt(a * a + b * b),
                hue);
    }

    private boolean inGamut() {
        double radians = Math.toRadians(this.hue);
        double a = this.chroma * Math.cos(radians);
        double b = this.chroma * Math.sin(radians);

        double lRoot = this.lightness + 0.3963377774D * a + 0.2158037573D * b;
        double mRoot = this.lightness - 0.1055613458D * a - 0.0638541728D * b;
        double sRoot = this.lightness - 0.0894841775D * a - 1.2914855480D * b;

        double l = lRoot * lRoot * lRoot;
        double m = mRoot * mRoot * mRoot;
        double s = sRoot * sRoot * sRoot;

        return inRange(4.0767416621D * l - 3.3077115913D * m + 0.2309699292D * s)
                && inRange(-1.2684380046D * l + 2.6097574011D * m - 0.3413193965D * s)
                && inRange(-0.0041960863D * l - 0.7034186147D * m + 1.7076147010D * s);
    }

    private static boolean inRange(double channel) {
        return channel >= -1.0E-6D && channel <= 1.0D + 1.0E-6D;
    }
}
