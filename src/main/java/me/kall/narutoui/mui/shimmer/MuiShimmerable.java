package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Per-instance marker that decides whether one component participates in shimmer.
 * <p>
 * The marker travels with the component itself, so two buttons on the same screen can differ
 * without a role, category or allow-list. All shimmer-capable widgets default to {@link #NEVER};
 * callers opt in with a marker or {@code MuiSettingsSection.shimmerNext()}. Dynamic cases can use
 * {@link #when(BooleanSupplier)}.
 *
 * <pre>{@code
 * new ShimmerTextView(context, MuiShimmerable.ALWAYS);
 * new ShimmerButton(context).shimmerable(MuiShimmerable.when(MyConfig::controlsShimmer));
 * }</pre>
 */
@FunctionalInterface
public interface MuiShimmerable {
    /** Marker that always allows shimmer. */
    MuiShimmerable ALWAYS = () -> true;
    /** Marker that never allows shimmer. */
    MuiShimmerable NEVER = () -> false;

    /**
     * Returns whether this component should shimmer.
     *
     * @return {@code true} to allow shimmer
     */
    boolean shimmer();

    /**
     * Creates a fixed marker.
     *
     * @param value {@code true} for {@link #ALWAYS}, {@code false} for {@link #NEVER}
     * @return the marker
     */
    static @NotNull MuiShimmerable of(boolean value) {
        return value ? ALWAYS : NEVER;
    }

    /**
     * Creates a marker backed by a condition. The condition is evaluated on each shimmer frame, so
     * it should be cheap. If it changes from {@code false} to {@code true} after the engine has
     * stopped, call {@link MuiShimmer#refresh()} to restart it.
     *
     * @param condition the condition
     * @return the marker
     */
    static @NotNull MuiShimmerable when(@NotNull BooleanSupplier condition) {
        return condition::getAsBoolean;
    }
}