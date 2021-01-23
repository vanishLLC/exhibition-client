package exhibition.gui.altmanager;

import exhibition.Client;
import exhibition.gui.generators.gui.AlteningGeneratorGUI;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.AltService;
import exhibition.gui.screen.PanoramaScreen;
import exhibition.gui.screen.component.GuiMenuButton;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.impl.other.StreamerMode;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GuiAltManager extends PanoramaScreen {
    private GuiButton login;
    private GuiButton remove;
    private GuiButton rename;
    private AltLoginThread loginThread;
    private int offset;
    public Alt selectedAlt;
    private String status;
    private boolean isDragging;
    private GuiScreen parentScreen;

    public GuiAltManager(GuiScreen parentScreen) {
        this.selectedAlt = null;
        this.status = EnumChatFormatting.GRAY + "Idle...";
        this.isDragging = false;
        this.parentScreen = parentScreen;
    }

    public void actionPerformed(final GuiButton button) {
        switch (button.id) {
            case 14: {
                if (AltManager.registry.isEmpty())
                    return;
                final List<Alt> registry = AltManager.registry;
                List<Alt> banned = registry.stream().filter(alt -> alt.getStatus() == Alt.Status.NotWorking).collect(Collectors.toList());
                banned.forEach(alt -> AltManager.registry.remove(alt));
                this.status = "Removed \247c" + banned.size() + "\247f NW alts.";
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (Exception ignored) {
                }
                break;
            }
            case 15: {
                if (AltManager.registry.isEmpty())
                    return;
                final List<Alt> registry = AltManager.registry;
                List<Alt> banned = registry.stream().filter(alt -> alt.getStatus() == Alt.Status.Banned).collect(Collectors.toList());
                registry.stream().filter(alt -> alt.getUnbanDate() > System.currentTimeMillis() && TimeUnit.MILLISECONDS.toDays((alt.getUnbanDate() - System.currentTimeMillis())) > 180).forEach(banned::add);
                banned.forEach(alt -> AltManager.registry.remove(alt));
                this.status = "Removed \247c" + banned.size() + "\247f Banned alts.";
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (Exception ignored) {
                }
                break;
            }
            case 13: {
                // Swap alt service
                if (!Client.altService.switchService()) {
                    Notifications.getManager().post("ERROR", "Error switching auth service!", Notifications.Type.WARNING);
                }
                if (Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING)
                    Client.sslVerification.verify();
                else
                    Client.sslVerification.revertChanges();
                button.displayString = Client.altService.getCurrentService().getServiceName();
                break;
            }
            case 12: {
                mc.displayGuiScreen(new AlteningGeneratorGUI(this));
                break;
            }
            case 1: {
                if (!selectedAlt.isValid()) {
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
                (this.loginThread = new AltLoginThread(selectedAlt)).start();
                break;
            }
            case 11: {
                if (AltManager.registry.isEmpty())
                    break;
                final List<Alt> registry = AltManager.registry;
                final Random random = new Random();
                List<Alt> unbanned = registry.stream().filter(Alt::isUnbanned).filter(Alt::isValid).filter(o -> !o.isFavorite()).collect(Collectors.toList());
                if (unbanned.isEmpty())
                    return;
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
                    (this.loginThread = new AltLoginThread(randomAlt)).start();
                } catch (Exception ignored) {
                    this.status = "\247cError with random alt";
                }
                break;
            }
            case 2: {
                if (this.loginThread != null) {
                    this.loginThread = null;
                }
                if (this.selectedAlt == null)
                    return;
                if (AltManager.registry.remove(this.selectedAlt))
                    this.status = "\247aRemoved.";
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (Exception ignored) {
                }
                this.selectedAlt = null;
                break;
            }
            case 3: {
                this.mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            }
            case 4: {
                this.mc.displayGuiScreen(new GuiAltLogin(this));
                break;
            }
            case 5: {
                if (AltManager.registry.isEmpty())
                    break;
                final List<Alt> registry = AltManager.registry;
                final Random random = new Random();
                final Alt randomAlt = registry.get(random.nextInt(AltManager.registry.size()));
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
                (this.loginThread = new AltLoginThread(randomAlt)).start();
                break;
            }
            case 6: {
                this.mc.displayGuiScreen(new GuiRenameAlt(this));
                break;
            }
            case 7: {
                this.mc.displayGuiScreen(parentScreen);
                break;
            }
            case 8: {
                try {
                    AltManager.registry.clear();
                    Client.getFileManager().getFile(Alts.class).loadFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.status = "\247bReloaded!";
                break;
            }
            case 9: {
                if (AltManager.lastAlt != null) {
                    Notifications.getManager().post("Logging In!", "Logging into last alt (" + AltManager.lastAlt.getMask() + ")", 2000, Notifications.Type.INFO);
                    if (!AltManager.lastAlt.isValid()) {
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
                    (this.loginThread = new AltLoginThread(AltManager.lastAlt)).start();
                }
                break;
            }
            case 10: {
                switch (sorting) {
                    case Chronological:
                        sorting = Sorting.Status;
                        break;
                    case Status:
                        sorting = Sorting.Name;
                        break;
                    case Name:
                        sorting = Sorting.TheAltening;
                        break;
                    case TheAltening:
                        sorting = Sorting.Chronological;
                        break;
                }
                sortButton.displayString = sorting.toString();
                break;
            }
            case 16: {
                try (PrintWriter alts = new PrintWriter(new FileWriter(new File(Client.getDataDir().getAbsolutePath() + File.separator + "AltExport.txt")))) {
                    for (final Alt alt : AltManager.registry) {
                        if (alt.getStatus() == Alt.Status.Banned || alt.getStatus() == Alt.Status.TempBan || alt.isAltening())
                            continue;
                        alts.println(alt.getUsername() + ":" + alt.getPassword());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 4206969: {
                if (this.selectedAlt != null) {
                    this.selectedAlt.setFavorite(!this.selectedAlt.isFavorite());
                    this.status = this.selectedAlt.isFavorite() ? "\2476Favorited Alt." : "\247cUnfavorited Alt.";
                    try {
                        Client.getFileManager().getFile(Alts.class).saveFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void drawScreen(final int par1, final int par2, final float par3) {
        renderSkybox(par1, par2, par3);

        List<Alt> alts = getAlts();
        int maximumInRegion = ((34 + this.height - 51) / 26) - 3;

        float offsetValueThing = (offset / 26F);
        float totalHeight = (alts.size() * 26F);

        int maxOffset = (totalHeight > (maximumInRegion * 26) ? (alts.size() * 26) - (maximumInRegion * 26) : 0);

        if (Mouse.hasWheel()) {
            final int wheel = Mouse.getDWheel();
            if (wheel < 0 && offset < maxOffset) {
                this.offset += 26;
                if (this.offset < 0) {
                    this.offset = 0;
                }
            } else if (wheel > 0) {
                this.offset -= 26;
                if (this.offset < 0) {
                    this.offset = 0;
                }
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

        String str = currentAlt.toString();


        this.drawString(this.fontRendererObj, str, 10, 10, 14540253);
        final FontRenderer fontRendererObj = this.fontRendererObj;
        String sb = "Alts: " + AltManager.registry.size() +
                " | Ban: " + AltManager.registry.stream().filter(o -> o.getStatus().equals(Alt.Status.Banned)).count() +
                " | Temp: " + AltManager.registry.stream().filter(o -> o.getStatus().equals(Alt.Status.TempBan)).count() +
                " | NW: " + AltManager.registry.stream().filter(o -> o.getStatus().equals(Alt.Status.NotWorking)).count() + " | UB: " + AltManager.registry.stream().filter(Alt::isUnbanned).count();
        this.drawCenteredString(fontRendererObj, sb, this.width / 2, 10, -1);
        this.drawCenteredString(this.fontRendererObj, (this.loginThread == null) ? this.status : this.loginThread.getStatus(), this.width / 2, 20, -1);
        RenderingUtil.rectangleBordered(50.0f, 33.0f, this.width - 50, this.height - 51, 1.0f, Colors.getColor(50, 60), Colors.getColor(1, 125));
        GL11.glPushMatrix();
        this.prepareScissorBox(0.0f, 33.0f, this.width, this.height - 50);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int y = 38;
        boolean modified = false;

        for (final Alt alt : alts) {
            if (alt.getStatus() == Alt.Status.TempBan && alt.getUnbanDate() < System.currentTimeMillis()) {
                alt.setUnbanDate(-1);
                alt.setStatus(Alt.Status.Working);
                Notifications.getManager().post("Alt Unbanned!", alt.getMask() + "'s ban has expired!", Notifications.Type.INFO);
                modified = true;
            }
            if (alt.getUsername().contains("@alt.com") && (alt.getUnbanDate() < System.currentTimeMillis()) && alt.getStatus() != Alt.Status.NotWorking) {
                Notifications.getManager().post("Alt Expired!", alt.getUsername() + "'s token has expired!", Notifications.Type.INFO);
                modified = true;
                alt.setStatus(Alt.Status.NotWorking);
            }

            if (isAltInArea(y)) {
                String name;

                if (alt.getMask().equals("")) {
                    name = alt.getUsername();
                } else {
                    name = alt.getMask();
                }
                if (name.equalsIgnoreCase(this.mc.session.getUsername()))
                    name = "\247n" + name;
                if (alt == AltManager.lastAlt)
                    name = "* " + name;
                String prefix = alt.getStatus().equals(Alt.Status.Banned) ? "\247c" : alt.getStatus().equals(Alt.Status.NotWorking) ? "\247m" : "";
                if (alt.getUsername().contains("@alt.com")) {
                    prefix += " TheAltening: ";
                }
                name = prefix + name + "\247r \2477| " + ((alt.getStatus().equals(Alt.Status.TempBan) || !alt.isGenerated()) ? alt.getStatus().toFormatted() + " - " + getUnbanTime(alt) : alt.getStatus().toFormatted());
                String pass;
                if (alt.getPassword().equals("")) {
                    pass = "\247cCracked";
                } else {
                    pass = alt.getPassword().replaceAll(".", "*");
                }
                if (alt == this.selectedAlt) {
                    boolean mouseOverAlt = par1 >= 78 && par2 >= (y - this.offset) - 4 && par1 <= this.width - 77 && par2 <= (y - this.offset) + 20 && par1 >= 0 && par2 >= 33 && par1 <= this.width && par2 <= this.height - 50;
                    if (mouseOverAlt && Mouse.isButtonDown(0)) {
                        RenderingUtil.rectangleBordered(52.0f + 26, y - this.offset - 4, this.width - 77, y - this.offset + 20, 1.0f, Colors.getColor(50, 60), -2142943931);
                    } else if (mouseOverAlt) {
                        RenderingUtil.rectangleBordered(52.0f + 26, y - this.offset - 4, this.width - 77, y - this.offset + 20, 1.0f, Colors.getColor(50, 60), -2142088622);
                    } else {
                        RenderingUtil.rectangleBordered(52.0f + 26, y - this.offset - 4, this.width - 77, y - this.offset + 20, 1.0f, Colors.getColor(50, 60), -2144259791);
                    }

                    {
                        boolean hovering = par1 >= this.width - 76 && par1 <= this.width - 52 && par2 >= y - this.offset - 4 && par2 <= y - this.offset + 20;
                        RenderingUtil.rectangleBordered(this.width - 76, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1, Colors.getColor(50, 60), hovering ? -2142088622 : -2144259791);
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(this.width - 74 + 10, y - this.offset, 0);
                        GlStateManager.scale(0.5, 0.5, 0.5);
                        mc.fontRendererObj.drawStringWithShadow("Change", -fontRendererObj.getStringWidth("Change") / 2D, 0, Colors.getColor(230, 255));
                        mc.fontRendererObj.drawStringWithShadow("Account", 0 - fontRendererObj.getStringWidth("Account") / 2D, 12, Colors.getColor(230, 255));
                        mc.fontRendererObj.drawStringWithShadow("Status", 0 - fontRendererObj.getStringWidth("Status") / 2D, 24, Colors.getColor(230, 255));
                        GlStateManager.popMatrix();
                    }

                    {
                        boolean hovering = par1 >= 52 && par1 <= 52 + 25 && par2 >= y - this.offset - 4 && par2 <= y - this.offset + 20;
                        RenderingUtil.rectangleBordered(52, y - this.offset - 4, 52 + 25, y - this.offset + 20, 1, Colors.getColor(50, 60), hovering ? -2142088622 : -2144259791);
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(52 + 25 / 2D, y - this.offset, 0);
                        GlStateManager.scale(0.5, 0.5, 0.5);
                        mc.fontRendererObj.drawStringWithShadow("Favorite", -fontRendererObj.getStringWidth("Favorite") / 2D, 6, Colors.getColor(230, 255));
                        mc.fontRendererObj.drawStringWithShadow("Account", 0 - fontRendererObj.getStringWidth("Account") / 2D, 18, Colors.getColor(230, 255));
                        GlStateManager.popMatrix();
                    }
                } else if (this.isMouseOverAlt(par1, par2, y - this.offset) && Mouse.isButtonDown(0)) {
                    RenderingUtil.rectangleBordered(52.0f, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1.0f, -Colors.getColor(50, 60), -2146101995);
                } else if (this.isMouseOverAlt(par1, par2, y - this.offset)) {
                    RenderingUtil.rectangleBordered(52.0f, y - this.offset - 4, this.width - 52, y - this.offset + 20, 1.0f, Colors.getColor(50, 60), -2145180893);
                }
                String numberP = (alt.isFavorite() ? "\2476❤ \247f" : "\2477" + (AltManager.registry.indexOf(alt) + 1) + ". \247f");
                this.drawCenteredString(this.fontRendererObj, numberP + name, this.width / 2, y - this.offset, -1);
                this.drawCenteredString(this.fontRendererObj, (alt.getStatus().equals(Alt.Status.NotWorking) ? "\247m" : "") + pass, this.width / 2, y - this.offset + 10, Colors.getColor(110));
                y += 26;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();

        float sliderBarLength = ((this.height - 51 - 34));

        if (totalHeight > (maximumInRegion * 26)) {
            boolean isWithingRegion = par1 >= this.width - 50.5 && par1 <= this.width - 45.5 && par2 >= 34 && par2 <= this.height - 51;

            RenderingUtil.rectangleBordered(this.width - 50, 33, this.width - 46, this.height - 51, 0.5, Colors.getColor(50, 60), Colors.getColor(1, 125));

            float relativeScale = sliderBarLength * ((maximumInRegion * 26) / totalHeight);
            float relativeOffset = (sliderBarLength) * (offsetValueThing / alts.size());
            float yPos = 34 + relativeOffset;

            if (isDragging && !Mouse.isButtonDown(0)) {
                isDragging = false;
            }

            if (isWithingRegion) {
                if (Mouse.isButtonDown(0)) {
                    this.isDragging = true;
                }
            }
            if (isDragging) {
                float positionRelativeToOffset = (par2 - 34 - relativeScale / 2) / (sliderBarLength);
                this.offset = ((int) (totalHeight * positionRelativeToOffset) / 26) * 26;
            }
            boolean hovering = par1 >= this.width - 49 && par1 <= this.width - 47 && par2 >= yPos && par2 <= yPos + relativeScale;

            RenderingUtil.rectangle(this.width - 49, yPos, this.width - 47, yPos + relativeScale - 1, isDragging ? -1 : hovering ? Colors.getColor(255, 175) : Colors.getColor(255, 125));
        }


        if (modified) {
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.drawScreen(par1, par2, par3);

        if (this.selectedAlt == null) {
            this.login.enabled = false;
            this.remove.enabled = false;
            this.rename.enabled = false;
        } else {
            this.login.enabled = true;
            this.remove.enabled = true;
            this.rename.enabled = true;
        }
        if (Keyboard.isKeyDown(200)) {
            this.offset -= 26;
        } else if (Keyboard.isKeyDown(208) && offset < maxOffset) {
            this.offset += 26;
        }
        if (this.offset < 0) {
            this.offset = 0;
        }
        if (offset > maxOffset) {
            offset = maxOffset;
        }

        this.seatchField.drawTextBox();
        if (seatchField.getText().isEmpty() && !seatchField.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Search Alt", this.width / 2 + 120, this.height - 18, Colors.getColor(180));
        }
    }

    private String getUnbanTime(Alt alt) {
        long difference = alt.getUnbanDate() - System.currentTimeMillis();
        long seconds = (difference / 1000) % 60;
        long minutes = (difference / 60000) % 60;
        long hours = (difference / 3600000) % 24;
        long days = difference / 86400000;
        StringBuilder stringBuilder = new StringBuilder();
        if (days > 0) stringBuilder.append(days).append("d ");
        if (hours > 0) stringBuilder.append(hours).append("h ");
        if (minutes > 0) stringBuilder.append(minutes).append("m ");
        if (seconds >= 0) stringBuilder.append(seconds).append("s");

        if (alt.getUnbanDate() < System.currentTimeMillis() && alt.isAltening())
            stringBuilder.append("Expired");

        return stringBuilder.toString();
    }

    private GuiTextField seatchField;

    private static Sorting sorting = Sorting.Chronological;

    public enum Sorting {
        Chronological("Chronological"), Name("Name"), Status("Status"), TheAltening("TheAltening");

        Sorting(String string) {
            this.string = string;
        }

        private final String string;

        public String toString() {
            return this.string;
        }

    }

    private GuiMenuButton sortButton;

    @Override
    public void initGui() {
        try {
            AltManager.registry.clear();
            Client.getFileManager().getFile(Alts.class).loadFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seatchField = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 + 116, this.height - 22, 72, 16);
        this.buttonList.add(new GuiMenuButton(4206969, 2, 30, 47, 20, "Favorite Current"));

        this.buttonList.add(new GuiMenuButton(12, this.width - 43, 30, 40, 20, "Altening-Gen"));
        this.buttonList.add(new GuiMenuButton(13, this.width - 43, 55, 40, 20, Client.altService.getCurrentService().getServiceName()));
        this.buttonList.add(new GuiMenuButton(14, this.width - 43, 80, 40, 20, "Remove NW"));
        this.buttonList.add(new GuiMenuButton(15, this.width - 43, 105, 40, 20, "Remove Ban'd"));
        this.buttonList.add(new GuiMenuButton(16, this.width - 43, 130, 40, 20, "Export Alts"));


        this.buttonList.add(this.sortButton = new GuiMenuButton(10, this.width - 125, 5, 75, 20, sorting.toString()));


        this.buttonList.add(this.login = new GuiMenuButton(1, this.width / 2 - 122, this.height - 48, 50, 20, "Login"));
        this.buttonList.add(this.remove = new GuiMenuButton(2, this.width / 2 - 40, this.height - 24, 77, 20, "Remove"));
        this.buttonList.add(new GuiMenuButton(3, this.width / 2 + 6, this.height - 48, 30, 20, "Add Alt"));
        GuiMenuButton button = new GuiMenuButton(9, this.width / 2 + 4 + 35, this.height - 48, 50, 20, "Last Alt");
        this.buttonList.add(new GuiMenuButton(11, this.width / 2 + 4 + 88, this.height - 48, 99, 20, "Random Unbanned Alt"));

        button.enabled = AltManager.lastAlt != null;
        this.buttonList.add(button);
        this.buttonList.add(new GuiMenuButton(4, this.width / 2 - 68, this.height - 48, 70, 20, "Direct Login"));
        this.buttonList.add(new GuiMenuButton(5, this.width / 2 - 122, this.height - 24, 78, 20, "Random Alt"));
        this.buttonList.add(this.rename = new GuiMenuButton(6, this.width / 2 + 39, this.height - 24, 73, 20, "Edit Alt"));
        this.buttonList.add(new GuiMenuButton(7, this.width / 2 - 190, this.height - 24, 65, 20, "Back"));
        this.buttonList.add(new GuiMenuButton(8, this.width / 2 - 190, this.height - 48, 65, 20, "Reload"));

        this.login.enabled = false;
        this.remove.enabled = false;
        this.rename.enabled = false;
        super.initGui();
    }

    @Override
    protected void keyTyped(final char par1, final int par2) {
        seatchField.textboxKeyTyped(par1, par2);
        if ((par1 == '\t' || par1 == '\r') && seatchField.isFocused()) {
            offset = 0;
            seatchField.setFocused(!seatchField.isFocused());
        }
        try {
            super.keyTyped(par1, par2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isAltInArea(final int y) {
        return y - this.offset <= this.height - 50;
    }

    private boolean isMouseOverAlt(final int x, final int y, final int y1) {
        return x >= 52 && y >= y1 - 4 && x <= this.width - 52 && y <= y1 + 20 && x >= 0 && y >= 33 && x <= this.width && y <= this.height - 50;
    }

    @Override
    protected void mouseClicked(final int par1, final int par2, final int par3) {
        seatchField.mouseClicked(par1, par2, par3);
        if (this.offset < 0) {
            this.offset = 0;
        }
        int y = 38 - this.offset;
        for (final Alt alt : getAlts()) {
            boolean hovering = par1 >= this.width - 76 && par1 <= this.width - 52 && par2 >= y - 4 && par2 <= y + 20;
            if (hovering && alt == this.selectedAlt) {
                switch (alt.getStatus()) {
                    case Unchecked:
                        alt.setUnbanDate(-1);
                        alt.setStatus(Alt.Status.Working);
                        break;
                    case Working:
                        alt.setUnbanDate(-1);
                        alt.setStatus(Alt.Status.Banned);
                        break;
                    case Banned:
                        alt.setUnbanDate(-1);
                        alt.setStatus(Alt.Status.NotWorking);
                        break;
                    case NotWorking:
                        alt.setUnbanDate(-1);
                        alt.setStatus(Alt.Status.Unchecked);
                        break;
                    case TempBan:
                        alt.setUnbanDate(-1);
                        alt.setStatus(Alt.Status.Working);
                        break;
                }
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            boolean favorite = par1 >= 52 && par1 <= 52 + 25 && par2 >= y - this.offset - 4 && par2 <= y - this.offset + 20;
            if (favorite && alt == this.selectedAlt) {
                this.selectedAlt.setFavorite(!this.selectedAlt.isFavorite());
                this.status = this.selectedAlt.isFavorite() ? "\2476Favorited Alt." : "\247cUnfavorited Alt.";
                try {
                    Client.getFileManager().getFile(Alts.class).saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (isMouseOverAlt(par1, par2, y)) {
                if (alt == this.selectedAlt) {
                    this.actionPerformed(login);
                    return;
                }
                this.selectedAlt = alt;
                return;
            }
            y += 26;
        }
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Alt> getAlts() {
        List<Alt> altList = new ArrayList<>();
        AltManager.registry.forEach(alt -> {
            if (seatchField.getText().isEmpty() || (alt.getMask().toLowerCase().contains(seatchField.getText().toLowerCase()) || alt.getUsername().toLowerCase().contains(seatchField.getText().toLowerCase())))
                altList.add(alt);
        });
        switch (sorting) {
            case Name: {
                altList.sort(Comparator.comparing(Alt::getMask));
                break;
            }
            case Status: {
                altList.sort(Comparator.comparing(Alt::getStatus));
                break;
            }
            case TheAltening: {
                altList.sort(Comparator.comparing(Alt::isGenerated));
                break;
            }
        }
        altList.sort(Comparator.comparing(o -> !o.isFavorite()));

        return altList;
    }

    private void prepareScissorBox(final float x, final float y, final float x2, final float y2) {
        final ScaledResolution scale = new ScaledResolution(this.mc);
        final int factor = scale.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

}

