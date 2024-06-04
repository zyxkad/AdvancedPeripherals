package de.srendi.advancedperipherals.common.setup;

import dan200.computercraft.shared.platform.RegistrationHelper;
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser;
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.shared.platform.PlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Registration {
    public static final RegistrationHelper<Block> BLOCKS = PlatformHelper.get().createRegistrationHelper(Registries.BLOCK);
    public static final RegistrationHelper<Item> ITEMS = PlatformHelper.get().createRegistrationHelper(Registries.ITEM);
    public static final RegistrationHelper<BlockEntityType<?>> TILE_ENTITIES = PlatformHelper.get().createRegistrationHelper(Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrationHelper<MenuType<?>> CONTAINER_TYPES = PlatformHelper.get().createRegistrationHelper(Registries.MENU_TYPE);
    public static final RegistrationHelper<PoiType> POI_TYPES = PlatformHelper.get().createRegistrationHelper(Registries.POI_TYPE);
    public static final RegistrationHelper<VillagerProfession> VILLAGER_PROFESSIONS = PlatformHelper.get().createRegistrationHelper(Registries.VILLAGER_PROFESSION);
    public static final RegistrationHelper<TurtleUpgradeSerialiser<?>> TURTLE_SERIALIZER = PlatformHelper.get().createRegistrationHelper(TurtleUpgradeSerialiser.registryId());
    public static final RegistrationHelper<PocketUpgradeSerialiser<?>> POCKET_SERIALIZER = PlatformHelper.get().createRegistrationHelper(PocketUpgradeSerialiser.registryId());
    public static final RegistrationHelper<CreativeModeTab> CREATIVE_MODE_TABS = PlatformHelper.get().createRegistrationHelper(Registries.CREATIVE_MODE_TAB);

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        TILE_ENTITIES.register();
        CONTAINER_TYPES.register();
        POI_TYPES.register();
        VILLAGER_PROFESSIONS.register();
        TURTLE_SERIALIZER.register();
        POCKET_SERIALIZER.register();
        CREATIVE_MODE_TABS.register();

        Blocks.register();
        BlockEntityTypes.register();
        Items.register();
        ContainerTypes.register();
        Villagers.register();
        CCRegistration.register();
        CreativeTabs.register();
    }
}
