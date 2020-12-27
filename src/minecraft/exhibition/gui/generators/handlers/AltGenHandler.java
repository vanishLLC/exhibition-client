package exhibition.gui.generators.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exhibition.util.security.Connection;
import exhibition.util.security.SSLConnector;

public class AltGenHandler {
    // My alt key: 4b91b6f1-9187-c914-0c4a-01fe7d0de041
    private static String baseURL = "https://api.alt-gen.com/";
    private AltGenUser user;
    private String apiKey;

    public AltGenHandler() {
        this.apiKey = null;
    }

    public static String getBaseURL() {
        return baseURL;
    }

/*
{
    "id": 1,                                // User ID.
    "username": "Warthy",                   // Username.
    "nickname": "Jørgen V.",                // User defined nickname.
    "email": "Warthy@AltDispenser.net",     // Email address.
    "first_name": "Jørgen",                 // First name.
    "last_name": "",                        // Last name.
    "verified": 1,                          // Whether or not the user's email address is verified.
    "created_at": "2016-01-19 13:34:54",    // Alt-Gen registration date.
    "displayname": "Jørgen V.",             // Name displayed to other users. Nickname is prioritized over first and last name.
    "full_name": "Jørgen",                  // First name and last name combined.
    "has_generator_access": false,          // Whether or not the user can access the generator.
    "active_plan": "Free"                   // The user's currently active Alt-Gen subscription/plan.
    "avatar": "https://www.gravatar.com/avatar/342a6bec9aaede125e09ccd070c48ad1?s=80&d=identicon&r=x" // URL to user's avatar.
}
 */

/*
{
   "id":36454,
   "username":"arthimo",
   "nickname":"",
   "email":"cool101dood@gmail.com",
   "first_name":"",
   "last_name":"",
   "verified":1,
   "created_at":"2016-07-02 00:16:48",
   "displayname":"arthimo",
   "full_name":" ",
   "avatar":"https:\/\/www.gravatar.com\/avatar\/cbea40bf6141e2c97ad46ff78a063ff1?s=80&d=identicon&r=x",
   "has_generator_access":false,
   "active_plan":"Free"
}
 */

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Connection connectApi(String dir) {
        Connection connection = new Connection(baseURL + dir);
        connection.setHeaders("X-Authorization", apiKey);
        return connection;
    }

    public boolean setupUser() {
        String response = SSLConnector.get(connectApi("user"));
        if (response.contains("error"))
            return false;
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        this.user = new AltGenUser(jo.get("username").getAsString(), jo.get("avatar").getAsString(), jo.get("active_plan").getAsString());
        return true;
    }

    public String getAlt() {
        String response = SSLConnector.get(connectApi("generate"));
        if (response.contains("error"))
            return "Error while generating alt!";
        if(response.contains("email")) {
            JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
            return jo.get("email").getAsString() + ":" + jo.get("password").getAsString();
        }
        return "Could not generate alt!";
    }

    public AltGenUser getUser() {
        return user;
    }

    public class AltGenUser {

        private String username;
        private String avatarURL;
        private String activePlan;
        private boolean softLimit;

        public AltGenUser(String username, String avatarURL, String activePlan) {
            this.username = username;
            this.avatarURL = avatarURL;
            this.activePlan = activePlan;
        }

        public String getUsername() {
            return username;
        }

        public String getAvatarURL() {
            return avatarURL;
        }

        public String getActivePlan() {
            return activePlan;
        }

        public boolean isSoftLimit() {
            return softLimit;
        }

        public void setSoftLimit(boolean softLimit) {
            this.softLimit = softLimit;
        }

    }

}
