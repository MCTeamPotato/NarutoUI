package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Builds a complete color scheme from one brand color.
 * <p>
 * Everything is generated in OKLCH, so a scheme cannot end up with muddy midpoints or a divider
 * that disappears into its card. The generator owns three layers:
 * <ul>
 *     <li>{@link #tokens(int, MuiSurfaceTint)} — the nine UI tokens,</li>
 *     <li>{@link #hud(MuiThemePalette, MuiHudPreset)} — the nine overlay tokens,</li>
 *     <li>{@link #ramp(MuiThemePalette, MuiHudPalette, MuiRampStyle)} — the nine shimmer stops.</li>
 * </ul>
 * The neutral ladder is a fixed set of lightness offsets from the surface, always moving toward
 * the text pole, so the same code serves dark and light surfaces. Contrast floors are enforced by
 * moving lightness only, which keeps the hue and the role of each token intact.
 * <p>
 * Results are pure functions of the inputs and are meant to be computed once when a scheme is
 * registered, never per frame.
 */
public final class MuiSchemeGenerator {
    /** Hue of the neutral-cool ladder, used when the surface does not follow the brand. */
    public static final double COOL_HUE = 270.0D;

    /** Lightness distance from the surface to the card. */
    private static final double CARD_DISTANCE = 0.047D;
    /** Lightness distance from the surface to a control. */
    private static final double CONTROL_DISTANCE = 0.097D;
    /** Lightness distance from the surface to primary text. */
    private static final double TEXT_DISTANCE = 0.777D;
    /** Lightness distance from the surface to secondary text. */
    private static final double SECONDARY_TEXT_DISTANCE = 0.572D;
    /** Lightness distance from the surface to a divider. */
    private static final double DIVIDER_DISTANCE = 0.182D;

    /** Chroma multiplier applied to the ladder when it does not follow the brand. */
    private static final double NEUTRAL_SCALE = 0.42D;

    private static final double SURFACE_CHROMA = 0.019D;
    private static final double CARD_CHROMA = 0.027D;
    private static final double CONTROL_CHROMA = 0.035D;
    private static final double TEXT_CHROMA = 0.020D;
    private static final double SECONDARY_TEXT_CHROMA = 0.039D;
    private static final double DIVIDER_CHROMA = 0.043D;

    private static final double INK_LIGHTNESS = 0.200D;
    private static final double WHITE_LIGHTNESS = 0.985D;

    /** Destructive-action hue anchor; fixed so a green scheme does not get a green error color. */
    private static final double ERROR_HUE = 23.0D;
    /** Warning hue anchor. */
    private static final double WARN_HUE = 78.0D;

    private static final double ERROR_LIGHTNESS = 0.710D;
    private static final double ERROR_CHROMA = 0.181D;
    private static final double WARN_LIGHTNESS = 0.850D;
    private static final double WARN_CHROMA = 0.150D;
    private static final double CRITICAL_LIGHTNESS = 0.680D;
    private static final double CRITICAL_CHROMA = 0.190D;

    private static final double TEXT_CONTRAST = 7.5D;
    private static final double PRIMARY_CONTRAST = 3.0D;
    private static final double STATUS_CONTRAST = 4.5D;

    private static final int BACKPLANE_ALPHA = 0xB0;
    private static final int BORDER_ALPHA = 0xCC;

    /** Lightness of the first colored stop of a hue sweep. */
    private static final double SWEEP_TOP_LIGHTNESS = 0.880D;
    /**
     * Lightness of the deep end of a hue sweep.
     * <p>
     * Low enough for a blood red or an ink green to actually exist, high enough to stay readable on
     * the dark surface these schemes use.
     */
    private static final double SWEEP_END_LIGHTNESS = 0.420D;
    /** Fraction of the sRGB gamut the sweep is allowed to use at the deep end. */
    private static final double SWEEP_CEILING = 0.970D;
    /**
     * Fraction of the gamut at the light end of a sweep.
     * <p>
     * A pale tint, not a saturated color: a fully chromatic cyan or lime at high lightness reads as
     * neon rather than as light on the surface.
     */
    private static final double SWEEP_HIGHLIGHT_CHROMA = 0.450D;
    /** How quickly chroma grows toward the deep end; below 1 it opens up early. */
    private static final double SWEEP_CHROMA_GAIN = 0.700D;

    private static final MuiHudPalette CHAKRA = new MuiHudPalette(
            0xFFFF8F2A, 0xFFE0A64A, 0xFFA03A26, 0xFFFFFFFF, 0xFFFFD64A,
            0xFFFF4A38, 0xB01E0A08, 0xCCE33A26, 0xFFFFC42E);

    private static final MuiHudPalette NEUTRAL_HUD = new MuiHudPalette(
            0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFF55,
            0xFFFF5555, 0xA0404040, 0xFFFFFFFF, 0x00000000);

    private static final int[] NEUTRAL_RAMP = {
            0xFFFFFFFF, 0xFFE6E6E6, 0xFFD0D0D0, 0xFFB8B8B8, 0xFF8E8E8E,
            0xFFB8B8B8, 0xFFD0D0D0, 0xFFE6E6E6, 0xFFFFFFFF};

    private MuiSchemeGenerator() {}

    /**
     * Builds the UI tokens of a scheme whose surface is derived from the brand.
     *
     * @param brand brand color
     * @param tint  where the neutral ladder takes its hue from
     * @return the nine UI tokens
     */
    public static @NotNull MuiThemePalette tokens(int brand, @NotNull MuiSurfaceTint tint) {
        return tokens(brand, tint, 0);
    }

    /**
     * Builds the UI tokens of a scheme, optionally over a caller-chosen surface.
     * <p>
     * A surface override is how a consumer honours a player-picked background: the rest of the
     * ladder and every contrast floor is derived from it, so a light surface automatically gets
     * dark text instead of unreadable light text.
     *
     * @param brand           brand color
     * @param tint            where the neutral ladder takes its hue from
     * @param surfaceOverride surface color, or {@code 0} to derive one from the brand
     * @return the nine UI tokens
     */
    public static @NotNull MuiThemePalette tokens(int brand, @NotNull MuiSurfaceTint tint, int surfaceOverride) {
        MuiOklch brandColor = MuiOklch.fromArgb(MuiColorMath.opaque(brand));
        double brandHue = brandColor.hue();
        double brandChroma = brandColor.chroma();
        boolean brandTint = tint == MuiSurfaceTint.BRAND;

        int surface = surfaceOverride == 0
                ? new MuiOklch(0.178D, SURFACE_CHROMA * (brandTint ? 1.0D : NEUTRAL_SCALE),
                        brandTint ? brandHue : COOL_HUE).toArgb()
                : MuiColorMath.opaque(surfaceOverride);

        MuiOklch surfaceColor = MuiOklch.fromArgb(surface);
        double surfaceLightness = surfaceColor.lightness();
        double ladderHue = surfaceOverride == 0
                ? (brandTint ? brandHue : COOL_HUE)
                : surfaceColor.hue();
        // Dark surfaces lift toward the light text pole, light surfaces drop toward the dark one.
        double direction = surfaceLightness < 0.5D ? 1.0D : -1.0D;
        double scale = brandTint ? 1.0D : NEUTRAL_SCALE;

        int surfaceLow = offset(surfaceLightness, direction, CARD_DISTANCE, ladderHue, CARD_CHROMA * scale);
        int surfaceHigh = offset(surfaceLightness, direction, CONTROL_DISTANCE, ladderHue, CONTROL_CHROMA * scale);
        int onSurface = offset(surfaceLightness, direction, TEXT_DISTANCE, ladderHue, TEXT_CHROMA * scale);
        int onSurfaceVariant = MuiColorMath.enforceContrast(
                offset(surfaceLightness, direction, SECONDARY_TEXT_DISTANCE, ladderHue, SECONDARY_TEXT_CHROMA * scale),
                surface, TEXT_CONTRAST);
        // A divider is meant to be subtle; a contrast floor here only makes it look hard.
        int outline = offset(surfaceLightness, direction, DIVIDER_DISTANCE, ladderHue, DIVIDER_CHROMA * scale);

        int primary = MuiColorMath.enforceContrast(
                new MuiOklch(MuiColorMath.clamp(brandColor.lightness(), 0.18D, 1.0D),
                        Math.min(brandChroma, 0.20D), brandHue).toArgb(),
                surface, PRIMARY_CONTRAST);
        int onPrimary = contentOn(primary, brandChroma, brandHue);
        int error = status(ERROR_LIGHTNESS, ERROR_CHROMA, ERROR_HUE, surface);

        return new MuiThemePalette(primary, onPrimary, surface, surfaceLow, surfaceHigh,
                onSurface, onSurfaceVariant, outline, error);
    }

    /**
     * Builds the overlay tokens of a scheme.
     *
     * @param theme  the scheme's UI tokens
     * @param preset how the overlay tokens are produced
     * @return the nine overlay tokens
     */
    public static @NotNull MuiHudPalette hud(@NotNull MuiThemePalette theme, @NotNull MuiHudPreset preset) {
        return switch (preset) {
            case CHAKRA -> CHAKRA;
            case NEUTRAL -> NEUTRAL_HUD;
            case DERIVED -> new MuiHudPalette(
                    theme.onSurfaceVariant(),
                    theme.onSurfaceVariant(),
                    theme.outline(),
                    theme.onSurface(),
                    status(WARN_LIGHTNESS, WARN_CHROMA, WARN_HUE, theme.surface()),
                    status(CRITICAL_LIGHTNESS, CRITICAL_CHROMA, ERROR_HUE, theme.surface()),
                    MuiColorMath.withAlpha(theme.surface(), BACKPLANE_ALPHA),
                    MuiColorMath.withAlpha(theme.primary(), BORDER_ALPHA),
                    theme.primary());
        };
    }

    /**
     * Builds the nine-stop shimmer ramp of a scheme.
     *
     * @param theme the scheme's UI tokens
     * @param hud   the scheme's overlay tokens
     * @param style how the ramp is produced
     * @return nine ARGB stops
     */
    public static int @NotNull [] ramp(@NotNull MuiThemePalette theme, @NotNull MuiHudPalette hud,
                                       @NotNull MuiRampStyle style) {
        return ramp(theme, hud, style, MuiRampAnchor.nearest(MuiOklch.fromArgb(theme.primary()).hue()));
    }

    /**
     * Builds the nine-stop shimmer ramp of a scheme.
     *
     * @param theme  the scheme's UI tokens
     * @param hud    the scheme's overlay tokens
     * @param style  how the ramp is produced
     * @param anchor the hue a {@link MuiRampStyle#HUE_SWEEP} ramp travels toward; ignored otherwise
     * @return nine ARGB stops
     */
    public static int @NotNull [] ramp(@NotNull MuiThemePalette theme, @NotNull MuiHudPalette hud,
                                       @NotNull MuiRampStyle style, @NotNull MuiRampAnchor deep) {
        return ramp(theme, hud, style, null, deep);
    }

    /**
     * Builds the nine-stop shimmer ramp of a scheme that spans two hue anchors.
     *
     * @param theme     the scheme's UI tokens
     * @param hud       the scheme's overlay tokens
     * @param style     how the ramp is produced
     * @param highlight the light end of a {@link MuiRampStyle#HUE_SWEEP} span, or {@code null} to
     *                  start at the accent hue
     * @param deep      the deep end of a {@link MuiRampStyle#HUE_SWEEP} span; ignored otherwise
     * @return nine ARGB stops
     */
    public static int @NotNull [] ramp(@NotNull MuiThemePalette theme, @NotNull MuiHudPalette hud,
                                       @NotNull MuiRampStyle style, @Nullable MuiRampAnchor highlight,
                                       @NotNull MuiRampAnchor deep) {
        int accent = hud.hasAccent() ? hud.accent() : theme.primary();
        return switch (style) {
            case NEUTRAL -> NEUTRAL_RAMP.clone();
            case SEMANTIC_HEAT -> semanticHeat(hud);
            case VALUE_GRADIENT -> valueGradient(accent, theme.outline());
            case HUE_SWEEP -> hueSweep(accent, highlight, deep);
        };
    }

    /**
     * White, then four stops on the hue segment between the highlight and the deep anchor, with
     * lightness falling and chroma growing to the sRGB edge, then mirrored.
     * <p>
     * The highlight is deliberately a pale tint rather than a saturated color: a fully chromatic
     * cyan or lime at high lightness reads as neon, while a pale one reads as light on the surface.
     * The deep end keeps full chroma so the trough of the wave stays colored instead of turning gray.
     * <p>
     * When a scheme names only its deep end, the span starts at the accent hue, which keeps the ramp
     * in the scheme's own family instead of travelling into a neighbour.
     */
    private static int @NotNull [] hueSweep(int accent, @Nullable MuiRampAnchor highlight,
                                            @NotNull MuiRampAnchor deep) {
        MuiOklch core = MuiOklch.fromArgb(MuiColorMath.opaque(accent));
        double from = highlight == null ? core.hue() : highlight.hue();
        double span = MuiColorMath.shortestHueDelta(from, deep.hue());

        int[] stops = new int[5];
        stops[0] = 0xFFFFFFFF;
        for (int i = 1; i < 5; i++) {
            double t = (i - 1) / 3.0D;
            double lightness = SWEEP_TOP_LIGHTNESS + (SWEEP_END_LIGHTNESS - SWEEP_TOP_LIGHTNESS) * t;
            double hue = MuiColorMath.normalizeHue(from + span * t);
            double fraction = SWEEP_HIGHLIGHT_CHROMA
                    + (SWEEP_CEILING - SWEEP_HIGHLIGHT_CHROMA) * Math.pow(t, SWEEP_CHROMA_GAIN);
            double chroma = new MuiOklch(lightness, 0.0D, hue).maxChroma() * fraction;
            stops[i] = new MuiOklch(lightness, chroma, hue).toArgb();
        }
        return new int[]{
                stops[0], stops[1], stops[2], stops[3], stops[4],
                stops[3], stops[2], stops[1], stops[0]};
    }

    /**
     * Walks the overlay status colors from bright to deep and mirrors the result.
     * <p>
     * This reproduces the hand-tuned chakra ramp exactly: the order is the color ladder the overlay
     * already uses for warnings and critical values, not a gradient.
     */
    private static int @NotNull [] semanticHeat(@NotNull MuiHudPalette hud) {
        Integer[] heat = {hud.warn(), hud.accent(), hud.label(), hud.critical()};
        Arrays.sort(heat, Comparator.comparingDouble((Integer color) -> -MuiOklch.fromArgb(color).lightness()));
        return new int[]{
                0xFFFFFFFF, heat[0], heat[1], heat[2], heat[3],
                heat[2], heat[1], heat[0], 0xFFFFFFFF};
    }

    /**
     * Builds a mirrored light-to-deep band between an accent and a deeper color.
     */
    private static int @NotNull [] valueGradient(int accent, int deep) {
        MuiOklch core = MuiOklch.fromArgb(MuiColorMath.opaque(accent));
        MuiOklch shadow = MuiOklch.fromArgb(MuiColorMath.opaque(deep));
        MuiOklch white = new MuiOklch(1.0D, 0.0D, 0.0D);

        int light2 = MuiOklch.mix(core, white, 0.75D).toArgb();
        int light1 = MuiOklch.mix(core, white, 0.45D).toArgb();
        int accentLight = MuiOklch.mix(core, white, 0.20D).toArgb();
        int center = core.toArgb();
        int bottom = shadow.toArgb();
        return new int[]{
                light2, light1, accentLight, center, bottom,
                center, accentLight, light1, light2};
    }

    private static int offset(double surfaceLightness, double direction, double distance,
                              double hue, double chroma) {
        return new MuiOklch(MuiColorMath.clamp(surfaceLightness + direction * distance, 0.0D, 1.0D), chroma, hue).toArgb();
    }

    /**
     * Picks dark or light content for a filled control, keeping some of the brand's chroma.
     */
    private static int contentOn(int background, double brandChroma, double brandHue) {
        int ink = new MuiOklch(INK_LIGHTNESS, Math.min(brandChroma * 0.35D, 0.060D), brandHue).toArgb();
        int white = new MuiOklch(WHITE_LIGHTNESS, Math.min(brandChroma * 0.05D, 0.006D), brandHue).toArgb();
        return MuiColorMath.contrast(ink, background) >= MuiColorMath.contrast(white, background) ? ink : white;
    }

    private static int status(double lightness, double chroma, double hue, int surface) {
        return MuiColorMath.enforceContrast(new MuiOklch(lightness, chroma, hue).toArgb(), surface, STATUS_CONTRAST);
    }
}
