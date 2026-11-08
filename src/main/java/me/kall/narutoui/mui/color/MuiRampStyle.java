package me.kall.narutoui.mui.color;

/**
 * How a scheme's nine-stop shimmer ramp is produced.
 * <p>
 * The ramp is not a gradient in general. A warm scheme walks a fixed ladder of its own semantic
 * colors, which is why the original Naruto ramp reads white, gold, orange, red instead of a single
 * hue getting lighter and darker. Cool schemes sweep their hue toward an anchor instead, and a
 * scheme without an accent gets a neutral band.
 */
public enum MuiRampStyle {
    /** Mirrored band between the accent and the outline, interpolated in OKLab. */
    VALUE_GRADIENT,
    /** Mirrored walk over the HUD warning, accent, label and critical colors, bright to deep. */
    SEMANTIC_HEAT,
    /**
     * White, then the accent hue sweeping toward a {@link MuiRampAnchor} while chroma stays at the
     * sRGB edge and lightness falls to a still-visible deep tone; the default for new schemes.
     */
    HUE_SWEEP,
    /** The fixed neutral white-to-gray band, for schemes without an accent. */
    NEUTRAL
}
