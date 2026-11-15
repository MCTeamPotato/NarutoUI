package me.kall.narutoui.mui.background;

import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.util.MuiLocalization;
import org.jetbrains.annotations.NotNull;

/**
 * Localized labels used by the configurable background.
 * <p>
 * The strings may be literal text or translation keys; they are resolved through
 * {@link MuiLocalization#resolve(CharSequence)} when the record is created. Use {@link #builder()}
 * to set only the labels that differ from the English defaults.
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
    /** Resolves every supplied label through the current language table. */
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

    /**
     * Starts a label builder with English defaults.
     *
     * @return a new builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Mutable label builder. Every setter accepts a translation key or literal text and returns
     * {@code this}; the keys are resolved by {@link #build()}.
     */
    public static final class Builder {
        private CharSequence pickerTitle = "Choose image";
        private CharSequence imageFilter = "Images";
        private CharSequence pickerFailed = "Could not open the image picker";
        private CharSequence folderPickerTitle = "Choose folder";
        private CharSequence folderPickerFailed = "Could not open the folder picker";
        private CharSequence failed = "Could not load the image";
        private CharSequence saved = "Background saved";
        private MuiCropOverlay.Labels crop = MuiCropOverlay.Labels.builder().build();

        private Builder() {}

        /**
         * Sets the image picker title.
         *
         * @param pickerTitle title or translation key
         * @return this builder
         */
        public @NotNull Builder pickerTitle(@NotNull CharSequence pickerTitle) {
            this.pickerTitle = pickerTitle;
            return this;
        }

        /**
         * Sets the image picker filter label.
         *
         * @param imageFilter filter label or translation key
         * @return this builder
         */
        public @NotNull Builder imageFilter(@NotNull CharSequence imageFilter) {
            this.imageFilter = imageFilter;
            return this;
        }

        /**
         * Sets the message shown when the image picker fails.
         *
         * @param pickerFailed message or translation key
         * @return this builder
         */
        public @NotNull Builder pickerFailed(@NotNull CharSequence pickerFailed) {
            this.pickerFailed = pickerFailed;
            return this;
        }

        /**
         * Sets the folder picker title.
         *
         * @param folderPickerTitle title or translation key
         * @return this builder
         */
        public @NotNull Builder folderPickerTitle(@NotNull CharSequence folderPickerTitle) {
            this.folderPickerTitle = folderPickerTitle;
            return this;
        }

        /**
         * Sets the message shown when the folder picker fails.
         *
         * @param folderPickerFailed message or translation key
         * @return this builder
         */
        public @NotNull Builder folderPickerFailed(@NotNull CharSequence folderPickerFailed) {
            this.folderPickerFailed = folderPickerFailed;
            return this;
        }

        /**
         * Sets the message shown when a picture cannot be decoded or stored.
         *
         * @param failed message or translation key
         * @return this builder
         */
        public @NotNull Builder failed(@NotNull CharSequence failed) {
            this.failed = failed;
            return this;
        }

        /**
         * Sets the message shown after a picture was stored.
         *
         * @param saved message or translation key
         * @return this builder
         */
        public @NotNull Builder saved(@NotNull CharSequence saved) {
            this.saved = saved;
            return this;
        }

        /**
         * Sets the crop overlay labels.
         *
         * @param crop crop labels
         * @return this builder
         */
        public @NotNull Builder crop(@NotNull MuiCropOverlay.Labels crop) {
            this.crop = crop;
            return this;
        }

        /**
         * Resolves every label through the current language table.
         *
         * @return the labels
         */
        public @NotNull MuiBackgroundLabels build() {
            return new MuiBackgroundLabels(
                    this.pickerTitle.toString(),
                    this.imageFilter.toString(),
                    this.pickerFailed.toString(),
                    this.folderPickerTitle.toString(),
                    this.folderPickerFailed.toString(),
                    this.failed.toString(),
                    this.saved.toString(),
                    this.crop);
        }
    }
}
