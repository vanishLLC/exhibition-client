package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.hud.HUD;
import exhibition.util.RenderingUtil;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Arithmo on 9/23/2017 at 4:23 PM.
 */
public class SpookySkeltal extends Module {

    private static final Map<EntityPlayer, float[][]> modelRotations = new HashMap<>();

    public SpookySkeltal(ModuleData data) {
        super(data);
    }

    @Override
    public Priority getPriority() {
        return Priority.LOWEST;
    }

    @Override
    public void onToggle() {
        modelRotations.clear();
    }

    @RegisterEvent(events = EventRender3D.class)
    public void onEvent(Event event) {

        boolean g = Client.getModuleManager().get(HUD.class).getSetting("CLIENT-NAME").getValue().toString().equals("Genesis");

        boolean lighting = glIsEnabled(GL_LIGHTING);

        if (lighting) {
            glDisable(GL_LIGHTING);
        }

        RenderingUtil.setupRender(true);
        glDisable(GL_LINE_SMOOTH);
        modelRotations.keySet().removeIf(player -> !mc.theWorld.loadedEntityList.contains(player));
        mc.theWorld.loadedEntityList.forEach(bruh -> {
            if (!(bruh instanceof EntityPlayer))
                return;
            EntityPlayer player = (EntityPlayer) bruh;

            if (player == mc.thePlayer || player.isInvisible())
                return;

            float[][] modelRotations = SpookySkeltal.modelRotations.get(player);
            if (modelRotations == null)
                return;

            glPushMatrix();

            glLineWidth(g ? 3F : 1.0F);
            glColor4f(1, 1, 1, 1);
            Vec3 interp = RenderingUtil.interpolateRender(player);
            double x = (interp.getX()) - RenderManager.renderPosX;
            double y = (interp.getY()) - RenderManager.renderPosY;
            double z = (interp.getZ()) - RenderManager.renderPosZ;
            glTranslated(x, y, z);
            float bodyYawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * mc.timer.renderPartialTicks;
            if (!g)
                glRotatef(-bodyYawOffset, 0, 1, 0);

            glTranslated(0, 0, player.isSneaking() ? -0.235 : 0.0);

            float legHeight = player.isSneaking() ? 0.6F : 0.75F;

            float armWidth = 0;

            Render var11;
            try {
                var11 = mc.getRenderManager().getEntityRenderObject(bruh);
                if (var11 != null && mc.getRenderManager().renderEngine != null) {
                    if (var11 instanceof RenderPlayer) {
                        RenderPlayer rp = (RenderPlayer) var11;
                        if (rp.getMainModel().smallArms) {
                            armWidth = 0.05F;
                        }
                    }
                }
            } catch (Exception e) {

            }

            {
                glPushMatrix();
                glTranslated(-0.125, legHeight, 0);

                if (!g) {
                    if (modelRotations[3][0] != 0.0F)
                        glRotatef(modelRotations[3][0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                    if (modelRotations[3][1] != 0.0F)
                        glRotatef(modelRotations[3][1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                    if (modelRotations[3][2] != 0.0F)
                        glRotatef(modelRotations[3][2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, -legHeight, 0);
                glEnd();
                glPopMatrix();
            }

            {
                glPushMatrix();
                glTranslated(0.125, legHeight, 0);

                if (!g) {
                    if (modelRotations[4][0] != 0.0F)
                        glRotatef(modelRotations[4][0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                    if (modelRotations[4][1] != 0.0F)
                        glRotatef(modelRotations[4][1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                    if (modelRotations[4][2] != 0.0F)
                        glRotatef(modelRotations[4][2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, -legHeight, 0);
                glEnd();
                glPopMatrix();
            }

            glTranslated(0, 0, player.isSneaking() ? 0.25 : 0.0);

            glPushMatrix();

            glTranslated(0, player.isSneaking() ? -0.05 : 0, player.isSneaking() ? -0.01725 : 0);

            {
                glPushMatrix();
                glTranslated(-0.375 + armWidth, legHeight + 0.55, 0);

                if (!g) {
                    if (modelRotations[1][0] != 0.0F)
                        glRotatef(modelRotations[1][0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                    if (modelRotations[1][1] != 0.0F)
                        glRotatef(modelRotations[1][1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                    if (modelRotations[1][2] != 0.0F)
                        glRotatef(-modelRotations[1][2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, -0.5, 0);
                glEnd();
                glPopMatrix();
            }

            {
                glPushMatrix();
                glTranslated(0.375 - armWidth, legHeight + 0.55, 0);

                if (!g) {

                    if (modelRotations[2][0] != 0.0F)
                        glRotatef(modelRotations[2][0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                    if (modelRotations[2][1] != 0.0F)
                        glRotatef(modelRotations[2][1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                    if (modelRotations[2][2] != 0.0F)
                        glRotatef(-modelRotations[2][2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, -0.5, 0);
                glEnd();
                glPopMatrix();
            }

            {
                glRotatef(bodyYawOffset - player.rotationYawHead, 0, 1, 0);
                glPushMatrix();
                glTranslated(0.0, legHeight + 0.55, 0);

                if (!g) {
                    if (modelRotations[0][0] != 0.0F)
                        glRotatef(modelRotations[0][0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, 0.3, 0);
                glEnd();
                glPopMatrix();
            }

            glPopMatrix();

            glRotatef(player.isSneaking() ? 25 : 0, 1, 0, 0);
            glTranslated(0, player.isSneaking() ? -0.16175 : 0, player.isSneaking() ? -0.48025 : 0);

            {
                glPushMatrix();
                glTranslated(0.0, legHeight, 0);

                glBegin(GL_LINE_STRIP);
                glVertex3d(-0.125, 0, 0);
                glVertex3d(0.125, 0, 0);
                glEnd();
                glPopMatrix();
            }

            {
                glPushMatrix();
                glTranslated(0.0, legHeight, 0);

                glBegin(GL_LINE_STRIP);
                glVertex3d(0, 0, 0);
                glVertex3d(0, 0.55, 0);
                glEnd();
                glPopMatrix();
            }

            {
                glPushMatrix();
                glTranslated(0.0, legHeight + 0.55, 0);

                glBegin(GL_LINE_STRIP);
                glVertex3d(-0.375 + armWidth, 0, 0);
                glVertex3d(0.375 - armWidth, 0, 0);
                glEnd();
                glPopMatrix();
            }

            glPopMatrix();
        });
        RenderingUtil.setupRender(false);

        if (lighting) {
            glEnable(GL_LIGHTING);
        }
    }

    public static void updateModel(EntityPlayer player, ModelPlayer model) {
        float[][] rotations = modelRotations.getOrDefault(player, new float[5][3]);
        // Biped Head
        rotations[0][0] = model.bipedHead.rotateAngleX;
        rotations[0][1] = model.bipedHead.rotateAngleY;
        rotations[0][2] = model.bipedHead.rotateAngleZ;

        // Biped Right Arm
        rotations[1][0] = model.bipedRightArm.rotateAngleX;
        rotations[1][1] = model.bipedRightArm.rotateAngleY;
        rotations[1][2] = model.bipedRightArm.rotateAngleZ;

        // Biped Left Arm
        rotations[2][0] = model.bipedLeftArm.rotateAngleX;
        rotations[2][1] = model.bipedLeftArm.rotateAngleY;
        rotations[2][2] = model.bipedLeftArm.rotateAngleZ;

        // Biped Right Leg
        rotations[3][0] = model.bipedRightLeg.rotateAngleX;
        rotations[3][1] = model.bipedRightLeg.rotateAngleY;
        rotations[3][2] = model.bipedRightLeg.rotateAngleZ;

        // Biped Left Leg
        rotations[4][0] = model.bipedLeftLeg.rotateAngleX;
        rotations[4][1] = model.bipedLeftLeg.rotateAngleY;
        rotations[4][2] = model.bipedLeftLeg.rotateAngleZ;

        modelRotations.put(player, rotations);
    }

}
