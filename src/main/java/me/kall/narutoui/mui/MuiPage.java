package me.kall.narutoui.mui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.MeasureSpec;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * One page of a {@link MuiConfigScreen}. Pages are scrollable, centred columns with a maximum
 * readable width; they are built once and kept alive while the screen switches tabs.
 */
final class MuiPage {
    /** Default maximum readable width of the centered page column, in DP. */
    static final int DEFAULT_CONTENT_WIDTH = 880;

    /** Scrollable root view inserted into the shell content frame. */
    private final ScrollView root;

    /**
     * Builds a page and invokes its body immediately.
     *
     * @param context      the screen context
     * @param theme        the active theme
     * @param contentWidth maximum content width in DP, or {@code <= 0} for no cap
     * @param title        optional page title; when empty no header is added
     * @param subtitle     optional subtitle shown below the title
     * @param body         page content callback
     */
    MuiPage(@NotNull Context context, @NotNull MuiTheme theme, int contentWidth,
            @Nullable CharSequence title, @Nullable CharSequence subtitle,
            @NotNull Consumer<MuiPageContext> body) {
        int maxWidth = contentWidth <= 0 ? 0 : MuiControls.dp(context, contentWidth);
        this.root = new CenteredScrollView(context, maxWidth);
        this.root.setClipToPadding(false);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setClipToPadding(false);
        int padding = MuiControls.dp(context, theme.spacing());
        content.setPadding(padding, padding, padding, MuiControls.dp(context, 48));

        this.root.addView(content, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));

        MuiPageContext page = new MuiPageContext(context, theme, content);
        if (title != null && !title.isEmpty()) page.header(title, subtitle);
        body.accept(page);
    }

    /**
     * Returns the page's scrollable root.
     *
     * @return the root view
     */
    @NotNull View root() {
        return this.root;
    }

    /**
     * Scroll view that caps its measured width so long text stays readable on wide windows.
     */
    private static final class CenteredScrollView extends ScrollView {
        /** Maximum measured width in pixels. */
        private final int maxWidth;

        /**
         * Creates a centered scroll view.
         *
         * @param context  the screen context
         * @param maxWidth maximum measured width in pixels
         */
        CenteredScrollView(@NotNull Context context, int maxWidth) {
            super(context);
            this.maxWidth = maxWidth;
        }

        /**
         * Caps the width measure spec at {@link #maxWidth} when the parent offers more space.
         *
         * @param widthMeasureSpec  incoming width spec
         * @param heightMeasureSpec incoming height spec
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (this.maxWidth > 0
                    && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED
                    && MeasureSpec.getSize(widthMeasureSpec) > this.maxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.maxWidth, MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}