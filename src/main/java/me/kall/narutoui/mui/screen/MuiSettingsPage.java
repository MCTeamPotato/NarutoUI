package me.kall.narutoui.mui.screen;

import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.MuiCollapsibleCard;
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
    /** Marker applied to every page block and the header by default, or {@code null}. */
    private MuiShimmerable allShimmerable;

    MuiSettingsPage(@NotNull MuiPageContext context) {
        this.context = context;
    }

    /**
     * Returns the underlying page context, which also exposes the owning shell and the shimmer
     * profile of this page.
     *
     * @return the page context
     */
    public @NotNull MuiPageContext context() {
        return this.context;
    }

    /**
     * Returns the theme used by this page.
     *
     * @return the active theme
     */
    public @NotNull MuiTheme theme() {
        return this.context.theme();
    }

    /**
     * Enables shimmer for the whole page: the header, already-added content and every later block.
     * Use {@link #shimmerNext()} before a block to override the default for that block.
     *
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerAll() {
        return this.shimmerAll(MuiShimmerable.ALWAYS);
    }

    /**
     * Sets the default marker for the whole page.
     *
     * @param marker the marker
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage shimmerAll(@NotNull MuiShimmerable marker) {
        this.allShimmerable = marker;
        this.nextShimmerable = null;
        this.context.shimmerHeader(marker);
        for (int i = 0; i < this.context.content().getChildCount(); i++) {
            MuiShimmerScope.apply(this.context.content().getChildAt(i), marker);
        }
        return this;
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
     * Adds an untitled section card and invokes {@code body} with its field builder. Use this for
     * source models whose groups have no display name.
     *
     * @param body section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage section(@NotNull Consumer<MuiSettingsSection> body) {
        int start = this.context.content().getChildCount();
        MuiLayout card = this.context.untitledSectionLayout();
        body.accept(new MuiSettingsSection(this.context, card));
        this.applyToNewChildren(start);
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
        this.applyToNewChildren(start);
        return this;
    }

    /**
     * Adds a collapsible card that starts expanded, without a summary.
     *
     * @param title card title
     * @param body  section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage collapsibleSection(@NotNull CharSequence title, @NotNull Consumer<MuiSettingsSection> body) {
        return this.collapsibleSection(title, null, true, body);
    }

    /**
     * Adds a collapsible card that starts expanded.
     *
     * @param title   card title
     * @param summary optional summary; empty hides it
     * @param body    section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage collapsibleSection(@NotNull CharSequence title, @Nullable CharSequence summary, @NotNull Consumer<MuiSettingsSection> body) {
        return this.collapsibleSection(title, summary, true, body);
    }

    /**
     * Adds a collapsible card and exposes its body as a regular field section. Useful for long
     * pages with independently expandable categories.
     *
     * @param title    card title
     * @param summary  optional summary; empty hides it
     * @param expanded whether the card starts expanded
     * @param body     section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage collapsibleSection(@NotNull CharSequence title, @Nullable CharSequence summary, boolean expanded, @NotNull Consumer<MuiSettingsSection> body) {
        int start = this.context.content().getChildCount();

        MuiCollapsibleCard card = MuiCollapsibleCard.card(
                this.context.context(), this.context.theme(), title, summary, expanded);
        icyllis.modernui.widget.LinearLayout.LayoutParams params = MuiControls.fill();
        params.topMargin = MuiControls.dp(this.context.context(), 12);
        this.context.content().addView(card, params);

        body.accept(new MuiSettingsSection(this.context, card.body()));

        this.applyToNewChildren(start);
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
        MuiShimmerable marker = this.resolve();
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

    private @Nullable MuiShimmerable resolve() {
        MuiShimmerable marker = this.consume();
        return marker == null ? this.allShimmerable : marker;
    }

    /**
     * Applies the page's shimmer marker to every view added after {@code start}.
     * <p>
     * A section may add more than one sibling (the titled variant adds a heading next to the card),
     * so the range is walked instead of a single view being marked.
     *
     * @param start child index the block started at
     */
    private void applyToNewChildren(int start) {
        MuiShimmerable marker = this.resolve();
        if (marker == null) return;
        for (int i = start; i < this.context.content().getChildCount(); i++) {
            MuiShimmerScope.apply(this.context.content().getChildAt(i), marker);
        }
    }
}