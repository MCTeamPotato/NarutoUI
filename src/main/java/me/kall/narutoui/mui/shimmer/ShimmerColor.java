package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;

/**
 * Color interpolation shared by the shimmer engine and its targets.
 */
public final class ShimmerColor {
    private ShimmerColor() {}

    /**
     * Linearly interpolates two ARGB colors.
     *
     * @param from   start color
     * @param to     end color
     * @param amount blend amount, clamped to {@code 0..1}
     * @return the blended color
     */
    public static int blend(int from, int to, float amount) {
        float a = Math.max(0.0F, Math.min(1.0F, amount));
        int alpha = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * a);
        int red = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * a);
        int green = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * a);
        int blue = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * a);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}