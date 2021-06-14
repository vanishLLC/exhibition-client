package exhibition.module.impl.hud;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventTick;
import exhibition.management.ColorManager;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.StringConversions;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Radar extends Module {

    private ResourceLocation texture = new ResourceLocation("textures/skeetchainmail.png");

    private String SCALE = "SCALE";
    private String X = "X";
    private String Y = "Y";
    private String SIZE = "SIZE";

    public Radar(ModuleData data) {
        super(data);
        settings.put(SCALE, new Setting<>(SCALE, 2.0, "Scales the radar.", 0.1, 0.1, 5));
        settings.put(X, new Setting<>(X, 125, "X position for radar.", 5, 1, 1920));
        settings.put(Y, new Setting<>(Y, 2, "Y position for radar.", 5, 1, 1080));
        settings.put(SIZE, new Setting<>(SIZE, 125, "Size of the radar.", 5, 50, 500));
    }

    private boolean dragging;
    float hue;

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    @RegisterEvent(events = {EventRenderGui.class, EventTick.class})
    public void onEvent(Event event) {
        if (event instanceof EventRenderGui) {
            EventRenderGui er = (EventRenderGui) event;
            int size = ((Number)settings.get(SIZE).getValue()).intValue();
            float xOffset = ((Number) settings.get(X).getValue()).floatValue();
            float yOffset = ((Number) settings.get(Y).getValue()).floatValue(); // Global Y
            float pTicks = mc.timer.renderPartialTicks;
            double playerOffsetX = mc.thePlayer.posX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * pTicks;
            double playerOffSetZ = mc.thePlayer.posZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * pTicks;

            int var141 = er.getResolution().getScaledWidth();
            int var151 = er.getResolution().getScaledHeight();
            final int mouseX = Mouse.getX() * var141 / this.mc.displayWidth;
            final int mouseY = var151 - Mouse.getY() * var151 / this.mc.displayHeight - 1;

            if(mouseX >= xOffset && mouseX <= xOffset + size && mouseY >= yOffset - 3 && mouseY <= yOffset + 10 && Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                dragging = !dragging;
            }
            if(dragging && mc.currentScreen instanceof GuiChat) {
                Object newValue = (StringConversions.castNumber(Double.toString(mouseX - size/2F), 5));
                settings.get(X).setValue(newValue);
                Object newValueY = (StringConversions.castNumber(Double.toString(mouseY - 2), 5));
                settings.get(Y).setValue(newValueY);
            } else {
                dragging = false;
            }

			/*
             * The offsets are used here for the first rectangle. + 100 is the
			 * current size to extend the Y and Z
			 */
            if (hue > 255) {
                hue = 0;
            }
            float h = hue;
            float h2 = hue + 85;
            float h3 = hue + 170;
            if (h > 255) {
                h = 0;
            }
            if(h2 > 255) {
                h2 -= 255;
            }
            if(h3 > 255) {
                h3 -= 255;
            }
            int color1 = MathHelper.hsvToRGB(h / 255.0f, 0.9f, 1);
            int color2 = MathHelper.hsvToRGB(h2 / 255.0f, 0.9f, 1);
            int color3 = MathHelper.hsvToRGB(h3 / 255.0f, 0.9f, 1);
            hue += 0.1;

            RenderingUtil.rectangleBordered(xOffset, yOffset, xOffset + size, yOffset + size, 0.5, Colors.getColor(60), Colors.getColor(10));
            RenderingUtil.rectangleBordered(xOffset + 1, yOffset + 1, xOffset + size - 1, yOffset + size - 1, 1.5, Colors.getColor(60), Colors.getColor(40));

            //RenderingUtil.rectangleBordered(xOffset + 2.5, yOffset + 2.5, xOffset + size - 2.5, yOffset + size - 2.5, 0.5, Colors.getColor(61), Colors.getColor(0));
            RenderingUtil.rectangleBordered(xOffset + 2.5, yOffset + 2.5, xOffset + size - 2.5, yOffset + size - 2.5, 0.5, Colors.getColor(22), Colors.getColor(60));
            //RenderingUtil.rectangle(xOffset + 3, yOffset + 3, xOffset + size - 3, yOffset + 3.5, Colors.getColor(56, 195, 255));

            //Draw texture in ghetto way
            GlStateManager.pushMatrix();
            Depth.pre();
            Depth.mask();
            RenderingUtil.rectangle(xOffset + 3, yOffset + 3.5, xOffset + size - 3, yOffset + 4, -1);
            Depth.render(GL11.GL_LESS);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.translate(xOffset + 2.5, yOffset + 3.5, 0);
            RenderingUtil.drawIcon(1, 1, -1, -1.5F, size - 7, size - 8, 812 / 2F, 688 / 2F);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            Depth.post();
            GlStateManager.popMatrix();

            /*
             * Horizontal line of the cross
			 */
            RenderingUtil.rectangle(xOffset + (size / 2F - 0.5), yOffset + 3.5, xOffset + (size / 2F + 0.5), yOffset + size - 3.5,
                    Colors.getColor(255, 80));

			/*
			 * Vertical line of the cross
			 */

            RenderingUtil.rectangle(xOffset + 3.5, yOffset + (size / 2F - 0.5), xOffset + size - 3.5, yOffset + (size / 2F + 0.5),
                    Colors.getColor(255, 80));

            RenderingUtil.drawGradientSideways(xOffset + 3.5, yOffset + 3.5, xOffset + size/2F, yOffset + 4.5, color1, color2);
            RenderingUtil.drawGradientSideways(xOffset + size/2F, yOffset + 3.5, xOffset + size - 3.5, yOffset + 4.5, color2, color3);
            RenderingUtil.rectangle(xOffset + 3.5, yOffset + 4, xOffset + size - 3.5, yOffset + 4.5, Colors.getColor(0, 110));


            /*
			 * For every entity (Player or valid entity)
			 */

            for (Object o : mc.theWorld.getLoadedEntityList()) {
                if (o instanceof EntityPlayer) {
                    EntityPlayer ent = (EntityPlayer) o;
                    if (ent.isEntityAlive() && ent != mc.thePlayer && !(ent.isInvisible() || ent.isInvisibleToPlayer(mc.thePlayer))) {
						/*
						 * (targetPlayer posX - localPlayer posX) * Distance
						 * Scale
						 */

                        float posX = (float) (((ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks) -
                                playerOffsetX) * ((Number) settings.get(SCALE).getValue()).doubleValue());
						/*
						 * (targetPlayer posZ - localPlayer posZ) * Distance
						 * Scale
						 */
                        float posZ = (float) (((ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks) -
                                playerOffSetZ) * ((Number) settings.get(SCALE).getValue()).doubleValue());
                        int color;
                        // Gay Friend Check
                        if (FriendManager.isFriend(ent.getName())) {
                            color = mc.thePlayer.canEntityBeSeen(ent) ? Colors.getColor(0, 195, 255)
                                    : Colors.getColor(0, 195, 255);
                        } else {
                            color = mc.thePlayer.canEntityBeSeen(ent) ? ColorManager.getEnemyVisible().getColorHex()
                                    : ColorManager.getEnemyInvisible().getColorHex();
                        }

						/*
						 * Fuck Ms. Goble's geometry class.
						 */

                        float cos = (float) Math.cos(mc.thePlayer.rotationYaw * (Math.PI * 2 / 360));
                        float sin = (float) Math.sin(mc.thePlayer.rotationYaw * (Math.PI * 2 / 360));
                        float rotY = -(posZ * cos - posX * sin);
                        float rotX = -(posX * cos + posZ * sin);
						/*
						 * Clamps to the edge of the radar, have it less than
						 * the radar if you don't want squares to come out.
						 */
                        if (rotY > (size / 2F - 5)) {
                            rotY = (size / 2F - 5f);
                        } else if (rotY < -(size / 2F - 5)) {
                            rotY = -(size / 2F - 5);
                        }
                        if (rotX > (size / 2F - 5f)) {
                            rotX = (size / 2F - 5);
                        } else if (rotX < -(size / 2F - 5)) {
                            rotX = -(size / 2F - 5f);
                        }
                        RenderingUtil.rectangleBordered(xOffset + (size / 2F) + rotX - 1.5,
                                yOffset + (size / 2F) + rotY - 1.5, xOffset + (size / 2F) + rotX + 1.5,
                                yOffset + (size / 2F) + rotY + 1.5, 0.5, color, Colors.getColor(0));
                    }
                }
            }
        }
			/*
			 * LocalPlayer square, doesn't need any pointers just rendered on
			 * the radar.
			 */
    }
}
