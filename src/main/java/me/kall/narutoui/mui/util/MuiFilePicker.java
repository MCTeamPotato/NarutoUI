package me.kall.narutoui.mui.util;

import icyllis.modernui.mc.MuiModApi;
import javax.swing.*;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generic image/folder picker backed by the native TinyFileDialogs dialog with AWT/Swing fallback.
 * Titles and filter labels are supplied by the caller, so the utility is not tied to a mod.
 */
public final class MuiFilePicker {
    private static final Logger LOGGER = LogManager.getLogger("Mui");

    /**
     * Pick callback.
     */
    public interface Callback {
        /**
         * Called on the UI thread with the selected path.
         *
         * @param path selected path
         */
        void onPicked(@NotNull Path path);

        /**
         * Called on the UI thread when the dialog fails.
         */
        default void onFailed() {}
    }

    private MuiFilePicker() {}

    /**
     * Opens an image file dialog.
     *
     * @param title       dialog title
     * @param filterLabel native filter label
     * @param callback    selection callback
     */
    public static void openImage(@NotNull String title, @NotNull String filterLabel, @NotNull Callback callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            openImageNow(title, filterLabel, callback);
        } else {
            minecraft.execute(() -> openImageNow(title, filterLabel, callback));
        }
    }

    /**
     * Opens a folder dialog.
     *
     * @param initial  initial folder, or {@code null}
     * @param title    dialog title
     * @param callback selection callback
     */
    public static void openFolder(@Nullable Path initial, @NotNull String title, @NotNull Callback callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            openFolderNow(initial, title, callback);
        } else {
            minecraft.execute(() -> openFolderNow(initial, title, callback));
        }
    }

    private static void openImageNow(@NotNull String title, @NotNull String filterLabel, @NotNull Callback callback) {
        try {
            String picked = openNativeImage(title, filterLabel);
            if (picked == null || picked.isBlank()) return;
            Path path = Path.of(picked);
            MuiModApi.postToUiThread(() -> callback.onPicked(path));
        } catch (Throwable error) {
            LOGGER.warn("Native image dialog failed, falling back to AWT", error);
            openAwtImage(title, callback);
        }
    }

    private static void openFolderNow(@Nullable Path initial, @NotNull String title, @NotNull Callback callback) {
        try {
            String picked = openNativeFolder(initial, title);
            if (picked == null || picked.isBlank()) return;
            Path path = Path.of(picked);
            MuiModApi.postToUiThread(() -> callback.onPicked(path));
        } catch (Throwable error) {
            LOGGER.warn("Native folder dialog failed, falling back to Swing", error);
            openSwingFolder(title, callback);
        }
    }

    private static @Nullable String openNativeImage(@NotNull String title, @NotNull String filterLabel) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(5);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.put(stack.UTF8("*.bmp"));
            filters.put(stack.UTF8("*.gif"));
            filters.flip();
            return TinyFileDialogs.tinyfd_openFileDialog(null, null, filters, filterLabel, false);
        }
    }

    private static @Nullable String openNativeFolder(@Nullable Path initial, @NotNull String title) {
        String defaultPath = initial != null && Files.isDirectory(initial) ? initial.toString() : Minecraft.getInstance().gameDirectory.getAbsolutePath();
        return TinyFileDialogs.tinyfd_selectFolderDialog(title, defaultPath);
    }

    private static void openAwtImage(@NotNull String title, @NotNull Callback callback) {
        Thread thread = new Thread(() -> {
            try {
                if (GraphicsEnvironment.isHeadless()) {
                    MuiModApi.postToUiThread(callback::onFailed);
                    return;
                }

                FileDialog dialog = new FileDialog((Frame) null, title, FileDialog.LOAD);
                dialog.setAlwaysOnTop(true);
                dialog.setMultipleMode(false);
                dialog.setFilenameFilter((dir, name) -> isImage(name));
                dialog.setFile("*.png;*.jpg;*.jpeg;*.bmp;*.gif");
                dialog.setVisible(true);

                String file = dialog.getFile();
                String directory = dialog.getDirectory();
                dialog.dispose();
                if (file == null || directory == null) return;

                Path path = Path.of(directory, file);
                MuiModApi.postToUiThread(() -> callback.onPicked(path));
            } catch (Throwable error) {
                LOGGER.error("Could not open the image picker", error);
                MuiModApi.postToUiThread(callback::onFailed);
            }
        }, "Mui-FilePicker");
        thread.setDaemon(true);
        thread.start();
    }

    private static void openSwingFolder(@NotNull String title, @NotNull Callback callback) {
        if (GraphicsEnvironment.isHeadless()) {
            MuiModApi.postToUiThread(callback::onFailed);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle(title);
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

                Path path = chooser.getSelectedFile().toPath();
                MuiModApi.postToUiThread(() -> callback.onPicked(path));
            } catch (Throwable error) {
                LOGGER.error("Could not open the folder picker", error);
                MuiModApi.postToUiThread(callback::onFailed);
            }
        });
    }

    private static boolean isImage(@NotNull String name) {
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp") || lower.endsWith(".gif") || lower.endsWith(".tga");
    }
}