/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.management.command.impl;

import exhibition.Client;
import exhibition.event.EventSystem;
import exhibition.management.command.Command;
import exhibition.module.Module;
import exhibition.util.misc.ChatUtil;

public class FixEvents extends Command {

    public FixEvents(String[] names, String description) {
        super(names, description);
    }

    @Override
    public String getUsage() {
        return "Resets event registry in case they are bugged.";
    }

    @Override
    public void fire(String[] args) {
        for (Module module : Client.getModuleManager().getArray()) {
            if(!module.isEnabled()) {
                EventSystem.unregister(module);
            }
        }
        ChatUtil.printChat(chatPrefix + "Force unregistered all disabled modules.");
    }
}
