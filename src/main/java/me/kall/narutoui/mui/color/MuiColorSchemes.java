package me.kall.narutoui.mui.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of named color schemes.
 * <p>
 * Consumers iterate {@link #all()} instead of maintaining their own enum, so a scheme registered
 * here (by NarutoUI or another mod) becomes available in every settings screen that uses the
 * catalog. Ids are stable config values; display names come from {@link MuiColorScheme#nameKey()}.
 */
public final class MuiColorSchemes {
    private static final List<MuiColorScheme> SCHEMES = new CopyOnWriteArrayList<>();

    /** Warm black, gold and red: the original Naruto scheme. */
    public static final MuiColorScheme NARUTO = new MuiColorScheme(
            "NARUTO",
            "narutoui.scheme.naruto",
            new MuiThemePalette(
                    0xFFE0A64A, 0xFF1A1208, 0xFF101116, 0xFF1F2027, 0xFF2A2B34,
                    0xFFEDEFF3, 0xFFA6AAB4, 0xFF3A3C48, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFFFF8F2A, 0xFFE0A64A, 0xFFA03A26, 0xFFFFFFFF, 0xFFFFD64A,
                    0xFFFF4A38, 0xB01E0A08, 0xCCE33A26, 0xFFFFC42E),
            new int[]{
                    0xFFFFFFFF, 0xFFFFD64A, 0xFFFFC42E, 0xFFFF8F2A, 0xFFFF4A38,
                    0xFFFF8F2A, 0xFFFFC42E, 0xFFFFD64A, 0xFFFFFFFF}
    );

    /** White and gray, without an accent. */
    public static final MuiColorScheme CLASSIC = new MuiColorScheme(
            "CLASSIC",
            "narutoui.scheme.classic",
            new MuiThemePalette(
                    0xFFFFFFFF, 0xFF16171C, 0xFF0E0F12, 0xFF17191E, 0xFF22252C,
                    0xFFF2F3F6, 0xFFA9AEB8, 0xFF3A3E47, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFF55,
                    0xFFFF5555, 0xA0404040, 0xFFFFFFFF, 0x00000000),
            MuiShimmerRamps.neutral()
    );

    /** Jade accent on a deep green surface. */
    public static final MuiColorScheme JADE = new MuiColorScheme(
            "JADE",
            "narutoui.scheme.jade",
            new MuiThemePalette(
                    0xFF3DDC97, 0xFF07281A, 0xFF0B1512, 0xFF13231D, 0xFF1C322A,
                    0xFFE7F6EF, 0xFF9DB8AC, 0xFF2B463C, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFF9DB8AC, 0xFF9DB8AC, 0xFF2B463C, 0xFFE7F6EF, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00B1512, 0xCC3DDC97, 0xFF3DDC97),
            MuiShimmerRamps.rich(0xFF3DDC97, 0xFF2B463C)
    );

    /** Azure accent on a deep blue surface. */
    public static final MuiColorScheme AZURE = new MuiColorScheme(
            "AZURE",
            "narutoui.scheme.azure",
            new MuiThemePalette(
                    0xFF5AA9FF, 0xFF04162B, 0xFF0A111C, 0xFF121C2C, 0xFF1B2A3F,
                    0xFFE8F1FF, 0xFFA3B6CF, 0xFF2B3D55, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFFA3B6CF, 0xFFA3B6CF, 0xFF2B3D55, 0xFFE8F1FF, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00A111C, 0xCC5AA9FF, 0xFF5AA9FF),
            MuiShimmerRamps.rich(0xFF5AA9FF, 0xFF2B3D55)
    );

    /** Sakura accent on a warm charcoal surface. */
    public static final MuiColorScheme SAKURA = new MuiColorScheme(
            "SAKURA",
            "narutoui.scheme.sakura",
            new MuiThemePalette(
                    0xFFFF8FB3, 0xFF2A0712, 0xFF150E13, 0xFF21161D, 0xFF2D1E27,
                    0xFFFDEBF1, 0xFFC9A9B6, 0xFF4A2E3A, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFFC9A9B6, 0xFFC9A9B6, 0xFF4A2E3A, 0xFFFDEBF1, 0xFFFFD24A,
                    0xFFFF6B6B, 0xB0150E13, 0xCCFF8FB3, 0xFFFF8FB3),
            MuiShimmerRamps.rich(0xFFFF8FB3, 0xFF4A2E3A)
    );

    /** Neutral graphite surface with a light accent. */
    public static final MuiColorScheme GRAPHITE = new MuiColorScheme(
            "GRAPHITE",
            "narutoui.scheme.graphite",
            new MuiThemePalette(
                    0xFFD6D9E0, 0xFF111318, 0xFF0E0F12, 0xFF17191E, 0xFF22252C,
                    0xFFF2F3F6, 0xFFA9AEB8, 0xFF3A3E47, 0xFFFF6B6B),
            new MuiHudPalette(
                    0xFFA9AEB8, 0xFFA9AEB8, 0xFF3A3E47, 0xFFF2F3F6, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00E0F12, 0xCCD6D9E0, 0xFFD6D9E0),
            MuiShimmerRamps.rich(0xFFD6D9E0, 0xFF3A3E47)
    );

    static {
        register(NARUTO);
        register(CLASSIC);
        register(JADE);
        register(AZURE);
        register(SAKURA);
        register(GRAPHITE);
    }

    private MuiColorSchemes() {}

    /**
     * Registers or replaces a scheme. The id is the key consumers store in their configs.
     *
     * @param scheme the scheme
     * @return the registered scheme
     */
    public static @NotNull MuiColorScheme register(@NotNull MuiColorScheme scheme) {
        SCHEMES.removeIf(existing -> existing.id().equalsIgnoreCase(scheme.id()));
        SCHEMES.add(scheme);
        return scheme;
    }

    /**
     * Returns the schemes in registration order.
     *
     * @return an immutable snapshot
     */
    public static @NotNull List<MuiColorScheme> all() {
        return List.copyOf(SCHEMES);
    }

    /**
     * Looks up a scheme by id, ignoring case.
     *
     * @param id the id, or {@code null}
     * @return the scheme, or {@code null}
     */
    public static @Nullable MuiColorScheme byId(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (MuiColorScheme scheme : SCHEMES) {
            if (scheme.id().equalsIgnoreCase(id)) {
                return scheme;
            }
        }
        return null;
    }
}