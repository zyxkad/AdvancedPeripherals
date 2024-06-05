// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package de.srendi.advancedperipherals.shared.platform;

import com.google.auto.service.AutoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.node.wired.WiredElementLookup;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.network.ClientNetworkContext;
import de.srendi.advancedperipherals.network.MessageType;
import de.srendi.advancedperipherals.network.NetworkMessage;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

@AutoService(de.srendi.advancedperipherals.impl.PlatformHelper.class)
public class PlatformHelperImpl implements PlatformHelper {
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> id) {
        var registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(id.location());
        if (registry == null) throw new IllegalArgumentException("Unknown registry " + id);
        return registry;
    }

    @Override
    public <T> ResourceLocation getRegistryKey(ResourceKey<Registry<T>> registry, T object) {
        var key = getRegistry(registry).getKey(object);
        if (key == null) throw new IllegalArgumentException(object + " was not registered in " + registry);
        return key;
    }

    @Override
    public <T> T getRegistryObject(ResourceKey<Registry<T>> registry, ResourceLocation id) {
        var value = getRegistry(registry).get(id);
        if (value == null) throw new IllegalArgumentException(id + " was not registered in " + registry);
        return value;
    }

    @Override
    public <T> RegistryWrappers.RegistryWrapper<T> wrap(ResourceKey<Registry<T>> registry) {
        return new RegistryWrapperImpl<>(registry.location(), getRegistry(registry));
    }

    @Override
    public <T> RegistrationHelper<T> createRegistrationHelper(ResourceKey<Registry<T>> registry) {
        return new RegistrationHelperImpl<>(getRegistry(registry));
    }

    @Nullable
    @Override
    public <T> T tryGetRegistryObject(ResourceKey<Registry<T>> registry, ResourceLocation id) {
        return getRegistry(registry).get(id);
    }

    @Override
    public boolean shouldLoadResource(JsonObject object) {
        return ResourceConditions.objectMatchesConditions(object);
    }

    @Override
    public void addRequiredModCondition(JsonObject object, String modId) {
        var conditions = GsonHelper.getAsJsonArray(object, ResourceConditions.CONDITIONS_KEY, null);
        if (conditions == null) {
            conditions = new JsonArray();
            object.add(ResourceConditions.CONDITIONS_KEY, conditions);
        }

        conditions.add(DefaultResourceConditions.allModsLoaded(modId).toJson());
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block block) {
        return FabricBlockEntityTypeBuilder.create(factory::apply).addBlock(block).build();
    }

    // @Override
    // public <C extends AbstractContainerMenu, T extends ContainerData> MenuType<C> createMenuType(Function<FriendlyByteBuf, T> reader, ContainerData.Factory<C, T> factory) {
    //     return new ExtendedScreenHandlerType<>((id, player, data) -> factory.create(id, player, reader.apply(data)));
    // }

    // @Override
    // public void openMenu(Player player, MenuProvider owner, ContainerData menu) {
    //     player.openMenu(new WrappedMenuProvider(owner, menu));
    // }

    @Override
    public <T extends NetworkMessage<?>> MessageType<T> createMessageType(int id, ResourceLocation channel, Class<T> klass, FriendlyByteBuf.Reader<T> reader) {
        return new FabricMessageType<>(channel, reader);
    }

    @Override
    public Packet<ClientGamePacketListener> createPacket(NetworkMessage<ClientNetworkContext> message) {
        var buf = PacketByteBufs.create();
        message.write(buf);
        return ServerPlayNetworking.createS2CPacket(FabricMessageType.toFabricType(message.type()).getId(), buf);
    }

    @Override
    public int getBurnTime(ItemStack stack) {
        @Nullable var fuel = FuelRegistry.INSTANCE.get(stack.getItem());
        return fuel == null ? 0 : fuel;
    }

    @Override
    public CreativeModeTab.Builder newCreativeModeTab() {
        return FabricItemGroup.builder();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public List<ItemStack> getRecipeRemainingItems(ServerPlayer player, Recipe<CraftingContainer> recipe, CraftingContainer container) {
        return recipe.getRemainingItems(container);
    }

    @Override
    public void onItemCrafted(ServerPlayer player, CraftingContainer container, ItemStack stack) {
    }

    @Override
    public boolean onNotifyNeighbour(Level level, BlockPos pos, BlockState block, Direction direction) {
        return true;
    }

    @Override
    public ServerPlayer createFakePlayer(ServerLevel world, GameProfile name) {
        return FakePlayer.create(world, name);
    }

    @Override
    public boolean hasToolUsage(ItemStack stack) {
        var item = stack.getItem();
        return item instanceof ShovelItem || stack.is(ItemTags.SHOVELS) ||
            item instanceof HoeItem || stack.is(ItemTags.HOES);
    }

    @Override
    public InteractionResult canAttackEntity(ServerPlayer player, Entity entity) {
        return AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, entity, null);
    }

    @Override
    public boolean interactWithEntity(ServerPlayer player, Entity entity, Vec3 hitPos) {
        return UseEntityCallback.EVENT.invoker().interact(player, entity.level(), InteractionHand.MAIN_HAND, entity, new EntityHitResult(entity, hitPos)).consumesAction() ||
            entity.interactAt(player, hitPos.subtract(entity.position()), InteractionHand.MAIN_HAND).consumesAction() ||
            player.interactOn(entity, InteractionHand.MAIN_HAND).consumesAction();
    }

    @Override
    public InteractionResult useOn(ServerPlayer player, ItemStack stack, BlockHitResult hit, Predicate<BlockState> canUseBlock) {
        var result = UseBlockCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, hit);
        if (result != InteractionResult.PASS) return result;

        var block = player.level().getBlockState(hit.getBlockPos());
        if (!block.isAir() && canUseBlock.test(block)) {
            var useResult = block.use(player.level(), player, InteractionHand.MAIN_HAND, hit);
            if (useResult.consumesAction()) return useResult;
        }

        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded();
    }

    private record RegistryWrapperImpl<T>(
        ResourceLocation name, Registry<T> registry
    ) implements RegistryWrappers.RegistryWrapper<T> {
        @Override
        public int getId(T object) {
            return registry.getId(object);
        }

        @Override
        public ResourceLocation getKey(T object) {
            var key = registry.getKey(object);
            if (key == null) throw new IllegalArgumentException(object + " was not registered in " + name);
            return key;
        }

        @Override
        public T get(ResourceLocation location) {
            var object = registry.get(location);
            if (object == null) throw new IllegalArgumentException(location + " was not registered in " + name);
            return object;
        }

        @Nullable
        @Override
        public T tryGet(ResourceLocation location) {
            return registry.get(location);
        }

        @Override
        public @Nullable T byId(int id) {
            return registry.byId(id);
        }

        @Override
        public int size() {
            return registry.size();
        }

        @Override
        public Iterator<T> iterator() {
            return registry.iterator();
        }
    }

    private static final class RegistrationHelperImpl<T> implements RegistrationHelper<T> {
        private final Registry<T> registry;
        private final List<RegistryEntryImpl<? extends T>> entries = new ArrayList<>();

        private RegistrationHelperImpl(Registry<T> registry) {
            this.registry = registry;
        }

        @Override
        public <U extends T> RegistryEntry<U> register(String name, Supplier<U> create) {
            var entry = new RegistryEntryImpl<>(new ResourceLocation(AdvancedPeripherals.MOD_ID, name), create);
            entries.add(entry);
            return entry;
        }

        @Override
        public void register() {
            for (var entry : entries) entry.register(registry);
        }
    }

    private static final class RegistryEntryImpl<T> implements RegistryEntry<T> {
        private final ResourceLocation id;
        private final Supplier<T> supplier;
        private @Nullable T instance;

        RegistryEntryImpl(ResourceLocation id, Supplier<T> supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        void register(Registry<? super T> registry) {
            Registry.register(registry, id, instance = supplier.get());
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public T get() {
            if (instance == null) throw new IllegalStateException(id + " has not been constructed yet");
            return instance;
        }
    }

    // private record WrappedMenuProvider(MenuProvider owner, ContainerData menu) implements ExtendedScreenHandlerFactory {
    //     @Nullable
    //     @Override
    //     public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    //         return owner.createMenu(id, inventory, player);
    //     }

    //     @Override
    //     public Component getDisplayName() {
    //         return owner.getDisplayName();
    //     }

    //     @Override
    //     public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
    //         menu.toBytes(buf);
    //     }
    // }
}
