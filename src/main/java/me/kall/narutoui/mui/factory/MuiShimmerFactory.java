package me.kall.narutoui.mui.factory;

import me.kall.narutoui.mui.theme.MuiTheme;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.CheckBox;
import icyllis.modernui.widget.ProgressBar;
import icyllis.modernui.widget.RadioButton;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.widget.ShimmerButton;
import me.kall.narutoui.mui.shimmer.widget.ShimmerCheckBox;
import me.kall.narutoui.mui.shimmer.widget.ShimmerProgressBar;
import me.kall.narutoui.mui.shimmer.widget.ShimmerRadioButton;
import me.kall.narutoui.mui.shimmer.widget.ShimmerSeekBar;
import me.kall.narutoui.mui.shimmer.widget.ShimmerSwitch;
import me.kall.narutoui.mui.shimmer.widget.ShimmerTextView;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;

/**
 * Widget factory that creates shimmer-capable text, switch, slider, button, checkbox, radio button
 * and progress bar controls. All of them default to {@link MuiShimmerable#NEVER}; enable a widget
 * explicitly or through {@code MuiSettingsSection.shimmerNext()}. Colors come from the active
 * {@link MuiTheme}, so consumers can keep their own accent palette by customizing the theme passed
 * to the screen.
 */
public final class MuiShimmerFactory implements MuiWidgetFactory {
    private static final MuiShimmerFactory INSTANCE = new MuiShimmerFactory();

    private MuiShimmerFactory() {}

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
        return this.text(context, text, size, color, MuiShimmerable.NEVER);
    }

    @Override
    public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker) {
        TextView view = new ShimmerTextView(context, marker);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    @Override
    public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
        return this.switchControl(context, theme, MuiShimmerable.NEVER);
    }

    @Override
    public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        ShimmerSwitch toggle = new ShimmerSwitch(context);
        int accent = theme.primary();

        ShapeDrawable track = new ShapeDrawable();
        track.setShape(ShapeDrawable.RECTANGLE);
        track.setSize(MuiControls.dp(context, 52), MuiControls.dp(context, 32));
        track.setCornerRadius(MuiControls.dp(context, 16));
        track.setColor(new ColorStateList(new int[][]{new int[]{-R.attr.state_enabled, -R.attr.state_checked}, new int[]{-R.attr.state_enabled, R.attr.state_checked}, new int[]{R.attr.state_checked}, StateSet.WILD_CARD}, new int[]{ColorStateList.modulateColor(theme.surfaceHigh(), 0.12F), ColorStateList.modulateColor(accent, 0.38F), accent, theme.surfaceHigh()}));
        track.setStroke(Math.max(1, MuiControls.dp(context, 2)), new ColorStateList(new int[][]{new int[]{R.attr.state_checked}, new int[]{-R.attr.state_enabled}, StateSet.WILD_CARD}, new int[]{0x00000000, ColorStateList.modulateColor(theme.outline(), 0.12F), theme.outline()}));
        toggle.setTrackDrawable(track);

        toggle.setThumbTintList(new ColorStateList(new int[][]{new int[]{-R.attr.state_enabled, -R.attr.state_checked}, new int[]{-R.attr.state_enabled, R.attr.state_checked}, new int[]{R.attr.state_checked}, StateSet.WILD_CARD}, new int[]{ColorStateList.modulateColor(theme.onSurface(), 0.38F), theme.surface(), theme.onPrimary(), theme.onSurfaceVariant()}));
        return toggle.bind(track, accent, theme.surfaceHigh()).shimmerable(marker);
    }

    @Override
    public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
        return this.slider(context, theme, max, progress, MuiShimmerable.NEVER);
    }

    @Override
    public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress, @NotNull MuiShimmerable marker) {
        ShimmerSeekBar bar = new ShimmerSeekBar(context);
        bar.setMin(0);
        bar.setMax(Math.max(1, max));
        bar.setProgress(Math.max(0, Math.min(max, progress)));

        int accent = theme.primary();
        bar.setProgressTintList(ColorStateList.valueOf(accent));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(MuiControls.alpha(accent, 0.28F)));
        bar.setThumbTintList(ColorStateList.valueOf(accent));
        return bar.bind(accent).shimmerable(marker);
    }

    @Override
    public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick) {
        return this.button(context, theme, label, outlined, onClick, MuiShimmerable.NEVER);
    }

    @Override
    public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick, @NotNull MuiShimmerable marker) {
        Button button = new ShimmerButton(context, null, outlined ? R.attr.buttonOutlinedStyle : R.attr.buttonTonalStyle);
        button.setText(label);
        button.setTextSize(12);
        button.setMinimumHeight(MuiControls.dp(context, 36));
        button.setOnClickListener(onClick);

        int[][] states = {new int[]{-R.attr.state_enabled}, StateSet.WILD_CARD};
        int accent = theme.primary();

        ShapeDrawable shape = new ShapeDrawable();
        shape.setCornerRadius(MuiControls.dp(context, 18));
        if (outlined) {
            shape.setColor(0x00000000);
            shape.setStroke(Math.max(1, MuiControls.dp(context, 1)), new ColorStateList(states, new int[]{ColorStateList.modulateColor(accent, 0.16F), MuiControls.alpha(accent, 0.55F)}));
            button.setTextColor(new ColorStateList(states, new int[]{ColorStateList.modulateColor(accent, 0.38F), accent}));
        } else {
            shape.setColor(new ColorStateList(states, new int[]{ColorStateList.modulateColor(accent, 0.16F), accent}));
            button.setTextColor(new ColorStateList(states, new int[]{ColorStateList.modulateColor(theme.onPrimary(), 0.38F), theme.onPrimary()}));
        }

        button.setBackground(new RippleDrawable(ColorStateList.valueOf(outlined ? ColorStateList.modulateColor(accent, 0.18F) : 0x33FFFFFF), shape, null));

        if (button instanceof ShimmerButton shimmer) {
            if (!outlined) shimmer.bindBackground(shape, accent).setTextShimmer(false);
            shimmer.shimmerable(marker);
        }

        return button;
    }

    @Override
    public @NotNull CheckBox checkBox(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        ColorStateList tint = ColorStateList.valueOf(theme.primary());
        ColorStateList text = ColorStateList.valueOf(theme.onSurface());
        ShimmerCheckBox checkBox = new ShimmerCheckBox(context);
        checkBox.setTextColor(text);
        checkBox.setButtonTintList(tint);
        return checkBox.bind(tint, text).shimmerable(marker);
    }

    @Override
    public @NotNull RadioButton radioButton(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        ColorStateList tint = ColorStateList.valueOf(theme.primary());
        ColorStateList text = ColorStateList.valueOf(theme.onSurface());
        ShimmerRadioButton radio = new ShimmerRadioButton(context);
        radio.setTextColor(text);
        radio.setButtonTintList(tint);
        return radio.bind(tint, text).shimmerable(marker);
    }

    @Override
    public @NotNull ProgressBar progressBar(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
        int accent = theme.primary();
        ColorStateList progress = ColorStateList.valueOf(accent);
        ColorStateList background = ColorStateList.valueOf(MuiControls.alpha(accent, 0.28F));
        ColorStateList secondary = ColorStateList.valueOf(MuiControls.alpha(accent, 0.5F));
        ColorStateList indeterminate = ColorStateList.valueOf(accent);

        ShimmerProgressBar bar = new ShimmerProgressBar(context);
        bar.setProgressTintList(progress);
        bar.setProgressBackgroundTintList(background);
        bar.setSecondaryProgressTintList(secondary);
        bar.setIndeterminateTintList(indeterminate);
        return bar.bind(progress, background, secondary, indeterminate).shimmerable(marker);
    }
}