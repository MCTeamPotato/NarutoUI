package me.kall.narutoui.mui.screen;

/**
 * Transition played when a {@link MuiConfigScreen} switches between pages.
 * <p>
 * Screens use the transition configured with
 * {@code MuiSettingsScreen.Builder#pageTransition(MuiPageTransition)} or
 * {@code MuiConfigScreen.Builder#pageTransition(MuiPageTransition)}. The default is
 * {@link #FADE}. To keep the instant page switch, call the matching
 * {@code pageTransitionDuration(0)} builder method; a duration of zero always switches without an
 * animation. The duration can also be overridden with a positive value; otherwise the value
 * returned by {@link #defaultDurationMillis()} is used.
 * <p>
 * Transitions run on the UI thread and are cancelled when a new switch starts, when the shell is
 * rebuilt after a resize, or when the screen view is destroyed. Page views are retained across
 * switches; only their alpha and vertical translation change while the effect plays.
 */
public enum MuiPageTransition {
    /**
     * Cross-fades the outgoing page into the incoming page. This is the default.
     */
    FADE(320L),
    /**
     * Moves the outgoing page up and brings the incoming page in from the bottom.
     */
    SLIDE_UP(220L),
    /**
     * Moves the outgoing page down and brings the incoming page in from the top.
     */
    SLIDE_DOWN(220L),
    /**
     * Slides in the direction of the page order: moving to a later page slides up, moving to an
     * earlier page slides down.
     */
    SLIDE_AUTO(220L);

    /** Duration used when the screen does not set an explicit one. */
    private final long defaultDurationMillis;

    MuiPageTransition(long defaultDurationMillis) {
        this.defaultDurationMillis = defaultDurationMillis;
    }

    /**
     * Returns the duration used when the screen does not set an explicit one.
     *
     * @return duration in milliseconds
     */
    public long defaultDurationMillis() {
        return this.defaultDurationMillis;
    }
}