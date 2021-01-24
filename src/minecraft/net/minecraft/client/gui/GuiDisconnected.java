package net.minecraft.client.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import exhibition.Client;
import exhibition.gui.altmanager.*;
import exhibition.gui.generators.handlers.altening.AlteningGenHandler;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.AltService;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.impl.other.BanStats;
import exhibition.module.impl.other.StreamerMode;
import exhibition.util.IPUtil;
import exhibition.util.security.SilentSnitch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;

public class GuiDisconnected extends GuiScreen {
    private String reason;
    private IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;

    private long timeDifference;

    public GuiDisconnected(GuiScreen p_i45020_1_, String p_i45020_2_, IChatComponent p_i45020_3_) {
        this.parentScreen = p_i45020_1_;
        this.reason = I18n.format(p_i45020_2_);
        this.message = p_i45020_3_;
        boolean changed = false;

        if (Client.loginTime != -1) {
            this.timeDifference = (System.currentTimeMillis() - Client.loginTime);
            Client.loginTime = -1;
        } else {
            this.timeDifference = -1;
        }

        String unformatted = this.message.getUnformattedText();

        String playTime = getTimeLength(timeDifference);
        String banLength = "Permanent";
        String banReason = unformatted.contains("WATCHDOG") ? "Watchdog" :
                (unformatted.toLowerCase().contains("compromised") || unformatted.toLowerCase().contains("alert")) ? "Security Alert" : "Staff Ban";
        String username = Client.getAuthUser().getDecryptedUsername();

        if (unformatted.split("\n")[0].contains("temporarily banned for")) {
            for (Alt alt : AltManager.registry)
                if ((alt.getMask() != null && alt.getMask().equals(Minecraft.getMinecraft().session.getUsername())) || alt.getUsername().equals(Minecraft.getMinecraft().session.getUsername())) {
                    String parseDate = unformatted.split("\n")[0].replace("\n", "");
                    long timeToBeAdded = 0;
                    String timeString = parseDate.substring(31).replace(" from this server!", "");
                    String[] timeValues = parseDate.substring(31).replace(" from this server!", "").split(" ");
                    banLength = timeString.replace(" from this server!", "");

                    for (String timeValue : timeValues) {
                        if (timeValue.length() == 0)
                            continue;

                        String format = timeValue.substring(timeValue.length() - 1);
                        long aLong = Long.parseLong(timeValue.substring(0, timeValue.length() - 1));
                        switch (format) {
                            case "d":
                                timeToBeAdded += (aLong * 86_400_000L);
                                break;
                            case "h":
                                timeToBeAdded += (aLong * 3_600_000L);
                                break;
                            case "m":
                                timeToBeAdded += (aLong * 60_000L);
                                break;
                            case "s":
                                timeToBeAdded += (aLong * 1_000L);
                                break;
                        }
                    }
                    long unbanDate = System.currentTimeMillis() + timeToBeAdded;
                    alt.setUnbanDate(unbanDate);
                    alt.setStatus(Alt.Status.TempBan);
                    Notifications.getManager().post("Temp Banned Account", alt.getMask() + " has been marked \2476Temp Banned\247r for " + TimeUnit.MILLISECONDS.toDays(timeToBeAdded) + " days!", Notifications.Type.NOTIFY);
                    changed = true;
                    break;
                }

        } else if (unformatted.split("\n")[0].contains("permanently banned from this server")) {
            for (Alt alt : AltManager.registry)
                if (alt.getMask().equals(Minecraft.getMinecraft().session.getUsername()) || alt.getUsername().equals(Minecraft.getMinecraft().session.getUsername())) {
                    alt.setStatus(Alt.Status.Banned);
                    Notifications.getManager().post("Banned Account", alt.getMask() + " has been marked \247cBanned\247r!", Notifications.Type.WARNING);
                    changed = true;
                    break;
                }
        }
        if (changed)
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (unformatted.contains("bann")) {
            if (timeDifference >= 10000L) // Don't report if < 10 Seconds
                new SilentSnitch.BanReport(playTime, banReason, banLength, username).start();
            new Thread(IPUtil::setIPBanned).start();
        }
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.field_175353_i = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu")));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 25, 100, 20, "Reconnect"));
        this.buttonList.add(new GuiButton(2, this.width / 2, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 50, 100, 20, "Delete Alt"));
        this.buttonList.add(new GuiButton(3, this.width / 2, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 25, 100, 20, "Random Alt"));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 25, 100, 20, "Random Unbanned"));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, 100, 20, "Check IP"));

        if (!Client.altService.isVanilla()) {
            this.buttonList.add(new GuiButton(8, this.width / 2 - 200, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 25, 100, 20, "Generate Alt"));
        }

        if (!this.message.getUnformattedText().split("\n")[0].contains("permanently banned from this server")) {
            this.buttonList.add(new GuiButton(5, this.width / 2 + 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 50, 100, 20, "Set Banned"));
        }
        this.buttonList.add(new GuiButton(7, this.width / 2 - 200, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, 100, 20, "Alt Manager"));

        if (Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING) {
            this.buttonList.add(new GuiButton(5, this.width / 2 + 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 50, 100, 20, "Set Banned"));
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
        } else if (button.id == 1) {
            try {
                GuiMultiplayer gui = (GuiMultiplayer) parentScreen;
                gui.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (button.id == 2) {
            for (Alt alt : AltManager.registry) {
                if (alt.getUsername().equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername()) || alt.getMask().equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
                    Notifications.getManager().post("Removed Alt", alt.getUsername() + " has been removed from manager!", Notifications.Type.WARNING);
                    AltManager.registry.remove(alt);
                    break;
                }
            }
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (Exception ignored) {
            }
        } else if (button.id == 3) {
            final Random random = new Random();
            if (AltManager.registry.isEmpty())
                return;
            List<Alt> unbanned = AltManager.registry.stream().filter(Alt::isValid).collect(Collectors.toList());
            final Alt randomAlt = unbanned.get(random.nextInt(unbanned.size()));
            try {
                if (!randomAlt.isValid()) {
                    if (!Client.altService.switchService()) {
                        Notifications.getManager().post("ERROR", "Error switching auth service!", Notifications.Type.WARNING);
                        return;
                    }
                    if (Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING)
                        Client.sslVerification.verify();
                    else
                        Client.sslVerification.revertChanges();
                    Notifications.getManager().post("Swapped Service", "Logging in with " + Client.altService.getCurrentService().getServiceName(), Notifications.Type.OKAY);
                }
                (new AltLoginThread(randomAlt)).start();
            } catch (Exception ignored) {

            }
        } else if (button.id == 4 && !AltManager.registry.isEmpty()) {
            final List<Alt> registry = AltManager.registry;
            final Random random = new Random();
            List<Alt> unbanned = registry.stream().filter(Alt::isUnbanned).filter(Alt::isValid).filter(o -> !o.isFavorite()).collect(Collectors.toList());
            if (unbanned.isEmpty()) {
                Notifications.getManager().post("No Unbanned Alts", "Looks like you're all out of alts!", Notifications.Type.NOTIFY);
                return;
            }
            final Alt randomAlt = unbanned.get(random.nextInt(unbanned.size()));
            if (!randomAlt.isValid()) {
                if (!Client.altService.switchService()) {
                    Notifications.getManager().post("ERROR", "Error switching auth service!", Notifications.Type.WARNING);
                    return;
                }
                if (Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING)
                    Client.sslVerification.verify();
                else
                    Client.sslVerification.revertChanges();
            }
            try {
                (new AltLoginThread(randomAlt)).start();
            } catch (Exception ignored) {

            }
        } else if (button.id == 5) {
            for (Alt alt : AltManager.registry) {
                if (alt.getUsername().equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername()) || alt.getMask().equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
                    alt.setStatus(Alt.Status.Banned);
                    Notifications.getManager().post("Banned Account", alt.getMask() + " has been marked \247cBanned\247r!", Notifications.Type.NOTIFY);
                    break;
                }
            }
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (Exception ignored) {
            }
        } else if (button.id == 6) {
            new Thread(IPUtil::checkIP).start();
        } else if (button.id == 7) {
            this.mc.displayGuiScreen(new GuiAltManager(this));
        } else if (button.id == 8) {
            new Thread(() -> {
                AlteningGenHandler handler = Client.alteningGenHandler;
                String key = handler.getAPIKey();
                if (key == null || key.equals("") || !key.startsWith("api-") || key.length() != 18) {
                    Notifications.getManager().post("Invalid Key", "Please set your Altening key in the Alt Manager.", 1000, Notifications.Type.WARNING);
                    return;
                }
                if (handler.getUser() == null) {
                    handler.setGenUser(handler.checkUserLicense());
                }

                if (handler.getUser().isLimited()) {
                    Notifications.getManager().post("Error Generating Alt", "You have reached your daily limit!", 1000, Notifications.Type.WARNING);
                    return;
                }
                if (handler.getUser().canGenAlts()) {
                    AlteningGenHandler.AlteningGenAlt currentAlt = handler.getAltLogin();
                    if (currentAlt == null) {
                        Notifications.getManager().post("Error Generating Alt", "Could not generate alt!", 1000, Notifications.Type.WARNING);
                    } else {
                        Notifications.getManager().post("Generated Alt", "Username: " + currentAlt.getUsername(), 1000, Notifications.Type.OKAY);
                        (new AltLoginThread(new Alt(currentAlt.getEmail(), "password", currentAlt.getUsername(), Alt.Status.Working))).start();
                    }
                } else
                    Notifications.getManager().post("Error Generating Alt", "You do not have a subscription!", 1000, Notifications.Type.WARNING);
            }).start();
        }

    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.reason, this.width / 2, this.height / 2 - this.field_175353_i / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int var4 = this.height / 2 - this.field_175353_i / 2;

        if (this.multilineMessage != null) {
            for (Iterator var5 = this.multilineMessage.iterator(); var5.hasNext(); var4 += this.fontRendererObj.FONT_HEIGHT) {
                String var6 = (String) var5.next();
                this.drawCenteredString(this.fontRendererObj, var6, this.width / 2, var4, 16777215);
            }
        }

        StringBuilder currentAlt = new StringBuilder(Minecraft.getMinecraft().session.getUsername());

        for (Alt alt : AltManager.registry) {
            if (alt.getMask().contentEquals(currentAlt)) {
                if (alt.getStatus() == Alt.Status.Banned) {
                    currentAlt.insert(0, "\247c");
                }
                if (alt.getStatus() == Alt.Status.TempBan) {
                    currentAlt.insert(0, "\2476");
                }
            }
        }

        String str = "Current Alt: " + currentAlt;
        fontRendererObj.drawStringWithShadow(str, this.width / 2F - fontRendererObj.getStringWidth(str) - 5, this.height / 2F + this.field_175353_i / 2F + this.fontRendererObj.FONT_HEIGHT + 56, -1);

        if (timeDifference != -1 && this.message.getUnformattedText().contains("ban")) {
            String timeDiff = "Account lasted " + getTimeLength(timeDifference);
            fontRendererObj.drawStringWithShadow(timeDiff, width / 2D - fontRendererObj.getStringWidth(timeDiff) / 2D, 12, -1);
        }

        BanStats banStats = Client.getModuleManager().getCast(BanStats.class);

        if(banStats.isEnabled()) {
            String s = "Bans since connect: " + banStats.bansSinceConnect;
            fontRendererObj.drawStringWithShadow(s, width / 2D - fontRendererObj.getStringWidth(s) / 2D, 22, -1);
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String getTimeLength(long timeDifference) {
        long seconds = (timeDifference / 1000) % 60;
        long minutes = (timeDifference / 60000) % 60;
        long hours = (timeDifference / 3600000) % 24;
        long days = timeDifference / 86400000;
        StringBuilder stringBuilder = new StringBuilder();
        if (days > 0) stringBuilder.append(days).append("d ");
        if (hours > 0) stringBuilder.append(hours).append("h ");
        if (minutes > 0) stringBuilder.append(minutes).append("m ");
        if (seconds >= 0) stringBuilder.append(seconds).append("s");

        return stringBuilder.toString();
    }

}
