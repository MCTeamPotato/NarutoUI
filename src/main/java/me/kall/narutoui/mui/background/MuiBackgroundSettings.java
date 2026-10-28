package me.kall.narutoui.mui.background;

import org.jetbrains.annotations.NotNull;

/**
 * Persistence adapter for the configurable background.
 * <p>
 * Implementations load the current values from their own configuration and write the complete
 * snapshot back. The save method is called on the UI thread after every user action, so it can
 * mark the configuration dirty.
 */
public interface MuiBackgroundSettings {
    /**
     * Loads the current settings.
     *
     * @return an immutable snapshot
     */
    @NotNull MuiBackgroundState load();

    /**
     * Persists the complete snapshot.
     *
     * @param state the new settings
     */
    void save(@NotNull MuiBackgroundState state);
}