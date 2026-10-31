package me.kall.narutoui.mui.shimmer;

import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewParent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives a chakra-style color wave across registered targets.
 * <p>
 * Two slowly interfering waves are sampled at each target's absolute position, so neighbouring
 * labels do not all change to the same colour at the same time. The wave value is mapped onto the
 * color ramp supplied by {@link MuiShimmerConfig}.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class MuiShimmer {
    private static final float SPATIAL_X = 0.008F;
    private static final float SPATIAL_Y = 0.014F;
    private static final float SPATIAL_X2 = 0.005F;
    private static final float SPATIAL_Y2 = 0.009F;
    private static final float TIME_SCALE = 0.45F;

    private static final List<ShimmerTarget> TARGETS = new ArrayList<>();
    private static @Nullable ValueAnimator animator;
    private static long lastStep;

    private MuiShimmer() {}

    /**
     * Installs the settings and color ramp used by the engine.
     *
     * @param config the config, or {@code null} to restore the default
     */
    public static void configure(@Nullable MuiShimmerConfig config) {
        MuiShimmerProfile.defaultProfile().configure(config);
    }

    /**
     * Returns the default profile's configuration.
     *
     * @return the config
     */
    public static @NotNull MuiShimmerConfig config() {
        return MuiShimmerProfile.defaultProfile().config();
    }

    /**
     * Registers a target. The target's profile controls its colors and enabled state.
     *
     * @param target the target
     */
    public static void attach(@NotNull ShimmerTarget target) {
        if (!TARGETS.contains(target)) TARGETS.add(target);
        refresh();
    }

    /**
     * Removes a target.
     *
     * @param target the target, or {@code null}
     */
    public static void detach(@Nullable ShimmerTarget target) {
        if (target == null) return;
        TARGETS.remove(target);
        if (TARGETS.isEmpty()) stop();
    }

    /**
     * Re-evaluates the enabled state and starts or stops the animator.
     */
    public static void refresh() {
        boolean active = false;
        for (ShimmerTarget target : TARGETS) {
            if (active(target)) {
                active = true;
                break;
            }
        }

        if (!active) {
            restore();
            stop();
            return;
        }
        start();
    }

    /**
     * Returns whether the animator is running.
     *
     * @return {@code true} when running
     */
    public static boolean isRunning() {
        return animator != null;
    }

    private static void start() {
        if (animator != null || TARGETS.isEmpty()) return;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        valueAnimator.setDuration(1000L);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(animation -> tick());
        valueAnimator.start();

        lastStep = 0L;
        animator = valueAnimator;
    }

    private static void stop() {
        if (animator == null) return;
        animator.cancel();
        animator = null;
    }

    private static void restore() {
        for (ShimmerTarget target : TARGETS) {
            target.applyBaseColor();
        }
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        int fps = 30;
        for (ShimmerTarget target : TARGETS) {
            if (active(target)) {
                fps = Math.max(fps, target.shimmerProfile().config().fps());
            }
        }
        long step = Math.max(1L, 1000L / fps);
        if (now - lastStep < step) return;
        lastStep = now;

        int[] location = new int[2];
        float[] stops = {0.0F, 0.12F, 0.25F, 0.40F, 0.55F, 0.70F, 0.82F, 0.92F, 1.0F};
        boolean active = false;

        for (ShimmerTarget target : TARGETS) {
            MuiShimmerProfile profile = target.shimmerProfile();
            if (!active(target)) {
                target.applyBaseColor();
                continue;
            }
            active = true;

            MuiShimmerConfig config = profile.config();
            View host = target.shimmerHost();
            if (host == null) continue;

            MuiShimmerClock clock = profile.clock();
            clock.update();
            float time = (float) clock.time();
            float scaleA = (float) clock.scaleFrom();
            float scaleB = (float) clock.scaleTo();
            float scaleMix = (float) clock.scaleMix();
            float strength = (float) clock.strength();
            int[] colors = config.ramp();

            host.getLocationInWindow(location);
            int x = location[0];
            int y = location[1];

            // getLocationInWindow() has already subtracted every ancestor's scroll offset.
            // Add it back so the sampled position is in content space and stays fixed while
            // the page scrolls.
            ViewParent parent = host.getParent();
            while (parent instanceof View view) {
                x += view.getScrollX();
                y += view.getScrollY();
                parent = view.getParent();
            }

            float waveA = wave(x, y, time, scaleA);
            float waveB = wave(x, y, time, scaleB);
            float wave = waveA + (waveB - waveA) * scaleMix;
            int color = rampColor(colors, stops, wave);
            target.applyShimmerColor(lerp(target.shimmerBaseColor(), color, target.shimmerStrength() * strength));
        }

        if (!active) {
            restore();
            stop();
        }
    }

    /**
     * Returns whether a target participates right now. This is the single place that combines the
     * target's local eligibility (including its marker), the global config and the profile clock.
     *
     * @param target the target
     * @return {@code true} when the target should animate
     */
    private static boolean active(@NotNull ShimmerTarget target) {
        if (!target.shimmerEnabled()) return false;

        View host = target.shimmerHost();
        if (host == null || !host.isAttachedToWindow()) return false;

        MuiShimmerProfile profile = target.shimmerProfile();
        MuiShimmerConfig config = profile.config();
        return config.enabled() && config.hasAccent();
    }

    private static float wave(float x, float y, float time, float scale) {
        float t = time * TIME_SCALE;
        float a = (float) Math.sin((x * SPATIAL_X + y * SPATIAL_Y) * scale - t);
        float b = (float) Math.sin((x * SPATIAL_X2 - y * SPATIAL_Y2) * scale + t * 0.7F + 1.3F);
        return (a + b) * 0.25F + 0.5F;
    }

    private static int rampColor(int[] colors, float @NotNull [] stops, float value) {
        float v = Math.max(0.0F, Math.min(1.0F, value));

        for (int i = 1; i < stops.length; i++) {
            if (v > stops[i]) continue;

            float span = stops[i] - stops[i - 1];
            float fraction = span <= 0.0F ? 0.0F : (v - stops[i - 1]) / span;
            return lerp(colors[i - 1], colors[i], fraction);
        }
        return colors[colors.length - 1];
    }

    private static int lerp(int from, int to, float fraction) {
        return ShimmerColor.blend(from, to, Math.max(0.0F, Math.min(1.0F, fraction)));
    }
}