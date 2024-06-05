package de.srendi.advancedperipherals.common.setup;

import com.google.common.collect.Sets;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralBlockEntity;
import de.srendi.advancedperipherals.common.blocks.blockentities.*;
import de.srendi.advancedperipherals.shared.platform.RegistryEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityTypes {
    public static final RegistryEntry<BlockEntityType<ChatBoxEntity>> CHAT_BOX = Registration.TILE_ENTITIES.register("chat_box", () -> new BlockEntityType<>(ChatBoxEntity::new, Sets.newHashSet(Blocks.CHAT_BOX.get()), null));
    public static final RegistryEntry<BlockEntityType<EnvironmentDetectorEntity>> ENVIRONMENT_DETECTOR = Registration.TILE_ENTITIES.register("environment_detector", () -> new BlockEntityType<>(EnvironmentDetectorEntity::new, Sets.newHashSet(Blocks.ENVIRONMENT_DETECTOR.get()), null));
    public static final RegistryEntry<BlockEntityType<PlayerDetectorEntity>> PLAYER_DETECTOR = Registration.TILE_ENTITIES.register("player_detector", () -> new BlockEntityType<>(PlayerDetectorEntity::new, Sets.newHashSet(Blocks.PLAYER_DETECTOR.get()), null));
    public static final RegistryEntry<BlockEntityType<MeBridgeEntity>> ME_BRIDGE = isModLoaded("ae2") ? Registration.TILE_ENTITIES.register("me_bridge", () -> new BlockEntityType<>(MeBridgeEntity::new, Sets.newHashSet(Blocks.ME_BRIDGE.get()), null)) : null;
    public static final RegistryEntry<BlockEntityType<RsBridgeEntity>> RS_BRIDGE = isModLoaded("refinedstorage") ? Registration.TILE_ENTITIES.register("rs_bridge", () -> new BlockEntityType<>(RsBridgeEntity::new, Sets.newHashSet(Blocks.RS_BRIDGE.get()), null)) : null;
    public static final RegistryEntry<BlockEntityType<EnergyDetectorEntity>> ENERGY_DETECTOR = Registration.TILE_ENTITIES.register("energy_detector", () -> new BlockEntityType<>(EnergyDetectorEntity::new, Sets.newHashSet(Blocks.ENERGY_DETECTOR.get()), null));
    public static final RegistryEntry<BlockEntityType<InventoryManagerEntity>> INVENTORY_MANAGER = Registration.TILE_ENTITIES.register("inventory_manager", () -> new BlockEntityType<>(InventoryManagerEntity::new, Sets.newHashSet(Blocks.INVENTORY_MANAGER.get()), null));
    public static final RegistryEntry<BlockEntityType<RedstoneIntegratorEntity>> REDSTONE_INTEGRATOR = Registration.TILE_ENTITIES.register("redstone_integrator", () -> new BlockEntityType<>(RedstoneIntegratorEntity::new, Sets.newHashSet(Blocks.REDSTONE_INTEGRATOR.get()), null));
    public static final RegistryEntry<BlockEntityType<BlockReaderEntity>> BLOCK_READER = Registration.TILE_ENTITIES.register("block_reader", () -> new BlockEntityType<>(BlockReaderEntity::new, Sets.newHashSet(Blocks.BLOCK_READER.get()), null));
    public static final RegistryEntry<BlockEntityType<GeoScannerEntity>> GEO_SCANNER = Registration.TILE_ENTITIES.register("geo_scanner", () -> new BlockEntityType<>(GeoScannerEntity::new, Sets.newHashSet(Blocks.GEO_SCANNER.get()), null));
    public static final RegistryEntry<BlockEntityType<ColonyIntegratorEntity>> COLONY_INTEGRATOR = Registration.TILE_ENTITIES.register("colony_integrator", () -> new BlockEntityType<>(ColonyIntegratorEntity::new, Sets.newHashSet(Blocks.COLONY_INTEGRATOR.get()), null));
    public static final RegistryEntry<BlockEntityType<NBTStorageEntity>> NBT_STORAGE = Registration.TILE_ENTITIES.register("nbt_storage", () -> new BlockEntityType<>(NBTStorageEntity::new, Sets.newHashSet(Blocks.NBT_STORAGE.get()), null));

    public static void register() {
        registerPeripheral(CHAT_BOX);
        registerPeripheral(ENVIRONMENT_DETECTOR);
        registerPeripheral(PLAYER_DETECTOR);
        registerPeripheral(ME_BRIDGE);
        registerPeripheral(RS_BRIDGE);
        registerPeripheral(ENERGY_DETECTOR);
        registerPeripheral(INVENTORY_MANAGER);
        registerPeripheral(REDSTONE_INTEGRATOR);
        registerPeripheral(BLOCK_READER);
        registerPeripheral(GEO_SCANNER);
        registerPeripheral(COLONY_INTEGRATOR);
        registerPeripheral(NBT_STORAGE);
    }

    public static boolean registerPeripheral(RegistryEntry<BlockEntityType<? extends PeripheralBlockEntity>> entry) {
        if (entry == null) {
            return;
        }
        PeripheralLookup.get().registerForBlockEntity((b, d) -> b.createPeripheral(), entry.get());
    }

    public static boolean isModLoaded(String modid) {
        return PlatformHelper.get().isModLoaded(modid);
    }
}
