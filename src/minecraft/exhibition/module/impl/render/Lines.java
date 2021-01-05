/**
 * Time: 6:29:26 PM
 * Date: Jan 3, 2017
 * Creator: cool1
 */
package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import org.lwjgl.opengl.GL11;

/**
 * @author cool1
 */
public class Lines extends Module {

    public String CHEST = "CHEST";
    public String SIGN = "SIGN";
    public String SPAWNER = "SPAWNER";
    public String PLAYER = "PLAYER";

    /**
     * @param data
     */
    public Lines(ModuleData data) {
        super(data);
        settings.put(SIGN, new Setting(SIGN, false, "Draw lines at signs."));
        settings.put(CHEST, new Setting(CHEST, true, "Draw lines at chests."));
        settings.put(SPAWNER, new Setting(SPAWNER, false, "Draw lines at spawners."));
        settings.put(PLAYER, new Setting(PLAYER, false, "Draw lines on players."));
    }

    /* (non-Javadoc)
     * @see EventListener#onEvent(Event)
     */
    @Override
    @RegisterEvent(events = {EventRender3D.class})
    public void onEvent(Event event) {
        EventRender3D er = (EventRender3D) event;
        final boolean bobbing = mc.gameSettings.viewBobbing;
        GL11.glLoadIdentity();
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);

        for (Object o : mc.theWorld.loadedTileEntityList) {
            int color = -1;
            TileEntity ent = (TileEntity) o;
            if (o instanceof TileEntityChest && ((boolean) settings.get(CHEST).getValue())) {
                color = Colors.getColor(114, 0, 187);
            }

            if (o instanceof TileEntityMobSpawner && ((boolean) settings.get(SPAWNER).getValue())) {
                color = Colors.getColor(255, 156, 0);
            }

            if (o instanceof TileEntitySign && ((boolean) settings.get(SIGN).getValue())) {
                color = Colors.getColor(130, 162, 0);
            }

            if (color == -1)
                continue;

            float posX = (float) ((float) ent.getPos().getX() + 0.5 - RenderManager.renderPosX);
            float posY = (float) ((float) ent.getPos().getY() + 0.5 - RenderManager.renderPosY);
            float posZ = (float) ((float) ent.getPos().getZ() + 0.5 - RenderManager.renderPosZ);
            RenderingUtil.draw3DLine(posX, posY, posZ, color);
        }

        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer && ((boolean) settings.get(PLAYER).getValue())) {
                int color = Colors.getColor(240);
                float posX = (float) ((float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks) - RenderManager.renderPosX);
                float posY = (float) ((float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks) - RenderManager.renderPosY);
                float posZ = (float) ((float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks) - RenderManager.renderPosZ);
                RenderingUtil.draw3DLine(posX, posY, posZ, color);
            }
        }
        mc.gameSettings.viewBobbing = bobbing;
    }

}
