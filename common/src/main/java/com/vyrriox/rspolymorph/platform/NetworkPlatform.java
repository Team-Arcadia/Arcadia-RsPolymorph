package com.vyrriox.rspolymorph.platform;

import net.minecraft.resources.ResourceLocation;

/**
 * Loader-agnostic networking abstraction.
 *
 * Only one direction is needed: the client tells the server which Polymorph recipe
 * the player selected. NeoForge implements this with {@code PacketDistributor}; Fabric
 * with {@code ClientPlayNetworking}. The payload type registration and the server-side
 * receiver are wired up by each loader's entrypoint.
 *
 * Author: vyrriox
 */
public interface NetworkPlatform {

    /**
     * Sends the recipe selection from the client to the server over the C2S channel.
     * Must be called on the client only.
     */
    void sendSelectToServer(ResourceLocation recipeId);
}
