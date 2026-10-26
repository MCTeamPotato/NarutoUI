package me.kall.narutoui.mui.background;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.graphics.drawable.Drawable;
import org.jetbrains.annotations.NotNull;

/**
 * Drawable that scales and crops an image to cover its bounds, with an optional dim overlay.
 */
public final class MuiImageDrawable extends Drawable {
    private final Image image;
    private final float cropX;
    private final float cropY;
    private final float cropWidth;
    private final float cropHeight;
    private final int dimAlpha;
    private final Paint paint = new Paint();

    /**
     * Creates an image background drawable.
     *
     * @param image      source image
     * @param cropX      crop left in {@code 0..1}
     * @param cropY      crop top in {@code 0..1}
     * @param cropWidth  crop width in {@code 0..1}
     * @param cropHeight crop height in {@code 0..1}
     * @param dim        dim overlay in {@code 0..1}
     */
    public MuiImageDrawable(@NotNull Image image, double cropX, double cropY, double cropWidth,
                            double cropHeight, double dim) {
        this.image = image;
        this.cropX = (float) clamp01(cropX);
        this.cropY = (float) clamp01(cropY);
        this.cropWidth = (float) Math.max(0.0D, Math.min(1.0D - this.cropX, cropWidth));
        this.cropHeight = (float) Math.max(0.0D, Math.min(1.0D - this.cropY, cropHeight));
        this.dimAlpha = Math.max(0, Math.min(255, (int) Math.round(dim * 255.0D)));
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    @Override
    public void draw(@NotNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty() || this.image.isClosed()) return;

        int width = this.image.getWidth();
        int height = this.image.getHeight();
        if (width <= 0 || height <= 0) return;

        float sourceWidth = Math.max(1.0F, this.cropWidth * width);
        float sourceHeight = Math.max(1.0F, this.cropHeight * height);
        float sourceLeft = Math.min(this.cropX * width, width - sourceWidth);
        float sourceTop = Math.min(this.cropY * height, height - sourceHeight);

        float scale = Math.max(bounds.width() / sourceWidth, bounds.height() / sourceHeight);
        float drawWidth = sourceWidth * scale;
        float drawHeight = sourceHeight * scale;
        float left = bounds.left + (bounds.width() - drawWidth) / 2.0F;
        float top = bounds.top + (bounds.height() - drawHeight) / 2.0F;

        this.paint.setColor(0xFFFFFFFF);
        canvas.drawImage(this.image, sourceLeft, sourceTop, sourceLeft + sourceWidth, sourceTop + sourceHeight,
                left, top, left + drawWidth, top + drawHeight, this.paint);

        if (this.dimAlpha > 0) {
            this.paint.setColor(this.dimAlpha << 24);
            canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, this.paint);
        }
    }
}