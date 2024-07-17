package de.srendi.advancedperipherals.common.util;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import net.minecraft.server.MinecraftServer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerWorker {

    private static final Queue<Runnable> callQueue = new ConcurrentLinkedQueue<>();

    public static void add(final Runnable call) {
        if (call != null) {
            callQueue.add(call);
        }
    }

    public static void serverTick(MinecraftServer server) {
        while (true) {
            final Runnable runnable = callQueue.poll();
            if (runnable == null) {
                return;
            }
            AdvancedPeripherals.debug("Running queued server worker call: " + runnable);
            runnable.run();
        }
    }
}
