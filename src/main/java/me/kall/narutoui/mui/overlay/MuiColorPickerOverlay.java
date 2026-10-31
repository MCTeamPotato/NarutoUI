package me.kall.narutoui.mui.overlay;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.*;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Locale;

/**
 * Generic ARGB color picker overlay with a hue wheel, brightness/alpha bars and apply/cancel actions.
 */
public final class MuiColorPickerOverlay extends FrameLayout {
    /**
     * Localized labels.
     *
     * @param title      picker title
     * @param brightness brightness label
     * @param alpha      alpha label
     * @param apply      apply button
     * @param cancel     cancel button
     */
    public record Labels(CharSequence title, CharSequence brightness, CharSequence alpha, CharSequence apply, CharSequence cancel) {}

    /**
     * Receives the selected color.
     */
    @FunctionalInterface
    public interface Listener {
        /**
         * Called when the user confirms a color.
         *
         * @param argb selected ARGB color
         */
        void onPick(int argb);
    }

    private static final float WHEEL_PADDING = 6.0F;
    private static final int HUE_SLICES = 180;

    private final MuiTheme theme;
    private final Listener listener;

    private final WheelView wheel;
    private final GradientBar brightnessBar;
    private final GradientBar alphaBar;
    private final View swatch;
    private final TextView hexLabel;

    private float hue;
    private float saturation;
    private float value;
    private int alpha;

    /**
     * Creates a color picker overlay.
     *
     * @param context  the screen context
     * @param theme    visual theme
     * @param argb     initial ARGB color
     * @param labels   localized labels
     * @param listener pick callback
     */
    public MuiColorPickerOverlay(@NotNull Context context, @NotNull MuiTheme theme, int argb, @NotNull Labels labels, @NotNull Listener listener) {
        super(context);
        this.theme = theme;
        this.listener = listener;

        this.alpha = (argb >>> 24) & 0xFF;
        float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.value = hsb[2];

        this.setBackground(new ColorDrawable(0xF2090A0F));
        this.setClickable(true);

        int padding = MuiControls.dp(context, 20);
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(padding, padding, padding, padding);
        this.addView(column, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));

        column.addView(MuiControls.heading(context, this.theme, labels.title(), 18, this.theme.onSurface()), MuiControls.wrap());

        this.wheel = new WheelView(context);
        this.wheel.setListener((h, s) -> {
            this.hue = h;
            this.saturation = s;
            this.refresh();
        });
        LinearLayout.LayoutParams wheelParams = new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F);
        wheelParams.topMargin = MuiControls.dp(context, 12);
        column.addView(this.wheel, wheelParams);

        column.addView(this.caption(labels.brightness()), this.captionParams());
        this.brightnessBar = new GradientBar(context);
        this.brightnessBar.setListener(fraction -> {
            this.value = 1.0F - fraction;
            this.refresh();
        });
        column.addView(this.brightnessBar, this.barParams(18));

        column.addView(this.caption(labels.alpha()), this.captionParams());
        this.alphaBar = new GradientBar(context);
        this.alphaBar.setListener(fraction -> {
            this.alpha = Math.round(fraction * 255.0F);
            this.refresh();
        });
        column.addView(this.alphaBar, this.barParams(18));

        LinearLayout footer = MuiControls.row(context);
        this.swatch = new View(context);
        footer.addView(this.swatch, new LinearLayout.LayoutParams(
                MuiControls.dp(context, 26), MuiControls.dp(context, 26)));
        this.hexLabel = MuiControls.text(context, this.theme, "", 12, this.theme.onSurfaceVariant());
        LinearLayout.LayoutParams hexParams = new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F);
        hexParams.setMargins(MuiControls.dp(context, 12), 0, 0, 0);
        footer.addView(this.hexLabel, hexParams);
        column.addView(footer, MuiControls.fill());

        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams buttonsParams = MuiControls.fill();
        buttonsParams.topMargin = MuiControls.dp(context, 10);
        column.addView(buttons, buttonsParams);

        LinearLayout.LayoutParams cancelParams = MuiControls.wrap();
        cancelParams.setMargins(0, 0, MuiControls.dp(context, 8), 0);
        buttons.addView(MuiControls.button(context, this.theme, labels.cancel(), true, view -> this.dismiss()), cancelParams);
        buttons.addView(MuiControls.button(context, this.theme, labels.apply(), false, view -> this.confirm()), MuiControls.wrap());

        this.refresh();

        float offset = MuiControls.dp(this.getContext(), 10);
        this.setTranslationY(offset);
        this.post(() -> {
            ValueAnimator animator = ValueAnimator.ofFloat(0.0F, 1.0F);
            animator.setDuration(160L);
            animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD_DECELERATE);
            animator.addUpdateListener(animation -> this.setTranslationY(offset * (1.0F - (Float) animation.getAnimatedValue())));
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationEnd(@NotNull Animator animation) {
                    MuiColorPickerOverlay.this.setTranslationY(0.0F);
                }
            });
            animator.start();
        });
    }

    private @NotNull TextView caption(@NotNull CharSequence text) {
        return MuiControls.text(this.getContext(), this.theme, text, 11, this.theme.onSurfaceVariant());
    }

    private @NotNull LinearLayout.LayoutParams captionParams() {
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(0, MuiControls.dp(this.getContext(), 14), 0, MuiControls.dp(this.getContext(), 4));
        return params;
    }

    @SuppressWarnings("SameParameterValue")
    private @NotNull LinearLayout.LayoutParams barParams(int heightDp) {
        return new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.dp(this.getContext(), heightDp));
    }

    private int currentArgb() {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.value);
        return this.alpha << 24 | rgb & 0xFFFFFF;
    }

    private int pureColor() {
        return 0xFF000000 | Color.HSBtoRGB(this.hue, this.saturation, 1.0F) & 0xFFFFFF;
    }

    private void refresh() {
        this.wheel.setState(this.hue, this.saturation, this.value);
        this.brightnessBar.setColors(this.pureColor(), 0xFF000000);
        this.brightnessBar.setFraction(1.0F - this.value);
        this.alphaBar.setColors(this.currentArgb() & 0x00FFFFFF, 0xFF000000 | this.currentArgb() & 0xFFFFFF);
        this.alphaBar.setFraction(this.alpha / 255.0F);

        int argb = this.currentArgb();
        this.swatch.setBackground(this.coloredRoundRect(argb));
        this.hexLabel.setText(String.format(Locale.ROOT, "#%08X", argb));
    }

    private @NotNull ShapeDrawable coloredRoundRect(int argb) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setCornerRadius(MuiControls.dp(this.getContext(), 6));
        drawable.setColor(argb);
        drawable.setStroke(Math.max(1, MuiControls.dp(this.getContext(), 1)), ColorStateList.modulateColor(0xFFFFFFFF, 0.25F));
        return drawable;
    }

    private void confirm() {
        int argb = this.currentArgb();
        this.dismiss();
        this.listener.onPick(argb);
    }

    private void dismiss() {
        ViewGroup parent = (ViewGroup) this.getParent();
        if (parent != null) parent.removeView(this);
    }

    private static final class WheelView extends View {
        interface Listener {
            void onPick(float hue, float saturation);
        }

        private final Paint shaderPaint = new Paint();
        private final Paint solidPaint = new Paint();
        private @Nullable RadialGradient saturationShader;
        private float shaderCenterX;
        private float shaderCenterY;
        private float shaderRadius;

        private float hue = 0.0F;
        private float saturation = 0.0F;
        private float value = 1.0F;
        private @Nullable Listener listener;

        WheelView(@NotNull Context context) {
            super(context);
            this.solidPaint.setAntiAlias(true);
            this.shaderPaint.setAntiAlias(true);
            this.setClickable(true);
        }

        void setListener(@Nullable Listener listener) {
            this.listener = listener;
        }

        void setState(float hue, float saturation, float value) {
            this.hue = hue;
            this.saturation = saturation;
            this.value = value;
            this.invalidate();
        }

        private float radius() {
            return Math.min(this.getWidth(), this.getHeight()) / 2.0F - this.dp(WHEEL_PADDING);
        }

        private void ensureShaders(float cx, float cy, float radius) {
            if (this.saturationShader != null && cx == this.shaderCenterX && cy == this.shaderCenterY && radius == this.shaderRadius) return;

            this.shaderCenterX = cx;
            this.shaderCenterY = cy;
            this.shaderRadius = radius;

            this.saturationShader = new RadialGradient(cx, cy, Math.max(1.0F, radius), 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP, null);
        }

        @Override
        protected void onDraw(@NotNull Canvas canvas) {
            float radius = this.radius();
            if (radius <= 0.0F) return;

            float cx = this.getWidth() / 2.0F;
            float cy = this.getHeight() / 2.0F;
            this.ensureShaders(cx, cy, radius);

            float sweep = 360.0F / HUE_SLICES;
            for (int i = 0; i < HUE_SLICES; i++) {
                this.solidPaint.setColor(0xFF000000 | Color.HSBtoRGB(i / (float) HUE_SLICES, 1.0F, 1.0F) & 0xFFFFFF);
                canvas.drawPie(cx, cy, radius, i * sweep, sweep + 0.5F, this.solidPaint);
            }

            this.shaderPaint.setShader(this.saturationShader);
            canvas.drawCircle(cx, cy, radius, this.shaderPaint);

            if (this.value < 1.0F) {
                this.solidPaint.setColor(Math.round((1.0F - this.value) * 255.0F) << 24);
                canvas.drawCircle(cx, cy, radius, this.solidPaint);
            }

            double angle = this.hue * Math.PI * 2.0D;
            float indicatorX = cx + (float) Math.cos(angle) * this.saturation * radius;
            float indicatorY = cy + (float) Math.sin(angle) * this.saturation * radius;

            this.solidPaint.setStyle(Paint.STROKE);
            this.solidPaint.setStrokeWidth(this.dp(2.0F));
            this.solidPaint.setColor(0xFF000000);
            canvas.drawCircle(indicatorX, indicatorY, this.dp(7.0F), this.solidPaint);
            this.solidPaint.setColor(0xFFFFFFFF);
            canvas.drawCircle(indicatorX, indicatorY, this.dp(5.0F), this.solidPaint);
            this.solidPaint.setStyle(Paint.FILL);
        }

        @Override
        public boolean onTouchEvent(@NotNull MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    float radius = Math.max(1.0F, this.radius());
                    float dx = event.getX() - this.getWidth() / 2.0F;
                    float dy = event.getY() - this.getHeight() / 2.0F;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    float newSaturation = Math.min(1.0F, distance / radius);

                    float newHue = this.hue;
                    if (distance > 1.0E-4F) {
                        float degrees = (float) Math.toDegrees(Math.atan2(dy, dx));
                        if (degrees < 0.0F) degrees += 360.0F;
                        newHue = degrees / 360.0F;
                    }
                    if (this.listener != null) this.listener.onPick(newHue, newSaturation);
                    return true;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    return true;
                }
                default -> {
                    return super.onTouchEvent(event);
                }
            }
        }
    }

    private static final class GradientBar extends View {
        @FunctionalInterface
        interface Listener {
            void onFraction(float fraction);
        }

        private final Paint shaderPaint = new Paint();
        private final Paint linePaint = new Paint();
        private int startColor = 0xFF000000;
        private int endColor = 0xFFFFFFFF;
        private float fraction;
        private @Nullable Listener listener;
        private @Nullable LinearGradient shader;
        private int shaderWidth;
        private int shaderStart;
        private int shaderEnd;

        GradientBar(@NotNull Context context) {
            super(context);
            this.shaderPaint.setAntiAlias(true);
            this.linePaint.setAntiAlias(true);
            this.setClickable(true);
        }

        void setListener(@Nullable Listener listener) {
            this.listener = listener;
        }

        void setColors(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.shader = null;
            this.invalidate();
        }

        void setFraction(float fraction) {
            this.fraction = Math.max(0.0F, Math.min(1.0F, fraction));
            this.invalidate();
        }

        private void ensureShader(int width) {
            if (this.shader != null && width == this.shaderWidth && this.startColor == this.shaderStart && this.endColor == this.shaderEnd) return;

            this.shaderWidth = width;
            this.shaderStart = this.startColor;
            this.shaderEnd = this.endColor;
            this.shader = new LinearGradient(0.0F, 0.0F, Math.max(1, width), 0.0F, this.startColor, this.endColor, Shader.TileMode.CLAMP, null);
        }

        @Override
        protected void onDraw(@NotNull Canvas canvas) {
            int width = this.getWidth();
            int height = this.getHeight();
            if (width <= 0 || height <= 0) return;

            this.ensureShader(width);
            this.shaderPaint.setShader(this.shader);
            canvas.drawRect(0.0F, 0.0F, width, height, this.shaderPaint);

            float x = this.fraction * width;
            this.linePaint.setStyle(Paint.STROKE);
            this.linePaint.setStrokeWidth(this.dp(2.0F));
            this.linePaint.setColor(0xFF000000);
            canvas.drawLine(x, 0.0F, x, height, this.linePaint);
            this.linePaint.setStrokeWidth(this.dp(1.0F));
            this.linePaint.setColor(0xFFFFFFFF);
            canvas.drawLine(x, 0.0F, x, height, this.linePaint);
        }

        @Override
        public boolean onTouchEvent(@NotNull MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    float fraction = Math.max(0.0F, Math.min(1.0F, event.getX() / Math.max(1.0F, this.getWidth())));
                    if (this.listener != null) this.listener.onFraction(fraction);
                    return true;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    return true;
                }
                default -> {
                    return super.onTouchEvent(event);
                }
            }
        }
    }
}
