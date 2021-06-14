
package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.management.ColorManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class ChestESP extends Module {

    public ChestESP(ModuleData data) {
        super(data);
    }

    @Override
    @RegisterEvent(events = {EventRender3D.class})
    public void onEvent(Event event) {
        EventRender3D e = (EventRender3D) event;

        RenderingUtil.pre3D();
        for (Object o : this.mc.theWorld.loadedTileEntityList) {
            if (o instanceof TileEntityChest) {
                TileEntityLockable storage = (TileEntityLockable) o;
                this.drawESPOnStorage(storage, storage.getPos().getX(), storage.getPos().getY(),
                        storage.getPos().getZ());
            } else if (o instanceof TileEntityEnderChest) {
                TileEntityEnderChest chest = (TileEntityEnderChest) o;
                AxisAlignedBB bb = new AxisAlignedBB(chest.getPos().getX() + 0.0625, chest.getPos().getY(), chest.getPos().getZ() + 0.0625,
                        chest.getPos().getX() + 0.9375, chest.getPos().getY() + 0.875, chest.getPos().getZ() + 0.9375);
                RenderingUtil.glColor(Colors.getColor(150, 0, 252, 75));
                RenderingUtil.drawBoundingBox(bb.offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));
                GL11.glColor4f(1, 1, 1, 1);
            }
        }
        RenderingUtil.post3D();

    }

    public void drawESPOnStorage(final TileEntityLockable storage, final double x, final double y, final double z) {
        if (storage.isLocked())
            return;
        TileEntityChest chest = (TileEntityChest) storage;
        Vec3 vec;
        Vec3 vec2;
        if (chest.adjacentChestZNeg != null) {
            vec = new Vec3(x + 0.0625, y, z - 0.9375);
            vec2 = new Vec3(x + 0.9375, y + 0.875, z + 0.9375);
        } else if (chest.adjacentChestXNeg != null) {
            vec = new Vec3(x + 0.9375, y, z + 0.0625);
            vec2 = new Vec3(x - 0.9375, y + 0.875, z + 0.9375);
        } else if (chest.adjacentChestXPos == null && chest.adjacentChestZPos == null) {
            vec = new Vec3(x + 0.0625, y, z + 0.0625);
            vec2 = new Vec3(x + 0.9375, y + 0.875, z + 0.9375);
        } else {
            return;
        }

        if (((TileEntityChest) storage).getChestType() != 0) {
            GL11.glColor4d(0.9, 0.3, 0.3, 0.3);
        } else if (chest.isEmpty || chest.lidAngle > 0) {
            GL11.glColor4d(0.3, 0.3, 0.3, 0.3);
        } else
            RenderingUtil.glColor(ColorManager.chestESPColor.getColorHex());

        RenderingUtil.drawBoundingBox(new AxisAlignedBB(vec.xCoord - RenderManager.renderPosX, vec.yCoord - RenderManager.renderPosY, vec.zCoord - RenderManager.renderPosZ, vec2.xCoord - RenderManager.renderPosX, vec2.yCoord - RenderManager.renderPosY, vec2.zCoord - RenderManager.renderPosZ));
        GL11.glColor4f(1, 1, 1, 1);
    }


}
