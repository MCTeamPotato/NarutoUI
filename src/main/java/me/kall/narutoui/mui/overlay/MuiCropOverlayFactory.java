package me.kall.narutoui.mui.overlay;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Image;
import me.kall.narutoui.mui.background.MuiConfigurableBackground;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the crop overlay used by {@link MuiConfigurableBackground}.
 * <p>
 * The hook lets callers apply their own theme, accent and widget factory without making the
 * background controller depend on a particular visual style. {@link #modern()} provides the
 * unstyled default.
 */
@FunctionalInterface
public interface MuiCropOverlayFactory {
    /**
     * Creates a crop overlay.
     *
     * @param context      screen context
     * @param image        decoded preview
     * @param imageWidth   source width
     * @param imageHeight  source height
     * @param targetAspect target aspect ratio
     * @param labels       localized labels
     * @param listener     crop callback
     * @return the overlay view
     */
    @NotNull MuiCropOverlay create(@NotNull Context context, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, @NotNull MuiCropOverlay.Labels labels, @NotNull MuiCropOverlay.Listener listener);

    /**
     * Creates the default overlay with the standard Modern UI theme.
     *
     * @return the default factory
     */
    static @NotNull MuiCropOverlayFactory modern() {
        return (context, image, imageWidth, imageHeight, targetAspect, labels, listener) -> new MuiCropOverlay(context, MuiTheme.modern(context), image, imageWidth, imageHeight, targetAspect, labels, listener);
    }
}