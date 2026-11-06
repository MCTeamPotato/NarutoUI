package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.color.MuiColorBands;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;

/**
 * Paints a discrete color ramp.
 * <p>
 * The shimmer engine samples a finite ramp, so drawing one segment per stop keeps the preview an
 * honest representation of the colors a band field or a palette card will produce. Reusing the same
 * array as the engine avoids a second interpolation path that could disagree with the screen.
 */
public final class MuiColorBandPreview extends View {
    private final Paint paint = new Paint();
    private int[] colors = new int[0];

    /**
     * Creates an empty preview.
     *
     * @param context the screen context
     */
    public MuiColorBandPreview(@NotNull Context context) {
        super(context);
        this.paint.setAntiAlias(false);
        this.setClickable(false);
        this.setMinimumHeight(MuiControls.dp(context, 16));
    }

    /**
     * Creates a preview with initial colors and height.
     *
     * @param context  the screen context
     * @param colors   the ramp stops
     * @param heightDp height in density-independent pixels
     * @return the preview
     */
    public static @NotNull MuiColorBandPreview create(@NotNull Context context, int @NotNull [] colors, int heightDp) {
        MuiColorBandPreview preview = new MuiColorBandPreview(context);
        preview.height(heightDp);
        preview.colors(colors);
        return preview;
    }

    /**
     * Replaces the previewed ramp.
     *
     * @param colors the ARGB stops
     * @return this preview, for chaining
     */
    public @NotNull MuiColorBandPreview colors(int @NotNull [] colors) {
        this.colors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            this.colors[i] = MuiColorBands.opaque(colors[i]);
        }
        this.invalidate();
        return this;
    }

    /**
     * Sets the preview height.
     *
     * @param heightDp height in density-independent pixels
     * @return this preview, for chaining
     */
    public @NotNull MuiColorBandPreview height(int heightDp) {
        this.setMinimumHeight(MuiControls.dp(this.getContext(), heightDp));
        return this;
    }

    @Override
    protected void onDraw(@NotNull Canvas canvas) {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width <= 0 || height <= 0 || this.colors.length == 0) {
            return;
        }

        float segment = width / (float) this.colors.length;
        for (int i = 0; i < this.colors.length; i++) {
            this.paint.setColor(this.colors[i]);
            float left = segment * i;
            float right = segment * (i + 1);
            if (i < this.colors.length - 1) {
                right += 1.0F;
            }
            canvas.drawRect(left, 0.0F, right, height, this.paint);
        }
    }
}