package com.vyrriox.rspolymorph.neoforge;

import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import com.vyrriox.rspolymorph.platform.NetworkPlatform;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * NeoForge implementation of {@link NetworkPlatform}, resolved by the common code through
 * {@code ServiceLoader} (see {@code META-INF/services}).
 *
 * In NeoForge 26.x the client→server send moved from {@code PacketDistributor.sendToServer} to
 * the client-only {@code ClientPacketDistributor.sendToServer}. This class is only ever called
 * on the client (from {@code RsGridRecipeWidget.selectRecipe}).
 *
 * Author: vyrriox
 */
public final class NeoForgeNetworkPlatform implements NetworkPlatform {

    @Override
    public void sendSelectToServer(Identifier recipeId) {
        ClientPacketDistributor.sendToServer(new SelectRecipePacket(recipeId));
    }
}
