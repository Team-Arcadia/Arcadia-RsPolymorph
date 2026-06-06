package com.vyrriox.rspolymorph.fabric;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.vyrriox.rspolymorph.TestItems;
import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric entrypoint for RS Polymorph. Mirrors {@code RsPolymorphNeoForge}: registers the
 * {@code selected_recipe} data component, the C2S selection payload and its server receiver,
 * and wires the shared {@link RsPolymorph} core. All gameplay logic lives in {@code common}.
 *
 * Author: vyrriox
 */
public final class RsPolymorphFabric implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("RS Polymorph (Fabric) initializing...");

        // Register the selected_recipe data component under the same id as NeoForge.
        DataComponentType<Identifier> component = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                RsPolymorph.SELECTED_RECIPE_COMPONENT_ID,
                DataComponentType.<Identifier>builder()
                        .persistent(Identifier.CODEC)
                        .build());
        RsPolymorph.setSelectedRecipeComponent(component);

        // Register the C2S payload type and its server-side receiver.
        PayloadTypeRegistry.playC2S().register(SelectRecipePacket.TYPE, SelectRecipePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SelectRecipePacket.TYPE, (payload, context) ->
                context.server().execute(() ->
                        SelectRecipePacket.applyOnServer(context.player(), payload.recipeId())));

        // Touch the grid store so its persistent attachment is registered at startup (the Fabric
        // attachment is created in FabricGridRecipeStore's class initializer).
        com.vyrriox.rspolymorph.platform.Services.GRID_STORE.getClass();

        // Two debug items with identical crafting inputs. MC 26.x requires Item.Properties#setId;
        // Fabric has no auto-setId helper, so set it manually from the registry key.
        Item s1 = registerItem("test_stick_1");
        Item s2 = registerItem("test_stick_2");
        TestItems.setTestStick1(s1);
        TestItems.setTestStick2(s2);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(s1);
            entries.accept(s2);
        });
    }

    private static Item registerItem(String path) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(RsPolymorph.MOD_ID, path));
        return Registry.register(BuiltInRegistries.ITEM, key.location(),
                new Item(new Item.Properties().setId(key)));
    }
}
