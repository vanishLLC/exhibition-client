package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.MathUtils;
import exhibition.util.PlayerUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arithmo on 11/18/2017 at 11:22 PM.
 */
public class BoundingBoxTest extends Module {

    public BoundingBoxTest(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventRender3D.class})
    public void onEvent(Event event) {
        EntityPlayerSP player = mc.thePlayer;

        double collisSize = 0.01D;
        AxisAlignedBB var11 = player.getEntityBoundingBox();

        double nextPosX = (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * 1.5F;
        double nextPosZ = (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * 1.5F;

        AxisAlignedBB var12 = new AxisAlignedBB(var11.minX - player.posX + nextPosX, var11.minY - player.posY, var11.minZ - player.posZ + nextPosZ,
                var11.maxX - player.posX + nextPosX, var11.minY - player.posY + 0.01F, var11.maxZ - player.posZ + nextPosZ).expand(collisSize, collisSize, collisSize);




    }


}
