package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.event.EventSystem;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.module.impl.combat.AntiBot;
import exhibition.module.impl.combat.Bypass;
import exhibition.module.impl.hud.HUD;
import exhibition.module.impl.other.ChatCommands;

import java.util.ArrayList;
import java.util.List;

public class HelpIAmmUsingABlatantClientAndNeedToPanic extends Command {

    public HelpIAmmUsingABlatantClientAndNeedToPanic(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "You're using a blatant client for fuck sake.";
    }

    @Override
    public void fire(String[] args) {

        Class[] classes = new Class[]{HUD.class, AntiBot.class, Bypass.class, ChatCommands.class};

        List<Module> modules = new ArrayList<>();

        for (Class mClass : classes) {
            modules.add(Client.getModuleManager().get(mClass));
        }

        for (Module module : Client.getModuleManager().getArray()) {
            if (!(module instanceof ChatCommands))
                if (module.isEnabled() && !modules.contains(module)) {
                    module.setEnabled(false);
                    EventSystem.unregister(module);
                    module.onDisable();
                }
        }
    }

}
