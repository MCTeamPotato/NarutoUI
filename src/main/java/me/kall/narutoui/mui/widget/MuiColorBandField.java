package me.kall.narutoui.mui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.color.MuiColorBand;
import me.kall.narutoui.mui.color.MuiColorBands;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A two-color band row: title, live ramp preview and two color fields.
 * <p>
 * Like {@link MuiColorField}, this widget owns presentation only. The caller decides how to show a
 * color picker and updates the field after the user confirms a color. That keeps it usable both in
 * configuration shells and in custom layouts, and avoids coupling the widget to an overlay host.
 *
 * <pre>{@code
 * MuiColorBandField band = MuiColorBandField.create(context, theme, "Glow band", first, second)
 *         .labels("Start", "End", "Pick")
 *         .onPickFirst(() -> showPicker(band.firstColor(), band::first))
 *         .onPickSecond(() -> showPicker(band.secondColor(), band::second));
 * }</pre>
 */
public final class MuiColorBandField extends LinearLayout {
    private static final String DEFAULT_FIRST_LABEL = "First color";
    private static final String DEFAULT_SECOND_LABEL = "Second color";
    private static final String DEFAULT_PICK_LABEL = "Pick";

    private final Context context;
    private final MuiTheme theme;
    private final TextView title;
    private final MuiColorBandPreview preview;
    private final MuiColorField firstField;
    private final MuiColorField secondField;

    private int firstColor;
    private int secondColor;
    private CharSequence firstLabel = DEFAULT_FIRST_LABEL;
    private CharSequence secondLabel = DEFAULT_SECOND_LABEL;
    private CharSequence pickLabel = DEFAULT_PICK_LABEL;
    private Runnable onPickFirst = () -> {};
    private Runnable onPickSecond = () -> {};
    private Function<MuiColorBand, int[]> rampProvider = MuiColorBand::ramp;

    private MuiColorBandField(@NotNull Context context, @NotNull MuiTheme theme,
                              @NotNull CharSequence titleText, int first, int second) {
        super(context);
        this.context = context;
        this.theme = theme;
        this.firstColor = MuiColorBands.opaque(first);
        this.secondColor = MuiColorBands.opaque(second);

        this.setOrientation(VERTICAL);

        this.title = MuiControls.text(context, theme, titleText, 13, theme.onSurface());
        this.addView(this.title, MuiControls.fill());

        this.preview = MuiColorBandPreview.create(context, this.currentRamp(), 16);
        LinearLayout.LayoutParams previewParams = MuiControls.fill();
        previewParams.topMargin = MuiControls.dp(context, 8);
        this.addView(this.preview, previewParams);

        this.firstField = MuiColorField.create(context, theme,
                this.fieldLabel(this.firstLabel, this.firstColor), this.firstColor);
        this.firstField.buttonLabel(this.pickLabel);
        this.firstField.onPick(() -> this.onPickFirst.run());
        LinearLayout.LayoutParams firstParams = MuiControls.fill();
        firstParams.topMargin = MuiControls.dp(context, 10);
        this.addView(this.firstField, firstParams);

        this.secondField = MuiColorField.create(context, theme,
                this.fieldLabel(this.secondLabel, this.secondColor), this.secondColor);
        this.secondField.buttonLabel(this.pickLabel);
        this.secondField.onPick(() -> this.onPickSecond.run());
        LinearLayout.LayoutParams secondParams = MuiControls.fill();
        secondParams.topMargin = MuiControls.dp(context, 8);
        this.addView(this.secondField, secondParams);
    }

    /**
     * Creates a band field.
     *
     * @param context the screen context
     * @param theme   the active theme
     * @param title   row title
     * @param first   first color
     * @param second  second color
     * @return the field
     */
    public static @NotNull MuiColorBandField create(@NotNull Context context, @NotNull MuiTheme theme,
                                                    @NotNull CharSequence title, int first, int second) {
        return new MuiColorBandField(context, theme, title, first, second);
    }

    /**
     * Replaces the optional title tooltip.
     *
     * @param description tooltip text, or {@code null}
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField description(@Nullable CharSequence description) {
        MuiControls.applyTitleTooltip(this.title, description);
        return this;
    }

    /**
     * Replaces the field and pick-button labels.
     *
     * @param first  first color label
     * @param second second color label
     * @param pick   pick button label
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField labels(@NotNull CharSequence first, @NotNull CharSequence second,
                                             @NotNull CharSequence pick) {
        this.firstLabel = first;
        this.secondLabel = second;
        this.pickLabel = pick;
        this.firstField.label(this.fieldLabel(first, this.firstColor)).buttonLabel(pick);
        this.secondField.label(this.fieldLabel(second, this.secondColor)).buttonLabel(pick);
        return this;
    }

    /**
     * Replaces the first color.
     *
     * @param color ARGB color
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField first(int color) {
        this.firstColor = MuiColorBands.opaque(color);
        this.firstField.color(this.firstColor).label(this.fieldLabel(this.firstLabel, this.firstColor));
        this.refreshPreview();
        return this;
    }

    /**
     * Replaces the second color.
     *
     * @param color ARGB color
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField second(int color) {
        this.secondColor = MuiColorBands.opaque(color);
        this.secondField.color(this.secondColor).label(this.fieldLabel(this.secondLabel, this.secondColor));
        this.refreshPreview();
        return this;
    }

    /**
     * Replaces both colors at once.
     *
     * @param first  first color
     * @param second second color
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField colors(int first, int second) {
        this.firstColor = MuiColorBands.opaque(first);
        this.secondColor = MuiColorBands.opaque(second);
        this.firstField.color(this.firstColor).label(this.fieldLabel(this.firstLabel, this.firstColor));
        this.secondField.color(this.secondColor).label(this.fieldLabel(this.secondLabel, this.secondColor));
        this.refreshPreview();
        return this;
    }

    /**
     * Replaces the ramp used by the live preview. Consumers that interpret the two colors as
     * core/deep can use a richer light-to-deep ramp here.
     *
     * @param provider ramp provider, or {@code null} for {@link MuiColorBand#ramp()}
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField rampProvider(@Nullable Function<MuiColorBand, int[]> provider) {
        this.rampProvider = provider == null ? MuiColorBand::ramp : provider;
        this.refreshPreview();
        return this;
    }

    /**
     * @return the current first color
     */
    public int firstColor() {
        return this.firstColor;
    }

    /**
     * @return the current second color
     */
    public int secondColor() {
        return this.secondColor;
    }

    /**
     * @return the current band value
     */
    public @NotNull MuiColorBand band() {
        return new MuiColorBand(this.firstColor, this.secondColor);
    }

    /**
     * @return the nine-stop OKLab ramp for the current colors
     */
    public int @NotNull [] ramp() {
        return this.band().ramp();
    }

    /**
     * Sets the action invoked when the first color field requests a picker.
     *
     * @param action picker action
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField onPickFirst(@NotNull Runnable action) {
        this.onPickFirst = action;
        return this;
    }

    /**
     * Sets the action invoked when the second color field requests a picker.
     *
     * @param action picker action
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField onPickSecond(@NotNull Runnable action) {
        this.onPickSecond = action;
        return this;
    }

    /**
     * Enables or disables the whole row.
     *
     * @param enabled {@code false} to disable both color fields
     * @return this field, for chaining
     */
    public @NotNull MuiColorBandField enabled(boolean enabled) {
        this.setEnabled(enabled);
        this.setAlpha(enabled ? 1.0F : 0.45F);
        this.title.setTextColor(enabled ? this.theme.onSurface() : MuiControls.alpha(this.theme.onSurface(), 0.38F));
        this.firstField.enabled(enabled);
        this.secondField.enabled(enabled);
        return this;
    }

    private void refreshPreview() {
        this.preview.colors(this.currentRamp());
    }

    private int @NotNull [] currentRamp() {
        int[] ramp = this.rampProvider.apply(this.band());
        return ramp == null ? this.band().ramp() : ramp;
    }

    private @NotNull CharSequence fieldLabel(@NotNull CharSequence base, int color) {
        return base + "  " + MuiColorBands.toHex(color);
    }
}