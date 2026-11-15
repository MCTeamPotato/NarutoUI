package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.factory.MuiWidgetFactory;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.overlay.MuiColorPickerOverlay;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * General services exposed by a configuration-screen host.
 * <p>
 * This is the single contract that pages, overlays, background controllers and application-level
 * hosts should depend on. {@link MuiConfigFragment} implements it directly; application hosts can
 * wrap a fragment to add app-specific services without introducing another host interface.
 * <p>
 * The host also owns the screen's theme, shimmer profile and widget factory. Anything built through
 * {@link #build(Function)} or through the overlay helpers ({@link #colorPickerView},
 * {@link #cropView}) inherits all three, so a late callback does not have to re-enter the
 * {@code withCurrent(...)} / {@code withFactory(...)} stacks by hand.
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
     * Builds an overlay with the host's theme, shimmer profile and widget factory, then adds it on
     * top of the configuration shell.
     *
     * @param factory overlay factory, invoked with the screen context
     */
    default void showOverlay(@NotNull Function<Context, ? extends View> factory) {
        this.showOverlay(this.build(factory));
    }

    /**
     * Removes a previously shown overlay.
     *
     * @param overlay the overlay view
     */
    void removeOverlay(@NotNull View overlay);

    /**
     * Rebuilds the currently visible page. Hosts are expected to keep the page's scroll position
     * and any keyed expand/collapse state.
     */
    void reloadCurrentPage();

    /**
     * Rebuilds every page from its original factory/body. Use this after a reset that changes
     * values on pages other than the visible one. The current page is rebuilt immediately; other
     * pages are rebuilt on their next visit.
     */
    default void reloadAllPages() {
        this.reloadCurrentPage();
    }

    /**
     * Returns the theme this host renders with. The default resolves the Modern UI attributes, but
     * a screen with a {@code themeProvider} returns the provided theme so overlays share its accent.
     *
     * @return the active theme
     */
    default @NotNull MuiTheme theme() {
        return MuiTheme.modern(this.requireContext());
    }

    /**
     * Returns the shimmer profile this host renders with.
     *
     * @return the shimmer profile
     */
    default @NotNull MuiShimmerProfile shimmerProfile() {
        return MuiShimmerProfile.current();
    }

    /**
     * Returns the widget factory this host renders with.
     *
     * @return the widget factory
     */
    default @NotNull MuiWidgetFactory widgetFactory() {
        return MuiControls.widgetFactory();
    }

    /**
     * Builds a view with the host's shimmer profile and widget factory active, so controls created
     * after the page body (for example inside a picker callback) look like the rest of the screen.
     *
     * @param factory view factory, invoked with the screen context
     * @param <T>     view type
     * @return the built view
     */
    default <T> @NotNull T build(@NotNull Function<Context, T> factory) {
        return MuiShimmerProfile.withCurrent(this.shimmerProfile(),
                () -> MuiControls.withFactory(this.widgetFactory(),
                        () -> factory.apply(this.requireContext())));
    }

    /**
     * Builds a color picker that inherits the host's theme, shimmer profile and widget factory.
     * The caller decides when to show it with {@link #showOverlay(View)}.
     *
     * @param argb     initial ARGB color
     * @param labels   localized labels
     * @param listener pick callback
     * @return the overlay view
     */
    default @NotNull MuiColorPickerOverlay colorPickerView(int argb, MuiColorPickerOverlay.@NotNull Labels labels, MuiColorPickerOverlay.@NotNull Listener listener) {
        return this.build(context -> new MuiColorPickerOverlay(context, this.theme(), argb, labels, listener));
    }

    /**
     * Builds and shows a color picker.
     *
     * @param argb     initial ARGB color
     * @param labels   localized labels
     * @param listener pick callback
     */
    default void colorPicker(int argb, MuiColorPickerOverlay.@NotNull Labels labels, MuiColorPickerOverlay.@NotNull Listener listener) {
        this.showOverlay(this.colorPickerView(argb, labels, listener));
    }

    /**
     * Builds a crop overlay that inherits the host's theme, shimmer profile and widget factory.
     * The caller decides when to show it with {@link #showOverlay(View)}.
     *
     * @param image        decoded preview
     * @param imageWidth   source width
     * @param imageHeight  source height
     * @param targetAspect target width/height ratio
     * @param labels       localized labels
     * @param listener     crop callback
     * @return the overlay view
     */
    default @NotNull MuiCropOverlay cropView(@NotNull Image image, int imageWidth, int imageHeight, float targetAspect, MuiCropOverlay.@NotNull Labels labels, MuiCropOverlay.@NotNull Listener listener) {
        return this.build(context -> new MuiCropOverlay(context, this.theme(), image, imageWidth, imageHeight, targetAspect, labels, listener));
    }

    /**
     * Builds and shows a crop overlay.
     *
     * @param image        decoded preview
     * @param imageWidth   source width
     * @param imageHeight  source height
     * @param targetAspect target width/height ratio
     * @param labels       localized labels
     * @param listener     crop callback
     */
    default void crop(@NotNull Image image, int imageWidth, int imageHeight, float targetAspect, MuiCropOverlay.@NotNull Labels labels, MuiCropOverlay.@NotNull Listener listener) {
        this.showOverlay(this.cropView(image, imageWidth, imageHeight, targetAspect, labels, listener));
    }

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
