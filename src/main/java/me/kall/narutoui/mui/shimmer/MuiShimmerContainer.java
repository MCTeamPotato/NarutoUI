package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;

/**
 * A composite widget that owns shimmer targets which are not direct descendant views (drawables) or
 * which it recreates later. {@link MuiShimmerScope} calls this instead of only walking the view tree,
 * so a row-level marker survives dropdown rebuilds and card reflows.
 */
public interface MuiShimmerContainer {
    /**
     * Applies a marker to this widget and every shimmer target it owns, including future rows.
     *
     * @param marker the marker
     */
    void setShimmerable(@NotNull MuiShimmerable marker);
}