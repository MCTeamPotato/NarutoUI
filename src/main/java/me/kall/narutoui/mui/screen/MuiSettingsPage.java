package me.kall.narutoui.mui.screen;

import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Page-level builder for {@link MuiSettingsScreen}. The API has already added the page header,
 * scroll container, content padding and section cards.
 */
public final class MuiSettingsPage {
    /** Underlying low-level page context. */
    private final MuiPageContext context;
    /** Marker applied to the next page block, or {@code null}. */
    private MuiShimmerable nextShimmerable;

    MuiSettingsPage(@NotNull MuiPageContext context) {
        this.context = context;
    }

    /**
     * Enables shimmer for the next page block: a section, a paragraph or a custom addition.
     *
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerNext() {
        return this.shimmerNext(MuiShimmerable.ALWAYS);
    }

    /**
     * Sets the marker applied to the next page block.
     *
     * @param marker the marker
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerNext(@NotNull MuiShimmerable marker) {
        this.nextShimmerable = marker;
        return this;
    }

    /**
     * Enables shimmer for the page title and subtitle. The header is created before the page body
     * runs, so it has its own opt-in instead of using {@link #shimmerNext()}.
     *
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerHeader() {
        return this.shimmerHeader(MuiShimmerable.ALWAYS);
    }

    /**
     * Sets the marker used by the page title and subtitle.
     *
     * @param marker the marker
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerHeader(@NotNull MuiShimmerable marker) {
        this.context.shimmerHeader(marker);
        return this;
    }

    /**
     * Adds a titled section card and invokes {@code body} with its field builder.
     *
     * @param title section heading
     * @param body  section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage section(@NotNull CharSequence title, @NotNull Consumer<MuiSettingsSection> body) {
        int start = this.context.content().getChildCount();
        MuiLayout card = this.context.sectionLayout(title);
        body.accept(new MuiSettingsSection(this.context, card));

        MuiShimmerable marker = this.consume();
        if (marker != null) {
            // sectionLayout adds the heading and the card as siblings; apply to both.
            for (int i = start; i < this.context.content().getChildCount(); i++) {
                MuiShimmerScope.apply(this.context.content().getChildAt(i), marker);
            }
        }
        return this;
    }

    /**
     * Adds a paragraph of text directly to the page column, outside a card.
     *
     * @param text paragraph text
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage text(@NotNull CharSequence text) {
        TextView view = MuiControls.text(this.context.context(), this.context.theme(), text, 12, this.context.theme().onSurfaceVariant());
        MuiShimmerable marker = this.consume();
        if (marker != null) MuiShimmerScope.apply(view, marker);
        this.context.add(view);
        return this;
    }

    /**
     * Escape hatch for arbitrary low-level composition inside the already-prepared page.
     *
     * @param body callback receiving the low-level page context
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage custom(@NotNull Consumer<MuiPageContext> body) {
        int start = this.context.content().getChildCount();
        body.accept(this.context);
        this.applyToNewChildren(start);
        return this;
    }

    /**
     * Adds arbitrary content directly to the page column using the fluent layout wrapper.
     *
     * @param body callback receiving the page content column
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage customLayout(@NotNull Consumer<MuiLayout> body) {
        int start = this.context.contentLayout().getChildCount();
        body.accept(this.context.contentLayout());
        this.applyToNewChildren(start);
        return this;
    }

    private @Nullable MuiShimmerable consume() {
        MuiShimmerable marker = this.nextShimmerable;
        this.nextShimmerable = null;
        return marker;
    }

    private void applyToNewChildren(int start) {
        MuiShimmerable marker = this.consume();
        if (marker == null) return;
        for (int i = start; i < this.context.content().getChildCount(); i++) {
            MuiShimmerScope.apply(this.context.content().getChildAt(i), marker);
        }
    }
}