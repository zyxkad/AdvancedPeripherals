package de.srendi.advancedperipherals.common.blocks;

import de.srendi.advancedperipherals.common.blocks.base.APBlockEntityBlock;
import de.srendi.advancedperipherals.common.blocks.blockentities.EnergyDetectorEntity;
import de.srendi.advancedperipherals.common.setup.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyDetectorBlock extends APBlockEntityBlock<EnergyDetectorEntity> {

    public EnergyDetectorBlock() {
        super(BlockEntityTypes.ENERGY_DETECTOR, true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return BlockEntityTypes.ENERGY_DETECTOR.get().create(pos, state);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        if (level.getBlockEntity(pos) instanceof EnergyDetectorEntity energyDetector) {
            energyDetector.invalidateStorages();
        }
    }

}
