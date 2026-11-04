package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.theme.MuiTheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Small modal helpers for configuration screens.
 * <p>
 * The dialog is an overlay owned by a {@link MuiScreenHost}. It is removed before the selected
 * callback runs, so callbacks may close the screen, rebuild the current page or show another
 * dialog.
 */
public final class MuiDialog {
    private MuiDialog() {}

    /**
     * Shows a confirmation dialog with one or two actions.
     *
     * @param host         screen host that owns the overlay
     * @param title        dialog title
     * @param message      body text; empty hides the body
     * @param confirmLabel primary action label
     * @param onConfirm    primary action callback
     * @param cancelLabel  optional secondary action label, or {@code null}
     * @param onCancel     optional secondary action callback
     */
    public static void confirm(@NotNull MuiScreenHost host,
                               @NotNull CharSequence title,
                               @NotNull CharSequence message,
                               @NotNull CharSequence confirmLabel,
                               @NotNull Runnable onConfirm,
                               @Nullable CharSequence cancelLabel,
                               @Nullable Runnable onCancel) {
        Context context = host.requireContext();
        MuiTheme theme = MuiTheme.modern(context);

        FrameLayout overlay = new FrameLayout(context);
        overlay.setBackground(new ColorDrawable(0xB0000000));
        overlay.setClickable(true);

        MuiLayout card = MuiLayout.card(context, theme).padding(20);
        card.add(MuiControls.heading(context, theme, title, 18, theme.onSurface()), MuiControls.fill());

        if (message.length() > 0) {
            TextView body = MuiControls.text(context, theme, message, 12, theme.onSurfaceVariant());
            LinearLayout.LayoutParams bodyParams = MuiControls.fill();
            bodyParams.topMargin = MuiControls.dp(context, 10);
            card.add(body, bodyParams);
        }

        MuiLayout buttons = MuiLayout.row(context, theme);
        Button confirm = MuiControls.button(context, theme, confirmLabel, false, view -> {
            host.removeOverlay(overlay);
            onConfirm.run();
        });
        buttons.add(confirm, MuiControls.wrap());

        if (cancelLabel != null) {
            Button cancel = MuiControls.button(context, theme, cancelLabel, true, view -> {
                host.removeOverlay(overlay);
                if (onCancel != null) {
                    onCancel.run();
                }
            });
            LinearLayout.LayoutParams cancelParams = MuiControls.wrap();
            cancelParams.leftMargin = MuiControls.dp(context, 8);
            buttons.add(cancel, cancelParams);
        }

        LinearLayout.LayoutParams buttonParams = MuiControls.fill();
        buttonParams.topMargin = MuiControls.dp(context, 16);
        card.add(buttons, buttonParams);

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                MuiControls.dp(context, 460), MuiControls.WRAP, Gravity.CENTER);
        overlay.addView(card, cardParams);
        host.showOverlay(overlay);
    }

    /**
     * Shows a one-action dialog.
     *
     * @param host         screen host that owns the overlay
     * @param title        dialog title
     * @param message      body text; empty hides the body
     * @param confirmLabel action label
     * @param onConfirm    action callback
     */
    public static void notice(@NotNull MuiScreenHost host,
                              @NotNull CharSequence title,
                              @NotNull CharSequence message,
                              @NotNull CharSequence confirmLabel,
                              @NotNull Runnable onConfirm) {
        confirm(host, title, message, confirmLabel, onConfirm, null, null);
    }
}