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
     * Called after a theme change. The default rebuilds the current page so page-local colors and
     * custom previews are resolved again. Hosts with additional theme state can override it.
     */
    default void onThemeChanged() {
        this.reloadCurrentPage();
    }
}