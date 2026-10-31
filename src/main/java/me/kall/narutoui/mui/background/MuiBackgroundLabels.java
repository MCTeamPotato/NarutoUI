package me.kall.narutoui.mui.background;

import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * Localized labels used by the configurable background.
 * <p>
 * The strings may be literal text or translation keys; they are resolved through
 * {@link MuiLocalization#resolve(CharSequence)} when the record is created.
 *
 * @param pickerTitle        image picker title
 * @param imageFilter        image picker filter label
 * @param pickerFailed       message shown when the image picker fails
 * @param folderPickerTitle  folder picker title
 * @param folderPickerFailed message shown when the folder picker fails
 * @param failed             message shown when a picture cannot be decoded or stored
 * @param saved              message shown after a picture was stored
 * @param crop               labels of the crop overlay
 */
public record MuiBackgroundLabels(@NotNull String pickerTitle, @NotNull String imageFilter, @NotNull String pickerFailed, @NotNull String folderPickerTitle, @NotNull String folderPickerFailed, @NotNull String failed, @NotNull String saved, @NotNull MuiCropOverlay.Labels crop) {
    public MuiBackgroundLabels {
        pickerTitle = MuiLocalization.resolve(pickerTitle);
        imageFilter = MuiLocalization.resolve(imageFilter);
        pickerFailed = MuiLocalization.resolve(pickerFailed);
        folderPickerTitle = MuiLocalization.resolve(folderPickerTitle);
        folderPickerFailed = MuiLocalization.resolve(folderPickerFailed);
        failed = MuiLocalization.resolve(failed);
        saved = MuiLocalization.resolve(saved);
        crop = new MuiCropOverlay.Labels(MuiLocalization.resolve(crop.title()), MuiLocalization.resolve(crop.hint()), MuiLocalization.resolve(crop.confirm()), MuiLocalization.resolve(crop.cancel()));
    }
}