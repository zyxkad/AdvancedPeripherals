package de.srendi.advancedperipherals.network.toclient;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.util.ToastUtil;
import de.srendi.advancedperipherals.network.ClientNetworkContext;
import de.srendi.advancedperipherals.network.MessageType;
import de.srendi.advancedperipherals.network.NetworkMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ToastToClientPacket implements NetworkMessage<ClientNetworkContext> {

    private final Component title;
    private final Component component;

    public ToastToClientPacket(Component title, Component component) {
        this.title = title;
        this.component = component;
    }

    @Override
    public void handle(ClientNetworkContext context) {
        // // Should in the theory not happen, but safe is safe.
        // if (!FMLEnvironment.dist.isClient()) {
        //     AdvancedPeripherals.debug("Tried to display toasts on the server, aborting.");
        //     return;
        // }
        ToastUtil.displayToast(title, component);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeComponent(this.title);
        buffer.writeComponent(this.component);
    }

    @Override
    public MessageType<ToastToClientPacket> type() {
        return NetworkMessages.TOAST2C;
    }

    public static ToastToClientPacket decode(FriendlyByteBuf buffer) {
        return new ToastToClientPacket(buffer.readComponent(), buffer.readComponent());
    }
}
