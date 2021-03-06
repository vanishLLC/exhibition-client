package exhibition.gui.screen.impl.mainmenu;

import exhibition.Client;
import exhibition.gui.altmanager.GuiAltManager;
import exhibition.gui.screen.component.GuiMenuButton;
import exhibition.management.ColorManager;
import exhibition.management.animate.Opacity;
import exhibition.management.animate.Translate;
import exhibition.management.animate.particles.ParticleContainer;
import exhibition.module.impl.hud.HUD;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import exhibition.util.security.AuthenticationUtil;
import exhibition.util.security.Connection;
import exhibition.util.security.Crypto;
import exhibition.util.security.SSLConnector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

import static org.lwjgl.opengl.GL11.*;

public class GuiModdedMainMenu extends GuiMainMenu {

    private ResourceLocation uparrow = new ResourceLocation("textures/skeetchainmail.png");

    private Translate translate;
    private Opacity scale;
    private boolean backwards;
    private float angle = MathUtils.randomNumber(-90, 90);

    private String[] changeLogList = new String[]{"."};
    private String[] announcements = new String[]{"."};

    private ParticleContainer particles;

    private boolean isBirthday;
    public static boolean first;

    public static PositionedSoundRecord menuSong;

    public GuiModdedMainMenu() {
        try {
            new Thread(() -> {
                try {
                    if (!first) {
                        mc.getSoundHandler().stopSounds();
                        mc.getSoundHandler().playSound(GuiModdedMainMenu.menuSong);
                        first = true;
                    }
                    Connection changeLog = new Connection("https://minesense.pub/nig/C");
                    SSLConnector.get(changeLog);
                    changeLogList = changeLog.getResponse().split("\n");

                    Connection announcementsConnection = new Connection("https://minesense.pub/nig/A");
                    SSLConnector.get(announcementsConnection);
                    announcements = announcementsConnection.getResponse().split("\n");
                } catch (Exception ignored) {

                }
            }).start();
        } catch (Exception ignored) {

        }
    }

    @Override
    public void initGui() {
        super.initGui();

        LocalDate today = LocalDate.now();
        LocalDate birthDayStart = today.withMonth(Month.JANUARY.getValue()).withDayOfMonth(10);
        LocalDate birthDayEnd = birthDayStart.plusDays(1);
        isBirthday = !today.isBefore(birthDayStart) && today.isBefore(birthDayEnd);


        particles = new ParticleContainer(-50, -50, width + 50, height + 50, 300, 0.5, -1);
        translate = new Translate(MathUtils.randomNumber(width - 20, 20), MathUtils.randomNumber(height - 20, 20));
        scale = new Opacity(0);
        this.buttonList.clear();
        String strSSP = "Singleplayer";
        String strSMP = "Multiplayer";
        String strOptions = "Options";
        String strQuit = "\247cExit Game";
        String strLang = "Language";
        String strAccounts = "Accounts";
        int initHeight = this.height / 4 + 48;
        int objHeight = 12;
        int objWidth = (24 * 4) / 2 + 15;
        int xMid = width / 2 - objWidth / 2;
        this.buttonList.add(new GuiMenuButton(0, xMid, initHeight, objWidth, objHeight, strSSP));
        this.buttonList.add(new GuiMenuButton(1, xMid, initHeight + 20, objWidth, objHeight, strSMP));
        this.buttonList.add(new GuiMenuButton(2, xMid, initHeight + 80, objWidth, objHeight, strOptions));
        this.buttonList.add(new GuiMenuButton(3, xMid, initHeight + 60, objWidth, objHeight, strLang));
        this.buttonList.add(new GuiMenuButton(4, xMid, initHeight + 40, objWidth, objHeight, strAccounts));
        this.buttonList.add(new GuiMenuButton(5, xMid, initHeight + 100, objWidth, objHeight, strQuit));
        if (AuthenticationUtil.getHwid() == 32161752 && mc.displayWidth < 1920 && mc.displayHeight < 1080) {
            this.buttonList.add(new GuiMenuButton(6, 2, height - 30, objWidth, objHeight, "Resize 1080p"));
        }

    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);

        try {
            if (x > 2 && x < 2 + mc.fontRendererObj.getStringWidth("MineSense Forums") && y > height - 12 && y < height - 1 && button == 0) {
                openURI(new URL("https://minesense.pub/forum").toURI());
            }
        } catch (URISyntaxException ignored) {

        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        } else if (button.id == 1) {
            if (!Client.getAuthUser().hashCheck())
                mc.displayGuiScreen(new GuiMultiplayer(this));
        } else if (button.id == 2) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        } else if (button.id == 3) {
            mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager()));
        } else if (button.id == 4) {
            mc.displayGuiScreen(new GuiAltManager(this));
        } else if (button.id == 5) {
            mc.shutdown();
        } else if (button.id == 6) {
            try {
                DisplayMode displayMode = new DisplayMode(1920, 1080);
                Display.setDisplayMode(displayMode);
                mc.resize(1920, 1080);
                Display.setResizable(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    int lastRotX = 0, lastRotY = 0, lastRotZ = 0;

    float lastHue = (float) Math.random();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        super.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        //String retardedShit = "----------";

        /*
            Render stupid shit lol
         */
//

        float cos = (float) Math.cos(angle * (Math.PI * 2 / 360));
        float sin = (float) Math.sin(angle * (Math.PI * 2 / 360));
        float rotY = -(1 * cos - 1 * sin);
        float rotX = -(1 * cos + 1 * sin);
        float targetX = translate.getX() + rotX, targetY = translate.getY() + rotY;
        translate.interpolate(targetX, targetY, 1);

        scale.interp(backwards ? 0 : 255, 5);

        if (scale.getOpacity() >= 255) {
            backwards = true;
        } else if (scale.getOpacity() == 0) {
            backwards = false;
        }

        if (translate.getX() + rotX < 15 || translate.getY() + rotY < 15 || translate.getX() + rotX > width - 15 || translate.getY() + rotY > height - 15) {
            angle += 120 + MathUtils.randomNumber(15, -15);
        }

//        GlStateManager.pushMatrix();
//        double scaleValue = 0.75 + (0.50 * (scale.getOpacity() / 255));
//        GlStateManager.translate(translate.getX(), translate.getY(), 0);
//        GlStateManager.rotate((float) ((scale.getOpacity() / 255) * (5 - -5)) + -5, 0, 0, 1);
//        GlStateManager.scale(scaleValue, scaleValue, 1);
//        mc.fontRendererObj.drawStringWithShadow(retardedShit, -mc.fontRendererObj.getStringWidth(retardedShit) / 2, 0, Colors.getColor(255, 60));
//        GlStateManager.popMatrix();

        double x = translate.getX();
        double y22 = translate.getY();
        double z = 0;

        double pX = 0;
        double pY = -20;
        double pZ = 0;

        particles.updateAndRender(sr.getScaledWidth_double() / 2F, sr.getScaledHeight(), mouseX, mouseY);

        GlStateManager.pushMatrix();
        float rot = (lastRotX += 1) / 2;
        GlStateManager.translate(x, y22, z);
        GlStateManager.rotate(translate.getX() * 3, 0, 1, 0);
        GlStateManager.rotate(translate.getY() * 5, 1, 0, 0);
        GlStateManager.rotate(rot, 0, 0, 1);


        GlStateManager.scale(5, 5, 5);

        GL11.glLineWidth(1.5F);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);


        RenderingUtil.glColor(Colors.getColor(Color.getHSBColor(lastHue += 0.0015, 0.8F, 1), 150));

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ + 0.25 * 10);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ + 0.25 * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ + 0.25 * 10);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ + 0.25 * 10);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glEnd();


        RenderingUtil.glColor(Colors.getColor(Color.getHSBColor(lastHue, 0.8F, 1), 200));
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glVertex3d(pX, pY + 2.5 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ + 0.25 * 10);
        GL11.glVertex3d(pX, pY + 1.5 * 10, pZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ - 0.25 * 10);
        GL11.glVertex3d(pX + 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glVertex3d(pX, pY + 2 * 10, pZ  + 0.25 * 10);
        GL11.glVertex3d(pX - 0.25 * 10, pY + 2 * 10, pZ);
        GL11.glEnd();


        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);

        GlStateManager.popMatrix();

//        GlStateManager.pushMatrix();
//        GL11.glLineWidth(1.5F);
//
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glShadeModel(GL11.GL_SMOOTH);
//        GL11.glDisable(GL11.GL_TEXTURE_2D);
//        GL11.glEnable(GL11.GL_LINE_SMOOTH);
//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glDisable(GL11.GL_LIGHTING);
//        GL11.glDepthMask(false);
//        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
//
//
//        GlStateManager.translate(150,110,0);
//        GlStateManager.rotate(-45,0,0,1);
//        GlStateManager.scale(20,20,0);
//        RenderingUtil.glColor(Colors.getColor(255));
//        GL11.glBegin(GL11.GL_LINES);
//        double lastBruhX = 0, lastBruhY = 0;
//        for(float i = 1.7F; i < 20; i += 0.01) {
//            double bruhX = i + 1.5F*Math.cos(15*i + (rot/4));
//            double bruhY = i + 1.5F*Math.sin(16*i + (rot/4));
//            if(lastBruhX == 0) {
//                lastBruhX = bruhX;
//                lastBruhY = bruhY;
//            }
//            GL11.glVertex3d(lastBruhX, lastBruhY, 0);
//            GL11.glVertex3d(lastBruhX = bruhX, lastBruhY = bruhY, 0);
//        }
//        GL11.glVertex3d(lastBruhX, lastBruhY, 0);
//        GL11.glEnd();
//
//        GL11.glDepthMask(true);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
//        GL11.glDisable(GL11.GL_LINE_SMOOTH);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glDisable(GL11.GL_BLEND);
//        GlStateManager.popMatrix();


        if (isBirthday) {
            String b = "Happy Birthday, Arithmo!";
            mc.fontRendererObj.drawStringWithShadow(b, width / 2 - mc.fontRendererObj.getStringWidth(b) / 2F, 2, -1);
        }

        RenderingUtil.rectangleBordered(sr.getScaledWidth_double() / 2 - 62 - 0.5, (height / 4D) + 30 - 0.5, sr.getScaledWidth_double() / 2 + 62 + 0.5, (height / 4D) + 175 + 0.5, 0.5, Colors.getColor(60), Colors.getColor(10));
        RenderingUtil.rectangleBordered(sr.getScaledWidth_double() / 2 - 62 + 0.5, (height / 4D) + 30 + 0.5, sr.getScaledWidth_double() / 2 + 62 - 0.5, (height / 4D) + 175 - 0.5, 1.5, Colors.getColor(60), Colors.getColor(40));
        RenderingUtil.rectangleBordered(sr.getScaledWidth_double() / 2 - 62 + 2.5, (height / 4D) + 30 + 2.5, sr.getScaledWidth_double() / 2 + 62 - 2.5, (height / 4D) + 175 - 2.5, 0.5, Colors.getColor(22), Colors.getColor(12));

        GlStateManager.pushMatrix();
        GlStateManager.color(2, 2, 1);
        mc.getTextureManager().bindTexture(uparrow);
        GlStateManager.translate(sr.getScaledWidth_double() / 2 - 60, (height / 4D) + 30, 0);
        drawIcon(1, 4, 0, 0, 117.5, 138, 812 / 2F, 688 / 2F);
        GlStateManager.popMatrix();

        RenderingUtil.drawGradientSideways(sr.getScaledWidth_double() / 2 - 62 + 3, (height / 4D) + 30 + 3, sr.getScaledWidth_double() / 2, (height / 4D) + 30 + 4, Colors.getColor(55, 177, 218), Colors.getColor(204, 77, 198));
        RenderingUtil.drawGradientSideways(sr.getScaledWidth_double() / 2, (height / 4D) + 30 + 3, sr.getScaledWidth_double() / 2 + 62 - 3, (height / 4D) + 30 + 4, Colors.getColor(204, 77, 198), Colors.getColor(204, 227, 53));
        RenderingUtil.rectangle(sr.getScaledWidth_double() / 2 - 62 + 3, (height / 4D) + 30 + 3.5, sr.getScaledWidth_double() / 2 + 62 - 3, (height / 4D) + 30 + 4, Colors.getColor(0, 110));

        RenderingUtil.rectangleBordered(sr.getScaledWidth_double() / 2 - 62 + 6, (height / 4D) + 30 + 8, sr.getScaledWidth_double() / 2 + 62 - 6.5, (height / 4D) + 169, 0.5, Colors.getColor(48), Colors.getColor(10));
        RenderingUtil.rectangle(sr.getScaledWidth_double() / 2 - 62 + 6 + 1, (height / 4D) + 30 + 9, sr.getScaledWidth_double() / 2 + 62 - 7.5, (height / 4D) + 169 - 1, Colors.getColor(17));
        RenderingUtil.rectangle(sr.getScaledWidth_double() / 2 - 62 + 6 + 4.5f, (height / 4D) + 30 + 8, sr.getScaledWidth_double() / 2 - 62 + 35, (height / 4D) + 30 + 9, Colors.getColor(17));

        GlStateManager.pushMatrix();
        GlStateManager.translate(sr.getScaledWidth_double() / 2 - 62 + 6 + 5, (height / 4D) + 30 + 8, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        mc.fontRendererObj.drawStringWithShadow("Main Menu", 0, 0, -1);
        GlStateManager.popMatrix();

        GL11.glColor3f(1F, 1F, 1F);
        for (GuiButton b : this.buttonList) {
            b.drawButton(mc, mouseX, mouseY);
        }
        GL11.glColor3f(1F, 1F, 1F);
        GlStateManager.pushMatrix();
        String exhibition = new ChatComponentText(Client.getModuleManager().get(HUD.class).getSetting("CLIENT-NAME").getValue().toString().replace("&", "\247")).getFormattedText();
        GlStateManager.translate(this.width / 2 - mc.fontRendererObj.getStringWidth(exhibition), 50, 0);
        GlStateManager.scale(2, 2, 2);
        mc.fontRendererObj.drawStringWithShadow(exhibition, 0, 0, ColorManager.hudColor.getColorHex());
        GlStateManager.popMatrix();

        String welcome = "Welcome" + (Client.isNewUser ? "" : " back") + ", \247e" + Client.getAuthUser().getDecryptedUsername();
        mc.fontRendererObj.drawStringWithShadow(welcome, this.width - mc.fontRendererObj.getStringWidth(welcome) - 2, height - 24, Colors.getColor(255, 150));

        String currentBuld = "Your current build is " + (Client.version.equals(Client.parsedVersion) ? "\247aLatest" : Client.isBeta() ? "\247bBeta" : "\247cOutdated") + "\247f!";
        mc.fontRendererObj.drawStringWithShadow(currentBuld, this.width - mc.fontRendererObj.getStringWidth(currentBuld) - 1, height - 10, Colors.getColor(255, 150));

        boolean hovering = mouseX > 2 && mouseX < 2 + mc.fontRendererObj.getStringWidth("MineSense Forums") && mouseY > height - 12 && mouseY < height - 1;

        String minesense = (hovering ? "\247n" : "") + "MineSense Forums";
        mc.fontRendererObj.drawStringWithShadow(minesense, 2, height - 10, -1);

        int y = 24;
        GlStateManager.enableBlend();
        mc.fontRendererObj.drawStringWithShadow("< \247cChangelog \247f>", 2, 2, Colors.getColor(255, 150));

        mc.fontRendererObj.drawStringWithShadow("< \247cAnnouncements \247f>", width - fontRendererObj.getStringWidth("< Announcements >") - 2, 2, Colors.getColor(255, 150));

        GlStateManager.scale(0.5, 0.5, 0.5);
        for (String bruh : changeLogList) {
            String str = bruh.trim();
            Depth.pre();
            Depth.mask();
            mc.fontRendererObj.drawString(str, 4, y, Colors.getColor(255));
            Depth.render(GL_LESS);
            mc.fontRendererObj.drawString(str, 4 - 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 4 + 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 4, y + 1, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, 4, y - 1, Colors.getColor(5, 175));
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
            mc.fontRendererObj.drawString(str, 4, y, Colors.getColor(255, 175));
            y += 12;
        }

        y = 24;
        for (String bruh : announcements) {
            String str = bruh.trim();
            Depth.pre();
            Depth.mask();
            mc.fontRendererObj.drawString(str, (width * 2 - fontRendererObj.getStringWidth(str) - 4), y, Colors.getColor(255));
            Depth.render(GL_GEQUAL);
            RenderingUtil.rectangle((width * 2 - fontRendererObj.getStringWidth(str) - 4), y, (width * 2 - 4), y + 10, Colors.getColor(255, 175));
            Depth.render(GL_LESS);
            mc.fontRendererObj.drawString(str, (width * 2 - fontRendererObj.getStringWidth(str) - 4) - 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, (width * 2 - fontRendererObj.getStringWidth(str) - 4) + 1, y, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, (width * 2 - fontRendererObj.getStringWidth(str) - 4), y + 1, Colors.getColor(5, 175));
            mc.fontRendererObj.drawString(str, (width * 2 - fontRendererObj.getStringWidth(str) - 4), y - 1, Colors.getColor(5, 175));
            Depth.post();
            y += 12;
        }
        GlStateManager.scale(2, 2, 2);
        GlStateManager.disableBlend();
    }

    private void drawIcon(double x, double y, float u, float v, double width, double height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) x, (double) (y + height), 0.0D).tex((double) (u * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), 0.0D).tex((double) ((u + (float) width) * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) y, 0.0D).tex((double) ((u + (float) width) * f), (double) (v * f1)).endVertex();
        worldrenderer.pos((double) x, (double) y, 0.0D).tex((double) (u * f), (double) (v * f1)).endVertex();
        tessellator.draw();
    }

    private void openURI(URI uri) {
        try {
            Class var2 = Class.forName("java.awt.Desktop");
            Object var3 = var2.getMethod("getDesktop").invoke(null);
            var2.getMethod("browse", URI.class).invoke(var3, uri);
        } catch (Throwable var4) {
            LogManager.getLogger().error("Couldn\'t open link", var4);
        }
    }

}
