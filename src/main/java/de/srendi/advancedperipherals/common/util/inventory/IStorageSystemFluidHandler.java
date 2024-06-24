package de.srendi.advancedperipherals.common.util.inventory;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import org.jetbrains.annotations.NotNull;

public interface IStorageSystemFluidHandler extends Storage<FluidVariant> {
    default long insert(FluidStack stack, TransactionContext transaction) {
        long transferred = insert(stack.getType(), stack.getAmount(), transaction);
        stack.shrink(transferred);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasAborted()) {
                stack.grow(transferred);
            }
        });
        return transferred;
    }

    /**
     * Used to extract a type of fluid from the system via a peripheral.
     * Uses a filter to find the right fluid.
     *
     * @param filter The parsed filter
     * @param transaction The transaction this operation is part of.
     * @return extracted from the slot, must be empty if nothing can be extracted. The returned FluidStack can be safely modified after, so fluid handlers should return a new or copied stack.
     */
    @NotNull
    FluidStack extract(FluidFilter filter, TransactionContext transaction);
}
