package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A named color scheme shared by configuration screens and overlays.
 * <p>
 * A scheme carries three related pieces:
 * <ul>
 *     <li>{@link #theme()} for Modern UI configuration screens,</li>
 *     <li>{@link #hud()} for HUD/overlay consumers,</li>
 *     <li>a nine-stop {@link #shimmerRamp()} in the scheme's own color family.</li>
 * </ul>
 * The id is stable and is what consumers store in their config. New schemes registered in
 * {@link MuiColorSchemes} automatically appear in every consumer that iterates the catalog.
 *
 * @param id          stable id, for example {@code JADE}
 * @param nameKey     translation key for the display name
 * @param theme       UI tokens
 * @param hud         HUD tokens
 * @param shimmerRamp nine ARGB shimmer stops
 */
public record MuiColorScheme(@NotNull String id, @NotNull String nameKey,
                             @NotNull MuiThemePalette theme, @NotNull MuiHudPalette hud,
                             int @NotNull [] shimmerRamp) {
    public MuiColorScheme {
        id = id.toUpperCase(Locale.ROOT);
        shimmerRamp = shimmerRamp.clone();
    }

    @Override
    public int @NotNull [] shimmerRamp() {
        return shimmerRamp.clone();
    }

    /**
     * @return the accent color used by the scheme's shimmer and HUD accent strip
     */
    public int accent() {
        return this.hud.hasAccent() ? this.hud.accent() : this.theme.primary();
    }
}