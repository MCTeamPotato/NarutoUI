package me.kall.narutoui.mui;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import org.jetbrains.annotations.NotNull;

/**
 * Creates the concrete widgets used by {@link MuiControls}. The screen API works with this factory
 * instead of hard-coding {@code TextView}, {@code Switch}, {@code SeekBar} or {@code Button}, so a
 * consumer can substitute animated or otherwise custom controls without the API depending on it.
 * <p>
 * Install a factory through {@link MuiControls#setWidgetFactory(MuiWidgetFactory)}. The default is
 * {@link #modern()}; use {@link #shimmer()} for the API's animated controls.
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
     * Creates a configured switch.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the switch
     */
    @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme);

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
     * Creates a configured button.
     *
     * @param context  the screen context
     * @param theme    active theme
     * @param label    button label
     * @param outlined {@code true} for an outlined button
     * @param onClick  click listener
     * @return the button
     */
    @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label,
                           boolean outlined, @NotNull View.OnClickListener onClick);

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
        return MuiShimmerWidgetFactory.instance();
    }

    /**
     * Default themed widgets implemented with plain Modern UI controls.
     */
    final class Modern implements MuiWidgetFactory {
        private static final Modern INSTANCE = new Modern();

        private Modern() {}

        @Override
        public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
            TextView view = new TextView(context);
            view.setText(text);
            view.setTextSize(size);
            view.setTextColor(color);
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
        public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label,
                                      boolean outlined, @NotNull View.OnClickListener onClick) {
            Button button = new Button(context);
            button.setText(label);
            button.setTextSize(12);
            button.setMinimumHeight(MuiControls.dp(context, 36));
            button.setGravity(Gravity.CENTER);
            button.setPadding(MuiControls.dp(context, 16), 0, MuiControls.dp(context, 16), 0);
            button.setOnClickListener(onClick);

            ShapeDrawable shape = new ShapeDrawable();
            shape.setCornerRadius(MuiControls.dp(context, 18));
            if (outlined) {
                shape.setColor(0x00000000);
                shape.setStroke(Math.max(1, MuiControls.dp(context, 1)),
                        MuiControls.alpha(theme.outline(), 0.8F));
                button.setTextColor(theme.primary());
            } else {
                shape.setColor(theme.primary());
                button.setTextColor(theme.onPrimary());
            }

            button.setBackground(new RippleDrawable(
                    ColorStateList.valueOf(outlined
                            ? MuiControls.alpha(theme.primary(), 0.16F)
                            : 0x33FFFFFF),
                    shape, null));
            return button;
        }
    }
}