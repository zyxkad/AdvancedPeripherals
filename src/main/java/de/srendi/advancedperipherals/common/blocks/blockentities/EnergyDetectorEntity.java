package de.srendi.advancedperipherals.common.blocks.blockentities;

import de.srendi.advancedperipherals.common.addons.computercraft.peripheral.EnergyDetectorPeripheral;
import de.srendi.advancedperipherals.common.blocks.base.IEnergyStorageBlock;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralBlockEntity;
import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.common.setup.BlockEntityTypes;
import de.srendi.advancedperipherals.common.util.EnergyStorageProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EnergyDetectorEntity extends PeripheralBlockEntity<EnergyDetectorPeripheral> implements IEnergyStorageBlock {

    private int currentTransferRate = 0;
    // storageProxy that will forward the energy to the output but limit it to maxTransferRate
    private EnergyStorageProxy storageProxy = new EnergyStorageProxy(this, APConfig.PERIPHERALS_CONFIG.energyDetectorMaxFlow.get());
    private Direction energyInDirection = Direction.NORTH;
    private Direction energyOutDirection = Direction.SOUTH;
    @NotNull
    private Optional<EnergyStorage> inputSideStorage = Optional.empty();
    @NotNull
    private Optional<EnergyStorage> outputSideStorage = Optional.empty();

    public EnergyDetectorEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypes.ENERGY_DETECTOR.get(), pos, state);
    }

    @NotNull
    @Override
    protected EnergyDetectorPeripheral createPeripheral() {
        return new EnergyDetectorPeripheral(this);
    }

    public int getCurrentTransferRate() {
        return currentTransferRate;
    }

    @Override
    @Nullable
    public EnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == energyInDirection) {
            return storageProxy.getInputSide();
        }
        if (direction == energyOutDirection) {
            return storageProxy.getOutputSide();
        }
        return null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("rateLimit", storageProxy.getMaxTransferRate());
    }

    @Override
    public <T extends BlockEntity> void handleTick(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            // this handles the rare edge case that receiveEnergy is called multiple times in one tick
            currentTransferRate = storageProxy.getTransferedInTick();
            storageProxy.resetTransferedInTick();
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        storageProxy.setMaxTransferRate(nbt.getInt("rateLimit"));
        super.deserializeNBT(nbt);
    }

    public void invalidateStorages() {
        inputSideStorage = Optional.empty();
        outputSideStorage = Optional.empty();
    }

    /**
     * @return the cached input side energyStorage of the receiving block or refetches it if it has been invalidated
     */
    @NotNull
    public Optional<EnergyStorage> getInputStorage() {
        if (inputSideStorage.isEmpty()) {
            EnergyStorage storage = EnergyStorage.SIDED.find(level, worldPosition.offset(energyOutDirection), energyOutDirection.getOpposite());
            if (storage != null) {
                inputSideStorage = Optional.of(storage);
            }
        }
        return inputSideStorage;
    }

    /**
     * @return the cached output side energyStorage of the receiving block or refetches it if it has been invalidated
     */
    @NotNull
    public Optional<EnergyStorage> getOutputStorage() {
        if (outputSideStorage.isEmpty()) {
            EnergyStorage storage = EnergyStorage.SIDED.find(level, worldPosition.offset(energyOutDirection), energyOutDirection.getOpposite());
            if (storage != null) {
                outputSideStorage = Optional.of(storage);
            }
        }
        return outputSideStorage;
    }
}
