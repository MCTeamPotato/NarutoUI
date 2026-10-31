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
     * Returns whether this target is locally able to shimmer right now. This must not consult the
     * global config; the engine combines that in one place. Implementations normally also check
     * their {@link MuiShimmerable} marker here.
     *
     * @return {@code true} when attached, enabled, marked and otherwise eligible
     */
    boolean shimmerEnabled();

    /**
     * Replaces this target's per-instance marker.
     *
     * @param marker the marker
     */
    default void setShimmerable(@NotNull MuiShimmerable marker) {
    }

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