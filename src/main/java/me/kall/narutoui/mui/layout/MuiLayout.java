package me.kall.narutoui.mui.layout;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A fluent {@link LinearLayout} used by the configuration-screen API.
 * <p>
 * The wrapper is a real {@code LinearLayout}, so it can still be passed to any API that accepts a
 * {@link View} or a {@code LinearLayout}. Every configuration method returns this instance, which
 * removes the local variables, layout params and repeated {@code addView} calls that low-level page
 * code otherwise needs:
 * <pre>{@code
 * MuiLayout.column(context, theme)
 *         .spacing(8)
 *         .padding(12)
 *         .heading("Appearance")
 *         .text("Changes apply immediately.")
 *         .button("Reset", MyConfig::reset);
 * }</pre>
 * Children added without explicit layout params follow the normal {@code LinearLayout} defaults:
 * a vertical layout stretches them to the full width, while a horizontal layout wraps them.
 */
public final class MuiLayout extends LinearLayout {
    /** Active visual tokens used by the text, divider and button helpers. */
    private final MuiTheme theme;
    /** Gap between children in pixels; {@code 0} means no gap. */
    private int spacing;
    /** Requested left margin in pixels. */
    private int marginLeft;
    /** Requested top margin in pixels. */
    private int marginTop;
    /** Requested right margin in pixels. */
    private int marginRight;
    /** Requested bottom margin in pixels. */
    private int marginBottom;
    /** Whether a margin has been requested. */
    private boolean hasMargins;

    /**
     * Creates a vertical layout.
     *
     * @param context the screen context
     * @param theme   active theme
     */
    public MuiLayout(@NotNull Context context, @NotNull MuiTheme theme) {
        super(context);
        this.theme = theme;
        super.setOrientation(VERTICAL);
    }

    /**
     * Creates a vertical layout.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return a new column
     */
    public static @NotNull MuiLayout column(@NotNull Context context, @NotNull MuiTheme theme) {
        return new MuiLayout(context, theme);
    }

    /**
     * Creates a horizontal, vertically centered row with standard height and padding.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return a new row
     */
    public static @NotNull MuiLayout row(@NotNull Context context, @NotNull MuiTheme theme) {
        MuiLayout layout = new MuiLayout(context, theme);
        MuiControls.applyRowStyle(layout, context);
        return layout;
    }

    /**
     * Creates a rounded, outlined card body.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return a new card layout
     */
    public static @NotNull MuiLayout card(@NotNull Context context, @NotNull MuiTheme theme) {
        MuiLayout layout = new MuiLayout(context, theme);
        applyCardStyle(layout, context, theme);
        return layout;
    }

    /**
     * Applies the shared rounded, outlined card style. Used by {@link #card(Context, MuiTheme)} and
     * {@code MuiCollapsibleCard} so all card-like containers stay visually identical.
     *
     * @param layout  the layout to style
     * @param context the screen context
     * @param theme   active theme
     */
    public static void applyCardStyle(@NotNull LinearLayout layout, @NotNull Context context, @NotNull MuiTheme theme) {
        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, theme.cornerRadius()));
        background.setColor(MuiControls.alpha(theme.surfaceLow(), 0.78F));
        background.setStroke(Math.max(1, MuiControls.dp(context, 1)), MuiControls.alpha(theme.outline(), 0.75F));
        layout.setBackground(background);

        int padding = MuiControls.dp(context, 12);
        layout.setPadding(padding, padding, padding, padding);
    }

    /**
     * Returns the active theme.
     *
     * @return the theme
     */
    public @NotNull MuiTheme theme() {
        return this.theme;
    }

    /**
     * Switches this layout to a horizontal row.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout horizontal() {
        this.setOrientation(HORIZONTAL);
        return this;
    }

    /**
     * Switches this layout to a vertical column.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout vertical() {
        this.setOrientation(VERTICAL);
        return this;
    }

    /**
     * Sets the orientation and keeps the current {@link #spacing(int)} gap in sync.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void setOrientation(@OrientationMode int orientation) {
        super.setOrientation(orientation);
        if (this.spacing > 0) this.applySpacing();
    }

    /**
     * Sets the layout gravity.
     *
     * @param gravity a {@link Gravity} value
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout gravity(int gravity) {
        this.setGravity(gravity);
        return this;
    }

    /**
     * Centers children on both axes.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout center() {
        return this.gravity(Gravity.CENTER);
    }

    /**
     * Centers children vertically.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout centerVertical() {
        return this.gravity(Gravity.CENTER_VERTICAL);
    }

    /**
     * Sets uniform padding.
     *
     * @param dp padding in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout padding(int dp) {
        return this.padding(dp, dp, dp, dp);
    }

    /**
     * Sets padding for each edge.
     *
     * @param left   left padding in density-independent pixels
     * @param top    top padding in density-independent pixels
     * @param right  right padding in density-independent pixels
     * @param bottom bottom padding in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout padding(int left, int top, int right, int bottom) {
        this.setPadding(MuiControls.dp(this.getContext(), left), MuiControls.dp(this.getContext(), top), MuiControls.dp(this.getContext(), right), MuiControls.dp(this.getContext(), bottom));
        return this;
    }

    /**
     * Sets uniform margins on this layout's layout params.
     *
     * @param dp margin in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout margin(int dp) {
        return this.margin(dp, dp, dp, dp);
    }

    /**
     * Sets margins for each edge. The margins are remembered and applied when a parent assigns
     * layout params, so this method can be called before the layout is added to a container.
     *
     * @param left   left margin in density-independent pixels
     * @param top    top margin in density-independent pixels
     * @param right  right margin in density-independent pixels
     * @param bottom bottom margin in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout margin(int left, int top, int right, int bottom) {
        this.marginLeft = MuiControls.dp(this.getContext(), left);
        this.marginTop = MuiControls.dp(this.getContext(), top);
        this.marginRight = MuiControls.dp(this.getContext(), right);
        this.marginBottom = MuiControls.dp(this.getContext(), bottom);
        this.hasMargins = true;

        ViewGroup.LayoutParams current = this.getLayoutParams();
        if (current instanceof ViewGroup.MarginLayoutParams margins) {
            this.applyMargins(margins);
            this.setLayoutParams(margins);
        }
        return this;
    }

    /**
     * Applies remembered margins whenever a parent or caller assigns layout params.
     *
     * @param params the new layout params
     */
    @Override
    public void setLayoutParams(ViewGroup.@NotNull LayoutParams params) {
        if (this.hasMargins && params instanceof ViewGroup.MarginLayoutParams margins) this.applyMargins(margins);
        super.setLayoutParams(params);
    }

    /**
     * Sets the minimum height.
     *
     * @param dp height in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout minHeight(int dp) {
        this.setMinimumHeight(MuiControls.dp(this.getContext(), dp));
        return this;
    }

    /**
     * Sets a solid background color.
     *
     * @param color the ARGB color
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout background(int color) {
        this.setBackground(new ColorDrawable(color));
        return this;
    }

    /**
     * Sets a custom background drawable.
     *
     * @param drawable the background drawable
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout background(@NotNull Drawable drawable) {
        this.setBackground(drawable);
        return this;
    }

    /**
     * Sets the gap between children. The gap is implemented with the layout's divider slot and is
     * therefore also removed by a later {@link #setDividerDrawable(Drawable)} call.
     *
     * @param dp gap in density-independent pixels; {@code 0} removes the gap
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout spacing(int dp) {
        this.spacing = Math.max(0, MuiControls.dp(this.getContext(), dp));
        this.applySpacing();
        return this;
    }

    /**
     * Appends a child with the default layout params for the current orientation.
     *
     * @param child the child view
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout add(@NotNull View child) {
        this.addView(child);
        return this;
    }

    /**
     * Appends a child with explicit layout params.
     *
     * @param child  the child view
     * @param params the child's layout params
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout add(@NotNull View child, @NotNull ViewGroup.LayoutParams params) {
        this.addView(child, params);
        return this;
    }

    /**
     * Appends a child that consumes the remaining space along the layout axis.
     *
     * @param child  the child view
     * @param weight the weight to distribute to this child
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout addWeighted(@NotNull View child, float weight) {
        LinearLayout.LayoutParams params = this.getOrientation() == HORIZONTAL ? new LinearLayout.LayoutParams(0, MuiControls.WRAP, weight) : new LinearLayout.LayoutParams(MuiControls.MATCH, 0, weight);
        this.addView(child, params);
        return this;
    }

    /**
     * Appends several children with the default layout params.
     *
     * @param children the child views
     * @return this layout, for chaining
     */
    @Contract("_ -> this")
    public @NotNull MuiLayout addAll(@NotNull View @NotNull ... children) {
        for (View child : children) this.addView(child);
        return this;
    }

    /**
     * Appends a fixed-size empty space along the layout axis.
     *
     * @param dp size in density-independent pixels
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout space(int dp) {
        int size = Math.max(0, MuiControls.dp(this.getContext(), dp));
        LinearLayout.LayoutParams params = this.getOrientation() == HORIZONTAL ? new LinearLayout.LayoutParams(size, MuiControls.WRAP) : new LinearLayout.LayoutParams(MuiControls.MATCH, size);
        this.addView(new View(this.getContext()), params);
        return this;
    }

    /**
     * Appends an empty view that consumes the remaining space along the layout axis.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout flexibleSpace() {
        return this.addWeighted(new View(this.getContext()), 1.0F);
    }

    /**
     * Appends a secondary text view.
     *
     * @param text text content
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout text(@NotNull CharSequence text) {
        return this.add(MuiControls.text(this.getContext(), this.theme, text, 12, this.theme.onSurfaceVariant()));
    }

    /**
     * Appends a text view with an explicit size and color.
     *
     * @param text  text content
     * @param size  text size in SP
     * @param color text color
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout text(@NotNull CharSequence text, float size, int color) {
        return this.add(MuiControls.text(this.getContext(), this.theme, text, size, color));
    }

    /**
     * Appends a bold heading.
     *
     * @param text heading text
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout heading(@NotNull CharSequence text) {
        return this.add(MuiControls.heading(this.getContext(), this.theme, text, 16, this.theme.onSurface()));
    }

    /**
     * Appends a bold heading with an explicit size and color.
     *
     * @param text  heading text
     * @param size  text size in SP
     * @param color text color
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout heading(@NotNull CharSequence text, float size, int color) {
        return this.add(MuiControls.heading(this.getContext(), this.theme, text, size, color));
    }

    /**
     * Appends a one-pixel divider sized for the current orientation.
     *
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout divider() {
        View divider = MuiControls.divider(this.getContext(), this.theme);
        if (this.getOrientation() == HORIZONTAL) {
            this.addView(divider, new LinearLayout.LayoutParams(Math.max(1, MuiControls.dp(this.getContext(), 1)), MuiControls.MATCH));
        } else {
            this.addView(divider, new LinearLayout.LayoutParams(MuiControls.MATCH, Math.max(1, MuiControls.dp(this.getContext(), 1))));
        }
        return this;
    }

    /**
     * Appends a themed button.
     *
     * @param label  button label
     * @param action click callback
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout button(@NotNull CharSequence label, @NotNull Runnable action) {
        return this.button(label, false, action);
    }

    /**
     * Appends a themed button.
     *
     * @param label    button label
     * @param outlined {@code true} for an outlined button, {@code false} for a filled button
     * @param action   click callback
     * @return this layout, for chaining
     */
    public @NotNull MuiLayout button(@NotNull CharSequence label, boolean outlined, @NotNull Runnable action) {
        return this.add(MuiControls.button(this.getContext(), this.theme, label, outlined, view -> action.run()));
    }

    /**
     * Copies the requested margins onto the given layout params.
     *
     * @param params the target layout params
     */
    private void applyMargins(@NotNull ViewGroup.MarginLayoutParams params) {
        params.setMargins(this.marginLeft, this.marginTop, this.marginRight, this.marginBottom);
    }

    /**
     * Applies or clears the transparent divider used as a child gap.
     */
    private void applySpacing() {
        if (this.spacing <= 0) {
            this.setShowDividers(SHOW_DIVIDER_NONE);
            this.setDividerDrawable(null);
            return;
        }

        int size = this.spacing;
        ColorDrawable gap = new ColorDrawable(0x00000000) {
            @Override
            public int getIntrinsicWidth() {
                return MuiLayout.this.getOrientation() == HORIZONTAL ? size : 0;
            }

            @Override
            public int getIntrinsicHeight() {
                return MuiLayout.this.getOrientation() == VERTICAL ? size : 0;
            }
        };
        this.setDividerDrawable(gap);
        this.setShowDividers(SHOW_DIVIDER_MIDDLE);
    }
}