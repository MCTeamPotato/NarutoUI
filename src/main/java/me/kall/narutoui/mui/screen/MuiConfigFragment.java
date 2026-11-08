package me.kall.narutoui.mui.screen;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.MuiScreen;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.text.TextUtils;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.widget.*;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Host fragment for a {@link MuiConfigScreen}. The shell is deliberately small: a left navigation
 * rail, a content frame, and a set of retained scrollable pages. In single-page mode the rail is
 * omitted and the one page fills the content area.
 * <p>
 * The fragment has a public no-arg constructor and resolves its model from
 * {@link MuiConfigFragment#ARG_MODEL}. This makes it safe for a FragmentManager to recreate.
 * <p>
 * It implements {@link MuiScreenHost}, so background controllers and other shell collaborators can
 * depend on the narrow host contract instead of the concrete fragment. It also implements
 * {@link ScreenCallback} so the shell can veto a user-initiated close through
 * {@link MuiConfigScreen.Builder#onCloseRequested}.
 */
@SuppressWarnings("DataFlowIssue")
public final class MuiConfigFragment extends Fragment implements MuiScreenHost, ScreenCallback {
    /** Fragment argument containing the registry key. */
    public static final String ARG_MODEL = "mui:model";
    private static final String ARG_SELECTED_PAGE = "mui:selected_page";
    private static final String ARG_SCROLL = "mui:scroll";

    /** Minimum rail width in DP. The rail grows for long page labels. */
    private static final int RAIL_MIN_WIDTH = 120;
    /** Maximum rail width in DP; longer labels are ellipsized instead of stealing the page. */
    private static final int RAIL_MAX_WIDTH = 220;
    /** Navigation label text size in SP, shared by measurement and the real item. */
    private static final float NAV_LABEL_SIZE = 13;

    /** Shell model, resolved in {@link #onCreate(DataSet)}. */
    private @Nullable MuiConfigScreen.Model model;
    /** Registry key used to remove the process-local model on final destroy. */
    private @Nullable String modelKey;
    /** Navigation item views, parallel to {@link #pages}. */
    private final List<TextView> navItems = new ArrayList<>();
    /** Page roots, built on first visit and retained across tab switches; {@code null} means unbuilt. */
    private final List<View> pages = new ArrayList<>();

    /** Shell root that also hosts overlays. */
    private @Nullable FrameLayout root;
    /** Content frame that swaps page roots. */
    private @Nullable FrameLayout content;
    /** Rebuilds the shell after a window/GUI-scale resize so dp sizes and the rail width refresh. */
    private final Runnable resizeRebuild = this::rebuildForResize;
    /** Registered while the view exists. */
    private final MuiModApi.OnWindowResizeListener resizeListener = (width, height, guiScale, oldGuiScale) ->
            MuiModApi.postToUiThread(this::scheduleResizeRebuild);
    /** Guards duplicate listener registration. */
    private boolean resizeListenerRegistered;
    /** Resolved or overridden theme. */
    private MuiTheme theme;
    /** Currently selected page index, or {@code -1}. */
    private int selected = -1;
    /** Running page transition, or {@code null}. */
    private @Nullable ValueAnimator pageAnimator;
    /** Outgoing page kept below the incoming page while a transition runs. */
    private @Nullable View outgoingPage;
    /** Backing Minecraft screen, used for back navigation. */
    private @Nullable Screen screen;
    /** Screen to return to when the backing screen cannot close itself. */
    private @Nullable Screen parent;
    /** Set once a confirmed close bypasses the close guard. */
    private boolean allowClose;
    /** Keep the model registered while a child screen (for example Oculus' pack screen) is open. */
    private boolean retainModelForChild;

    /**
     * No-arg constructor required by {@code FragmentFactory} and FragmentManager recreation.
     */
    public MuiConfigFragment() {}

    /**
     * Links this fragment to the screen that hosts it.
     *
     * @param screen the backing screen
     * @param parent the screen to return to, or {@code null}
     */
    void attachScreen(@NotNull Screen screen, @Nullable Screen parent) {
        this.screen = screen;
        this.parent = parent;
    }

    /**
     * Keeps this screen's model registered while a child screen is opened from it. Without this,
     * the fragment is destroyed when the child screen replaces the shell and the model is removed;
     * returning from the child then recreates the fragment without a model.
     * <p>
     * The flag is cleared when the view is created again, so a normal close after returning still
     * removes the model.
     */
    public void retainModelForChildScreen() {
        this.retainModelForChild = true;
    }

    /**
     * Resolves the shell model from arguments or a registered stable recipe.
     *
     * @param savedInstanceState optional fragment state
     */
    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataSet arguments = this.getArguments();
        this.modelKey = arguments == null ? null : arguments.getString(ARG_MODEL);
        if (this.modelKey != null && !this.modelKey.isEmpty()) {
            this.model = MuiConfigScreen.buildModelFor(this.modelKey);
        }
    }

    /**
     * Builds the rail, content frame, action bar and initial page. Other pages are built on first visit.
     *
     * @param inflater           layout inflater supplied by the fragment manager
     * @param container          optional parent container
     * @param savedInstanceState optional restored state
     * @return the shell root
     */
    @Override
    public @NotNull View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable DataSet savedInstanceState) {
        Context context = this.requireContext();
        if (this.model == null) return this.buildRecoveryView(context);

        this.allowClose = false;
        this.theme = this.resolveTheme(context);

        if (!this.resizeListenerRegistered) {
            MuiModApi.addOnWindowResizeListener(this.resizeListener);
            this.resizeListenerRegistered = true;
        }

        FrameLayout root = new FrameLayout(context);
        this.root = root;
        this.model.background().attach(this, root);

        int initialPage = this.model.initialPage();
        if (savedInstanceState != null) {
            initialPage = savedInstanceState.getInt(ARG_SELECTED_PAGE, initialPage);
        }
        this.buildShell(context, root, initialPage);
        this.restoreScroll(savedInstanceState);
        return root;
    }

    /**
     * Builds the rail, content frame, action bar and initial page into an existing root. Called on
     * first view creation and again after a window/GUI-scale resize. The callbacks that own staged
     * values live in the model, so rebuilding the view preserves unsaved edits.
     *
     * @param context     screen context
     * @param root        shell root to fill
     * @param initialPage page index to select
     */
    private void buildShell(@NotNull Context context, @NotNull FrameLayout root, int initialPage) {
        this.pages.clear();
        this.navItems.clear();

        LinearLayout body = MuiControls.column(context);
        root.addView(body, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));

        LinearLayout shell = null;
        if (!this.model.singlePage()) {
            shell = new LinearLayout(context);
            body.addView(shell, new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F));

            if (this.model.navigation() == MuiNavigation.TOP) {
                shell.setOrientation(LinearLayout.VERTICAL);

                View topBar = MuiShimmerProfile.withCurrent(this.model.shimmerProfile(), () -> MuiControls.withFactory(this.model.widgetFactory(), () -> this.buildTopBar(context, this.theme)));
                shell.addView(topBar, new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
            } else {
                shell.setOrientation(LinearLayout.HORIZONTAL);

                int railWidth = MuiControls.withFactory(this.model.widgetFactory(), () -> this.resolveRailWidth(context));
                View rail = MuiShimmerProfile.withCurrent(this.model.shimmerProfile(), () -> MuiControls.withFactory(this.model.widgetFactory(), () -> this.buildRail(context, this.theme)));
                shell.addView(rail, new LinearLayout.LayoutParams(railWidth, MuiControls.MATCH));
            }
        }

        this.content = new FrameLayout(context);
        this.content.setClipChildren(true);
        if (shell == null) {
            body.addView(this.content, new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F));
        } else if (this.model.navigation() == MuiNavigation.TOP) {
            shell.addView(this.content, new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F));
        } else {
            shell.addView(this.content, new LinearLayout.LayoutParams(0, MuiControls.MATCH, 1.0F));
        }

        Function<Context, View> actionBar = this.model.actionBar();
        if (actionBar != null) {
            View bar = MuiShimmerProfile.withCurrent(this.model.shimmerProfile(), () -> MuiControls.withFactory(this.model.widgetFactory(), () -> actionBar.apply(context)));
            if (bar != null) {
                FrameLayout host = new FrameLayout(context);
                host.setBackground(new ColorDrawable(MuiControls.alpha(this.theme.surface(), 0.96F)));
                host.setPadding(MuiControls.dp(context, 12), MuiControls.dp(context, 8), MuiControls.dp(context, 12), MuiControls.dp(context, 8));
                host.addView(bar, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
                body.addView(host, new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
            }
        }

        for (int i = 0; i < this.model.pages().size(); i++) {
            this.pages.add(null);
        }

        this.showPage(initialPage, false);
    }

    private void scheduleResizeRebuild() {
        FrameLayout current = this.root;
        if (current == null) {
            return;
        }

        // Debounce continuous window dragging; the shell is rebuilt once after the size settles.
        current.removeCallbacks(this.resizeRebuild);
        current.postDelayed(this.resizeRebuild, 120L);
    }

    private void rebuildForResize() {
        FrameLayout current = this.root;
        if (current == null || this.model == null) {
            return;
        }

        int page = Math.max(0, this.selected);
        Context context = this.requireContext();
        this.theme = this.resolveTheme(context);
        this.cancelPageTransition();
        this.model.background().detach();
        current.removeAllViews();
        this.content = null;
        this.selected = -1;
        this.model.background().attach(this, current);
        this.buildShell(context, current, page);
    }

    /**
     * Rebuilds the shell with the current theme provider while keeping the selected page.
     * <p>
     * This is stronger than {@link #reloadCurrentPage()}: the rail, navigation highlight, section
     * headings and widgets are all recreated, so a live accent change cannot leave half of the
     * screen on the previous theme.
     */
    public void reloadTheme() {
        if (this.root == null || this.model == null) {
            return;
        }
        this.rebuildForResize();
    }

    private @NotNull MuiTheme resolveTheme(@NotNull Context context) {
        if (this.model != null && this.model.themeProvider() != null) {
            MuiTheme provided = this.model.themeProvider().apply(context);
            if (provided != null) {
                return provided;
            }
        }
        if (this.model != null && this.model.theme() != null) {
            return this.model.theme();
        }
        return MuiTheme.modern(context);
    }

    @Override
    public void onThemeChanged() {
        this.reloadTheme();
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
        this.retainModelForChild = false;
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
            View page = this.pages.get(i);
            scroll[i] = page == null ? 0 : page.getScrollY();
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
        return MuiShimmerProfile.withCurrent(this.model.shimmerProfile(), () -> MuiControls.withFactory(this.model.widgetFactory(), () -> {
                    if (spec.viewFactory() != null) return spec.viewFactory().apply(context);
                    return new MuiPage(context, this.theme, this.model.contentWidth(), spec.title(), spec.subtitle(), spec.body()).root();
                }));
    }

    private void restoreScroll(@Nullable DataSet savedInstanceState) {
        if (savedInstanceState == null) return;
        int[] scroll = savedInstanceState.getIntArray(ARG_SCROLL);
        if (scroll == null) return;

        for (int i = 0; i < this.pages.size() && i < scroll.length; i++) {
            View page = this.pages.get(i);
            if (page == null) continue;
            int y = scroll[i];
            page.post(() -> page.scrollTo(0, y));
        }
    }

    /**
     * Rebuilds the currently visible page from its original factory/body. Custom pages can use
     * this after a theme change or a config-dependent layout change.
     */
    @Override
    public void reloadCurrentPage() {
        if (this.model == null || this.selected < 0 || this.selected >= this.pages.size()) return;

        int index = this.selected;
        this.pages.set(index, this.createPage(this.requireContext(), this.model.pages().get(index)));
        this.showPage(index, false);
    }

    /**
     * Rebuilds every page. The current page is rebuilt immediately; other pages are rebuilt lazily
     * on their next visit.
     */
    @Override
    public void reloadAllPages() {
        if (this.model == null || this.pages.isEmpty()) return;

        int index = Math.max(0, this.selected);
        this.pages.replaceAll(ignored -> null);
        this.showPage(index, false);
    }

    /**
     * Adds an overlay on top of the whole configuration shell.
     *
     * @param overlay the overlay view
     */
    @Override
    public void showOverlay(@NotNull View overlay) {
        if (this.root == null) return;
        this.root.addView(overlay, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));
    }

    /**
     * Removes a previously shown overlay.
     *
     * @param overlay the overlay view
     */
    @Override
    public void removeOverlay(@NotNull View overlay) {
        if (this.root == null) return;
        this.root.removeView(overlay);
    }

    /**
     * Only Escape is a back key for configuration screens. The inventory key must keep its normal
     * meaning instead of closing the screen.
     *
     * @param keyCode the key code
     * @param event   the key event
     * @return {@code true} for Escape
     */
    @Override
    public boolean isBackKey(int keyCode, @NotNull KeyEvent event) {
        return keyCode == KeyEvent.KEY_ESCAPE;
    }

    /**
     * Consults the close guard registered on the builder.
     *
     * @return {@code true} when the screen may close
     */
    @Override
    public boolean shouldClose() {
        if (this.allowClose) return true;
        return this.model == null || this.model.closeGuard() == null || this.model.closeGuard().getAsBoolean();
    }

    /**
     * Configuration screens pause the game in single-player, matching the vanilla screen behavior.
     *
     * @return {@code true}
     */
    @Override
    public boolean isPauseScreen() {
        return true;
    }

    /**
     * Asks the screen to perform its normal back action, including the close guard.
     */
    @Override
    public void requestClose() {
        this.performClose(false);
    }

    /**
     * Closes the screen after a confirmed close flow, bypassing the close guard.
     */
    @Override
    public void close() {
        this.performClose(true);
    }

    /**
     * Closes the backing Modern UI screen, falling back to the previous screen when the fragment was
     * recreated without its original screen reference.
     *
     * @param force {@code true} to bypass the close guard
     */
    private void performClose(boolean force) {
        if (force) {
            this.allowClose = true;
        }

        MuiScreen mui = null;
        if (this.screen instanceof MuiScreen attached) {
            mui = attached;
        } else if (Minecraft.getInstance().screen instanceof MuiScreen visible) {
            mui = visible;
        }

        if (mui != null) {
            mui.onBackPressed();
        } else {
            Minecraft.getInstance().setScreen(this.parent);
        }
    }

    /**
     * Resolves the rail width from the longest page label. The label is measured with the same text
     * factory, size and horizontal padding as the real navigation item, then the rail padding and a
     * small safety margin are added. Short labels keep the minimum width; very long labels are
     * capped and ellipsized by {@link #createNavItem} instead of widening the rail indefinitely.
     *
     * @param context the screen context
     * @return the rail width in pixels
     */
    private int resolveRailWidth(@NotNull Context context) {
        int minWidth = MuiControls.dp(context, RAIL_MIN_WIDTH);
        int maxWidth = MuiControls.dp(context, RAIL_MAX_WIDTH);
        int itemPadding = MuiControls.dp(context, 10);
        int widest = 0;

        for (MuiConfigScreen.PageSpec spec : this.model.pages()) {
            TextView probe = MuiControls.text(context, this.theme, MuiFormatting.strip(spec.navLabel()), NAV_LABEL_SIZE, this.theme.onSurfaceVariant());
            probe.setSingleLine(true);
            probe.setPadding(itemPadding, 0, itemPadding, 0);

            int unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            probe.measure(unspecified, unspecified);
            widest = Math.max(widest, probe.getMeasuredWidth());
        }

        // Rail horizontal padding (12 + 12) plus a few DP so the longest label never sits flush
        // against the scrollbar or the selected-item outline.
        int wanted = widest + MuiControls.dp(context, 24 + 8);
        return Math.max(minWidth, Math.min(maxWidth, wanted));
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
        rail.setPadding(MuiControls.dp(context, 12), MuiControls.dp(context, 18), MuiControls.dp(context, 12), MuiControls.dp(context, 12));
        rail.setBackground(new ColorDrawable(MuiControls.alpha(theme.surface(), 0.96F)));

        if (this.model.railHeader() != null) {
            View header = this.model.railHeader().apply(context);
            if (header != null) {
                if (this.model.titlesShimmerable() != MuiShimmerable.NEVER) MuiShimmerScope.apply(header, this.model.titlesShimmerable());
                rail.addView(header, MuiControls.fill());
            }
        } else {
            TextView title = MuiControls.heading(context, theme, this.model.title(), 16, theme.onSurface(), this.model.titlesShimmerable());
            rail.addView(title, MuiControls.fill());

            if (!this.model.subtitle().isBlank()) {
                TextView subtitle = MuiControls.text(context, theme, this.model.subtitle(), 10, theme.onSurfaceVariant(), this.model.titlesShimmerable());
                LinearLayout.LayoutParams params = MuiControls.fill();
                params.topMargin = MuiControls.dp(context, 4);
                rail.addView(subtitle, params);
            }
        }

        View gap = new View(context);
        rail.addView(gap, new LinearLayout.LayoutParams(0, MuiControls.dp(context, 18)));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(true);

        LinearLayout nav = MuiControls.column(context);
        for (int i = 0; i < this.model.pages().size(); i++) {
            MuiConfigScreen.PageSpec spec = this.model.pages().get(i);
            TextView item = this.createNavItem(context, theme, spec, i, false);

            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(context, 4);
            nav.addView(item, params);
        }
        scroll.addView(nav, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP));
        rail.addView(scroll, new LinearLayout.LayoutParams(MuiControls.MATCH, 0, 1.0F));

        if (this.model.railFooter() != null) {
            View footer = this.model.railFooter().apply(context);
            if (footer != null) {
                if (this.model.titlesShimmerable() != MuiShimmerable.NEVER) MuiShimmerScope.apply(footer, this.model.titlesShimmerable());
                rail.addView(footer, MuiControls.fill());
            }
        } else if (!this.model.footer().isBlank()) {
            TextView footer = MuiControls.text(context, theme, this.model.footer(), 10, theme.onSurfaceVariant(), this.model.titlesShimmerable());
            rail.addView(footer, MuiControls.fill());
        }

        this.refreshNav();
        return rail;
    }

    /**
     * Builds the horizontal navigation bar used by {@link MuiNavigation#TOP}.
     *
     * @param context the screen context
     * @param theme   active theme
     * @return the top bar
     */
    private @NotNull LinearLayout buildTopBar(@NotNull Context context, @NotNull MuiTheme theme) {
        LinearLayout bar = MuiControls.row(context);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(MuiControls.dp(context, 12), MuiControls.dp(context, 8), MuiControls.dp(context, 12), MuiControls.dp(context, 8));
        bar.setBackground(new ColorDrawable(MuiControls.alpha(theme.surface(), 0.96F)));

        if (this.model.railHeader() != null) {
            View header = this.model.railHeader().apply(context);
            if (header != null) {
                if (this.model.titlesShimmerable() != MuiShimmerable.NEVER) MuiShimmerScope.apply(header, this.model.titlesShimmerable());
                bar.addView(header, MuiControls.wrap());
            }
        } else {
            LinearLayout titleBlock = MuiControls.column(context);
            titleBlock.addView(MuiControls.heading(context, theme, this.model.title(), 16, theme.onSurface(), this.model.titlesShimmerable()), MuiControls.fill());

            if (!this.model.subtitle().isBlank()) {
                TextView subtitle = MuiControls.text(context, theme, this.model.subtitle(), 10, theme.onSurfaceVariant(), this.model.titlesShimmerable());
                LinearLayout.LayoutParams params = MuiControls.fill();
                params.topMargin = MuiControls.dp(context, 2);
                titleBlock.addView(subtitle, params);
            }

            bar.addView(titleBlock, MuiControls.wrap());
        }

        View gap = new View(context);
        bar.addView(gap, new LinearLayout.LayoutParams(MuiControls.dp(context, 16), MuiControls.dp(context, 1)));

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);

        LinearLayout navRow = MuiControls.row(context);
        navRow.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < this.model.pages().size(); i++) {
            MuiConfigScreen.PageSpec spec = this.model.pages().get(i);
            TextView item = this.createNavItem(context, theme, spec, i, true);
            LinearLayout.LayoutParams params = MuiControls.wrap();
            params.setMargins(0, 0, MuiControls.dp(context, 4), 0);
            navRow.addView(item, params);
        }

        scroll.addView(navRow, new FrameLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP));
        bar.addView(scroll, new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F));

        if (this.model.railFooter() != null) {
            View footer = this.model.railFooter().apply(context);
            if (footer != null) {
                if (this.model.titlesShimmerable() != MuiShimmerable.NEVER) MuiShimmerScope.apply(footer, this.model.titlesShimmerable());
                LinearLayout.LayoutParams params = MuiControls.wrap();
                params.leftMargin = MuiControls.dp(context, 8);
                bar.addView(footer, params);
            }
        } else if (!this.model.footer().isBlank()) {
            TextView footer = MuiControls.text(context, theme, this.model.footer(), 10, theme.onSurfaceVariant(), this.model.titlesShimmerable());
            LinearLayout.LayoutParams params = MuiControls.wrap();
            params.leftMargin = MuiControls.dp(context, 8);
            bar.addView(footer, params);
        }

        this.refreshNav();
        return bar;
    }

    /**
     * Creates one navigation item and registers it for selection styling.
     *
     * @param context the screen context
     * @param theme   active theme
     * @param spec    page spec
     * @param index   page index
     * @param top     {@code true} for the horizontal top bar
     * @return the navigation item
     */
    private @NotNull TextView createNavItem(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiConfigScreen.PageSpec spec, int index, boolean top) {
        TextView item = MuiControls.text(context, theme, MuiFormatting.strip(spec.navLabel()), NAV_LABEL_SIZE, theme.onSurfaceVariant(), this.model.navigationShimmerable());
        item.setSingleLine(true);
        if (!top) {
            item.setEllipsize(TextUtils.TruncateAt.END);
        }
        item.setGravity(top ? Gravity.CENTER : Gravity.CENTER_VERTICAL);
        item.setPadding(MuiControls.dp(context, top ? 12 : 10), MuiControls.dp(context, top ? 7 : 9), MuiControls.dp(context, top ? 12 : 10), MuiControls.dp(context, top ? 7 : 9));
        item.setOnClickListener(view -> this.showPage(index));
        this.navItems.add(item);
        return item;
    }

    /**
     * Swaps the visible page and plays the configured transition.
     *
     * @param index zero-based page index; clamped to the available range
     */
    private void showPage(int index) {
        this.showPage(index, true);
    }

    /**
     * Swaps the visible page and updates the navigation selection.
     *
     * @param index   zero-based page index; clamped to the available range
     * @param animate {@code true} to play the configured page transition
     */
    private void showPage(int index, boolean animate) {
        if (this.content == null || this.pages.isEmpty()) return;

        int clamped = Math.max(0, Math.min(this.pages.size() - 1, index));
        if (animate && clamped == this.selected) return;

        this.cancelPageTransition();

        View page = this.pages.get(clamped);
        if (page == null) {
            page = this.createPage(this.requireContext(), this.model.pages().get(clamped));
            this.pages.set(clamped, page);
        }

        int previousIndex = this.selected;
        View previous = previousIndex >= 0 && previousIndex < this.pages.size() ? this.pages.get(previousIndex) : null;
        if (previous == page || previous != null && previous.getParent() != this.content) previous = null;

        if (animate) {
            this.removeOtherChildren(previous);
        } else {
            this.content.removeAllViews();
        }

        this.content.addView(page, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH, Gravity.CENTER_HORIZONTAL));
        this.selected = clamped;
        this.refreshNav();

        MuiPageTransition transition = this.model.pageTransition();
        long duration = this.model.pageTransitionDuration();
        if (!animate || previous == null || duration <= 0L
                || !this.content.isAttachedToWindow() || !ValueAnimator.areAnimatorsEnabled()) {
            if (previous != null) this.content.removeView(previous);
            return;
        }

        this.startPageTransition(previous, page, transition, duration, clamped > previousIndex);
    }

    /**
     * Removes every content child except the retained outgoing page.
     *
     * @param keep the child to keep, or {@code null} to remove all
     */
    private void removeOtherChildren(@Nullable View keep) {
        if (this.content == null) return;
        for (int i = this.content.getChildCount() - 1; i >= 0; i--) {
            View child = this.content.getChildAt(i);
            if (child != keep) this.content.removeViewAt(i);
        }
    }

    /**
     * Runs the configured page transition.
     *
     * @param outgoing   page leaving the content area
     * @param incoming   page entering the content area
     * @param transition transition style
     * @param duration   duration in milliseconds
     * @param forward    {@code true} when the incoming page has a higher index
     */
    private void startPageTransition(@NotNull View outgoing, @NotNull View incoming, @NotNull MuiPageTransition transition, long duration, boolean forward) {
        boolean slide = transition != MuiPageTransition.FADE;
        boolean upward = transition == MuiPageTransition.SLIDE_UP || transition == MuiPageTransition.SLIDE_AUTO && forward;
        int travel = this.content.getHeight();
        if (travel <= 0) travel = MuiControls.dp(this.requireContext(), 240);
        final int distance = travel;

        incoming.setAlpha(slide ? 1.0F : 0.0F);
        incoming.setTranslationY(slide ? (upward ? distance : -distance) : 0.0F);
        outgoing.setAlpha(1.0F);
        outgoing.setTranslationY(0.0F);

        ValueAnimator animator = ValueAnimator.ofFloat(0.0F, 1.0F);
        animator.setDuration(duration);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            if (this.pageAnimator != animator) return;

            float progress = (Float) animation.getAnimatedValue();
            if (!slide) {
                outgoing.setAlpha(1.0F - progress);
                incoming.setAlpha(progress);
                return;
            }

            float start = upward ? distance : -distance;
            outgoing.setTranslationY(-start * progress);
            incoming.setTranslationY(start * (1.0F - progress));
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (MuiConfigFragment.this.pageAnimator != animator) return;
                MuiConfigFragment.this.finishPageTransition(animator);
            }
        });
        this.pageAnimator = animator;
        this.outgoingPage = outgoing;
        animator.start();
    }

    /**
     * Removes the outgoing page once the transition ends.
     *
     * @param animator the animator that finished
     */
    private void finishPageTransition(@NotNull ValueAnimator animator) {
        if (this.pageAnimator != animator) return;
        this.pageAnimator = null;

        View outgoing = this.outgoingPage;
        this.outgoingPage = null;

        View incoming = this.selected >= 0 && this.selected < this.pages.size() ? this.pages.get(this.selected) : null;
        if (incoming != null) {
            incoming.setAlpha(1.0F);
            incoming.setTranslationY(0.0F);
        }
        if (outgoing == null || outgoing == incoming) return;

        outgoing.setAlpha(1.0F);
        outgoing.setTranslationY(0.0F);
        if (this.content != null) this.content.removeView(outgoing);
    }

    /**
     * Cancels a running page transition and restores the pages it touched.
     */
    private void cancelPageTransition() {
        ValueAnimator animator = this.pageAnimator;
        this.pageAnimator = null;
        if (animator != null) animator.cancel();

        View outgoing = this.outgoingPage;
        this.outgoingPage = null;
        if (outgoing != null) {
            outgoing.setAlpha(1.0F);
            outgoing.setTranslationY(0.0F);
            if (this.content != null) this.content.removeView(outgoing);
        }

        if (this.selected >= 0 && this.selected < this.pages.size()) {
            View current = this.pages.get(this.selected);
            if (current != null) {
                current.setAlpha(1.0F);
                current.setTranslationY(0.0F);
            }
        }
    }

    /**
     * Applies selected/unselected styling to every navigation item.
     */
    private void refreshNav() {
        if (this.navItems.isEmpty() || this.theme == null) return;

        Context context = this.requireContext();
        for (int i = 0; i < this.navItems.size(); i++) {
            TextView item = this.navItems.get(i);
            boolean active = i == this.selected;

            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(context, 10));
            background.setColor(active ? MuiControls.alpha(this.theme.primary(), 0.16F) : 0x00000000);
            if (active) {
                background.setStroke(Math.max(1, MuiControls.dp(context, 1)), MuiControls.alpha(this.theme.primary(), 0.38F));
            }

            item.setBackground(background);
            item.setTextColor(active ? this.theme.primary() : this.theme.onSurfaceVariant());
        }
    }

    private @NotNull View buildRecoveryView(@NotNull Context context) {
        MuiTheme theme = MuiTheme.modern(context);

        FrameLayout root = new FrameLayout(context);
        LinearLayout column = MuiControls.column(context);
        column.setPadding(MuiControls.dp(context, 24), MuiControls.dp(context, 24), MuiControls.dp(context, 24), MuiControls.dp(context, 24));

        TextView title = MuiControls.heading(context, theme, "Configuration unavailable", 20, theme.onSurface());
        column.addView(title, MuiControls.fill());

        TextView message = MuiControls.text(context, theme, "The configuration screen was recreated without its model. Close it and reopen the config screen.", 12, theme.onSurfaceVariant());
        LinearLayout.LayoutParams messageParams = MuiControls.fill();
        messageParams.topMargin = MuiControls.dp(context, 8);
        column.addView(message, messageParams);

        Button close = MuiControls.button(context, theme, "Close", false, view -> this.performClose(true));
        LinearLayout.LayoutParams closeParams = MuiControls.wrap();
        closeParams.topMargin = MuiControls.dp(context, 18);
        column.addView(close, closeParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MuiControls.dp(context, 420), MuiControls.WRAP, Gravity.CENTER);
        root.addView(column, params);
        return root;
    }

    /**
     * Detaches the background, invokes the optional {@code onDestroyed} callback, and clears the
     * retained pages.
     */
    @Override
    public void onDestroyView() {
        this.cancelPageTransition();
        if (this.resizeListenerRegistered) {
            MuiModApi.removeOnWindowResizeListener(this.resizeListener);
            this.resizeListenerRegistered = false;
        }
        if (this.root != null) {
            this.root.removeCallbacks(this.resizeRebuild);
        }
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
        if (this.modelKey != null && this.model != null && !this.retainModelForChild) {
            MuiConfigRegistry.removeModel(this.modelKey, this.model);
        }
        super.onDestroy();
    }
}