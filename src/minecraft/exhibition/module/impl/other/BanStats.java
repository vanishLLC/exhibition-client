package exhibition.module.impl.other;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BanStats extends Module {

    private final BanStatsThread banStatsThread = new BanStatsThread(this);

    public long banDifference;
    public long bansLastMinute;
    public long bansSinceConnect;


    public final Timer banTimer = new Timer();

    private final ConcurrentHashMap<Long, Long> banDifferenceMap = new ConcurrentHashMap<>();

    public final Setting<Boolean> alertBans = new Setting<>("ALERT", false, "Notifies you if BanStats notices spikes in bans.");

    public BanStats(ModuleData data) {
        super(data);
        addSetting(alertBans);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        banStatsThread.stopThread();
    }

    @RegisterEvent(events = {EventTick.class})
    public void onEvent(Event event) {
        if (!banStatsThread.isRunning) {
            banStatsThread.start();
        }

        try {
            long tempBansLastMinute = 0;
            Iterator<Map.Entry<Long, Long>> iterator = banDifferenceMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, Long> set = iterator.next();

                if (set.getKey() < System.currentTimeMillis()) {
                    iterator.remove();
                    continue;
                }

                long bans = set.getValue();
                tempBansLastMinute += bans;
            }
            bansLastMinute = tempBansLastMinute;
        } catch (Exception e) {

        }
    }

    private static class BanStatsThread extends Thread {

        private final BanStats banStats;
        private boolean isRunning;
        private long staffTotalBans = 0;

        public BanStatsThread(BanStats banStats) {
            this.banStats = banStats;
            this.isRunning = false;
        }

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {
            while (Client.instance != null && banStats.isEnabled() && isRunning) {
                while (banStats.mc.thePlayer == null || banStats.mc.theWorld == null || Client.instance.hypixelApiKey == null || Client.instance.hypixelApiKey.equals("")) {
                    if (!isRunning)
                        return;
                    Thread.yield();
                }

                try {
                    Connection hypixelApiConnection = new Connection("https://api.hypixel.net/watchdogStats");

                    hypixelApiConnection.setParameters("key", Client.instance.hypixelApiKey);

                    try {
                        String response = Connector.get(hypixelApiConnection);
                        JsonObject jsonObject = (JsonObject) JsonParser.parseString(response);

                        boolean success = jsonObject.get("success").getAsBoolean();
                        long staff_total = jsonObject.get("staff_total").getAsLong();

                        if (success) {
                            if(staffTotalBans != 0 && banStats.bansSinceConnect == 0 && banStats.banTimer.getDifference() >= 40_000) {
                                staffTotalBans = 0;
                                banStats.banTimer.reset();
                            }

                            long diff = staff_total - staffTotalBans;
                            if (staffTotalBans != 0)
                                banStats.banDifference = diff;
                            if (staffTotalBans != 0 && diff != 0) {
                                banStats.banTimer.reset();
                                banStats.banDifferenceMap.put(System.currentTimeMillis() + 300_000, diff);
                                banStats.bansSinceConnect += diff;
                            }

                            if (staffTotalBans != 0 && diff >= 4 && banStats.alertBans.getValue()) {
                                Notifications.getManager().post("Staff Activity", "Staff seem to be banning a lot.", 3000, Notifications.Type.WARNING);
                            }

                            if (staffTotalBans != 0 && diff > 0 && banStats.alertBans.getValue() && banStats.banTimer.getDifference() >= 120_000) {
                                Notifications.getManager().post("Staff Activity", "Staff are no longer inactive.", 3000, Notifications.Type.WARNING);
                            }

                            staffTotalBans = staff_total;
                        }
                        Thread.sleep(20_000);
                    } catch (Exception e) {
                        Thread.sleep(5_000);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    isRunning = false;
                }
            }
        }

        public void stopThread() {
            this.isRunning = false;
        }

    }

}
