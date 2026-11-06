package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.util.DataSet;
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
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry point for building a Modern UI configuration screen.
 *
 * <pre>{@code
 * Screen screen = MuiConfigScreen.builder("My Mod")
 *         .subtitle("Settings for the example mod")
 *         .page("General", "General", "Everyday options", ui ->
 *                 ui.sectionLayout("Rendering")
 *                         .add(ui.switchRow("Enable overlay", null, true, value -> {})))
 *         .create(parent);
 * }</pre>
 *
 * <p>The screen owns a left navigation rail, one scrollable page per {@link Builder#page} call, an
 * optional footer and an optional fixed action bar. Pages are built lazily when first shown and
 * retained while the screen is open. Custom pages can supply their own root view with
 * {@link Builder#customPage}; rail header/footer factories, the action bar and lifecycle callbacks
 * cover the advanced shell cases.
 * Single-page mode removes the rail entirely and lets the caller fill one page. For multi-page
 * screens, {@link Builder#navigation(MuiNavigation)} places the navigation on the left or top.
 * The rail scrolls when it contains more items than fit, and {@link Builder#onCloseRequested}
 * can veto a user-initiated close (for example while there are unsaved changes). Use
 * {@link Builder#actionBar} for a fixed Save/Cancel row and
 * {@link MuiScreenHost#requestClose()} / {@link MuiScreenHost#close()} to drive the close flow.
 * All widgets use the supplied {@link MuiTheme}, which defaults to
 * {@link MuiTheme#modern(icyllis.modernui.core.Context)}.
 */
public final class MuiConfigScreen {
    private MuiConfigScreen() {}

    /**
     * Starts a low-level configuration-screen builder.
     *
     * @param title screen/rail title
     * @return a new builder
     */
    public static @NotNull Builder builder(@NotNull String title) {
        return new Builder(title);
    }

    /**
     * Mutable low-level builder. All methods return {@code this} for chaining.
     */
    public static final class Builder {
        /** Screen/rail title. */
        private final String title;
        /** Ordered page specs. */
        private final List<PageSpec> pages = new ArrayList<>();
        /** Optional rail subtitle. */
        private String subtitle = "";
        /** Optional rail footer text. */
        private String footer = "";
        /** {@code true} to remove the navigation rail and render one page only. */
        private boolean singlePage;
        /** Navigation placement for multi-page screens. */
        private MuiNavigation navigation = MuiNavigation.LEFT;
        /** Page-switch transition. */
        private MuiPageTransition pageTransition = MuiPageTransition.FADE;
        /** Page-switch transition duration in milliseconds; negative uses the style default. */
        private long pageTransitionDuration = -1L;
        /** Optional theme override. */
        private MuiTheme theme;
        /** Optional context-aware theme provider; takes precedence over {@link #theme}. */
        private Function<Context, MuiTheme> themeProvider;
        /** Per-screen widget factory. */
        private MuiWidgetFactory widgetFactory = MuiWidgetFactory.modern();
        /** Per-screen shimmer profile. */
        private MuiShimmerProfile shimmerProfile = MuiShimmerProfile.defaultProfile();
        /** Marker applied to every navigation label. */
        private MuiShimmerable navigationShimmerable = MuiShimmerable.NEVER;
        /** Marker applied to rail/top-bar titles, subtitles and footers. */
        private MuiShimmerable titlesShimmerable = MuiShimmerable.NEVER;
        /** Maximum content width in DP; zero or negative means full width. */
        private int contentWidth = MuiPage.DEFAULT_CONTENT_WIDTH;
        /** Background provider. */
        private MuiBackground background = MuiBackground.scrim();
        /** Initial selected page index. */
        private int initialPage;
        /** Initial page navigation label, or {@code null} when {@link #initialPage} is used. */
        private String initialPageLabel;
        /** Optional custom rail header factory. */
        private Function<Context, View> railHeader;
        /** Optional custom rail footer factory. */
        private Function<Context, View> railFooter;
        /** Optional fixed bottom action bar factory. */
        private Function<Context, View> actionBar;
        /** Optional creation callback. */
        private Consumer<MuiConfigFragment> onCreated;
        /** Optional destruction callback. */
        private Consumer<MuiConfigFragment> onDestroyed;
        /** Optional guard consulted before a user-initiated close. */
        private BooleanSupplier closeGuard;

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
         * Enables single-page mode: the left navigation rail is omitted and exactly one page is
         * rendered directly in the content area. Add one page with {@link #page} or
         * {@link #customPage}.
         *
         * @return this builder
         */
        public @NotNull Builder singlePage() {
            this.singlePage = true;
            return this;
        }

        /**
         * Sets where the navigation is rendered. {@link MuiNavigation#LEFT} is a vertical rail;
         * {@link MuiNavigation#TOP} is a horizontal bar above the content. Single-page mode ignores
         * this setting.
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
         * Overrides the resolved Modern UI theme. Passing {@code null} restores the default.
         *
         * @param theme theme override, or {@code null}
         * @return this builder
         */
        public @NotNull Builder theme(@Nullable MuiTheme theme) {
            this.theme = theme;
            this.themeProvider = null;
            return this;
        }

        /**
         * Uses a context-aware theme provider. Unlike {@link #theme(MuiTheme)}, the provider is
         * evaluated after the Modern UI context exists and again whenever the shell is rebuilt, so
         * its accent can follow a live setting.
         *
         * @param provider theme provider, or {@code null} to clear it
         * @return this builder
         */
        public @NotNull Builder themeProvider(@Nullable Function<Context, MuiTheme> provider) {
            this.themeProvider = provider;
            if (provider != null) {
                this.theme = null;
            }
            return this;
        }

        /**
         * Sets the widget factory used to build this screen's controls. This is scoped to the
         * screen, so different mods can use different visual styles at the same time.
         *
         * @param factory the factory
         * @return this builder
         */
        public @NotNull Builder widgetFactory(@NotNull MuiWidgetFactory factory) {
            this.widgetFactory = Objects.requireNonNull(factory, "factory");
            return this;
        }

        /**
         * Sets the shimmer profile used by controls on this screen.
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
            this.initialPageLabel = null;
            return this;
        }

        /**
         * Selects the page shown when the screen opens by its navigation label. The label is
         * resolved when the screen is built, so pages may be added after this call. An unknown
         * label fails with {@link IllegalArgumentException}.
         *
         * @param navLabel navigation label of the initial page
         * @return this builder
         */
        public @NotNull Builder initialPage(@NotNull String navLabel) {
            this.initialPageLabel = Objects.requireNonNull(navLabel, "navLabel");
            return this;
        }

        /**
         * Sets the transition played when the player switches pages. The default is
         * {@link MuiPageTransition#FADE}; use {@link #pageTransitionDuration(long)} with
         * {@code 0} to keep the instant page switch. The first page shown when the screen opens,
         * page reloads and shell rebuilds after a resize never animate, so this only affects
         * navigation between pages.
         *
         * @param transition the transition
         * @return this builder
         */
        public @NotNull Builder pageTransition(@NotNull MuiPageTransition transition) {
            this.pageTransition = Objects.requireNonNull(transition, "transition");
            return this;
        }

        /**
         * Overrides the transition duration. Values are clamped to {@code 0..2000} milliseconds;
         * {@code 0} makes the switch instant even when a transition is selected. By default the
         * duration of the selected {@link MuiPageTransition} is used.
         *
         * @param millis duration in milliseconds
         * @return this builder
         */
        public @NotNull Builder pageTransitionDuration(long millis) {
            this.pageTransitionDuration = Math.max(0L, Math.min(2000L, millis));
            return this;
        }

        /**
         * Replaces the default title/subtitle block at the top of the navigation rail.
         *
         * @param factory header factory, or {@code null} for the default
         * @return this builder
         */
        public @NotNull Builder railHeader(@Nullable Function<Context, View> factory) {
            this.railHeader = factory;
            return this;
        }

        /**
         * Replaces the default footer text at the bottom of the navigation rail.
         *
         * @param factory footer factory, or {@code null} for the default
         * @return this builder
         */
        public @NotNull Builder railFooter(@Nullable Function<Context, View> factory) {
            this.railFooter = factory;
            return this;
        }

        /**
         * Adds a fixed action bar below the page content and above the window edge. The bar is
         * hosted on the active theme's surface, so callers normally return a horizontal row of
         * {@link me.kall.narutoui.mui.layout.MuiControls#button} controls. Returning {@code null}
         * omits the bar for this screen.
         * <p>
         * The bar does not scroll with the page. Use {@link MuiScreenHost#requestClose()} for a
         * normal back close (which still consults {@link #onCloseRequested}) and
         * {@link MuiScreenHost#close()} once a confirmation flow has completed.
         *
         * @param factory action-bar factory, or {@code null} for no action bar
         * @return this builder
         */
        public @NotNull Builder actionBar(@Nullable Function<Context, View> factory) {
            this.actionBar = factory;
            return this;
        }

        /**
         * Registers a guard that runs when the player presses back/Escape. Returning {@code false}
         * keeps the screen open, which lets the caller show an unsaved-changes overlay from the
         * guard. Returning {@code true} allows the normal close to continue.
         * <p>
         * The guard runs on the UI thread and may safely call {@link MuiScreenHost#showOverlay}.
         * Programmatic closes through {@link MuiScreenHost#close()} bypass the guard; closes through
         * {@link MuiScreenHost#requestClose()} consult it.
         *
         * @param guard close guard, or {@code null} to always allow closing
         * @return this builder
         */
        public @NotNull Builder onCloseRequested(@Nullable BooleanSupplier guard) {
            this.closeGuard = guard;
            return this;
        }

        /**
         * Called after the shell has been created. The argument implements {@link MuiScreenHost}, so
         * it can be handed to background controllers or used to show overlays and reload a page.
         *
         * @param callback creation callback, or {@code null}
         * @return this builder
         */
        public @NotNull Builder onCreated(@Nullable Consumer<MuiConfigFragment> callback) {
            this.onCreated = callback;
            return this;
        }

        /**
         * Called before the shell view is destroyed.
         *
         * @param callback destruction callback, or {@code null}
         * @return this builder
         */
        public @NotNull Builder onDestroyed(@Nullable Consumer<MuiConfigFragment> callback) {
            this.onDestroyed = callback;
            return this;
        }

        /**
         * Adds a page whose title is also the navigation label.
         *
         * @param navLabel navigation label and page title
         * @param body     page body callback
         * @return this builder
         */
        public @NotNull Builder page(@NotNull String navLabel, @NotNull Consumer<MuiPageContext> body) {
            return this.page(navLabel, navLabel, null, body);
        }

        /**
         * Adds a low-level page with a distinct title and no subtitle.
         *
         * @param navLabel navigation label
         * @param title    page title
         * @param body     page body callback
         * @return this builder
         */
        public @NotNull Builder page(@NotNull String navLabel, @NotNull String title, @NotNull Consumer<MuiPageContext> body) {
            return this.page(navLabel, title, null, body);
        }

        /**
         * Adds a low-level page.
         *
         * @param navLabel navigation label
         * @param title    page title
         * @param subtitle optional subtitle
         * @param body     page body callback
         * @return this builder
         */
        public @NotNull Builder page(@NotNull String navLabel, @NotNull String title, @Nullable String subtitle, @NotNull Consumer<MuiPageContext> body) {
            this.pages.add(PageSpec.body(navLabel, title, subtitle, body));
            return this;
        }

        /**
         * Adds a page whose root view is supplied by the caller. The root should be a complete
         * scrollable page; the shell does not wrap it in another ScrollView.
         *
         * @param navLabel navigation label
         * @param factory  root-view factory
         * @return this builder
         */
        public @NotNull Builder customPage(@NotNull String navLabel, @NotNull Function<Context, View> factory) {
            this.pages.add(PageSpec.custom(navLabel, factory));
            return this;
        }

        /**
         * Builds the screen through {@link MuiModApi}.
         *
         * @param parent previous screen, or {@code null}
         * @return the created screen
         * @throws IllegalStateException when no page was added
         * @throws IllegalArgumentException when the initial page label does not match a page
         */
        public @NotNull Screen create(@Nullable Screen parent) {
            return createScreen(this.buildModel(), UUID.randomUUID().toString(), parent);
        }

        /**
         * Validates this builder and creates the immutable shell model.
         *
         * @return the model
         * @throws IllegalStateException when no page was added
         * @throws IllegalArgumentException when the initial page label does not match a page
         */
        @Contract(" -> new")
        MuiConfigScreen.@NotNull Model buildModel() {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("A MuiConfigScreen needs at least one page");
            }
            if (this.singlePage && this.pages.size() != 1) {
                throw new IllegalStateException("Single-page mode needs exactly one page");
            }

            int initialPageIndex = this.resolveInitialPage();
            return new Model(this.title, this.subtitle, this.footer, this.singlePage, this.navigation, this.pageTransition, this.resolveTransitionDuration(), this.theme, this.widgetFactory, this.shimmerProfile, this.navigationShimmerable, this.titlesShimmerable, this.contentWidth, this.background, List.copyOf(this.pages), initialPageIndex, this.railHeader, this.railFooter, this.actionBar, this.onCreated, this.onDestroyed, this.closeGuard, this.themeProvider);
        }

        /**
         * Resolves the page-transition duration, falling back to the style default.
         *
         * @return duration in milliseconds
         */
        private long resolveTransitionDuration() {
            return this.pageTransitionDuration >= 0L
                    ? this.pageTransitionDuration
                    : this.pageTransition.defaultDurationMillis();
        }

        /**
         * Resolves the initial page index from either the numeric index or the navigation label.
         *
         * @return the zero-based initial page index
         */
        private int resolveInitialPage() {
            if (this.initialPageLabel == null) return this.initialPage;

            for (int i = 0; i < this.pages.size(); i++) {
                if (this.pages.get(i).navLabel().equals(this.initialPageLabel)) return i;
            }
            throw new IllegalArgumentException("No page with navigation label: " + this.initialPageLabel);
        }
    }

    /**
     * Registers a stable screen recipe. Registering the same id replaces the previous recipe.
     *
     * @param id    stable screen id, for example {@code mymod:config}
     * @param title rail title
     * @param body  builder recipe
     */
    public static void register(@NotNull String id, @NotNull String title, @NotNull Consumer<Builder> body) {
        MuiConfigRegistry.putRecipe(id, new MuiConfigRegistry.Recipe(title, body));
    }

    /**
     * Creates a screen from a stable registered recipe.
     *
     * @param id     stable screen id
     * @param parent previous screen, or {@code null}
     * @return the created screen
     * @throws IllegalStateException when the id is not registered
     */
    public static @NotNull Screen createRegistered(@NotNull String id, @Nullable Screen parent) {
        MuiConfigRegistry.Recipe recipe = MuiConfigRegistry.getRecipe(id);
        if (recipe == null) {
            throw new IllegalStateException("No MuiConfigScreen recipe registered for " + id);
        }

        Builder builder = builder(recipe.title());
        recipe.body().accept(builder);
        return createScreen(builder.buildModel(), id, parent);
    }

    /**
     * Builds a model for an already registered stable id.
     *
     * @param id stable screen id
     * @return the model, or {@code null} when no recipe exists
     */
    static @Nullable Model buildModelFor(@NotNull String id) {
        Model model = MuiConfigRegistry.getModel(id);
        return model != null ? model : MuiConfigRegistry.buildModel(id);
    }

    private static @NotNull Screen createScreen(@NotNull Model model, @NotNull String key, @Nullable Screen parent) {
        MuiConfigRegistry.putModel(key, model);

        DataSet args = new DataSet();
        args.putString(MuiConfigFragment.ARG_MODEL, key);

        MuiConfigFragment fragment = new MuiConfigFragment();
        fragment.setArguments(args);
        var screen = MuiModApi.get().createScreen(fragment, fragment, parent, model.title());
        fragment.attachScreen(screen, parent);
        return screen;
    }

    /**
     * Immutable shell configuration passed to {@link MuiConfigFragment}.
     *
     * @param title       rail title
     * @param subtitle    rail subtitle
     * @param footer      rail footer text
     * @param singlePage  {@code true} to omit the navigation
     * @param navigation  navigation placement
     * @param pageTransition page-switch transition
     * @param pageTransitionDuration page-switch transition duration in milliseconds
     * @param theme       optional theme override
     * @param widgetFactory per-screen widget factory
     * @param shimmerProfile per-screen shimmer profile
     * @param navigationShimmerable marker for navigation labels
     * @param titlesShimmerable marker for rail/top-bar titles, subtitles and footers
     * @param contentWidth max page width in DP, or {@code <= 0}
     * @param background  background provider
     * @param pages       ordered page specs
     * @param initialPage initial page index
     * @param railHeader  optional custom rail header factory
     * @param railFooter  optional custom rail footer factory
     * @param actionBar   optional fixed bottom action-bar factory
     * @param onCreated   optional creation callback
     * @param onDestroyed optional destruction callback
     * @param closeGuard  optional guard consulted before a user-initiated close
     * @param themeProvider optional context-aware theme provider
     */
    record Model(String title, String subtitle, String footer, boolean singlePage, MuiNavigation navigation, MuiPageTransition pageTransition, long pageTransitionDuration, @Nullable MuiTheme theme, MuiWidgetFactory widgetFactory, MuiShimmerProfile shimmerProfile, MuiShimmerable navigationShimmerable, MuiShimmerable titlesShimmerable, int contentWidth, MuiBackground background, List<PageSpec> pages, int initialPage, @Nullable Function<Context, View> railHeader, @Nullable Function<Context, View> railFooter, @Nullable Function<Context, View> actionBar, @Nullable Consumer<MuiConfigFragment> onCreated, @Nullable Consumer<MuiConfigFragment> onDestroyed, @Nullable BooleanSupplier closeGuard, @Nullable Function<Context, MuiTheme> themeProvider) {}

    /**
     * One page entry, either a low-level body callback or a custom root-view factory.
     *
     * @param navLabel    navigation label
     * @param title       page title, or {@code null} for a custom page
     * @param subtitle    optional page subtitle
     * @param body        body callback, or {@code null} for a custom page
     * @param viewFactory custom root factory, or {@code null} for a body page
     */
    record PageSpec(String navLabel, @Nullable String title, @Nullable String subtitle, @Nullable Consumer<MuiPageContext> body, @Nullable Function<Context, View> viewFactory) {
        /**
         * Creates a body-page spec.
         *
         * @param nav      navigation label
         * @param title    page title
         * @param subtitle optional subtitle
         * @param body     body callback
         * @return the spec
         */
        @Contract("_, _, _, _ -> new")
        static @NotNull PageSpec body(String nav, String title, @Nullable String subtitle, Consumer<MuiPageContext> body) {
            return new PageSpec(nav, title, subtitle, body, null);
        }

        /**
         * Creates a custom-page spec.
         *
         * @param nav     navigation label
         * @param factory root-view factory
         * @return the spec
         */
        @Contract("_, _ -> new")
        static @NotNull PageSpec custom(String nav, Function<Context, View> factory) {
            return new PageSpec(nav, null, null, null, factory);
        }
    }
}