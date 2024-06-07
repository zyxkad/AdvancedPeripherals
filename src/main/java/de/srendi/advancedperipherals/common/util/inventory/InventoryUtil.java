package de.srendi.advancedperipherals.common.util.inventory;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.IPeripheralOwner;
import de.srendi.advancedperipherals.common.util.CoordUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InventoryUtil {

    private InventoryUtil() {
    }

    public static Container extractHandler(@Nullable Object object) {
        if (object instanceof Container itemHandler) {
            return itemHandler;
        }
        return null;
    }

    public static int moveItem(Container invFrom, Container invTo, ItemFilter filter) {
        if (invFrom == null || invTo == null) {
            return 0;
        }

        int fromSlot = filter.getFromSlot();
        int toSlot = filter.getToSlot();

        int requireAmount = filter.getCount();
        int transferableAmount = 0;

        if (invFrom instanceof IStorageSystemItemHandler storageFrom) {
            if (invTo instanceof IStorageSystemItemHandler storageTo) {
                return moveItemStorage2Storage(storageFrom, storageTo, filter);
            }
            return moveItemStorage2Container(storageFrom, invTo, filter);
        }

        if (fromSlot != -1) {
            if (!filter.test(invFrom.getItem(fromSlot))) {
                return;
            }
        }

        if (invTo instanceof IStorageSystemItemHandler storageTo) {
            if (fromSlot != -1) {
                return moveItemContainer2Storage(invFrom, storageTo, requireAmount, fromSlot);
            }
            for (int i = 0; i < invFrom.getContainerSize(); i++) {
                if (filter.test(inventoryFrom.getStackInSlot(i))) {
                    transferableAmount += moveItemContainer2Storage(invFrom, storageTo, requireAmount - transferableAmount, i);
                }
            }
            return transferableAmount;
        }

        if (fromSlot != -1) {
            return moveItemContainer2Container(invFrom, invTo, requireAmount, fromSlot, toSlot);
        }
        for (int i = 0; i < invFrom.getContainerSize(); i++) {
            if (filter.test(invFrom.getItem(i))) {
                transferableAmount += moveItemContainer2Container(invFrom, invTo, requireAmount - transferableAmount, i, toSlot);
            }
        }
        return transferableAmount;
    }

    private static int moveItemStorage2Storage(IStorageSystemItemHandler invFrom, IStorageSystemItemHandler invTo, ItemFilter filter) {
        int requireAmount = filter.getCount();
        int transferableAmount = 0;
        while (requireAmount > transferableAmount) {
            ItemStack extracted = invFrom.extractItem(filter, requireAmount - transferableAmount, true);
            if (extracted.isEmpty()) {
                break;
            }
            ItemFilter extractedFilter = ItemFilter.fromStack(extracted);
            int inserted = invTo.insertItem(extracted, false);
            int transfered = invFrom.extractItem(extractedFilter, inserted, false).getCount();
            transferableAmount += transfered;
            if (transfered < inserted) {
                // should Technically not happen?
                invTo.extractItem(extractedFilter, inserted - transfered, false);
            }
        }
        return transferableAmount;
    }

    private static int moveItemStorage2Container(IStorageSystemItemHandler invFrom, Container invTo, ItemFilter filter) {
        int toSlot = filter.getToSlot();
        int requireAmount = filter.getCount();
        int transferableAmount = 0;
        while (requireAmount > transferableAmount) {
            ItemStack extracted = invFrom.extractItem(filter, requireAmount - transferableAmount, true);
            if (extracted.isEmpty()) {
                break;
            }
            ItemFilter extractedFilter = ItemFilter.fromStack(extracted);
            int inserted;
            if (toSlot == -1) {
                inserted = insertItem(invTo, extracted, false);
            } else {
                inserted = insertItem(invTo, extracted, toSlot, false);
            }
            if (inserted == 0) {
                break;
            }
            int transfered = invFrom.extractItem(extractedFilter, inserted, false).getCount();
            transferableAmount += transfered;
            if (transfered < inserted) {
                // should Technically not happen?
                invTo.extractItem(extractedFilter, inserted - transfered, false);
            }
        }
        return transferableAmount;
    }

    private static int moveItemContainer2Storage(Container invFrom, IStorageSystemItemHandler invTo, int requireAmount, int fromSlot) {
        ItemStack extracted = invFrom.getItem(fromSlot);
        if (extracted.isEmpty()) {
            return 0;
        }
        int transfered = invTo.insertItem(extracted.copyWithCount(Math.min(requireAmount, extracted.getCount())), false);
        extracted.shrink(transfered);
        return transfered;
    }

    private static int moveItemContainer2Container(Container invFrom, Container invTo, int requireAmount, int fromSlot, int toSlot) {
        ItemStack extracted = invFrom.getItem(fromSlot);
        if (extracted.isEmpty()) {
            return 0;
        }
        ItemStack inserting = extracted.copyWithCount(Math.min(requireAmount, extracted.getCount()));
        int inserted;
        if (toSlot == -1) {
            inserted = insertItem(inventoryTo, inserting, false);
        } else {
            inserted = insertItem(inventoryTo, inserting, toSlot, false);
        }
        extracted.shrink(inserted);
        return inserted;
    }

    private static int insertItem(Container inv, ItemStack stack, boolean simulate) {
        int transfered = 0;
        for (int slot = 0; slot < inv.getContainerSize() && stack.getCount() > 0; slot++) {
            transfered += insertItem(inv, stack, slot, simulate);
        }
        return transfered;
    }

    private static int insertItem(Container inv, ItemStack stack, int slot, boolean simulate) {
        ItemStack i = inv.getItem(slot);
        if (i == ItemStack.EMPTY || i.getItem() == stack.getItem()) {
            int count = Math.min(stack.getCount(), stack.getMaxStackSize() - i.getCount());
            if (!simulate) {
                inv.setItem(slot, stack.copyWithCount(i.getCount() + count));
                stack.shrink(count);
            }
            return count;
        }
        return 0;
    }

    // public static int moveFluid(IFluidHandler inventoryFrom, IFluidHandler inventoryTo, FluidFilter filter) {
    //     if (inventoryFrom == null) return 0;

    //     int amount = filter.getCount();
    //     int transferableAmount = 0;

    //     // The logic changes with storage systems since these systems do not have slots
    //     if (inventoryFrom instanceof IStorageSystemFluidHandler storageSystemHandler) {
    //         FluidStack extracted = storageSystemHandler.drain(filter, IFluidHandler.FluidAction.SIMULATE);
    //         int inserted = inventoryTo.fill(extracted, IFluidHandler.FluidAction.EXECUTE);

    //         transferableAmount += storageSystemHandler.drain(filter.setCount(inserted), IFluidHandler.FluidAction.EXECUTE).getAmount();

    //         return transferableAmount;
    //     }

    //     if (inventoryTo instanceof IStorageSystemFluidHandler storageSystemHandler) {
    //         if (filter.test(inventoryFrom.getFluidInTank(0))) {
    //             FluidStack toExtract = inventoryFrom.getFluidInTank(0).copy();
    //             toExtract.setAmount(amount);
    //             FluidStack extracted = inventoryFrom.drain(toExtract, IFluidHandler.FluidAction.SIMULATE);
    //             if (extracted.isEmpty())
    //                 return 0;
    //             int inserted = storageSystemHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);

    //             extracted.setAmount(inserted);
    //             transferableAmount += inventoryFrom.drain(extracted, IFluidHandler.FluidAction.EXECUTE).getAmount();
    //         }

    //         return transferableAmount;
    //     }

    //     return transferableAmount;
    // }


    @Nullable
    public static Container getHandlerFromName(@NotNull IComputerAccess access, String name) throws LuaException {
        IPeripheral location = access.getAvailablePeripheral(name);
        if (location == null) {
            return null;
        }

        return extractHandler(location.getTarget());
    }

    @Nullable
    public static Container getHandlerFromDirection(@NotNull String direction, @NotNull IPeripheralOwner owner) throws LuaException {
        Level level = owner.getLevel();
        Objects.requireNonNull(level);
        Direction relativeDirection = CoordUtil.getDirection(owner.getOrientation(), direction);
        BlockEntity target = level.getBlockEntity(owner.getPos().relative(relativeDirection));
        if (target == null) {
            return null;
        }

        return extractHandler(target);
    }
}
