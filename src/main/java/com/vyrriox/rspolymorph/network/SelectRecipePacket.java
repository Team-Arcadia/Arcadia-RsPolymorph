package com.vyrriox.rspolymorph.network;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.vyrriox.rspolymorph.RsGridRecipeData;
import com.vyrriox.rspolymorph.RsPolymorph;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Client → Server packet to communicate Polymorph recipe selection to the dedicated server.
 *
 * In singleplayer the JVM is shared, so the client can directly schedule work on the
 * integrated server thread (see RsGridRecipeWidget.selectRecipe). On a dedicated server
 * the client must send this packet instead.
 *
 * Author: vyrriox
 */
public record SelectRecipePacket(ResourceLocation recipeId) implements CustomPacketPayload {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Type<SelectRecipePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RsPolymorph.MOD_ID, "select_recipe"));

    public static final StreamCodec<FriendlyByteBuf, SelectRecipePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    SelectRecipePacket::recipeId,
                    SelectRecipePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler. Looks up the recipe by ID and applies it to the BlockEntity
     * backing the player's currently open crafting grid.
     */
    public static void handleOnServer(SelectRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Level level = player.level();
            Optional<RecipeHolder<?>> recipeOpt = level.getRecipeManager().byKey(packet.recipeId());
            if (recipeOpt.isEmpty()) {
                LOGGER.warn("[RS Polymorph] Server received unknown recipe ID: {}", packet.recipeId());
                return;
            }
            RecipeHolder<?> recipe = recipeOpt.get();

            // Scan the player's open container menu for a RecipeMatrixContainer slot.
            AbstractContainerMenu menu = player.containerMenu;
            for (Slot slot : menu.slots) {
                if (!(slot.container instanceof RecipeMatrixContainer rmc)) continue;
                BlockEntity be = RsPolymorph.getBlockEntityForContainer(rmc);
                if (be == null) continue;

                var data = PolymorphApi.getInstance().getBlockEntityRecipeData(be);
                if (data instanceof RsGridRecipeData rsData) {
                    rsData.selectRecipe(recipe);
                    return; // Only update the first matching BE
                }
            }

            LOGGER.warn("[RS Polymorph] Could not find target BlockEntity for recipe selection (player: {})", player.getName().getString());
        });
    }
}
