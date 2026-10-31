package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.background.MuiBackground;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.factory.MuiWidgetFactory;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * High-level declarative entry point for configuration screens.
 * <p>
 * Compared with {@link MuiConfigScreen}, callers never build rows, cards, headers, dividers or
 * padding by hand. A page callback receives a {@link MuiSettingsPage}; each section callback
 * receives a {@link MuiSettingsSection} with ready-made fields.
 *
 * <p>Use {@link Builder#singlePage()} to omit the left navigation rail and render one page as a
 * single form. Use {@link Builder#topNavigation()} to move the navigation above the content.</p>
 *
 * <pre>{@code
 * Screen screen = MuiSettingsScreen.builder("My Mod")
 *         .page("General", page -> page
 *                 .section("Rendering", section -> section
 *                         .toggle("Enable HUD", MyState::enabled, MyState::setEnabled)
 *                         .slider("Scale", 50, 200, MyState::scale, MyState::setScale, value -> value + "%")))
 *         .create(parent);
 * }</pre>
 */
public final class MuiSettingsScreen {
    private MuiSettingsScreen() {}

    /**
     * Starts a high-level settings screen builder.
     *
     * @param title screen/rail title
     * @return a new builder
     */
    public static @NotNull Builder builder(@NotNull String title) {
        return new Builder(title);
    }

    /**
     * Mutable builder for a declarative settings screen. All methods return {@code this} for
     * chaining.
     */
    public static final class Builder {
        /** Screen/rail title. */
        private final String title;
        /** Ordered page definitions (DSL and custom pages share the list). */
        private final List<PageDefinition> pages = new ArrayList<>();
        /** Optional rail subtitle. */
        private String subtitle = "";
        /** Optional rail footer text. */
        private String footer = "";
        /** {@code true} to remove the navigation rail and render one page only. */
        private boolean singlePage;
        /** Navigation placement for multi-page screens. */
        private MuiNavigation navigation = MuiNavigation.LEFT;
        /** Optional theme override. */
        private MuiTheme theme;
        /** Per-screen widget factory. */
        private MuiWidgetFactory widgetFactory = MuiWidgetFactory.modern();
        /** Per-screen shimmer profile. */
        private MuiShimmerProfile shimmerProfile = MuiShimmerProfile.defaultProfile();
        /** Marker applied to every navigation label. */
        private MuiShimmerable navigationShimmerable = MuiShimmerable.NEVER;
        /** Marker applied to rail/top-bar titles, subtitles and footers. */
        private MuiShimmerable titlesShimmerable = MuiShimmerable.NEVER;
        /** Maximum content width in DP. */
        private int contentWidth = MuiPage.DEFAULT_CONTENT_WIDTH;
        /** Background provider. */
        private MuiBackground background = MuiBackground.scrim();
        /** Initial selected page index. */
        private int initialPage;
        /** Optional custom rail header factory. */
        private Function<Context, View> railHeader;
        /** Optional custom rail footer factory. */
        private Function<Context, View> railFooter;
        /** Optional creation callback. */
        private Consumer<MuiConfigFragment> onCreated;
        /** Optional destruction callback. */
        private Consumer<MuiConfigFragment> onDestroyed;

        private Builder(@NotNull String title) {
            this.title = Objects.requireNonNull(title, "title");
        }

        /**
         * Sets the rail subtitle.
         *
         * @param subtitle subtitle text
         * @return this builder
         */
        public @NotNull Builder subtitle(@NotNull String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * Enables single-page mode: no left navigation rail, exactly one page. This is useful for
         * small settings panels that should feel like a single form.
         *
         * @return this builder
         */
        public @NotNull Builder singlePage() {
            this.singlePage = true;
            return this;
        }

        /**
         * Sets where the navigation is rendered. Single-page mode ignores this setting.
         *
         * @param navigation navigation placement
         * @return this builder
         */
        public @NotNull Builder navigation(@NotNull MuiNavigation navigation) {
            this.navigation = Objects.requireNonNull(navigation, "navigation");
            return this;
        }

        /**
         * Places navigation in a horizontal bar above the content.
         *
         * @return this builder
         */
        public @NotNull Builder topNavigation() {
            return this.navigation(MuiNavigation.TOP);
        }

        /**
         * Places navigation in a vertical rail on the left.
         *
         * @return this builder
         */
        public @NotNull Builder leftNavigation() {
            return this.navigation(MuiNavigation.LEFT);
        }

        /**
         * Sets the rail footer text.
         *
         * @param footer footer text
         * @return this builder
         */
        public @NotNull Builder footer(@NotNull String footer) {
            this.footer = footer;
            return this;
        }

        /**
         * Overrides the resolved Modern UI theme.
         *
         * @param theme theme override, or {@code null} to resolve automatically
         * @return this builder
         */
        public @NotNull Builder theme(@Nullable MuiTheme theme) {
            this.theme = theme;
            return this;
        }

        /**
         * Sets the per-screen widget factory.
         *
         * @param factory the factory
         * @return this builder
         */
        public @NotNull Builder widgetFactory(@NotNull MuiWidgetFactory factory) {
            this.widgetFactory = Objects.requireNonNull(factory, "factory");
            return this;
        }

        /**
         * Sets the per-screen shimmer profile.
         *
         * @param profile the profile
         * @return this builder
         */
        public @NotNull Builder shimmerProfile(@NotNull MuiShimmerProfile profile) {
            this.shimmerProfile = Objects.requireNonNull(profile, "profile");
            return this;
        }

        /**
         * Enables shimmer for every navigation label on this screen.
         *
         * @return this builder
         */
        public @NotNull Builder shimmerNavigation() {
            return this.shimmerNavigation(MuiShimmerable.ALWAYS);
        }

        /**
         * Sets the marker used by every navigation label on this screen.
         *
         * @param marker the marker
         * @return this builder
         */
        public @NotNull Builder shimmerNavigation(@NotNull MuiShimmerable marker) {
            this.navigationShimmerable = Objects.requireNonNull(marker, "marker");
            return this;
        }

        /**
         * Enables shimmer for the rail/top-bar title, subtitle and footer text.
         *
         * @return this builder
         */
        public @NotNull Builder shimmerTitles() {
            return this.shimmerTitles(MuiShimmerable.ALWAYS);
        }

        /**
         * Sets the marker used by the rail/top-bar title, subtitle and footer text.
         *
         * @param marker the marker
         * @return this builder
         */
        public @NotNull Builder shimmerTitles(@NotNull MuiShimmerable marker) {
            this.titlesShimmerable = Objects.requireNonNull(marker, "marker");
            return this;
        }

        /**
         * Sets the maximum page content width.
         *
         * @param width width in DP, or {@code <= 0} for full width
         * @return this builder
         */
        public @NotNull Builder contentWidth(int width) {
            this.contentWidth = width;
            return this;
        }

        /**
         * Sets the background provider.
         *
         * @param background background provider
         * @return this builder
         */
        public @NotNull Builder background(@NotNull MuiBackground background) {
            this.background = Objects.requireNonNull(background, "background");
            return this;
        }

        /**
         * Selects the page shown when the screen opens.
         *
         * @param index zero-based page index
         * @return this builder
         */
        public @NotNull Builder initialPage(int index) {
            this.initialPage = index;
            return this;
        }

        /**
         * Replaces the default rail title/subtitle block.
         *
         * @param factory header view factory, or {@code null} for the default
         * @return this builder
         */
        public @NotNull Builder railHeader(@Nullable Function<Context, View> factory) {
            this.railHeader = factory;
            return this;
        }

        /**
         * Replaces the default rail footer.
         *
         * @param factory footer view factory, or {@code null} for the default
         * @return this builder
         */
        public @NotNull Builder railFooter(@Nullable Function<Context, View> factory) {
            this.railFooter = factory;
            return this;
        }

        /**
         * Receives the shell fragment after its view is created. The fragment implements
         * {@link MuiScreenHost}.
         *
         * @param callback creation callback, or {@code null}
         * @return this builder
         */
        public @NotNull Builder onCreated(@Nullable Consumer<MuiConfigFragment> callback) {
            this.onCreated = callback;
            return this;
        }

        /**
         * Receives the shell fragment before its view is destroyed.
         *
         * @param callback destruction callback, or {@code null}
         * @return this builder
         */
        public @NotNull Builder onDestroyed(@Nullable Consumer<MuiConfigFragment> callback) {
            this.onDestroyed = callback;
            return this;
        }

        /**
         * Adds a page whose title is also used as the navigation label.
         *
         * @param navLabel navigation label and page title
         * @param body     page builder callback
         * @return this builder
         */
        public @NotNull Builder page(@NotNull String navLabel, @NotNull Consumer<MuiSettingsPage> body) {
            return this.page(navLabel, navLabel, null, body);
        }

        /**
         * Adds a DSL page.
         *
         * @param navLabel navigation label
         * @param title    page title
         * @param subtitle optional page subtitle
         * @param body     page builder callback
         * @return this builder
         */
        public @NotNull Builder page(@NotNull String navLabel, @NotNull String title, @Nullable String subtitle, @NotNull Consumer<MuiSettingsPage> body) {
            this.pages.add(PageDefinition.form(navLabel, title, subtitle, body));
            return this;
        }

        /**
         * Adds a page whose complete root view is supplied by the caller. Useful as a bridge for
         * an existing page implementation while the rest of the screen uses the high-level DSL.
         *
         * @param navLabel navigation label
         * @param factory  root-view factory
         * @return this builder
         */
        public @NotNull Builder customPage(@NotNull String navLabel, @NotNull Function<Context, View> factory) {
            this.pages.add(PageDefinition.custom(navLabel, factory));
            return this;
        }

        /**
         * Builds the screen through {@link MuiConfigScreen}.
         *
         * @param parent previous screen, or {@code null}
         * @return the created screen
         * @throws IllegalStateException when no page was added
         */
        public @NotNull Screen create(@Nullable Screen parent) {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("A settings screen needs at least one page");
            }

            MuiConfigScreen.Builder delegate = MuiConfigScreen.builder(this.title);
            this.applyTo(delegate);
            return delegate.create(parent);
        }

        @SuppressWarnings("DataFlowIssue")
        private void applyTo(@NotNull MuiConfigScreen.Builder delegate) {
            delegate.subtitle(this.subtitle)
                    .footer(this.footer)
                    .widgetFactory(this.widgetFactory)
                    .shimmerProfile(this.shimmerProfile)
                    .shimmerNavigation(this.navigationShimmerable)
                    .shimmerTitles(this.titlesShimmerable)
                    .contentWidth(this.contentWidth)
                    .background(this.background)
                    .initialPage(this.initialPage)
                    .navigation(this.navigation)
                    .railHeader(this.railHeader)
                    .railFooter(this.railFooter)
                    .onCreated(this.onCreated)
                    .onDestroyed(this.onDestroyed);

            if (this.singlePage) delegate.singlePage();
            if (this.theme != null) delegate.theme(this.theme);

            for (PageDefinition page : this.pages) {
                if (page.viewFactory() != null) {
                    delegate.customPage(page.navLabel(), page.viewFactory());
                } else {
                    delegate.page(page.navLabel(), page.title(), page.subtitle(), context -> page.body().accept(new MuiSettingsPage(context)));
                }
            }
        }
    }

    /**
     * Registers a stable high-level settings recipe.
     *
     * @param id    stable screen id
     * @param title rail title
     * @param body  builder recipe
     */
    public static void register(@NotNull String id, @NotNull String title, @NotNull Consumer<Builder> body) {
        MuiConfigScreen.register(id, title, delegate -> {
            Builder builder = builder(title);
            body.accept(builder);
            builder.applyTo(delegate);
        });
    }

    /**
     * Creates a screen from a stable registered high-level recipe.
     *
     * @param id     stable screen id
     * @param parent previous screen, or {@code null}
     * @return the created screen
     */
    public static @NotNull Screen createRegistered(@NotNull String id, @Nullable Screen parent) {
        return MuiConfigScreen.createRegistered(id, parent);
    }

    /**
     * Page entry shared by DSL and custom pages.
     *
     * @param navLabel    navigation label
     * @param title       DSL page title, or {@code null} for a custom page
     * @param subtitle    DSL page subtitle, or {@code null}
     * @param body        DSL body, or {@code null} for a custom page
     * @param viewFactory custom root factory, or {@code null} for a DSL page
     */
    private record PageDefinition(String navLabel, @Nullable String title, @Nullable String subtitle, @Nullable Consumer<MuiSettingsPage> body, @Nullable Function<Context, View> viewFactory) {
        /**
         * Creates a DSL page definition.
         *
         * @param nav       navigation label
         * @param title     page title
         * @param subtitle  optional subtitle
         * @param body      page builder
         * @return the definition
         */
        @Contract("_, _, _, _ -> new")
        static @NotNull PageDefinition form(String nav, String title, @Nullable String subtitle, Consumer<MuiSettingsPage> body) {
            return new PageDefinition(nav, title, subtitle, body, null);
        }

        /**
         * Creates a custom-page definition.
         *
         * @param nav     navigation label
         * @param factory root-view factory
         * @return the definition
         */
        @Contract("_, _ -> new")
        static @NotNull PageDefinition custom(String nav, Function<Context, View> factory) {
            return new PageDefinition(nav, null, null, null, factory);
        }
    }
}