package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Anything that can take part in the API's shimmer.
 */
public interface ShimmerTarget {
    /**
     * Returns the color the target had before shimmering.
     *
     * @return the base ARGB color
     */
    int shimmerBaseColor();

    /**
     * Applies a shimmer color.
     *
     * @param color the blended ARGB color
     */
    void applyShimmerColor(int color);

    /**
     * Restores the target's base color.
     */
    void applyBaseColor();

    /**
     * Returns whether this target should shimmer right now.
     *
     * @return {@code true} when active
     */
    boolean shimmerEnabled();

    /**
     * Returns how strongly the shimmer color is applied.
     *
     * @return strength in {@code 0..1}
     */
    default float shimmerStrength() {
        return 0.65F;
    }

    /**
     * Returns the profile that controls this target.
     *
     * @return the profile
     */
    default @NotNull MuiShimmerProfile shimmerProfile() {
        return MuiShimmerProfile.current();
    }

    /**
     * Returns the view used to sample the wave position.
     *
     * @return the host view, or {@code null}
     */
    @Nullable
    View shimmerHost();
}