package de.srendi.advancedperipherals.common.setup;

import de.srendi.advancedperipherals.APCreativeTab;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.shared.platform.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CreativeTabs {
    public static final RegistryEntry<CreativeModeTab> AP_CREATIVE_MODE_TAB = Registration.CREATIVE_MODE_TABS.register(AdvancedPeripherals.MOD_ID, CreativeTabs::createCreativeTab);

    private static CreativeModeTab createCreativeTab() {
        CreativeModeTab.Builder builder = new CreativeModeTab.Builder(CreativeModeTab.Row.BOTTOM, -1);
        APCreativeTab.populateCreativeTabBuilder(builder);
        return builder.build();
    }

    public static void register() {
    }
}
