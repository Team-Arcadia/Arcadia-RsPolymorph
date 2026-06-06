package com.vyrriox.rspolymorph.neoforge;

import com.vyrriox.rspolymorph.RsPolymorph;
import com.vyrriox.rspolymorph.TestItems;
import com.vyrriox.rspolymorph.platform.GridSelectionData;
import com.vyrriox.rspolymorph.network.SelectRecipePacket;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * NeoForge entrypoint for RS Polymorph. Registers the {@code selected_recipe} data component
 * (stamped on printed patterns), the per-grid selection {@code AttachmentType}, and the C2S
 * selection payload, then wires the shared {@link RsPolymorph} core. All gameplay logic lives
 * in the {@code common} module; this class only performs NeoForge-specific registration.
 *
 * Author: vyrriox
 */
@Mod(RsPolymorph.MOD_ID)
public final class RsPolymorphNeoForge {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RsPolymorph.MOD_ID);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SELECTED_RECIPE_COMPONENT =
            DATA_COMPONENT_TYPES.register("selected_recipe", () ->
                    DataComponentType.<ResourceLocation>builder()
                            .persistent(ResourceLocation.CODEC)
                            .build());

    // Per-grid recipe selection, persisted on the grid block entity's NBT via the attachment.
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RsPolymorph.MOD_ID);

    private static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<ResourceLocation, ResourceLocation>>> GRID_SELECTION =
            ATTACHMENT_TYPES.register("grid_selection", () ->
                    AttachmentType.builder(GridSelectionData::empty)
                            .serialize(GridSelectionData.CODEC)
                            .build());

    // Two debug items with identical crafting inputs (test_stick_1 / test_stick_2) to exercise
    // the recipe-selection popup. On 1.21.1 the registry id comes from the registry key only —
    // no Item.Properties.setId needed (that is a 1.21.2+ requirement, handled in the 26.1.2 build).
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RsPolymorph.MOD_ID);

    private static final DeferredHolder<Item, Item> TEST_STICK_1 =
            ITEMS.register("test_stick_1", () -> new Item(new Item.Properties()));
    private static final DeferredHolder<Item, Item> TEST_STICK_2 =
            ITEMS.register("test_stick_2", () -> new Item(new Item.Properties()));

    public RsPolymorphNeoForge(IEventBus modEventBus) {
        DATA_COMPONENT_TYPES.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::addCreativeTab);
    }

    private void addCreativeTab(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(TEST_STICK_1.get());
            event.accept(TEST_STICK_2.get());
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                SelectRecipePacket.TYPE,
                SelectRecipePacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SelectRecipePacket.applyOnServer(player, packet.recipeId());
                    }
                })
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("RS Polymorph (NeoForge) initializing...");

        // Inject the registered data component into the loader-agnostic core.
        RsPolymorph.setSelectedRecipeComponent(SELECTED_RECIPE_COMPONENT.get());

        // Bind the attachment-backed grid selection store used by NeoForgeGridRecipeStore.
        NeoForgeGridRecipeStore.bind(GRID_SELECTION);

        // Expose the debug items to the loader-agnostic core.
        TestItems.setTestStick1(TEST_STICK_1.get());
        TestItems.setTestStick2(TEST_STICK_2.get());

        // Client widget registration lives in a separate class to keep client-only type
        // references out of this class's constant pool / lambda methods on a dedicated server.
        if (FMLEnvironment.dist.isClient()) {
            com.vyrriox.rspolymorph.client.ClientSetup.init();
        }
    }
}
