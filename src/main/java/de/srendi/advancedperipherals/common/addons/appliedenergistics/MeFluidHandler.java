package de.srendi.advancedperipherals.common.addons.appliedenergistics;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;
import de.srendi.advancedperipherals.common.util.Pair;
import de.srendi.advancedperipherals.common.util.inventory.FluidFilter;
import de.srendi.advancedperipherals.common.util.inventory.IStorageSystemFluidHandler;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import org.jetbrains.annotations.NotNull;

/**
 * Used to transfer item between an inventory and the ME system.
 * @see de.srendi.advancedperipherals.common.addons.computercraft.peripheral.MeBridgePeripheral
 */
public class MeFluidHandler implements IStorageSystemFluidHandler {

    @NotNull
    private final MEStorage storageMonitor;
    @NotNull
    private final IActionSource actionSource;

    public MeFluidHandler(@NotNull MEStorage storageMonitor, @NotNull IActionSource actionSource) {
        this.storageMonitor = storageMonitor;
        this.actionSource = actionSource;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        AEFluidKey fluidKey = AEFluidKey.of(resource);
        if (fluidKey == null) {
            return 0;
        }
        long inserted = storageMonitor.insert(fluidKey, maxAmount, Actionable.SIMULATE, actionSource);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasCommitted()) {
                // TODO: this is unsafe because we did not update the final inserted amount
                storageMonitor.insert(fluidKey, inserted, Actionable.MODULATE, actionSource);
            }
        });
        return inserted;
    }

    @NotNull
    @Override
    public FluidStack extract(FluidFilter filter, TransactionContext transaction) {
        Pair<Long, AEFluidKey> fluidKeys = AppEngApi.findAEFluidFromFilter(storageMonitor, null, filter);
        if(fluidKeys == null) {
            return FluidStack.EMPTY;
        }
        AEFluidKey fluidKey = fluidKey.getRight();
        long extracted = storageMonitor.extract(fluidKey, filter.getCount(), Actionable.SIMULATE, actionSource);
        FluidStack stack = new FluidStack(fluidKey.toVariant(), extracted);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasCommitted()) {
                stack.setAmount(storageMonitor.extract(fluidKey, extracted, Actionable.MODULATE, actionSource));
            }
        });
        return stack;
    }
}
