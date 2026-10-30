package me.kall.narutoui.mui.screen;

import icyllis.modernui.core.Context;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.background.MuiBackground;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.theme.MuiWidgetFactory;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
 * <p>The screen owns a left navigation rail, one scrollable page per {@link Builder#page} call, and an
 * optional footer. Pages are built when the screen is created and retained while it is open.
 * Custom pages can supply their own root view with {@link Builder#customPage}; rail header/footer
 * factories and lifecycle callbacks cover the advanced shell cases.
 * Single-page mode removes the rail entirely and lets the caller fill one page. For multi-page
 * screens, {@link Builder#navigation(MuiNavigation)} places the navigation on the left or top.
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
        /** Optional theme override. */
        private MuiTheme theme;
        /** Per-screen widget factory. */
        private MuiWidgetFactory widgetFactory = MuiWidgetFactory.modern();
        /** Per-screen shimmer profile. */
        private MuiShimmerProfile shimmerProfile = MuiShimmerProfile.defaultProfile();
        /** Maximum content width in DP; zero or negative means full width. */
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
            return navigation(MuiNavigation.TOP);
        }

        /**
         * Places navigation in a vertical rail on the left.
         *
         * @return this builder
         */
        public @NotNull Builder leftNavigation() {
            return navigation(MuiNavigation.LEFT);
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
            return page(navLabel, navLabel, null, body);
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
         */
        public @NotNull Screen create(@Nullable Screen parent) {
            return createScreen(this.buildModel(), UUID.randomUUID().toString(), parent);
        }

        /**
         * Validates this builder and creates the immutable shell model.
         *
         * @return the model
         * @throws IllegalStateException when no page was added
         */
        MuiConfigScreen.Model buildModel() {
            if (this.pages.isEmpty()) {
                throw new IllegalStateException("A MuiConfigScreen needs at least one page");
            }
            if (this.singlePage && this.pages.size() != 1) {
                throw new IllegalStateException("Single-page mode needs exactly one page");
            }

            return new Model(this.title, this.subtitle, this.footer, this.singlePage, this.navigation, this.theme, this.widgetFactory, this.shimmerProfile, this.contentWidth, this.background, List.copyOf(this.pages), this.initialPage, this.railHeader, this.railFooter, this.onCreated, this.onDestroyed);
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
        return MuiModApi.get().createScreen(fragment, null, parent);
    }

    /**
     * Immutable shell configuration passed to {@link MuiConfigFragment}.
     *
     * @param title       rail title
     * @param subtitle    rail subtitle
     * @param footer      rail footer text
     * @param singlePage  {@code true} to omit the navigation
     * @param navigation  navigation placement
     * @param theme       optional theme override
     * @param widgetFactory per-screen widget factory
     * @param shimmerProfile per-screen shimmer profile
     * @param contentWidth max page width in DP, or {@code <= 0}
     * @param background  background provider
     * @param pages       ordered page specs
     * @param initialPage initial page index
     * @param railHeader  optional custom rail header factory
     * @param railFooter  optional custom rail footer factory
     * @param onCreated   optional creation callback
     * @param onDestroyed optional destruction callback
     */
    record Model(String title, String subtitle, String footer, boolean singlePage, MuiNavigation navigation, @Nullable MuiTheme theme, MuiWidgetFactory widgetFactory, MuiShimmerProfile shimmerProfile, int contentWidth, MuiBackground background, List<PageSpec> pages, int initialPage, @Nullable Function<Context, View> railHeader, @Nullable Function<Context, View> railFooter, @Nullable Consumer<MuiConfigFragment> onCreated, @Nullable Consumer<MuiConfigFragment> onDestroyed) {}

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