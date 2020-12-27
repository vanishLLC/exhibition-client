/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import net.minecraft.client.Minecraft;

public class KillProcess {

    public static Minecraft mc = Minecraft.getMinecraft();

    public static void killMC() {
        Minecraft.shutdownMinecraftApplet();
    }

}
