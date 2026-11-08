package me.kall.narutoui.mui.color;

/**
 * Where a scheme's neutral ladder takes its hue from.
 * <p>
 * The ladder is the surface, card, control and text stack. It is not always a tint of the brand:
 * a warm accent on a cool dark surface separates the two planes, while a cool accent on a cool
 * surface keeps the whole scheme quiet. That choice cannot be derived from the brand color alone,
 * so it is an explicit input.
 */
public enum MuiSurfaceTint {
    /** Near-neutral ladder with a slight cool cast; the default. */
    NEUTRAL_COOL,
    /** Ladder tinted with the brand hue, for schemes whose identity is a colored surface. */
    BRAND
}
