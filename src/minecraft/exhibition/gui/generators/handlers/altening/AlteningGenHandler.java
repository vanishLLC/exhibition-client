

/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.generators.handlers.altening;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exhibition.Client;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.util.security.Connection;
import exhibition.util.security.Connector;
import exhibition.util.security.Crypto;
import net.minecraft.util.CryptManager;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AlteningGenHandler {

    private String LICENSEURL = "http://api.thealtening.com/v2/license", GENURL = "http://api.thealtening.com/v2/generate";

    private String APIKEY = null;

    private AlteningGenUser genUser;

    public AlteningGenUser getUser() {
        return genUser;
    }

    public void setGenUser(AlteningGenUser genUser) {
        this.genUser = genUser;
    }

    public AlteningGenHandler() {
        loadAPIKey();
    }

    public boolean setAPIKey(String key) {
        this.APIKEY = key;
        if (checkUserLicense() != null) {
            return true;
        }
        APIKEY = null;
        return false;
    }

    public AlteningGenUser checkUserLicense() {
        if (APIKEY == null || APIKEY.equals(""))
            return null;
        Connection connection = new Connection(LICENSEURL);
        connection.setParameters("key", APIKEY);
        Connector.get(connection);
        JsonObject json = (JsonObject) JsonParser.parseString(connection.getResponse().trim());
        try {
            saveAPIKey();
            String licenseType = json.get("licenseType").isJsonNull() ? "None" : json.get("licenseType").getAsString();

            String expires = json.get("expires").isJsonNull() ? "Expired" : json.get("expires").getAsString();

            return this.genUser = new AlteningGenUser(json.get("username").getAsString(),
                    json.get("hasLicense").getAsBoolean(),
                    licenseType,
                    expires);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AlteningGenAlt getAltLogin() {
        if (!genUser.isLimited)
            if (APIKEY != null && !APIKEY.equals("")) {
                try {
                    Connection connection = new Connection(GENURL);
                    connection.setParameters("key", APIKEY);
                    Connector.get(connection);
                    JsonObject json = (JsonObject) JsonParser.parseString(connection.getResponse().trim());
                    if (json.get("limit").getAsBoolean()) {
                        genUser.setLimited();
                    }

                    return new AlteningGenAlt(json.get("username").getAsString(), json.get("token").getAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        return null;
    }

    private File getFile() {
        return new File(Client.getDataDir().getAbsolutePath() + File.separator + "Other" + File.separator + "Altening.txt");
    }

    public void loadAPIKey() {
        File saveFile = getFile();
        if (saveFile.exists()) {
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(saveFile), StandardCharsets.UTF_8));
                this.APIKEY = Crypto.decrypt(CryptManager.getSecretNew(), in.readLine());
                in.close();
            } catch (Exception ignored) {
            }
        }

    }

    public void saveAPIKey() {
        if (APIKEY == null || APIKEY.equals(""))
            return;

        File saveFile = getFile();
        if (!saveFile.exists()) {
            try {
                if (saveFile.createNewFile()) {
                    System.out.println("API Key created successfully!");
                }
            } catch (Exception e) {
                System.out.println("Error loading API Key! " + e.getMessage());
                return;
            }
        }
        try {
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), StandardCharsets.UTF_8));
            out.write(Crypto.encrypt(CryptManager.getSecretNew(), APIKEY));
            out.close();
        } catch (Exception e) {
            System.out.println("Error writing to API Key file! " + e.getMessage());
        }
    }

    public String getAPIKey() {
        return APIKEY;
    }

    public class AlteningGenUser {

        private String username;
        private boolean hasLicense;
        private String licenseType;
        private String experation;
        private boolean isLimited;

        public AlteningGenUser(String username, boolean hasLicense, String licenseType, String expiration) {
            this.username = username;
            this.hasLicense = hasLicense;
            this.licenseType = licenseType;
            this.experation = expiration;
        }

        public boolean canGenAlts() {
            return hasLicense && !licenseType.equals("None") && !this.isLimited;
        }

        public void setLimited() {
            this.isLimited = true;
        }

        public boolean isLimited() {
            return this.isLimited;
        }
    }


    public class AlteningGenAlt {

        String username;
        String email;

        public AlteningGenAlt(String username, String email) {
            this.username = username;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

    }
}
