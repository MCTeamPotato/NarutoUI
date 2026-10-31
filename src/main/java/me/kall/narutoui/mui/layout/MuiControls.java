package me.kall.narutoui.mui.layout;

import me.kall.narutoui.mui.field.MuiTextField;
import me.kall.narutoui.mui.field.MuiNumberField;
import me.kall.narutoui.mui.field.MuiListEditor;
import me.kall.narutoui.mui.choice.MuiOption;
import me.kall.narutoui.mui.choice.MuiDropdown;
import me.kall.narutoui.mui.choice.MuiChoiceGrid;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.factory.MuiWidgetFactory;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

/**
 * Theme-aware Modern UI widgets for configuration screens.
 * <p>
 * Every factory takes the current {@link MuiTheme} explicitly, so a screen can be themed without
 * touching global Modern UI state and multiple differently-themed screens can coexist. Fluent
 * layout building is available through {@link MuiLayout}, a {@code LinearLayout} subclass.
 */
public final class MuiControls {
    /** Alias for {@link ViewGroup.LayoutParams#MATCH_PARENT}. */
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    /** Alias for {@link ViewGroup.LayoutParams#WRAP_CONTENT}. */
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    /** Active widget factory, replaceable through {@link #setWidgetFactory(MuiWidgetFactory)}. */
    private static volatile MuiWidgetFactory widgetFactory = MuiWidgetFactory.modern();
    /** Per-screen factory stack used while a page or rail is being built. */
    private static final ThreadLocal<Deque<MuiWidgetFactory>> FACTORY_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private MuiControls() {}

    /**
     * Replaces the fallback widget factory. Prefer a per-screen factory via the screen builder;
     * this method is intended for a single-owner application.
     *
     * @param factory the new factory, or {@code null} to restore the default
     */
    public static void setWidgetFactory(@Nullable MuiWidgetFactory factory) {
        widgetFactory = factory == null ? MuiWidgetFactory.modern() : factory;
    }

    /**
     * Returns the fallback widget factory.
     *
     * @return the factory
     */
    public static @NotNull MuiWidgetFactory widgetFactory() {
        return widgetFactory;
    }

    /**
     * Runs an action with a temporary widget factory. Screens use this so two mods can render
     * different control styles without overwriting a global setting.
     *
     * @param factory the temporary factory
     * @param action  the action to run
     * @param <T>     result type
     * @return the action result
     */
    public static <T> T withFactory(@NotNull MuiWidgetFactory factory, @NotNull Supplier<T> action) {
        Deque<MuiWidgetFactory> stack = FACTORY_STACK.get();
        stack.push(factory);
        try {
            return action.get();
        } finally {
            stack.pop();
            if (stack.isEmpty()) FACTORY_STACK.remove();
        }
    }

    /**
     * Runs an action with a temporary widget factory.
     *
     * @param factory the temporary factory
     * @param action  the action to run
     */
    public static void withFactory(@NotNull MuiWidgetFactory factory, @NotNull Runnable action) {
        withFactory(factory, () -> {
            action.run();
            return null;
        });
    }

    private static @NotNull MuiWidgetFactory factory() {
        MuiWidgetFactory current = FACTORY_STACK.get().peek();
        return current == null ? widgetFactory : current;
    }

    /**
     * Converts density-independent pixels to physical pixels.
     *
     * @param context the screen context
     * @param value   size in DP
     * @return size in pixels
     */
    @SuppressWarnings("UnstableApiUsage")
    public static int dp(@NotNull Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    /**
     * Multiplies a color's alpha channel.
     *
     * @param color  the ARGB color
     * @param factor the alpha multiplier
     * @return the modulated color
     */
    public static int alpha(int color, float factor) {
        return ColorStateList.modulateColor(color, factor);
    }

    /**
     * Creates a plain text view.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    text content
     * @param size    text size in SP
     * @param color   text color
     * @return the text view
     */
    public static @NotNull TextView text(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text, float size, int color) {
        return factory().text(context, MuiLocalization.resolve(text), size, color, MuiShimmerable.NEVER);
    }

    /**
     * Creates a plain text view with a per-instance shimmer marker.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    text content
     * @param size    text size in SP
     * @param color   text color
     * @param marker  shimmer marker
     * @return the text view
     */
    public static @NotNull TextView text(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker) {
        return factory().text(context, MuiLocalization.resolve(text), size, color, marker);
    }

    /**
     * Creates a bold heading text view.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    text content
     * @param size    text size in SP
     * @param color   text color
     * @return the heading view
     */
    public static @NotNull TextView heading(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text, float size, int color) {
        return heading(context, theme, text, size, color, MuiShimmerable.NEVER);
    }

    /**
     * Creates a bold heading with a per-instance shimmer marker.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    heading text
     * @param size    text size in SP
     * @param color   text color
     * @param marker  shimmer marker
     * @return the heading view
     */
    public static @NotNull TextView heading(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker) {
        TextView view = text(context, theme, text, size, color, marker);
        view.setTextStyle(TextPaint.BOLD);
        return view;
    }

    /**
     * Creates a vertical layout.
     *
     * @param context the screen context
     * @return a new column
     */
    public static @NotNull LinearLayout column(@NotNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    /**
     * Creates a horizontal, vertically centered row with standard height and padding.
     *
     * @param context the screen context
     * @return a new row
     */
    public static @NotNull LinearLayout row(@NotNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        applyRowStyle(layout, context);
        return layout;
    }

    /**
     * Applies the shared row geometry to a layout. Used by {@link #row(Context)} and
     * {@link MuiLayout#row(Context, MuiTheme)} so both factories stay identical.
     *
     * @param layout  the layout to configure
     * @param context the screen context
     */
    static void applyRowStyle(@NotNull LinearLayout layout, @NotNull Context context) {
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setMinimumHeight(dp(context, 44));
        int dp2 = dp(context, 2);
        int dp3 = dp(context, 6);
        layout.setPadding(dp2, dp3, dp2, dp3);
    }

    /**
     * Creates the title and optional description column used by every field row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @return the label column
     */
    private static @NotNull LinearLayout labelBlock(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description) {
        LinearLayout texts = column(context);
        texts.addView(text(context, theme, title, 14, theme.onSurface()), fill());
        if (description != null) {
            TextView hint = text(context, theme, description, 11, theme.onSurfaceVariant());
            LinearLayout.LayoutParams params = fill();
            params.topMargin = dp(context, 2);
            texts.addView(hint, params);
        }
        return texts;
    }

    /**
     * Creates a rounded, outlined card body.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return a new card layout
     */
    public static @NotNull MuiLayout card(@NotNull Context context, @NotNull MuiTheme theme) {
        return MuiLayout.card(context, theme);
    }

    /**
     * Creates a one-pixel horizontal divider.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the divider view
     */
    public static @NotNull View divider(@NotNull Context context, @NotNull MuiTheme theme) {
        View view = new View(context);
        view.setBackground(new ColorDrawable(alpha(theme.outline(), 0.6F)));
        return view;
    }

    /**
     * Creates a themed text input.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    initial text
     * @return the text field
     */
    public static @NotNull MuiTextField textField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
        return factory().textField(context, theme, text);
    }

    /**
     * Creates a themed numeric input.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @return the number field
     */
    public static @NotNull MuiNumberField numberField(@NotNull Context context, @NotNull MuiTheme theme, long value) {
        return factory().numberField(context, theme, value);
    }

    /**
     * Creates a themed select control.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     option value type
     * @return the dropdown
     */
    public static <T> @NotNull MuiDropdown<T> dropdown(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        return factory().dropdown(context, theme, options);
    }

    /**
     * Creates a themed card-style selection control.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     option value type
     * @return the choice grid
     */
    public static <T> @NotNull MuiChoiceGrid<T> choiceGrid(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
        return factory().choiceGrid(context, theme, options);
    }

    /**
     * Creates a themed list editor.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param values  initial entries
     * @return the list editor
     */
    public static @NotNull MuiListEditor listEditor(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values) {
        return factory().listEditor(context, theme, values);
    }

    /**
     * Returns wrap-content layout params.
     *
     * @return wrap layout params
     */
    public static @NotNull LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(WRAP, WRAP);
    }

    /**
     * Returns match-width, wrap-height layout params.
     *
     * @return fill layout params
     */
    public static @NotNull LinearLayout.LayoutParams fill() {
        return new LinearLayout.LayoutParams(MATCH, WRAP);
    }

    /**
     * Returns layout params that consume the remaining horizontal space.
     *
     * @return weighted layout params
     */
    public static @NotNull LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, WRAP, 1.0F);
    }

    /**
     * Creates a themed switch control.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the switch
     */
    public static @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
        return factory().switchControl(context, theme);
    }

    /**
     * Creates a themed switch control with a per-instance shimmer marker.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the switch
     */
    public static @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        return factory().switchControl(context, theme, marker);
    }

    /**
     * Creates a themed checkbox.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the checkbox
     */
    public static @NotNull CheckBox checkBox(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        return factory().checkBox(context, theme, marker);
    }

    /**
     * Creates a themed radio button.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the radio button
     */
    public static @NotNull RadioButton radioButton(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        return factory().radioButton(context, theme, marker);
    }

    /**
     * Creates a themed progress bar.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the progress bar
     */
    public static @NotNull ProgressBar progressBar(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        return factory().progressBar(context, theme, marker);
    }

    /**
     * Creates a switch row with a static initial state.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param value       initial checked state
     * @param onChange    state callback
     * @return the composed row
     */
    public static @NotNull LinearLayout switchRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, boolean value, @NotNull BooleanConsumer onChange) {
        LinearLayout row = row(context);
        row.addView(labelBlock(context, theme, title, description), weight());

        Switch toggle = switchControl(context, theme);
        toggle.setChecked(value);
        toggle.setOnCheckedChangeListener((view, checked) -> onChange.accept(checked));

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(toggle, params);
        return row;
    }

    /**
     * A switch row backed by an external {@link BooleanSupplier}. The supplier is polled on the
     * UI thread about ten times per second, so an external state change is reflected without
     * rebuilding the screen. Polling stops when the row leaves the window.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param value       live state supplier
     * @param onChange    state callback
     * @return the composed row
     */
    public static @NotNull LinearLayout switchRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        LinearLayout row = row(context);
        row.addView(labelBlock(context, theme, title, description), weight());

        Switch toggle = switchControl(context, theme);
        toggle.setChecked(value.getAsBoolean());

        boolean[] syncing = new boolean[1];
        toggle.setOnCheckedChangeListener((view, checked) -> {
            if (!syncing[0]) onChange.accept(checked);
        });

        refreshPeriodically(toggle, () -> {
            boolean expected = value.getAsBoolean();
            if (toggle.isChecked() == expected) return;

            syncing[0] = true;
            toggle.setChecked(expected);
            syncing[0] = false;
        });

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(toggle, params);
        return row;
    }

    /**
     * Creates a text field row with a static initial value. The value is written on commit (Enter or
     * focus loss), not on every keystroke.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param value       initial text
     * @param onCommit    commit callback
     * @return the composed row
     */
    public static @NotNull LinearLayout textFieldRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull CharSequence value, @NotNull Consumer<String> onCommit) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiTextField field = textField(context, theme, value).onCommit(onCommit);
        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(field, params);
        return block;
    }

    /**
     * A text field row whose text tracks an external supplier. The supplier is polled only while the
     * field is not focused, so typing is never overwritten.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param value       live text supplier
     * @param onCommit    commit callback
     * @return the composed row
     */
    public static @NotNull LinearLayout textFieldRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull Supplier<String> value, @NotNull Consumer<String> onCommit) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiTextField field = textField(context, theme, value.get()).onCommit(onCommit);
        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(field, params);

        refreshPeriodically(field, () -> {
            if (field.isFocused()) return;
            String expected = value.get();
            if (!expected.contentEquals(field.text())) field.text(expected);
        });
        return block;
    }

    /**
     * Creates a string list row. The editor reports the complete list on every edit.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param values      initial entries
     * @param onChanged   change callback
     * @return the composed row
     */
    public static @NotNull LinearLayout listRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<String> values, @NotNull Consumer<List<String>> onChanged) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiListEditor editor = listEditor(context, theme, values).onChanged(onChanged);
        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(editor, params);
        return block;
    }

    /**
     * Creates an integer number row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return the composed row
     */
    public static @NotNull LinearLayout numberRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        LinearLayout row = row(context);
        row.addView(labelBlock(context, theme, title, description), weight());

        MuiNumberField field = numberField(context, theme, value.getAsInt()).bounds(min, max).width(96).onChangeInt(onChange);

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(field, params);

        refreshPeriodically(field, () -> {
            if (field.isFocused()) return;
            int expected = value.getAsInt();
            if (field.longValue() != expected) field.value(expected);
        });
        return row;
    }

    /**
     * Creates a long number row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return the composed row
     */
    public static @NotNull LinearLayout longNumberRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, long min, long max, @NotNull LongSupplier value, @NotNull LongConsumer onChange) {
        LinearLayout row = row(context);
        row.addView(labelBlock(context, theme, title, description), weight());

        MuiNumberField field = numberField(context, theme, value.getAsLong()).bounds(min, max).width(96).onChangeLong(onChange);

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(field, params);

        refreshPeriodically(field, () -> {
            if (field.isFocused()) return;
            long expected = value.getAsLong();
            if (field.longValue() != expected) field.value(expected);
        });
        return row;
    }

    /**
     * Creates a decimal number row.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param min         minimum value (inclusive)
     * @param max         maximum value (inclusive)
     * @param value       live value supplier
     * @param onChange    value callback
     * @return the composed row
     */
    public static @NotNull LinearLayout decimalRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, double min, double max, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange) {
        LinearLayout row = row(context);
        row.addView(labelBlock(context, theme, title, description), weight());

        MuiNumberField field = MuiNumberField.create(context, theme, value.getAsDouble(), -1).bounds(min, max).width(96).onChangeDouble(onChange);

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(field, params);

        refreshPeriodically(field, () -> {
            if (field.isFocused()) return;
            double expected = value.getAsDouble();
            if (Double.compare(field.doubleValue(), expected) != 0) field.value(expected);
        });
        return row;
    }

    /**
     * Creates a select row with a static selection.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    initial selection, or {@code null}
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return the composed row
     */
    public static <T> @NotNull LinearLayout dropdownRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiDropdown<T> dropdown = dropdown(context, theme, options).selected(selected).onSelected(onSelected);

        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(dropdown, params);
        return block;
    }

    /**
     * A select row whose selection tracks an external supplier.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param value       live selection supplier
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return the composed row
     */
    public static <T> @NotNull LinearLayout dropdownRowBound(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> value, @NotNull Consumer<T> onSelected) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiDropdown<T> dropdown = dropdown(context, theme, options).selected(value.get()).onSelected(onSelected);

        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(dropdown, params);

        refreshPeriodically(dropdown, () -> {
            if (dropdown.isExpanded()) return;
            T expected = value.get();
            if (!Objects.equals(expected, dropdown.selectedValue())) dropdown.selected(expected);
        });
        return block;
    }

    /**
     * Creates a card-style choice row with a static selection.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param selected    initial selection, or {@code null}
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return the composed row
     */
    public static <T> @NotNull LinearLayout choiceGridRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @Nullable T selected, @NotNull Consumer<T> onSelected) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiChoiceGrid<T> grid = choiceGrid(context, theme, options).selected(selected).onSelected(onSelected);

        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(grid, params);
        return block;
    }

    /**
     * A card-style choice row whose selection tracks an external supplier.
     *
     * @param context     the screen context
     * @param theme       active theme
     * @param title       row title
     * @param description optional description
     * @param options     available options
     * @param value       live selection supplier
     * @param onSelected  selection callback
     * @param <T>         option value type
     * @return the composed row
     */
    public static <T> @NotNull LinearLayout choiceGridRowBound(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @Nullable CharSequence description, @NotNull List<MuiOption<T>> options, @NotNull Supplier<T> value, @NotNull Consumer<T> onSelected) {
        LinearLayout block = column(context);
        block.addView(labelBlock(context, theme, title, description), fill());

        MuiChoiceGrid<T> grid = choiceGrid(context, theme, options).selected(value.get()).onSelected(onSelected);

        LinearLayout.LayoutParams params = fill();
        params.topMargin = dp(context, 6);
        block.addView(grid, params);

        refreshPeriodically(grid, () -> {
            T expected = value.get();
            if (!Objects.equals(expected, grid.selectedValue())) grid.selected(expected);
        });
        return block;
    }

    /**
     * Creates a static value row.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param title   row title
     * @param value   value text
     * @return the composed row
     */
    public static @NotNull LinearLayout valueRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull CharSequence value) {
        LinearLayout row = row(context);
        row.addView(text(context, theme, title, 14, theme.onSurface()), weight());
        row.addView(text(context, theme, value, 12, theme.onSurfaceVariant()), wrap());
        return row;
    }

    /**
     * A value row that refreshes its right-hand text from an external integer source.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param title   row title
     * @param value   live integer supplier
     * @return the composed row
     */
    public static @NotNull LinearLayout valueRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull IntSupplier value) {
        return valueRow(context, theme, title, Integer::toString, value);
    }

    /**
     * A value row with a custom integer formatter.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param title   row title
     * @param format  value formatter
     * @param value   live integer supplier
     * @return the composed row
     */
    public static @NotNull LinearLayout valueRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, @NotNull IntSupplier value) {
        LinearLayout row = row(context);
        row.addView(text(context, theme, title, 14, theme.onSurface()), weight());

        TextView valueView = text(context, theme, format.apply(value.getAsInt()), 12, theme.onSurfaceVariant());
        row.addView(valueView, wrap());

        refreshPeriodically(valueView, () -> {
            CharSequence next = format.apply(value.getAsInt());
            if (!next.toString().contentEquals(valueView.getText())) valueView.setText(next);
        });
        return row;
    }

    /**
     * Creates a themed slider with range {@code 0..max}.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param max      maximum progress
     * @param progress initial progress
     * @return the slider
     */
    public static @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
        return factory().slider(context, theme, max, progress);
    }

    /**
     * Creates a themed slider with a per-instance shimmer marker.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param max      maximum progress
     * @param progress initial progress
     * @param marker   shimmer marker
     * @return the slider
     */
    public static @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress, @NotNull MuiShimmerable marker) {
        return factory().slider(context, theme, max, progress, marker);
    }

    /**
     * Creates a slider row with a static value label.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    row title
     * @param value    value label
     * @param max      maximum progress
     * @param progress initial progress
     * @param onChange progress callback
     * @return the composed row
     */
    public static @NotNull LinearLayout sliderRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull CharSequence value, int max, int progress, @NotNull IntConsumer onChange) {
        LinearLayout block = column(context);
        sliderHeader(context, theme, title, value, block);

        SeekBar bar = slider(context, theme, max, progress);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                onChange.accept(progress);
            }
        });
        block.addView(bar, fill());
        return block;
    }

    /**
     * Creates a supplier-backed slider row with range {@code 0..max}.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    row title
     * @param format   value formatter
     * @param max      maximum value
     * @param value    live value supplier
     * @param onChange value callback
     * @return the composed row
     */
    public static @NotNull LinearLayout sliderRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return sliderRow(context, theme, title, format, 0, max, value, onChange);
    }

    /**
     * Adds the title/value header shared by the int and double slider rows.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param title   row title
     * @param value   formatted value text
     * @param block   slider column that receives the header
     * @return the value text view
     */
    private static @NotNull TextView sliderHeader(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull CharSequence value, @NotNull LinearLayout block) {
        LinearLayout header = row(context);
        header.addView(text(context, theme, title, 14, theme.onSurface()), weight());
        TextView valueView = text(context, theme, value, 12, theme.primary());
        header.addView(valueView, wrap());
        block.addView(header, fill());
        return valueView;
    }

    /**
     * Creates a supplier-backed slider row with an arbitrary inclusive range.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    row title
     * @param format   value formatter
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return the composed row
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull LinearLayout sliderRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        if (max <= min) {
            int fixed = min;
            return valueRow(context, theme, title, format, () -> fixed);
        }

        long span = (long) max - min;
        int sliderMax = (int) Math.min(span, MAX_SLIDER_STEPS);

        LinearLayout block = column(context);

        int initial = clamp(value.getAsInt(), min, max);
        TextView valueView = sliderHeader(context, theme, title, format.apply(initial), block);

        SeekBar bar = slider(context, theme, sliderMax, progressOf(initial, min, span, sliderMax));
        boolean[] tracking = new boolean[1];
        boolean[] syncing = new boolean[1];

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                int actual = valueOf(progress, min, span, sliderMax);
                valueView.setText(format.apply(actual));
                onChange.accept(actual);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tracking[0] = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tracking[0] = false;
            }
        });

        refreshPeriodically(bar, () -> {
            if (tracking[0] || syncing[0]) return;

            int expected = clamp(value.getAsInt(), min, max);
            int expectedProgress = progressOf(expected, min, span, sliderMax);
            if (bar.getProgress() != expectedProgress) {
                syncing[0] = true;
                bar.setProgress(expectedProgress);
                syncing[0] = false;
            }

            int actual = valueOf(bar.getProgress(), min, span, sliderMax);
            CharSequence next = format.apply(actual);
            if (!next.toString().contentEquals(valueView.getText())) valueView.setText(next);
        });

        block.addView(bar, fill());
        return block;
    }

    /**
     * Creates a supplier-backed decimal slider row. The slider maps the range onto a bounded number
     * of steps, while the value label always shows the exact snapped value.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param title    row title
     * @param format   value formatter
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param step     value increment, greater than {@code 0}
     * @param value    live value supplier
     * @param onChange value callback
     * @return the composed row
     */
    public static @NotNull LinearLayout sliderRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull DoubleFunction<CharSequence> format, double min, double max, double step, @NotNull DoubleSupplier value, @NotNull DoubleConsumer onChange) {
        if (!(max > min) || !(step > 0) || !Double.isFinite(min) || !Double.isFinite(max) || !Double.isFinite(step)) {
            double fixed = Math.max(max, min);
            return valueRow(context, theme, title, format, () -> fixed);
        }

        long steps = Math.round((max - min) / step);
        if (steps <= 0) steps = 1;
        int sliderMax = (int) Math.min(steps, MAX_SLIDER_STEPS);

        LinearLayout block = column(context);

        double initial = clamp(value.getAsDouble(), min, max);
        TextView valueView = sliderHeader(context, theme, title, format.apply(initial), block);

        SeekBar bar = slider(context, theme, sliderMax, progressOf(initial, min, max, sliderMax));
        boolean[] tracking = new boolean[1];
        boolean[] syncing = new boolean[1];

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                double actual = valueOf(progress, min, max, step, sliderMax);
                valueView.setText(format.apply(actual));
                onChange.accept(actual);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tracking[0] = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tracking[0] = false;
            }
        });

        refreshPeriodically(bar, () -> {
            if (tracking[0] || syncing[0]) return;

            double expected = clamp(value.getAsDouble(), min, max);
            int expectedProgress = progressOf(expected, min, max, sliderMax);
            if (bar.getProgress() != expectedProgress) {
                syncing[0] = true;
                bar.setProgress(expectedProgress);
                syncing[0] = false;
            }

            double actual = valueOf(bar.getProgress(), min, max, step, sliderMax);
            CharSequence next = format.apply(actual);
            if (!next.toString().contentEquals(valueView.getText())) valueView.setText(next);
        });

        block.addView(bar, fill());
        return block;
    }

    /**
     * Creates a supplier-backed decimal value row.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param title   row title
     * @param format  value formatter
     * @param value   live value supplier
     * @return the composed row
     */
    public static @NotNull LinearLayout valueRow(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence title, @NotNull DoubleFunction<CharSequence> format, @NotNull DoubleSupplier value) {
        LinearLayout row = row(context);
        row.addView(text(context, theme, title, 14, theme.onSurface()), weight());

        TextView valueView = text(context, theme, format.apply(value.getAsDouble()), 12, theme.onSurfaceVariant());
        row.addView(valueView, wrap());

        refreshPeriodically(valueView, () -> {
            CharSequence next = format.apply(value.getAsDouble());
            if (!next.toString().contentEquals(valueView.getText())) valueView.setText(next);
        });
        return row;
    }

    /**
     * Maps an integer value to a slider progress in {@code 0..sliderMax}.
     *
     * @param value     the value
     * @param min       range minimum
     * @param span      range span
     * @param sliderMax slider maximum progress
     * @return the slider progress
     */
    private static int progressOf(int value, int min, long span, int sliderMax) {
        if (span <= 0 || sliderMax <= 0) return 0;
        long offset = (long) value - min;
        return (int) Math.round((double) offset * sliderMax / span);
    }

    /**
     * Maps a slider progress back to an integer value.
     *
     * @param progress  slider progress
     * @param min       range minimum
     * @param span      range span
     * @param sliderMax slider maximum progress
     * @return the value
     */
    private static int valueOf(int progress, int min, long span, int sliderMax) {
        if (span <= 0 || sliderMax <= 0) return min;
        long offset = Math.round((double) progress * span / sliderMax);
        return (int) (min + offset);
    }

    /**
     * Maps a decimal value to a slider progress in {@code 0..sliderMax}.
     *
     * @param value     the value
     * @param min       range minimum
     * @param max       range maximum
     * @param sliderMax slider maximum progress
     * @return the slider progress
     */
    private static int progressOf(double value, double min, double max, int sliderMax) {
        double span = max - min;
        if (!(span > 0) || sliderMax <= 0) return 0;
        return (int) Math.round((value - min) / span * sliderMax);
    }

    /**
     * Maps a slider progress back to a decimal value snapped to the configured step.
     *
     * @param progress  slider progress
     * @param min       range minimum
     * @param max       range maximum
     * @param step      value increment
     * @param sliderMax slider maximum progress
     * @return the snapped value
     */
    private static double valueOf(int progress, double min, double max, double step, int sliderMax) {
        double span = max - min;
        if (!(span > 0) || sliderMax <= 0) return min;
        double raw = min + span * progress / sliderMax;
        double snapped = min + step * Math.round((raw - min) / step);
        return Math.max(min, Math.min(max, snapped));
    }

    /**
     * Clamps an integer to an inclusive range.
     *
     * @param value the value
     * @param min   lower bound
     * @param max   upper bound
     * @return the clamped value
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a decimal to an inclusive range.
     *
     * @param value the value
     * @param min   lower bound
     * @param max   upper bound
     * @return the clamped value
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Upper bound for the integer slider resolution. */
    private static final int MAX_SLIDER_STEPS = 100_000;

    /**
     * Runs {@code refresh} immediately and then every 100 ms while {@code host} is attached.
     *
     * @param host    the view whose attachment controls the polling lifetime
     * @param refresh refresh callback
     */
    private static void refreshPeriodically(@NotNull View host, @NotNull Runnable refresh) {
        refresh.run();

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (!host.isAttachedToWindow()) return;
                refresh.run();
                host.postDelayed(this, 100L);
            }
        };
        host.postDelayed(tick, 100L);
    }

    /**
     * Creates a pill button.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    button label
     * @param outlined {@code true} for an outlined button, {@code false} for a filled button
     * @param onClick  click listener
     * @return the button
     */
    public static @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick) {
        return factory().button(context, theme, MuiLocalization.resolve(label), outlined, onClick);
    }

    /**
     * Creates a pill button with a per-instance shimmer marker.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    button label
     * @param outlined {@code true} for an outlined button, {@code false} for a filled button
     * @param onClick  click listener
     * @param marker   shimmer marker
     * @return the button
     */
    public static @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick, @NotNull MuiShimmerable marker) {
        return factory().button(context, theme, MuiLocalization.resolve(label), outlined, onClick, marker);
    }
}