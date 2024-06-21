package de.srendi.advancedperipherals.common.util.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IStorageSystemItemHandler extends Container {

    @NotNull
    ItemStack insertItem(@NotNull ItemStack stack, boolean simulate);

    @Override
    default void setItem(int slot, ItemStack stack) {
        insertItem(stack, false);
    }

    /**
     * Used to extract an item from the system via a peripheral.
     * Uses a filter to find the right item. The amount should never be greater than 64
     * stack sizes greater than 64.
     *
     * @param filter The parsed filter
     * @param count The amount to extract
     * @param simulate Should this action be simulated
     * @return extracted from the slot, must be empty if nothing can be extracted. The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     */
    ItemStack extractItem(ItemFilter filter, int count, boolean simulate);

    /*
    These 4 methods are ignored in our transferring logic. Storage Systems do not respect slots and to extract we need a filter
     */

    @Override
    default int getContainerSize() {
        return 0;
    }

    @NotNull
    @Override
    default ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    default ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }
}
