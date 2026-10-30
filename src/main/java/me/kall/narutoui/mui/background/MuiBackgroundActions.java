package me.kall.narutoui.mui.background;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Background actions and live information exposed to configuration pages.
 * <p>
 * Pages receive this through their host and can drive the configurable background without knowing
 * how it is stored or rendered. The read methods let a page display the current folder and the
 * number of pictures without downcasting the controller.
 */
public interface MuiBackgroundActions {
    /**
     * Returns the current settings snapshot.
     *
     * @return the current state
     */
    @NotNull MuiBackgroundState state();

    /**
     * Returns the pictures currently found in the slideshow folder, in playback order.
     *
     * @return the ordered pictures
     */
    @NotNull List<Path> images();

    /**
     * Opens the image picker and installs the selected picture as the fallback background.
     */
    void chooseBackgroundImage();

    /**
     * Clears the fallback background picture and resets its crop.
     */
    void clearBackgroundImage();

    /**
     * Opens the folder picker and switches the background to that folder's slideshow.
     */
    void chooseBackgroundFolder();

    /**
     * Clears the slideshow folder and falls back to the single background picture.
     */
    void clearBackgroundFolder();

    /**
     * Stores the slideshow interval.
     *
     * @param seconds seconds each picture is shown
     */
    void setBackgroundInterval(double seconds);

    /**
     * Stores the cross-fade duration.
     *
     * @param seconds fade duration in seconds
     */
    void setBackgroundFade(double seconds);

    /**
     * Stores the background dim factor.
     *
     * @param dim dim factor in {@code 0..1}
     */
    void setBackgroundDim(double dim);
}