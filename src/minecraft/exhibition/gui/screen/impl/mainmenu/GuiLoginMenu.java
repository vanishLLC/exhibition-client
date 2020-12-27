package exhibition.gui.screen.impl.mainmenu;


import exhibition.Client;
import exhibition.gui.altmanager.PasswordField;
import exhibition.gui.click.virtue.VirtueClickGui;
import exhibition.gui.screen.PanoramaScreen;
import exhibition.gui.screen.component.GuiMenuButton;
import exhibition.gui.screen.component.GuiSkeetPWField;
import exhibition.gui.screen.component.GuiSkeetTextField;
import exhibition.management.animate.Opacity;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import exhibition.util.security.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.CryptManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static exhibition.util.security.AuthenticationUtil.getHwid;

/**
 * Created by Arithmo on 10/1/2017 at 7:52 PM.
 */

@SuppressWarnings("Duplicates")
public class GuiLoginMenu extends PanoramaScreen {

    private Client oldInstance;

    static class Status {

        static Status IDLE = new Status("Idle"),
                AUTHENTICATING = new Status("Authenticating"),
                SUCCESS = new Status("Success"),
                ERROR = new Status("Error"),
                LOGIN_FAILED = new Status("Login Failed"), // User not authorized
                INVALID_PASSWORD = new Status("Invalid Password"),
                INVALID_HWID = new Status("Invalid HWID");

        String name;

        Status(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    private PasswordField password;
    private GuiTextField username;
    private ProgressBar progressBar;
    private Status status = Status.IDLE;

    private final boolean fade;

    public GuiLoginMenu(boolean fade) {
        this.fade = fade;
        oldInstance = Client.instance;
        try {
            Class var2 = Class.forName("java.lang.management.ManagementFactory");
            Object var3 = var2.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke((Object) null, new Object[0]);
            Method method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            List<String> list = (List) method.invoke(var3, new Object[0]);
            list.forEach(a -> {
                if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752)) {
                    try {
                        exhibition.util.security.Snitch.snitch(0, list.toArray(new String[]{}));
                        oldInstance = null;
                    } catch (Exception e) {

                    }
                }
            });

            Client.instance = null;
        } catch (Exception e) {
            e.printStackTrace();
            AuthenticationUtil.snitch(13);
            Client.instance.killSwitch();
        }
        menuSong = new PositionedSoundRecord(new ResourceLocation("sounds/music/fortnut.ogg"), 1, 1, true, 0, ISound.AttenuationType.LINEAR, 0, 0, 0);
        GuiModdedMainMenu.menuSong = new PositionedSoundRecord(new ResourceLocation("sounds/music/fortnat.ogg"), 1, 1, true, 0, ISound.AttenuationType.LINEAR, 0, 0, 0);

    }

    private PositionedSoundRecord menuSong;

    private ResourceLocation songLocation = new ResourceLocation("sounds/music/fortnut.ogg");

    private boolean first;

    private Opacity opacity;

    @Override
    public void initGui() {
        if (!first) {
            progressBar = new ProgressBar(width / 2 - 50, 47, 100, 8);
            mc.getSoundHandler().loadCustomSound(songLocation);
            mc.getSoundHandler().loadCustomSound(new ResourceLocation("sounds/music/fortnat.ogg"));

            mc.getSoundHandler().playSound(GuiModdedMainMenu.menuSong);
            mc.getSoundHandler().stopSound(GuiModdedMainMenu.menuSong);

            if (!mc.getSoundHandler().isSoundPlaying(menuSong))
                mc.getSoundHandler().playSound(menuSong);

            opacity = new Opacity(fade ? 300 : 0);
            first = true;
        }

        progressBar.updatePosition(width / 2F - 50, 47);

        final int var3 = height / 4 + 24;
        buttonList.add(new GuiMenuButton(0, width / 2 - 100, var3 + 72 + 12, 200, 12, "Login"));

        username = new GuiSkeetTextField(var3, mc.fontRendererObj, width / 2 - 100, 60, 200, 20);
        password = new GuiSkeetPWField(mc.fontRendererObj, width / 2 - 100, 100, 200, 20);

        //username.setEnableBackgroundDrawing(false);
        password.setEnableBackgroundDrawing(false);

        if (AuthenticationUtil.fuck) {
            AuthenticationUtil.fuck();
        }

        password.setMaxStringLength(256);
        username.setFocused(true);
        List<String> okHand = LoginUtil.getLoginInformation();
        try {
            if (!okHand.isEmpty() && okHand.size() > 1) {
                username.setText(getDecrypted(okHand.get(0)));
                password.setText(getDecrypted(okHand.get(1)));
                if (okHand.size() > 2)
                    LoginUtil.cachedLogin = Integer.valueOf(okHand.get(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Keyboard.enableRepeatEvents(true);
        super.initGui();
    }

    private String getCrypted(String str) throws Exception {
        return Crypto.encrypt(CryptManager.getSecretNew(), str);
    }

    private String getDecrypted(String str) throws Exception {
        return Crypto.decrypt(CryptManager.getSecretNew(), str);
    }

    public class AuthenticationThread extends Thread implements Runnable {

        AuthenticationThread(GuiLoginMenu loginInstance) {
            this.loginInstance = loginInstance;
        }

        final GuiLoginMenu loginInstance;

        boolean hasFailed;
        boolean isRunning;

        public boolean isRunning() {
            return isRunning;
        }

        public void stopThread() {
            isRunning = false;
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                Class var2 = Class.forName("java.lang.management.ManagementFactory");
                Object var3 = var2.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke((Object) null, new Object[0]);
                Method method = var3.getClass().getMethod("getInputArguments");
                method.setAccessible(true);
                List<String> list = (List) method.invoke(var3, new Object[0]);
                list.forEach(a -> {
                    if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */)) {
                        try {
                            exhibition.util.security.Snitch.snitch(0, list.toArray(new String[]{}));
                            oldInstance = null;
                        } catch (Exception e) {

                        }
                    }
                });
                if (oldInstance != null && !Objects.equals(username.getText(), "") && !Objects.equals(password.getText(), ""))
                    try {
                        status = Status.AUTHENTICATING;
                        String one = getCrypted(username.getText());
                        String two = getCrypted(password.getText());
                        String three = getCrypted((username.getText().hashCode()) + "");
                        String four = getCrypted((password.getText().hashCode()) + "");
                        try {
                            Object nigga = AuthenticationUtil.isAuth(this, loginInstance, one, two, three, four);
                            if (nigga != null && RuntimeVerification.argumentsMatch(list).isEmpty()) {
                                if (Client.instance == null) {
                                    Client.instance = oldInstance;
                                    Field field = Class.forName("exhibition.Client").getDeclaredField("authUser");
                                    field.setAccessible(true);
                                    field.set(Client.instance, nigga);
                                    Object authUserInstance = field.get(Client.instance);
                                    Class.forName("exhibition.util.security.AuthenticatedUser").getDeclaredMethod("setupClient", Object.class).invoke(authUserInstance, Client.instance);
                                }
                                Client.getAuthUser().justMakingSure();
                                loginInstance.status = Status.SUCCESS;
                                LoginUtil.saveLogin((String) one, (String) two);
                            }
                        } catch (Exception ignored) {
                            setError();
                            setProgress(0.455);
                            ignored.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginInstance.status = Status.ERROR;
                        hasFailed = true;
                    }
            } catch (Exception e) {
                e.printStackTrace();
                loginInstance.status = Status.ERROR;
                hasFailed = true;
            }
            isRunning = false;
        }


    }

    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/logo.png");


    public AuthenticationThread thread;

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id == 0) {
            if (!username.getText().equals("") && !password.getText().equals("") && thread == null || thread.hasFailed || !thread.isRunning()) {
                username.setEnabled(false);
                password.isEnabled = false;
                thread = new AuthenticationThread(this);
                thread.start();
            }
        }
    }

    private boolean firstDraw = true;

    @Override
    public void drawScreen(final int x, final int y, final float z) {
        if (firstDraw) {
            opacity.setOpacity(fade ? 300 : 0);
            opacity.interp(300, 0);
            firstDraw = false;
        }
        renderSkybox(x, y, z);

        if (!status.equals(Status.SUCCESS) && thread == null || thread.hasFailed || !thread.isRunning) {
            username.setEnabled(true);
            password.isEnabled = true;
        }

        drawCenteredString(mc.fontRendererObj, status.getName(), width / 2, 32, -1);
        GuiButton button = buttonList.get(0);
        button.enabled = !status.equals(Status.AUTHENTICATING);
        if (status.equals(Status.AUTHENTICATING)) {
            username.setEnabled(false);
            password.isEnabled = false;
        }

        RenderingUtil.rectangleBordered(width / 2F - 100, 61.5, width / 2F + 100, 76.5, 0.5, Colors.getColor(41), Colors.getColor(0));
        RenderingUtil.rectangleBordered(width / 2F - 99.5, 62, width / 2F + 99.5, 76, 0.5, Colors.getColor(0, 0), Colors.getColor(username.isFocused() ? 130 : 80));

        RenderingUtil.rectangleBordered(width / 2F - 100, 101.5, width / 2F + 100, 116.5, 0.5, Colors.getColor(41), Colors.getColor(0));
        RenderingUtil.rectangleBordered(width / 2F - 99.5, 102, width / 2F + 99.5, 116, 0.5, Colors.getColor(0, 0), Colors.getColor(password.isFocused() ? 130 : 80));

        String currentBuld = "Your current build is " + (Client.version.equals(Client.parsedVersion) ? "\247aLatest" : Client.isBeta() ? "\247bBeta" : "\247cOutdated") + "\247f!";
        mc.fontRendererObj.drawStringWithShadow(currentBuld, this.width - mc.fontRendererObj.getStringWidth(currentBuld) - 1, height - 10, -1);

        boolean renderUser = username.getText().isEmpty() && !username.isFocused();
        if (renderUser) {
            Client.fss.drawBorderedString("Username", width / 2F - 96, 67, Colors.getColor(90), Colors.getColor(0));
        }

        boolean renderPass = password.getText().isEmpty() && !password.isFocused();
        if (renderPass) {
            Client.fss.drawBorderedString("Password", width / 2F - 96, 107, Colors.getColor(90), Colors.getColor(0));
        }
        username.drawTextBox();
        password.drawTextBox();
        if (!renderPass && status == Status.SUCCESS && !renderUser && Client.getAuthUser() != null && Client.getAuthUser().isEverythingOk()) {
            mc.getSoundHandler().stopSound(menuSong);
            if (Boolean.parseBoolean(System.getProperty("virtueTheme2"))) {
                Client.virtueFont = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/ascii.png"), mc.getTextureManager(), false);
                if (mc.gameSettings.language != null) {
                    mc.fontRendererObj.setUnicodeFlag(mc.isUnicode());
                    mc.fontRendererObj.setBidiFlag(mc.getLanguageManager().isCurrentLanguageBidirectional());
                }
                mc.mcResourceManager.registerReloadListener(Client.virtueFont);

                Client.blockyFont = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/blocky.png"), mc.getTextureManager(), false);
                if (mc.gameSettings.language != null) {
                    mc.fontRendererObj.setUnicodeFlag(mc.isUnicode());
                    mc.fontRendererObj.setBidiFlag(mc.getLanguageManager().isCurrentLanguageBidirectional());
                }
                mc.mcResourceManager.registerReloadListener(Client.blockyFont);

                VirtueClickGui.start();
            }
            mc.displayGuiScreen(status == Status.SUCCESS ? new ClientMainMenu() : new GuiGameOver());
        }
        boolean hovering = x > 2 && x < 2 + mc.fontRendererObj.getStringWidth("MineSense Forums") && y > height - 12 && y < height - 1;

        String minesense = (hovering ? "\247n" : "") + "MineSense Forums";
        mc.fontRendererObj.drawStringWithShadow(minesense, 2, height - 10, -1);


        progressBar.draw();

        super.drawScreen(x, y, z);


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
    protected void keyTyped(final char character, final int key) {
        if (opacity.getOpacity() > 50)
            return;

        if (character == '\t') {
            if (!username.isFocused() && !password.isFocused()) {
                username.setFocused(true);
            } else {
                username.setFocused(password.isFocused());
                password.setFocused(!username.isFocused());
            }
        }
        if (buttonList.size() > 0 && buttonList.get(0).enabled)
            if (character == '\r') {
                actionPerformed((GuiButton) buttonList.get(0));
            }
        username.textboxKeyTyped(character, key);
        password.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(final int x, final int y, final int button) {
        try {
            super.mouseClicked(x, y, button);
        } catch (IOException e) {
            e.printStackTrace();
        }
        username.mouseClicked(x, y, button);
        password.mouseClicked(x, y, button);

        boolean mineSense = x > 2 && x < 2 + mc.fontRendererObj.getStringWidth("MineSense Forums") && y > height - 12 && y < height - 1;
        try {
            if (mineSense && button == 0) {
                openURI(new URI("https://minesense.pub/forum/"));

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setProgress(double percent) {
        this.progressBar.setPercent(percent);
    }

    public void setInvalid(boolean password) {
        this.status = password ? Status.INVALID_PASSWORD : Status.INVALID_HWID;
    }

    public void setLoginFailed() {
        if (status == Status.AUTHENTICATING)
            this.status = Status.LOGIN_FAILED;
    }

    public void setError() {
        if (status == Status.AUTHENTICATING)
            this.status = Status.ERROR;
    }

    public void setInvalidHWID() {
        this.status = Status.INVALID_HWID;
    }

    private void openURI(URI uri) {
        try {
            Class var2 = Class.forName("java.awt.Desktop");
            Object var3 = var2.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            var2.getMethod("browse", new Class[]{URI.class}).invoke(var3, new Object[]{uri});
        } catch (Throwable var4) {
            //mc.getLogger().error("Couldn\'t open link", var4);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        username.updateCursorCounter();
        password.updateCursorCounter();
    }

}
