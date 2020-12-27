package exhibition.management.command;

import exhibition.management.command.impl.*;
import exhibition.util.security.AuthenticationUtil;
import org.sun.verification.Unknown;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by cool1 on 3/9/2017.
 */
public class CommandManager {

    public void addCommand(String name, Command command) {
        commandMap.put(name, command);
    }

    public Collection<Command> getCommands() {
        return commandMap.values();
    }

    public Command getCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }

    public static final HashMap<String, Command> commandMap = new HashMap<>();

    public void setup() {
        new Unknown().unknown();
        new ColorCommand(new String[]{"Color", "c"}, "Saves colors and shit.").register(this);
        //new Insult(new String[]{"Insult", "i"}, "Insult those nodus users.").register(this);
        new Damage(new String[]{"Damage", "d", "dmg", "kys", "suicide", "amandatodd"}, "Damages the player").register(this);
        new Load(new String[]{"Load", "l"}, "Loads your settings from settings.txt").register(this);
        new Save(new String[]{"Save", "s"}, "Save your settings from settings.txt").register(this);
        new Toggle(new String[]{"Toggle", "t"}, "Toggles the module or changes toggle mode.").register(this);
        new Settings(new String[]{"Setting", "set", "s"}, "Changing and listing settings for modules.").register(this);
        new Help(new String[]{"Help", "halp", "h"}, "Help for commands.").register(this);
        new Say(new String[]{"Sudo", "ssay"}, "Forge messages with color codes that only appear in your chat.").register(this);
        new Bind(new String[]{"Bind", "key", "b"}, "Bind a key to a module.").register(this);
        new Friend(new String[]{"Friend", "fr", "f"}, "Add and remove friends.").register(this);
        new Target(new String[]{"Target", "focus", "tar", "vip", "priority"}, "Set the target for the Aura.").register(this);
        new Clear(new String[]{"Clear", "cl", "clr"}, "Clears chat for you.").register(this);
        if (AuthenticationUtil.getHwid() == 32161752)
            new NotificationTest(new String[]{"Test", "nt"}, "Notifications test.").register(this);

        new VClip(new String[]{"Vclip", "vc", "clip"}, "Clips you vertically.").register(this);
        new Waypoint(new String[]{"Waypoint", "waypoints", "wp", "marker"}, "Waypoint command.").register(this);
        new Config(new String[]{"Config", "configs", "cfg", "cfgs"}, "Config management.").register(this);
        new Hide(new String[]{"Hide", "hidden", "visible", "vis", "hi"}, "Hide/Show modules.").register(this);
        new PluginScanner(new String[]{"Plugins", "plugin", "pluginscanner", "scanner"}, "Scans for server plugins.").register(this);
        new HelpIAmmUsingABlatantClientAndNeedToPanic(new String[]{"Panic", "imgettingscreenshared", "panick", "codered"}, "Does what a GHOST client does?").register(this);
        new FixEvents(new String[]{"FixEvents", "fix", "events"}, "Resets Event Registry to fix bugged modules.");
        new Teleport(new String[]{"Teleport", "tp", "tpto"}, "Teleports you to a certain player/position.").register(this);
        new Name(new String[]{"Name", "username", "copy"}, "Copies your name to your clipboard.").register(this);
    }

}
