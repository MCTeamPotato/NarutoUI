package me.kall.narutoui.mui.color;

/**
 * How a scheme's HUD tokens are produced.
 * <p>
 * Most schemes reuse the UI tokens: labels are secondary text, dividers are outlines, the accent
 * is the primary color. Two schemes do not fit that shape and carry a fixed set instead.
 */
public enum MuiHudPreset {
    /** Derive every HUD token from the UI tokens; the default. */
    DERIVED,
    /** The fixed warm overlay palette used by the original Naruto scheme. */
    CHAKRA,
    /** The fixed white overlay palette used by the accent-less classic scheme. */
    NEUTRAL
}
