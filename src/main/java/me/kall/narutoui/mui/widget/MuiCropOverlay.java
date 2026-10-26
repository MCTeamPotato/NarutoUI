package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.RectF;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.MuiControls;
import me.kall.narutoui.mui.MuiTheme;
import org.jetbrains.annotations.NotNull;

/**
 * Generic aspect-ratio image crop overlay with move/resize handles.
 */
public final class MuiCropOverlay extends FrameLayout {
    /**
     * Localized labels.
     *
     * @param title   title
     * @param hint    hint text
     * @param confirm confirm button
     * @param cancel  cancel button
     */
    public record Labels(CharSequence title, CharSequence hint, CharSequence confirm, CharSequence cancel) {}

    /**
     * Receives the crop result.
     */
    public interface Listener {
        /**
         * Called when the user confirms a crop.
         *
         * @param x      source X
         * @param y      source Y
         * @param width  source width
         * @param height source height
         */
        void onCrop(int x, int y, int width, int height);

        /**
         * Called when the user cancels cropping.
         */
        void onCancel();
    }

    /**
     * Creates a crop overlay.
     *
     * @param context      the screen context
     * @param theme        visual theme
     * @param image        preview image
     * @param imageWidth   source image width
     * @param imageHeight  source image height
     * @param targetAspect target width/height ratio
     * @param labels       localized labels
     * @param listener     crop callback
     */
    public MuiCropOverlay(@NotNull Context context, @NotNull MuiTheme theme, @NotNull Image image,
                          int imageWidth, int imageHeight, float targetAspect,
                          @NotNull Labels labels, @NotNull Listener listener) {
        super(context);
        setBackground(new ColorDrawable(0xF2090A0F));
        setClickable(true);

        int padding = MuiControls.dp(context, 20);
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(padding, padding, padding, padding);
        addView(column, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));

        TextView title = MuiControls.heading(context, theme, labels.title(), 18,
                theme.onSurface());
        column.addView(title, MuiControls.wrap());

        TextView hint = MuiControls.text(context, theme, labels.hint(), 12,
                theme.onSurfaceVariant());
        LinearLayout.LayoutParams hintParams = MuiControls.fill();
        hintParams.topMargin = MuiControls.dp(context, 4);
        column.addView(hint, hintParams);

        CropView cropView = new CropView(context, image, imageWidth, imageHeight, targetAspect);
        LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F);
        cropParams.topMargin = MuiControls.dp(context, 12);
        column.addView(cropView, cropParams);

        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams barParams = MuiControls.fill();
        barParams.topMargin = MuiControls.dp(context, 12);
        column.addView(bar, barParams);

        Button cancel = MuiControls.button(context, theme, labels.cancel(), true,
                view -> listener.onCancel());
        LinearLayout.LayoutParams cancelParams = MuiControls.wrap();
        cancelParams.setMargins(0, 0, MuiControls.dp(context, 8), 0);
        bar.addView(cancel, cancelParams);

        Button confirm = MuiControls.button(context, theme, labels.confirm(), false, view -> {
            int[] crop = cropView.crop();
            listener.onCrop(crop[0], crop[1], crop[2], crop[3]);
        });
        bar.addView(confirm, MuiControls.wrap());
    }

    private static final class CropView extends View {
        private static final int MODE_NONE = 0;
        private static final int MODE_MOVE = 1;
        private static final int MODE_TOP_LEFT = 2;
        private static final int MODE_TOP_RIGHT = 3;
        private static final int MODE_BOTTOM_LEFT = 4;
        private static final int MODE_BOTTOM_RIGHT = 5;

        private final Image image;
        private final int imageWidth;
        private final int imageHeight;
        private final float aspect;

        private final RectF display = new RectF();
        private final RectF crop = new RectF();
        private final Paint paint = new Paint();

        private int mode = MODE_NONE;
        private float downX;
        private float downY;
        private float startLeft;
        private float startTop;
        private float startRight;
        private float startBottom;

        CropView(@NotNull Context context, @NotNull Image image, int imageWidth, int imageHeight,
                 float aspect) {
            super(context);
            this.image = image;
            this.imageWidth = Math.max(1, imageWidth);
            this.imageHeight = Math.max(1, imageHeight);
            this.aspect = aspect > 0.0F ? aspect : 16.0F / 9.0F;
            this.paint.setAntiAlias(true);
            setClickable(true);
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            this.updateLayout(width, height);
        }

        private void updateLayout(int width, int height) {
            int padding = dp(8);
            float availableWidth = Math.max(1, width - padding * 2);
            float availableHeight = Math.max(1, height - padding * 2);
            float scale = Math.min(availableWidth / this.imageWidth, availableHeight / this.imageHeight);
            float drawWidth = this.imageWidth * scale;
            float drawHeight = this.imageHeight * scale;
            float left = (width - drawWidth) / 2.0F;
            float top = (height - drawHeight) / 2.0F;
            this.display.set(left, top, left + drawWidth, top + drawHeight);
            this.resetCrop();
        }

        private void resetCrop() {
            float width = this.display.width();
            float height = this.display.height();
            float cropWidth = width;
            float cropHeight = cropWidth / this.aspect;
            if (cropHeight > height) {
                cropHeight = height;
                cropWidth = cropHeight * this.aspect;
            }
            float left = this.display.left + (width - cropWidth) / 2.0F;
            float top = this.display.top + (height - cropHeight) / 2.0F;
            this.crop.set(left, top, left + cropWidth, top + cropHeight);
        }

        @Override
        protected void onDraw(@NotNull Canvas canvas) {
            if (this.display.isEmpty()) {
                this.updateLayout(getWidth(), getHeight());
            }
            if (this.display.isEmpty()) return;

            this.paint.setStyle(Paint.FILL);
            this.paint.setColor(0xFFFFFFFF);
            canvas.drawImage(this.image, 0, 0, this.imageWidth, this.imageHeight,
                    this.display.left, this.display.top, this.display.right, this.display.bottom,
                    this.paint);

            this.paint.setColor(0xB4000000);
            canvas.drawRect(0, 0, getWidth(), this.crop.top, this.paint);
            canvas.drawRect(0, this.crop.bottom, getWidth(), getHeight(), this.paint);
            canvas.drawRect(0, this.crop.top, this.crop.left, this.crop.bottom, this.paint);
            canvas.drawRect(this.crop.right, this.crop.top, getWidth(), this.crop.bottom, this.paint);

            this.paint.setStyle(Paint.STROKE);
            this.paint.setStrokeWidth(Math.max(1, dp(1)));
            this.paint.setColor(0x55FFFFFF);
            float thirdW = this.crop.width() / 3.0F;
            float thirdH = this.crop.height() / 3.0F;
            for (int i = 1; i < 3; i++) {
                float x = this.crop.left + thirdW * i;
                float y = this.crop.top + thirdH * i;
                canvas.drawLine(x, this.crop.top, x, this.crop.bottom, this.paint);
                canvas.drawLine(this.crop.left, y, this.crop.right, y, this.paint);
            }

            this.paint.setStyle(Paint.STROKE);
            this.paint.setStrokeWidth(Math.max(1, dp(1)));
            this.paint.setColor(0xFFFFFFFF);
            canvas.drawRect(this.crop.left, this.crop.top, this.crop.right, this.crop.bottom, this.paint);

            this.paint.setStyle(Paint.FILL);
            float handle = dp(5);
            this.handle(canvas, this.crop.left, this.crop.top, handle);
            this.handle(canvas, this.crop.right, this.crop.top, handle);
            this.handle(canvas, this.crop.left, this.crop.bottom, handle);
            this.handle(canvas, this.crop.right, this.crop.bottom, handle);
        }

        private void handle(@NotNull Canvas canvas, float x, float y, float size) {
            canvas.drawRect(x - size, y - size, x + size, y + size, this.paint);
        }

        @Override
        public boolean onTouchEvent(@NotNull MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    this.downX = x;
                    this.downY = y;
                    this.mode = this.hitTest(x, y);
                    this.startLeft = this.crop.left;
                    this.startTop = this.crop.top;
                    this.startRight = this.crop.right;
                    this.startBottom = this.crop.bottom;
                    return this.mode != MODE_NONE;
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (this.mode == MODE_NONE) return false;
                    if (this.mode == MODE_MOVE) {
                        this.moveBy(x - this.downX, y - this.downY);
                    } else {
                        this.resize(x, y);
                    }
                    invalidate();
                    return true;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    this.mode = MODE_NONE;
                    return true;
                }
                default -> {
                    return super.onTouchEvent(event);
                }
            }
        }

        private int hitTest(float x, float y) {
            float radius = dp(18);
            if (near(x, y, this.crop.left, this.crop.top, radius)) return MODE_TOP_LEFT;
            if (near(x, y, this.crop.right, this.crop.top, radius)) return MODE_TOP_RIGHT;
            if (near(x, y, this.crop.left, this.crop.bottom, radius)) return MODE_BOTTOM_LEFT;
            if (near(x, y, this.crop.right, this.crop.bottom, radius)) return MODE_BOTTOM_RIGHT;
            if (x >= this.crop.left && x <= this.crop.right && y >= this.crop.top && y <= this.crop.bottom) {
                return MODE_MOVE;
            }
            return MODE_NONE;
        }

        private static boolean near(float x, float y, float targetX, float targetY, float radius) {
            return Math.abs(x - targetX) <= radius && Math.abs(y - targetY) <= radius;
        }

        private void moveBy(float deltaX, float deltaY) {
            float width = this.startRight - this.startLeft;
            float height = this.startBottom - this.startTop;
            float left = clamp(this.startLeft + deltaX, this.display.left, this.display.right - width);
            float top = clamp(this.startTop + deltaY, this.display.top, this.display.bottom - height);
            this.crop.set(left, top, left + width, top + height);
        }

        private void resize(float x, float y) {
            float minWidth = Math.max(dp(48), 8);
            boolean fromRight = this.mode == MODE_TOP_LEFT || this.mode == MODE_BOTTOM_LEFT;
            boolean fromBottom = this.mode == MODE_TOP_LEFT || this.mode == MODE_TOP_RIGHT;

            float anchorX = fromRight ? this.startRight : this.startLeft;
            float anchorY = fromBottom ? this.startBottom : this.startTop;

            float maxWidth = fromRight ? anchorX - this.display.left : this.display.right - anchorX;
            float maxHeight = fromBottom ? anchorY - this.display.top : this.display.bottom - anchorY;

            float width = fromRight ? anchorX - x : x - anchorX;
            width = clamp(width, minWidth, maxWidth);

            float height = width / this.aspect;
            if (height > maxHeight) {
                height = maxHeight;
                width = height * this.aspect;
            }

            float left = fromRight ? anchorX - width : anchorX;
            float top = fromBottom ? anchorY - height : anchorY;
            this.crop.set(left, top, left + width, top + height);
        }

        int @NotNull [] crop() {
            if (this.display.isEmpty()) {
                return new int[]{0, 0, this.imageWidth, this.imageHeight};
            }

            float scaleX = this.imageWidth / this.display.width();
            float scaleY = this.imageHeight / this.display.height();

            int x = Math.round((this.crop.left - this.display.left) * scaleX);
            int y = Math.round((this.crop.top - this.display.top) * scaleY);
            int width = Math.round(this.crop.width() * scaleX);
            int height = Math.round(this.crop.height() * scaleY);

            x = clamp(x, 0, this.imageWidth - 1);
            y = clamp(y, 0, this.imageHeight - 1);
            width = clamp(width, 1, this.imageWidth - x);
            height = clamp(height, 1, this.imageHeight - y);
            return new int[]{x, y, width, height};
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(value, max));
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(value, max));
        }
    }
}
