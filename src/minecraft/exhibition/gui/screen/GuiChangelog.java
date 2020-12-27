package exhibition.gui.screen;

import exhibition.Client;
import exhibition.gui.screen.component.GuiMenuButton;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.animate.Opacity;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import exhibition.util.security.Connection;
import exhibition.util.security.SSLConnector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_LESS;

public class GuiChangelog extends PanoramaScreen {

    private int offset;
    private boolean isDragging;

    private String[] changeLogList = new String[]{"Loading Changelog"};

    private boolean first;

    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/logo.png");

    private boolean hasRead = false;

    public GuiChangelog() {
    }

    @Override
    public void initGui() {
        super.initGui();


        new Thread(() -> {
            try {
                changeLogList = SSLConnector.get(new Connection("https://minesense.pub/nig/" + "C")).split("\n");
            } catch (Exception ignored) {

            }
        }).start();

        buttonList.add(new GuiMenuButton(0, width / 2 - 100, this.height - 20, 200, 12, "Okay"));

        if (!first) {
            opacity = new Opacity(300);
            first = true;
            buttonList.get(0).enabled = hasRead;
        }
    }

    private Opacity opacity;

    private boolean firstDraw = true;

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (firstDraw) {
            opacity.setOpacity(300);
            opacity.interp(300, 0);
            firstDraw = false;
        }
        renderSkybox(mouseX, mouseY, partialTicks);

        GlStateManager.pushMatrix();
        String ver = "Changelog \247a" + Client.parsedVersion.substring(0, 2) + "/" + Client.parsedVersion.substring(2, 4) + "/20" + Client.parsedVersion.substring(4, 6);
        GlStateManager.translate(this.width/2F - mc.fontRendererObj.getStringWidth(ver),10, 0);
        GlStateManager.scale(2,2,2);
        mc.fontRendererObj.drawStringWithShadow(ver, 0, 0, -1);
        GlStateManager.popMatrix();

        RenderingUtil.rectangleBordered(50.0f, 33.0f, this.width - 50, this.height - 51, 1.0f, Colors.getColor(60, 100), Colors.getColor(0, 100));
        GL11.glPushMatrix();
        this.prepareScissorBox(0.0f, 33.0f, this.width, this.height - 50);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int maximumInRegion = ((34 + this.height - 51) / 12) - 7;

        float totalHeight = (changeLogList.length * 12F);

        int maxOffset = (totalHeight > (maximumInRegion * 12) ? (changeLogList.length * 12) - (maximumInRegion * 12) : 0);

        if (Mouse.hasWheel()) {
            final int wheel = Mouse.getDWheel();
            if (wheel < 0 && offset < maxOffset) {
                this.offset += 12;
                if (this.offset < 0) {
                    this.offset = 0;
                }
            } else if (wheel > 0) {
                this.offset -= 12;
                if (this.offset < 0) {
                    this.offset = 0;
                }
            }
        }

        float sliderBarLength = ((this.height - 51 - 34));

        if (totalHeight > (maximumInRegion * 12)) {
            float offsetValueThing = (offset / 12F);

            boolean isWithingRegion = mouseX >= this.width - 50.5 && mouseX <= this.width - 45.5 && mouseY >= 34 && mouseY <= this.height - 51;

            RenderingUtil.rectangleBordered(this.width - 50, 33, this.width - 46, this.height - 51, 0.5, Colors.getColor(60, 100), Colors.getColor(0, 100));

            float relativeScale = sliderBarLength * ((maximumInRegion * 12) / totalHeight);
            float relativeOffset = (sliderBarLength) * (offsetValueThing / changeLogList.length);
            float yPos = 34 + relativeOffset;

            if (isDragging && !Mouse.isButtonDown(0)) {
                isDragging = false;
            }

            if (isWithingRegion) {
                if (Mouse.isButtonDown(0)) {
                    this.isDragging = true;
                }
            }
            if (isDragging) {
                float positionRelativeToOffset = (mouseY - 34 - relativeScale / 2) / (sliderBarLength);
                this.offset = ((int) (totalHeight * positionRelativeToOffset) / 12) * 12;
            }
            boolean hovering = mouseX >= this.width - 49 && mouseX <= this.width - 47 && mouseY >= yPos && mouseY <= yPos + relativeScale;

            RenderingUtil.rectangle(this.width - 49, yPos, this.width - 47, yPos + relativeScale - 1, isDragging ? -1 : hovering ? Colors.getColor(255, 175) : Colors.getColor(255, 125));
        }

        if (Keyboard.isKeyDown(200)) {
            this.offset -= 12;
        } else if (Keyboard.isKeyDown(208) && offset < maxOffset) {
            this.offset += 12;
        }
        if (this.offset <= 0) {
            this.offset = 0;
        }
        if (offset >= maxOffset) {
            offset = maxOffset;
            hasRead = true;
            buttonList.get(0).enabled = true;
        }

        int y = 35 - offset;
        for (String bruh : changeLogList) {
            String str = bruh.trim();
            Depth.pre();
            Depth.mask();
            mc.fontRendererObj.drawString(str, 53, y, Colors.getColor(255));
            Depth.render(GL_LESS);
            mc.fontRendererObj.drawString(str, 53 - 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 53 + 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 53, y + 1, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 53, y - 1, Colors.getColor(5, 175));
            Depth.post();
            if (str.startsWith(">")) {
                str = "\2476" + str;
            } else if (str.startsWith("?")) {
                str = "\2479" + str;
            } else if (str.startsWith("+")) {
                str = "\247a" + str;
            } else if (str.startsWith("-")) {
                str = "\247c" + str;
            }
            mc.fontRendererObj.drawString(str, 53, y, Colors.getColor(255,175));
            y += 12;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();



        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution scaledresolution = new ScaledResolution(mc);
        int j = 256;
        int k = 256;

        float fadePercent = (255 - MathHelper.clamp_float(opacity.getOpacity(), 0, 255)) / 255F;

        float nigga = 1 - (fadePercent * fadePercent);

        int fadeOpacity = (int) (255 * nigga);

        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle((scaledresolution.getScaledWidth() - j * nigga) / 2, (scaledresolution.getScaledHeight() - k * nigga) / 2, (scaledresolution.getScaledWidth() + j * nigga) / 2, (scaledresolution.getScaledHeight() + k * nigga) / 2, -1);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        Depth.render(GL11.GL_LESS);

        GlStateManager.color(2, 2, 1, fadeOpacity / 255F);
        mc.getTextureManager().bindTexture(locationMojangPng);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double) mc.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, (int) (fadeOpacity)).endVertex();
        worldrenderer.pos((double) mc.displayWidth, (double) mc.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, (int) (fadeOpacity)).endVertex();
        worldrenderer.pos((double) mc.displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, (int) (fadeOpacity)).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, (int) (fadeOpacity)).endVertex();
        tessellator.draw();

        Depth.post();

        GlStateManager.color(1.0F, 1.0F, 1.0F, fadeOpacity / 255F);

        GlStateManager.translate((-j / 2F) * nigga, (-k / 2F) * nigga, 0);
        GlStateManager.translate((scaledresolution.getScaledWidth()) / 2F, (scaledresolution.getScaledHeight()) / 2F, 0);
        GlStateManager.scale(nigga, nigga, 0);
        mc.func_181536_a(0, 0, 0, 0, j, k, 255, 255, 255, (int) (fadeOpacity));
        GlStateManager.scale(0.5, 0.5, 0);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        opacity.interp(0, 5);

    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id == 0 && button.enabled) {
            mc.displayGuiScreen(new GuiLoginMenu(false));
        }
    }

    private void prepareScissorBox(final float x, final float y, final float x2, final float y2) {
        final ScaledResolution scale = new ScaledResolution(this.mc);
        final int factor = scale.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    @Override
    protected void keyTyped(final char character, final int key) {

    }

}
