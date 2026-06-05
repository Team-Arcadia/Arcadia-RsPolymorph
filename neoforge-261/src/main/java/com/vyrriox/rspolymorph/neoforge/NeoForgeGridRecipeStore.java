package com.vyrriox.rspolymorph.neoforge;

import com.vyrriox.rspolymorph.platform.GridRecipeStore;
import com.vyrriox.rspolymorph.platform.GridSelectionData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * NeoForge implementation of {@link GridRecipeStore} backed by a serializable
 * {@link AttachmentType} on the grid block entity. Resolved by the common code via
 * {@code ServiceLoader} (see {@code META-INF/services}).
 *
 * The attachment instance is registered by {@link RsPolymorphNeoForge} and supplied here.
 *
 * Author: vyrriox
 */
public final class NeoForgeGridRecipeStore implements GridRecipeStore {

    /** Set once by the entrypoint after the DeferredRegister has registered the type. */
    private static Supplier<AttachmentType<Map<Identifier, Identifier>>> attachment;

    static void bind(Supplier<AttachmentType<Map<Identifier, Identifier>>> type) {
        attachment = type;
    }

    @Override
    public Identifier get(BlockEntity be, RecipeType<?> type) {
        if (be == null || attachment == null) return null;
        Identifier typeId = GridSelectionData.typeId(type);
        if (typeId == null) return null;
        Map<Identifier, Identifier> map = be.getData(attachment.get());
        return map.get(typeId);
    }

    @Override
    public void set(BlockEntity be, RecipeType<?> type, Identifier recipeId) {
        if (be == null || attachment == null) return;
        Identifier typeId = GridSelectionData.typeId(type);
        if (typeId == null) return;

        Map<Identifier, Identifier> map = be.getData(attachment.get());
        if (recipeId == null) {
            map.remove(typeId);
        } else {
            map.put(typeId, recipeId);
        }
        // getData returns the live attached map; mark the BE dirty so the change is saved.
        be.setChanged();
    }
}
