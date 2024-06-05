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
        if (object instanceof Container itemHandler)
            return itemHandler;
        return null;
    }

    // TODO: need refactor
    public static int moveItem(Container inventoryFrom, Container inventoryTo, ItemFilter filter) {
        if (inventoryFrom == null) return 0;

        int fromSlot = filter.getFromSlot();
        int toSlot = filter.getToSlot();

        int amount = filter.getCount();
        int transferableAmount = 0;

        // The logic changes with storage systems since these systems do not have slots
        if (inventoryFrom instanceof IStorageSystemItemHandler storageSystemHandler) {
            ItemStack extracted = storageSystemHandler.extractItem(filter, filter.getCount(), true);
            if (extracted.isEmpty()) {
                continue;
            }
            if (toSlot == -1) {
                insertItem(inventoryTo, extracted);
            } else {
                insertItem(inventoryTo, extracted, toSlot);
            }
            amount -= extracted.getCount();
            transferableAmount += storageSystemHandler.extractItem(filter, extracted.getCount(), false).getCount();
            if (transferableAmount >= filter.getCount()) {
                break;
            }
            return transferableAmount;
        }

        if (inventoryTo instanceof IStorageSystemItemHandler storageSystemHandler) {
            for (int i = fromSlot == -1 ? 0 : fromSlot; i < (fromSlot == -1 ? inventoryFrom.getSlots() : fromSlot + 1); i++) {
                if (filter.test(inventoryFrom.getStackInSlot(i))) {
                    ItemStack extracted = inventoryFrom.extractItem(i, amount - transferableAmount, true);
                    if (extracted.isEmpty()) {
                        continue;
                    }
                    int inserted = insertItem(storageSystemHandler, extracted, toSlot);
                    amount -= extracted.getCount();
                    transferableAmount += inventoryFrom.extractItem(i, inserted, false).getCount();
                    if (transferableAmount >= filter.getCount()) {
                        break;
                    }
                }
            }
            return transferableAmount;
        }

        for (int i = fromSlot == -1 ? 0 : fromSlot; i < (fromSlot == -1 ? inventoryFrom.getSlots() : fromSlot + 1); i++) {
            if (filter.test(inventoryFrom.getStackInSlot(i))) {
                ItemStack extracted = inventoryFrom.extractItem(i, amount - transferableAmount, true);
                if (extracted.isEmpty()) {
                    continue;
                }
                int inserted;
                if (toSlot == -1) {
                    inserted = insertItem(inventoryTo, extracted);
                } else {
                    inserted = insertItem(inventoryTo, extracted, toSlot);
                }
                amount -= extracted.getCount();
                transferableAmount += inventoryFrom.extractItem(i, inserted, false).getCount();
                if (transferableAmount >= filter.getCount()) {
                    break;
                }
            }
        }
        return transferableAmount;
    }

    private static int insertItem(Container inv, ItemStack stack) {
        int transfered = 0;
        for (int slot = 0; slot < inv.getContainerSize() && stack.getCount() > 0; slot++) {
            transfered += insertItem(inv, stack, slot);
        }
        return transfered;
    }

    private static int insertItem(Container inv, ItemStack stack, int slot) {
        ItemStack i = inv.getItem(slot);
        if (i == ItemStack.EMPTY || i.getItem() == stack.getItem()) {
            int count = Math.min(stack.getCount(), stack.getMaxStackSize() - i.getCount());
            inv.setItem(slot, stack.copyWithCount(count));
            stack.shrink(count);
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
        if (location == null)
            return null;

        return extractHandler(location.getTarget());
    }

    @Nullable
    public static Container getHandlerFromDirection(@NotNull String direction, @NotNull IPeripheralOwner owner) throws LuaException {
        Level level = owner.getLevel();
        Objects.requireNonNull(level);
        Direction relativeDirection = CoordUtil.getDirection(owner.getOrientation(), direction);
        BlockEntity target = level.getBlockEntity(owner.getPos().relative(relativeDirection));
        if (target == null)
            return null;

        return extractHandler(target);
    }
}
