package de.srendi.advancedperipherals.common.setup;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.common.items.APItem;
import de.srendi.advancedperipherals.common.items.MemoryCardItem;
import de.srendi.advancedperipherals.common.items.WeakAutomataCore;
import de.srendi.advancedperipherals.impl.PlatformHelper;
import de.srendi.advancedperipherals.shared.platform.RegistryEntry;
import net.minecraft.world.item.Item;

public final class Items {
    public static final RegistryEntry<Item> CHUNK_CONTROLLER = Registration.ITEMS.register("chunk_controller", () -> new APItem(new Item.Properties().stacksTo(16), APConfig.PERIPHERALS_CONFIG.enableChunkyTurtle::get));
    public static final RegistryEntry<Item> COMPUTER_TOOL = Registration.ITEMS.register("computer_tool", () -> new APItem(new Item.Properties().stacksTo(1), () -> true));
    public static final RegistryEntry<Item> MEMORY_CARD = Registration.ITEMS.register("memory_card", MemoryCardItem::new);
    public static final RegistryEntry<Item> END_AUTOMATA_CORE = Registration.ITEMS.register("end_automata_core", () -> new APItem(new Item.Properties().stacksTo(1), APConfig.METAPHYSICS_CONFIG.enableEndAutomataCore));
    public static final RegistryEntry<Item> HUSBANDRY_AUTOMATA_CORE = Registration.ITEMS.register("husbandry_automata_core", () -> new APItem(new Item.Properties().stacksTo(1), APConfig.METAPHYSICS_CONFIG.enableHusbandryAutomataCore));
    public static final RegistryEntry<Item> WEAK_AUTOMATA_CORE = Registration.ITEMS.register("weak_automata_core", () -> new WeakAutomataCore(new Item.Properties().stacksTo(1)));
    public static final RegistryEntry<Item> OVERPOWERED_WEAK_AUTOMATA_CORE = Registration.ITEMS.register("overpowered_weak_automata_core", () -> new APItem(new Item.Properties().stacksTo(1), APConfig.METAPHYSICS_CONFIG.enableWeakAutomataCore));
    public static final RegistryEntry<Item> OVERPOWERED_END_AUTOMATA_CORE = Registration.ITEMS.register("overpowered_end_automata_core", () -> new APItem(new Item.Properties().stacksTo(1), APConfig.METAPHYSICS_CONFIG.enableEndAutomataCore));
    public static final RegistryEntry<Item> OVERPOWERED_HUSBANDRY_AUTOMATA_CORE = Registration.ITEMS.register("overpowered_husbandry_automata_core", () -> new APItem(new Item.Properties().stacksTo(1), APConfig.METAPHYSICS_CONFIG.enableHusbandryAutomataCore));

    public static void register() {
    }
}
