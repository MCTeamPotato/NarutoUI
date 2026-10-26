package me.kall.narutoui.mui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.InputFilter;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.function.IntConsumer;

/**
 * Generic card-list editor. A caller adapts its own card model to {@link Model}; the editor owns
 * the section/card layout, enabled toggle, position fields, presets, field toggles and reset
 * actions.
 */
public final class MuiCardEditor {
    private MuiCardEditor() {}

    /**
     * Localized strings used by the card editor.
     *
     * @param enabled       card enabled label
     * @param position      position section label
     * @param resetPosition reset-position action label
     * @param resetCard     reset-card action label
     * @param label         field label chip text
     * @param unit          field unit chip text
     * @param topLeft       top-left preset label
     * @param topRight      top-right preset label
     * @param bottomLeft    bottom-left preset label
     * @param bottomRight   bottom-right preset label
     */
    public record Labels(String enabled, String position, String resetPosition, String resetCard,
                         String label, String unit, String topLeft, String topRight,
                         String bottomLeft, String bottomRight) {
        /**
         * Returns the built-in English labels.
         *
         * @return English labels
         */
        public static Labels english() {
            return new Labels("Enabled", "Position", "Reset", "Reset card", "Label", "Unit",
                    "Top left", "Top right", "Bottom left", "Bottom right");
        }
    }

    /**
     * Adaptation contract for one editable card.
     */
    public interface Model {
        /**
         * Returns the card title shown as the section heading.
         *
         * @return the card title
         */
        @NotNull String title();

        /**
         * Returns an optional summary shown at the top of the section.
         *
         * @return the summary, or an empty string
         */
        default @NotNull String summary() {
            return "";
        }

        /**
         * Returns whether the card is enabled.
         *
         * @return {@code true} when visible
         */
        boolean enabled();

        /**
         * Updates the card enabled state.
         *
         * @param value the new state
         */
        void enabled(boolean value);

        /**
         * Returns the card's configured X position.
         *
         * @return the X coordinate
         */
        int x();

        /**
         * Returns the card's configured Y position.
         *
         * @return the Y coordinate
         */
        int y();

        /**
         * Moves the card.
         *
         * @param x the new X coordinate
         * @param y the new Y coordinate
         */
        void position(int x, int y);

        /**
         * Restores the default position. The default implementation does nothing.
         */
        default void resetPosition() {}

        /**
         * Moves the card to a corner preset.
         *
         * @param horizontal {@code 0} for left, {@code 1} for right
         * @param vertical   {@code 0} for top, {@code 1} for bottom
         */
        default void preset(int horizontal, int vertical) {}

        /**
         * Returns the editable fields in display order.
         *
         * @return the field models
         */
        @NotNull List<Field> fields();

        /**
         * Restores the card's default values. The default implementation does nothing.
         */
        default void reset() {}
    }

    /**
     * Adaptation contract for one card field.
     */
    public interface Field {
        /**
         * Returns the field title.
         *
         * @return the localized field name
         */
        @NotNull String title();

        /**
         * Returns whether the field is visible.
         *
         * @return the enabled state
         */
        boolean enabled();

        /**
         * Updates the field visibility.
         *
         * @param value the new state
         */
        void enabled(boolean value);

        /**
         * Returns whether the field supports a label chip.
         *
         * @return {@code true} when a label can be hidden
         */
        default boolean hasLabel() {
            return false;
        }

        /**
         * Returns whether the field label is currently shown.
         *
         * @return the label state
         */
        default boolean label() {
            return true;
        }

        /**
         * Updates the field label visibility.
         *
         * @param value the new state
         */
        default void label(boolean value) {}

        /**
         * Returns whether the field supports a unit chip.
         *
         * @return {@code true} when a unit can be hidden
         */
        default boolean hasUnit() {
            return false;
        }

        /**
         * Returns whether the field unit is currently shown.
         *
         * @return the unit state
         */
        default boolean unit() {
            return true;
        }

        /**
         * Updates the field unit visibility.
         *
         * @param value the new state
         */
        default void unit(boolean value) {}
    }

    /**
     * Renders one section per card.
     *
     * @param page    high-level page builder
     * @param cards   card models in display order
     * @param labels  localized labels
     * @param changed callback invoked after a configuration value changed
     */
    public static void add(@NotNull MuiSettingsPage page, @NotNull List<? extends Model> cards,
                           @NotNull Labels labels, @NotNull Runnable changed) {
        for (Model card : cards) {
            page.section(card.title(), section -> {
                if (!card.summary().isBlank()) section.text(card.summary());

                section.toggle(labels.enabled(), () -> card.enabled(), value -> card.enabled(value));
                section.custom(row -> addPosition(row, section, card, labels, changed));

                for (Field field : card.fields()) {
                    section.custom(row -> addField(row, section, field, labels, changed));
                }

                section.button(labels.resetCard(), () -> {
                    card.reset();
                    changed.run();
                });
            });
        }
    }

    /**
     * Adds the X/Y fields, reset action and four corner presets for one card.
     *
     * @param card    section card body
     * @param section owning section
     * @param model   card model
     * @param labels  localized labels
     * @param changed change callback
     */
    private static void addPosition(@NotNull LinearLayout card, @NotNull MuiSettingsSection section,
                                    @NotNull Model model, @NotNull Labels labels, @NotNull Runnable changed) {
        Context context = section.page().context();
        MuiTheme theme = section.page().theme();

        LinearLayout row = MuiControls.row(context);
        row.addView(MuiControls.text(context, theme, "X", 12, theme.onSurfaceVariant()), MuiControls.wrap());

        EditText x = numberField(context, theme, model.x());
        EditText y = numberField(context, theme, model.y());
        x.setGravity(Gravity.CENTER);
        y.setGravity(Gravity.CENTER);

        Runnable sync = () -> {
            if (!x.getText().toString().equals(String.valueOf(model.x()))) x.setText(String.valueOf(model.x()));
            if (!y.getText().toString().equals(String.valueOf(model.y()))) y.setText(String.valueOf(model.y()));
        };

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                MuiControls.dp(context, 64), MuiControls.WRAP);
        fieldParams.setMargins(MuiControls.dp(context, 6), 0, 0, 0);
        row.addView(x, fieldParams);

        LinearLayout.LayoutParams yLabelParams = MuiControls.wrap();
        yLabelParams.setMargins(MuiControls.dp(context, 14), 0, 0, 0);
        row.addView(MuiControls.text(context, theme, "Y", 12, theme.onSurfaceVariant()), yLabelParams);
        row.addView(y, fieldParams);

        View spacer = new View(context);
        row.addView(spacer, MuiControls.weight());

        TextView reset = action(context, theme, labels.resetPosition(), () -> {
            model.resetPosition();
            changed.run();
            sync.run();
        });
        row.addView(reset, MuiControls.wrap());
        card.addView(row, MuiControls.fill());

        LinearLayout presets = MuiControls.row(context);
        presets.setPadding(0, 0, 0, 0);
        presets.setMinimumHeight(0);

        addPreset(presets, section, labels.topLeft(), 0, 0, model, changed, sync);
        addPreset(presets, section, labels.topRight(), 1, 0, model, changed, sync);
        addPreset(presets, section, labels.bottomLeft(), 0, 1, model, changed, sync);
        addPreset(presets, section, labels.bottomRight(), 1, 1, model, changed, sync);

        LinearLayout.LayoutParams presetParams = MuiControls.fill();
        presetParams.topMargin = MuiControls.dp(context, 6);
        card.addView(presets, presetParams);

        bindNumber(x, 0, Integer.MAX_VALUE, value -> {
            model.position(value, model.y());
            changed.run();
        });
        bindNumber(y, 0, Integer.MAX_VALUE, value -> {
            model.position(model.x(), value);
            changed.run();
        });
    }

    /**
     * Adds one equally weighted corner preset button.
     *
     * @param presets  preset row
     * @param section  owning section
     * @param label    button label
     * @param horizontal horizontal preset index
     * @param vertical   vertical preset index
     * @param model    card model
     * @param changed  change callback
     * @param sync     input-field synchronizer
     */
    private static void addPreset(@NotNull LinearLayout presets, @NotNull MuiSettingsSection section,
                                  @NotNull String label, int horizontal, int vertical, @NotNull Model model,
                                  @NotNull Runnable changed, @NotNull Runnable sync) {
        Context context = section.page().context();
        Button button = MuiControls.button(context, section.page().theme(), label, true, view -> {
            model.preset(horizontal, vertical);
            changed.run();
            sync.run();
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F);
        params.setMargins(presets.getChildCount() == 0 ? 0 : MuiControls.dp(context, 6), 0, 0, 0);
        presets.addView(button, params);
    }

    /**
     * Adds one field row with an enabled switch and optional label/unit chips.
     *
     * @param card    section card body
     * @param section owning section
     * @param field   field model
     * @param labels  localized labels
     * @param changed change callback
     */
    private static void addField(@NotNull LinearLayout card, @NotNull MuiSettingsSection section,
                                 @NotNull Field field, @NotNull Labels labels, @NotNull Runnable changed) {
        Context context = section.page().context();
        MuiTheme theme = section.page().theme();

        LinearLayout row = MuiControls.row(context);
        row.addView(MuiControls.text(context, theme, field.title(), 13, theme.onSurface()), MuiControls.weight());

        if (field.hasLabel()) {
            row.addView(chip(context, theme, labels.label(), field.label(), checked -> {
                field.label(checked);
                changed.run();
            }), chipParams(context));
        }
        if (field.hasUnit()) {
            row.addView(chip(context, theme, labels.unit(), field.unit(), checked -> {
                field.unit(checked);
                changed.run();
            }), chipParams(context));
        }

        Switch toggle = MuiControls.switchControl(context, theme);
        toggle.setChecked(field.enabled());
        toggle.setOnCheckedChangeListener((view, checked) -> {
            field.enabled(checked);
            changed.run();
        });

        LinearLayout.LayoutParams toggleParams = MuiControls.wrap();
        toggleParams.setMargins(MuiControls.dp(context, 10), 0, 0, 0);
        row.addView(toggle, toggleParams);
        card.addView(row, MuiControls.fill());
    }

    /**
     * Creates a clickable chip whose selected state is reflected in its background and text color.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    chip text
     * @param checked  initial selected state
     * @param onChange selection callback
     * @return the chip view
     */
    private static @NotNull TextView chip(@NotNull Context context, @NotNull MuiTheme theme,
                                          @NotNull String label, boolean checked,
                                          @NotNull BooleanConsumer onChange) {
        TextView chip = MuiControls.text(context, theme, label, 11,
                checked ? theme.primary() : theme.onSurfaceVariant());
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 4),
                MuiControls.dp(context, 10), MuiControls.dp(context, 4));

        boolean[] state = {checked};
        Runnable apply = () -> {
            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(context, 14));
            background.setColor(state[0]
                    ? MuiControls.alpha(theme.primary(), 0.18F)
                    : theme.surfaceHigh());
            chip.setBackground(background);
            chip.setTextColor(state[0] ? theme.primary() : theme.onSurfaceVariant());
        };
        apply.run();
        chip.setOnClickListener(view -> {
            state[0] = !state[0];
            apply.run();
            onChange.accept(state[0]);
        });
        return chip;
    }

    /**
     * Returns layout params for a chip with a leading gap.
     *
     * @param context the screen context
     * @return chip layout params
     */
    private static @NotNull LinearLayout.LayoutParams chipParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(MuiControls.dp(context, 8), 0, 0, 0);
        return params;
    }

    /**
     * Creates a small primary-colored text action.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param label   action label
     * @param action  click callback
     * @return the action view
     */
    private static @NotNull TextView action(@NotNull Context context, @NotNull MuiTheme theme,
                                            @NotNull String label, @NotNull Runnable action) {
        TextView view = MuiControls.text(context, theme, label, 12, theme.primary());
        view.setPadding(MuiControls.dp(context, 8), MuiControls.dp(context, 4),
                MuiControls.dp(context, 8), MuiControls.dp(context, 4));
        view.setOnClickListener(v -> action.run());
        return view;
    }

    /**
     * Creates a rounded numeric input field.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @return the input field
     */
    private static @NotNull EditText numberField(@NotNull Context context, @NotNull MuiTheme theme, int value) {
        EditText field = new EditText(context);
        field.setSingleLine();
        field.setTextSize(14);
        field.setTextColor(theme.onSurface());
        field.setText(String.valueOf(value));

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, 8));
        background.setColor(theme.surfaceHigh());
        field.setBackground(background);
        field.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 6),
                MuiControls.dp(context, 10), MuiControls.dp(context, 6));
        field.setFilters((InputFilter) (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) return "";
            }
            return null;
        });
        return field;
    }

    /**
     * Binds a numeric {@link EditText} to an integer consumer, clamping to the given range.
     *
     * @param field    the input field
     * @param min      minimum value
     * @param max      maximum value
     * @param onChange value callback
     */
    private static void bindNumber(@NotNull EditText field, int min, int max,
                                   @NotNull IntConsumer onChange) {
        boolean[] guard = new boolean[1];
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (guard[0]) return;
                if (s.length() == 0) return;
                int parsed;
                try {
                    parsed = Integer.parseInt(s.toString());
                } catch (NumberFormatException exception) {
                    return;
                }

                int value = Math.max(min, Math.min(max, parsed));
                onChange.accept(value);
                if (value != parsed) {
                    guard[0] = true;
                    field.post(() -> {
                        field.setText(String.valueOf(value));
                        guard[0] = false;
                    });
                }
            }
        });
    }
}