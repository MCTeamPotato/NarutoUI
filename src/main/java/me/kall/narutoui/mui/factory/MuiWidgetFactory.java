package me.kall.narutoui.mui.factory;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.*;
import me.kall.narutoui.mui.choice.MuiChoiceGrid;
import me.kall.narutoui.mui.choice.MuiDropdown;
import me.kall.narutoui.mui.choice.MuiOption;
import me.kall.narutoui.mui.field.MuiListEditor;
import me.kall.narutoui.mui.field.MuiNumberField;
import me.kall.narutoui.mui.field.MuiTextField;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiFormatting;
import me.kall.narutoui.mui.widget.MuiTextView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Creates the concrete widgets used by {@link MuiControls}. The screen API works with this factory
 * instead of hard-coding {@code TextView}, {@code Switch}, {@code SeekBar}, {@code Button},
 * {@code CheckBox}, {@code RadioButton} or {@code ProgressBar}, so a consumer can substitute
 * animated or otherwise custom controls without the API depending on it.
 * <p>
 * Install a factory through {@link MuiControls#setWidgetFactory(MuiWidgetFactory)}. The default is
 * {@link #modern()}; use {@link #shimmer()} for the API's animated controls. Every method is
 * abstract: a custom factory must implement the complete contract instead of relying on fallback
 * widget construction. Shimmer controls default to {@link MuiShimmerable#NEVER}; enable them with a
 * marker or {@code MuiSettingsSection.shimmerNext()}.
 */
public interface MuiWidgetFactory {
    /**
     * Creates a text view.
     *
     * @param context the screen context
     * @param text    text content
     * @param size    text size in SP
     * @param color   text color
     * @return the text view
     */
    @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color);

    /**
     * Creates a text view with a per-instance shimmer marker. Factories that do not support
     * shimmering can ignore the marker.
     *
     * @param context the screen context
     * @param text    text content
     * @param size    text size in SP
     * @param color   text color
     * @param marker  shimmer marker
     * @return the text view
     */
    @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker);

    /**
     * Creates a configured switch.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the switch
     */
    @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme);

    /**
     * Creates a configured switch with a per-instance shimmer marker.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the switch
     */
    @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker);

    /**
     * Creates a configured slider.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param max      maximum progress
     * @param progress initial progress
     * @return the slider
     */
    @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress);

    /**
     * Creates a configured slider with a per-instance shimmer marker.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param max      maximum progress
     * @param progress initial progress
     * @param marker   shimmer marker
     * @return the slider
     */
    @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress, @NotNull MuiShimmerable marker);

    /**
     * Creates a configured button.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    button label
     * @param outlined {@code true} for an outlined button
     * @param onClick  click listener
     * @return the button
     */
    @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick);

    /**
     * Creates a configured button with a per-instance shimmer marker.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    button label
     * @param outlined {@code true} for an outlined button
     * @param onClick  click listener
     * @param marker   shimmer marker
     * @return the button
     */
    @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick, @NotNull MuiShimmerable marker);

    /**
     * Creates a checkbox.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the checkbox
     */
    @NotNull CheckBox checkBox(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker);

    /**
     * Creates a radio button.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the radio button
     */
    @NotNull RadioButton radioButton(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker);

    /**
     * Creates a progress bar.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param marker  shimmer marker
     * @return the progress bar
     */
    @NotNull ProgressBar progressBar(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker);

    /**
     * Creates a text input.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param text    initial text
     * @return the text field
     */
    @NotNull MuiTextField textField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text);

    /**
     * Creates a numeric input.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param value   initial value
     * @return the number field
     */
    @NotNull MuiNumberField numberField(@NotNull Context context, @NotNull MuiTheme theme, long value);

    /**
     * Creates a select control.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     option value type
     * @return the dropdown
     */
    <T> @NotNull MuiDropdown<T> dropdown(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options);

    /**
     * Creates a card-style selection control.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param options available options
     * @param <T>     option value type
     * @return the choice grid
     */
    <T> @NotNull MuiChoiceGrid<T> choiceGrid(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options);

    /**
     * Creates a list editor.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param values  initial entries
     * @return the list editor
     */
    @NotNull MuiListEditor listEditor(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values);

    /**
     * Returns the default Modern UI widget factory.
     *
     * @return the default factory
     */
    static @NotNull MuiWidgetFactory modern() {
        return Modern.INSTANCE;
    }

    /**
     * Returns the shimmer-enabled widget factory.
     *
     * @return the shimmer factory
     */
    static @NotNull MuiWidgetFactory shimmer() {
        return MuiShimmerFactory.instance();
    }

    /**
     * Default themed widgets implemented with plain Modern UI controls.
     */
    final class Modern implements MuiWidgetFactory {
        private static final Modern INSTANCE = new Modern();

        private Modern() {}

        @Override
        public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
            // MuiTextView translates legacy § codes on every setText, so supplier-backed labels can
            // be refreshed with their raw formatted text without losing the styling. Use the normal
            // buffer: forcing BufferType.SPANNABLE makes even plain text a SpannableString, and
            // Modern UI's setSingleLine() then crashes inside setSpan.
            MuiTextView view = new MuiTextView(context);
            view.setTextSize(size);
            view.setTextColor(color);
            view.setText(text);
            return view;
        }

        @Override
        public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
            Switch toggle = new Switch(context);

            ShapeDrawable track = new ShapeDrawable();
            track.setShape(ShapeDrawable.RECTANGLE);
            track.setSize(MuiControls.dp(context, 52), MuiControls.dp(context, 32));
            track.setCornerRadius(MuiControls.dp(context, 16));
            track.setColor(new ColorStateList(
                    new int[][]{
                            new int[]{R.attr.state_checked},
                            new int[]{-R.attr.state_enabled},
                            StateSet.WILD_CARD
                    },
                    new int[]{
                            theme.primary(),
                            MuiControls.alpha(theme.surfaceHigh(), 0.45F),
                            theme.surfaceHigh()
                    }));
            track.setStroke(Math.max(1, MuiControls.dp(context, 1)), new ColorStateList(
                    new int[][]{
                            new int[]{R.attr.state_checked},
                            StateSet.WILD_CARD
                    },
                    new int[]{
                            0x00000000,
                            theme.outline()
                    }));
            toggle.setTrackDrawable(track);
            toggle.setThumbTintList(new ColorStateList(
                    new int[][]{
                            new int[]{R.attr.state_checked},
                            StateSet.WILD_CARD
                    },
                    new int[]{
                            theme.onPrimary(),
                            theme.onSurfaceVariant()
                    }));
            return toggle;
        }

        @Override
        public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
            SeekBar bar = new SeekBar(context);
            bar.setMin(0);
            bar.setMax(Math.max(1, max));
            bar.setProgress(Math.max(0, Math.min(max, progress)));
            bar.setProgressTintList(ColorStateList.valueOf(theme.primary()));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(MuiControls.alpha(theme.outline(), 0.55F)));
            bar.setThumbTintList(ColorStateList.valueOf(theme.primary()));
            return bar;
        }

        @Override
        public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick) {
            Button button = new Button(context);
            button.setText(MuiFormatting.parse(label));
            button.setTextSize(12);
            button.setMinimumHeight(MuiControls.dp(context, 36));
            button.setGravity(Gravity.CENTER);
            button.setPadding(MuiControls.dp(context, 16), 0, MuiControls.dp(context, 16), 0);
            button.setOnClickListener(onClick);

            ShapeDrawable shape = new ShapeDrawable();
            shape.setCornerRadius(MuiControls.dp(context, 18));
            if (outlined) {
                shape.setColor(0x00000000);
                shape.setStroke(Math.max(1, MuiControls.dp(context, 1)), MuiControls.alpha(theme.outline(), 0.8F));
                button.setTextColor(theme.primary());
            } else {
                shape.setColor(theme.primary());
                button.setTextColor(theme.onPrimary());
            }

            button.setBackground(new RippleDrawable(ColorStateList.valueOf(outlined ? MuiControls.alpha(theme.primary(), 0.16F) : 0x33FFFFFF), shape, null));
            return button;
        }

        @Override
        public @NotNull CheckBox checkBox(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setTextColor(theme.onSurface());
            checkBox.setButtonTintList(ColorStateList.valueOf(theme.primary()));
            return checkBox;
        }

        @Override
        public @NotNull RadioButton radioButton(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            RadioButton radio = new RadioButton(context);
            radio.setTextColor(theme.onSurface());
            radio.setButtonTintList(ColorStateList.valueOf(theme.primary()));
            return radio;
        }

        @Override
        public @NotNull ProgressBar progressBar(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            int accent = theme.primary();
            ProgressBar bar = new ProgressBar(context);
            bar.setProgressTintList(ColorStateList.valueOf(accent));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(MuiControls.alpha(accent, 0.28F)));
            bar.setSecondaryProgressTintList(ColorStateList.valueOf(MuiControls.alpha(accent, 0.5F)));
            bar.setIndeterminateTintList(ColorStateList.valueOf(accent));
            return bar;
        }

        @Override
        public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker) {
            return this.text(context, text, size, color);
        }

        @Override
        public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            return this.switchControl(context, theme);
        }

        @Override
        public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress, @NotNull MuiShimmerable marker) {
            return this.slider(context, theme, max, progress);
        }

        @Override
        public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick, @NotNull MuiShimmerable marker) {
            return this.button(context, theme, label, outlined, onClick);
        }

        @Override
        public @NotNull MuiTextField textField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
            return MuiTextField.create(context, theme, text);
        }

        @Override
        public @NotNull MuiNumberField numberField(@NotNull Context context, @NotNull MuiTheme theme, long value) {
            return MuiNumberField.create(context, theme, value);
        }

        @Override
        public <T> @NotNull MuiDropdown<T> dropdown(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
            return MuiControls.withFactory(this, () -> MuiDropdown.create(context, theme, options));
        }

        @Override
        public <T> @NotNull MuiChoiceGrid<T> choiceGrid(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> options) {
            return MuiControls.withFactory(this, () -> MuiChoiceGrid.create(context, theme, options));
        }

        @Override
        public @NotNull MuiListEditor listEditor(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values) {
            return MuiControls.withFactory(this, () -> MuiListEditor.create(context, theme, values));
        }
    }
}