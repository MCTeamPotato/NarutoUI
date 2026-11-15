package me.kall.narutoui.mui.overlay;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Image;
import me.kall.narutoui.mui.background.MuiConfigurableBackground;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the crop overlay used by {@link MuiConfigurableBackground}.
 * <p>
 * The hook lets callers apply their own theme, accent and widget factory without making the
 * background controller depend on a particular visual style. {@link #modern()} provides the
 * default, which already follows the host theme, shimmer profile and widget factory.
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
     * Creates a crop overlay inside the host's shimmer profile and widget factory. Implementations
     * that only need the screen context can ignore the host; the default forwards to
     * {@link #create(Context, Image, int, int, float, MuiCropOverlay.Labels,
     * MuiCropOverlay.Listener)}.
     *
     * @param host         owning screen host
     * @param image        decoded preview
     * @param imageWidth   source width
     * @param imageHeight  source height
     * @param targetAspect target aspect ratio
     * @param labels       localized labels
     * @param listener     crop callback
     * @return the overlay view
     */
    default @NotNull MuiCropOverlay create(@NotNull MuiScreenHost host, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, @NotNull MuiCropOverlay.Labels labels, @NotNull MuiCropOverlay.Listener listener) {
        return host.build(context -> this.create(context, image, imageWidth, imageHeight, targetAspect, labels, listener));
    }

    /**
     * Creates the default overlay with the host theme.
     *
     * @return the default factory
     */
    static @NotNull MuiCropOverlayFactory modern() {
        return new MuiCropOverlayFactory() {
            @Override
            public @NotNull MuiCropOverlay create(@NotNull Context context, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, MuiCropOverlay.@NotNull Labels labels, MuiCropOverlay.@NotNull Listener listener) {
                return new MuiCropOverlay(context, MuiTheme.modern(context), image, imageWidth, imageHeight, targetAspect, labels, listener);
            }

            @Override
            public @NotNull MuiCropOverlay create(@NotNull MuiScreenHost host, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, MuiCropOverlay.@NotNull Labels labels, MuiCropOverlay.@NotNull Listener listener) {
                return host.build(context -> new MuiCropOverlay(context, host.theme(), image, imageWidth, imageHeight, targetAspect, labels, listener));
            }
        };
    }
}
