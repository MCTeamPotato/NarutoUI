package me.kall.narutoui.mui.choice;

import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A themed select control for a small, fixed set of values.
 * <p>
 * The control is composed from ordinary views: a full-width button shows the current option and a
 * panel expands in place when the button is clicked. That makes it usable inside a page scroll
 * without a popup window, and every part follows the same {@link MuiTheme} and localization rules
 * as the rest of the API.
 * <p>
 * It is intended for enum-valued configuration entries and other short option lists (roughly up to
 * a dozen entries). Longer lists should use a searchable page instead.
 *
 * @param <T> option value type
 */
public final class MuiDropdown<T> extends LinearLayout implements MuiShimmerContainer {
    /** Context used for density conversion. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Available options. */
    private final List<MuiOption<T>> options;
    /** Panel that receives the option rows. */
    private final MuiLayout rows;
    /** Button that shows the current option and toggles the panel. */
    private final Button header;
    /** Index of the selected option, or {@code -1}. */
    private int selectedIndex = -1;
    /** Whether the option panel is open. */
    private boolean expanded;
    /** Whether the control accepts interaction. */
    private boolean enabled = true;
    /** Text shown when nothing is selected. */
    private CharSequence placeholder = "-";
    /** Optional selection listener. */
    private @Nullable Consumer<T> onSelected;
    /** Marker applied to the header and option rows. */
    private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

    /**
     * Creates a dropdown.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     */
    public MuiDropdown(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.options = new ArrayList<>(options);

        this.setOrientation(VERTICAL);

        this.header = MuiControls.button(context, theme, "", true, view -> this.expanded(!this.expanded));
        this.addView(this.header, MuiControls.fill());

        this.rows = MuiLayout.column(context, theme);
        this.rows.padding(6);
        this.rows.spacing(2);
        ShapeDrawable panel = new ShapeDrawable();
        panel.setCornerRadius(MuiControls.dp(context, 10));
        panel.setColor(MuiControls.alpha(theme.surfaceHigh(), 0.96F));
        panel.setStroke(Math.max(1, MuiControls.dp(context, 1)), MuiControls.alpha(theme.outline(), 0.7F));
        this.rows.setBackground(panel);
        this.rows.setVisibility(View.GONE);

        LinearLayout.LayoutParams panelParams = MuiControls.fill();
        panelParams.topMargin = MuiControls.dp(context, 4);
        this.addView(this.rows, panelParams);

        this.refreshHeader();
        this.rebuildRows();
    }

    /**
     * Creates a dropdown.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     option value type
     * @return the dropdown
     */
    public static <T> @NotNull MuiDropdown<T> create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        return new MuiDropdown<>(context, theme, options);
    }

    /**
     * Creates a dropdown from values and their labels.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param values  option values
     * @param labels  label for each value, or a translation key
     * @param <T>     option value type
     * @return the dropdown
     */
    public static <T> @NotNull MuiDropdown<T> of(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<T> values, @NotNull List<? extends CharSequence> labels) {
        if (values.size() != labels.size()) {
            throw new IllegalArgumentException("values and labels must have the same size");
        }

        List<MuiOption<T>> options = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) options.add(MuiOption.of(values.get(i), labels.get(i)));
        return new MuiDropdown<>(context, theme, options);
    }

    /**
     * Selects an option without firing the selection listener.
     *
     * @param value the option value
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> selected(@Nullable T value) {
        this.selectedIndex = this.indexOf(value);
        this.refreshHeader();
        this.rebuildRows();
        return this;
    }

    /**
     * Selects an option by index without firing the selection listener.
     *
     * @param index the option index, or {@code -1} for no selection
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> selectedIndex(int index) {
        this.selectedIndex = index >= 0 && index < this.options.size() ? index : -1;
        this.refreshHeader();
        this.rebuildRows();
        return this;
    }

    /**
     * Replaces the option list and clears the selection if the old value is no longer present.
     *
     * @param options new options
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> options(@NotNull List<MuiOption<T>> options) {
        T selected = this.selectedValue();
        this.options.clear();
        this.options.addAll(options);
        this.selectedIndex = this.indexOf(selected);
        this.refreshHeader();
        this.rebuildRows();
        return this;
    }

    /**
     * Sets the text shown when no option is selected.
     *
     * @param placeholder placeholder text or translation key
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> placeholder(@NotNull CharSequence placeholder) {
        this.placeholder = placeholder;
        this.refreshHeader();
        return this;
    }

    /**
     * Enables or disables the control.
     *
     * @param enabled {@code false} to disable
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> enabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.expanded = false;
            this.rows.setVisibility(View.GONE);
        }
        this.header.setEnabled(enabled);
        this.refreshHeader();
        this.rebuildRows();
        return this;
    }

    /**
     * Registers the selection listener. Programmatic {@link #selected(Object)} updates do not fire
     * it.
     *
     * @param listener the listener
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> onSelected(@Nullable Consumer<T> listener) {
        this.onSelected = listener;
        return this;
    }

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        this.shimmerable = marker;
        MuiShimmerScope.apply(this.header, marker);
        MuiShimmerScope.apply(this.rows, marker);
        MuiShimmer.refresh();
    }

    /**
     * Returns the selected value.
     *
     * @return the selected value, or {@code null}
     */
    public @Nullable T selectedValue() {
        return this.selectedIndex < 0 ? null : this.options.get(this.selectedIndex).value();
    }

    /**
     * Returns whether the option panel is open.
     *
     * @return {@code true} when the panel is visible
     */
    public boolean isExpanded() {
        return this.expanded;
    }

    /**
     * Opens or closes the option panel.
     *
     * @param expanded {@code true} to open
     * @return this dropdown, for chaining
     */
    public @NotNull MuiDropdown<T> expanded(boolean expanded) {
        this.expanded = expanded && this.enabled;
        this.rows.setVisibility(this.expanded ? View.VISIBLE : View.GONE);
        this.refreshHeader();
        return this;
    }

    /**
     * Selects an option and fires the listener.
     *
     * @param index option index
     */
    private void select(int index) {
        if (index < 0 || index >= this.options.size()) return;
        if (!this.options.get(index).enabled()) return;
        this.selectedIndex = index;
        this.expanded = false;
        this.rows.setVisibility(View.GONE);
        this.refreshHeader();
        this.rebuildRows();
        if (this.onSelected != null) this.onSelected.accept(this.options.get(index).value());
    }

    /**
     * Rebuilds the option panel for the current selection.
     */
    private void rebuildRows() {
        this.rows.removeAllViews();
        for (int i = 0; i < this.options.size(); i++) {
            boolean selected = i == this.selectedIndex;
            MuiOption<T> option = this.options.get(i);
            boolean enabled = this.enabled && option.enabled();
            CharSequence label = MuiLocalization.resolve(option.label());
            TextView row = MuiControls.text(this.context, this.theme, label, 13, selected && enabled ? this.theme.primary() : this.theme.onSurface());
            row.setPadding(MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8));

            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(this.context, 6));
            background.setColor(selected ? MuiControls.alpha(this.theme.primary(), 0.14F) : 0x00000000);
            row.setBackground(background);
            row.setEnabled(enabled);
            if (!enabled) row.setTextColor(MuiControls.alpha(this.theme.onSurface(), 0.38F));

            final int index = i;
            row.setOnClickListener(view -> this.select(index));
            this.rows.addView(row, MuiControls.fill());
        }

        if (this.options.isEmpty()) {
            TextView empty = MuiControls.text(this.context, this.theme, "-", 13, MuiControls.alpha(this.theme.onSurfaceVariant(), 0.7F));
            empty.setPadding(MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8), MuiControls.dp(this.context, 8));
            this.rows.addView(empty, MuiControls.fill());
        }

        MuiShimmerScope.apply(this.rows, this.shimmerable);
    }

    /**
     * Updates the header text and enabled state.
     */
    private void refreshHeader() {
        CharSequence label = this.selectedIndex < 0 ? MuiLocalization.resolve(this.placeholder) : MuiLocalization.resolve(this.options.get(this.selectedIndex).label());
        this.header.setText(label + "  " + (this.expanded ? "^" : "v"));
        this.header.setTextColor(this.enabled ? this.theme.primary() : MuiControls.alpha(this.theme.primary(), 0.38F));
    }

    /**
     * Returns the index of a value.
     *
     * @param value the value
     * @return the index, or {@code -1}
     */
    private int indexOf(@Nullable T value) {
        for (int i = 0; i < this.options.size(); i++) {
            if (Objects.equals(this.options.get(i).value(), value)) return i;
        }
        return -1;
    }
}