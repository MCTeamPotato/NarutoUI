package me.kall.narutoui.mui.background;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import me.kall.narutoui.mui.MuiBackground;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Slideshow background preset: cross-fades through a list of images with configurable interval,
 * fade duration and dim overlay.
 */
public final class MuiSlideshowBackground implements MuiBackground {
    private static final Logger LOGGER = LogManager.getLogger("Mui");
    private static final int FALLBACK_COLOR = 0xC00A0B0F;

    private final MuiSlideDrawable drawable = new MuiSlideDrawable();
    private final Runnable tick = this::advance;
    private List<Path> files;
    private double interval;
    private double fade;
    private double dim;
    private @Nullable FrameLayout root;
    private int index = -1;
    private @Nullable Image current;
    private @Nullable Image incoming;
    private @Nullable ValueAnimator fadeAnimator;
    private boolean running;
    private boolean loading;
    private int token;

    /**
     * Creates a slideshow background.
     *
     * @param files    image paths
     * @param interval seconds between pictures
     * @param fade     cross-fade duration in seconds
     * @param dim      dim overlay in {@code 0..1}
     */
    public MuiSlideshowBackground(@NotNull List<Path> files, double interval, double fade, double dim) {
        this.files = List.copyOf(files);
        this.interval = Math.max(1.0D, interval);
        this.fade = Math.max(0.0D, fade);
        this.dim = Math.max(0.0D, Math.min(1.0D, dim));
        this.drawable.setDim(this.dim);
    }

    @Override
    public @NotNull Drawable create(@NotNull Context context) {
        return new ColorDrawable(FALLBACK_COLOR);
    }

    @Override
    public void attach(@NotNull FrameLayout root) {
        if (this.root != null) this.stop();
        this.root = root;
        root.setBackground(this.drawable);
        this.start();
    }

    @Override
    public void detach() {
        this.stop();
        this.root = null;
    }

    /**
     * Replaces the picture list and restarts when attached.
     *
     * @param files new image paths
     */
    public void setFiles(@NotNull List<Path> files) {
        this.files = List.copyOf(files);
        if (this.root != null) {
            this.stop();
            this.start();
        }
    }

    /**
     * Sets the delay between pictures.
     *
     * @param seconds interval in seconds
     */
    public void setInterval(double seconds) {
        this.interval = Math.max(1.0D, seconds);
        this.reschedule();
    }

    /**
     * Sets the cross-fade duration.
     *
     * @param seconds fade duration in seconds
     */
    public void setFade(double seconds) {
        this.fade = Math.max(0.0D, seconds);
    }

    /**
     * Sets the dim overlay.
     *
     * @param dim dim in {@code 0..1}
     */
    public void setDim(double dim) {
        this.dim = Math.max(0.0D, Math.min(1.0D, dim));
        this.drawable.setDim(this.dim);
    }

    /**
     * Restarts the countdown after the interval changes.
     */
    public void reschedule() {
        if (!this.running || this.fadeAnimator != null || this.loading || this.root == null) return;
        this.root.removeCallbacks(this.tick);
        this.schedule();
    }

    private void start() {
        if (this.files.isEmpty() || this.root == null) return;

        this.running = true;
        this.token++;
        this.load(0, image -> {
            if (!this.running) {
                image.close();
                return;
            }
            this.current = image;
            this.index = 0;
            this.drawable.setImages(null, image);
            this.schedule();
        });
    }

    private void stop() {
        this.running = false;
        this.loading = false;
        this.token++;
        if (this.root != null) this.root.removeCallbacks(this.tick);

        ValueAnimator animator = this.fadeAnimator;
        this.fadeAnimator = null;
        if (animator != null) animator.cancel();

        this.drawable.setImages(null, null);
        this.closeImages();
    }

    private void schedule() {
        if (!this.running || this.root == null) return;
        this.root.postDelayed(this.tick, Math.max(1000L, Math.round(this.interval * 1000.0D)));
    }

    private void advance() {
        if (!this.running || this.fadeAnimator != null || this.loading) return;
        if (this.files.size() < 2) {
            this.schedule();
            return;
        }

        int next = (this.index + 1) % this.files.size();
        this.load(next, image -> {
            if (!this.running) {
                image.close();
                return;
            }
            this.beginFade(image, next);
        });
    }

    private void beginFade(@NotNull Image image, int nextIndex) {
        Image previous = this.current;
        this.incoming = image;
        this.drawable.setImages(previous, image);
        this.drawable.setProgress(0.0F);

        long duration = Math.max(0L, Math.round(this.fade * 1000.0D));
        if (duration == 0L) {
            this.finishFade(image, nextIndex);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0.0F, 1.0F);
        animator.setDuration(duration);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            if (this.fadeAnimator != animator) return;
            this.drawable.setProgress((Float) animation.getAnimatedValue());
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (MuiSlideshowBackground.this.fadeAnimator != animator) return;
                MuiSlideshowBackground.this.fadeAnimator = null;
                MuiSlideshowBackground.this.finishFade(image, nextIndex);
            }
        });
        this.fadeAnimator = animator;
        animator.start();
    }

    private void finishFade(@NotNull Image image, int nextIndex) {
        Image previous = this.current;
        this.current = image;
        this.incoming = null;
        this.index = nextIndex;
        this.drawable.setImages(null, image);
        this.drawable.setProgress(1.0F);
        if (previous != null && previous != image) previous.close();
        this.schedule();
    }

    private void load(int fileIndex, @NotNull Callback callback) {
        if (this.loading) return;
        this.loading = true;

        Path path = this.files.get(fileIndex);
        int myToken = ++this.token;
        CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable error) {
                LOGGER.warn("Could not decode slideshow image {}", path, error);
                return null;
            }
        }).whenComplete((bitmap, error) -> {
            if (bitmap == null) {
                MuiModApi.postToUiThread(() -> {
                    if (!this.running || myToken != this.token) return;
                    this.loading = false;
                    this.index = fileIndex;
                    this.schedule();
                });
                return;
            }

            MuiModApi.postToUiThread(() -> {
                if (!this.running || myToken != this.token) {
                    bitmap.close();
                    this.loading = false;
                    return;
                }

                Image created = Image.createTextureFromBitmap(bitmap);
                bitmap.close();
                this.loading = false;
                if (created == null) {
                    this.schedule();
                    return;
                }
                callback.onLoaded(created);
            });
        });
    }

    private void closeImages() {
        if (this.current != null) {
            this.current.close();
            this.current = null;
        }
        if (this.incoming != null) {
            this.incoming.close();
            this.incoming = null;
        }
    }

    private interface Callback {
        void onLoaded(@NotNull Image image);
    }
}