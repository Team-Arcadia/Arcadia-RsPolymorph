package com.vyrriox.rspolymorph.fabric;

import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import com.vyrriox.rspolymorph.platform.NetworkPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric implementation of {@link NetworkPlatform}, resolved by the common code through
 * {@code ServiceLoader} (see {@code META-INF/services}).
 *
 * {@code ClientPlayNetworking.send} is client-only API, but this class is only ever instantiated
 * on the client: the sole caller is {@code RsGridRecipeWidget.selectRecipe}, which runs client-side.
 *
 * Author: vyrriox
 */
public final class FabricNetworkPlatform implements NetworkPlatform {

    @Override
    public void sendSelectToServer(ResourceLocation recipeId) {
        ClientPlayNetworking.send(new SelectRecipePacket(recipeId));
    }
}
