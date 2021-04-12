package exhibition.util.security;

import exhibition.Client;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;

public class DiscordUtil {

    private static final Object discordUsername = null;

    private static final Object discordDiscriminator = null;

    private static final Object discordID = null;

    private static long startTime;

    public static void initDiscord() {
        try {

            Class discordRPCClass = Class.forName("net.arikia.dev.drpc.DiscordRPC");

            Class handlerClass = Class.forName("net.arikia.dev.drpc.DiscordEventHandlers");

            Object handlerObject = null;

            Class builderClass = Class.forName("net.arikia.dev.drpc.DiscordEventHandlers$Builder");

            Object builderInstance = handlerObject = builderClass.newInstance();

            builderClass.getDeclaredMethod("setReadyEventHandler", ReadyCallback.class).invoke(handlerObject, new ReadyCallback() {
                @Override
                public void apply(DiscordUser discordUser) {
                    try {
                        Client.isDiscordReady = true;
                        ReflectionUtil.setStaticField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordUsername"), discordUser.username);
                        ReflectionUtil.setStaticField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordDiscriminator"), discordUser.discriminator);
                        ReflectionUtil.setStaticField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordID"), discordUser.userId);
                        startTime = System.currentTimeMillis();
                    } catch (Exception e) {

                    }
                }
            });

            handlerObject = builderClass.getDeclaredMethod("build").invoke(builderInstance);

            discordRPCClass.getMethod("discordInitialize", String.class, handlerClass, boolean.class).invoke(null, "633162444134416413", handlerObject, false);
            discordRPCClass.getMethod("discordRegister", String.class, String.class).invoke(null, "633162444134416413", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getDiscordUsername(Object anyInstance) {
        String bruhString = "";
        try {
            String hashSymbol = "kj09asd31#90fda";
            Object username = ReflectionUtil.getField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordUsername"), anyInstance);
            Object discrim = ReflectionUtil.getField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordDiscriminator"), username);
            return username + hashSymbol.replace("kj09asd31", "").replace("90fda", "") + discrim;
        } catch (Exception e) {

        }
        return bruhString;
    }

    public static Object getDiscordID(Object anyInstance) {
        String bruhString = "";
        try {
            return ReflectionUtil.getField(Class.forName("exhibition.util.security.DiscordUtil").getDeclaredField("discordID"), anyInstance);
        } catch (Exception e) {
        }
        return bruhString;
    }

    public static void setDiscordPresence(String state, String details) {
        if(!Client.isDiscordReady)
            return;

        DiscordRichPresence.Builder discordRichPresence = new DiscordRichPresence.Builder("Status: " + state).setStartTimestamps(Client.joinTime != -1 ? Client.joinTime : startTime).setBigImage("logo", "");

        if (Client.getAuthUser() != null) {
            switch (Client.getAuthUser().userID) {
                case 1: { // Arithmo
                    discordRichPresence.setBigImage("logo", "Rubbish Client").setSmallImage("money", "Money Money Money Money Money Money Money Money Money Money Money Money! (Only cares about Money)");
                    break;
                }
                case 2: { // Dream
                    discordRichPresence.setBigImage("logo", "").setSmallImage("dream", "consider urself cursed.");
                    break;
                }
                case 3: { // Neohack
                    discordRichPresence.setBigImage("logo", "").setSmallImage("neo", "Huh?");
                    break;
                }
                case 5: { // Frog
                    discordRichPresence.setBigImage("frog", "Will you let him in?").setSmallImage("logo", "");
                    break;
                }
                case 46: { // Max
                    discordRichPresence.setBigImage("logo", "").setSmallImage("max", "[Intro] Playboi Carti Jamie Foxx " +
                            "Yo, Pierre, you wanna come out here? " +
                            "Uh " +
                            "[Chorus] " +
                            "In New York I Milly Rock (Rock)");
                    break;
                }
                case 114: { // 3DS
                    discordRichPresence.setBigImage("3ds", "I'm bumboy08's girlfriend >.<").setSmallImage("cop", "im gonna shoot you, you stupid fucking prisoner");
                    break;
                }
                default:
                    break;
            }

        }

        discordRichPresence.setDetails(details);
        DiscordRPC.discordUpdatePresence(discordRichPresence.build());
    }

}
