package me.kall.narutoui.mui.color;

/**
 * Overlay-facing color tokens of a named color scheme.
 * <p>
 * These are the tokens used by HUD-style consumers: text levels, dividers, backplane, border and
 * accent. Keeping them on the same scheme as the UI tokens lets one catalog serve both settings
 * screens and overlays.
 *
 * @param label     field-label color
 * @param unit      unit-suffix color
 * @param separator divider color
 * @param normal    normal value color
 * @param warn      warning value color
 * @param critical  critical value color
 * @param backplane panel background color, including alpha
 * @param border    panel border color
 * @param accent    accent color, or a fully transparent color when the scheme has no accent
 */
public record MuiHudPalette(int label, int unit, int separator, int normal, int warn, int critical,
                            int backplane, int border, int accent) {
    /**
     * @return whether this scheme provides an accent color
     */
    public boolean hasAccent() {
        return (this.accent >>> 24) != 0;
    }
}