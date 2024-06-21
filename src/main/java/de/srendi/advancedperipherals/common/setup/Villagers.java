package de.srendi.advancedperipherals.common.setup;

import com.google.common.collect.ImmutableSet;
import dan200.computercraft.shared.ModRegistry;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.shared.platform.RegistryEntry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;

public class Villagers {
    public static final RegistryEntry<PoiType> COMPUTER_SCIENTIST_POI = Registration.POI_TYPES.register("computer_scientist", () -> new PoiType(ImmutableSet.copyOf(ModRegistry.Blocks.COMPUTER_ADVANCED.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryEntry<VillagerProfession> COMPUTER_SCIENTIST = Registration.VILLAGER_PROFESSIONS.register("computer_scientist", () -> new VillagerProfession(AdvancedPeripherals.MOD_ID + ":computer_scientist", holder -> holder.is(COMPUTER_SCIENTIST_POI.getKey()), holder -> holder.is(COMPUTER_SCIENTIST_POI.getKey()), ImmutableSet.of(), ImmutableSet.of(ModRegistry.Blocks.COMPUTER_ADVANCED.get()), SoundEvents.VILLAGER_WORK_TOOLSMITH));

    public static void register() {
    }
}
