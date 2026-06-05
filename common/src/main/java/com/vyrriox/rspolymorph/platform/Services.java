package com.vyrriox.rspolymorph.platform;

import java.util.ServiceLoader;

/**
 * Loads loader-specific service implementations from the common code without a
 * compile-time dependency on NeoForge or Fabric.
 *
 * Each loader subproject ships a {@code META-INF/services} entry pointing at its
 * implementation of {@link NetworkPlatform}. The common code resolves it lazily
 * via {@link ServiceLoader} the first time it is needed.
 *
 * Author: vyrriox
 */
public final class Services {

    private Services() {}

    public static final NetworkPlatform NETWORK = load(NetworkPlatform.class);

    public static final GridRecipeStore GRID_STORE = load(GridRecipeStore.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No RS Polymorph service implementation found for " + clazz.getName()
                                + " — the loader subproject must register one in META-INF/services."));
    }
}
