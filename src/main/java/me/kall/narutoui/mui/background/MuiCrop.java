package me.kall.narutoui.mui.background;

/**
 * Normalized crop rectangle of the single background picture.
 * <p>
 * All values are fractions in {@code 0..1}. The compact constructor clamps them so a malformed
 * configuration cannot create a zero or out-of-bounds crop.
 *
 * @param x      left edge
 * @param y      top edge
 * @param width  visible width
 * @param height visible height
 */
public record MuiCrop(double x, double y, double width, double height) {
    /** Full picture, no cropping. */
    public static final MuiCrop FULL = new MuiCrop(0.0D, 0.0D, 1.0D, 1.0D);

    public MuiCrop {
        x = Math.min(0.99D, clamp01(x));
        y = Math.min(0.99D, clamp01(y));
        width = clampSize(width, 1.0D - x);
        height = clampSize(height, 1.0D - y);
    }

    private static double clamp01(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, Math.min(1.0D, value)) : 0.0D;
    }

    private static double clampSize(double value, double remaining) {
        if (!Double.isFinite(value)) return remaining;
        return Math.max(0.01D, Math.min(remaining, value));
    }
}