package me.kall.narutoui.mui.field;

import me.kall.narutoui.mui.layout.MuiControls;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.KeyEvent;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Shared styling and commit behavior for the API's {@link EditText} controls.
 * <p>
 * {@link MuiTextField} and {@link MuiNumberField} differ in parsing and validation, but their
 * background, disabled state, width handling and commit listeners are identical. Keeping that code
 * here avoids two copies drifting apart.
 */
final class MuiInputSupport {
    private MuiInputSupport() {}

    /**
     * Applies the standard input style.
     *
     * @param field    the field to style
     * @param context  the screen context
     * @param theme    active theme
     * @param paddingY vertical padding in pixels
     * @return the background drawable, kept by the caller for validation and disabled states
     */
    static @NotNull ShapeDrawable style(@NotNull EditText field, @NotNull Context context, @NotNull MuiTheme theme, int paddingY) {
        field.setSingleLine();
        field.setTextSize(14);
        field.setTextColor(theme.onSurface());
        field.setHintTextColor(MuiControls.alpha(theme.onSurfaceVariant(), 0.72F));

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, 8));
        background.setColor(theme.surfaceHigh());
        background.setStroke(Math.max(1, MuiControls.dp(context, 1)), MuiControls.alpha(theme.outline(), 0.65F));
        field.setBackground(background);

        int paddingX = MuiControls.dp(context, 10);
        field.setPadding(paddingX, paddingY, paddingX, paddingY);
        return background;
    }

    /**
     * Applies the enabled or disabled appearance.
     *
     * @param field      the field
     * @param background its input background
     * @param theme      active theme
     * @param enabled    whether the field is enabled
     */
    static void applyEnabled(@NotNull EditText field, @NotNull ShapeDrawable background, @NotNull MuiTheme theme, boolean enabled) {
        field.setEnabled(enabled);
        field.setFocusable(enabled);
        field.setFocusableInTouchMode(enabled);
        field.setTextColor(enabled ? theme.onSurface() : MuiControls.alpha(theme.onSurface(), 0.38F));
        background.setColor(enabled ? theme.surfaceHigh() : MuiControls.alpha(theme.surfaceHigh(), 0.5F));
    }

    /**
     * Applies a fixed width in DP to the field's layout params.
     *
     * @param field   the field
     * @param context the screen context
     * @param dp      width in density-independent pixels
     */
    static void applyWidth(@NotNull EditText field, @NotNull Context context, int dp) {
        ViewGroup.LayoutParams currentParams = field.getLayoutParams();
        LinearLayout.LayoutParams params;
        if (currentParams instanceof LinearLayout.LayoutParams linear) {
            params = linear;
        } else if (currentParams instanceof ViewGroup.MarginLayoutParams margins) {
            params = new LinearLayout.LayoutParams(margins);
        } else if (currentParams != null) {
            params = new LinearLayout.LayoutParams(currentParams);
        } else {
            params = new LinearLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP);
        }
        params.width = MuiControls.dp(context, dp);
        field.setLayoutParams(params);
    }

    /**
     * Applies the validation outline when the validity changed.
     *
     * @param background    the input background
     * @param context       the screen context
     * @param theme         active theme
     * @param valid         whether the current text is valid
     * @param currentlyValid the previously applied validity
     * @return the new validity
     */
    static boolean applyValid(@NotNull ShapeDrawable background, @NotNull Context context, @NotNull MuiTheme theme, boolean valid, boolean currentlyValid) {
        if (valid == currentlyValid) return currentlyValid;
        background.setStroke(Math.max(1, MuiControls.dp(context, valid ? 1 : 2)), valid ? MuiControls.alpha(theme.outline(), 0.65F) : theme.error());
        return valid;
    }

    /**
     * Installs commit-on-focus-loss and commit-on-Enter listeners.
     *
     * @param field      the field
     * @param allowEnter whether Enter should commit (multiline fields return {@code false})
     * @param commit     commit action
     */
    static void installCommit(@NotNull EditText field, @NotNull BooleanSupplier allowEnter, @NotNull Runnable commit) {
        field.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) commit.run();
        });

        field.setOnKeyListener((view, keyCode, event) -> {
            if (!allowEnter.getAsBoolean()) return false;
            if (event.getAction() != KeyEvent.ACTION_UP) return false;
            if (keyCode != KeyEvent.KEY_ENTER && keyCode != KeyEvent.KEY_KP_ENTER) return false;
            commit.run();
            return true;
        });
    }
}