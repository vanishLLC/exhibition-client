

package exhibition.gui.generators.handlers.altening.stupidaltserviceshit;

import exhibition.management.notifications.usernotification.Notifications;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class AltService {

    private final ReflectionUtility userAuthentication = new ReflectionUtility("com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication");
    private final ReflectionUtility minecraftSession = new ReflectionUtility("com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService");
    private EnumAltService currentService;

    public void switchService(EnumAltService enumAltService) throws NoSuchFieldException, IllegalAccessException {

        if (currentService == enumAltService)
            return;
        reflectionFields(enumAltService.hostname, enumAltService.serviceName);

        currentService = enumAltService;
    }

    public boolean switchService() {
        try {
            if (currentService == EnumAltService.MOJANG) {
                switchService(EnumAltService.THEALTENING);
            } else {
                switchService(EnumAltService.MOJANG);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void reflectionFields(String authServer, String serviceName) throws NoSuchFieldException, IllegalAccessException {
        final HashMap<String, URL> userAuthenticationModifies = new HashMap();
        userAuthenticationModifies.put("ROUTE_AUTHENTICATE", constantURL("https://authserver." + authServer + ".com/authenticate"));
        userAuthenticationModifies.put("ROUTE_INVALIDATE", constantURL("https://authserver" + authServer + "com/invalidate"));
        userAuthenticationModifies.put("ROUTE_REFRESH", constantURL("https://authserver." + authServer + ".com/refresh"));
        userAuthenticationModifies.put("ROUTE_VALIDATE", constantURL("https://authserver." + authServer + ".com/validate"));
        userAuthenticationModifies.put("ROUTE_SIGNOUT", constantURL("https://authserver." + authServer + ".com/signout"));

        userAuthenticationModifies.forEach((key, value) -> {
            try {
                userAuthentication.setStaticField(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        userAuthentication.setStaticField("BASE_URL", "https://authserver." + authServer + ".com/");
        minecraftSession.setStaticField("BASE_URL", "https://sessionserver." + authServer + ".com/session/minecraft/");
        minecraftSession.setStaticField("JOIN_URL", constantURL("https://sessionserver." + authServer + ".com/session/minecraft/join"));
        minecraftSession.setStaticField("CHECK_URL", constantURL("https://sessionserver." + authServer + ".com/session/minecraft/hasJoined"));
        minecraftSession.setStaticField("WHITELISTED_DOMAINS", new String[]{".minecraft.net", ".mojang.com", ".thealtening.com"});

        Notifications.getManager().post("Alt Service Switched", "You are now using the " + serviceName + " auth servers!", 2000);
    }

    private URL constantURL(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException ex) {
            throw new Error("Couldn't create constant for " + url, ex);
        }
    }

    public EnumAltService getCurrentService() {
        if (currentService == null) currentService = EnumAltService.MOJANG;

        return currentService;
    }

    public boolean isVanilla() {
        return getCurrentService() == EnumAltService.MOJANG;
    }

    public static class EnumAltService {

        static EnumAltService MOJANG = new EnumAltService("mojang", "Mojang");
        public static EnumAltService THEALTENING = new EnumAltService("thealtening", "TheAltening");
        String hostname;
        String serviceName;

        EnumAltService(String hostname, String serviceName) {
            this.hostname = hostname;
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
