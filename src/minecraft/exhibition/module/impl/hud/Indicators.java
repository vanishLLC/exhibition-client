package exhibition.module.impl.hud;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.ColorManager;
import exhibition.management.animate.Opacity;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.render.Nametags;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class Indicators extends Module {

    private Map<Entity, Nametags.Bruh> entityConvertedPointsMap = new HashMap<>();
    private Opacity opacity = new Opacity(0);

    private String RADIUS = "RADIUS";
    private String INVISIBLES = "INVISIBLES";
    private Setting VISCHECK = new Setting<>("VISCHECK", true, "Show only when visible.");

    public Indicators(ModuleData data) {
        super(data);
        settings.put(RADIUS, new Setting<>(RADIUS, 100, "Radius for indicators.", 5, 15, 100));
        settings.put(INVISIBLES, new Setting<>(INVISIBLES, false, "Show invisibles."));
        settings.put(VISCHECK.getName(), VISCHECK);
    }

    boolean forward = true;

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    private final Nametags.Bruh defaultTo = new Nametags.Bruh(new double[]{-1337, -1337, -1337, -1337});

    @RegisterEvent(events = {EventRenderGui.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D) {
            updatePositions();
        } else {
            if (forward && opacity.getOpacity() >= 300) {
                forward = false;
            } else if (!forward && opacity.getOpacity() <= 25) {
                forward = true;
            }
            opacity.interp(forward ? 300 : 25, 10);

            int alpha = (int) opacity.getOpacity();
            if (alpha > 255) {
                alpha = 255;
            } else if (alpha < 0) {
                alpha = 0;
            }

            EventRenderGui er = event.cast();
            ScaledResolution scaledRes = er.getResolution();
            double w = scaledRes.getScaledWidth_double() / 2;
            double h = scaledRes.getScaledHeight_double() / 2;
            List<Entity> removal = new ArrayList<>();
            for (Map.Entry<Entity, Nametags.Bruh> e : entityConvertedPointsMap.entrySet()) {
                if (!mc.theWorld.getLoadedEntityList().contains(e.getKey())) {
                    removal.add(e.getKey());
                }
            }
            removal.forEach(e -> entityConvertedPointsMap.remove(e));

            GlStateManager.pushMatrix();
            for (Entity ent : mc.theWorld.getLoadedEntityList()) {
                boolean isVisible = mc.thePlayer.canEntityBeSeen(ent);

                if (!(ent instanceof EntityPlayer) || ent instanceof EntityPlayerSP || ((boolean) VISCHECK.getValue() && !isVisible))
                    continue;

                double[] renderPositions = entityConvertedPointsMap.getOrDefault(ent, defaultTo).array;
                if (renderPositions[0] == -1337)
                    continue;

                if (AntiBot.isBot(ent))
                    continue;

                if ((((boolean) settings.get(INVISIBLES).getValue() && (ent.isInvisible() && !AntiBot.isBot(ent))) || !ent.isInvisible())) {
                    if (!(isInView(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), scaledRes)) || renderPositions[2] > 1) {
                        float angle = findAngle(w, renderPositions[0] / scaledRes.getScaleFactor(), h, renderPositions[1] / scaledRes.getScaleFactor()) + (renderPositions[2] > 1 ? 180 : 0);

                        double width = scaledRes.getScaledWidth_double() / 2 - 20;
                        double height = scaledRes.getScaledHeight_double() / 2 - 20;

                        double a = width * (Math.min(((Number) settings.get(RADIUS).getValue()).intValue(), 100) / 100.0);
                        double b = height * (Math.min(((Number) settings.get(RADIUS).getValue()).intValue(), 100) / 100.0);

                        double x = Math.sqrt(1.0 / (1 / (a * a) + Math.pow(Math.tan(Math.toRadians(angle)), 2) / (b * b)));
                        double y = (Math.tan(Math.toRadians(angle))) * x;

                        float calculatedAngle = MathHelper.wrapAngleTo180_float(angle + 90);

                        if (calculatedAngle < 0) {
                            x = -x;
                            if (calculatedAngle > -180) {
                                y = -y;
                            }
                        }

                   /* double t = Math.atan( scaledRes.getScaledWidth() * Math.tan( angle * Math.PI/180.0 ) / scaledRes.getScaledHeight()) + Math.PI;
                    double x1 = scaledRes.getScaledWidth() - 50 * Math.cos(t);
                    double y1 = scaledRes.getScaledHeight() - 50 * Math.sin(t);*/

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(x + w, y + h, 0);
                        GlStateManager.rotate(angle - 90, 0, 0, 1);

                        int color = isVisible ? ColorManager.getEnemyVisible().getColorHex() : ColorManager.getEnemyInvisible().getColorHex();
                        if (FriendManager.isFriend(ent.getName())) {
                            color = isVisible ? ColorManager.getFriendlyVisible().getColorHex() : ColorManager.getFriendlyInvisible().getColorHex();
                        }


                        int f = alpha;
                        int colorD = Colors.getColorOpacity(color, f);

                        GL11.glPushMatrix();
                        RenderingUtil.enableGL2D();

                        GL11.glLineWidth(2.5F);
                        RenderingUtil.glColor(Colors.getColor(0, alpha));
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        GL11.glVertex2f(-6.5F, -0.5F);
                        GL11.glVertex2f(0, 14.5F);
                        GL11.glVertex2f(6.5F, -0.5F);
                        GL11.glEnd();

                        RenderingUtil.glColor(colorD);
                        GL11.glLineWidth(1);

                        Depth.pre();
                        Depth.mask();
                        GL11.glBegin(GL11.GL_POLYGON);
                        GL11.glVertex2f(-6, 0);
                        GL11.glVertex2f(0, 14);
                        GL11.glVertex2f(6, 0);
                        GL11.glEnd();
                        Depth.render(GL11.GL_LESS);
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        GL11.glVertex2f(-6, 0);
                        GL11.glVertex2f(0, 14);
                        GL11.glVertex2f(6, 0);
                        GL11.glEnd();
                        Depth.post();

                        GL11.glBegin(GL11.GL_POLYGON);
                        GL11.glVertex2f(-6, 0);
                        GL11.glVertex2f(0, 14);
                        GL11.glVertex2f(6, 0);
                        GL11.glEnd();
                        RenderingUtil.disableGL2D();
                        GL11.glPopMatrix();

                        RenderingUtil.glColor(-1);
                        GlStateManager.popMatrix();

                    }
                /*mc.fontRendererObj.drawStringWithShadow(waypoint.getName() + "\247a " +
                        (int)renderPositions[0]/scaledRes.getScaleFactor() + "\247b " +
                        (int)renderPositions[1]/scaledRes.getScaleFactor() + "\247c " +
                        (int)renderPositions[2] + "\247e " +
                        (isInView(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), scaledRes, waypoint)),300,y1,-1);
            */
                }
            }
            GlStateManager.popMatrix();
        }

    }

    private void updatePositions() {
        float pTicks = mc.timer.renderPartialTicks;
        for (Entity ent : mc.theWorld.getLoadedEntityList()) {
            double x = ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks - mc.getRenderManager().viewerPosX;
            double y = ent.lastTickPosY + 1 + (ent.posY - ent.lastTickPosY) * pTicks - mc.getRenderManager().viewerPosY;
            double z = ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks - mc.getRenderManager().viewerPosZ;
            if (ent instanceof EntityPlayer) {
                if (ent != mc.thePlayer) {

                    Nametags.Bruh bruh;
                    if (entityConvertedPointsMap.containsKey(ent)) {
                        bruh = entityConvertedPointsMap.get(ent);
                    } else {
                        bruh = new Nametags.Bruh(new double[4]);
                        entityConvertedPointsMap.put(ent, bruh);
                    }
                    RenderingUtil.worldToScreenOptimized(x, y, z, bruh);
                    if (bruh.array[0] == -1337) {
                        entityConvertedPointsMap.remove(ent);
                    }
                }
            }
        }
    }

    private float findAngle(double x, double x2, double y, double y2) {
        return (float) (Math.toDegrees(Math.atan2(y2 - y, x2 - x)));
    }

    private boolean isInView(double x, double y, ScaledResolution resolution) {
        return x > 0 && y > 0 && x < resolution.getScaledWidth() && y < resolution.getScaledHeight();
    }
}
