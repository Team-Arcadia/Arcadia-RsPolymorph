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

    /**
     * The grid-selection persistence store. Server-safe (no client-only references), so it is
     * eagerly resolved — touching it at server startup is how each loader registers its attachment.
     */
    public static final GridRecipeStore GRID_STORE = load(GridRecipeStore.class);

    /**
     * The networking platform. Resolved LAZILY (initialization-on-demand holder) because the Fabric
     * implementation references client-only {@code ClientPlayNetworking}; eagerly loading it
     * alongside {@code GRID_STORE} would risk a {@code NoClassDefFoundError} on a dedicated server.
     * Only the client send path ({@code RsGridRecipeWidget.selectRecipe}) ever calls this.
     */
    public static NetworkPlatform network() {
        return NetworkHolder.INSTANCE;
    }

    private static final class NetworkHolder {
        private static final NetworkPlatform INSTANCE = load(NetworkPlatform.class);
    }

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No RS Polymorph service implementation found for " + clazz.getName()
                                + " — the loader subproject must register one in META-INF/services."));
    }
}
