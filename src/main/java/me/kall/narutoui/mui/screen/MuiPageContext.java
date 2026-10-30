package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Per-page builder context handed to {@link MuiConfigScreen} page bodies.
 * <p>
 * The context exposes the page's content column and a few common composition helpers. Anything
 * else can be assembled with {@link MuiControls} and added to {@link #content()}, or built
 * fluently on {@link #contentLayout()}.
 */
public final class MuiPageContext {
    /** Screen context used for density conversion and widget construction. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Vertical content column that receives page children. */
    private final MuiLayout content;

    /**
     * Creates a page context. Called by {@link MuiPage}.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param content the page content column
     */
    MuiPageContext(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiLayout content) {
        this.context = context;
        this.theme = theme;
        this.content = content;
    }

    /**
     * Returns the screen context.
     *
     * @return the context
     */
    public @NotNull Context context() {
        return this.context;
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
     * Returns the vertical content column. Callers may add arbitrary views to it.
     *
     * @return the content column
     */
    public @NotNull LinearLayout content() {
        return this.content;
    }

    /**
     * Returns the vertical content column as a fluent layout wrapper.
     *
     * @return the content column
     */
    public @NotNull MuiLayout contentLayout() {
        return this.content;
    }

    /**
     * Adds a page header.
     *
     * @param title    page title
     * @param subtitle optional subtitle; {@code null} or empty omits it
     */
    public void header(@NotNull CharSequence title, @Nullable CharSequence subtitle) {
        TextView titleView = MuiControls.heading(this.context, this.theme, title, 30, this.theme.onSurface());
        this.content.addView(titleView, MuiControls.wrap());

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = MuiControls.text(this.context, this.theme, subtitle, 12, this.theme.onSurfaceVariant());
            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(this.context, 6);
            this.content.addView(subtitleView, params);
        }
    }

    /**
     * Adds a section heading followed by a card.
     *
     * @param title section title
     * @return the card's vertical body
     */
    public @NotNull LinearLayout section(@NotNull CharSequence title) {
        return sectionLayout(title);
    }

    /**
     * Adds a section heading followed by a card and returns the fluent card body.
     *
     * @param title section title
     * @return the card's vertical body
     */
    public @NotNull MuiLayout sectionLayout(@NotNull CharSequence title) {
        TextView titleView = MuiControls.heading(this.context, this.theme, title, 16, this.theme.primary());
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(0, MuiControls.dp(this.context, 28), 0, MuiControls.dp(this.context, 8));
        this.content.addView(titleView, params);

        MuiLayout card = MuiLayout.card(this.context, this.theme);
        this.content.addView(card, MuiControls.fill());
        return card;
    }

    /**
     * Adds a full-width view to the page column.
     *
     * @param view the view to add
     */
    public void add(@NotNull View view) {
        this.content.addView(view, MuiControls.fill());
    }

    /**
     * Creates a switch row using a static initial value.
     *
     * @param title       row title
     * @param description optional description
     * @param value       initial checked state
     * @param onChange    state callback
     * @return the composed row
     */
    public @NotNull LinearLayout switchRow(@NotNull CharSequence title, @Nullable CharSequence description, boolean value, @NotNull BooleanConsumer onChange) {
        return MuiControls.switchRow(this.context, this.theme, title, description, value, onChange);
    }

    /**
     * Creates a supplier-backed switch row.
     *
     * @param title       row title
     * @param description optional description
     * @param value       live state supplier
     * @param onChange    state callback
     * @return the composed row
     */
    public @NotNull LinearLayout switchRow(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        return MuiControls.switchRow(this.context, this.theme, title, description, value, onChange);
    }

    /**
     * Creates a static value row.
     *
     * @param title row title
     * @param value value text
     * @return the composed row
     */
    public @NotNull LinearLayout valueRow(@NotNull CharSequence title, @NotNull CharSequence value) {
        return MuiControls.valueRow(this.context, this.theme, title, value);
    }

    /**
     * Creates a supplier-backed integer value row.
     *
     * @param title row title
     * @param value live integer supplier
     * @return the composed row
     */
    public @NotNull LinearLayout valueRow(@NotNull CharSequence title, @NotNull IntSupplier value) {
        return MuiControls.valueRow(this.context, this.theme, title, value);
    }

    /**
     * Creates a supplier-backed integer value row with custom formatting.
     *
     * @param title  row title
     * @param format value formatter
     * @param value  live integer supplier
     * @return the composed row
     */
    public @NotNull LinearLayout valueRow(@NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, @NotNull IntSupplier value) {
        return MuiControls.valueRow(this.context, this.theme, title, format, value);
    }

    /**
     * Creates a slider row with a static value label.
     *
     * @param title    row title
     * @param value    value label
     * @param max      maximum progress
     * @param progress initial progress (0 to {@code max})
     * @param onChange progress callback
     * @return the composed row
     */
    public @NotNull LinearLayout sliderRow(@NotNull CharSequence title, @NotNull CharSequence value, int max, int progress, @NotNull IntConsumer onChange) {
        return MuiControls.sliderRow(this.context, this.theme, title, value, max, progress, onChange);
    }

    /**
     * Creates a supplier-backed slider row.
     *
     * @param title    row title
     * @param format   value formatter
     * @param max      maximum progress
     * @param value    live value supplier
     * @param onChange progress callback
     * @return the composed row
     */
    public @NotNull LinearLayout sliderRow(@NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return MuiControls.sliderRow(this.context, this.theme, title, format, max, value, onChange);
    }
}