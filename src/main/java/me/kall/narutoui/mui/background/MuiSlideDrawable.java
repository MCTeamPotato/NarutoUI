package me.kall.narutoui.mui.background;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.graphics.drawable.Drawable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Two-image cross-fade drawable for the API slideshow preset.
 */
public final class MuiSlideDrawable extends Drawable {
    private final Paint paint = new Paint();
    private @Nullable Image previous;
    private @Nullable Image current;
    private float progress = 1.0F;
    private float dim;

    /**
     * Creates an empty cross-fade drawable.
     */
    public MuiSlideDrawable() {}

    /**
     * Sets the images participating in the cross-fade.
     *
     * @param previous outgoing image
     * @param current  incoming/current image
     */
    public void setImages(@Nullable Image previous, @Nullable Image current) {
        this.previous = previous;
        this.current = current;
        this.progress = previous == null ? 1.0F : 0.0F;
        this.invalidateSelf();
    }

    /**
     * Sets the cross-fade progress.
     *
     * @param progress progress in {@code 0..1}
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0.0F, Math.min(1.0F, progress));
        this.invalidateSelf();
    }

    /**
     * Sets the dim overlay.
     *
     * @param dim dim in {@code 0..1}
     */
    public void setDim(double dim) {
        this.dim = (float) Math.max(0.0D, Math.min(1.0D, dim));
        this.invalidateSelf();
    }

    @Override
    public void draw(@NotNull Canvas canvas) {
        Rect bounds = this.getBounds();
        if (bounds.isEmpty()) return;

        this.paint.setColor(0xC00A0B0F);
        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, this.paint);

        if (this.previous != null) this.drawImage(canvas, this.previous, bounds, 1.0F);
        if (this.current != null) this.drawImage(canvas, this.current, bounds, this.progress);

        int alpha = Math.round(this.dim * 255.0F);
        if (alpha > 0) {
            this.paint.setColor(alpha << 24);
            canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, this.paint);
        }
    }

    private void drawImage(@NotNull Canvas canvas, @NotNull Image image, @NotNull Rect bounds, float alpha) {
        int opacity = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        if (opacity <= 0 || image.isClosed()) return;

        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 0) return;

        float scale = Math.max(bounds.width() / (float) width, bounds.height() / (float) height);
        float drawWidth = width * scale;
        float drawHeight = height * scale;
        float left = bounds.left + (bounds.width() - drawWidth) / 2.0F;
        float top = bounds.top + (bounds.height() - drawHeight) / 2.0F;

        this.paint.setColor(opacity << 24 | 0xFFFFFF);
        canvas.drawImage(image, 0.0F, 0.0F, width, height, left, top, left + drawWidth, top + drawHeight, this.paint);
    }
}