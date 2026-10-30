package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.text.TextPaint;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.theme.MuiWidgetFactory;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
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
        return factory().text(context, MuiLocalization.resolve(text), size, color);
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
        TextView view = text(context, theme, text, size, color);
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
     * Creates a rounded, outlined card body.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return a new card layout
     */
    public static @NotNull LinearLayout card(@NotNull Context context, @NotNull MuiTheme theme) {
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

        LinearLayout texts = column(context);
        texts.addView(text(context, theme, title, 14, theme.onSurface()), fill());
        if (description != null) {
            TextView hint = text(context, theme, description, 11, theme.onSurfaceVariant());
            LinearLayout.LayoutParams params = fill();
            params.topMargin = dp(context, 2);
            texts.addView(hint, params);
        }
        row.addView(texts, weight());

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

        LinearLayout texts = column(context);
        texts.addView(text(context, theme, title, 14, theme.onSurface()), fill());
        if (description != null) {
            TextView hint = text(context, theme, description, 11, theme.onSurfaceVariant());
            LinearLayout.LayoutParams params = fill();
            params.topMargin = dp(context, 2);
            texts.addView(hint, params);
        }
        row.addView(texts, weight());

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

        LinearLayout header = row(context);
        header.addView(text(context, theme, title, 14, theme.onSurface()), weight());
        TextView valueView = text(context, theme, value, 12, theme.primary());
        header.addView(valueView, wrap());
        block.addView(header, fill());

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
        int lower = min;
        int upper = Math.max(lower + 1, max);
        int span = upper - lower;

        LinearLayout block = column(context);

        int initial = clamp(value.getAsInt(), lower, upper);
        LinearLayout header = row(context);
        header.addView(text(context, theme, title, 14, theme.onSurface()), weight());
        TextView valueView = text(context, theme, format.apply(initial), 12, theme.primary());
        header.addView(valueView, wrap());
        block.addView(header, fill());

        SeekBar bar = slider(context, theme, span, initial - lower);
        boolean[] tracking = new boolean[1];
        boolean[] syncing = new boolean[1];

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                int actual = lower + progress;
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

            int expected = clamp(value.getAsInt(), lower, upper);
            int expectedProgress = expected - lower;
            if (bar.getProgress() != expectedProgress) {
                syncing[0] = true;
                bar.setProgress(expectedProgress);
                syncing[0] = false;
            }

            int actual = lower + bar.getProgress();
            CharSequence next = format.apply(actual);
            if (!next.toString().contentEquals(valueView.getText())) valueView.setText(next);
        });

        block.addView(bar, fill());
        return block;
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
}