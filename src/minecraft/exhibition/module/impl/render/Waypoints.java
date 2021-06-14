package exhibition.module.impl.render;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.animate.Opacity;
import exhibition.management.font.TTFFontRenderer;
import exhibition.management.waypoints.Waypoint;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arithmo on 5/15/2017 at 2:33 PM.
 */
public class Waypoints extends Module {

    private double gradualFOVModifier;
    public static Map<Waypoint, Bruh> waypointMap = new HashMap<>();
    private Opacity opacity = new Opacity(0);
    private String ARROWS = "ARROWS";
    private String RADIUS = "RADIUS";

    public Waypoints(ModuleData data) {
        super(data);
        settings.put(ARROWS, new Setting<>(ARROWS, true, "Shows arrows for out of FOV waypoints."));
        settings.put(RADIUS, new Setting<>(RADIUS, 100, "Radius for arrows.", 5, 15, 100));
    }

    boolean forward = true;

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventRenderGui.class, EventRender3D.class})
    public void onEvent(Event event) {
        if (event instanceof EventRender3D) {
            updatePositions();
        } else {
            EventRenderGui er = event.cast();
            GlStateManager.pushMatrix();
            ScaledResolution scaledRes = er.getResolution();
            double w = scaledRes.getScaledWidth_double() / 2;
            double h = scaledRes.getScaledHeight_double() / 2;
            for (Waypoint waypoint : waypointMap.keySet()) {
                double[] renderPositions = waypointMap.get(waypoint).array;
                if (!(mc.getCurrentServerData() != null ? waypoint.getAddress().equals(mc.getCurrentServerData().serverIP) : waypoint.getAddress().equals("SINGLEPLAYER"))) {
                    continue;
                }
                if (isInView(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), scaledRes, waypoint) && renderPositions[2] < 1) {
                    GlStateManager.pushMatrix();
                    String str = "\247l" + waypoint.getName() + " \247a" + (int) mc.thePlayer.getDistance(waypoint.getVec3().xCoord, waypoint.getVec3().yCoord, waypoint.getVec3().zCoord) + "m ";
                    TTFFontRenderer font = Client.nametagsFont;
                    GlStateManager.translate(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), 0.0D);
                    scale();
                    GlStateManager.translate(0.0D, -2.5D, 0.0D);
                    float strWidth = font.getWidth(str);
                    RenderingUtil.rectangleBordered(-strWidth / 2 - 3, -12.0D, strWidth / 2 + 3, 1.0D, 0.5f, Colors.getColor(0, 100), waypoint.getColor());
                    GlStateManager.color(1.0F, 1.0F, 1.0F);
                    font.drawStringWithShadow(str, -strWidth / 2, -9.5f, -1);
                    GlStateManager.rotate(90, 0, 0, 1);
                    RenderingUtil.drawCircle(3f, 0, 4, 3, waypoint.getColor());
                    RenderingUtil.drawCircle(3f, 0, 3, 3, waypoint.getColor());
                    RenderingUtil.drawCircle(3f, 0, 2, 3, waypoint.getColor());
                    RenderingUtil.drawCircle(3f, 0, 1, 3, waypoint.getColor());

                    GlStateManager.popMatrix();

                } else if ((Boolean) settings.get(ARROWS).getValue()) {

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
                    GlStateManager.rotate(angle, 0, 0, 1);
                    GlStateManager.scale(1.5f, 1, 1);

                    if (forward && opacity.getOpacity() >= 300) {
                        forward = false;
                    } else if (!forward && opacity.getOpacity() <= 25) {
                        forward = true;
                    }
                    opacity.interp(forward ? 300 : 25, 3);

                    int alpha = (int) opacity.getOpacity();
                    if (alpha > 255) {
                        alpha = 255;
                    } else if (alpha < 0) {
                        alpha = 0;
                    }


                    int f = alpha;
                    int f1 = (waypoint.getColor() >> 16 & 0xFF);
                    int f2 = (waypoint.getColor() >> 8 & 0xFF);
                    int f3 = (waypoint.getColor() & 0xFF);
                    int color = Colors.getColor(f1, f2, f3, f);

                    RenderingUtil.drawCircle(0, 0, 6, 3, Colors.getColor(0, f));
                    RenderingUtil.drawCircle(0, 0, 5, 3, color);
                    RenderingUtil.drawCircle(0, 0, 4, 3, color);
                    RenderingUtil.drawCircle(0, 0, 3, 3, color);
                    RenderingUtil.drawCircle(0, 0, 2, 3, color);
                    RenderingUtil.drawCircle(0, 0, 1, 3, color);
                    RenderingUtil.drawCircle(0, 0, 0f, 3, color);

                    GlStateManager.popMatrix();

                }
                /*mc.fontRendererObj.drawStringWithShadow(waypoint.getName() + "\247a " +
                        (int)renderPositions[0]/scaledRes.getScaleFactor() + "\247b " +
                        (int)renderPositions[1]/scaledRes.getScaleFactor() + "\247c " +
                        (int)renderPositions[2] + "\247e " +
                        (isInView(renderPositions[0] / scaledRes.getScaleFactor(), renderPositions[1] / scaledRes.getScaleFactor(), scaledRes, waypoint)),300,y1,-1);
            */
            }
            GlStateManager.popMatrix();

        }
    }

    public double[] isInFOV(double x, double y) {
        return null;
    }

    private void scale() {
        float scale = 1;
        float target = scale
                * (mc.gameSettings.fovSetting / (mc.gameSettings.fovSetting * mc.thePlayer.getFovModifier()));
        if ((this.gradualFOVModifier == 0.0D) || (Double.isNaN(this.gradualFOVModifier))) {
            this.gradualFOVModifier = target;
        }
        this.gradualFOVModifier += (target - this.gradualFOVModifier) / (Minecraft.getDebugFPS() * 0.7D);

        scale = (float) (scale * this.gradualFOVModifier);

        scale *= ((mc.currentScreen == null) && (GameSettings.isKeyDown(mc.gameSettings.ofKeyBindZoom)) ? 3 : 1);
        GlStateManager.scale(scale, scale, scale);
    }

    private void updatePositions() {
        waypointMap.clear();
        for (Waypoint waypoint : Client.waypointManager.getWaypoints()) {
            double x = waypoint.getVec3().xCoord - mc.getRenderManager().viewerPosX;
            double y = waypoint.getVec3().yCoord - mc.getRenderManager().viewerPosY;
            double z = waypoint.getVec3().zCoord - mc.getRenderManager().viewerPosZ;
            y += 0.2D;

            Waypoints.Bruh bruh;
            if (waypointMap.containsKey(waypoint)) {
                bruh = waypointMap.get(waypoint);
            } else {
                bruh = new Waypoints.Bruh(new double[4]);
                waypointMap.put(waypoint, bruh);
            }
            optimizedW2S(x, y, z, bruh);
            if (bruh.array[0] == -1337) {
                waypointMap.remove(waypoint);
            }
        }
    }

    private void optimizedW2S(double x, double y, double z, Bruh bruh) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, bruh.modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, bruh.projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, bruh.viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, bruh.modelView, bruh.projection, bruh.viewport, bruh.screenCoords);
        if (result) {
            bruh.array[0] = bruh.screenCoords.get(0);
            bruh.array[1] = Display.getHeight() - bruh.screenCoords.get(1);
            bruh.array[2] = bruh.screenCoords.get(2);
        } else {
            bruh.array[0] = bruh.array[1] = bruh.array[2] = -1337;
        }
    }

    private float findAngle(double x, double x2, double y, double y2) {
        return (float) (Math.toDegrees(Math.atan2(y2 - y, x2 - x)));
    }

    private boolean isInView(double x, double y, ScaledResolution resolution, Waypoint waypoint) {
        return x > 0 && y > 0 && x < resolution.getScaledWidth() && y < resolution.getScaledHeight();
    }

    public static class Bruh {

        public FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        public IntBuffer viewport = BufferUtils.createIntBuffer(16);
        public FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        public FloatBuffer projection = BufferUtils.createFloatBuffer(16);

        // public boolean isImportant = false;

        public double[] array = new double[3];

        public Bruh(double[] array) {
            this.array[0] = array[0];
            this.array[1] = array[1];
            this.array[2] = array[2];
        }

    }

}

