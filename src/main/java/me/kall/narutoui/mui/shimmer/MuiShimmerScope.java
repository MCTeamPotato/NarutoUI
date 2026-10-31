package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Applies a {@link MuiShimmerable} marker to every {@link ShimmerTarget} in a view tree.
 * <p>
 * The settings DSL uses this for {@code shimmerNext()}: rows are assembled before they are added
 * to the card, so the marker is pushed down to the row's labels and controls at that point.
 */
public final class MuiShimmerScope {
    private MuiShimmerScope() {}

    /**
     * Applies a marker to a view and all of its descendants.
     *
     * @param root   the root view, or {@code null}
     * @param marker the marker
     */
    public static void apply(@Nullable View root, @NotNull MuiShimmerable marker) {
        if (root == null) return;
        if (root instanceof MuiShimmerContainer container) container.setShimmerable(marker);
        if (root instanceof ShimmerTarget target) target.setShimmerable(marker);
        if (root instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) apply(group.getChildAt(i), marker);
        }
    }
}