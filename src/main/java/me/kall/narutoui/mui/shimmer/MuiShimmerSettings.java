package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared holder for shimmer settings. Kept separate from {@link MuiShimmer} so the HUD clock can
 * read settings without loading Modern UI widget classes.
 */
public final class MuiShimmerSettings {
    private MuiShimmerSettings() {}

    /**
     * Installs the settings on the default profile.
     *
     * @param config the config, or {@code null} to restore defaults
     */
    public static void configure(@Nullable MuiShimmerConfig config) {
        MuiShimmerProfile.defaultProfile().configure(config);
    }

    /**
     * Returns the default profile's settings.
     *
     * @return the config
     */
    public static @NotNull MuiShimmerConfig config() {
        return MuiShimmerProfile.defaultProfile().config();
    }
}