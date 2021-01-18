package exhibition;

import com.github.creeper123123321.viafabric.ViaFabric;
import exhibition.event.Event;
import exhibition.event.EventListener;
import exhibition.event.EventSystem;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.gui.altmanager.FileManager;
import exhibition.gui.click.ClickGui;
import exhibition.gui.console.SourceConsoleGUI;
import exhibition.gui.generators.handlers.altening.AlteningGenHandler;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.AltService;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.SSLVerification;
import exhibition.gui.screen.GuiChangelog;
import exhibition.gui.screen.impl.mainmenu.ClientMainMenu;
import exhibition.gui.screen.impl.mainmenu.GuiLoginMenu;
import exhibition.management.ColorManager;
import exhibition.management.command.CommandManager;
import exhibition.management.config.ConfigManager;
import exhibition.management.font.DynamicTTFFont;
import exhibition.management.font.TTFFontRenderer;
import exhibition.management.friend.FriendManager;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.management.waypoints.WaypointManager;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.util.HypixelUtil;
import exhibition.util.MathUtils;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.security.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;

import static exhibition.util.security.AuthenticationUtil.getHwid;
import static exhibition.util.security.Snitch.snitch;

// Credits to LPK for initial base
public class Client extends Castable implements EventListener {


    public static Client instance;

    public static boolean isNewUser;

    // Client data
    public static String version = "011821";
    public static String parsedVersion;
    public static String clientName = "ArthimoWare";
    public static ColorManager cm = new ColorManager();
    public static ConfigManager configManager;
    public static WaypointManager waypointManager;

    // Alt Gen Handlers
    public static AlteningGenHandler alteningGenHandler;
    public static AltService altService = new AltService();
    public static SSLVerification sslVerification = new SSLVerification();

    // Managers
    private final ModuleManager<Module> moduleManager;

    private static FileManager fileManager;
    private static ClickGui clickGui;
    private static SourceConsoleGUI sourceConsoleGUI;

    public static CommandManager commandManager;
    private KillProcess killProcess = new KillProcess();

    public static FontRenderer virtueFont;
    public static FontRenderer blockyFont;

    // Other data
    private File dataDirectory;
    private GuiScreen mainMenu = new ClientMainMenu();
    private boolean isHidden;

    public static long loginTime = -1;

    public static ResourceLocation chainmailTexture = new ResourceLocation("textures/skeetchainmail.png");
    public static ResourceLocation capeLocation = new ResourceLocation("textures/cape.png");
    public static ResourceLocation overlayLocation = new ResourceLocation("textures/overlay.png");

    // Auth
    private static AuthenticatedUser authUser;

    // Fonts
    public static TTFFontRenderer f, fs, fsnotbold, fss, fssBold, badCache, verdana16, verdana10, fsmallbold, nametagsFont, test1, test2, test3;

    public static DynamicTTFFont fsmallboldscaled, nametagsFontscaled, header, subHeader, fssDynamic;

    public static DynamicTTFFont.DynamicTTForMC hudFont;

    public static TTFFontRenderer[] fonts;

    public static DynamicTTFFont[] dynamicFonts;

    public static DynamicTTFFont.DynamicTTForMC[] dynamicMcFonts;

    public boolean isHypixel;

    public ProgressScreen progressScreenTask;

    public String hypixelApiKey = null;

    public Client(Object[] args) {
        try {
            Class var2 = Class.forName("java.lang.management.ManagementFactory");
            Object var3 = var2.getDeclaredMethod("getRuntimeMXBean", new Class[0]).invoke(args[7]);
            Method method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            List<String> list = (List) method.invoke(var3, new Object[0]);
            this.progressScreenTask = (ProgressScreen) args[2];
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Object bruh = unsafeClass.getDeclaredField("theUnsafe");

            /* TODO: REMOVE ON UPDATE */
            if (getHwid() != 32161752 && !this.getClass().getName().startsWith("TEMPPROTECT")) {
                Snitch.snitch(1000, this.getClass().getName());
            }
            boolean doArgumentsMatch = false; // should turn into true
            boolean foundDisableAttach = false;
            Class fieldClass = Class.forName("java.lang.reflect.Field");
            List<String> mismatching = null;
            for (String a : list) {
                if (a.contains(Crypto.decryptPrivate("W9Io33+u6h/y824F8vB4YA==")) || (a.contains(Crypto.decryptPrivate("hRawfwHiKgsEGWqMl+wcaQ==")) && getHwid() != 32161752 /* TODO: REMOVE ON UPDATE */)) {
                    snitch(1, a);
                }
                if (a.equals(Crypto.decryptPrivate("jcsPRcUqWhxo0fClF+5v02BFfJ8h2uE1iJ2JkdnvftE="))) {
                    foundDisableAttach = true;
                    if (!doArgumentsMatch)
                        doArgumentsMatch = (mismatching = RuntimeVerification.argumentsMatch(list)).isEmpty();
                }
            }
            Object field = Class.forName("java.lang.System").getDeclaredField("err");
            fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
            if (!doArgumentsMatch) {
                snitch(-555, mismatching.toArray(new String[]{}));
            }
            if (!foundDisableAttach) {
                snitch(-555, "Missing Disable");
            }
            this.progressScreenTask.render();
            Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);

            this.progressScreenTask.incrementStage(); // Stage 1 pass arguments check

            // TODO: ADD BEFORE UPDATE
//            if (getHwid() != 32161752) {
//                Object custom = Class.forName("net.minecraft.util.LoggingPrintStream").
//                        getConstructor(String.class, Class.forName("java.io.OutputStream")).
//                        newInstance("", unsafeClass.getMethod("getObject", Object.class, long.class).
//                                invoke(unsafeInstance, unsafeClass.getMethod("staticFieldBase", fieldClass).invoke(unsafeInstance, field), unsafeClass.getMethod("staticFieldOffset", fieldClass).invoke(unsafeInstance, field)));
//
//                unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class).invoke(unsafeInstance,
//                        unsafeClass.getMethod("staticFieldBase", fieldClass).invoke(unsafeInstance, field),
//                        unsafeClass.getMethod("staticFieldOffset", fieldClass).invoke(unsafeInstance, field),
//                        custom);
//            }

            ((ProgressScreen) args[35]).incrementStage();
        } catch (Exception e) {
        }


        String version = "";
        try {
            Connection connection = new Connection("https://minesense.pub/nig/version.php")
                    .setParameters("i", Crypto.decryptPrivate("YuMCLvpP/fQUkLzHtx6DLg=="));

            version = SSLConnector.get(connection).trim();
            parsedVersion = Crypto.decrypt(CryptManager.getDecrypt(), version);
        } catch (Exception e) {
            e.printStackTrace();
            snitch(12, e.getMessage(), version);
        }
        this.progressScreenTask.incrementStage(); // Stage 2 version was fetched correctly
        List<String> okHand = LoginUtil.getLoginInformation();
        try {
            if (!okHand.isEmpty() && okHand.size() > 1) {
                if (okHand.size() > 2)
                    LoginUtil.cachedLogin = (int) Math.sqrt(Integer.parseInt(okHand.get(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.progressScreenTask.incrementStage(); // Stage 3 login cache was checked
        AuthenticationUtil.isHWIDValid(parsedVersion + commandManager, true);
//        if (!b) {
//            exhibition.module.impl.combat.AutoPot.snitch(11);
//            killSwitch();
//        }

        try {
            this.progressScreenTask = (ProgressScreen) args[8334];
        } catch (Exception e) {

        }

        this.progressScreenTask.incrementStage(); // Stage 4 passed basic hwid check
        Client.instance = this;

        try {
            Client.instance = ((Castable) args[8334]).cast();
        } catch (Exception e) {

        }

        instance.setupFonts();
        dataDirectory = new File(Client.clientName);
        this.progressScreenTask.incrementStage(); // Stage 5
        commandManager = new CommandManager();
        this.progressScreenTask.incrementStage(); // Stage 6
        moduleManager = new ModuleManager(Module.class);
        this.progressScreenTask.incrementStage(); // Stage 7
        try {
            new ViaFabric().onInitialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        this.progressScreenTask.incrementStage(); // Stage 8

        try {
            Client.authUser = ((Castable) args[323]).cast();
        } catch (Exception e) {

        }
        // Client.mojang.connect();
    }

    public boolean is1_16_4() {
        return ViaFabric.config.getClientSideVersion() == 754;
    }

    public boolean is1_9orGreater() {
        return ViaFabric.config.getClientSideVersion() >= 107;
    }

    public void killSwitch() {
        killInstance();
        //sout(code);
    }

    public Client killPC() {
        killPC();
        return null;
    }

    public static AuthenticatedUser getAuthUser() {
        return authUser;
    }

    public static void setAuthUser(Object authUser) {
        Client.authUser = ((Castable) authUser).cast();
    }

    public static ClickGui getClickGui() {
        return clickGui;
    }

    public static SourceConsoleGUI getSourceConsoleGUI() {
        return sourceConsoleGUI;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public void killInstance() {
        killProcess.killMC();
    }

    public void setup() {
        commandManager.setup();
        isNewUser = !getDataDir().exists();
        moduleManager.setup();
        /*accountManager.setup();*/
        ModuleManager.loadSettings();
        (Client.fileManager = new FileManager()).loadFiles();
        sourceConsoleGUI = new SourceConsoleGUI();
        waypointManager = new WaypointManager();
        configManager = new ConfigManager();
        alteningGenHandler = new AlteningGenHandler();
        clickGui = new ClickGui();
        FriendManager.start();

        EventSystem.register(this);
    }

    /*
        f = new TTFFontRenderer(new Font("Impact", Font.PLAIN, 24), true);
        fs = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 11), true);
        fsnotbold = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 9), false);
        test2 = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 10), true);
        fss = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 10), false);
        test3 = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 10), true);
        fsmallbold = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 10), true);
        header = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 20), true);
        subHeader = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 16), true);
        verdana16 = new TTFFontRenderer(new Font("Lucida Console", Font.PLAIN, 9), false);
        test1 = new TTFFontRenderer(new Font("Verdana", Font.PLAIN, 9), true);
        verdana10 = new TTFFontRenderer(new Font("Lucida Console", Font.PLAIN, 10), false);
        nametagsFont = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 18), true);
     */

    public void setupFonts() {

        f = new TTFFontRenderer(new Font("Impact", Font.PLAIN, 24), true);
        fs = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 11), true);
        fsnotbold = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 9), false);
        test2 = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 10), true);
        fss = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 10), false);
        fssBold = new TTFFontRenderer(new Font("Arial Bold", Font.BOLD, 11), false);

        fssDynamic = new DynamicTTFFont(new Font("Tahoma", Font.PLAIN, 10), false);
        test3 = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 10), true);
        fsmallbold = new TTFFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 10), true);
        header = new DynamicTTFFont(new Font("Tahoma", Font.PLAIN, 20), true);
        subHeader = new DynamicTTFFont(new Font("Tahoma", Font.PLAIN, 16), true);
        verdana16 = new TTFFontRenderer(new Font("Lucida Console", Font.PLAIN, 9), false);
        test1 = new TTFFontRenderer(new Font("Verdana", Font.PLAIN, 9), true);
        verdana10 = new TTFFontRenderer(new Font("Lucida Console", Font.PLAIN, 10), false);
        nametagsFont = new TTFFontRenderer(new Font("Tahoma", Font.PLAIN, 18), true);

        fsmallboldscaled = new DynamicTTFFont(new Font("Tahoma Bold", Font.PLAIN, 10), true);
        nametagsFontscaled = new DynamicTTFFont(new Font("Tahoma", Font.PLAIN, 18), true);
        hudFont = new DynamicTTFFont.DynamicTTForMC(new DynamicTTFFont(new Font("Calibri", Font.PLAIN, 20), true));

        try {
            InputStream istream = getClass().getResourceAsStream("/assets/minecraft/font.ttf");
            Font myFont = Font.createFont(Font.PLAIN, istream);
            myFont = myFont.deriveFont(Font.PLAIN, 36);
            badCache = new TTFFontRenderer(myFont, true);
        } catch (Exception e) {
            System.out.println("Error loadig font?");
            badCache = new TTFFontRenderer(new Font("Impact", Font.PLAIN, 36), true);
        }

        fonts = new TTFFontRenderer[10];

        fonts[0] = new TTFFontRenderer(new Font("Calibri Bold", Font.PLAIN, 12), true);
        fonts[1] = new TTFFontRenderer(new Font("Calibri Bold", Font.PLAIN, 18), true);
        fonts[2] = new TTFFontRenderer(new Font("Calibri Bold", Font.PLAIN, 11).deriveFont(10.5F), false);

        fonts[3] = new TTFFontRenderer(new Font("Helvetica", Font.PLAIN, 13).deriveFont(13.5F), true);

    }

    public static GuiScreen getScreen() {
        try {
            String currentVersion = version.substring(0, 2) + "/" + version.substring(2, 4) + "/20" + version.substring(4, 6);

            String version = LoginUtil.getLastVersion();
            if (version.equals(""))
                return new GuiChangelog();

            String lastVersion = version.substring(0, 2) + "/" + version.substring(2, 4) + "/20" + version.substring(4, 6);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

            if (format.parse(currentVersion).after(format.parse(lastVersion)) && Client.version.equals(parsedVersion)) {
                return new GuiChangelog();
            }
        } catch (Exception ignored) {
        }
        return new GuiLoginMenu(true);
    }

    private static boolean isBeta = false;

    private static boolean first = false;

    public static boolean isBeta() {
        if (!first) {
            try {
                String currentVersion = version.substring(0, 2) + "/" + version.substring(2, 4) + "/20" + version.substring(4, 6);
                String e = parsedVersion.substring(0, 2) + "/" + parsedVersion.substring(2, 4) + "/20" + parsedVersion.substring(4, 6);
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                isBeta = format.parse(currentVersion).after(format.parse(e));
            } catch (Exception ignored) {
            }
            first = true;
        }
        return isBeta;
    }

    public static ModuleManager<Module> getModuleManager() {
        return instance.moduleManager;
    }

    public static File getDataDir() {
        return instance.dataDirectory;
    }

    public static boolean isHidden() {
        return instance.isHidden;
    }

    public static void setHidden(boolean hidden) {
        instance.isHidden = hidden;
        if (hidden) {
            instance.mainMenu = new GuiMainMenu();
        } else {
            instance.mainMenu = new ClientMainMenu();
        }
    }

    public static void resetClickGui() {
        clickGui = new ClickGui();
    }

    private final Timer packetTimer = new Timer();

    public boolean isLagging() {
        return packetTimer.roundDelay(250);
    }

    public double spawnY = 86;

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        if (event instanceof EventPacket) {
            EventPacket eventPacket = event.cast();
            Packet packet = eventPacket.getPacket();
            if (eventPacket.isIncoming() && !(packet instanceof C00PacketKeepAlive)) {
                packetTimer.reset();
            }

            if (packet instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook spawnPosition = (S08PacketPlayerPosLook) packet;

                double x = spawnPosition.getX();
                double y = spawnPosition.getY();
                double z = spawnPosition.getZ();

                double distance = Math.sqrt(x * x + z * z);

                double yOffset = MathUtils.roundToPlace((y - (int) y), 10);

                if (distance < 20 && yOffset == 0.6) {
                    spawnY = y - 15;
                }
            }

            if (Minecraft.getMinecraft().thePlayer != null) {
                if (packet instanceof C03PacketPlayer && HypixelUtil.isVerifiedHypixel() && hypixelApiKey == null && Minecraft.getMinecraft().thePlayer.ticksExisted > 2) {
                    ChatUtil.sendChat("/api new");
                    hypixelApiKey = "";
                    return;
                }
                if (packet instanceof S02PacketChat) {
                    S02PacketChat packetChat = (S02PacketChat) packet;
                    String unformatted = StringUtils.stripControlCodes(packetChat.getChatComponent().getUnformattedText());
                    if (unformatted.contains("Your new API key is ")) {
                        hypixelApiKey = unformatted.split("Your new API key is ")[1].trim();
                        event.setCancelled(true);
                        Notifications.getManager().post("New API Key", "Grabbed your Hypixel API key.", Notifications.Type.OKAY);
                    }
                }
            }
        }
    }

}
