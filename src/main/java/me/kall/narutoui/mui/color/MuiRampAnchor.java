package me.kall.narutoui.mui.color;

/**
 * A hue a shimmer ramp can travel between.
 * <p>
 * The values are the primaries and secondaries of the OKLCH wheel. A ramp declared as a span of two
 * anchors reads as a color journey: the hue moves while lightness falls and chroma grows, so the
 * wave never passes through a gray midpoint.
 * <p>
 * Anchors are a design vocabulary, not something derivable from the brand color. {@link #nearest}
 * gives a usable default when a scheme only wants to name its deep end.
 */
public enum MuiRampAnchor {
    /** Red, the warm pole. */
    RED(25.0D),
    /** Orange. */
    ORANGE(55.0D),
    /** Amber, between orange and yellow. */
    AMBER(80.0D),
    /** Yellow, the bright warm pole. */
    YELLOW(95.0D),
    /** Lime, the yellow-green highlight of a jade ramp. */
    LIME(120.0D),
    /** Green. */
    GREEN(145.0D),
    /** Spring green, the deep end of a jade ramp. */
    SPRING(165.0D),
    /** Cyan, the highlight of a water ramp. */
    CYAN(195.0D),
    /** Steel, a desaturated cyan-blue used as a cool highlight. */
    STEEL(210.0D),
    /** Blue, the deep cool pole. */
    BLUE(260.0D),
    /** Magenta. */
    MAGENTA(330.0D),
    /** Rose, the pink highlight of a sakura ramp. */
    ROSE(340.0D);

    private final double hue;

    MuiRampAnchor(double hue) {
        this.hue = hue;
    }

    /**
     * Returns the anchor hue.
     *
     * @return the anchor hue in degrees
     */
    public double hue() {
        return this.hue;
    }

    /**
     * Returns the anchor closest to a hue.
     *
     * @param hue hue in degrees
     * @return the nearest anchor
     */
    public static MuiRampAnchor nearest(double hue) {
        MuiRampAnchor best = RED;
        double bestDistance = Double.MAX_VALUE;
        for (MuiRampAnchor anchor : values()) {
            double distance = Math.abs(MuiColorMath.shortestHueDelta(hue, anchor.hue));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = anchor;
            }
        }
        return best;
    }
}
