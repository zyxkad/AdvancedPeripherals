package de.srendi.advancedperipherals.common.addons.computercraft.integrations;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.impl.Peripherals; // TODO: not public API?
import dan200.computercraft.shared.peripheral.generic.ComponentLookup;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.util.Platform;
import de.srendi.advancedperipherals.lib.integrations.IPeripheralIntegration;
import de.srendi.advancedperipherals.lib.peripherals.BlockEntityIntegrationPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
// import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

public class IntegrationPeripheralProvider implements ComponentLookup<?> {

    private static final String[] SUPPORTED_MODS = new String[]{"powah", "create", "mekanism", "botania"};

    private static final PriorityQueue<IPeripheralIntegration> integrations = new PriorityQueue<>(Comparator.comparingInt(IPeripheralIntegration::getPriority));

    private static void registerIntegration(IPeripheralIntegration integration) {
        integrations.add(integration);
    }

    public static void load() {
        ComputerCraftAPI.registerGenericSource(new BeaconIntegration());
        registerIntegration(new BlockIntegration(NoteBlockIntegration::new, NoteBlock.class::isInstance));

        for (String mod : SUPPORTED_MODS) {
            Optional<Object> integration = Platform.maybeLoadIntegration(mod, mod + ".Integration");
            integration.ifPresent(obj -> {
                AdvancedPeripherals.LOGGER.warn("Successfully loaded integration for {}", mod);
                ((Runnable) obj).run();
            });
            if (integration.isEmpty()) AdvancedPeripherals.LOGGER.warn("Failed to load integration for {}", mod);
        }
        Peripherals.addGenericLookup(new IntegrationPeripheralProvider());
    }

    @Nullable
    @Override
    public Object find(ServerLevel level, BlockPos blockPos, BlockState state, BlockEntity blockEntity, Direction direction, Runnable invalidate) {
        for (IPeripheralIntegration integration : integrations) {
            if (integration.isSuitable(level, blockPos, direction)) {
                return integration.buildPeripheral(level, blockPos, direction);
            }
        }
        return null;
    }
}
