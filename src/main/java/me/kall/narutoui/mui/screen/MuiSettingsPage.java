package me.kall.narutoui.mui.screen;

import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiLayout;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Page-level builder for {@link MuiSettingsScreen}. The API has already added the page header,
 * scroll container, content padding and section cards.
 */
public final class MuiSettingsPage {
    /** Underlying low-level page context. */
    private final MuiPageContext context;

    MuiSettingsPage(@NotNull MuiPageContext context) {
        this.context = context;
    }

    /**
     * Adds a titled section card and invokes {@code body} with its field builder.
     *
     * @param title section heading
     * @param body  section field callback
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage section(@NotNull CharSequence title, @NotNull Consumer<MuiSettingsSection> body) {
        MuiLayout card = this.context.sectionLayout(title);
        body.accept(new MuiSettingsSection(this.context, card));
        return this;
    }

    /**
     * Adds a paragraph of text directly to the page column, outside a card.
     *
     * @param text paragraph text
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage text(@NotNull CharSequence text) {
        this.context.add(MuiControls.text(this.context.context(), this.context.theme(), text, 12, this.context.theme().onSurfaceVariant()));
        return this;
    }

    /**
     * Escape hatch for arbitrary low-level composition inside the already-prepared page.
     *
     * @param body callback receiving the low-level page context
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage custom(@NotNull Consumer<MuiPageContext> body) {
        body.accept(this.context);
        return this;
    }

    /**
     * Adds arbitrary content directly to the page column using the fluent layout wrapper.
     *
     * @param body callback receiving the page content column
     * @return this page, for chaining
     */
    public @NotNull MuiSettingsPage customLayout(@NotNull Consumer<MuiLayout> body) {
        body.accept(this.context.contentLayout());
        return this;
    }
}