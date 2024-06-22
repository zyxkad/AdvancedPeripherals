package de.srendi.advancedperipherals.common.util;

import de.srendi.advancedperipherals.common.blocks.blockentities.EnergyDetectorEntity;
import team.reborn.energy.api.EnergyStorage;

import java.util.Optional;

public class EnergyStorageProxy {

    private final InputSide inputSide = this.new InputSide();
    private final OutputSide outputSide = this.new OutputSide();
    protected final EnergyDetectorEntity energyDetector;
    private int maxTransferRate;
    protected int transferedInTick = 0;

    public EnergyStorageProxy(EnergyDetectorEntity energyDetector, int maxTransferRate) {
        this.energyDetector = energyDetector;
        this.maxTransferRate = maxTransferRate;
    }

    public long getEnergyStored() {
        Optional<EnergyStorage> out = energyDetector.getOutputStorage();
        return out.isEmpty() ? out.get().getAmount() : 0;
    }

    public long getMaxEnergyStored() {
        Optional<EnergyStorage> out = energyDetector.getOutputStorage();
        return out.isEmpty() ? out.get().getCapacity() : 0;
    }

    public int getMaxTransferRate() {
        return maxTransferRate;
    }

    public void setMaxTransferRate(int rate) {
        maxTransferRate = rate;
    }

    /**
     * should be called on every tick
     */
    public void resetTransferedInTick() {
        transferedInTick = 0;
    }

    public int getTransferedInTick() {
        return transferedInTick;
    }

    public EnergyStorage getInputSide() {
        return inputSide;
    }

    public EnergyStorage getOutputSide() {
        return outputSide;
    }

    private class InputSide implements EnergyStorage {
        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            long amountLeft = getMaxTransferRate() - transferedInTick;
            if (amountLeft <= 0) {
                return 0;
            }
            Optional<EnergyStorage> out = energyDetector.getOutputStorage();
            if (out.isEmpty()) {
                return 0;
            }
            long transferred = out.get().insert(Math.min(maxAmount, amountLeft), transaction);
            if (transferred == 0) {
                return 0;
            }
            transferedInTick += (int) transferred;
            transaction.addCloseCallback((transaction, result) -> {
                if (result == TransactionContext.Result.ABORTED) {
                    transferedInTick -= (int) transferred;
                }
            });
            return transferred;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            return getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return getMaxEnergyStored();
        }
    }

    private class OutputSide implements EnergyStorage {
        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            long amountLeft = getMaxTransferRate() - transferedInTick;
            if (amountLeft <= 0) {
                return 0;
            }
            Optional<EnergyStorage> in = energyDetector.getInputStorage();
            if (in.isEmpty()) {
                return 0;
            }
            long transferred = in.get().extract(Math.min(maxAmount, amountLeft), transaction);
            if (transferred == 0) {
                return 0;
            }
            transferedInTick += (int) transferred;
            transaction.addCloseCallback((transaction, result) -> {
                if (result == TransactionContext.Result.ABORTED) {
                    transferedInTick -= (int) transferred;
                }
            });
            return 0;
        }

        @Override
        public long getAmount() {
            return getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return getMaxEnergyStored();
        }
    }
}
