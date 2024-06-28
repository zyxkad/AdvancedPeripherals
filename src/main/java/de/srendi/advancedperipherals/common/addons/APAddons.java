package de.srendi.advancedperipherals.common.addons;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.addons.refinedstorage.RefinedStorage;
import de.srendi.advancedperipherals.shared.platform.PlatformHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = AdvancedPeripherals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class APAddons {

    public static final String CURIOS_MODID = "curios";
    public static final String REFINEDSTORAGE_MODID = "refinedstorage";
    public static final String AE_THINGS_MODID = "ae2things";
    public static final String AE_ADDITIONS_MODID = "ae2additions";
    public static final String APP_MEKANISTICS_MODID = "appmek";

    public static boolean curiosLoaded;
    public static boolean refinedStorageLoaded;
    public static boolean aeThingsLoaded;
    public static boolean aeAdditionsLoaded;
    public static boolean appMekLoaded;

    private APAddons() {
    }

    public static void commonSetup() {
        curiosLoaded = PlatformHelper.get().isModLoaded(CURIOS_MODID);
        refinedStorageLoaded = PlatformHelper.get().isModLoaded(REFINEDSTORAGE_MODID);
        aeThingsLoaded = PlatformHelper.get().isModLoaded(AE_THINGS_MODID);
        aeAdditionsLoaded = PlatformHelper.get().isModLoaded(AE_ADDITIONS_MODID);
        appMekLoaded = PlatformHelper.get().isModLoaded(APP_MEKANISTICS_MODID);

        if (refinedStorageLoaded)
            RefinedStorage.instance = new RefinedStorage();

    }

    @SubscribeEvent
    public static void interModComms(InterModEnqueueEvent event) {
        /*
        if (!curiosLoaded) {
        }

        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("glasses").size(1).build());
        */
    }
}
