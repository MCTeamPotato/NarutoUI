package me.kall.narutoui.mui.screen;

import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/**
 * Section-level builder for {@link MuiSettingsScreen}. Every method accepts raw state accessors and
 * appends the fully-styled row; the caller never sees layout params, dividers or value labels.
 */
public final class MuiSettingsSection {
    /** Owning page context. */
    private final MuiPageContext page;
    /** Card body that receives the rows. */
    private final MuiLayout card;
    /** Number of fields added, used for automatic dividers. */
    private int fields;

    MuiSettingsSection(@NotNull MuiPageContext page, @NotNull MuiLayout card) {
        this.page = page;
        this.card = card;
    }

    /**
     * Returns the owning page context.
     *
     * @return the page context
     */
    public @NotNull MuiPageContext page() {
        return this.page;
    }

    /**
     * Returns the card body. Prefer {@link #customLayout(Consumer)} over direct manipulation.
     *
     * @return the card layout
     */
    public @NotNull LinearLayout card() {
        return this.card;
    }

    /**
     * Returns the card body as a fluent layout wrapper.
     *
     * @return the card layout
     */
    public @NotNull MuiLayout cardLayout() {
        return this.card;
    }

    /**
     * Adds a supplier-backed switch row.
     *
     * @param title    row title
     * @param value    live state supplier
     * @param onChange state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, null, value, onChange));
    }

    /**
     * Adds a supplier-backed switch row with a description.
     *
     * @param title       row title
     * @param description optional description
     * @param value       live state supplier
     * @param onChange    state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, @Nullable CharSequence description, @NotNull BooleanSupplier value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, description, value, onChange));
    }

    /**
     * Adds a switch row with a static initial state.
     *
     * @param title    row title
     * @param value    initial checked state
     * @param onChange state callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection toggle(@NotNull CharSequence title, boolean value, @NotNull BooleanConsumer onChange) {
        return this.add(MuiControls.switchRow(this.page.context(), this.page.theme(), title, null, value, onChange));
    }

    /**
     * Adds a supplier-backed integer slider with the default value label.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange) {
        return this.slider(title, min, max, value, onChange, Integer::toString);
    }

    /**
     * Adds a supplier-backed integer slider with a custom formatter.
     *
     * @param title    row title
     * @param min      minimum value (inclusive)
     * @param max      maximum value (inclusive)
     * @param value    live value supplier
     * @param onChange value callback
     * @param format   value formatter
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection slider(@NotNull CharSequence title, int min, int max, @NotNull IntSupplier value, @NotNull IntConsumer onChange, @NotNull IntFunction<CharSequence> format) {
        return this.add(MuiControls.sliderRow(this.page.context(), this.page.theme(), title, format, min, max, value, onChange));
    }

    /**
     * Adds a static value row.
     *
     * @param title row title
     * @param value value text
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull CharSequence value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed integer value row.
     *
     * @param title row title
     * @param value live value supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull IntSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, value));
    }

    /**
     * Adds a supplier-backed integer value row with a custom formatter.
     *
     * @param title  row title
     * @param format value formatter
     * @param value  live value supplier
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection value(@NotNull CharSequence title, @NotNull IntFunction<CharSequence> format, @NotNull IntSupplier value) {
        return this.add(MuiControls.valueRow(this.page.context(), this.page.theme(), title, format, value));
    }

    /**
     * Adds a paragraph inside the card.
     *
     * @param text paragraph text
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection text(@NotNull CharSequence text) {
        return this.add(MuiControls.text(this.page.context(), this.page.theme(), text, 12, this.page.theme().onSurfaceVariant()));
    }

    /**
     * Adds a full-width action button.
     *
     * @param label  button label
     * @param action click callback
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection button(@NotNull CharSequence label, @NotNull Runnable action) {
        Button button = MuiControls.button(this.page.context(), this.page.theme(), label, false, view -> action.run());
        return this.add(button);
    }

    /**
     * Adds an explicit divider. Automatic dividers are normally sufficient.
     *
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection divider() {
        this.card.addView(MuiControls.divider(this.page.context(), this.page.theme()), MuiControls.fill());
        return this;
    }

    /**
     * Adds a custom view and still participates in automatic divider placement.
     *
     * @param view the custom row
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection custom(@NotNull View view) {
        return this.add(view);
    }

    /**
     * Adds arbitrary content as one automatic row using the fluent layout wrapper.
     *
     * @param body callback receiving the row body
     * @return this section, for chaining
     */
    public @NotNull MuiSettingsSection customLayout(@NotNull Consumer<MuiLayout> body) {
        MuiLayout row = MuiLayout.column(this.page.context(), this.page.theme());
        body.accept(row);
        return this.add(row);
    }

    /**
     * Appends a row, inserting a divider before every row after the first.
     *
     * @param row row view
     * @return this section, for chaining
     */
    private @NotNull MuiSettingsSection add(@NotNull View row) {
        if (this.fields++ > 0) this.card.addView(MuiControls.divider(this.page.context(), this.page.theme()), MuiControls.fill());
        this.card.addView(row, MuiControls.fill());
        return this;
    }
}