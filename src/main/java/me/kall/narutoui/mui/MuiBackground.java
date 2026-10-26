package me.kall.narutoui.mui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.widget.FrameLayout;
import me.kall.narutoui.mui.background.MuiImageBackground;
import me.kall.narutoui.mui.background.MuiSlideshowBackground;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Supplies the background drawable for a {@link MuiConfigScreen}.
 * <p>
 * The default is a dim, almost-black scrim. Implementations can return an image drawable, a
 * gradient, or any other Modern UI drawable without the screen shell knowing about it.
 * <p>
 * Override {@link #attach(FrameLayout)} when the background owns a slideshow or other state that
 * needs the root view; override {@link #detach()} to stop it when the screen is destroyed.
 */
@FunctionalInterface
public interface MuiBackground {
    /**
     * Creates the drawable used as the root background.
     *
     * @param context the screen context
     * @return a non-null background drawable
     */
    @NotNull Drawable create(@NotNull Context context);

    /**
     * Applies the background to the screen root. The default implementation installs
     * {@link #create(Context)}; stateful backgrounds can override this and take ownership of
     * their root view.
     *
     * @param root the screen root layout
     */
    default void attach(@NotNull FrameLayout root) {
        root.setBackground(this.create(root.getContext()));
    }

    /**
     * Releases any state acquired by {@link #attach(FrameLayout)}. Called when the screen view is
     * destroyed.
     */
    default void detach() {}

    /**
     * Creates a solid-color background.
     *
     * @param argb the ARGB color
     * @return a background provider
     */
    static @NotNull MuiBackground color(int argb) {
        return context -> new ColorDrawable(argb);
    }

    /**
     * Creates a fully transparent background.
     *
     * @return a transparent background provider
     */
    static @NotNull MuiBackground transparent() {
        return color(0x00000000);
    }

    /**
     * Creates an opaque black background.
     *
     * @return a black background provider
     */
    static @NotNull MuiBackground black() {
        return color(0xFF000000);
    }

    /**
     * Creates a single-image background covering the screen.
     *
     * @param path image path
     * @param dim  dim overlay in {@code 0..1}
     * @return an image background provider
     */
    static @NotNull MuiBackground image(@NotNull Path path, double dim) {
        return new MuiImageBackground(path, dim);
    }

    /**
     * Creates a cropped single-image background.
     *
     * @param path       image path
     * @param cropX      crop left in {@code 0..1}
     * @param cropY      crop top in {@code 0..1}
     * @param cropWidth  crop width in {@code 0..1}
     * @param cropHeight crop height in {@code 0..1}
     * @param dim        dim overlay in {@code 0..1}
     * @return an image background provider
     */
    static @NotNull MuiBackground image(@NotNull Path path, double cropX, double cropY,
                                        double cropWidth, double cropHeight, double dim) {
        return new MuiImageBackground(path, cropX, cropY, cropWidth, cropHeight, dim);
    }

    /**
     * Creates a slideshow background.
     *
     * @param files    image paths
     * @param interval seconds between pictures
     * @param fade     cross-fade duration in seconds
     * @param dim      dim overlay in {@code 0..1}
     * @return a slideshow background provider
     */
    static @NotNull MuiBackground slideshow(@NotNull List<Path> files, double interval,
                                            double fade, double dim) {
        return new MuiSlideshowBackground(files, interval, fade, dim);
    }

    /**
     * Creates the default dim near-black scrim.
     *
     * @return the default background provider
     */
    static @NotNull MuiBackground scrim() {
        return color(0xC00A0B0F);
    }

    /**
     * Wraps a drawable factory.
     *
     * @param factory the factory invoked for each screen
     * @return a background provider backed by {@code factory}
     */
    static @NotNull MuiBackground of(@NotNull Function<Context, Drawable> factory) {
        return factory::apply;
    }
}