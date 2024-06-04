package de.srendi.advancedperipherals.common.setup;

import de.srendi.advancedperipherals.common.container.InventoryManagerContainer;
import de.srendi.advancedperipherals.shared.platform.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;

public class ContainerTypes {
    public static final RegistryEntry<MenuType<InventoryManagerContainer>> INVENTORY_MANAGER_CONTAINER = Registration.CONTAINER_TYPES.register("memory_card_container", () -> PlatformHelper.get().createMenuType((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        Level level = inv.player.getCommandSenderWorld();
        return new InventoryManagerContainer(windowId, inv, pos, level);
    }));

    public static void register() {
    }
}
