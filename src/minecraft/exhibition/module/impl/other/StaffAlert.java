/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class StaffAlert extends Module {

    private List<String> alertedNames = new ArrayList<>();

    public StaffAlert(ModuleData data) {
        super(data);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @Override
    public void onEnable() {
        alertedNames.clear();
    }

    @RegisterEvent(events = {EventTick.class})
    public void onEvent(Event event) {
        if(mc.getIntegratedServer() != null || mc.getCurrentServerData() == null)
            return;
        try {
            for (Entity entity : mc.theWorld.getLoadedEntityList()) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    String unformatted = entity.getDisplayName().getUnformattedText().toLowerCase();
                    String formatted = entity.getDisplayName().getFormattedText();

                    if (unformatted.contains("[helper]") || unformatted.contains("[mod]") || formatted.contains("\2479" + entity.getName()) && !formatted.contains("\247l")) {
                        if (!this.alertedNames.contains(entity.getName())) {
                            Notifications.getManager().post("Staff Detected!", "Staff member in your Lobby! " + entity.getDisplayName().getUnformattedText(), 2000, Notifications.Type.WARNING);
                            alertedNames.add(entity.getName());
                        }
                    }
                }
            }

        } catch (Exception e) {

        }
    }

}
