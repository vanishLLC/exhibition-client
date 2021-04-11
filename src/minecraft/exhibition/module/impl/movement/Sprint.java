package exhibition.module.impl.movement;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.player.Scaffold;
import net.minecraft.client.settings.KeyBinding;

public class Sprint extends Module {
	public Sprint(ModuleData data) {
		super(data);
	}

	@RegisterEvent(events = { EventRenderGui.class })
	public void onEvent(Event event) {
			if (canSprint()) {
				if(!mc.gameSettings.keyBindSprint.getIsKeyPressed()) {
					KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
					KeyBinding.onTick(mc.gameSettings.keyBindSprint.getKeyCode());
				}
			} else if(Client.getModuleManager().isEnabled(Scaffold.class) && !Client.getModuleManager().isEnabled(Speed.class) && mc.gameSettings.keyBindSprint.getIsKeyPressed()) {
				KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
				KeyBinding.onTick(mc.gameSettings.keyBindSprint.getKeyCode());
			}
	}

	private boolean canSprint() {
		return !(mc.thePlayer.moveForward == 0) && !mc.thePlayer.isSneaking() && mc.thePlayer.getFoodStats().getFoodLevel() >= 6 && !mc.thePlayer.isCollidedHorizontally && (!Client.getModuleManager().isEnabled(Scaffold.class) || Client.getModuleManager().isEnabled(Speed.class));
	}
}
