package me.kall.narutoui.mui.shimmer;

import org.jetbrains.annotations.NotNull;

/**
 * Clock for one shimmer profile. HUD and configuration screens can use independent clocks instead
 * of sharing one global phase and speed.
 */
public final class MuiShimmerClock {
    private static final double MAX_STEP_SECONDS = 0.1D;
    private static final double SMOOTHING = 10.0D;
    private static final long SCALE_DEBOUNCE_MS = 140L;
    private static final long SCALE_TRANSITION_MS = 900L;
    private static final double SCALE_EPSILON = 0.001D;

    private final MuiShimmerConfig config;
    private boolean initialized;
    private long lastMillis;
    private double time;
    private double speed = 1.0D;
    private double strength = 1.0D;
    private double scaleFrom = 1.0D;
    private double scaleTo = 1.0D;
    private double scaleMix = 1.0D;
    private double pendingScale = 1.0D;
    private long pendingSince;
    private long scaleTransitionStart;
    private boolean scaleTransition;

    MuiShimmerClock(@NotNull MuiShimmerConfig config) {
        this.config = config;
    }

    /**
     * Advances the clock from the profile configuration.
     */
    public synchronized void update() {
        long now = System.currentTimeMillis();

        double targetSpeed = this.config.speed();
        double targetScale = this.config.scale();
        double targetStrength = this.config.strength();

        if (!this.initialized) {
            this.initialized = true;
            this.lastMillis = now;
            this.pendingSince = now;
            this.speed = targetSpeed;
            this.strength = targetStrength;
            this.scaleFrom = targetScale;
            this.scaleTo = targetScale;
            this.pendingScale = targetScale;
            this.scaleMix = 1.0D;
            return;
        }

        double dt = Math.max(0.0D, Math.min(MAX_STEP_SECONDS, (now - this.lastMillis) / 1000.0D));
        this.lastMillis = now;

        if (dt > 0.0D) {
            double blend = 1.0D - Math.exp(-dt * SMOOTHING);
            this.speed += (targetSpeed - this.speed) * blend;
            this.strength += (targetStrength - this.strength) * blend;
            this.time += dt * this.speed;
        }

        if (Math.abs(targetScale - this.pendingScale) > SCALE_EPSILON) {
            this.pendingScale = targetScale;
            this.pendingSince = now;
        }

        if (this.scaleTransition) {
            double progress = (now - this.scaleTransitionStart) / (double) SCALE_TRANSITION_MS;
            if (progress >= 1.0D) {
                this.scaleFrom = this.scaleTo;
                this.scaleMix = 1.0D;
                this.scaleTransition = false;
            } else {
                double p = Math.max(0.0D, progress);
                this.scaleMix = p * p * (3.0D - 2.0D * p);
            }
        }

        if (!this.scaleTransition && now - this.pendingSince >= SCALE_DEBOUNCE_MS
                && Math.abs(this.pendingScale - this.scaleTo) > SCALE_EPSILON) {
            this.scaleFrom = this.scaleTo;
            this.scaleTo = this.pendingScale;
            this.scaleMix = 0.0D;
            this.scaleTransitionStart = now;
            this.scaleTransition = true;
        }
    }

    /**
     * Returns the accumulated phase.
     *
     * @return phase in seconds
     */
    public synchronized double time() {
        return this.time;
    }

    /**
     * Returns the previous density frequency.
     *
     * @return spatial scale
     */
    public synchronized double scaleFrom() {
        return this.scaleFrom;
    }

    /**
     * Returns the target density frequency.
     *
     * @return spatial scale
     */
    public synchronized double scaleTo() {
        return this.scaleTo;
    }

    /**
     * Returns the cross-fade progress between {@link #scaleFrom()} and {@link #scaleTo()}.
     *
     * @return mix in {@code 0..1}
     */
    public synchronized double scaleMix() {
        return this.scaleMix;
    }

    /**
     * Returns the smoothed strength.
     *
     * @return strength in {@code 0..1}
     */
    public synchronized double strength() {
        return this.strength;
    }
}