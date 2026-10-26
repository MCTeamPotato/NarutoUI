package me.kall.narutoui.mui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Process-local storage used by {@link MuiConfigFragment} to recover its model after a Fragment
 * recreation. Stable registered recipes handle process restarts.
 */
final class MuiConfigRegistry {
    private static final Map<String, MuiConfigScreen.Model> MODELS = new ConcurrentHashMap<>();
    private static final Map<String, Recipe> RECIPES = new ConcurrentHashMap<>();

    private MuiConfigRegistry() {}

    static void putModel(@NotNull String key, @NotNull MuiConfigScreen.Model model) {
        MODELS.put(key, model);
    }

    static @Nullable MuiConfigScreen.Model getModel(@NotNull String key) {
        return MODELS.get(key);
    }

    static void removeModel(@NotNull String key, @NotNull MuiConfigScreen.Model model) {
        MODELS.remove(key, model);
    }

    static void putRecipe(@NotNull String id, @NotNull Recipe recipe) {
        RECIPES.put(id, recipe);
    }

    static @Nullable Recipe getRecipe(@NotNull String id) {
        return RECIPES.get(id);
    }

    /**
     * Rebuilds a model from a registered recipe.
     *
     * @param id stable screen id
     * @return the model, or {@code null} if no recipe is registered
     */
    static @Nullable MuiConfigScreen.Model buildModel(@NotNull String id) {
        Recipe recipe = RECIPES.get(id);
        if (recipe == null) return null;

        MuiConfigScreen.Builder builder = MuiConfigScreen.builder(recipe.title());
        recipe.body().accept(builder);
        return builder.buildModel();
    }

    record Recipe(String title, Consumer<MuiConfigScreen.Builder> body) {}
}