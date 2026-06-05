package com.vyrriox.rspolymorph.fabric.client;

import com.vyrriox.rspolymorph.client.ClientSetup;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint. Registers the Polymorph grid widget on the client dist only —
 * the equivalent of the NeoForge {@code FMLEnvironment.dist.isClient()} guard.
 *
 * Author: vyrriox
 */
public final class RsPolymorphFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSetup.init();
    }
}
