package com.vyrriox.rspolymorph.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared data shape + codec for a per-grid recipe selection, used by both loaders' attachment
 * implementations. The selection is a small map of {@code recipeTypeId -> recipeId}.
 *
 * {@link RecipeType} has no built-in codec, so it is keyed by its registry id via
 * {@link BuiltInRegistries#RECIPE_TYPE}. Unresolved type ids are skipped on read.
 *
 * Author: vyrriox
 */
public final class GridSelectionData {

    private GridSelectionData() {}

    /** typeId -> recipeId. Loader-agnostic, persisted through each loader's attachment serializer. */
    public static final Codec<Map<ResourceLocation, ResourceLocation>> CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC);

    /** Stable resource id shared by both loaders' attachment registration. */
    public static final ResourceLocation ATTACHMENT_ID =
            ResourceLocation.fromNamespaceAndPath("rspolymorph", "grid_selection");

    /** Maps a RecipeType to its registry id, or {@code null} if it is not registered. */
    public static ResourceLocation typeId(RecipeType<?> type) {
        return BuiltInRegistries.RECIPE_TYPE.getKey(type);
    }

    /** Creates an empty selection map (the attachment's default value). */
    public static Map<ResourceLocation, ResourceLocation> empty() {
        return new HashMap<>();
    }
}
