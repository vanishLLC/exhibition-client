package exhibition.management;

import com.google.gson.*;
import exhibition.Client;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;
import net.minecraft.client.Minecraft;

import java.util.*;

public class UUIDResolver {

    public static UUIDResolver instance = new UUIDResolver();

    public boolean isChecking;

    public Gson gson = new Gson();

    public HashMap<String, Long> validMap = new HashMap<>();
    public List<String> checkedUsernames = new ArrayList<>();

    private final HashMap<String, Long> responseMap = new HashMap<>();
    private final HashMap<String, Long> hypixelResponseMap = new HashMap<>();
    private boolean isRateLimited;

    public boolean isInvalidName(String username) {
        if (!checkedUsernames.contains(username)) {
            return false;
        }

        if (!validMap.containsKey(username)) {
            return true;
        }

        // Check hypixel api

        return false;
    }

    public void checkNames(HashMap<String, UUID> usernamesToCheck) {
        responseMap.entrySet().removeIf(map -> (map.getValue() + 600_000) < System.currentTimeMillis());
        hypixelResponseMap.entrySet().removeIf(map -> (map.getValue() + 60_000) < System.currentTimeMillis());

        if (responseMap.size() < 600) {
            if (isRateLimited && responseMap.size() < 550) {
                isRateLimited = false;
                Notifications.getManager().post("Nick Detector", "No longer rate limited. Checking again.", 1500, Notifications.Type.OKAY);
            }
            if (!isRateLimited) {
                CheckThread checkThread = new CheckThread(usernamesToCheck);
                checkThread.start();
            }
        } else {
            if (!isRateLimited) {
                isRateLimited = true;
                Notifications.getManager().post("Nick Detector", "Rate limit reached. Waiting for requests to expire.", 2500, Notifications.Type.NOTIFY);
            }
        }
    }

    private void resolveNames(String... names) {

        try {
            Connection testConnection = new Connection("https://api.mojang.com/profiles/minecraft");
            testConnection.setContentType("application/json");

            JsonArray jsonArray = new JsonArray();
            for (String name : names) {
                jsonArray.add(name);
            }
            testConnection.setJson(gson.toJson(jsonArray));
            String resultStr = Connector.post(testConnection);

            JsonArray resultArray = JsonParser.parseString(resultStr).getAsJsonArray();

            long current = System.currentTimeMillis();
            for (JsonElement jsonElement : resultArray) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                String name = jsonObject.get("name").getAsString();
                validMap.put(name, current);
            }

            responseMap.put(resultStr, current);

            for (String name : names) {
                checkedUsernames.add(name);
                if (!validMap.containsKey(name)) {
                    Notifications.getManager().post("Nick Detector", name + " is not a real player name!", 2500, Notifications.Type.WARNING);
                }
            }
        } catch (Exception ignored) {

        }
    }

    public class CheckThread extends Thread {

        private final HashMap<String, UUID> usernameList;

        public CheckThread(HashMap<String, UUID> usernames) {
            this.usernameList = usernames;
        }

        @Override
        public void run() {
            isChecking = true;
            try {
                List<String> tempList = new ArrayList<>();
                for (String s : usernameList.keySet()) {
                    if (!isChecking || Minecraft.getMinecraft().thePlayer == null)
                        return;
                    tempList.add(s);
                    if (tempList.size() == 10) {
                        resolveNames(tempList.toArray(new String[]{}));
                        tempList.clear();
                    }
                }
                if (!tempList.isEmpty() && tempList.size() <= 10) {
                    resolveNames(tempList.toArray(new String[]{}));
                }

                try {
                    if (isChecking && Client.instance.hypixelApiKey != null && !Client.instance.hypixelApiKey.equals("") && hypixelResponseMap.size() < 120) {
                        for (String username : usernameList.keySet()) {
                            if (!isChecking || Minecraft.getMinecraft().thePlayer == null)
                                return;
                            if (validMap.containsKey(username) && checkedUsernames.contains(username)) {
                                Connection hypixelApiConnection = new Connection("https://api.hypixel.net/player");

                                hypixelApiConnection.setParameters("key", Client.instance.hypixelApiKey);
                                hypixelApiConnection.setParameters("uuid", usernameList.get(username).toString());

                                String response = Connector.get(hypixelApiConnection);

                                JsonObject jsonObject = (JsonObject) JsonParser.parseString(response);

                                boolean success = jsonObject.get("success").getAsBoolean();
                                boolean playerNull = jsonObject.get("player").isJsonNull();

                                if (success) {
                                    if (playerNull) {
                                        Notifications.getManager().post("Nick Detector", username + " is in /nick!", 2500, Notifications.Type.WARNING);
                                        validMap.remove(username);
                                    }
                                }

                                hypixelResponseMap.put(response, System.currentTimeMillis());
                            }
                        }
                    }
                } catch (Exception e) {

                }
            } catch (Exception e) {
                isChecking = false;
            }
            isChecking = false;
        }
    }

}
