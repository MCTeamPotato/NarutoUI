package me.kall.narutoui.mui.background;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable snapshot of the configurable background settings.
 * <p>
 * The state is owned by the page adapter and persisted through
 * {@link MuiBackgroundSettings#save(MuiBackgroundState)}. The controller only replaces single
 * fields with the {@code with*} helpers, so a settings change cannot accidentally reset unrelated
 * values.
 *
 * @param image    fallback picture name, relative to the game directory
 * @param folder   slideshow folder, relative to the game directory or absolute
 * @param order    preferred slideshow order; unlisted pictures follow alphabetically
 * @param interval seconds each picture is shown
 * @param fade     cross-fade duration in seconds
 * @param dim      dim overlay in {@code 0..1}
 * @param crop     crop of the fallback picture
 */
public record MuiBackgroundState(@NotNull String image, @NotNull String folder, @NotNull List<String> order, double interval, double fade, double dim, @NotNull MuiCrop crop) {
    /** Neutral state used when a settings adapter has no values yet. */
    public static final MuiBackgroundState DEFAULT = new MuiBackgroundState("", "", List.of(), 12.0D, 0.6D, 0.55D, MuiCrop.FULL);

    public MuiBackgroundState {
        order = List.copyOf(order);
        interval = Double.isFinite(interval) ? Math.max(1.0D, interval) : 12.0D;
        fade = Double.isFinite(fade) ? Math.max(0.0D, fade) : 0.6D;
        dim = Double.isFinite(dim) ? Math.max(0.0D, Math.min(1.0D, dim)) : 0.55D;
    }

    public @NotNull MuiBackgroundState withImage(@NotNull String image) {
        return new MuiBackgroundState(image, this.folder, this.order, this.interval, this.fade, this.dim, this.crop);
    }

    public @NotNull MuiBackgroundState withFolder(@NotNull String folder) {
        return new MuiBackgroundState(this.image, folder, this.order, this.interval, this.fade, this.dim, this.crop);
    }

    public @NotNull MuiBackgroundState withOrder(@NotNull List<String> order) {
        return new MuiBackgroundState(this.image, this.folder, order, this.interval, this.fade, this.dim, this.crop);
    }

    public @NotNull MuiBackgroundState withInterval(double interval) {
        return new MuiBackgroundState(this.image, this.folder, this.order, interval, this.fade, this.dim, this.crop);
    }

    public @NotNull MuiBackgroundState withFade(double fade) {
        return new MuiBackgroundState(this.image, this.folder, this.order, this.interval, fade, this.dim, this.crop);
    }

    public @NotNull MuiBackgroundState withDim(double dim) {
        return new MuiBackgroundState(this.image, this.folder, this.order, this.interval, this.fade, dim, this.crop);
    }

    public @NotNull MuiBackgroundState withCrop(@NotNull MuiCrop crop) {
        return new MuiBackgroundState(this.image, this.folder, this.order, this.interval, this.fade, this.dim, crop);
    }
}