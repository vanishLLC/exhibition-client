package exhibition.management;

import com.google.gson.*;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;

import java.util.*;

public class UUIDResolver {

    public static UUIDResolver instance = new UUIDResolver();

    public boolean isChecking;

    public Gson gson = new Gson();

    public HashMap<String, Long> validMap = new HashMap<>();
    public List<String> checkedUsernames = new ArrayList<>();

    private final HashMap<String, Long> responseMap = new HashMap<>();
    private boolean isRateLimited;

    public boolean isInvalidName(String username) {
        if (isChecking)
            return false;

        if (!checkedUsernames.contains(username))
            return false;

        validMap.entrySet().removeIf(map -> (map.getValue() + 600_000) > System.currentTimeMillis());

        return validMap.containsKey(username);
    }

    public void checkNames(List<String> usernamesToCheck) {
        responseMap.entrySet().removeIf(map -> (map.getValue() + 600_000) > System.currentTimeMillis());

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
                checkedUsernames.add(name);
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
        } catch (Exception ignored) {

        }
    }

    public class CheckThread extends Thread {

        private final List<String> usernameList;

        public CheckThread(List<String> usernames) {
            this.usernameList = usernames;
        }

        @Override
        public void run() {
            isChecking = true;
            List<String> tempList = new ArrayList<>();
            for (String s : usernameList) {
                tempList.add(s);
                if (tempList.size() == 10) {
                    resolveNames(tempList.toArray(new String[]{}));
                    tempList.clear();
                }
            }
            if (!tempList.isEmpty() && tempList.size() <= 10) {
                resolveNames(tempList.toArray(new String[]{}));
            }
            isChecking = false;
        }
    }

}
