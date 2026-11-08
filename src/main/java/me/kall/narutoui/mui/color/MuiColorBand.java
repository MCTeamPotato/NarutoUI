package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable two-color band.
 * <p>
 * Both endpoints are opaque and ordered: the band runs from {@link #first()} to {@link #second()}
 * and back. Keeping the pair as one value lets callers compare staged and applied selections with
 * record equality.
 *
 * @param first  first endpoint
 * @param second second endpoint
 */
public record MuiColorBand(int first, int second) {
    /**
     * Forces both endpoints opaque, so a band cannot carry a transparent half.
     */
    public MuiColorBand {
        first = MuiColorBands.opaque(first);
        second = MuiColorBands.opaque(second);
    }

    /**
     * Builds the nine-stop ramp consumed by NarutoUI's shimmer engine.
     *
     * @return nine ARGB stops
     */
    public int @NotNull [] ramp() {
        return this.ramp(9);
    }

    /**
     * Builds a ramp with a custom number of stops.
     *
     * @param stops number of stops, at least 2
     * @return the ARGB stops
     */
    public int @NotNull [] ramp(int stops) {
        return MuiColorBands.oklabBand(this.first, this.second, stops);
    }

    /**
     * Returns a copy with a replaced first endpoint.
     *
     * @param color the new first endpoint
     * @return the updated band
     */
    public @NotNull MuiColorBand withFirst(int color) {
        return new MuiColorBand(color, this.second);
    }

    /**
     * Returns a copy with a replaced second endpoint.
     *
     * @param color the new second endpoint
     * @return the updated band
     */
    public @NotNull MuiColorBand withSecond(int color) {
        return new MuiColorBand(this.first, color);
    }
}