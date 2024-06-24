package de.srendi.advancedperipherals.common.util.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public interface IStorageSystemItemHandler extends Storage<ItemVariant> {
    default long insert(ItemStack stack, TransactionContext transaction) {
        long transferred = insert(ItemVariant.of(stack), stack.getCount(), transaction);
        stack.shrink((int) transferred);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasAborted()) {
                stack.grow((int) transferred);
            }
        });
        return transferred;
    }

    /**
     * Used to extract an item from the system via a peripheral.
     * Uses a filter to find the right item. The amount should never be greater than 64
     * stack sizes greater than 64.
     *
     * @param filter The parsed filter
     * @param transaction The transaction this operation is part of.
     * @return extracted from the slot, must be empty if nothing can be extracted. The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     */
    @NotNull
    ItemStack extract(ItemFilter filter, TransactionContext transaction);
}
