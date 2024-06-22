package de.srendi.advancedperipherals.common.blocks.base;

import team.reborn.energy.api.EnergyStorage;

import org.jetbrains.annotations.Nullable;

public interface IEnergyStorageBlock {
	@Nullable
	EnergyStorage getEnergyStorage(@Nullable Direction direction);
}
