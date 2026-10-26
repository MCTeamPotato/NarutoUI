package me.kall.narutoui.mui;

import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Host fragment for a {@link MuiConfigScreen}. The shell is deliberately small: a left navigation
 * rail, a content frame, and a set of retained scrollable pages.
 * <p>
 * The fragment has a public no-arg constructor and resolves its model from
 * {@link MuiConfigFragment#ARG_MODEL}. This makes it safe for a FragmentManager to recreate.
 */
public final class MuiConfigFragment extends Fragment {
    /** Fragment argument containing the registry key. */
    public static final String ARG_MODEL = "mui:model";
    private static final String ARG_SELECTED_PAGE = "mui:selected_page";
    private static final String ARG_SCROLL = "mui:scroll";

    /** Rail width in DP. */
    private static final int RAIL_WIDTH = 120;

    /** Shell model, resolved in {@link #onCreate(DataSet)}. */
    private @Nullable MuiConfigScreen.Model model;
    /** Registry key used to remove the process-local model on final destroy. */
    private @Nullable String modelKey;
    /** {@code true} when neither a model nor a stable recipe could be resolved. */
    private boolean missingModel;
    /** Navigation item views, parallel to {@link #pages}. */
    private final List<TextView> navItems = new ArrayList<>();
    /** Built page roots, retained across tab switches. */
    private final List<View> pages = new ArrayList<>();

    /** Shell root that also hosts overlays. */
    private @Nullable FrameLayout root;
    /** Content frame that swaps page roots. */
    private @Nullable FrameLayout content;
    /** Resolved or overridden theme. */
    private MuiTheme theme;
    /** Currently selected page index, or {@code -1}. */
    private int selected = -1;

    /**
     * No-arg constructor required by {@code FragmentFactory} and FragmentManager recreation.
     */
    public MuiConfigFragment() {}

    /**
     * Resolves the shell model from arguments or a registered stable recipe.
     *
     * @param savedInstanceState optional fragment state
     */
    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataSet arguments = getArguments();
        this.modelKey = arguments == null ? null : arguments.getString(ARG_MODEL);
        if (this.modelKey != null && !this.modelKey.isEmpty()) {
            this.model = MuiConfigScreen.buildModelFor(this.modelKey);
        }
        this.missingModel = this.model == null;
    }

    /**
     * Builds the rail, content frame, pages and initial page.
     *
     * @param inflater           layout inflater supplied by the fragment manager
     * @param container          optional parent container
     * @param savedInstanceState optional restored state
     * @return the shell root
     */
    @Override
    public @NotNull View onCreateView(@NotNull LayoutInflater inflater,
                                      @Nullable ViewGroup container,
                                      @Nullable DataSet savedInstanceState) {
        Context context = requireContext();
        if (this.model == null) return this.buildRecoveryView(context);

        this.theme = this.model.theme() != null ? this.model.theme() : MuiTheme.modern(context);

        FrameLayout root = new FrameLayout(context);
        this.root = root;
        this.model.background().attach(root);

        LinearLayout shell = new LinearLayout(context);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(shell, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));

        shell.addView(MuiShimmerProfile.withCurrent(this.model.shimmerProfile(),
                () -> MuiControls.withFactory(this.model.widgetFactory(),
                        () -> this.buildRail(context, this.theme))),
                new LinearLayout.LayoutParams(MuiControls.dp(context, RAIL_WIDTH), MuiControls.MATCH));

        this.content = new FrameLayout(context);
        shell.addView(this.content, new LinearLayout.LayoutParams(0, MuiControls.MATCH, 1.0F));

        for (MuiConfigScreen.PageSpec spec : this.model.pages()) {
            this.pages.add(this.createPage(context, spec));
        }

        int initialPage = this.model.initialPage();
        if (savedInstanceState != null) {
            initialPage = savedInstanceState.getInt(ARG_SELECTED_PAGE, initialPage);
        }
        this.showPage(initialPage);
        this.restoreScroll(savedInstanceState);
        return root;
    }

    /**
     * Invokes the optional {@code onCreated} callback once the view is attached.
     *
     * @param view               the created shell view
     * @param savedInstanceState optional restored state
     */
    @Override
    public void onViewCreated(@NotNull View view, @Nullable DataSet savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.model != null && this.model.onCreated() != null) {
            this.model.onCreated().accept(this);
        }
    }

    /**
     * Saves the active page and per-page scroll offsets.
     *
     * @param outState state container
     */
    @Override
    public void onSaveInstanceState(@NotNull DataSet outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_SELECTED_PAGE, Math.max(0, this.selected));

        int[] scroll = new int[this.pages.size()];
        for (int i = 0; i < this.pages.size(); i++) {
            scroll[i] = this.pages.get(i).getScrollY();
        }
        outState.putIntArray(ARG_SCROLL, scroll);
    }

    /**
     * Builds one page from its spec, either through {@link MuiPage} or a custom factory.
     *
     * @param context the screen context
     * @param spec    page spec
     * @return the page root
     */
    private @NotNull View createPage(@NotNull Context context, @NotNull MuiConfigScreen.PageSpec spec) {
        return MuiShimmerProfile.withCurrent(this.model.shimmerProfile(),
                () -> MuiControls.withFactory(this.model.widgetFactory(), () -> {
                    if (spec.viewFactory() != null) return spec.viewFactory().apply(context);
                    return new MuiPage(context, this.theme, this.model.contentWidth(),
                            spec.title(), spec.subtitle(), spec.body()).root();
                }));
    }

    private void restoreScroll(@Nullable DataSet savedInstanceState) {
        if (savedInstanceState == null) return;
        int[] scroll = savedInstanceState.getIntArray(ARG_SCROLL);
        if (scroll == null) return;

        for (int i = 0; i < this.pages.size() && i < scroll.length; i++) {
            View page = this.pages.get(i);
            int y = scroll[i];
            page.post(() -> page.scrollTo(0, y));
        }
    }

    /**
     * Rebuilds the currently visible page from its original factory/body. Custom pages can use
     * this after a theme change or a config-dependent layout change.
     */
    public void reloadCurrentPage() {
        if (this.model == null || this.selected < 0 || this.selected >= this.pages.size()) return;

        int index = this.selected;
        this.pages.set(index, this.createPage(requireContext(), this.model.pages().get(index)));
        this.showPage(index);
    }

    /**
     * Adds an overlay on top of the whole configuration shell.
     *
     * @param overlay the overlay view
     */
    public void showOverlay(@NotNull View overlay) {
        if (this.root == null) return;
        this.root.addView(overlay, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));
    }

    /**
     * Removes a previously shown overlay.
     *
     * @param overlay the overlay view
     */
    public void removeOverlay(@NotNull View overlay) {
        if (this.root == null) return;
        this.root.removeView(overlay);
    }

    /**
     * Builds the navigation rail.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the rail layout
     */
    private @NotNull LinearLayout buildRail(@NotNull Context context, @NotNull MuiTheme theme) {
        LinearLayout rail = MuiControls.column(context);
        rail.setPadding(MuiControls.dp(context, 12), MuiControls.dp(context, 18),
                MuiControls.dp(context, 12), MuiControls.dp(context, 12));
        rail.setBackground(new ColorDrawable(MuiControls.alpha(theme.surface(), 0.96F)));

        if (this.model.railHeader() != null) {
            View header = this.model.railHeader().apply(context);
            if (header != null) rail.addView(header, MuiControls.fill());
        } else {
            TextView title = MuiControls.heading(context, theme, this.model.title(), 16, theme.onSurface());
            rail.addView(title, MuiControls.fill());

            if (!this.model.subtitle().isBlank()) {
                TextView subtitle = MuiControls.text(context, theme, this.model.subtitle(), 10,
                        theme.onSurfaceVariant());
                LinearLayout.LayoutParams params = MuiControls.fill();
                params.topMargin = MuiControls.dp(context, 4);
                rail.addView(subtitle, params);
            }
        }

        View gap = new View(context);
        rail.addView(gap, new LinearLayout.LayoutParams(0, MuiControls.dp(context, 18)));

        for (int i = 0; i < this.model.pages().size(); i++) {
            MuiConfigScreen.PageSpec spec = this.model.pages().get(i);
            TextView item = MuiControls.text(context, theme, spec.navLabel(), 13, theme.onSurfaceVariant());
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(MuiControls.dp(context, 10), MuiControls.dp(context, 9),
                    MuiControls.dp(context, 10), MuiControls.dp(context, 9));

            int index = i;
            item.setOnClickListener(view -> this.showPage(index));

            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(context, 4);
            rail.addView(item, params);
            this.navItems.add(item);
        }

        View spacer = new View(context);
        rail.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1.0F));

        if (this.model.railFooter() != null) {
            View footer = this.model.railFooter().apply(context);
            if (footer != null) rail.addView(footer, MuiControls.fill());
        } else if (!this.model.footer().isBlank()) {
            TextView footer = MuiControls.text(context, theme, this.model.footer(), 10, theme.onSurfaceVariant());
            rail.addView(footer, MuiControls.fill());
        }

        this.refreshNav();
        return rail;
    }

    /**
     * Swaps the visible page and updates the navigation selection.
     *
     * @param index zero-based page index; clamped to the available range
     */
    private void showPage(int index) {
        if (this.content == null || this.pages.isEmpty()) return;

        int clamped = Math.max(0, Math.min(this.pages.size() - 1, index));
        View page = this.pages.get(clamped);
        this.content.removeAllViews();
        this.content.addView(page, new FrameLayout.LayoutParams(
                MuiControls.MATCH, MuiControls.MATCH, Gravity.CENTER_HORIZONTAL));
        this.selected = clamped;
        this.refreshNav();
    }

    /**
     * Applies selected/unselected styling to every navigation item.
     */
    private void refreshNav() {
        if (this.navItems.isEmpty() || this.theme == null) return;

        Context context = requireContext();
        for (int i = 0; i < this.navItems.size(); i++) {
            TextView item = this.navItems.get(i);
            boolean active = i == this.selected;

            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(context, 10));
            background.setColor(active ? MuiControls.alpha(this.theme.primary(), 0.16F) : 0x00000000);
            if (active) {
                background.setStroke(Math.max(1, MuiControls.dp(context, 1)),
                        MuiControls.alpha(this.theme.primary(), 0.38F));
            }

            item.setBackground(background);
            item.setTextColor(active ? this.theme.primary() : this.theme.onSurfaceVariant());
        }
    }

    private @NotNull View buildRecoveryView(@NotNull Context context) {
        MuiTheme theme = MuiTheme.modern(context);

        FrameLayout root = new FrameLayout(context);
        LinearLayout column = MuiControls.column(context);
        column.setPadding(MuiControls.dp(context, 24), MuiControls.dp(context, 24),
                MuiControls.dp(context, 24), MuiControls.dp(context, 24));

        TextView title = MuiControls.heading(context, theme, "Configuration unavailable", 20,
                theme.onSurface());
        column.addView(title, MuiControls.fill());

        TextView message = MuiControls.text(context, theme,
                "The configuration screen was recreated without its model. Close it and reopen the config screen.",
                12, theme.onSurfaceVariant());
        LinearLayout.LayoutParams messageParams = MuiControls.fill();
        messageParams.topMargin = MuiControls.dp(context, 8);
        column.addView(message, messageParams);

        Button close = MuiControls.button(context, theme, "Close", false,
                view -> Minecraft.getInstance().setScreen(null));
        LinearLayout.LayoutParams closeParams = MuiControls.wrap();
        closeParams.topMargin = MuiControls.dp(context, 18);
        column.addView(close, closeParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                MuiControls.dp(context, 420), MuiControls.WRAP, Gravity.CENTER);
        root.addView(column, params);
        return root;
    }

    /**
     * Detaches the background, invokes the optional {@code onDestroyed} callback, and clears the
     * retained pages.
     */
    @Override
    public void onDestroyView() {
        if (this.model != null) {
            this.model.background().detach();
            if (this.model.onDestroyed() != null) this.model.onDestroyed().accept(this);
        }
        this.root = null;
        this.content = null;
        this.pages.clear();
        this.navItems.clear();
        this.selected = -1;
        super.onDestroyView();
    }

    /**
     * Removes the process-local model only when this fragment instance owns it.
     */
    @Override
    public void onDestroy() {
        if (this.modelKey != null && this.model != null) {
            MuiConfigRegistry.removeModel(this.modelKey, this.model);
        }
        super.onDestroy();
    }
}