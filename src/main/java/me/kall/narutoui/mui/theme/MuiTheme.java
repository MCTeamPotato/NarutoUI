package me.kall.narutoui.mui.theme;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.resources.TypedValue;
import me.kall.narutoui.mui.color.MuiColorSchemes;
import me.kall.narutoui.mui.color.MuiThemePalette;
import org.jetbrains.annotations.NotNull;

/**
 * Visual tokens used by the {@code me.kall.narutoui.mui} configuration-screen API.
 * <p>
 * The default theme resolves the current Modern UI attributes and falls back to a neutral dark
 * palette when the host theme does not provide them. All geometric values are in density-independent
 * pixels so the screen scales with Modern UI's GUI scale.
 *
 * @param primary          main accent color
 * @param onPrimary        content color on {@code primary}
 * @param surface          screen surface color
 * @param surfaceLow       low-emphasis card color
 * @param surfaceHigh      high-emphasis control color
 * @param onSurface        primary text color
 * @param onSurfaceVariant secondary text color
 * @param outline          divider and border color
 * @param error            destructive-action color
 * @param cornerRadius     corner radius in density-independent pixels
 * @param spacing          page/content spacing in density-independent pixels
 */
public record MuiTheme(int primary, int onPrimary, int surface, int surfaceLow, int surfaceHigh, int onSurface, int onSurfaceVariant, int outline, int error, int cornerRadius, int spacing) {
    /** Default corner radius in density-independent pixels. */
    public static final int DEFAULT_CORNER_RADIUS = 12;
    /** Default page and content spacing in density-independent pixels. */
    public static final int DEFAULT_SPACING = 20;

    /**
     * Resolves the current Modern UI theme attributes, falling back to the catalog's Naruto palette.
     * <p>
     * The fallback is not spelled out here: it is the same generated palette every consumer sees,
     * so a change to the shared scheme cannot leave the resolved theme behind.
     *
     * @param context the screen context
     * @return a theme resolved from {@code context}
     */
    public static @NotNull MuiTheme modern(@NotNull Context context) {
        MuiThemePalette fallback = MuiColorSchemes.NARUTO.theme();
        return new MuiTheme(
                attr(context, R.attr.colorPrimary, fallback.primary()),
                attr(context, R.attr.colorOnPrimary, fallback.onPrimary()),
                attr(context, R.attr.colorSurface, fallback.surface()),
                attr(context, R.attr.colorSurfaceContainerLow, fallback.surfaceLow()),
                attr(context, R.attr.colorSurfaceContainerHighest, fallback.surfaceHigh()),
                attr(context, R.attr.colorOnSurface, fallback.onSurface()),
                attr(context, R.attr.colorOnSurfaceVariant, fallback.onSurfaceVariant()),
                attr(context, R.attr.colorOutlineVariant, fallback.outline()),
                attr(context, R.attr.colorError, fallback.error()),
                DEFAULT_CORNER_RADIUS,
                DEFAULT_SPACING
        );
    }

    /**
     * Returns a copy with a different accent color.
     *
     * @param color the replacement primary color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withPrimary(int color) {
        return new MuiTheme(color, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different content color on primary-filled controls.
     *
     * @param color the replacement on-primary color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withOnPrimary(int color) {
        return new MuiTheme(this.primary, color, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different screen surface color.
     *
     * @param color the replacement surface color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withSurface(int color) {
        return new MuiTheme(this.primary, this.onPrimary, color, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different low-emphasis card color.
     *
     * @param color the replacement low-emphasis surface color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withSurfaceLow(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, color, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different high-emphasis control color.
     *
     * @param color the replacement high-emphasis surface color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withSurfaceHigh(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, color, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different primary text color.
     *
     * @param color the replacement on-surface color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withOnSurface(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, color, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different secondary text color.
     *
     * @param color the replacement on-surface-variant color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withOnSurfaceVariant(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, color, this.outline, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different divider and border color.
     *
     * @param color the replacement outline color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withOutline(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, color, this.error, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different destructive-action color.
     *
     * @param color the replacement error color
     * @return a new theme instance
     */
    public @NotNull MuiTheme withError(int color) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, color, this.cornerRadius, this.spacing);
    }

    /**
     * Returns a copy with a different page content spacing.
     *
     * @param spacing spacing in density-independent pixels
     * @return a new theme instance
     */
    public @NotNull MuiTheme withSpacing(int spacing) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, this.cornerRadius, spacing);
    }

    /**
     * Returns a copy with a different corner radius.
     *
     * @param cornerRadius radius in density-independent pixels
     * @return a new theme instance
     */
    public @NotNull MuiTheme withCornerRadius(int cornerRadius) {
        return new MuiTheme(this.primary, this.onPrimary, this.surface, this.surfaceLow, this.surfaceHigh, this.onSurface, this.onSurfaceVariant, this.outline, this.error, cornerRadius, this.spacing);
    }

    /**
     * Resolves a theme attribute with a fallback.
     *
     * @param context  the screen context
     * @param name     the attribute name
     * @param fallback fallback ARGB value
     * @return the resolved color
     */
    @SuppressWarnings("UnstableApiUsage")
    private static int attr(@NotNull Context context, @NotNull String name, int fallback) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(R.ns, name, value, true) ? value.data : fallback;
    }
}