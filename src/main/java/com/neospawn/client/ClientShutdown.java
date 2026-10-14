package com.neospawn.client;

import net.minecraft.client.Minecraft;

/** Closes the game window. Client-only; used by the realm self-check so the window can't be reused. */
public final class ClientShutdown {
    public static void stop() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(minecraft::stop);
    }

    private ClientShutdown() {}
}
