package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A title with an optional description line. Useful for usage hints, explanatory rows and
 * read-only settings entries that are not a control.
 */
public final class MuiDescriptionRow extends LinearLayout {
    /**
     * Creates a description row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       title or translation key
     * @param description optional description or translation key
     */
    public MuiDescriptionRow(@NotNull Context context, @NotNull MuiTheme theme,
                             @NotNull CharSequence title, @Nullable CharSequence description) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(MuiControls.dp(context, 2), MuiControls.dp(context, 8),
                MuiControls.dp(context, 2), MuiControls.dp(context, 8));
        addView(MuiControls.text(context, theme, title, 13, theme.onSurface()), MuiControls.fill());

        if (description != null && !description.isEmpty()) {
            TextView hint = MuiControls.text(context, theme, description, 11, theme.onSurfaceVariant());
            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(context, 3);
            addView(hint, params);
        }
    }

    /**
     * Creates a description row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       title or translation key
     * @param description optional description or translation key
     * @return the row
     */
    public static @NotNull MuiDescriptionRow create(@NotNull Context context, @NotNull MuiTheme theme,
                                                    @NotNull CharSequence title, @Nullable CharSequence description) {
        return new MuiDescriptionRow(context, theme, title, description);
    }
}