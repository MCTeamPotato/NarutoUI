package me.kall.narutoui.mui.screen;

import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.choice.MuiChoiceGrid;
import me.kall.narutoui.mui.choice.MuiOption;
import me.kall.narutoui.mui.choice.MuiThemePicker;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.widget.MuiCollapsibleCard;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.MuiDescriptionRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.*;

/**
 * Section-level builder for {@link MuiSettingsScreen}. Every method accepts raw state accessors and
 * appends the fully-styled row; the caller never sees layout params, dividers or value labels.
 * <p>
 * A {@code description} passed to a row is shown as a tooltip when the pointer hovers over that
 * row's title. Long descriptions therefore no longer make the page taller or mix into the title
 * line. Use {@link #description(CharSequence, CharSequence)} or {@link #note(CharSequence, CharSequence)}
 * for text that should stay visible.
 */
public final class MuiSettingsSection {
    /** Owning page context. */
    private final MuiPageContext page;
    /** Card body that receives the rows. */
    private final MuiLayout card;
    /** Number of fields added, used for automatic dividers. */
    private int fields;
    /** Open note block that collects consecutive notes, or {@code null}. */
    private @Nullable MuiLayout noteBlock;
    /** Marker applied to the next row, consumed by {@link #add(View)}. */
    private @Nullable MuiShimmerable nextShimmerable;
    /** Marker applied to rows added to the current note block. */
    private MuiShimmerable noteShimmerable = MuiShimmerable.NEVER;
    /** Most recently added row, used by {@link #enabledWhen(BooleanSupplier)}. */
    private @Nullable View lastRow;

    MuiSettingsSection(@NotNull MuiPageContext page, @NotNull MuiLayout card) {
        this.page = page;
        this.card = card;
    }

    /**
     * Returns the owning page context.
     *
     * @return the page context
     */
    public @NotNull MuiPageContext page() {
        return this.page;
    }

    /**
     * Returns the card body. Prefer {@link #customLayout(Consumer)} over direct manipulation.
     *
     * @return the card layout
     */
    public @NotNull LinearLayout card() {
        return this.card;
    }

    /**
     * Returns the card body as a fluent layout wrapper.
     *
     * @return the card layout
     */
    public @NotNull MuiLayout cardLayout() {
        return this.card;
    }

    /**
     * Adds a supplier-backed switch row.
     *
     * @param title    row title
     * @param value    live state supplier
     * @param onChange state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, null, value, onChange));
    }

    /**
     * Adds a supplier-backed switch row with a description.
     *
     * @param title       row title
     * @param description optional description
     * @param value       live state supplier
     * @param onChange    state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, description, value, onChange));
    }

    /**
     * Adds a switch row with a static initial state.
     *
     * @param title    row title
     * @param value    initial checked state
     * @param onChange state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, boolean value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, null, value, onChange));
    }

    /**
     * Adds a switch row with a static initial state and a description.
     *
     * @param title       row title
     * @param description optional description
     * @param value       initial checked state
     * @param onChange    state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, @Nullable CharSequence description, boolean value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, description, value, onChange));
    }

    /**
     * Adds a supplier-backed integer slider with the default value label.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return this.slider(title, min, max, value, onChange, Integer::toString);
    }

    /**
     * Adds a supplier-backed integer slider with a description and the default value label.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, @Nullable CharSequence description, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return this.slider(title, description, min, max, value, onChange, Integer::toString);
    }

    /**
     * Adds a supplier-backed integer slider with a custom formatter.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @param format   value formatter
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange, @NotNull IntFunction<CharSequence> format) {
        return this.add(MuiControls.sliderRow(this.page.context(), this.page.theme(), title, format, min, max, value, onChange));
    }

    /**
     * Adds a supplier-backed decimal slider with a fixed step.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param step     value increment, greater than {@code 0}
     * @param value    live value supplier
     * @param onChange value callback
     * @param format   value formatter
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, double min, double max, double step, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange, @NotNull DoubleFunction<CharSequence> format) {
        return this.add(MuiControls.sliderRow(this.page.context(), this.page.theme(), title, format, min, max, step, value, onChange));
    }

    /**
     * Adds a supplier-backed integer slider with a description and custom formatter.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @param format      value formatter
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, @Nullable CharSequence description, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange, @NotNull IntFunction<CharSequence> format) {
        LinearLayout row = MuiControls.sliderRow(this.page.context(), this.page.theme(), title, description, format, min, max, value, onChange);
        return this.add(row);
    }

    /**
     * Adds a supplier-backed decimal slider with a description and a fixed step.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param step        value increment, greater than {@code 0}
     * @param value       live value supplier
     * @param onChange    value callback
     * @param format      value formatter
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, @Nullable CharSequence description, double min, double max, double step, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange, @NotNull DoubleFunction<CharSequence> format) {
        LinearLayout row = MuiControls.sliderRow(this.page.context(), this.page.theme(), title, description, format, min, max, step, value, onChange);
        return this.add(row);
    }

    /**
     * Adds a text field row with a static initial value and no description. The value is written on
     * commit (Enter or focus loss), so a half-typed string never reaches the config object.
     *
     * @param title    row title
     * @param value    initial text
     * @param onCommit commit callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection textField(@NotNull CharSequence title, @NotNull CharSequence value, @NotNull Consumer<String> onCommit) {
        return this.textField(title, null, value, onCommit);
    }

    /**
     * Adds a text field row whose text tracks an external supplier and has no description.
     *
     * @param title    row title
     * @param value    live text supplier
     * @param onCommit commit callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection textField(@NotNull CharSequence title, @NotNull Supplier<String> value, @NotNull Consumer<String> onCommit) {
        return this.textField(title, null, value, onCommit);
    }

    /**
     * Adds a text field row with a static initial value. The value is written on commit (Enter or
     * focus loss), so a half-typed string never reaches the config object.
     *
     * @param title       row title
     * @param description optional description
     * @param value       initial text
     * @param onCommit    commit callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection textField(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull CharSequence value, @NotNull Consumer<String> onCommit) {
        return this.add(MuiControls.textFieldRow(this.page.context(), this.page.theme(), title, description, value, onCommit));
    }

    /**
     * Adds a text field row whose text tracks an external supplier.
     *
     * @param title       row title
     * @param description optional description
     * @param value       live text supplier
     * @param onCommit    commit callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection textField(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull Supplier<String> value, @NotNull Consumer<String> onCommit) {
        return this.add(MuiControls.textFieldRow(this.page.context(), this.page.theme(), title, description, value, onCommit));
    }

    /**
     * Adds an integer number row without a description.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection number(@NotNull CharSequence title, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return this.number(title, null, min, max, value, onChange);
    }

    /**
     * Adds an integer number row.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection number(@NotNull CharSequence title, @Nullable CharSequence description, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return this.add(MuiControls.numberRow(this.page.context(), this.page.theme(), title, description, min, max, value, onChange));
    }

    /**
     * Adds a long number row without a description.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection longNumber(@NotNull CharSequence title, long min, long max, @NotNull LongSupplier value, @NotNull LongConsumer onChange) {
        return this.longNumber(title, null, min, max, value, onChange);
    }

    /**
     * Adds a long number row.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection longNumber(@NotNull CharSequence title, @Nullable CharSequence description, long min, long max, @NotNull LongSupplier value, @NotNull LongConsumer onChange) {
        return this.add(MuiControls.longNumberRow(this.page.context(), this.page.theme(), title, description, min, max, value, onChange));
    }

    /**
     * Adds a decimal number row without a description.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection decimal(@NotNull CharSequence title, double min, double max, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange) {
        return this.decimal(title, null, min, max, value, onChange);
    }

    /**
     * Adds a decimal number row.
     *
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection decimal(@NotNull CharSequence title, @Nullable CharSequence description, double min, double max, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange) {
        return this.add(MuiControls.decimalRow(this.page.context(), this.page.theme(), title, description, min, max, value, onChange));
    }

    /**
     * Adds a select row with a static selection and no description.
     *
     * @param title      row title
     * @param options    available options
     * @param selected   initial selection, or {@code null}
     * @param onSelected selection callback
     * @param <T>        option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection dropdown(@NotNull CharSequence title, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        return this.dropdown(title, null, options, selected, onSelected);
    }

    /**
     * Adds a select row with a static selection.
     *
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    initial selection, or {@code null}
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection dropdown(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        return this.add(MuiControls.dropdownRow(this.page.context(), this.page.theme(), title, description, options, selected, onSelected));
    }

    /**
     * Adds a select row whose selection tracks an external supplier and has no description.
     *
     * @param title      row title
     * @param options    available options
     * @param selected   live selection supplier
     * @param onSelected selection callback
     * @param <T>        option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection dropdownBound(@NotNull CharSequence title, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> selected, @NotNull Consumer<T> onSelected) {
        return this.dropdownBound(title, null, options, selected, onSelected);
    }

    /**
     * Adds a select row whose selection tracks an external supplier.
     *
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    live selection supplier
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection dropdownBound(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> selected, @NotNull Consumer<T> onSelected) {
        return this.add(MuiControls.dropdownRowBound(this.page.context(), this.page.theme(), title, description, options, selected, onSelected));
    }

    /**
     * Adds a card-style choice row with a static selection and no description.
     *
     * @param title      row title
     * @param options    available options
     * @param selected   initial selection, or {@code null}
     * @param onSelected selection callback
     * @param <T>        option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection choiceGrid(@NotNull CharSequence title, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        return this.choiceGrid(title, null, options, selected, onSelected);
    }

    /**
     * Adds a card-style choice row with a static selection.
     *
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    initial selection, or {@code null}
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection choiceGrid(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        return this.add(MuiControls.choiceGridRow(this.page.context(), this.page.theme(), title, description, options, selected, onSelected));
    }

    /**
     * Adds a card-style choice row whose selection tracks an external supplier and has no
     * description.
     *
     * @param title      row title
     * @param options    available options
     * @param selected   live selection supplier
     * @param onSelected selection callback
     * @param <T>        option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection choiceGridBound(@NotNull CharSequence title, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> selected, @NotNull Consumer<T> onSelected) {
        return this.choiceGridBound(title, null, options, selected, onSelected);
    }

    /**
     * Adds a card-style choice row whose selection tracks an external supplier.
     *
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    live selection supplier
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return this section, for chaining
     */
    public <T> @NotNull MuiSettingsSection choiceGridBound(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> selected, @NotNull Consumer<T> onSelected) {
        return this.add(MuiControls.choiceGridRowBound(this.page.context(), this.page.theme(), title, description, options, selected, onSelected));
    }

    /**
     * Adds a string list editor row without a description.
     *
     * @param title     row title
     * @param values    initial entries
     * @param onChanged change callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection list(@NotNull CharSequence title, @NotNull List<String> values, @NotNull Consumer<List<String>> onChanged) {
        return this.list(title, null, values, onChanged);
    }

    /**
     * Adds a string list editor row.
     *
     * @param title       row title
     * @param description optional description
     * @param values      initial entries
     * @param onChanged   change callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection list(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<String> values, @NotNull Consumer<List<String>> onChanged) {
        return this.add(MuiControls.listRow(this.page.context(), this.page.theme(), title, description, values, onChanged));
    }

    /**
     * Adds a static value row.
     *
     * @param title row title
     * @param value value text
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull CharSequence value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed integer value row.
     *
     * @param title row title
     * @param value live value supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull IntSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed decimal value row.
     *
     * @param title row title
     * @param value live decimal supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull DoubleSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed long value row.
     *
     * @param title row title
     * @param value live long supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull LongSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed integer value row with a custom formatter.
     *
     * @param title  row title
     * @param format value formatter
     * @param value  live value supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, @NotNull IntSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, format, value));
    }

    /**
     * Adds a paragraph inside the card.
     *
     * @param text paragraph text
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection text(@NotNull CharSequence text) {
        return this.add(MuiControls.text(this.page.context(), this.page.theme(), text, 12, this.page.theme().onSurfaceVariant()));
    }

    /**
     * Adds a title-only description row. A description row is a read-only entry with a title line
     * and an optional second line; use {@link #text(CharSequence)} for a plain paragraph.
     *
     * @param title title or translation key
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection description(@NotNull CharSequence title) {
        return this.description(title, null);
    }

    /**
     * Adds a description row. Participates in automatic divider placement like any other field.
     *
     * @param title       title or translation key
     * @param description optional description or translation key; {@code null} hides the line
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection description(@NotNull CharSequence title, @Nullable CharSequence description) {
        return this.add(MuiDescriptionRow.create(this.page.context(), this.page.theme(), title, description));
    }

    /**
     * Adds a title-only note.
     *
     * @param title title or translation key
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection note(@NotNull CharSequence title) {
        return this.note(Note.of(title));
    }

    /**
     * Adds a note with an optional description line.
     *
     * @param title       title or translation key
     * @param description optional description or translation key; {@code null} hides the line
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection note(@NotNull CharSequence title, @Nullable CharSequence description) {
        return this.note(Note.of(title, description));
    }

    /**
     * Adds one note to the current divider-free note block. Consecutive {@code note} calls share the
     * same block, so usage hints read as one group; any other row closes the block and starts a new
     * one. The block itself still gets the usual divider before it.
     *
     * @param note the note to add
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection note(@NotNull Note note) {
        MuiDescriptionRow row = MuiDescriptionRow.create(this.page.context(), this.page.theme(), note.title(), note.description());
        MuiLayout block = this.noteBlock;

        if (block == null || this.card.getChildCount() == 0 || this.card.getChildAt(this.card.getChildCount() - 1) != block) {
            block = MuiLayout.column(this.page.context(), this.page.theme());
            block.add(row, MuiControls.fill());

            MuiShimmerable marker = this.nextShimmerable == null ? MuiShimmerable.NEVER : this.nextShimmerable;
            this.add(block);
            this.noteBlock = block;
            this.noteShimmerable = marker;
        } else {
            MuiShimmerable marker = this.nextShimmerable;
            this.nextShimmerable = null;
            block.add(row, MuiControls.fill());
            MuiShimmerScope.apply(row, marker == null ? this.noteShimmerable : marker);
        }

        return this;
    }

    /**
     * Adds a two-column theme picker row using the default {@code "In use"} label.
     *
     * @param choices  candidate themes
     * @param selected callback invoked with the chosen theme
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection themePicker(@NotNull List<MuiThemePicker.Choice> choices, @NotNull Consumer<MuiTheme> selected) {
        return this.themePicker(choices, "In use", selected);
    }

    /**
     * Adds a two-column theme picker row.
     *
     * @param choices  candidate themes
     * @param inUse    label shown on the selected card
     * @param selected callback invoked with the chosen theme
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection themePicker(@NotNull List<MuiThemePicker.Choice> choices, @NotNull CharSequence inUse, @NotNull Consumer<MuiTheme> selected) {
        MuiChoiceGrid<MuiThemePicker.Choice> grid = MuiThemePicker.create(this.page.context(), this.page.theme(), choices, inUse);
        grid.onSelected(choice -> selected.accept(choice.theme()));
        return this.add(grid);
    }

    /**
     * Adds a full-width action button.
     *
     * @param label  button label
     * @param action click callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection button(@NotNull CharSequence label, @NotNull Runnable action) {
        return this.button(label, false, action);
    }

    /**
     * Adds a full-width action button.
     *
     * @param label    button label
     * @param outlined {@code true} for an outlined button, {@code false} for a filled button
     * @param action   click callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection button(@NotNull CharSequence label, boolean outlined, @NotNull Runnable action) {
        Button button = MuiControls.button(this.page.context(), this.page.theme(), label, outlined, view -> action.run());
        return this.add(button);
    }

    /**
     * Keeps the most recently added row enabled or disabled from an external predicate. Call this
     * immediately after the row it refers to; it is useful for options that an external mod or a
     * hardware capability can make unavailable at runtime.
     *
     * @param enabled availability predicate
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection enabledWhen(@NotNull BooleanSupplier enabled) {
        View row = this.lastRow;
        if (row != null) {
            MuiControls.enabledWhen(row, enabled);
        }
        return this;
    }

    /**
     * Adds an explicit divider. Automatic dividers are normally sufficient.
     *
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection divider() {
        this.noteBlock = null;
        this.card.addView(MuiControls.divider(this.page.context(), this.page.theme()), MuiControls.fill());
        return this;
    }

    /**
     * Adds a custom view and still participates in automatic divider placement.
     *
     * @param view the custom row
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection custom(@NotNull View view) {
        return this.add(view);
    }

    /**
     * Adds a nested collapsible card that starts expanded, without a summary.
     *
     * @param title card title
     * @param body  nested section body
     * @return the nested section
     */
    public @NotNull MuiSettingsSection collapsible(@NotNull CharSequence title, @NotNull Consumer<MuiSettingsSection> body) {
        return this.collapsible(title, null, true, body);
    }

    /**
     * Adds a nested collapsible card that starts expanded.
     *
     * @param title   card title
     * @param summary optional summary; empty hides it
     * @param body    nested section body
     * @return the nested section
     */
    public @NotNull MuiSettingsSection collapsible(@NotNull CharSequence title, @Nullable CharSequence summary, @NotNull Consumer<MuiSettingsSection> body) {
        return this.collapsible(title, summary, true, body);
    }

    /**
     * Adds a nested collapsible card as one row and exposes its body as another field section.
     * This keeps the native multi-level shader option menus readable without flattening them.
     *
     * @param title    card title
     * @param summary  optional summary; empty hides it
     * @param expanded whether the nested card starts expanded
     * @param body     nested section body
     * @return the nested section
     */
    public @NotNull MuiSettingsSection collapsible(@NotNull CharSequence title, @Nullable CharSequence summary, boolean expanded, @NotNull Consumer<MuiSettingsSection> body) {
        MuiCollapsibleCard card = MuiCollapsibleCard.card(
                this.page.context(), this.page.theme(), title, summary, expanded);
        this.add(card);

        MuiSettingsSection nested = new MuiSettingsSection(this.page, card.body());
        body.accept(nested);
        return nested;
    }

    /**
     * Adds arbitrary content as one automatic row using the fluent layout wrapper.
     *
     * @param body callback receiving the row body
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection customLayout(@NotNull Consumer<MuiLayout> body) {
        MuiLayout row = MuiLayout.column(this.page.context(), this.page.theme());
        body.accept(row);
        return this.add(row);
    }

    /**
     * Marks the next row added to this section. The marker is applied to every shimmer target
     * created by that row (labels, controls and values), then cleared. It does not affect later
     * rows.
     *
     * @param marker the marker
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection shimmerNext(@NotNull MuiShimmerable marker) {
        this.nextShimmerable = marker;
        return this;
    }

    /**
     * Enables shimmer for the next row. All shimmer is off by default, so this is the explicit
     * opt-in for one row.
     *
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection shimmerNext() {
        this.nextShimmerable = MuiShimmerable.ALWAYS;
        return this;
    }

    /**
     * Appends a row, inserting a divider before every row after the first.
     *
     * @param row row view
     * @return this section, for chaining
     */
    private @NotNull MuiSettingsSection add(@NotNull View row) {
        this.noteBlock = null;

        MuiShimmerable marker = this.nextShimmerable;
        this.nextShimmerable = null;
        if (marker != null) MuiShimmerScope.apply(row, marker);

        boolean first = this.fields == 0;
        if (!first) {
            LinearLayout.LayoutParams dividerParams = MuiControls.fill();
            dividerParams.topMargin = MuiControls.dp(this.page.context(), 10);
            this.card.addView(MuiControls.divider(this.page.context(), this.page.theme()), dividerParams);
        }
        this.fields++;

        LinearLayout.LayoutParams params = MuiControls.fill();
        if (!first) {
            params.topMargin = MuiControls.dp(this.page.context(), 10);
        }
        this.card.addView(row, params);
        this.lastRow = row;
        return this;
    }

    /**
     * One entry for {@link #note(Note)}: a title and an optional description line.
     *
     * @param title       title or translation key
     * @param description optional description or translation key; {@code null} hides the line
     */
    public record Note(@NotNull CharSequence title, @Nullable CharSequence description) {
        /**
         * Creates a note with a description.
         *
         * @param title       title or translation key
         * @param description optional description or translation key
         * @return the note
         */
        public static @NotNull Note of(@NotNull CharSequence title, @Nullable CharSequence description) {
            return new Note(title, description);
        }

        /**
         * Creates a title-only note.
         *
         * @param title title or translation key
         * @return the note
         */
        public static @NotNull Note of(@NotNull CharSequence title) {
            return new Note(title, null);
        }
    }
}