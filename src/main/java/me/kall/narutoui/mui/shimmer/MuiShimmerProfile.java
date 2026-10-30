package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Settings and clock for one shimmer owner, such as a configuration screen or the HUD.
 * <p>
 * Widgets created while a profile is active capture that profile. Two mods can therefore use
 * different shimmer settings without overwriting a global configuration.
 */
public final class MuiShimmerProfile {
    /** Default profile used by HUD widgets and screens that do not select one. */
    private static final MuiShimmerProfile DEFAULT = new MuiShimmerProfile(MuiShimmerConfig.defaults());

    private static final ThreadLocal<Deque<MuiShimmerProfile>> CURRENT = ThreadLocal.withInitial(ArrayDeque::new);

    private final MuiShimmerClock clock;
    private MuiShimmerConfig config;

    /**
     * Creates a profile.
     *
     * @param config the config
     * @return the profile
     */
    public static @NotNull MuiShimmerProfile create(@NotNull MuiShimmerConfig config) {
        return new MuiShimmerProfile(config);
    }

    /**
     * Returns the shared default profile.
     *
     * @return the default profile
     */
    public static @NotNull MuiShimmerProfile defaultProfile() {
        return DEFAULT;
    }

    /**
     * Returns the profile active while a screen or rail is being built.
     *
     * @return the active profile, or the default
     */
    public static @NotNull MuiShimmerProfile current() {
        MuiShimmerProfile current = CURRENT.get().peek();
        return current == null ? DEFAULT : current;
    }

    /**
     * Runs an action with a temporary current profile.
     *
     * @param profile the profile
     * @param action  the action
     * @param <T>     result type
     * @return the action result
     */
    public static <T> T withCurrent(@NotNull MuiShimmerProfile profile, @NotNull Supplier<T> action) {
        Deque<MuiShimmerProfile> stack = CURRENT.get();
        stack.push(profile);
        try {
            return action.get();
        } finally {
            stack.pop();
            if (stack.isEmpty()) CURRENT.remove();
        }
    }

    /**
     * Runs an action with a temporary current profile.
     *
     * @param profile the profile
     * @param action  the action
     */
    public static void withCurrent(@NotNull MuiShimmerProfile profile, @NotNull Runnable action) {
        withCurrent(profile, () -> {
            action.run();
            return null;
        });
    }

    private MuiShimmerProfile(@NotNull MuiShimmerConfig config) {
        this.config = config;
        this.clock = new MuiShimmerClock(new ClockConfig());
    }

    /**
     * Returns the current configuration.
     *
     * @return the config
     */
    public @NotNull MuiShimmerConfig config() {
        return this.config;
    }

    /**
     * Replaces the configuration.
     *
     * @param config the new config
     */
    public void configure(@Nullable MuiShimmerConfig config) {
        this.config = config == null ? MuiShimmerConfig.defaults() : config;
    }

    /**
     * Returns this profile's clock.
     *
     * @return the clock
     */
    public @NotNull MuiShimmerClock clock() {
        return this.clock;
    }

    /**
     * Adapter that lets the clock always read the profile's current config.
     */
    private final class ClockConfig implements MuiShimmerConfig {
        @Override
        public boolean text() {
            return MuiShimmerProfile.this.config.text();
        }

        @Override
        public boolean configText() {
            return MuiShimmerProfile.this.config.configText();
        }

        @Override
        public boolean controls() {
            return MuiShimmerProfile.this.config.controls();
        }

        @Override
        public boolean hasAccent() {
            return MuiShimmerProfile.this.config.hasAccent();
        }

        @Override
        public int fps() {
            return MuiShimmerProfile.this.config.fps();
        }

        @Override
        public double speed() {
            return MuiShimmerProfile.this.config.speed();
        }

        @Override
        public double scale() {
            return MuiShimmerProfile.this.config.scale();
        }

        @Override
        public double strength() {
            return MuiShimmerProfile.this.config.strength();
        }

        @Override
        public int[] ramp() {
            return MuiShimmerProfile.this.config.ramp();
        }
    }
}