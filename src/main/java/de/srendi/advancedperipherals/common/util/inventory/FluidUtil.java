package de.srendi.advancedperipherals.common.util.inventory;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.addons.computercraft.owner.IPeripheralOwner;
import de.srendi.advancedperipherals.common.util.CoordUtil;
import de.srendi.advancedperipherals.common.util.StringUtil;
import net.minecraft.core.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.CloneNotSupportedException;
import java.util.Objects;

public class FluidUtil {

    private static final MessageDigest MD5 = MessageDigest.getInstance("MD5");
    private static final CompoundTag EMPTY_TAG = new CompoundTag();

    private FluidUtil() {
    }

    @Nullable
    private static Storage<FluidVariant> extractHandler(@Nullable Object object, @Nullable Direction direction) {
        if (object == null) {
            return null
        }
        if (object instanceof Storage<?> storage) {
            return InventoryUtil.checkStorageType(storage, FluidVariant.class) ? storage : null;
        }
        BlockPos pos = null;
        if (target instanceof BlockPos bpos) {
            pos = bpos;
        } else if (target instanceof IPeripheralOwner owner) {
            pos = owner.getPos();
        } else if (target instanceof BlockEntity block) {
            pos = block.getBlockPos();
        }
        if (pos != null) {
            return FluidStorage.SIDED.find(level, pos, direction);
        }
        return null;
    }

    @Nullable
    private static Storage<FluidVariant> extractHandler(@NotNull IPeripheral peripheral) {
        Object target = peripheral.getTarget();
        Direction direction = null;
        if (peripheral instanceof GenericPeripheral generic) {
            direction = generic.getSide().opposite();
        }
        return extractHandler(target, direction);
    }

    @NotNull
    public static Storage<FluidVariant> getStorageFromDirection(@NotNull String direction, @NotNull IPeripheralOwner owner) throws LuaException {
        Level level = owner.getLevel();
        Objects.requireNonNull(level);
        Direction relativeDirection = CoordUtil.getDirection(owner.getOrientation(), direction);
        BlockPos pos = owner.getPos().relative(relativeDirection);
        Storage<FluidVariant> fluidStorage = extractHandler(pos, relativeDirection.opposite());
        if (fluidStorage == null) {
            throw new LuaException("Target '" + direction + "' is empty or not a fluid handler");
        }
        return fluidStorage;
    }

    @Nullable
    public static Storage<FluidVariant> getStorageFromName(@NotNull IComputerAccess access, String name) throws LuaException {
        IPeripheral location = access.getAvailablePeripheral(name);

        // Tanks/Block Entities can't be accessed if the bridge is not exposed to the same network as the target tank/block entity
        // This can occur when the bridge was wrapped via a side and not via modems
        if (location == null) {
            return null;
        }

        Storage<FluidVariant> fluidStorage = extractHandler(location);
        if (fluidStorage == null) {
            throw new LuaException("Target '" + name + "' is not a fluid storage");
        }
        return fluidStorage;
    }

    @NotNull
    public static String getFingerprint(@NotNull FluidStack stack) {
        try {
            MessageDigest md = MD5.clone();
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                tag = EMPTY_TAG;
            }
            md.update(tag.toString().getBytes(StandardCharsets.UTF_8));
            md.update(getRegistryKey(stack).toString().getBytes(StandardCharsets.UTF_8));
            md.update(stack.getDisplayName().getString().getBytes(StandardCharsets.UTF_8));
            return StringUtil.toHexString(md.digest());
        } catch (CloneNotSupportedException ex) {
            AdvancedPeripherals.debug("Could not parse fingerprint.", org.apache.logging.log4j.Level.ERROR);
            ex.printStackTrace();
        }
        return "";
    }

    public static ResourceLocation getRegistryKey(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static ResourceLocation getRegistryKey(FluidStack fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid.getFluid());
    }
}
