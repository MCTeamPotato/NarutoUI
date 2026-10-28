package me.kall.narutoui.mui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * Services exposed by a configuration-screen host.
 * <p>
 * {@link MuiConfigFragment} implements this contract, and it is the narrow interface that
 * background controllers and other shell collaborators should depend on. It deliberately excludes
 * page building, theming and the screen model.
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
}