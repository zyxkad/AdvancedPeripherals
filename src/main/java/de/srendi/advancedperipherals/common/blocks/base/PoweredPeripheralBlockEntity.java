package de.srendi.advancedperipherals.common.blocks.base;

import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.lib.peripherals.BasePeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PoweredPeripheralBlockEntity<T extends BasePeripheral<?>> extends PeripheralBlockEntity<T> implements IEnergyStorageBlock {

    private SimpleEnergyStorage energyStorage = null;

    public PoweredPeripheralBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    protected abstract int getMaxEnergyStored();

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        if (energyStorage != null) {
            compound.putInt("energy", energyStorage.getAmount());
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        if (APConfig.PERIPHERALS_CONFIG.enablePoweredPeripherals.get()) {
            int maxEnergy = this.getMaxEnergyStored();
            energyStorage = new SimpleEnergyStorage(maxEnergy, maxEnergy, maxEnergy);
            energyStorage.amount = Math.min(maxEnergy, compound.getInt("energy"));
        }
    }

    @Override
    @Nullable
    public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return energyStorage;
    }
}
