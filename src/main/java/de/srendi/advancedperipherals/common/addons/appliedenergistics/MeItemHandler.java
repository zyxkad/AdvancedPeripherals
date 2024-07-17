package de.srendi.advancedperipherals.common.addons.appliedenergistics;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import de.srendi.advancedperipherals.common.util.Pair;
import de.srendi.advancedperipherals.common.util.inventory.IStorageSystemItemHandler;
import de.srendi.advancedperipherals.common.util.inventory.ItemFilter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Used to transfer item between an inventory and the ME system.
 *
 * @see de.srendi.advancedperipherals.common.addons.computercraft.peripheral.MeBridgePeripheral
 */
public class MeItemHandler implements IStorageSystemItemHandler {

    @NotNull
    private final MEStorage storageMonitor;
    @NotNull
    private final IActionSource actionSource;

    public MeItemHandler(@NotNull MEStorage storageMonitor, @NotNull IActionSource actionSource) {
        this.storageMonitor = storageMonitor;
        this.actionSource = actionSource;
    }

    @NotNull
    @Override
    public long insert(@NotNull ItemVariant item, long maxCount, TransactionContext transaction) {
        AEItemKey itemKey = AEItemKey.of(item);
        if (itemKey == null) {
            return 0;
        }
        long inserted = storageMonitor.insert(itemKey, maxCount, Actionable.SIMULATE, actionSource);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasCommitted()) {
                // TODO: this is unsafe because we did not update the final inserted amount
                storageMonitor.insert(itemKey, inserted, Actionable.MODULATE, actionSource);
            }
        });
        return inserted;
    }

    @Override
    public ItemStack extract(ItemFilter filter, TransactionContext transaction) {
        Pair<Long, AEItemKey> itemKeys = AppEngApi.findAEStackFromFilter(storageMonitor, null, filter);
        if (itemKeys.getRight() == null) {
            return ItemStack.EMPTY;
        }
        AEItemKey itemKey = itemKeys.getRight();
        long extracted = storageMonitor.extract(itemKey, filter.getCount(), Actionable.SIMULATE, actionSource);
        ItemStack stack = itemKey.toStack(extracted);
        transaction.addCloseCallback((transaction, result) -> {
            if (result.wasCommitted()) {
                stack.setCount(storageMonitor.extract(itemKey, extracted, Actionable.MODULATE, actionSource));
            }
        });
        return stack;
    }

}
