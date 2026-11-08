package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiFormatting;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A title with an optional description line. Useful for usage hints, explanatory rows and
 * read-only settings entries that are not a control.
 */
public final class MuiDescriptionRow extends LinearLayout implements MuiShimmerContainer {
    /** Title line. */
    private final TextView titleView;
    /** Description line. */
    private final TextView descriptionView;
    /** Default title color. */
    private final int titleColor;
    /** Default description color. */
    private final int descriptionColor;

    /**
     * Creates a description row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       title or translation key
     * @param description optional description or translation key
     */
    public MuiDescriptionRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description) {
        super(context);
        this.setOrientation(VERTICAL);
        this.setPadding(MuiControls.dp(context, 2), MuiControls.dp(context, 8), MuiControls.dp(context, 2), MuiControls.dp(context, 8));

        this.titleColor = theme.onSurface();
        this.descriptionColor = theme.onSurfaceVariant();
        this.titleView = MuiControls.text(context, theme, title, 13, this.titleColor);
        this.addView(this.titleView, MuiControls.fill());

        this.descriptionView = MuiControls.text(context, theme, description == null ? "" : description, 11, this.descriptionColor);
        LinearLayout.LayoutParams params = MuiControls.fill();
        params.topMargin = MuiControls.dp(context, 3);
        this.descriptionView.setVisibility(description == null || description.isEmpty() ? GONE : VISIBLE);
        this.addView(this.descriptionView, params);
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        /** Marker applied to both text lines. */
        MuiShimmerScope.apply(this.titleView, marker);
        MuiShimmerScope.apply(this.descriptionView, marker);
        MuiShimmer.refresh();
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
    public static @NotNull MuiDescriptionRow create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description) {
        return new MuiDescriptionRow(context, theme, title, description);
    }

    /**
     * Replaces the title.
     *
     * @param title title or translation key
     * @return this row, for chaining
     */
    public @NotNull MuiDescriptionRow title(@NotNull CharSequence title) {
        CharSequence resolved = MuiLocalization.resolve(title);
        this.titleView.setText(MuiFormatting.parse(resolved));
        int solid = MuiFormatting.solidColor(resolved);
        this.titleView.setTextColor(solid != 0 ? solid : this.titleColor);
        return this;
    }

    /**
     * Returns the current title.
     *
     * @return the title
     */
    public @NotNull CharSequence title() {
        return this.titleView.getText();
    }

    /**
     * Replaces the description. A {@code null} or empty description hides the line.
     *
     * @param description description or translation key, or {@code null}
     * @return this row, for chaining
     */
    public @NotNull MuiDescriptionRow description(@Nullable CharSequence description) {
        if (description == null) {
            this.descriptionView.setText("");
            this.descriptionView.setVisibility(GONE);
            return this;
        }

        String resolved = MuiLocalization.resolve(description);
        this.descriptionView.setText(MuiFormatting.parse(resolved));
        int solid = MuiFormatting.solidColor(resolved);
        this.descriptionView.setTextColor(solid != 0 ? solid : this.descriptionColor);
        this.descriptionView.setVisibility(resolved.isEmpty() ? GONE : VISIBLE);
        return this;
    }

    /**
     * Returns the current description.
     *
     * @return the description, or an empty string
     */
    public @NotNull CharSequence description() {
        return this.descriptionView.getText();
    }
}