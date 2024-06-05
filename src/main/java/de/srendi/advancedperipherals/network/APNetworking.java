package de.srendi.advancedperipherals.network;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.network.toclient.ToastToClientPacket;
import de.srendi.advancedperipherals.client.platform.ClientPlatformHelper;
import de.srendi.advancedperipherals.shared.platform.PlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public final class APNetworking {
    private static final String PROTOCOL_VERSION = AdvancedPeripherals.getVersion();
    private static int idCounter = 0;
    private static final List<PacketRecord<?>> s2cPackets = new ArrayList<>();
    private static final List<PacketRecord<?>> c2sPackets = new ArrayList<>();
    // private static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(AdvancedPeripherals.MOD_ID, "main_channel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static final MessageType<ToastToClientPacket> TOAST2C = registerServerToClient("display_toast", ToastToClientPacket.class, ToastToClientPacket::decode);

    private static <T extends NetworkMessage<?>> MessageType<T> createMessageType(int id, String channel, Class<T> packet, FriendlyByteBuf.Reader<T> decoder) {
        return PlatformHelper.get().createMessageType(id, new ResourceLocation(AdvancedPeripherals.MOD_ID, channel), packet, decoder);
    }

    private static <T extends NetworkMessage<ClientNetworkContext>> MessageType<T> registerServerToClient(String channel, Class<T> packet, FriendlyByteBuf.Reader<T> decoder) {
        final int id = idCounter++;
        MessageType<T> type = createMessageType(id, channel, packet, decoder);
        s2cPackets.add(new PacketRecord(id, type, packet, decoder));
        return type;
    }

    private static <T extends NetworkMessage<ServerNetworkContext>> MessageType<T> registerClientToServer(String channel, Class<T> packet, FriendlyByteBuf.Reader<T> decoder) {
        final int id = idCounter++;
        MessageType<T> type = createMessageType(id, channel, packet, decoder);
        c2sPackets.add(new PacketRecord(id, type, packet, decoder));
        return type;
    }

    public static void initClientSide() {
        // for (PacketRecord<?> r : s2cPackets) {
        //     NETWORK_CHANNEL.registerMessage(r.id(), r.packet(), IPacket::encode, r.decoder(), IPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        // }
        for (PacketRecord<?> r : s2cPackets) {
            ClientPlayNetworking.registerGlobalReceiver(
                FabricMessageType.toFabricType(r.type()), (packet, player, sender) -> packet.payload().handle(null)
            );
        }
    }

    public static void initServerSide() {
        // for (PacketRecord<?> r : c2sPackets) {
        //     NETWORK_CHANNEL.registerMessage(r.id(), r.packet(), IPacket::encode, r.decoder(), IPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        // }
        for (PacketRecord<?> r : c2sPackets) {
            ServerPlayNetworking.registerGlobalReceiver(
                FabricMessageType.toFabricType(r.type()), (packet, player, sender) -> packet.payload().handle(() -> player)
            );
        }
    }

    private static record PacketRecord<T extends NetworkMessage<?>>(int id, Class<T> packet, Function<FriendlyByteBuf, T> decoder) {
    }

    /**
     * Sends a packet to the server.<p>
     * Must be called Client side.
     */
    public static void sendToServer(NetworkMessage<ServerNetworkContext> msg) {
        // NETWORK_CHANNEL.sendToServer(msg);
        PacketByteBufs buf = PacketByteBufs.create();
        msg.write(buf);
        ClientPlayNetworking.send(FabricMessageType.toFabricType(msg.type()).getId(), buf);
    }

    /**
     * Send a packet to a specific player.<p>
     * Must be called Server side.
     */
    public static void sendTo(NetworkMessage<ClientNetworkContext> msg, ServerPlayer player) {
        if (player instanceof FakePlayer) {
            return;
        }
        // NETWORK_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
        PacketByteBufs buf = PacketByteBufs.create();
        msg.write(buf);
    }

    public static void sendPacketToAll(NetworkMessage<?> msg) {
        // NETWORK_CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        PacketByteBufs buf = PacketByteBufs.create();
        msg.write(buf);
        Packet<?> pkt = ServerPlayNetworking.createS2CPacket(FabricMessageType.toFabricType(msg.type()).getId(), buf);
        for (ServerPlayer player : PlayerLookup.all(AdvancedPeripherals.getServer())) {
            ServerPlayNetworking.send(player, pkt);
        }
    }

    public static void sendToAllAround(Object mes, ResourceKey<Level> dim, BlockPos pos, int radius) {
        // NETWORK_CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, dim)), mes);
    }

    public static void sendToAllInWorld(Object mes, ServerLevel world) {
        // NETWORK_CHANNEL.send(PacketDistributor.DIMENSION.with(world::dimension), mes);
    }

    private static Packet<ClientGamePacketListener> createS2CPacket(NetworkMessage<ClientNetworkContext> msg) {
        return PlatformHelper.get().createPacket(msg);
    }

    private static Packet<ServerGamePacketListener> createC2SPacket(NetworkMessage<ServerNetworkContext> msg) {
        return ClientPlatformHelper.get().createPacket(msg);
    }
}
