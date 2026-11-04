package me.kall.narutoui.mui.util;

import icyllis.modernui.text.SpannableString;
import icyllis.modernui.text.Spanned;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.text.style.ForegroundColorSpan;
import icyllis.modernui.text.style.StrikethroughSpan;
import icyllis.modernui.text.style.StyleSpan;
import icyllis.modernui.text.style.UnderlineSpan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts Minecraft legacy formatting codes ({@code §e}, {@code §l}, ...) into Modern UI spans.
 * <p>
 * Shader packs use color codes in option labels, for example {@code §eBloom§r}. Modern UI's
 * {@code TextView} does not understand those codes, so they would otherwise be rendered as raw
 * {@code §} characters. Formatting codes are removed from the visible text and translated into
 * foreground colors and text styles.
 */
public final class MuiFormatting {
    private static final char SECTION = '\u00a7';

    private MuiFormatting() {}

    /**
     * Parses legacy formatting codes into a styled sequence.
     *
     * @param text source text
     * @return styled text, or the original text when it contains no formatting codes
     */
    public static @NotNull CharSequence parse(@NotNull CharSequence text) {
        String source = text.toString();
        if (source.indexOf(SECTION) < 0) {
            return text;
        }

        StringBuilder visible = new StringBuilder(source.length());
        List<StyleRange> ranges = new ArrayList<>();
        // 0 is the "no explicit legacy color" sentinel. ARGB legacy colors have alpha 0xFF and are
        // negative ints, so they must not be compared with < 0.
        int color = 0;
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        int start = 0;

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch != SECTION || i + 1 >= source.length()) {
                visible.append(ch);
                continue;
            }

            addRange(ranges, start, visible.length(), color, bold, italic, underlined, strikethrough);
            start = visible.length();

            char code = Character.toLowerCase(source.charAt(i + 1));
            i++;
            switch (code) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' -> {
                    color = legacyColor(code);
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                }
                case 'l' -> bold = true;
                case 'o' -> italic = true;
                case 'n' -> underlined = true;
                case 'm' -> strikethrough = true;
                case 'r' -> {
                    color = 0;
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                }
                default -> {
                    // Unknown/obfuscated code: drop the code and keep the visible text.
                }
            }
        }

        addRange(ranges, start, visible.length(), color, bold, italic, underlined, strikethrough);
        if (ranges.isEmpty()) {
            return visible.toString();
        }

        // A single solid color covering the whole text is handled through TextView#setTextColor.
        // Returning plain text here also avoids Modern UI's setSingleLine + SpannableString crash
        // for one-character/very short labels.
        if (ranges.size() == 1) {
            StyleRange only = ranges.get(0);
            if (only.start == 0 && only.end == visible.length() && only.color != 0
                    && !only.bold && !only.italic && !only.underlined && !only.strikethrough) {
                return visible.toString();
            }
        }

        SpannableString styled = new SpannableString(visible.toString());
        for (StyleRange range : ranges) {
            if (range.color != 0) {
                styled.setSpan(new ForegroundColorSpan(range.color), range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int style = Typeface.NORMAL;
            if (range.bold) style |= Typeface.BOLD;
            if (range.italic) style |= Typeface.ITALIC;
            if (style != Typeface.NORMAL) {
                styled.setSpan(new StyleSpan(style), range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (range.underlined) {
                styled.setSpan(new UnderlineSpan(), range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (range.strikethrough) {
                styled.setSpan(new StrikethroughSpan(), range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return styled;
    }

    /**
     * Removes legacy formatting codes without applying styles. Useful for tooltips and other
     * single-color surfaces.
     *
     * @param text source text
     * @return text without formatting codes
     */
    /**
     * Returns the single legacy color used by the whole visible text, or {@code -1} when the text
     * is uncolored or uses more than one color. Callers can use this to set a plain text color for
     * simple labels such as {@code §eBloom§r}, which is more robust than relying only on spans.
     *
     * @param text source text
     * @return a solid ARGB color, or {@code 0} when the text has no single solid color
     */
    public static int solidColor(@NotNull CharSequence text) {
        String source = text.toString();
        if (source.indexOf(SECTION) < 0) {
            return 0;
        }

        int color = 0;
        int current = 0;
        boolean visible = false;
        boolean uncolored = false;

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == SECTION && i + 1 < source.length()) {
                char code = Character.toLowerCase(source.charAt(i + 1));
                i++;
                if (code == 'r') {
                    current = 0;
                } else if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
                    current = legacyColor(code);
                }
                continue;
            }

            visible = true;
            if (current == 0) {
                uncolored = true;
            } else if (color == 0) {
                color = current;
            } else if (color != current) {
                return 0;
            }
        }

        return visible && !uncolored ? color : 0;
    }

    /**
     * Removes legacy formatting codes without applying styles. Useful for tooltips and other
     * single-color surfaces.
     *
     * @param text source text
     * @return text without formatting codes
     */
    public static @NotNull String strip(@NotNull CharSequence text) {
        String source = text.toString();
        if (source.indexOf(SECTION) < 0) {
            return source;
        }

        StringBuilder result = new StringBuilder(source.length());
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == SECTION && i + 1 < source.length()) {
                i++;
                continue;
            }
            result.append(ch);
        }
        return result.toString();
    }

    private static void addRange(@NotNull List<StyleRange> ranges, int start, int end, int color,
                                 boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        if (end <= start) {
            return;
        }
        if (color == 0 && !bold && !italic && !underlined && !strikethrough) {
            return;
        }
        ranges.add(new StyleRange(start, end, color, bold, italic, underlined, strikethrough));
    }

    private static int legacyColor(char code) {
        return switch (code) {
            case '0' -> 0xFF000000;
            case '1' -> 0xFF0000AA;
            case '2' -> 0xFF00AA00;
            case '3' -> 0xFF00AAAA;
            case '4' -> 0xFFAA0000;
            case '5' -> 0xFFAA00AA;
            case '6' -> 0xFFFFAA00;
            case '7' -> 0xFFAAAAAA;
            case '8' -> 0xFF555555;
            case '9' -> 0xFF5555FF;
            case 'a' -> 0xFF55FF55;
            case 'b' -> 0xFF55FFFF;
            case 'c' -> 0xFFFF5555;
            case 'd' -> 0xFFFF55FF;
            case 'e' -> 0xFFFFFF55;
            case 'f' -> 0xFFFFFFFF;
            default -> 0;
        };
    }

    private record StyleRange(int start, int end, int color, boolean bold, boolean italic,
                              boolean underlined, boolean strikethrough) {}
}