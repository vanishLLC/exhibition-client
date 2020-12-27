package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventChat;
import exhibition.management.command.Command;
import exhibition.management.command.CommandManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.misc.ChatUtil;

import java.util.Arrays;

public class ChatCommands extends Module {
    private String KEY_PREFIX = "CHAT-PREFIX";

    public ChatCommands(ModuleData data) {
        super(data);
        settings.put(KEY_PREFIX, new Setting(KEY_PREFIX, ".", "Command prefix."));
    }

    @RegisterEvent(events = {EventChat.class})
    public void onEvent(Event event) {
        EventChat ec = (EventChat) event;
        //If the event does not start with the chat prefix, ignore it
        String prefix = (String) settings.get(KEY_PREFIX).getValue();
        if (!ec.getText().startsWith(prefix)) {
            return;
        }
        //If it begins with the chat prefix, cancel it.
        event.setCancelled(true);
        //Get the command and its arguments
        String commandBits[] = ec.getText().substring(prefix.length()).split(" ");
        String commandName = commandBits[0].toLowerCase();
        //Get the command and fire it with arguments
        Command command = CommandManager.commandMap.get(commandName);
        if (command == null) {
            ChatUtil.printChat(Command.chatPrefix + "Command \"" + commandName + "\" does not exist!");
            return;
        }
        if (commandBits.length > 1) {
            String[] commandArguments = Arrays.copyOfRange(commandBits, 1, commandBits.length);
            command.fire(commandArguments);
        } else {
            command.fire(null);
        }
    }

}
