package me.kall.narutoui.mui.background;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.widget.FrameLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Single-image background preset with optional crop and dim. The image is decoded off the UI
 * thread and installed when ready.
 */
public final class MuiImageBackground implements MuiBackground {
    private static final Logger LOGGER = LogManager.getLogger("Mui");
    private static final int LOADING_COLOR = 0xFF000000;

    private final Path path;
    private final double cropX;
    private final double cropY;
    private final double cropWidth;
    private final double cropHeight;
    private final double dim;

    private @Nullable FrameLayout root;
    private @Nullable Image image;
    private int token;

    /**
     * Creates a full-image background.
     *
     * @param path image path
     * @param dim  dim overlay in {@code 0..1}
     */
    public MuiImageBackground(@NotNull Path path, double dim) {
        this(path, 0.0D, 0.0D, 1.0D, 1.0D, dim);
    }

    /**
     * Creates a cropped image background.
     *
     * @param path       image path
     * @param cropX      crop left in {@code 0..1}
     * @param cropY      crop top in {@code 0..1}
     * @param cropWidth  crop width in {@code 0..1}
     * @param cropHeight crop height in {@code 0..1}
     * @param dim        dim overlay in {@code 0..1}
     */
    public MuiImageBackground(@NotNull Path path, double cropX, double cropY, double cropWidth, double cropHeight, double dim) {
        this.path = path;
        this.cropX = cropX;
        this.cropY = cropY;
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.dim = dim;
    }

    @Override
    public @NotNull Drawable create(@NotNull Context context) {
        return new ColorDrawable(LOADING_COLOR);
    }

    @Override
    public void attach(@NotNull FrameLayout root) {
        this.root = root;
        root.setBackground(new ColorDrawable(LOADING_COLOR));
        this.load();
    }

    @Override
    public void detach() {
        this.token++;
        this.root = null;
        this.closeImage();
    }

    private void load() {
        int myToken = ++this.token;
        Path source = this.path;
        CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(source);
            } catch (Throwable error) {
                LOGGER.warn("Could not decode background image {}", source, error);
                return null;
            }
        }).whenComplete((bitmap, error) -> MuiModApi.postToUiThread(() -> {
            if (bitmap == null) return;
            if (myToken != this.token || this.root == null) {
                bitmap.close();
                return;
            }

            Image created = Image.createTextureFromBitmap(bitmap);
            bitmap.close();
            if (created == null) return;

            this.closeImage();
            this.image = created;
            this.root.setBackground(new MuiImageDrawable(created, this.cropX, this.cropY, this.cropWidth, this.cropHeight, this.dim));
        }));
    }

    private void closeImage() {
        if (this.image == null) return;
        this.image.close();
        this.image = null;
    }
}