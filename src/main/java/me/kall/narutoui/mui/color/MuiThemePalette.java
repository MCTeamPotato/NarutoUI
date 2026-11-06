package me.kall.narutoui.mui.color;

import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;

/**
 * UI-facing color tokens of a named color scheme.
 * <p>
 * These map directly to {@link MuiTheme}; a consumer can create a complete theme without knowing
 * whether the scheme also carries HUD tokens.
 *
 * @param primary          main accent color
 * @param onPrimary        content color on {@code primary}
 * @param surface          screen surface color
 * @param surfaceLow       card color
 * @param surfaceHigh      control color
 * @param onSurface        primary text color
 * @param onSurfaceVariant secondary text color
 * @param outline          divider and border color
 * @param error            destructive-action color
 */
public record MuiThemePalette(int primary, int onPrimary, int surface, int surfaceLow, int surfaceHigh,
                              int onSurface, int onSurfaceVariant, int outline, int error) {
    /**
     * Builds a Modern UI theme from these tokens.
     *
     * @param cornerRadius corner radius in DP
     * @param spacing      content spacing in DP
     * @return the theme
     */
    public @NotNull MuiTheme toTheme(int cornerRadius, int spacing) {
        return new MuiTheme(
                this.primary,
                this.onPrimary,
                this.surface,
                this.surfaceLow,
                this.surfaceHigh,
                this.onSurface,
                this.onSurfaceVariant,
                this.outline,
                this.error,
                cornerRadius,
                spacing
        );
    }
}