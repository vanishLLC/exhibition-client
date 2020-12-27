package exhibition.module;

import exhibition.Client;
import exhibition.event.EventSystem;
import exhibition.management.AbstractManager;
import exhibition.management.GlobalValues;
import exhibition.management.SubFolder;
import exhibition.management.keybinding.Keybind;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.data.BlockList;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.combat.*;
import exhibition.module.impl.gta.Aimbot;
import exhibition.module.impl.gta.AntiAim;
import exhibition.module.impl.gta.ZombieAim;
import exhibition.module.impl.hud.*;
import exhibition.module.impl.movement.*;
import exhibition.module.impl.other.*;
import exhibition.module.impl.player.*;
import exhibition.module.impl.render.*;
import exhibition.util.FileUtils;
import exhibition.util.StringConversions;
import exhibition.util.security.KillProcess;
import exhibition.util.security.Snitch;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

import static exhibition.module.data.ModuleData.Type.*;

public class ModuleManager<E extends Module> extends AbstractManager<Module> {
    private static final File MODULE_DIR = FileUtils.getConfigFile("Mods");
    private static final File BINDS_DIR = FileUtils.getConfigFile("Binds");
    private static final File SETTINGS_DIR = FileUtils.getConfigFile("Sets");

    private boolean setup;

    public ModuleManager(Class<Module> clazz) {
        super(clazz, 0);
    }

    /**
     * Sets up the ModuleManager.
     * <hr>
     * Two ways to initiate modules internally:<br>
     * Modify constructor: so that it looks like: <i>super(clazz, numOfMods)</i>
     * ; Initiate modules by array index like so:
     *
     * <pre>
     * array[0] = new ModuleExample(...);
     * array[1] = new ModuleExample(...);
     * array[2] = new ModuleExample(...);
     * </pre>
     * <p>
     * or use the add method (Slight addition to startup time)
     *
     * <pre>
     *
     * add(new ModuleExample(...);
     * add(new ModuleExample(...);
     * add(new ModuleExample(...);
     * </pre>
     */

    @Override
    public void setup() {
        // Load modules from jars
        if (setup)
            return;
        //loadLocalPlugins();

        try {
            File runTimeFile = new File((Minecraft.isIsRunningOnWindows ? "" : "/") + System.class.getResource("System.class").getPath().split("!")[0].replace("file:/", "").replace("%20", " "));
            Class md = Class.forName("java.security.MessageDigest");
            Object mdInstance = md.getMethod("getInstance", String.class).invoke(null, "SHA-256");
            try (InputStream in = new FileInputStream(runTimeFile)) {
                byte[] block = new byte[4096];
                int length;
                while ((length = in.read(block)) > 0) {
                    md.getMethod("update", byte[].class, int.class, int.class).invoke(mdInstance, block, 0, length);
                }
                boolean bruh = (boolean) (Class.forName("net.minecraft.client.Minecraft").getDeclaredField("isIsRunningOnWindows").get(null));

                String checkSum = (String) Class.forName("javax.xml.bind.DatatypeConverter").getMethod("printHexBinary", byte[].class).invoke(null, (byte[]) md.getMethod("digest").invoke(mdInstance));
                if (183572818 != checkSum.hashCode() && checkSum.hashCode() != 589290158 && -927836280 != checkSum.hashCode() && 1791589503 != checkSum.hashCode() && bruh) {
                    Snitch.snitch(23, runTimeFile.getAbsolutePath(), checkSum, checkSum.hashCode() + ""); // checksum mismatch
                } else {
                    add(new AutoPlay(new ModuleData(Other, "AutoPlay", "Automatically clicks on \"Click Here to play again\" messages for you.")));
                    add(new NoItemRender(new ModuleData(Visuals, "NoItems", "Removes dropped items from the game, reducing lag and memory usage.")));
                    add(new KillSults(new ModuleData(Other, "KillSult", "Insult people when you kill them!")));
                    add(new TargetStrafe(new ModuleData(Movement, "TargetStrafe", "Strafe around a target, can break enemy killaura.")));
                    add(new Animations(new ModuleData(Other, "Animations", "Swank your swong with swing")));
                    add(new AutoGG(new ModuleData(Other, "AutoGG", "Automatically sends a message when winning a game.")));
                    add(new Waypoints(new ModuleData(Visuals, "Waypoints", "Renders waypoints on screen.")));
                    add(new Timer(new ModuleData(Other, "GameSpeed", "Modifies game speed. Can be used to 'bow fly'.")));
                    add(new Step(new ModuleData(Movement, "Step", "Increases your step height.")));
                    add(new Tags(new ModuleData(Visuals, "3DTags", "Nametags but 3D.")));
                    add(new Indicators(new ModuleData(Visuals, "Indicators", "Shows players outside of your view.")));
                    add(new PingSpoof(new ModuleData(Other, "PingSpoof", "Chokes KeepAlive packets to fake high ping.")));
                    add(new Speed(new ModuleData(Movement, "Speed", "Move faster.")));
                    add(new Criticals(new ModuleData(Combat, "Criticals", "Force critical attack each hit.")));
                    add(new SpeedMine(new ModuleData(Player, "SpeedMine", "Mine blocks faster.")));
                    add(new Brightness(new ModuleData(Visuals, "Brightness", "Applies night vision.")));
                    add(new Lines(new ModuleData(Visuals, "Lines", "Draws lines at entities.")));
                    add(new DONOTFUCKINGDIEYOURETARD(new ModuleData(Visuals, "Health", "Shows your health in the middle of the screen.")));
                    add(new AutoTool(new ModuleData(Player, "AutoTool", "Switches to best tool.")));
                    add(new Scaffold(new ModuleData(Movement, "Scaffold", "Automatically places blocks under for you.")));
                    add(new NoFall(new ModuleData(Player, "NoFall", "Take no fall damage.")));
                    add(new Freecam(new ModuleData(Visuals, "FreeCam", "Allows you to view around in noclip.")));
                    add(new LongJump(new ModuleData(Movement, "LongJump", "Move around with a long jump. Enable 'Autism' for fast fly.")));
                    add(new AntiBot(new ModuleData(Combat, "AntiBot", "Ignores/Removes bots.")));
                    add(new Jesus(new ModuleData(Movement, "Jesus", "Walk on water.")));
                    add(new FastUse(new ModuleData(Player, "FastUse", "Consume items faster.")));
                    add(new ChestStealer(new ModuleData(Player, "ChestStealer", "Steal items from chests.")));
                    add(new Fly(new ModuleData(Movement, "Fly", "Allows you to fly.")));
                    add(new AntiAim(new ModuleData(Other, "AntiAim", "Derp but with CS:GO influences.")));
                    add(new AutoSoup(new ModuleData(Combat, "AutoSoup", "Consumes soups to heal for you.")));
                    add(new ChestESP(new ModuleData(Visuals, "ChestESP", "Draws a box around chests.")));
                    add(new Sprint(new ModuleData(Movement, "Sprint", "Automatically sprints for you.")));
                    add(new Xray(new ModuleData(Visuals, "Xray", "Allows you to see blocks through walls.")));
                    add(new AntiVelocity(new ModuleData(Combat, "AntiVelocity", "Reduce/Remove velocity.")));
                    add(new FastPlace(new ModuleData(Player, "FastPlace", "Reduces the time between right clicks.")));
                    add(new ChatCommands(new ModuleData(Other, "Commands", "Commands, but for chat.")));
                    add(new HUD(new ModuleData(Visuals, "HUD", "Heads up display.")));
                    add(new Nametags(new ModuleData(Visuals, "2DTags", "Nametags that are rendered in the 2D space.")));
                    add(new ArmorStatus(new ModuleData(Visuals, "ArmorHUD", "Shows you your armor stats.")));
                    add(new AutoArmor(new ModuleData(Player, "AutoArmor", "Switches out current armor for best armor.")));
                    add(new InventoryWalk(new ModuleData(Player, "Inventory", "Walk in inventory + carry extra items.")));
                    add(new Crosshair(new ModuleData(Visuals, "Crosshair", "Draws a custom crosshair.")));
                    add(new Killaura(new ModuleData(Combat, "KillAura", "Attacks entities for you.")));
                    add(new NoRotate(new ModuleData(Player, "NoRotate", "Prevents the server from forcing head rotations.")));
                    add(new NoSlowdown(new ModuleData(Movement, "NoSlowdown", "Movement isn't reduced when using an item.")));
                    add(new AutoPot(new ModuleData(Combat, "AutoPot", "Throws potions to heal for you.")));
                    add(new ESP2D(new ModuleData(Visuals, "2DESP", "Outlined box ESP that is rendered in the 2D space.")));
                    add(new Radar(new ModuleData(Visuals, "Radar", "Shows you all the players around you.")));
                    add(new AutoSay(new ModuleData(Other, "AutoSay", "Automatically sends a custom message.")));
                    add(new AutoRespawn(new ModuleData(Player, "Respawn", "Respawns you after you've died.")));
                    add(new Chams(new ModuleData(Visuals, "Chams", "Renders player models through walls.")));
                    add(new TabGUI(new ModuleData(Visuals, "TabGUI", "TabGUI.")));
                    add(new Phase(new ModuleData(Movement, "Phase", "Clip through blocks.")));
                    add(new BedFucker(new ModuleData(Other, "BedNuker", "Breaks beds around you.")));
                    add(new AutoSword(new ModuleData(Combat, "AutoSword", "Moves certain items to your hotbar.")));
                    add(new AntiFall(new ModuleData(Movement, "AntiFall", "For bots who fall off maps 24/7.")));
                    add(new Weather(new ModuleData(Visuals, "Weather", "Set your time to a constant time.")));
                    add(new InventoryCleaner(new ModuleData(Player, "InventoryCleaner", "Cleans your inventory for you.")));
                    add(new TargetHUD(new ModuleData(Visuals, "TargetHUD", "Shows information about the target on the HUD.")));
                    add(new SpookySkeltal(new ModuleData(Visuals, "Skeletal", "Skeleton ESP.")));
                    add(new MCF(new ModuleData(Other, "MCF", "Adds/Removes friends when you Middle Click hovering over them.")));
                    add(new ViewClip(new ModuleData(Visuals, "ViewClip", "Removes camera view collision.")));
                    add(new AntiDesync(new ModuleData(Other, "AntiDesync", "Prevents block desynchronization when breaking/placing.")));
                    add(new BorderHop(new ModuleData(Movement, "WaterHop", "Jumps high when in the water.")));
                    add(new TargetESP(new ModuleData(Visuals, "TargetESP", "Highlights important/priority targets. Automatically marks PIT bounties.")));
                    add(new HackerDetect(new ModuleData(Other, "HackerDetect", "Automatically notifies you about possible hackers.")));
                    add(new NoHurtCam(new ModuleData(Visuals, "NoHurt", "Removes hurtcam shake effect.")));
                    add(new NoTitles(new ModuleData(Visuals, "NoTitles", "Disables titles from showing. (The text on the center of your screen)")));
                    add(new SurvivalNuker(new ModuleData(Player, "SurvivalNuker", "Mines for you automatically in a specified radius.")));
                    add(new Blink(new ModuleData(Movement, "Blink", "Chokes packets while you move.")));
                    add(new MoreParticles(new ModuleData(Visuals, "MoreParticles", "Multiplies the amount of attack particles you see.")));
                    add(new SimsESP(new ModuleData(Visuals, "SimsESP", "Swebsi Madoo Geelfrob. Renders a Sims thing over peoples heads.")));
                    add(new Spotify(new ModuleData(Other, "Spotify", "Shows currently playing song on Spotify. (Credits to Alerithe HTTP Server)")));
                    add(new PlayPause(new ModuleData(Other, "Play/Pause", "Bind this to toggle Spotify playback.")));
                    add(new Bypass(new ModuleData(Combat, "Bypass", "PingSpoof for Hypixel. Helps features bypass.")));
                    add(new LightningLog(new ModuleData(Other, "LightningLog", "Prints out the coordinates of lightning.")));
                    add(new SilentView(new ModuleData(Other, "SilentView", "Shows your silent view angles in third person.")));
                    add(new AutoMath(new ModuleData(Other, "AutoMath", "Automatically solves \247d\247lQUICK MATHS\247f in the pit.")));
                    add(new ClickTP(new ModuleData(Player, "ClickTP", "Teleports to your clicked position.")));
                    add(new ChatFilter(new ModuleData(Other, "ChatFilter", "Filters the chat in The Pit.")));

                    if (Boolean.parseBoolean(System.getProperty("NEoBuMASs"))) {
                        add(new Aimbot(new ModuleData(Combat, "Aimbot", "Cops n Crims aimbot.")));
                    }

                    if (Boolean.parseBoolean(System.getProperty("")) && Boolean.parseBoolean(System.getProperty(""))) {
                        add(new ZombieAim(new ModuleData(Combat, "ZombieAim", "Aimbot for Hypixel Zombies.")));
                    }

                    //add(new AutoSkin(new ModuleData(Other, "AutoSkin", "Automatically applies a skin when you log into an alt.")));
                    //add(new LagExploit(new ModuleData(Movement, "LagExploit", "")));
                    //GTAMC: add(new AutoBandage(new ModuleData(Other, /*AutoBandage*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0x412a18d3 >>> 0x18),(byte) (0xf0dd41b4 >>> 0xe),(byte) (0xd1d20430 >>> 0x12),(byte) (0x9adffd51 >>> 0x11),(byte) (0x679d0ac0 >>> 0xa),(byte) (0xec3f1eed >>> 0x15),(byte) (0x42752374 >>> 0x3),(byte) (0xc12190ff >>> 0xa),(byte) (0xa184db5 >>> 0xe),(byte) (0xdc7ace93 >>> 0x9),(byte) (0x1877b2ca >>> 0x1)}), /*OK.*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0xddc5c13c >>> 0x2),(byte) (0xda5dae5d >>> 0x13),(byte) (0xc05cfa32 >>> 0x11)}))));
                    //GTAMC: add(new AutoIon(new ModuleData(Other, /*AutoIon*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0x628d4152 >>> 0x8),(byte) (0x6d164eb5 >>> 0x5),(byte) (0x6f1489d2 >>> 0x2),(byte) (0xd24026f9 >>> 0x4),(byte) (0x7158922 >>> 0x5),(byte) (0xc0a6fdd7 >>> 0xc),(byte) (0xbb7657f1 >>> 0x13)}), /*OK.*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0xddc5c13c >>> 0x2),(byte) (0xda5dae5d >>> 0x13),(byte) (0xc05cfa32 >>> 0x11)}))));
                    //add(new Debug(new ModuleData(Other, /*Debug*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0xf164883f >>> 0x9),(byte) (0x7f06517 >>> 0x8),(byte) (0x29ec549c >>> 0xd),(byte) (0x23a944f6 >>> 0x13),(byte) (0xd37119e5 >>> 0x6)}),/*Enables debugging features (Console & Dev Notifs).*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {(byte) (0x1b35116 >>> 0x2),(byte) (0x8c683374 >>> 0x3),(byte) (0x75be6166 >>> 0x8),(byte) (0x188fa268 >>> 0x16),(byte) (0x9b2e81c8 >>> 0x16),(byte) (0xd658074a >>> 0x14),(byte) (0x3b4a7389 >>> 0x8),(byte) (0x7fe3d208 >>> 0x4),(byte) (0xdaea50c9 >>> 0x1),(byte) (0xd78b2b80 >>> 0xb),(byte) (0x5313077d >>> 0x13),(byte) (0x997752f2 >>> 0xc),(byte) (0xb8cf57e5 >>> 0x11),(byte) (0xb233b3d6 >>> 0x7),(byte) (0x6d565a6a >>> 0x6),(byte) (0xd34a0377 >>> 0x3),(byte) (0xb8567bef >>> 0xc),(byte) (0x739df440 >>> 0x1),(byte) (0x9c19a7e4 >>> 0xe),(byte) (0xa6567dd0 >>> 0x14),(byte) (0x546c355a >>> 0xd),(byte) (0x88b918e9 >>> 0x1),(byte) (0x5baf061f >>> 0x13),(byte) (0x37db9586 >>> 0xb),(byte) (0x5d9f032a >>> 0x3),(byte) (0x6a0d1cfc >>> 0x6),(byte) (0x903d633a >>> 0x17),(byte) (0x5a2c141b >>> 0x7),(byte) (0x50877206 >>> 0x11),(byte) (0xb9edee92 >>> 0xd),(byte) (0x14896ed5 >>> 0x8),(byte) (0x251239d0 >>> 0x7),(byte) (0x9edde37d >>> 0x3),(byte) (0xbcd8bf1f >>> 0x11),(byte) (0x41954b94 >>> 0x12),(byte) (0xa905e32b >>> 0x13),(byte) (0xd9377623 >>> 0x13),(byte) (0x20b4e366 >>> 0x18),(byte) (0x6a441856 >>> 0x10),(byte) (0x56566351 >>> 0x14),(byte) (0xd3b27150 >>> 0x13),(byte) (0xa4142e97 >>> 0x15),(byte) (0x8434ee67 >>> 0xc),(byte) (0x6c96de3c >>> 0x9),(byte) (0xa65d193d >>> 0xe),(byte) (0x4c9a5715 >>> 0xe),(byte) (0xaa96566e >>> 0x4),(byte) (0xc5cf6fb8 >>> 0x12),(byte) (0x4a71d384 >>> 0x16),(byte) (0xb2e6e1d0 >>> 0x14)}))));
                    //add(new StreamerMode(new ModuleData(Other, /*Streamer*/AuthenticationUtil.decodeByteArray(new byte[]{(byte) (0x98a6dee4 >>> 0x11), (byte) (0x2304574f >>> 0x4), (byte) (0x251191c9 >>> 0x2), (byte) (0x85348cb2 >>> 0x5), (byte) (0x898b56c2 >>> 0x1), (byte) (0x46d7cb9 >>> 0x10), (byte) (0x56508e69 >>> 0x14), (byte) (0xe3b976f0 >>> 0xf)}),/*Hides information that staff members can use to ban you while streaming.*/AuthenticationUtil.decodeByteArray(new byte[]{(byte) (0x430891b4 >>> 0x9), (byte) (0xd9a9534d >>> 0x3), (byte) (0x6404fed5 >>> 0x18), (byte) (0x4659788d >>> 0xe), (byte) (0x4e7fdf70 >>> 0x15), (byte) (0x1f4400de >>> 0xd), (byte) (0x35569b52 >>> 0xc), (byte) (0x320e22dc >>> 0x1), (byte) (0x439dd9b5 >>> 0x6), (byte) (0x7537ddd1 >>> 0xf), (byte) (0x1918e44a >>> 0x9), (byte) (0xf98db671 >>> 0xa), (byte) (0xb0d1f331 >>> 0x17), (byte) (0xdd08458d >>> 0x16), (byte) (0x365a6690 >>> 0x4), (byte) (0x1b792580 >>> 0x13), (byte) (0xc4f7b744 >>> 0x7), (byte) (0xb5429040 >>> 0x7), (byte) (0x88ba29e1 >>> 0xf), (byte) (0xcf868d04 >>> 0xc), (byte) (0x9f6c1308 >>> 0x3), (byte) (0x969d0f5d >>> 0xe), (byte) (0x1881d6fe >>> 0x12), (byte) (0x58624ae6 >>> 0x1), (byte) (0xc7ae8dea >>> 0xd), (byte) (0x861b1ce5 >>> 0x14), (byte) (0xf672ccc >>> 0x1), (byte) (0x29c72cde >>> 0x5), (byte) (0x41d4079 >>> 0x15), (byte) (0xa6ded924 >>> 0x14), (byte) (0x91d95671 >>> 0xe), (byte) (0x8db2cd7b >>> 0x15), (byte) (0x6415318b >>> 0x2), (byte) (0xf0c65a9e >>> 0xc), (byte) (0x7c717250 >>> 0x8), (byte) (0x1d2b9cb0 >>> 0xb), (byte) (0x10686cd9 >>> 0x17), (byte) (0x1d630fa2 >>> 0x10), (byte) (0x5aa75853 >>> 0x6), (byte) (0x406e2fc4 >>> 0x10), (byte) (0x1d827480 >>> 0x2), (byte) (0x11d403f8 >>> 0x12), (byte) (0xde239929 >>> 0xb), (byte) (0x1d95f8ad >>> 0x12), (byte) (0x7200de07 >>> 0x14), (byte) (0xd8fba366 >>> 0xb), (byte) (0xe932c9bc >>> 0x2), (byte) (0x2bf84839 >>> 0x6), (byte) (0xd88fe18d >>> 0x16), (byte) (0xb9f26161 >>> 0x8), (byte) (0x456e66eb >>> 0x4), (byte) (0x3c524162 >>> 0x9), (byte) (0xe87dbcf3 >>> 0x1), (byte) (0x6f1a6925 >>> 0x18), (byte) (0x1c97519e >>> 0xc), (byte) (0x905390f2 >>> 0x17), (byte) (0x64169df7 >>> 0x6), (byte) (0xe2a75a3a >>> 0x6), (byte) (0xef468d24 >>> 0x5), (byte) (0x447279b3 >>> 0x2), (byte) (0xf655d16 >>> 0x10), (byte) (0xf1076389 >>> 0x13), (byte) (0x2e8d4b9e >>> 0x3), (byte) (0xc7d5e740 >>> 0x4), (byte) (0xe3cee4e7 >>> 0x9), (byte) (0x4ca1bb7a >>> 0x15), (byte) (0x41ac2caa >>> 0xd), (byte) (0xf89d6d64 >>> 0x8), (byte) (0xb87234e9 >>> 0x7), (byte) (0xd226ed2e >>> 0xc), (byte) (0xab38c894 >>> 0x13), (byte) (0xa0974e19 >>> 0xf)}))));

                    add(new NetGraph(new ModuleData(Other, "NetGraph", "Shows incoming/outgoing network data.")));

                }
            } catch (Exception e) {
                exhibition.util.security.Snitch.snitch(22, e.getMessage(), e.getLocalizedMessage()); // ????
                KillProcess.killMC();
            }
        } catch (Exception e) {
        }

        setup = true;
        loadStatus();
        if (!get(ChatCommands.class).isEnabled()) {
            get(ChatCommands.class).toggle();
        }

        if (!get(Bypass.class).isHidden()) {
            get(Bypass.class).setHidden(true);
        }
    }

    public static void saveStatus() {
        if (!Client.getModuleManager().isSetup())
            return;
        List<String> fileContent = new ArrayList<>();

        List<String> binds = new ArrayList<>();

        for (Module module : Client.getModuleManager().getArray()) {
            String displayName = module.getName();
            String enabled = Boolean.toString(module.isEnabled());
            String hidden = Boolean.toString(module.isHidden());
            String onHeld = Boolean.toString(module.getHeld());
            fileContent.add(String.format("%s:%s:%s:%s", displayName, enabled, hidden, onHeld));

            int bind = module.data.getKey();
            binds.add(String.format("%s:%s", displayName, bind));
        }
        FileUtils.write(MODULE_DIR, fileContent, true);

        FileUtils.write(BINDS_DIR, binds, true);
    }

    public static void saveSettings() {
        List<String> fileContent = new ArrayList<>();
        for (Module module : Client.getModuleManager().getArray()) {
            for (Setting setting : module.getSettings().values()) {
                if ((setting.getValue() instanceof Options)) {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    String settingValue = ((Options) setting.getValue()).getSelected();
                    fileContent.add(String.format("%s:%s:%s", displayName, settingName, settingValue));
                } else if (setting.getValue() instanceof MultiBool) {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    List<String> enabled = new ArrayList<>();
                    ((MultiBool) setting.getValue()).getBooleans().forEach(set -> enabled.add(set.getName() + "=" + set.getValue()));
                    String settingValue = enabled.toString();
                    fileContent.add(String.format("%s:%s:%s", displayName, settingName, settingValue));
                } else if (setting.getValue() instanceof BlockList) {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    BlockList blockList = (BlockList) setting.getValue();
                    String settingValue = blockList.toString().replaceAll("\\s+", "");
                    ;
                    fileContent.add(String.format("%s:%s:%s", displayName, settingName, settingValue));
                } else {
                    String displayName = module.getName();
                    String settingName = setting.getName();
                    String settingValue = setting.getValue().toString();
                    fileContent.add(String.format("%s:%s:%s", displayName, settingName, settingValue));
                }
            }
        }
        for (Setting setting : GlobalValues.globalValues) {
            if ((setting.getValue() instanceof Options)) {
                String settingName = setting.getName();
                String settingValue = ((Options) setting.getValue()).getSelected();
                fileContent.add(String.format("%s:%s:%s", "GlobalValue", settingName, settingValue));
            } else if (setting.getValue() instanceof MultiBool) {
                String settingName = setting.getName();
                List<String> enabled = new ArrayList<>();
                ((MultiBool) setting.getValue()).getBooleans().forEach(set -> enabled.add(set.getName() + "=" + set.getValue()));
                String settingValue = enabled.toString();
                fileContent.add(String.format("%s:%s:%s", "GlobalValue", settingName, settingValue));
            } else if (setting.getValue() instanceof BlockList) {
                String settingName = setting.getName();
                BlockList blockList = (BlockList) setting.getValue();
                String settingValue = blockList.toString().replaceAll("\\s+", "");
                ;
                fileContent.add(String.format("%s:%s:%s", "GlobalValue", settingName, settingValue));
            } else {
                String settingName = setting.getName();
                String settingValue = setting.getValue().toString();
                fileContent.add(String.format("%s:%s:%s", "GlobalValue", settingName, settingValue));
            }
        }
        FileUtils.write(SETTINGS_DIR, fileContent, true);
    }

    public static void loadStatus() {
        try {
            boolean isNew = false;
            List<String> fileContent = FileUtils.read(MODULE_DIR);
            for (String line : fileContent) {
                String[] split = line.split(":");
                // The client recognizes that the Mods file is new
                if (split.length >= 3) {
                    String displayName = split[0];
                    for (Module module : Client.getModuleManager().getArray()) {
                        if (module.getName().equalsIgnoreCase(displayName)) {
                            String strEnabled = split[1];
                            boolean enabled = Boolean.parseBoolean(strEnabled);
                            if (enabled && !module.isEnabled()) {
                                module.setEnabled(true);
                                EventSystem.register(module);
                                module.onEnable();
                            }
                            module.setHidden(Boolean.parseBoolean(split[2]));
                            // displayName, enabled, hidden, onHeld
                            if (split.length > 3) {
                                module.setHeld(Boolean.parseBoolean(split[3]));
                            }
                        }
                    }
                    isNew = true;
                } else {
                    String displayName = split[0];
                    for (Module module : Client.getModuleManager().getArray()) {
                        if (module.getName().equalsIgnoreCase(displayName)) {
                            boolean enabled = Boolean.parseBoolean(split[1]);
                            module.setKeybind(new Keybind(module, Integer.parseInt(split[2])));
                            if (enabled && !module.isEnabled()) {
                                module.setEnabled(true);
                                EventSystem.register(module);
                                module.onEnable();
                            }
                            module.setHidden(Boolean.parseBoolean(split[3]));
                        }
                    }
                }
            }
            if (!isNew) {
                Notifications.getManager().post("Updated Mods.txt", "Your Mods.txt has been updated successfully!", 5000, Notifications.Type.INFO);
                Notifications.getManager().post("Updated Binds.txt", "Binds.txt has been created successfully!", 5000, Notifications.Type.INFO);
                saveStatus();
            } else {
                List<String> binds = FileUtils.read(BINDS_DIR);
                for (String line : binds) {
                    String[] split = line.split(":");
                    String displayName = split[0];
                    for (Module module : Client.getModuleManager().getArray()) {
                        if (module.getName().equalsIgnoreCase(displayName)) {
                            int bind = Integer.parseInt(split[1]);
                            module.setKeybind(new Keybind(module, bind));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSettings() {
        String lastLine = "";
        try {
            List<String> fileContent = FileUtils.read(SETTINGS_DIR);
            for (String line : fileContent) {
                lastLine = line;
                String[] split = line.split(":");
                if (split[0].equalsIgnoreCase("GlobalValue")) {
                    for (Setting setting : GlobalValues.globalValues) {
                        if (split.length < 3)
                            continue;

                        if (!setting.getName().equalsIgnoreCase(split[1]))
                            continue;

                        String settingValue = split[2];
                        if (setting.getValue() instanceof Number) {
                            if (settingValue.equalsIgnoreCase("true") || settingValue.equalsIgnoreCase("false"))
                                continue;
                            Object newValue = (StringConversions.castNumber(settingValue, setting.getValue()));
                            if (newValue != null) {
                                setting.setValue(newValue);
                            }
                        } // If the multiBool is supposed to be a string
                        else if (setting.getValue().getClass().equals(String.class)) {
                            StringBuilder fixedString = new StringBuilder(settingValue);

                            if (split.length > 3) {
                                System.out.println("Has more bruh??");

                                for (int i = 3; i < split.length; i++) {
                                    fixedString.append(":");
                                    fixedString.append(split[i]);
                                }

                                System.out.println("Fixed: " + fixedString);
                            }

                            String parsed = fixedString.toString();

                            if (!setting.getName().equalsIgnoreCase("URL")) {
                                parsed = parsed.replaceAll("_", " ");
                            }

                            setting.setValue(parsed);
                        } // If the multiBool is supposed to be a boolean
                        else if (setting.getValue().getClass().equals(Boolean.class)) {
                            setting.setValue(Boolean.parseBoolean(settingValue));
                        } // If the multiBool is supposed to be an option
                        else if (setting.getValue().getClass().equals(Options.class)) {
                            Options option = ((Options) setting.getValue());
                            for (String options : option.getOptions()) {
                                if (settingValue.equals(options)) {
                                    option.setSelected(options);
                                    break;
                                }
                            }
                        } // If the multiBool is supposed to be a multibool
                        else if (setting.getValue().getClass().equals(MultiBool.class)) {
                            MultiBool multiBool = (MultiBool) setting.getValue();
                            List<String> items = Arrays.asList(settingValue.replace("[", "").replace("]", "").split("\\s*,\\s*"));
                            items.forEach(o -> multiBool.getBooleans().forEach(bool -> {
                                if (o.startsWith(bool.getName())) {
                                    String e = (o.split("=")[1]);
                                    bool.setValue(e.equalsIgnoreCase("true"));
                                }
                            }));
                        } // If the multiBool is supposed to be a multibool
                        else if (setting.getValue().getClass().equals(BlockList.class)) {
                            BlockList blockList = (BlockList) setting.getValue();
                            int[] ids = Stream.of(settingValue.replace("[", "").replace("]", "").split("\\s*,\\s*")).mapToInt(Integer::parseInt).toArray();
                            blockList.setBlockList(ids);
                        }
                    }
                }

                for (Module module : Client.getModuleManager().getArray()) {
                    if (module.getName().equalsIgnoreCase(split[0])) {
                        if (split.length < 3)
                            continue;
                        Setting setting = Module.getSetting(module.getSettings(), split[1]);
                        String settingValue = split[2];
                        if (setting != null) {
                            if (setting.getValue() instanceof Number) {
                                if (settingValue.equalsIgnoreCase("true") || settingValue.equalsIgnoreCase("false"))
                                    continue;
                                Object newValue = (StringConversions.castNumber(settingValue, setting.getValue()));
                                if (newValue != null) {
                                    setting.setValue(newValue);
                                }
                            } // If the multiBool is supposed to be a string
                            else if (setting.getValue().getClass().equals(String.class)) {
                                StringBuilder fixedString = new StringBuilder(settingValue);

                                if (split.length > 3) {
                                    for (int i = 3; i < split.length; i++) {
                                        fixedString.append(":");
                                        fixedString.append(split[i]);
                                    }
                                }

                                String parsed = fixedString.toString();

                                if (!setting.getName().equalsIgnoreCase("URL")) {
                                    parsed = parsed.replaceAll("_", " ");
                                }

                                setting.setValue(parsed);
                            } // If the multiBool is supposed to be a boolean
                            else if (setting.getValue().getClass().equals(Boolean.class)) {
                                setting.setValue(Boolean.parseBoolean(settingValue));
                            } // If the multiBool is supposed to be an option
                            else if (setting.getValue().getClass().equals(Options.class)) {
                                Options option = ((Options) setting.getValue());
                                for (String options : option.getOptions()) {
                                    if (settingValue.equals(options)) {
                                        option.setSelected(options);
                                        break;
                                    }
                                }
                            } // If the multiBool is supposed to be a multibool
                            else if (setting.getValue().getClass().equals(MultiBool.class)) {
                                MultiBool multiBool = (MultiBool) setting.getValue();
                                List<String> items = Arrays.asList(settingValue.replace("[", "").replace("]", "").split("\\s*,\\s*"));
                                items.forEach(o -> multiBool.getBooleans().forEach(bool -> {
                                    if (o.startsWith(bool.getName())) {
                                        String e = (o.split("=")[1]);
                                        bool.setValue(e.equalsIgnoreCase("true"));
                                    }
                                }));
                            } // If the multiBool is supposed to be a multibool
                            else if (setting.getValue().getClass().equals(BlockList.class)) {
                                BlockList blockList = (BlockList) setting.getValue();
                                int[] ids = Stream.of(settingValue.replace("[", "").replace("]", "").split("\\s*,\\s*")).mapToInt(Integer::parseInt).toArray();
                                blockList.setBlockList(ids);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(lastLine);
            e.printStackTrace();
        }
    }

    private void loadLocalPlugins() {
        // Get the directory of the jars
        String basePath = Client.getDataDir().getAbsolutePath();
        String newPath = basePath + ((basePath.endsWith(File.separator)) ? SubFolder.ModuleJars.getFolderName() : File.separator + SubFolder.ModuleJars.getFolderName());
        File test = new File(newPath);
        // Make the directory if it does not exist
        if (!test.exists()) {
            test.mkdirs();
        }
        // Loop through files in the directory
        for (File file : test.listFiles()) {
            // Load jars
            if (file.getAbsolutePath().endsWith(".jar")) {
                try {
                    loadJar(file);
                    LogManager.getLogger().info(file.getAbsoluteFile().getName() + " has been successfully loaded!");
                } catch (IOException e) {
                    System.out.println("IOException thrown! -- Error loading Plugin.");
                    e.printStackTrace();
                }
            }
        }
    }


    private void loadJar(File file) throws IOException {
        JarInputStream jis = new JarInputStream(new FileInputStream(file));
        URLClassLoader urlLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()});
        for (JarEntry jarEntry = jis.getNextJarEntry(); jarEntry != null; jarEntry = jis.getNextJarEntry()) {
            // Skip non-jar entries
            if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
                continue;
            }
            String className = jarEntry.getName().replace('/', '.').substring(0,
                    jarEntry.getName().length() - ".class".length());
            // Skip internal classes / others
            if (className.contains("$")) {
                continue;
            }
            try {
                // Attempt to load the class and create an instance.
                Class<?> classs = urlLoader.loadClass(className);
                // If the class is a module, load it.
                if (Module.class.isAssignableFrom(classs)) {
                    add((Module) classs.newInstance());
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        // Close resources when complete.
        jis.close();
        urlLoader.close();
    }

    public boolean isSetup() {
        return setup;
    }

    public boolean isEnabled(Class<? extends Module> clazz) {
        Module module = get(clazz);
        return module != null && module.isEnabled();
    }

    public Module get(String name) {
        for (Module module : getArray()) {
            if (module.getName().toLowerCase().equals(name.toLowerCase())) {
                return module;
            }
        }
        return null;
    }

}
