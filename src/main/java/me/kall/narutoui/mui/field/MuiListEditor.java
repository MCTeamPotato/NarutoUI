package me.kall.narutoui.mui.field;

import me.kall.narutoui.mui.widget.MuiTextAction;
import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A themed editor for a list of strings, used by list-valued configuration entries.
 * <p>
 * Every entry is a {@link MuiTextField}; edits commit when the field loses focus or the player
 * presses Enter. Adding and removing entries rebuilds the rows after committing pending text, so a
 * half-edited entry is not lost. The control reports the complete list through
 * {@link #onChanged(Consumer)} whenever the player edits, adds or removes an entry.
 * <p>
 * The editor grows with its content. It is intended for short lists (up to a few dozen entries);
 * very long lists should use a dedicated page with a scrolling list.
 */
public final class MuiListEditor extends LinearLayout {
    /** Context used for density conversion. */
    private final Context context;
    /** Active visual tokens. */
    private final MuiTheme theme;
    /** Current entries. */
    private final List<String> values = new ArrayList<>();
    /** Column that receives one row per entry. */
    private final LinearLayout items;
    /** Add button below the rows. */
    private final Button addButton;
    /** Fields for the current rows, in row order. */
    private final List<MuiTextField> fields = new ArrayList<>();
    /** Optional change listener. */
    private @Nullable Consumer<List<String>> onChanged;
    /** Maximum number of entries. */
    private int maxEntries = 64;
    /** Placeholder used by new entries. */
    private CharSequence itemPlaceholder = "";
    /** Whether the editor accepts interaction. */
    private boolean enabled = true;
    /** Guards programmatic list updates. */
    private boolean guard;

    /**
     * Creates a list editor.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param values  initial entries
     */
    public MuiListEditor(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.setOrientation(VERTICAL);
        this.values.addAll(values);

        this.items = MuiControls.column(context);
        this.addView(this.items, MuiControls.fill());

        this.addButton = MuiControls.button(context, theme, "Add", true, view -> this.add());
        LinearLayout.LayoutParams buttonParams = MuiControls.wrap();
        buttonParams.topMargin = MuiControls.dp(context, 6);
        this.addView(this.addButton, buttonParams);

        this.rebuild();
    }

    /**
     * Creates a list editor.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param values  initial entries
     * @return the editor
     */
    public static @NotNull MuiListEditor create(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values) {
        return new MuiListEditor(context, theme, values);
    }

    /**
     * Sets the label of the add button.
     *
     * @param label label or translation key
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor addLabel(@NotNull CharSequence label) {
        this.addButton.setText(MuiLocalization.resolve(label));
        return this;
    }

    /**
     * Sets the hint used by empty entries.
     *
     * @param placeholder hint text or translation key
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor itemPlaceholder(@NotNull CharSequence placeholder) {
        this.itemPlaceholder = placeholder;
        this.rebuild();
        return this;
    }

    /**
     * Sets the maximum number of entries.
     *
     * @param maxEntries maximum entries, at least {@code 0}
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor maxEntries(int maxEntries) {
        this.maxEntries = Math.max(0, maxEntries);
        this.rebuild();
        return this;
    }

    /**
     * Enables or disables the editor.
     *
     * @param enabled {@code false} to disable
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor enabled(boolean enabled) {
        this.enabled = enabled;
        this.rebuild();
        return this;
    }

    /**
     * Registers the change listener.
     *
     * @param listener the listener
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor onChanged(@Nullable Consumer<List<String>> listener) {
        this.onChanged = listener;
        return this;
    }

    /**
     * Returns the current entries.
     *
     * @return a copy of the entries
     */
    @Contract(pure = true)
    public @NotNull @Unmodifiable List<String> values() {
        return List.copyOf(this.values);
    }

    /**
     * Replaces the entries without firing the change listener.
     *
     * @param values the new entries
     * @return this editor, for chaining
     */
    public @NotNull MuiListEditor values(@NotNull List<String> values) {
        this.guard = true;
        this.values.clear();
        this.values.addAll(values);
        this.guard = false;
        this.rebuild();
        return this;
    }

    /**
     * Appends an entry.
     */
    private void add() {
        if (!this.enabled || this.values.size() >= this.maxEntries) return;
        this.commitAll();
        this.values.add("");
        this.rebuild();
        if (!this.fields.isEmpty()) this.fields.get(this.fields.size() - 1).requestFocus();
    }

    /**
     * Removes an entry.
     *
     * @param index entry index
     */
    private void remove(int index) {
        if (!this.enabled || index < 0 || index >= this.values.size()) return;
        this.commitAll();
        this.values.remove(index);
        this.rebuild();
        this.fire();
    }

    /**
     * Commits pending text in every row.
     */
    private void commitAll() {
        for (MuiTextField field : this.fields) field.commit();
    }

    /**
     * Rebuilds the rows from the current entries.
     */
    private void rebuild() {
        this.items.removeAllViews();
        this.fields.clear();

        for (int i = 0; i < this.values.size(); i++) {
            final int index = i;
            LinearLayout row = MuiControls.row(this.context);

            MuiTextField field = MuiControls.textField(this.context, this.theme, this.values.get(i))
                    .hint(this.itemPlaceholder)
                    .enabled(this.enabled)
                    .onCommit(value -> {
                        if (index >= this.values.size()) return;
                        if (value.equals(this.values.get(index))) return;
                        this.values.set(index, value);
                        this.fire();
                    });

            row.addView(field, MuiControls.weight());

            MuiTextAction remove = MuiTextAction.create(this.context, this.theme, "x", () -> this.remove(index)).enabled(this.enabled);
            LinearLayout.LayoutParams removeParams = MuiControls.wrap();
            removeParams.setMargins(MuiControls.dp(this.context, 10), 0, 0, 0);
            removeParams.gravity = Gravity.CENTER_VERTICAL;
            row.addView(remove, removeParams);

            this.items.addView(row, MuiControls.fill());
            this.fields.add(field);
        }

        if (this.values.isEmpty()) {
            TextView empty = MuiControls.text(this.context, this.theme, "No entries", 11, MuiControls.alpha(this.theme.onSurfaceVariant(), 0.7F));
            empty.setPadding(MuiControls.dp(this.context, 2), MuiControls.dp(this.context, 4), MuiControls.dp(this.context, 2), MuiControls.dp(this.context, 4));
            this.items.addView(empty, MuiControls.fill());
        }

        this.addButton.setEnabled(this.enabled && this.values.size() < this.maxEntries);
    }

    /**
     * Notifies the listener with a copy of the list.
     */
    private void fire() {
        if (this.guard || this.onChanged == null) return;
        this.onChanged.accept(List.copyOf(this.values));
    }
}