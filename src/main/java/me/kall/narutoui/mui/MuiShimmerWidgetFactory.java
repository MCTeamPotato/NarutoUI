package me.kall.narutoui.mui;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.ShimmerButton;
import me.kall.narutoui.mui.shimmer.ShimmerSeekBar;
import me.kall.narutoui.mui.shimmer.ShimmerSwitch;
import me.kall.narutoui.mui.shimmer.ShimmerTextView;
import org.jetbrains.annotations.NotNull;

/**
 * Widget factory that creates the API's shimmer-enabled text, switch, slider and button controls.
 * Colors come from the active {@link MuiTheme}, so consumers can keep their own accent palette by
 * customizing the theme passed to the screen.
 */
public final class MuiShimmerWidgetFactory implements MuiWidgetFactory {
    private static final MuiShimmerWidgetFactory INSTANCE = new MuiShimmerWidgetFactory();

    private MuiShimmerWidgetFactory() {}

    /**
     * Returns the shared shimmer widget factory.
     *
     * @return the shimmer factory
     */
    public static @NotNull MuiWidgetFactory instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
        TextView view = new ShimmerTextView(context);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    @Override
    public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
        ShimmerSwitch toggle = new ShimmerSwitch(context);
        int accent = theme.primary();

        ShapeDrawable track = new ShapeDrawable();
        track.setShape(ShapeDrawable.RECTANGLE);
        track.setSize(MuiControls.dp(context, 52), MuiControls.dp(context, 32));
        track.setCornerRadius(MuiControls.dp(context, 16));
        track.setColor(new ColorStateList(
                new int[][]{
                        new int[]{-R.attr.state_enabled, -R.attr.state_checked},
                        new int[]{-R.attr.state_enabled, R.attr.state_checked},
                        new int[]{R.attr.state_checked},
                        StateSet.WILD_CARD
                },
                new int[]{
                        ColorStateList.modulateColor(theme.surfaceHigh(), 0.12F),
                        ColorStateList.modulateColor(accent, 0.38F),
                        accent,
                        theme.surfaceHigh()
                }));
        track.setStroke(Math.max(1, MuiControls.dp(context, 2)), new ColorStateList(
                new int[][]{
                        new int[]{R.attr.state_checked},
                        new int[]{-R.attr.state_enabled},
                        StateSet.WILD_CARD
                },
                new int[]{
                        0x00000000,
                        ColorStateList.modulateColor(theme.outline(), 0.12F),
                        theme.outline()
                }));
        toggle.setTrackDrawable(track);

        toggle.setThumbTintList(new ColorStateList(
                new int[][]{
                        new int[]{-R.attr.state_enabled, -R.attr.state_checked},
                        new int[]{-R.attr.state_enabled, R.attr.state_checked},
                        new int[]{R.attr.state_checked},
                        StateSet.WILD_CARD
                },
                new int[]{
                        ColorStateList.modulateColor(theme.onSurface(), 0.38F),
                        theme.surface(),
                        theme.onPrimary(),
                        theme.onSurfaceVariant()
                }));
        toggle.bind(track, accent, theme.surfaceHigh());
        return toggle;
    }

    @Override
    public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
        ShimmerSeekBar bar = new ShimmerSeekBar(context);
        bar.setMin(0);
        bar.setMax(Math.max(1, max));
        bar.setProgress(Math.max(0, Math.min(max, progress)));

        int accent = theme.primary();
        bar.setProgressTintList(ColorStateList.valueOf(accent));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(MuiControls.alpha(accent, 0.28F)));
        bar.setThumbTintList(ColorStateList.valueOf(accent));
        bar.bind(accent);
        return bar;
    }

    @Override
    public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme,
                                  @NotNull CharSequence label, boolean outlined,
                                  @NotNull View.OnClickListener onClick) {
        Button button = outlined
                ? new ShimmerButton(context, null, R.attr.buttonOutlinedStyle)
                : new ShimmerButton(context, null, R.attr.buttonTonalStyle);
        button.setText(label);
        button.setTextSize(12);
        button.setMinimumHeight(MuiControls.dp(context, 36));
        button.setOnClickListener(onClick);

        int[][] states = {
                new int[]{-R.attr.state_enabled},
                StateSet.WILD_CARD
        };
        int accent = theme.primary();

        ShapeDrawable shape = new ShapeDrawable();
        shape.setCornerRadius(MuiControls.dp(context, 18));
        if (outlined) {
            shape.setColor(0x00000000);
            shape.setStroke(Math.max(1, MuiControls.dp(context, 1)), new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.16F),
                            MuiControls.alpha(accent, 0.55F)
                    }));
            button.setTextColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.38F),
                            accent
                    }));
        } else {
            shape.setColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.16F),
                            accent
                    }));
            button.setTextColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(theme.onPrimary(), 0.38F),
                            theme.onPrimary()
                    }));
        }

        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(outlined
                        ? ColorStateList.modulateColor(accent, 0.18F)
                        : 0x33FFFFFF),
                shape, null));

        if (!outlined && button instanceof ShimmerButton shimmer) {
            shimmer.bindBackground(shape, accent);
            shimmer.setTextShimmer(false);
        }
        return button;
    }
}