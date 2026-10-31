package me.kall.narutoui.mui.choice;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A stateful grid of selectable value cards.
 * <p>
 * Each card is described by a {@link MuiOption}. The grid owns the current selection, so a click
 * updates the card styling immediately and then calls {@link #onSelected(Consumer)}. Programmatic
 * {@link #selected(Object)} updates never fire that callback, which makes the control safe to bind
 * to a supplier or a caller-owned draft.
 * <p>
 * The grid is presentation-only: it does not save anything. Callers usually pass a supplier for the
 * current value and a consumer for changes, or wire both to their own draft state.
 *
 * @param <T> value type
 */
public final class MuiChoiceGrid<T> extends LinearLayout implements MuiShimmerContainer {
    /** Context used for density conversion and previews. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Available options. */
    private final List<MuiOption<T>> options = new ArrayList<>();
    /** Current selection. */
    private @Nullable T selected;
    /** Optional user-selection listener. */
    private @Nullable Consumer<T> onSelected;
    /** Number of cards per row. */
    private int columns = 2;
    /** Label shown on the selected card; empty hides it. */
    private CharSequence selectedLabel = "";
    /** Whether the whole grid accepts interaction. */
    private boolean enabled = true;
    /** Marker stored for every card, including cards rebuilt later. */
    private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

    private MuiChoiceGrid(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.setOrientation(VERTICAL);
        this.options.addAll(options);
    }

    /**
     * Creates a choice grid.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     value type
     * @return the grid
     */
    public static <T> @NotNull MuiChoiceGrid<T> create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        return new MuiChoiceGrid<>(context, theme, options);
    }

    /**
     * Replaces the options. The current selection is kept when it is still present, and becomes an
     * unknown value otherwise.
     *
     * @param options new options
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> options(@NotNull List<MuiOption<T>> options) {
        this.options.clear();
        this.options.addAll(options);
        this.rebuild();
        return this;
    }

    /**
     * Selects a value without firing the listener.
     *
     * @param value the value, or {@code null}
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> selected(@Nullable T value) {
        this.selected = value;
        this.rebuild();
        return this;
    }

    /**
     * Registers the user-selection listener. Programmatic {@link #selected(Object)} updates do not
     * fire it.
     *
     * @param listener the listener, or {@code null}
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> onSelected(@Nullable Consumer<T> listener) {
        this.onSelected = listener;
        return this;
    }

    /**
     * Sets the number of cards per row.
     *
     * @param columns columns, at least {@code 1}
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> columns(int columns) {
        this.columns = Math.max(1, columns);
        this.rebuild();
        return this;
    }

    /**
     * Sets the label shown on the selected card.
     *
     * @param label label or translation key, or an empty value to hide it
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> selectedLabel(@NotNull CharSequence label) {
        this.selectedLabel = label;
        this.rebuild();
        return this;
    }

    /**
     * Enables or disables the whole grid.
     *
     * @param enabled {@code false} to disable
     * @return this grid, for chaining
     */
    public @NotNull MuiChoiceGrid<T> enabled(boolean enabled) {
        this.enabled = enabled;
        this.rebuild();
        return this;
    }

    /**
     * Returns the current selection.
     *
     * @return the selected value, or {@code null}
     */
    public @Nullable T selectedValue() {
        return this.selected;
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.shimmerable = marker;
        this.rebuild();
        MuiShimmer.refresh();
    }

    /**
     * Rebuilds the card rows.
     */
    private void rebuild() {
        this.removeAllViews();

        if (this.options.isEmpty() && this.selected == null) {
            TextView empty = MuiControls.text(this.context, this.theme, "No options", 11,
                    MuiControls.alpha(this.theme.onSurfaceVariant(), 0.7F));
            empty.setPadding(MuiControls.dp(this.context, 2), MuiControls.dp(this.context, 4),
                    MuiControls.dp(this.context, 2), MuiControls.dp(this.context, 4));
            this.addView(empty, MuiControls.fill());
            return;
        }

        boolean known = this.selected == null;
        for (MuiOption<T> option : this.options) {
            if (Objects.equals(option.value(), this.selected)) {
                known = true;
                break;
            }
        }

        List<View> cards = new ArrayList<>(this.options.size() + 1);
        for (MuiOption<T> option : this.options) {
            cards.add(this.card(option, Objects.equals(option.value(), this.selected)));
        }
        if (!known) {
            cards.add(this.unknownCard(this.selected));
        }

        for (int i = 0; i < cards.size(); i += this.columns) {
            LinearLayout row = MuiControls.column(this.context);
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            int end = Math.min(i + this.columns, cards.size());
            for (int j = i; j < end; j++) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F);
                if (j + 1 < end) params.rightMargin = MuiControls.dp(this.context, 8);
                row.addView(cards.get(j), params);
            }

            LinearLayout.LayoutParams rowParams = MuiControls.fill();
            if (i > 0) rowParams.topMargin = MuiControls.dp(this.context, 8);
            this.addView(row, rowParams);
        }
    }

    /**
     * Creates the padded card shell shared by option cards and the unknown-value card.
     *
     * @param active   whether the card is interactive
     * @param selected whether the card is selected
     * @param error    whether the card uses the error outline
     * @return the styled card shell
     */
    private @NotNull LinearLayout newCard(boolean active, boolean selected, boolean error) {
        LinearLayout card = MuiControls.column(this.context);
        int padding = MuiControls.dp(this.context, 10);
        card.setPadding(padding, padding, padding, padding);

        int stroke = error ? this.theme.error() : selected ? this.theme.primary() : MuiControls.alpha(this.theme.outline(), 0.8F);

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(this.context, this.theme.cornerRadius()));
        background.setColor(active ? this.theme.surfaceHigh() : MuiControls.alpha(this.theme.surfaceHigh(), 0.55F));
        background.setStroke(Math.max(1, MuiControls.dp(this.context, 1)), stroke);
        card.setBackground(background);
        return card;
    }

    /**
     * Builds one selectable card.
     *
     * @param option   option model
     * @param selected whether this card is selected
     * @return the card view
     */
    private @NotNull View card(@NotNull MuiOption<T> option, boolean selected) {
        boolean active = this.enabled && option.enabled();
        LinearLayout card = this.newCard(active, selected, false);

        MuiOption.Preview preview = option.preview();
        if (preview != null) card.addView(preview.create(this.context, this.theme), MuiControls.fill());

        TextView title = MuiControls.text(this.context, this.theme, option.label(), 13, selected ? this.theme.primary() : (active ? this.theme.onSurface() : MuiControls.alpha(this.theme.onSurface(), 0.5F)));
        LinearLayout.LayoutParams titleParams = MuiControls.fill();
        if (preview != null) titleParams.topMargin = MuiControls.dp(this.context, 8);
        card.addView(title, titleParams);

        CharSequence description = option.description();
        if (description != null && !description.isEmpty()) {
            card.addView(MuiControls.text(this.context, this.theme, description, 10, MuiControls.alpha(this.theme.onSurfaceVariant(), active ? 1.0F : 0.6F)), MuiControls.fill());
        }

        CharSequence status = option.badge();
        if (status == null && selected && !this.selectedLabel.isEmpty()) status = this.selectedLabel;
        if (status != null && !status.toString().isEmpty()) {
            TextView badge = MuiControls.text(this.context, this.theme, status, 10, selected ? this.theme.primary() : this.theme.onSurfaceVariant());
            badge.setGravity(Gravity.END);
            LinearLayout.LayoutParams badgeParams = MuiControls.fill();
            badgeParams.topMargin = MuiControls.dp(this.context, 4);
            card.addView(badge, badgeParams);
        }

        if (active) {
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(view -> this.select(option.value()));
        }
        MuiShimmerScope.apply(card, this.shimmerable);
        return card;
    }

    /**
     * Builds the placeholder card for a current value that is not in the option list.
     *
     * @param value the unknown value
     * @return the card view
     */
    private @NotNull View unknownCard(@Nullable T value) {
        LinearLayout card = this.newCard(false, false, true);

        card.addView(MuiControls.text(this.context, this.theme, String.valueOf(value), 13, this.theme.onSurface()), MuiControls.fill());
        TextView badge = MuiControls.text(this.context, this.theme, "Unknown value", 10, this.theme.error());
        badge.setGravity(Gravity.END);
        LinearLayout.LayoutParams params = MuiControls.fill();
        params.topMargin = MuiControls.dp(this.context, 4);
        card.addView(badge, params);
        MuiShimmerScope.apply(card, this.shimmerable);
        return card;
    }

    /**
     * Applies a user selection.
     *
     * @param value the selected value
     */
    private void select(@Nullable T value) {
        this.selected = value;
        this.rebuild();
        if (this.onSelected != null && value != null) this.onSelected.accept(value);
    }
}