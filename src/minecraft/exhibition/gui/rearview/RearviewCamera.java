/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.rearview;

import net.minecraft.client.Minecraft;

public class RearviewCamera extends Camera{

    private Minecraft mc;

    public RearviewCamera(){
        super(true);
        mc = Minecraft.getMinecraft();
    }

    @Override
    public void updateFramebuffer() {
        setToEntityPosition(mc.thePlayer);

        cameraRotationYaw = mc.thePlayer.rotationYaw+180;
        cameraRotationPitch = mc.thePlayer.rotationPitch;

        super.updateFramebuffer();
    }

}
