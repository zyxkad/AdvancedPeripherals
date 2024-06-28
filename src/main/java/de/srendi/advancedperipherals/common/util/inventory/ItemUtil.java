package de.srendi.advancedperipherals.common.util.inventory;

import dan200.computercraft.shared.ModRegistry;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.util.StringUtil;
import de.srendi.advancedperipherals.shared.platform.RegistryWrappers;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.CloneNotSupportedException;
import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static final Item TURTLE_NORMAL = ModRegistry.Items.TURTLE_NORMAL.get();
    public static final Item TURTLE_ADVANCED = ModRegistry.Items.TURTLE_ADVANCED.get();

    public static final Item POCKET_NORMAL = ModRegistry.Items.POCKET_COMPUTER_NORMAL.get();
    public static final Item POCKET_ADVANCED = ModRegistry.Items.POCKET_COMPUTER_ADVANCED.get();

    private static final MessageDigest MD5 = MessageDigest.getInstance("MD5");
    private static final CompoundTag EMPTY_TAG = new CompoundTag();

    private ItemUtil() {
    }

    public static <T> T getRegistryEntry(String name, RegistryWrappers.RegistryWrapper<T> registry) {
        ResourceLocation location;
        try {
            location = new ResourceLocation(name);
        } catch (ResourceLocationException ex) {
            return null;
        }

        return registry.tryGet(location);
    }

    /**
     * Fingerprints are MD5 hashes generated out of the nbt tag, the registry name and the display name from item stacks
     * Used to filter inventory specific operations. {@link de.srendi.advancedperipherals.common.addons.computercraft.peripheral.InventoryManagerPeripheral}
     *
     * @return A generated MD5 hash from the item stack
     */
    public static String getFingerprint(ItemStack stack) {
        try {
            byte[] bytesOfHash = fingerprint.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MD5.clone();
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                tag = EMPTY_TAG;
            }
            md.update(tag.toString().getBytes(StandardCharsets.UTF_8));
            md.update(getRegistryKey(stack).toString().getBytes(StandardCharsets.UTF_8));
            md.update(stack.getDisplayName().getString().getBytes(StandardCharsets.UTF_8));
            return StringUtil.toHexString(md.digest());
        } catch (CloneNotSupportedException ex) {
            AdvancedPeripherals.debug("Could not parse fingerprint.", Level.ERROR);
            ex.printStackTrace();
        }
        return "";
    }

    public static ItemStack makeTurtle(Item turtle, String upgrade) {
        ItemStack stack = new ItemStack(turtle);
        stack.getOrCreateTag().putString("RightUpgrade", upgrade);
        return stack;
    }

    public static ItemStack makePocket(Item turtle, String upgrade) {
        ItemStack stack = new ItemStack(turtle);
        stack.getOrCreateTag().putString("Upgrade", upgrade);
        return stack;
    }

    public static void addComputerItemToTab(ResourceLocation turtleID, ResourceLocation pocketID, NonNullList<ItemStack> items) {
        if (turtleID != null) {
            items.add(makeTurtle(TURTLE_ADVANCED, turtleID.toString()));
            items.add(makeTurtle(TURTLE_NORMAL, turtleID.toString()));
        }
        if (pocketID != null) {
            items.add(makePocket(POCKET_ADVANCED, pocketID.toString()));
            items.add(makePocket(POCKET_NORMAL, pocketID.toString()));
        }
    }

    public static ResourceLocation getRegistryKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation getRegistryKey(ItemStack item) {
        return BuiltInRegistries.ITEM.getKey(item.getItem());
    }
}
