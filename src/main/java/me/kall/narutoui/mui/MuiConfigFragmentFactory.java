package me.kall.narutoui.mui;

import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.fragment.FragmentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Optional factory for hosts that expose {@code FragmentManager#setFragmentFactory}.
 * {@link MuiConfigFragment} already has a public no-arg constructor, so this is not required for
 * the standard {@code MuiModApi.createScreen} path.
 */
public final class MuiConfigFragmentFactory extends FragmentFactory {
    /**
     * Creates the fragment factory.
     */
    public MuiConfigFragmentFactory() {}

    @Override
    public @NotNull Fragment instantiate(@NotNull ClassLoader classLoader, @NotNull String className) {
        if (MuiConfigFragment.class.getName().equals(className)) {
            return new MuiConfigFragment();
        }
        return super.instantiate(classLoader, className);
    }
}