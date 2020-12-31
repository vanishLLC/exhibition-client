package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderEntity;
import exhibition.management.ColorManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import org.lwjgl.opengl.GL11;

public class SilentView extends Module {

    private Setting<Boolean> ghost = new Setting<>("GHOST", true, "Renders a ghost model of your silent view.");

    private float lastYaw, currentYaw;
    private float lastPitch, currentPitch;

    private boolean isSilent = false;

    public float lastSilentYaw = -1337;
    public float lastSilentPitch = -1337;

    public SilentView(ModuleData data) {
        super(data);

        addSetting(ghost);
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            lastYaw = mc.thePlayer.rotationYaw;
            lastPitch = mc.thePlayer.rotationPitch;
        }
    }

    @RegisterEvent(events = {EventMotionUpdate.class, EventRender3D.class, EventRenderEntity.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost() && !em.isCancelled()) {
                if (em.getYaw() != mc.thePlayer.rotationYaw || em.getPitch() != mc.thePlayer.rotationPitch) {
                    lastYaw = currentYaw;
                    lastPitch = currentPitch;

                    currentYaw = em.getYaw();
                    currentPitch = em.getPitch();

                    isSilent = true;
                } else {
                    if (isSilent) {
                        lastSilentYaw = currentYaw;
                        lastSilentPitch = currentPitch;
                        isSilent = false;
                    } else {
                        lastSilentYaw = -1337;
                        lastSilentPitch = -1337;
                    }

                    lastYaw = mc.thePlayer.rotationYaw;
                    lastPitch = mc.thePlayer.rotationPitch;
                    currentYaw = mc.thePlayer.rotationYaw;
                    currentPitch = mc.thePlayer.rotationPitch;
                }
            }
        }
        if (event instanceof EventRender3D) {
            if (ghost.getValue() && isSilent && mc.gameSettings.thirdPersonView != 0) {
                EventRender3D er = event.cast();

                RenderingUtil.pre3D();
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);

//                double x = (mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * er.renderPartialTicks) - RenderManager.renderPosX;
//                double y = (mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * er.renderPartialTicks) - RenderManager.renderPosY;
//                double z = (mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * er.renderPartialTicks) - RenderManager.renderPosZ;

                float prevRotationYawHead = mc.thePlayer.prevRotationYawHead;
                float rotationYawHead = mc.thePlayer.rotationYawHead;

                float prevRenderYawOffset = mc.thePlayer.prevRenderYawOffset;
                float renderYawOffset = mc.thePlayer.renderYawOffset;

                float prevRotationPitch = mc.thePlayer.prevRotationPitch;
                float rotationPitch = mc.thePlayer.rotationPitch;

                mc.thePlayer.prevRotationYawHead = interpolatedYaw(er.renderPartialTicks);
                mc.thePlayer.rotationYawHead = interpolatedYaw(er.renderPartialTicks);

                mc.thePlayer.prevRenderYawOffset = interpolatedYaw(er.renderPartialTicks);
                mc.thePlayer.renderYawOffset = interpolatedYaw(er.renderPartialTicks);

                mc.thePlayer.prevRotationPitch = interpolatedPitch(er.renderPartialTicks);
                mc.thePlayer.rotationPitch = interpolatedPitch(er.renderPartialTicks);

                RendererLivingEntity.renderLayers = false;
                RendererLivingEntity.ignoreChams = true;
                Render var11;
                try {
                    var11 = mc.getRenderManager().getEntityRenderObject(mc.thePlayer);
                    if (var11 != null && mc.getRenderManager().renderEngine != null) {
                        if (var11 instanceof RenderPlayer) {
                            RenderPlayer rp = (RenderPlayer) var11;

                            GL11.glPushMatrix();
                            //GL11.glDisable(GL11.GL_DEPTH_TEST);
                            GL11.glDisable(GL11.GL_TEXTURE_2D);
                            GL11.glEnable(GL11.GL_BLEND);

                            boolean bruh = false;
                            float oldgamma = mc.gameSettings.gammaSetting;

                            Chams chams = (Chams) Client.getModuleManager().get(Chams.class);

                            if ((boolean) chams.getSetting("FLAT").getValue()) {
                                if (GL11.glIsEnabled(GL11.GL_LIGHTING)) {
                                    bruh = true;
                                    GL11.glDisable(GL11.GL_LIGHTING);
                                }
                                mc.gameSettings.gammaSetting = 10000000;
                                mc.entityRenderer.forceUpdateLightmap(mc.timer.renderPartialTicks);
                            }

//                            RenderingUtil.glColor(ColorManager.getFriendlyInvisible().getColorHex());
//
//                            ((RendererLivingEntity) var11).doRender(mc.thePlayer, 0, 0, 0, 0, er.renderPartialTicks);

                            RenderingUtil.glColor(Colors.getColorOpacity(ColorManager.getFriendlyVisible().getColorHex(), 100));

                            RendererLivingEntity.ignoreChams = true;
                            rp.doRender(mc.thePlayer, 0, 0, 0, 0, er.renderPartialTicks);
                            RendererLivingEntity.ignoreChams = false;

                            GL11.glDisable(GL11.GL_BLEND);
                            GL11.glEnable(GL11.GL_TEXTURE_2D);
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                            if ((Boolean) chams.getSetting("FLAT").getValue()) {
                                mc.gameSettings.gammaSetting = oldgamma;
                                if (bruh)
                                    GL11.glEnable(GL11.GL_LIGHTING);
                                mc.entityRenderer.forceUpdateLightmap(mc.timer.renderPartialTicks);
                            }
                            GL11.glPopMatrix();

                        }
                    }
                } catch (Exception ignored) {
                }

                RendererLivingEntity.ignoreChams = false;
                RendererLivingEntity.renderLayers = true;

                mc.thePlayer.prevRotationYawHead = prevRotationYawHead;
                mc.thePlayer.rotationYawHead = rotationYawHead;

                mc.thePlayer.prevRenderYawOffset = prevRenderYawOffset;
                mc.thePlayer.renderYawOffset = renderYawOffset;

                mc.thePlayer.prevRotationPitch = prevRotationPitch;
                mc.thePlayer.rotationPitch = rotationPitch;

                RenderingUtil.post3D();

            }
        }

        if (event instanceof EventRenderEntity) {
            EventRenderEntity er = event.cast();

            if (shouldBeTransparent() && er.getEntity() == mc.thePlayer) {
                if (er.isPre()) {
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glColor4f(1, 1, 1, 0.3F);
                } else {
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glColor4f(1, 1, 1, 1);
                }
            }


        }
    }

    public boolean shouldBeTransparent() {
        return isSilent && ghost.getValue();
    }

    public boolean silent() {
        return isSilent && !ghost.getValue();
    }

    public float interpolatedYaw(float pticks) {
        return lastYaw + (currentYaw - lastYaw) * pticks;
    }

    public float interpolatedPitch(float pticks) {
        return lastPitch + (currentPitch - lastPitch) * pticks;
    }

}
