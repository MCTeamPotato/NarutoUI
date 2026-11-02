package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * General services exposed by a configuration-screen host.
 * <p>
 * This is the single contract that pages, overlays, background controllers and application-level
 * hosts should depend on. {@link MuiConfigFragment} implements it directly; application hosts can
 * wrap a fragment to add app-specific services without introducing another host interface.
 */
public interface MuiScreenHost {
    /**
     * Returns the context of the attached screen.
     *
     * @return the screen context
     * @throws IllegalStateException when no screen is attached
     */
    @NotNull Context requireContext();

    /**
     * Adds an overlay on top of the whole configuration shell.
     *
     * @param overlay the overlay view
     */
    void showOverlay(@NotNull View overlay);

    /**
     * Removes a previously shown overlay.
     *
     * @param overlay the overlay view
     */
    void removeOverlay(@NotNull View overlay);

    /**
     * Rebuilds the currently visible page.
     */
    void reloadCurrentPage();

    /**
     * Asks the host to close through the normal back path. The close guard registered with
     * {@link MuiConfigScreen.Builder#onCloseRequested} runs first and may keep the screen open.
     * <p>
     * Hosts that do not own a Minecraft screen may leave this unsupported.
     *
     * @throws UnsupportedOperationException when the host cannot close itself
     */
    default void requestClose() {
        throw new UnsupportedOperationException("This MuiScreenHost does not support closing");
    }

    /**
     * Closes the host immediately, bypassing the close guard. Call this after an unsaved-changes
     * confirmation has been accepted. Prefer {@link #requestClose()} for normal back navigation.
     *
     * @throws UnsupportedOperationException when the host cannot close itself
     */
    default void close() {
        throw new UnsupportedOperationException("This MuiScreenHost does not support closing");
    }

    /**
     * Called after a theme change. The default rebuilds the current page so page-local colors and
     * custom previews are resolved again. Hosts with additional theme state can override it.
     */
    default void onThemeChanged() {
        this.reloadCurrentPage();
    }
}