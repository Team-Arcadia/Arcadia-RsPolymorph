package com.vyrriox.rspolymorph.neoforge;

import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import com.vyrriox.rspolymorph.platform.NetworkPlatform;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * NeoForge implementation of {@link NetworkPlatform}, resolved by the common code through
 * {@code ServiceLoader} (see {@code META-INF/services}).
 *
 * Author: vyrriox
 */
public final class NeoForgeNetworkPlatform implements NetworkPlatform {

    @Override
    public void sendSelectToServer(ResourceLocation recipeId) {
        PacketDistributor.sendToServer(new SelectRecipePacket(recipeId));
    }
}
