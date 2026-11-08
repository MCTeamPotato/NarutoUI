package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.AttributeSet;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.util.MuiFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A text view that understands Minecraft legacy formatting codes ({@code §e}, {@code §l}, ...).
 * <p>
 * Plain Modern UI {@code TextView}s render the codes literally, so every label that comes from a
 * shader pack, a server or any other legacy source would show stray {@code §} characters. More
 * importantly, the color is lost again as soon as the label is refreshed: a slider value that is
 * re-read from its source every tick would replace the styled text with the raw code.
 * <p>
 * This view therefore translates the codes on <em>every</em> {@link #setText(CharSequence,
 * BufferType)} call, not just once at construction. Silent codes are removed from the visible text
 * and the formatting is carried by spans; a single color covering the whole label is applied
 * through {@link #setTextColor(int)} so it also works for the one-line labels where Modern UI does
 * not keep spans.
 * <p>
 * The color passed to {@code setTextColor} is remembered as the caller's base color. Legacy codes
 * take precedence while they are present (matching the game's own font renderer), and the base
 * color is restored when the next update has no codes.
 */
public class MuiTextView extends TextView {
    private int fallbackColor;
    private boolean hasFallbackColor;
    private @Nullable ColorStateList fallbackColors;
    private int legacyColor;
    private boolean applyingColor;
    private @Nullable String lastSource;
    private BufferType lastType;

    /**
     * Creates a formatted text view.
     *
     * @param context the screen context
     */
    public MuiTextView(@NotNull Context context) {
        super(context);
    }

    /**
     * Creates a formatted text view that reads attributes from XML.
     *
     * @param context the screen context
     * @param attrs   the attribute set, or {@code null}
     */
    public MuiTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(@Nullable CharSequence text, @Nullable BufferType type) {
        CharSequence source = text == null ? "" : text;

        // Editable buffers keep the exact user input; formatting codes in a text field are data,
        // not styling.
        if (type == BufferType.EDITABLE) {
            this.lastSource = null;
            this.lastType = null;
            super.setText(source, type);
            return;
        }

        String raw = source.toString();
        if (type == this.lastType && raw.equals(this.lastSource)) {
            // Supplier-backed rows refresh their labels about ten times per second. The text did not
            // change, so skip the whole parse/layout path and only re-apply the visible color.
            this.applyResolvedColor();
            return;
        }

        this.legacyColor = MuiFormatting.solidColor(source);
        super.setText(MuiFormatting.parse(source), type);
        this.lastSource = raw;
        this.lastType = type;
        this.applyResolvedColor();
    }

    @Override
    public void setTextColor(int color) {
        this.fallbackColor = color;
        this.hasFallbackColor = true;
        this.fallbackColors = null;
        this.applyResolvedColor();
    }

    @Override
    public void setTextColor(@Nullable ColorStateList colors) {
        this.fallbackColors = colors;
        this.fallbackColor = colors == null ? 0 : colors.getDefaultColor();
        this.hasFallbackColor = colors != null;
        this.applyResolvedColor();
    }

    /**
     * Applies the color that should be visible for the current text. Legacy formatting wins over
     * the caller's base color while codes are present, because that is how the game renders the
     * same label.
     */
    private void applyResolvedColor() {
        if (this.applyingColor) return;

        this.applyingColor = true;
        try {
            if (this.legacyColor != 0) {
                this.applyColor(this.legacyColor);
            } else if (this.hasFallbackColor) {
                if (this.fallbackColors != null) {
                    this.applyColorState(this.fallbackColors);
                } else {
                    this.applyColor(this.fallbackColor);
                }
            }
        } finally {
            this.applyingColor = false;
        }
    }

    /**
     * Sets a solid text color. Subclasses that animate the text color can override this hook instead
     * of {@link #setTextColor(int)}.
     *
     * @param color the ARGB color
     */
    protected void applyColor(int color) {
        this.applyRawColor(color);
    }

    /**
     * Sets a text color state list. Subclasses that animate the text color can override this hook.
     *
     * @param colors the color state list
     */
    protected void applyColorState(@NotNull ColorStateList colors) {
        this.applyRawColorState(colors);
    }

    /**
     * Applies a color directly to Modern UI, bypassing the hook that subclasses may override.
     *
     * @param color the ARGB color
     */
    protected final void applyRawColor(int color) {
        super.setTextColor(color);
    }

    /**
     * Applies a color state list directly to Modern UI, bypassing the hook a subclass may override.
     *
     * @param colors the color state list
     */
    protected final void applyRawColorState(@NotNull ColorStateList colors) {
        super.setTextColor(colors);
    }
}