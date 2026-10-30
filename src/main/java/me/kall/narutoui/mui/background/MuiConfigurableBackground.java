package me.kall.narutoui.mui.background;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.Toast;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.screen.MuiConfigScreen;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import me.kall.narutoui.mui.util.MuiFilePicker;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import me.kall.narutoui.mui.overlay.MuiCropOverlayFactory;

/**
 * Turnkey configurable background for a {@link me.kall.narutoui.mui.screen.MuiConfigScreen}.
 * <p>
 * It combines the existing image and slideshow presets with folder/image pickers, cropping, and
 * settings persistence. Persistence is delegated to {@link MuiBackgroundSettings}, so the class
 * neither knows about Forge configuration values nor about the owning mod's localization keys.
 * <p>
 * The controller only depends on the screen services in {@link MuiScreenHost}; it can therefore be
 * owned by a host adapter without creating a dependency cycle.
 */
public final class MuiConfigurableBackground implements MuiBackground, MuiBackgroundActions {
    private static final Logger LOGGER = LogManager.getLogger("Mui");
    private static final int FALLBACK_COLOR = 0xC00A0B0F;

    private final MuiBackgroundSettings settings;
    private final MuiBackgroundLabels labels;
    private final MuiBackgroundFiles files;
    private final MuiCropOverlayFactory cropFactory;

    private MuiBackgroundState state;
    private @Nullable MuiScreenHost host;
    private @Nullable FrameLayout root;
    private @Nullable MuiSlideshowBackground slideshow;
    private @Nullable Image image;
    private String loaded = "";
    private int loadToken;

    /**
     * Creates a controller with the default Modern UI crop overlay.
     *
     * @param settings persistence adapter
     * @param labels   localized labels
     * @param files    file helper rooted at the game directory
     */
    public MuiConfigurableBackground(@NotNull MuiBackgroundSettings settings, @NotNull MuiBackgroundLabels labels, @NotNull MuiBackgroundFiles files) {
        this(settings, labels, files, MuiCropOverlayFactory.modern());
    }

    /**
     * Creates a controller with a custom crop overlay factory.
     *
     * @param settings    persistence adapter
     * @param labels      localized labels
     * @param files       file helper rooted at the game directory
     * @param cropFactory crop overlay factory
     */
    public MuiConfigurableBackground(@NotNull MuiBackgroundSettings settings, @NotNull MuiBackgroundLabels labels, @NotNull MuiBackgroundFiles files, @NotNull MuiCropOverlayFactory cropFactory) {
        this.settings = settings;
        this.labels = labels;
        this.files = files;
        this.cropFactory = cropFactory;
        this.state = settings.load();
    }

    /**
     * Returns the current settings snapshot.
     *
     * @return the current state
     */
    @Override
    public @NotNull MuiBackgroundState state() {
        return this.state;
    }

    /**
     * Returns the pictures currently found in the slideshow folder, in playback order.
     *
     * @return the ordered pictures
     */
    @Override
    public @NotNull List<Path> images() {
        return this.files.images(this.state.folder(), this.state.order());
    }

    /**
     * Reloads the persisted settings and redraws the attached root.
     */
    public void reload() {
        this.state = this.settings.load();
        this.refresh();
    }

    /**
     * Drops the cached fallback picture so the next refresh decodes it again.
     */
    public void invalidate() {
        this.loadToken++;
        this.closeImage();
        this.loaded = "";
    }

    @Override
    public @NotNull Drawable create(@NotNull Context context) {
        return new ColorDrawable(FALLBACK_COLOR);
    }

    @Override
    public void attach(@NotNull MuiScreenHost host, @NotNull FrameLayout root) {
        this.host = host;
        this.state = this.settings.load();
        this.attach(root);
    }

    @Override
    public void attach(@NotNull FrameLayout root) {
        this.root = root;
        this.refresh();
    }

    @Override
    public void detach() {
        this.stopSlideshow();
        this.loadToken++;
        this.closeImage();
        this.loaded = "";
        this.root = null;
        this.host = null;
    }

    @Override
    public void chooseBackgroundImage() {
        if (this.host == null) return;

        MuiFilePicker.openImage(this.labels.pickerTitle(), this.labels.imageFilter(),
                new MuiFilePicker.Callback() {
                    @Override
                    public void onPicked(@NotNull Path path) {
                        MuiConfigurableBackground.this.onImagePicked(path);
                    }

                    @Override
                    public void onFailed() {
                        MuiConfigurableBackground.this.toast(MuiConfigurableBackground.this.labels.pickerFailed());
                    }
                });
    }

    @Override
    public void chooseBackgroundFolder() {
        if (this.host == null) return;

        MuiFilePicker.openFolder(this.files.folder(this.state.folder()), this.labels.folderPickerTitle(),
                new MuiFilePicker.Callback() {
                    @Override
                    public void onPicked(@NotNull Path path) {
                        MuiConfigurableBackground controller = MuiConfigurableBackground.this;
                        controller.state = controller.state.withFolder(controller.files.store(path));
                        controller.persist();
                        controller.refresh();
                        controller.reloadPage();
                    }

                    @Override
                    public void onFailed() {
                        MuiConfigurableBackground.this.toast(MuiConfigurableBackground.this.labels.folderPickerFailed());
                    }
                });
    }

    @Override
    public void clearBackgroundFolder() {
        this.state = this.state.withFolder("");
        this.persist();
        this.refresh();
        this.reloadPage();
    }

    @Override
    public void clearBackgroundImage() {
        this.state = this.state.withImage("").withCrop(MuiCrop.FULL);
        this.persist();
        this.invalidate();
        this.refresh();
        this.reloadPage();
    }

    @Override
    public void setBackgroundInterval(double seconds) {
        this.state = this.state.withInterval(seconds);
        this.persist();
        if (this.slideshow != null) this.slideshow.setInterval(seconds);
    }

    @Override
    public void setBackgroundFade(double seconds) {
        this.state = this.state.withFade(seconds);
        this.persist();
        if (this.slideshow != null) this.slideshow.setFade(seconds);
    }

    @Override
    public void setBackgroundDim(double dim) {
        this.state = this.state.withDim(dim);
        this.persist();
        if (this.slideshow != null) {
            this.slideshow.setDim(dim);
        } else {
            this.refresh();
        }
    }

    private void persist() {
        this.settings.save(this.state);
    }

    private void refresh() {
        FrameLayout root = this.root;
        if (root == null) return;

        this.stopSlideshow();

        List<Path> images = this.images();
        if (!images.isEmpty()) {
            MuiSlideshowBackground show = new MuiSlideshowBackground(images,
                    this.state.interval(),
                    this.state.fade(),
                    this.state.dim());
            this.slideshow = show;
            show.attach(root);
            return;
        }

        if (!this.state.folder().isBlank()) {
            root.setBackground(new ColorDrawable(FALLBACK_COLOR));
            return;
        }

        Image image = this.image;
        if (image != null) {
            MuiCrop crop = this.state.crop();
            root.setBackground(new MuiImageDrawable(image, crop.x(), crop.y(), crop.width(),
                    crop.height(), this.state.dim()));
        } else {
            root.setBackground(new ColorDrawable(FALLBACK_COLOR));
            this.loadAsync(this::refresh);
        }
    }

    private void stopSlideshow() {
        if (this.slideshow == null) return;
        this.slideshow.detach();
        this.slideshow = null;
    }

    private void onImagePicked(@NotNull Path path) {
        MuiBackgroundFiles.decode(path).whenComplete((bitmap, error) -> MuiModApi.postToUiThread(() -> {
            if (bitmap == null) {
                this.toast(this.labels.failed());
                return;
            }
            this.startCrop(path, bitmap);
        }));
    }

    private void startCrop(@NotNull Path path, @NotNull Bitmap bitmap) {
        MuiScreenHost current = this.host;
        if (current == null) {
            bitmap.close();
            return;
        }

        Context context = current.requireContext();
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        Minecraft minecraft = Minecraft.getInstance();
        float target = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
        float actual = (float) imageWidth / (float) imageHeight;
        if (Math.abs(actual - target) <= 0.01F) {
            bitmap.close();
            this.saveBackground(path, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight);
            return;
        }

        Image preview = Image.createTextureFromBitmap(bitmap);
        bitmap.close();
        if (preview == null) return;

        MuiCropOverlay[] holder = new MuiCropOverlay[1];
        MuiCropOverlay.Listener listener = new MuiCropOverlay.Listener() {
            @Override
            public void onCrop(int x, int y, int width, int height) {
                current.removeOverlay(holder[0]);
                preview.close();
                MuiConfigurableBackground.this.saveBackground(path, imageWidth, imageHeight, x, y, width, height);
            }

            @Override
            public void onCancel() {
                current.removeOverlay(holder[0]);
                preview.close();
            }
        };

        holder[0] = this.cropFactory.create(context, preview, imageWidth, imageHeight, target, this.labels.crop(), listener);
        current.showOverlay(holder[0]);
    }

    private void saveBackground(@NotNull Path path, int imageWidth, int imageHeight, int x, int y, int width, int height) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return this.files.copyInto(path);
            } catch (Throwable error) {
                LOGGER.error("Could not store the background image", error);
                return null;
            }
        }).whenComplete((name, error) -> MuiModApi.postToUiThread(() -> {
            if (name == null) {
                this.toast(this.labels.failed());
                return;
            }

            double cropX = clamp01((double) x / Math.max(1, imageWidth));
            double cropY = clamp01((double) y / Math.max(1, imageHeight));
            double cropWidth = Math.max(0.01D, Math.min(1.0D - cropX,
                    (double) width / Math.max(1, imageWidth)));
            double cropHeight = Math.max(0.01D, Math.min(1.0D - cropY,
                    (double) height / Math.max(1, imageHeight)));

            this.state = this.state.withImage(name)
                    .withCrop(new MuiCrop(cropX, cropY, cropWidth, cropHeight));
            this.persist();
            this.invalidate();
            this.refresh();
            this.reloadPage();
            this.toast(this.labels.saved());
        }));
    }

    private void loadAsync(@NotNull Runnable onReady) {
        String name = this.state.image();
        if (name.isEmpty()) {
            this.closeImage();
            this.loaded = "";
            return;
        }
        if (this.image != null && name.equals(this.loaded)) return;

        Path path = this.files.file(name);
        if (!Files.isReadable(path)) {
            this.closeImage();
            this.loaded = "";
            return;
        }

        int token = ++this.loadToken;
        MuiBackgroundFiles.decode(path).whenComplete((bitmap, error) -> {
            if (bitmap == null) return;
            MuiModApi.postToUiThread(() -> {
                if (token != this.loadToken) {
                    bitmap.close();
                    return;
                }
                this.closeImage();
                Image created = Image.createTextureFromBitmap(bitmap);
                bitmap.close();
                if (created != null) {
                    this.image = created;
                    this.loaded = name;
                }
                onReady.run();
            });
        });
    }

    private void closeImage() {
        if (this.image == null) return;
        this.image.close();
        this.image = null;
    }

    private void reloadPage() {
        MuiScreenHost current = this.host;
        if (current != null) current.reloadCurrentPage();
    }

    private void toast(@NotNull CharSequence message) {
        MuiScreenHost current = this.host;
        if (current == null) return;
        Toast.makeText(current.requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}