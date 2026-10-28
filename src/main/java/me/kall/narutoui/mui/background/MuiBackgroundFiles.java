package me.kall.narutoui.mui.background;

import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.BitmapFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * File and image helpers used by {@link MuiConfigurableBackground}.
 * <p>
 * The helper is intentionally free of configuration and UI state: it resolves names against a
 * game directory, lists slideshow pictures in the configured order, stores picked pictures under a
 * stable base name, and decodes bitmaps off the UI thread.
 */
public final class MuiBackgroundFiles {
    private static final Logger LOGGER = LogManager.getLogger("Mui");

    private final Path gameDir;
    private final String baseName;

    /**
     * Creates a helper rooted at a game directory.
     *
     * @param gameDir  game directory used to resolve relative names
     * @param baseName file name prefix used when storing a picked picture
     */
    public MuiBackgroundFiles(@NotNull Path gameDir, @NotNull String baseName) {
        if (baseName.isBlank()) throw new IllegalArgumentException("baseName must not be blank");
        this.gameDir = gameDir.toAbsolutePath().normalize();
        this.baseName = baseName;
    }

    /**
     * Resolves a stored background name against the game directory.
     *
     * @param name relative name
     * @return the resolved path
     */
    public @NotNull Path file(@NotNull String name) {
        return this.gameDir.resolve(name).normalize();
    }

    /**
     * Resolves a configured slideshow folder. Relative paths are resolved against the game
     * directory.
     *
     * @param configured configured folder
     * @return the resolved folder
     */
    public @NotNull Path folder(@NotNull String configured) {
        try {
            Path path = Path.of(configured);
            return path.isAbsolute() ? path.normalize() : this.gameDir.resolve(path).normalize();
        } catch (RuntimeException ignored) {
            return this.gameDir.resolve("__invalid-background-folder__");
        }
    }

    /**
     * Stores the selected folder in a portable form: relative to the game directory when possible.
     *
     * @param selected selected folder
     * @return the stored value
     */
    public @NotNull String store(@NotNull Path selected) {
        Path path = selected.toAbsolutePath().normalize();
        if (path.equals(this.gameDir)) return ".";
        if (path.startsWith(this.gameDir)) {
            return this.gameDir.relativize(path).toString().replace('\\', '/');
        }
        return path.toString();
    }

    /**
     * Lists the pictures of a slideshow folder in playback order.
     * <p>
     * Pictures named in {@code order} come first in that order, the rest follow alphabetically.
     *
     * @param configuredFolder configured folder
     * @param order            preferred file names
     * @return the ordered pictures, or an empty list when the folder is blank or unreadable
     */
    public @NotNull List<Path> images(@NotNull String configuredFolder, @NotNull List<String> order) {
        if (configuredFolder.isBlank()) return List.of();

        Path folder = folder(configuredFolder);
        if (!Files.isDirectory(folder)) return List.of();

        List<Path> found = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isImage(path.getFileName().toString()))
                    .forEach(found::add);
        } catch (IOException error) {
            LOGGER.warn("Could not list the background folder {}", folder, error);
            return List.of();
        }
        found.sort(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)));

        if (order.isEmpty()) return List.copyOf(found);

        List<Path> ordered = new ArrayList<>(found.size());
        for (String name : order) {
            String wanted = name == null ? "" : name.trim();
            if (wanted.isEmpty()) continue;

            for (Iterator<Path> iterator = found.iterator(); iterator.hasNext(); ) {
                Path path = iterator.next();
                if (path.getFileName().toString().equalsIgnoreCase(wanted)) {
                    ordered.add(path);
                    iterator.remove();
                    break;
                }
            }
        }
        ordered.addAll(found);
        return List.copyOf(ordered);
    }

    /**
     * Copies a picked picture into the game directory under the stable base name.
     *
     * @param source picked picture
     * @return the stored file name
     * @throws IOException when the picture cannot be copied
     */
    public @NotNull String copyInto(@NotNull Path source) throws IOException {
        String name = this.baseName + extensionOf(source.getFileName().toString());
        Path target = file(name);
        if (!source.toAbsolutePath().normalize().equals(target)) {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return name;
    }

    /**
     * Tests whether a file name is a supported picture.
     *
     * @param name file name
     * @return {@code true} for png, jpg, jpeg, bmp and gif
     */
    public static boolean isImage(@NotNull String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".bmp") || lower.endsWith(".gif");
    }

    /**
     * Decodes a picture off the UI thread.
     *
     * @param path picture path
     * @return a future that completes with the bitmap, or {@code null} when decoding fails
     */
    public static @NotNull CompletableFuture<Bitmap> decode(@NotNull Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable error) {
                LOGGER.warn("Could not decode background image {}", path, error);
                return null;
            }
        });
    }

    private static @NotNull String extensionOf(@NotNull String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return ".png";
        String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,5}") ? extension : ".png";
    }
}