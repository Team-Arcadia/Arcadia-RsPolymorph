package com.vyrriox.rspolymorph;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

/**
 * Custom recipe data for RS2 Grids.
 * Author: vyrriox
 */
public class RsGridRecipeData extends AbstractBlockEntityRecipeData<BlockEntity> {

    /** Persisted recipe selection per RecipeType. */
    private final Map<RecipeType<?>, RecipeHolder<?>> selections = new HashMap<>();

    /**
     * Hash of the last recipe set pushed via updateRecipesList().
     * Used to skip redundant Polymorph sync packets when nothing changed.
     */
    private int lastRecipeListHash = 0;

    public RsGridRecipeData(BlockEntity owner) {
        super(owner);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the RecipeMatrix instances that belong to this block entity.
     *
     * Lock safety: we snapshot the CONTAINER_TO_BE map under its own lock, then
     * look up each container in CONTAINER_TO_MATRIX individually (each get() on a
     * synchronizedMap is atomic, so no outer lock needed).
     */
    private List<RecipeMatrix<?, ?>> getMatrices() {
        Map<RecipeMatrixContainer, BlockEntity> beMap = RsPolymorph.getMatrixMap();
        Map<RecipeMatrixContainer, RecipeMatrix<?, ?>> matrixMap = RsPolymorph.getContainerToMatrixMap();

        // Snapshot the containers owned by this BE under the beMap lock only.
        List<RecipeMatrixContainer> owned = new ArrayList<>();
        synchronized (beMap) {
            for (Map.Entry<RecipeMatrixContainer, BlockEntity> entry : beMap.entrySet()) {
                if (entry.getValue() == getOwner()) {
                    owned.add(entry.getKey());
                }
            }
        }

        // Each individual matrixMap.get() is thread-safe on a synchronizedMap.
        List<RecipeMatrix<?, ?>> result = new ArrayList<>(owned.size());
        for (RecipeMatrixContainer c : owned) {
            RecipeMatrix<?, ?> m = matrixMap.get(c);
            if (m != null) result.add(m);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // AbstractBlockEntityRecipeData overrides
    // -------------------------------------------------------------------------

    @Override
    protected NonNullList<ItemStack> getInput() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            RecipeMatrixContainer container = matrix.getMatrix();
            // Preserve slot positions so shaped recipes match correctly.
            for (int i = 0; i < container.getContainerSize(); i++) {
                stacks.add(container.getItem(i).copy());
            }
        }
        return stacks;
    }

    @Override
    public void selectRecipe(RecipeHolder<?> recipe) {
        if (recipe == null) return;
        selections.put(recipe.value().getType(), recipe);
        super.setSelectedRecipe(recipe);

        // Trigger RS2 to recalculate the grid output with the new selection.
        Level level = getOwner().getLevel();
        if (level != null && !level.isClientSide()) {
            for (RecipeMatrix<?, ?> matrix : getMatrices()) {
                if (((IRsRecipeMatrix<?, ?>) matrix).rspolymorph$getRecipeType() == recipe.value().getType()) {
                    matrix.updateResult(level);
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            if (!matrix.getMatrix().isEmpty()) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public RecipeHolder<?> getSelectedRecipe(RecipeType<?> type) {
        return selections.get(type);
    }

    /**
     * Forces server-side recipe detection and pushes the result to Polymorph's sync
     * mechanism (packet to listening players). Throttled: skips the packet if the
     * recipe list hasn't changed since the last push.
     */
    @SuppressWarnings("unchecked")
    public void forceUpdateRecipes(Level level) {
        if (level.isClientSide()) return;

        SortedSet<IRecipePair> pairs = new TreeSet<>();
        for (RecipeMatrix<?, ?> matrix : getMatrices()) {
            if (!(matrix instanceof IRsRecipeMatrix<?, ?> rsMatrix)) continue;
            if (matrix.getMatrix().isEmpty()) continue;

            RecipeInput input = (RecipeInput) rsMatrix.rspolymorph$getInputProvider().apply(matrix.getMatrix());
            if (input == null) continue;

            RecipeType<Recipe<RecipeInput>> type =
                    (RecipeType<Recipe<RecipeInput>>) rsMatrix.rspolymorph$getRecipeType();
            for (RecipeHolder<?> holder : level.getRecipeManager().getRecipesFor(type, input, level)) {
                ItemStack output = holder.value().getResultItem(level.registryAccess());
                pairs.add(new RsRecipePair(holder.id(), output));
            }
        }

        // Compute a hash of recipe IDs to detect whether the list actually changed.
        int newHash = 0;
        for (IRecipePair pair : pairs) {
            newHash = 31 * newHash + pair.getResourceLocation().hashCode();
        }

        if (newHash == lastRecipeListHash) return; // nothing changed — skip the packet
        lastRecipeListHash = newHash;

        // updateRecipesList is protected in AbstractRecipeData — stores + syncs to clients.
        updateRecipesList(pairs);
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    private static final class RsRecipePair implements IRecipePair {
        private final ResourceLocation id;
        private final ItemStack output;

        RsRecipePair(ResourceLocation id, ItemStack output) {
            this.id = id;
            this.output = output;
        }

        @Override public ItemStack getOutput() { return output; }
        @Override public ResourceLocation getResourceLocation() { return id; }
        @Override public int compareTo(IRecipePair other) { return id.compareTo(other.getResourceLocation()); }
    }
}
