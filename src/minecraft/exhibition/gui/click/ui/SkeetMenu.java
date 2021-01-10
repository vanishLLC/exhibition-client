package exhibition.gui.click.ui;

import exhibition.Client;
import exhibition.gui.click.ClickGui;
import exhibition.gui.click.components.Button;
import exhibition.gui.click.components.Checkbox;
import exhibition.gui.click.components.*;
import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.GlobalValues;
import exhibition.management.animate.Opacity;
import exhibition.management.animate.Translate;
import exhibition.management.command.Command;
import exhibition.management.command.impl.ColorCommand;
import exhibition.management.font.TTFFontRenderer;
import exhibition.management.keybinding.Keybind;
import exhibition.management.notifications.dev.DevNotifications;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.module.impl.movement.*;
import exhibition.module.impl.render.TargetESP;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.StringConversions;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.*;

/**
 * Created by cool1 on 1/21/2017.
 */
public class SkeetMenu extends UI {

    private final boolean allowMinigames = Boolean.parseBoolean(System.getProperty("NEoBuMASs"));

    public static Opacity opacity = new Opacity(0);
    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void mainConstructor(ClickGui p0) {
    }

    private ResourceLocation texture = new ResourceLocation("textures/skeetchainmail.png");
    private ResourceLocation cursor = new ResourceLocation("textures/cursor.png");


    private Translate bar = new Translate(0, 0);

    @Override
    public void mainPanelDraw(MainPanel panel, int p0, int p1) {
        opacity.interp(panel.opacity, 25);

        RenderingUtil.rectangleBordered(panel.x + panel.dragX - 0.3, panel.y + panel.dragY - 0.3, panel.x + 340 + panel.dragX + 0.5, panel.y + 310 + panel.dragY + 0.3, 0.5, Colors.getColor(0, 0), Colors.getColor(10, (int) opacity.getOpacity()));//60 60 opaccity and 22
        RenderingUtil.rectangleBordered(panel.x + panel.dragX, panel.y + panel.dragY, panel.x + 340 + panel.dragX, panel.y + 310 + panel.dragY, 0.5, Colors.getColor(0, 0), Colors.getColor(60, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2, panel.y + panel.dragY + 2, panel.x + 340 + panel.dragX - 2, panel.y + 310 + panel.dragY - 2, 0.5, Colors.getColor(0, 0), Colors.getColor(60, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 0.6, panel.y + panel.dragY + 0.6, panel.x + 340 + panel.dragX - 0.5, panel.y + 310 + panel.dragY - 0.6, 1.3, Colors.getColor(0, 0), Colors.getColor(40, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2.5, panel.y + panel.dragY + 2.5, panel.x + 340 + panel.dragX - 2.5, panel.y + 310 + panel.dragY - 2.5, 0.5, Colors.getColor(22, (int) opacity.getOpacity()), Colors.getColor(22, (int) opacity.getOpacity()));
        RenderingUtil.drawGradientSideways(panel.x + panel.dragX + 3, panel.y + panel.dragY + 3, panel.x + 178 + panel.dragX - 3, panel.dragY + panel.y + 4, Colors.getColor(55, 177, 218, (int) opacity.getOpacity()), Colors.getColor(204, 77, 198, (int) opacity.getOpacity()));
        RenderingUtil.drawGradientSideways(panel.x + panel.dragX + 175, panel.y + panel.dragY + 3, panel.x + 340 + panel.dragX - 3, panel.dragY + panel.y + 4, Colors.getColor(204, 77, 198, (int) opacity.getOpacity()), Colors.getColor(204, 227, 53, (int) opacity.getOpacity()));

        int i11 = (int) opacity.getOpacity() - 145;
        if (i11 < 0) {
            i11 = 0;
        }
        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + 3.3, panel.x + 340 + panel.dragX - 3, panel.dragY + panel.y + 4, Colors.getColor(0, i11));
        RenderingUtil.drawGradientSideways(-1, -1, -1, -1, Colors.getColor(255, (int) opacity.getOpacity()), Colors.getColor(255, (int) opacity.getOpacity()));

        float y = 15;
        for (int i = 0; i <= panel.typeButton.size(); i++) {
            if (i <= panel.typeButton.size() - 1 && panel.typeButton.get(i).categoryPanel.visible && i > 0) {
                y = 15 + ((i) * 40);
            }
        }
        bar.interpolate(0, y, 0.6F);
        y = bar.getY();

        //Draw texture in ghetto way
        GlStateManager.pushMatrix();
        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + 4F, panel.x + panel.dragX + 40, panel.y + panel.dragY + y, -1);
        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + y + 40, panel.x + panel.dragX + 40, panel.y + panel.dragY + 308, -1);
        Depth.render(GL11.GL_LESS);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.translate(panel.x + panel.dragX + 2.5, panel.dragY + panel.y + 3f, 0);
        drawIcon(1, 1, 0, 0, 340 - 6.5, 310 - 7, 812 / 2F, 688 / 2F);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        Depth.post();
        GlStateManager.popMatrix();
/*

        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 57, panel.y + panel.dragY + 16, panel.x + 390 + panel.dragX, panel.y + 275 + panel.dragY, 0.5, Colors.getColor(46), Colors.getColor(10));
        RenderingUtil.rectangle(panel.x + panel.dragX + 58, panel.y + panel.dragY + 17, panel.x + 390 + panel.dragX - 1, panel.y + 275 + panel.dragY - 1, Colors.getColor(17));
*/


        GlStateManager.pushMatrix();
        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + 4F, panel.x + panel.dragX + 40, panel.y + panel.dragY + y + 1, -1);
        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + y + 40, panel.x + panel.dragX + 40, panel.y + panel.dragY + 307.5, -1);

        Depth.render();
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2, panel.y + panel.dragY + 3, panel.x + panel.dragX + 40, panel.y + panel.dragY + y, 1, Colors.getColor(0, 0), Colors.getColor(0, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2, panel.y + panel.dragY + 3, panel.x + panel.dragX + 40, panel.y + panel.dragY + y, 0.5, Colors.getColor(0, 0), Colors.getColor(48, (int) opacity.getOpacity()));

        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + 4, panel.x + panel.dragX + 39, panel.y + panel.dragY + y - 1, Colors.getColor(12, (int) opacity.getOpacity()));

        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2, panel.y + panel.dragY + y + 40, panel.x + panel.dragX + 40, panel.y + panel.dragY + 308, 1, Colors.getColor(0, 0), Colors.getColor(0, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(panel.x + panel.dragX + 2, panel.y + panel.dragY + y + 40, panel.x + panel.dragX + 40, panel.y + panel.dragY + 308, 0.5, Colors.getColor(0, 0), Colors.getColor(48, (int) opacity.getOpacity()));

        RenderingUtil.rectangle(panel.x + panel.dragX + 3, panel.y + panel.dragY + y + 41, panel.x + panel.dragX + 39, panel.y + panel.dragY + 307.5, Colors.getColor(12, (int) opacity.getOpacity()));
        Depth.post();
        GlStateManager.popMatrix();

        if (opacity.getOpacity() != 0) {
            for (SLButton button : panel.slButtons) {
                button.draw(p0, p1);
            }
            for (CategoryButton button : panel.typeButton) {
                button.draw(p0, p1);
            }
            ScaledResolution rs = new ScaledResolution(mc);
            double twoDscale = (rs.getScaleFactor() / Math.pow(rs.getScaleFactor(), 2.0D)) * 2;
            if (panel.dragging) {
                panel.dragX = p0 - panel.lastDragX;
                panel.dragY = p1 - panel.lastDragY;
            }
            double xBorder = (rs.getScaledWidth() / twoDscale - 392);
            if (panel.dragX > xBorder) {
                panel.dragX = (float) xBorder;
            }
            if (panel.dragX < 2 - 50) {
                panel.dragX = 2 - 50;
            }
            double yBorder = (rs.getScaledHeight() / twoDscale - 362);

            if (panel.dragY > yBorder) {
                panel.dragY = (float) yBorder;
            }
            if (panel.dragY < 2 - 50) {
                panel.dragY = 2 - 50;
            }
        }

        if (panel.opacity == 255 && !GlobalValues.showCursor.getValue() && Mouse.isGrabbed()) {
            GlStateManager.pushMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(cursor);
            GlStateManager.translate(p0, p1, 0);
            ColorObject c = ColorManager.hudColor;
            RenderingUtil.glColor(Colors.getColor(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * opacity.getScale())));
            GlStateManager.scale(0.5, 0.5, 0.5);
            drawIcon(0, 0, 0, 0, 12, 19, 12, 19);
            GL11.glColor4d(1, 1, 1, 1);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.popMatrix();
        }
    }

    private void drawIcon(double x, double y, float u, float v, double width, double height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) x, (double) (y + height), 0.0D).tex((double) (u * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), 0.0D).tex((double) ((u + (float) width) * f), (double) ((v + (float) height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) y, 0.0D).tex((double) ((u + (float) width) * f), (double) (v * f1)).endVertex();
        worldrenderer.pos((double) x, (double) y, 0.0D).tex((double) (u * f), (double) (v * f1)).endVertex();
        tessellator.draw();
    }

    @Override
    public void mainPanelKeyPress(MainPanel panel, int key) {
        boolean bad = false;
        for (CategoryButton button : Client.getClickGui().mainPanel.typeButton) {
            for (TextBox textBox : button.categoryPanel.textBoxes) {
                if (textBox.isTyping || textBox.isFocused)
                    bad = true;
            }
            if (button.categoryPanel.configTextBox != null) {
                ConfigTextBox textBox = button.categoryPanel.configTextBox;
                if (button.categoryPanel.visible) {
                    if (textBox.isTyping || textBox.isFocused)
                        bad = true;
                }
            }
        }
        if (opacity.getOpacity() < 10)
            return;
        if (key == 1) {
            for (CategoryButton buttonb : panel.typeButton) {
                for (Button button : buttonb.categoryPanel.buttons) {
                    if (button.isBinding) {
                        bad = true;
                    }
                }
            }
        }

        if (!bad && ((key == Keyboard.KEY_ESCAPE) || key == Keyboard.KEY_INSERT || key == Keyboard.KEY_DELETE || key == Keyboard.KEY_RSHIFT)) {
            try {
                panel.typeButton.forEach(o -> o.categoryPanel.multiDropdownBoxes.forEach(b -> b.active = false));
                panel.typeButton.forEach(o -> o.categoryPanel.dropdownBoxes.forEach(b -> b.active = false));
                panel.typeButton.forEach(o -> o.categoryPanel.buttons.forEach(b -> b.isBinding = false));
                panel.typeButton.forEach(o -> o.categoryPanel.textBoxes.forEach(b -> b.isTyping = false));
                panel.typeButton.forEach(o -> o.categoryPanel.textBoxes.forEach(b -> b.isFocused = false));


                mc.displayGuiScreen(null);
            } catch (Exception e) {

            }
        }
        panel.typeButton.forEach(o -> o.categoryPanel.buttons.forEach(b -> b.keyPressed(key)));
        panel.typeButton.forEach(o -> o.categoryPanel.textBoxes.forEach(t -> t.keyPressed(key)));
        for (CategoryButton button : Client.getClickGui().mainPanel.typeButton) {
            if (button.categoryPanel.configTextBox != null) {
                ConfigTextBox textBox = button.categoryPanel.configTextBox;
                if (button.categoryPanel.visible) {
                    textBox.keyPressed(key);
                }
            }
        }
    }

    @Override
    public void panelConstructor(MainPanel mainPanel, float x, float y) {
        int y1 = 15;
        for (ModuleData.Type types : ModuleData.Type.values()) {
            mainPanel.typeButton.add(new CategoryButton(mainPanel, types.name(), x + 3, y + y1));
            y += 40;
        }
        if (allowMinigames)
            mainPanel.typeButton.add(new CategoryButton(mainPanel, ModuleData.Type.Minigames.name(), x + 3, y + y1));
        y += 40;
        mainPanel.typeButton.add(new CategoryButton(mainPanel, "Colors", x + 3, y + y1));
        mainPanel.typeButton.get(0).enabled = true;
        mainPanel.typeButton.get(0).categoryPanel.visible = true;
/*        mainPanel.slButtons.add(new SLButton(mainPanel, "Save", 5, 275 - 49, false));
        mainPanel.slButtons.add(new SLButton(mainPanel, "Load", 5, 275 - 35, true));*/
    }

    @Override
    public void panelMouseClicked(MainPanel mainPanel, int x, int y, int z) {
        if (opacity.getOpacity() < 220)
            return;
        if (x >= mainPanel.x + mainPanel.dragX && y >= mainPanel.dragY + mainPanel.y && x <= mainPanel.dragX + mainPanel.x + 400 && y <= mainPanel.dragY + mainPanel.y + 12.0f && z == 0) {
            mainPanel.dragging = true;
            mainPanel.lastDragX = x - mainPanel.dragX;
            mainPanel.lastDragY = y - mainPanel.dragY;
        }
        mainPanel.typeButton.forEach(c -> {
            c.mouseClicked(x, y, z);
            c.categoryPanel.mouseClicked(x, y, z);
        });
        mainPanel.slButtons.forEach(slButton -> slButton.mouseClicked(x, y, z));
    }

    @Override
    public void panelMouseMovedOrUp(MainPanel mainPanel, int x, int y, int z) {
        if (opacity.getOpacity() < 220)
            return;
        if (z == 0) {
            mainPanel.dragging = false;
        }
        for (CategoryButton button : mainPanel.typeButton) {
            button.mouseReleased(x, y, z);
        }
    }

    @Override
    public void categoryButtonConstructor(CategoryButton p0, MainPanel p1) {
        p0.categoryPanel = new CategoryPanel(p0.name, p0, 0, 0);
    }

    @Override
    public void categoryButtonMouseClicked(CategoryButton p0, MainPanel p1, int p2, int p3, int p4) {
        if (p2 >= p0.x + p1.dragX && p3 >= p1.dragY + p0.y && p2 <= p1.dragX + p0.x + 40 && p3 <= p1.dragY + p0.y + 40 && p4 == 0) {
            for (CategoryButton button : p1.typeButton) {
                if (button == p0) {
                    p0.enabled = true;
                    p0.categoryPanel.visible = true;
                } else {
                    button.enabled = false;
                    button.categoryPanel.visible = false;
                }
            }
        }
    }

    @Override
    public void categoryButtonDraw(CategoryButton p0, float p2, float p3) {
        //RenderingUtil.rectangle(p0.x + p0.panel.dragX, p0.y + p0.panel.dragY, p0.x + p0.panel.dragX + 50, p0.y + 40 + p0.panel.dragY, Colors.getColor(230,30));

        int brightness = p0.enabled ? 210 : 91;
        boolean hovering = p2 >= p0.x + p0.panel.dragX && p3 >= p0.panel.dragY + p0.y && p2 <= p0.panel.dragX + p0.x + 40 && p3 < p0.panel.dragY + p0.y + 40;
        if (hovering && !p0.enabled) {
            brightness = 165;
        }

        if (hovering) {
            Client.fss.drawStringWithShadow(p0.name, (p0.panel.x + 2 + p0.panel.dragX) + 55, (p0.panel.y + 9 + p0.panel.dragY), Colors.getColor(220, (int) opacity.getOpacity()));
        }

        p0.fade.interp(brightness, 10);
        int color = Colors.getColor((int) p0.fade.getOpacity(), (int) opacity.getOpacity());
/*            if (p0.enabled) {
                RenderingUtil.rectangle(p0.x + 3 + p0.panel.dragX, p0.y + p0.panel.dragY, p0.x + 6 + p0.panel.dragX, p0.y + 12 + p0.panel.dragY, Colors.getColor(165,241,165));
            }*/

        switch (p0.name) {
            case "Other":
                Client.badCache.drawCenteredString("I", (p0.x + 19 + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            case "Combat":
                Client.badCache.drawCenteredString("E", (p0.x + 19 + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            case "Player":
                Client.badCache.drawCenteredString("F", (p0.x + 18 + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            case "Movement":
                Client.badCache.drawCenteredString("J", (p0.x + 19 + p0.panel.dragX), (p0.y + 22 + p0.panel.dragY), color);
                break;
            case "Visuals":
                Client.badCache.drawCenteredString("C", (p0.x + 18 + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            case "Colors":
                Client.badCache.drawCenteredString("H", (p0.x + 18.5F + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            case "Minigames":
                Client.badCache.drawCenteredString("G", (p0.x + 18.5F + p0.panel.dragX), (p0.y + 20 + p0.panel.dragY), color);
                break;
            default:
                Client.f.drawStringWithShadow(p0.name.substring(0, 1), (p0.x + 12 + p0.panel.dragX), (p0.y + 13 + p0.panel.dragY), color);
                break;
        }

        if (p0.enabled) {
            p0.categoryPanel.draw(p2, p3);
        }
    }

    private List<Setting> getSettings(Module mod) {
        List<Setting> settings = new ArrayList();
        for (Setting set : mod.getSettings().values()) {
            settings.add(set);
        }
        if (settings.isEmpty()) {
            return null;
        }
        return settings;
    }

    @Override
    public void categoryPanelConstructor(CategoryPanel categoryPanel, CategoryButton categoryButton, float x, float y) {
        float xOff = 50 + categoryButton.panel.x;
        float yOff = 15 + categoryButton.panel.y;

        if (categoryButton.name.equalsIgnoreCase("Minigames")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType() == ModuleData.Type.Minigames) {
                    y = 20;
                    List<Setting> list = getSettings(module);
                    if (getSettings(module) != null) {
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        for (Setting setting : list) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                }
                            }
                        }
                        if (x1 == 44.5f) {
                            y += 10;
                        }
                        x1 = 0.5f;
                        int tY = 0;
                        List<Setting> sliders = new ArrayList<>();
                        list.forEach(setting -> {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        });
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += 12;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options || setting.getValue() instanceof MultiBool) {
                                if (x1 == 44.5f) {
                                    y += 14;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                if (x1 == 44.5f) {
                                    y += 11;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 240) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 240, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 40;
                    }
                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
        }

        if (categoryButton.name.equalsIgnoreCase("Combat")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
//                if(module == Client.getModuleManager().get(Secret.class))
//                    continue;

                if (module.getType() == ModuleData.Type.Combat) {
                    if (module.getName().equalsIgnoreCase("AutoSword")) {
                        yOff -= 95 + 36;
                        xOff += 95;
                    }
                    if (module.getName().equalsIgnoreCase("AimBot")) {
                        yOff -= 95 + 56;
                        xOff += 95;
                    }

                    if (module.getName().equalsIgnoreCase("ZombieAim")) {
                        yOff += 13;
                    }
                    if (module.getName().equalsIgnoreCase("AutoPot")) {
                        yOff -= 5;
                    }
                    if (module.getName().equalsIgnoreCase("Bypass")) {
                        yOff += 22;
                    }
                    if (module.getName().equalsIgnoreCase("AntiVelocity"))
                        xOff += 95;
                    if (module.getName().equalsIgnoreCase("Killaura"))
                        xOff -= 95 * 2;
                    if (module.getName().equalsIgnoreCase("AutoPot"))
                        xOff += 95;
                    y = 20;
                    List<Setting> list = getSettings(module);
                    if (getSettings(module) != null) {
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        for (Setting setting : list) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                }
                            }
                        }
                        if (x1 == 44.5f) {
                            y += 10;
                        }
                        x1 = 0.5f;
                        int tY = 0;
                        List<Setting> sliders = new ArrayList<>();
                        list.forEach(setting -> {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        });
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += 12;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options || setting.getValue() instanceof MultiBool) {
                                if (x1 == 44.5f) {
                                    y += 14;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                if (x1 == 44.5f) {
                                    y += 11;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 240) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 240, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 40;
                    }
                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
        }
        if (categoryButton.name.equalsIgnoreCase("Player")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType() == ModuleData.Type.Player) {
                    y = 20;
                    if (module.getName().equalsIgnoreCase("ChestStealer"))
                        yOff -= 20;
                    if (module.getName().equalsIgnoreCase("Inventory")) {
                        xOff = 290.0F;
                        yOff = 127.0F;
                    }
                    if (module.getName().equalsIgnoreCase("InventoryCleaner"))
                        yOff -= 20;
                    if (module.getName().equalsIgnoreCase("SurvivalNuker")) {
                        yOff -= 31;
                        xOff = 50 + categoryButton.panel.x + 95;
                    }
                    if (module.getName().equalsIgnoreCase("AutoArmor")) {
                        xOff = 50 + categoryButton.panel.x + 95 * 2;
                        yOff = 158.0F;
                    }
                    if (module.getName().equalsIgnoreCase("StreamerMode")) {
                        yOff += 45;
                    }

                    if (getSettings(module) != null) {
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                }
                            }
                        }
                        if (x1 == 44.5f) {
                            y += 10;
                        }
                        x1 = 0.5f;
                        int tY = 0;
                        List<Setting> sliders = new ArrayList<>();
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        }
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += 12;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options || setting.getValue() instanceof MultiBool) {
                                if (!module.getName().equalsIgnoreCase("ChestStealer")) {
                                    if (x1 == 44.5f) {
                                        y += 14;
                                    }
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                if (x1 == 44.5f) {
                                    y += 11;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 240) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 240, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 40;
                    }

                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
        }
        if (categoryButton.name.equalsIgnoreCase("Movement")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType() == ModuleData.Type.Movement) {
                    y = 20;
                    if (module == Client.getModuleManager().get(NoSlowdown.class)) {
                        yOff -= 20;
                    }
                    if (module == Client.getModuleManager().get(Phase.class)) {
                        yOff -= 24;
                    }
                    if (module == Client.getModuleManager().get(AntiFall.class)) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += 54;
                    }
                    if (module == Client.getModuleManager().get(LongJump.class)) {
                        yOff -= 27 + 10;
                    }
                    if (module == Client.getModuleManager().get(Fly.class)) {
                        yOff += 37;
                    }

                    if (getSettings(module) != null) {
                        int tY = 0;
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                x1 += 44;
                                tY = 10;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                    tY = 0;
                                }
                            }
                        }
                        if (x1 == 44.5 && (module == Client.getModuleManager().get(TargetStrafe.class) || module == Client.getModuleManager().get(LongJump.class))) {
                            x1 = 0.5f;
                            y += 10;
                        }
                        List<Setting> sliders = new ArrayList<>();
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        }
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += 10;
                            }
                        }
                        if (x1 == 44.5f && module == Client.getModuleManager().get(TargetStrafe.class)) {
                            x1 = 0.5f;
                            y += 10;
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                tY = 11;
                                if (x1 == 44.5f) {
                                    y += 11;
                                    tY = 0;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 240) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 240, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 40;
                    }

                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
        }
        if (categoryButton.name.equalsIgnoreCase("Visuals")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType() == ModuleData.Type.Visuals) {
                    y = 20;
                    if (module.getName().equalsIgnoreCase("Weather")) {
                        yOff -= 40;
                        xOff += 95;
                        xOff += 95;
                    }
                    if (module.getName().equalsIgnoreCase("2dtags")) {
                        yOff -= 10;
                    }
                    if (module.getName().equalsIgnoreCase("Radar")) {
                        yOff -= 15.5;
                    }
                    if (module.getName().equalsIgnoreCase("Chams")) {
                        yOff -= 5;
                    }
                    if (module.getName().equalsIgnoreCase("MoreParticles")) {
                        xOff = 50 + categoryButton.panel.x + 95 + 95;
                        yOff += 38;
                    }
                    if (module.getName().equalsIgnoreCase("SimsESP")) {
                        xOff = 50 + categoryButton.panel.x + 95;
                        yOff -= 95;
                    }
                    if (module.getName().equalsIgnoreCase("Lines")) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff -= 10;
                    }
                    if (module.getName().equalsIgnoreCase("Targetesp")) {
                        yOff += 5;
                    }
                    if (module.getName().equalsIgnoreCase("Xray")) {
                        yOff += 10;
                    }
                    if (module.getName().equalsIgnoreCase("2desp")) {
                        yOff -= 10;
                    }
                    if (module.getName().equalsIgnoreCase("hud")) {
                        yOff -= 10;
                    }
                    if (module.getName().equalsIgnoreCase("Crosshair")) {
                        yOff += 20;
                    }
                    if (module == Client.getModuleManager().get(TargetESP.class)) {
                        xOff = 50 + categoryButton.panel.x + 95;
                        yOff -= 110;
                    }

                    if (getSettings(module) != null) {
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        int tY = 0;
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                tY = 10;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                }
                            }
                        }
                        if (!module.getName().equalsIgnoreCase("Waypoints") && !module.getName().equalsIgnoreCase("HUD") && !module.getName().equals("2DTags")) {
                            if (x1 == 44.5f) {
                                y += 10;
                            }
                            x1 = 0.5f;
                        }

                        List<Setting> sliders = new ArrayList<>();
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        }
                        boolean small = tY == 10;
                        tY = 0;
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += small ? 10 : 12;
                            }

                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options || setting.getValue() instanceof MultiBool) {
                                if (!module.getName().equalsIgnoreCase("2DTags")) {
                                    if (x1 == 44.5f) {
                                        y += 14;
                                    }
                                    x1 = 0.5f;
                                }
                            }
                        }
                        if (module.getName().equalsIgnoreCase("Glow")) {
                            y -= 14F;
                            x1 += 44;
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                if (x1 == 44.5f) {
                                    y += 11;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 259) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 259, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 37;
                    }

                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
        }
        if (categoryButton.name.equalsIgnoreCase("Other")) {
            float biggestY = 18 + 16;
            float noSets = 0;
            for (Module module : Client.getModuleManager().getArray()) {
                if (module.getType() == ModuleData.Type.Other) {
                    y = 20;
                    if (getSettings(module) != null) {
                        if (module.getName().equalsIgnoreCase("BedNuker")) {
                            yOff += 17;
                        }
                        if (module.getName().equalsIgnoreCase("Commands")) {
                            yOff -= 17;
                        }
                        if (module.getName().equalsIgnoreCase("HackerDetect")) {
                            yOff -= 28.5;
                        }
                        if (module.getName().equalsIgnoreCase("Spotify")) {
                            xOff = 50 + categoryButton.panel.x;
                            yOff += 65.5;
                        }
                        if (module.getName().equalsIgnoreCase("SilentView")) {
                            xOff = 50 + categoryButton.panel.x;
                            yOff += 32;
                        }
                        if (module.getName().equalsIgnoreCase("AutoSkin")) {
                            xOff = 50 + categoryButton.panel.x + 95;
                            yOff += 37;
                        }
                        if (module.getName().equalsIgnoreCase("ChatFilter")) {
                            yOff -= 52.5 - 4;
                        }
                        if (module.getName().equalsIgnoreCase("NetInfo")) {
                            //xOff = 50 + categoryButton.panel.x + 95;
                            yOff -= 38;
                        }
                        categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), xOff + 0.5f, yOff + 10, module));
                        float x1 = 0.5f;
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Boolean) {
                                categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    x1 = 0.5f;
                                    y += 10;
                                }
                            }
                        }
                        if (x1 == 44.5f) {
                            y += 10;
                        }
                        x1 = 0.5f;
                        int tY = 0;
                        List<Setting> sliders = new ArrayList<>();
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Number) {
                                sliders.add(setting);
                            }
                        }
                        sliders.sort(Comparator.comparing(Setting::getName));
                        for (Setting setting : sliders) {
                            categoryPanel.sliders.add(new Slider(categoryPanel, xOff + x1 + 1, yOff + y + 4, setting));
                            x1 += 44;
                            tY = 10;
                            if (x1 == 88.5f) {
                                tY = 0;
                                x1 = 0.5f;
                                y += 12;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options || setting.getValue() instanceof MultiBool) {
                                if (x1 == 44.5f) {
                                    y += 14;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue() instanceof Options) {
                                categoryPanel.dropdownBoxes.add(new DropdownBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                            if (setting.getValue() instanceof MultiBool) {
                                categoryPanel.multiDropdownBoxes.add(new MultiDropdownBox((MultiBool) setting.getValue(), setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 17;
                                x1 += 44;
                                if (x1 == 88.5f) {
                                    y += 17;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                if (x1 == 44.5f) {
                                    y += 11;
                                }
                                x1 = 0.5f;
                            }
                        }
                        for (Setting setting : getSettings(module)) {
                            if (setting.getValue().getClass().equals(String.class)) {
                                categoryPanel.textBoxes.add(new TextBox(setting, xOff + x1, yOff + y + 4, categoryPanel));
                                tY = 16;
                                x1 += 88;
                                if (x1 == 88.5f) {
                                    y += 15.5;
                                    tY = 0;
                                    x1 = 0.5f;
                                }
                            }
                        }
                        y += tY;
                        categoryPanel.groupBoxes.add(new GroupBox(module.getName(), categoryPanel, xOff, yOff, y == 34 ? 40 : y - 11));
                        xOff += 95;
                        if (y >= biggestY) {
                            biggestY = y;
                        }
                    } else {
                        if (noSets >= 240) {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets - 240, 345, module));
                        } else {
                            categoryPanel.buttons.add(new Button(categoryPanel, module.getName(), 55 + categoryButton.panel.x + noSets, 330, module));
                        }
                        noSets += 40;
                    }

                    if (xOff > 20 + categoryButton.panel.y + 310) {
                        xOff = 50 + categoryButton.panel.x;
                        yOff += (y == 20 && biggestY == 20) ? 26 : biggestY;
                    }
                }
            }
            float globalsOffsetX = 290 - 95, globalsOffsetY = 178 + 75;
            categoryPanel.groupBoxes.add(new GroupBox("Client Settings", categoryPanel, globalsOffsetX, globalsOffsetY, 30.5F, true));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 0.5F, globalsOffsetY + 10, GlobalValues.centerNotifs));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 0.5F, globalsOffsetY + 20, GlobalValues.scaleFix));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 0.5F, globalsOffsetY + 30, GlobalValues.showCape));

            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 44.5F, globalsOffsetY + 10, GlobalValues.showCursor));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 44.5F, globalsOffsetY + 20, GlobalValues.allowDebug));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, globalsOffsetX + 44.5F, globalsOffsetY + 30, GlobalValues.keepPriority));


            float xOffsetXD = 290, yOffsetXD = 215 - 10;
            categoryPanel.groupBoxes.add(new GroupBox("Configs", categoryPanel, xOffsetXD, yOffsetXD, 100, true));
            categoryPanel.configTextBox = new ConfigTextBox(xOffsetXD, yOffsetXD + 90, categoryPanel);
            categoryPanel.configList = new ConfigList(xOffsetXD, yOffsetXD + 10, categoryPanel);

            //                                 categoryPanel.checkboxes.add(new Checkbox(categoryPanel, setting.getName(), xOff + x1, yOff + y, setting));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, GlobalValues.saveVisuals.getName(), xOffsetXD + 0.5F, yOffsetXD + 100, GlobalValues.saveVisuals));
            categoryPanel.checkboxes.add(new Checkbox(categoryPanel, GlobalValues.loadVisuals.getName(), xOffsetXD + 44.5F, yOffsetXD + 100, GlobalValues.loadVisuals));

        }
        if (categoryButton.name.equalsIgnoreCase("Colors")) {
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.fVis, "Friendly Visible", xOff + 77.5F, y, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.fInvis, "Friendly Invisible", xOff + 77.5F, y + 57, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.eVis, "Enemy Visible", xOff + 177.5F, y, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.eInvis, "Enemy Invisible", xOff + 177.5F, y + 57, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.fTeam, "Friendly Team", xOff + 77.5F, y + 114, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.eTeam, "Enemy Team", xOff + 177.5F, y + 114, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.strafeColor, "TargetStrafe Active", xOff + 277.5F, y + 114, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.hudColor, "HUD", xOff + 277.5F, y, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.xhair, "Crosshair", xOff + 277.5F, y + 57, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.chamsVis, "Chams Visible", xOff + 77.5F, y + 171, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.chamsInvis, "Chams Invisible", xOff + 177.5F, y + 171, categoryButton));
            categoryPanel.colorPreviews.add(new ColorPreview(ColorManager.chestESPColor, "ChestESP", xOff + 277.5F, y + 171, categoryButton));
        }
    }

    @Override
    public void categoryPanelMouseClicked(CategoryPanel categoryPanel, int p1, int p2, int p3) {
        boolean active = false;
        for (TextBox tb : categoryPanel.textBoxes) {
            if (tb.isFocused || tb.isTyping) {
                tb.mouseClicked(p1, p2, p3);
                active = true;
                break;
            }
        }
        if (categoryPanel.configTextBox != null) {
            ConfigTextBox tb = categoryPanel.configTextBox;
            if (tb.isFocused || tb.isTyping) {
                tb.mouseClicked(p1, p2, p3);
                active = true;
            }
        }
        for (DropdownBox db : categoryPanel.dropdownBoxes) {
            if (db.active) {
                db.mouseClicked(p1, p2, p3);
                active = true;
                break;
            }
        }
        for (MultiDropdownBox db : categoryPanel.multiDropdownBoxes) {
            if (db.active) {
                db.mouseClicked(p1, p2, p3);
                active = true;
                break;
            }
        }
        if (!active) {
            if (categoryPanel.configTextBox != null) {
                if (categoryPanel.visible) {
                    ConfigTextBox tb = categoryPanel.configTextBox;
                    tb.mouseClicked(p1, p2, p3);
                    categoryPanel.configList.mouseClicked(p1, p2, p3);
                }
            }
            categoryPanel.textBoxes.forEach(o -> o.mouseClicked(p1, p2, p3));
            categoryPanel.dropdownBoxes.forEach(o -> o.mouseClicked(p1, p2, p3));
            for (MultiDropdownBox db : categoryPanel.multiDropdownBoxes)
                db.mouseClicked(p1, p2, p3);
            for (Button button : categoryPanel.buttons) {
                button.mouseClicked(p1, p2, p3);
            }
            for (Checkbox checkbox : categoryPanel.checkboxes) {
                checkbox.mouseClicked(p1, p2, p3);
            }
            for (Slider slider : categoryPanel.sliders) {
                slider.mouseClicked(p1, p2, p3);
            }
            for (ColorPreview cp : categoryPanel.colorPreviews) {
                for (HSVColorPicker slider : cp.sliders) {
                    slider.mouseClicked(p1, p2, p3);
                }
            }
        }
    }

    @Override
    public void categoryPanelDraw(CategoryPanel categoryPanel, float x, float y) {
        for (ColorPreview cp : categoryPanel.colorPreviews) {
            cp.draw(x, y);
        }
        for (GroupBox groupBox : categoryPanel.groupBoxes) {
            groupBox.draw(x, y);
        }
        if (!categoryPanel.categoryButton.name.equalsIgnoreCase("Colors") && !categoryPanel.categoryButton.name.equalsIgnoreCase("Combat") && !categoryPanel.categoryButton.name.equals("Minigames")) {
            float xOff = 100 + categoryPanel.categoryButton.panel.dragX - 2.5F;
            float yOff = 322 + categoryPanel.categoryButton.panel.dragY;
            RenderingUtil.rectangleBordered(xOff, yOff - 6, xOff + 280, yOff + 33, 0.5, Colors.getColor(0, 0), Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.rectangleBordered(xOff + 0.5, yOff - 5.5, xOff + 280 - 0.5, yOff + 33 - 0.5, 0.5, Colors.getColor(0, 0), Colors.getColor(48, (int) opacity.getOpacity()));
            RenderingUtil.rectangle(xOff + 1, yOff - 5, xOff + 279, yOff + 33 - 1, Colors.getColor(17, (int) opacity.getOpacity()));
            RenderingUtil.rectangle(xOff + 5, yOff - 6, xOff + Client.fs.getWidth("No Settings") + 5, yOff - 4, Colors.getColor(17, (int) opacity.getOpacity()));
            Client.fs.drawStringWithShadow("No Settings", xOff + 5, yOff - 7, Colors.getColor(220, (int) opacity.getOpacity()));
        }
        for (TextBox tb : categoryPanel.textBoxes) {
            if (categoryPanel.visible) {
                tb.draw(x, y);
            }
        }
        if (categoryPanel.configTextBox != null) {
            ConfigTextBox tb = categoryPanel.configTextBox;
            if (categoryPanel.visible) {
                tb.draw(x, y);
                categoryPanel.configList.draw(x, y);
            }
        }
        for (Button button : categoryPanel.buttons) {
            button.draw(x, y);
        }
        for (Checkbox checkbox : categoryPanel.checkboxes) {
            checkbox.draw(x, y);
        }
        for (Slider slider : categoryPanel.sliders) {
            slider.draw(x, y);
        }


        List<MultiDropdownBox> multiList = new ArrayList<>(categoryPanel.multiDropdownBoxes);
        Collections.reverse(multiList);

        for (MultiDropdownBox db : multiList) {
            db.draw(x, y);
        }
        for (MultiDropdownBox db : multiList) {
            if (db.active) {
                for (MultiDropdownButton b : db.buttons) {
                    b.draw(x, y);
                }
            }
        }
        List<DropdownBox> list = new ArrayList<>(categoryPanel.dropdownBoxes);
        Collections.reverse(list);
        for (DropdownBox db : list) {
            db.draw(x, y);
        }
        for (DropdownBox db : list) {
            if (db.active) {
                for (DropdownButton b : db.buttons) {
                    b.draw(x, y);
                }
            }
        }


    }

    @Override
    public void categoryPanelMouseMovedOrUp(CategoryPanel categoryPanel, int x, int y, int button) {
        for (Slider slider : categoryPanel.sliders) {
            slider.mouseReleased(x, y, button);
        }
        for (ColorPreview cp : categoryPanel.colorPreviews) {
            for (HSVColorPicker slider : cp.sliders) {
                slider.mouseReleased(x, y, button);
            }
        }
    }

    @Override
    public void groupBoxConstructor(GroupBox groupBox, float x, float y) {

    }

    @Override
    public void groupBoxMouseClicked(GroupBox groupBox, int p1, int p2, int p3) {

    }

    @Override
    public void groupBoxDraw(GroupBox groupBox, float x, float y) {
        float xOff = groupBox.x + groupBox.categoryPanel.categoryButton.panel.dragX - 2.5F;
        float yOff = groupBox.y + groupBox.categoryPanel.categoryButton.panel.dragY + 10;
        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(xOff + 4.5, yOff - 6, xOff + Client.fs.getWidth(groupBox.label) + 6.5, yOff - 5.5, -1);
        RenderingUtil.rectangle(xOff + 5, yOff - 5.5, xOff + Client.fs.getWidth(groupBox.label) + 6, yOff - 5, -1);
        Depth.render(GL11.GL_LESS);
        RenderingUtil.rectangleBordered(xOff, yOff - 6, xOff + 90, yOff + groupBox.ySize, 0.5, Colors.getColor(0, 0), Colors.getColor(10, (int) opacity.getOpacity()));
        Depth.post();


        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(xOff + 4.5, yOff - 6, xOff + Client.fs.getWidth(groupBox.label) + 6.5, yOff - 5.5, -1);
        RenderingUtil.rectangle(xOff + 5, yOff - 5.5, xOff + Client.fs.getWidth(groupBox.label) + 6, yOff - 5, -1);

        Depth.render(GL11.GL_LESS);
        RenderingUtil.rectangleBordered(xOff + 0.5, yOff - 5.5, xOff + 90 - 0.5, yOff + groupBox.ySize - 0.5, 0.5, Colors.getColor(17, (int) opacity.getOpacity()), Colors.getColor(48, (int) opacity.getOpacity()));
        Depth.post();

        if (groupBox.renderLabel) {
            Client.fs.drawStringWithShadow(groupBox.label, xOff + 6, yOff - 6.5F, Colors.getColor(220, (int) opacity.getOpacity()));
        }
    }

    @Override
    public void groupBoxMouseMovedOrUp(GroupBox groupBox, int x, int y, int button) {

    }

    @Override
    public void handleMouseInput(MainPanel panel) {
        for (CategoryButton button : panel.typeButton) {
            CategoryPanel categoryPanel = button.categoryPanel;
            if (categoryPanel.configTextBox != null) {
                categoryPanel.configList.handleMouseInput();
            }
        }
    }

    @Override
    public void configButtonDraw(ConfigButton configButton, float x, float y) {
        float xOff = configButton.configList.categoryPanel.categoryButton.panel.dragX;
        float yOff = configButton.configList.categoryPanel.categoryButton.panel.dragY;

        boolean hovering = (x >= xOff + configButton.x) && (y >= yOff + configButton.y) && (x <= xOff + configButton.x + 85) && (y <= yOff + configButton.y + 7.5);

        RenderingUtil.rectangle(configButton.x + xOff - 0.3, configButton.y + yOff - 0.3, configButton.x + xOff + 85 + 0.3, configButton.y + yOff + 7.5 + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(configButton.x + xOff, configButton.y + yOff, configButton.x + xOff + 85, configButton.y + yOff + 7.5, Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        TTFFontRenderer font = Client.fsmallbold;

        String name = configButton.buttonType.name().charAt(0) + configButton.buttonType.name().toLowerCase().substring(1);
        font.drawStringWithShadow(name, (configButton.x + 85 / 2F + xOff) - font.getWidth(name) / 2, (configButton.y + 2 + yOff), hovering ? Colors.getColor(255, (int) opacity.getOpacity()) : Colors.getColor(150, (int) opacity.getOpacity()));
        if (hovering) {
            RenderingUtil.rectangleBordered(configButton.x + xOff, configButton.y + yOff, configButton.x + xOff + 85, configButton.y + yOff + 7.5, 0.3, Colors.getColor(0, 0), Colors.getColor(90, (int) opacity.getOpacity()));
        }
    }

    @Override
    public void configButtonMouseClicked(ConfigButton configButton, float x, float y, int button) {
        float xOff = configButton.configList.categoryPanel.categoryButton.panel.dragX;
        float yOff = configButton.configList.categoryPanel.categoryButton.panel.dragY;
        boolean hovering = (x >= xOff + configButton.x) && (y >= yOff + configButton.y) && (x <= xOff + configButton.x + 84) && (y <= yOff + configButton.y + 7.5);
        if (hovering && button == 0 && configButton.buttonType == ConfigButton.ButtonType.CREATE) {
            Client.configManager.createConfig(configButton.configList.configTextBox.textString);
            configButton.configList.configs = Client.configManager.getConfigs().toArray(new String[]{});
        } else if (hovering && button == 0 && configButton.configList.configs.length > 0 && configButton.configList.selectedConfigID != -1) {
            switch (configButton.buttonType) {
                case LOAD:
                    Client.configManager.load(configButton.configList.configs[configButton.configList.selectedConfigID]);
                    break;
                case SAVE:
                    Client.configManager.save(configButton.configList.configs[configButton.configList.selectedConfigID]);
                    break;
                case DELETE:
                    Client.configManager.deleteConfig(configButton.configList.configs[configButton.configList.selectedConfigID]);
                    configButton.configList.selectedConfigID = -1;
                    break;
            }
            configButton.configList.configs = Client.configManager.getConfigs().toArray(new String[]{});
        }
    }

    Timer timer = new Timer();

    @Override
    public void configHandleMouseInput(ConfigList configList) {
        if (configList.configs.length <= 4 || !configList.hovering)
            return;
        int var10 = Mouse.getEventDWheel();
        if (var10 != 0) {
            if (var10 > 0) {
                var10 = -1;
            } else if (var10 < 0) {
                var10 = 1;
            }

            configList.amountScrolled += (float) (var10 * (-7.5) / 2);
            if (configList.amountScrolled > 0) {
                configList.amountScrolled = 0;
            }
            if (configList.amountScrolled < ((configList.configs.length - 4) * -7.5F)) {
                configList.amountScrolled = ((configList.configs.length - 4) * -7.5F);
            }
        }
    }

    @Override
    public void configListDraw(ConfigList configList, float x, float y) {
        float xOff = configList.categoryPanel.categoryButton.panel.dragX;
        float yOff = configList.categoryPanel.categoryButton.panel.dragY;

        if (configList.categoryPanel.visible)
            if (timer.delay(1000)) {
                configList.configs = Client.configManager.getConfigs().toArray(new String[]{});
                timer.reset();
            }

        boolean hovering = configList.hovering = (x >= xOff + configList.x) && (y >= yOff + configList.y) && (x <= xOff + configList.x + 84) && (y <= yOff + configList.y + 30);

        RenderingUtil.rectangle(configList.x + xOff - 0.3, configList.y + yOff - 0.3, configList.x + xOff + 84 + 1.5, configList.y + yOff + 30 + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(configList.x + xOff, configList.y + yOff, configList.x + xOff + 85, configList.y + yOff + 30, Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        if (hovering) {
            RenderingUtil.rectangleBordered(configList.x + xOff, configList.y + yOff, configList.x + xOff + 85, configList.y + yOff + 30, 0.3, Colors.getColor(0, 0), Colors.getColor(90, (int) opacity.getOpacity()));
        }

        float offsetY = 2 + (configList.amountScrolled);
        float offsetValueThing = (configList.amountScrolled / -7.5F);
        float totalHeight = (configList.configs.length * 7.5F);
        if (totalHeight > 30) {
            float relativeScale = 29 * (30 / totalHeight);
            float relativeOffset = (29) * (offsetValueThing / configList.configs.length);
            float yPos = configList.y + yOff + relativeOffset;
            RenderingUtil.rectangle(configList.x + xOff + 82, yPos + 0.5, configList.x + xOff + 83, yPos + relativeScale + 0.5, Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.rectangleBordered(configList.x + xOff + 81.5, yPos + 1, configList.x + xOff + 83.5, yPos + relativeScale, 0.5, Colors.getColor(50, (int) opacity.getOpacity()), Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.rectangle(configList.x + xOff + 82, yPos + 1, configList.x + xOff + 83, yPos + relativeScale, Colors.getColor(50, (int) opacity.getOpacity()));
        }

        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(configList.x + xOff, configList.y + yOff + 0.5, configList.x + xOff + 84, configList.y + yOff + 30 - 0.5, -1);
        Depth.render();
        int i = 0;
        for (String cfgName : configList.configs) {
            boolean hoveringName = (x >= xOff + configList.x) && (y >= yOff + configList.y + offsetY) && (x <= xOff + configList.x + 84) && (y <= yOff + configList.y + offsetY + 7.5F);
            TTFFontRenderer font = Client.fsmallbold;
            int color = configList.selectedConfigID == i ? Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()) : hoveringName ? Colors.getColor(255, (int) opacity.getOpacity()) : Colors.getColor(150, (int) opacity.getOpacity());
            font.drawStringWithShadow(cfgName, (configList.x + 1.5F + xOff), (configList.y + offsetY + yOff), color);
            offsetY += 7.5F;
            i++;
        }
        Depth.post();

    }

    @Override
    public void configListMouseClicked(ConfigList configList, float x, float y, int button) {
        float xOff = configList.categoryPanel.categoryButton.panel.dragX;
        float yOff = configList.categoryPanel.categoryButton.panel.dragY;
        if (!configList.categoryPanel.visible)
            return;
        boolean hovering = (x >= xOff + configList.x) && (y >= yOff + configList.y) && (x <= xOff + configList.x + 84) && (y <= yOff + configList.y + 30);
        float offsetY = 2 + (configList.amountScrolled);
        int i = 0;
        for (String ignored : configList.configs) {
            boolean hoveringName = hovering && (x >= xOff + configList.x) && (y >= yOff + configList.y + offsetY) && (x <= xOff + configList.x + 84) && (y <= yOff + configList.y + offsetY + 7.5F);
            offsetY += 7.5F;
            if (hoveringName) {
                configList.selectedConfigID = i;
                break;
            } else if (hovering) {
                configList.selectedConfigID = -1;
            }
            i++;
        }
    }

    @Override
    public void buttonContructor(Button p0, CategoryPanel panel) {

    }

    @Override
    public void buttonMouseClicked(Button p0, int p2, int p3, int p4, CategoryPanel panel) {
        if (panel.categoryButton.enabled) {
            float xOff = panel.categoryButton.panel.dragX;
            float yOff = panel.categoryButton.panel.dragY;
            boolean hovering = p2 >= p0.x + xOff && p3 >= p0.y + yOff && p2 <= p0.x + 35 + xOff && p3 <= p0.y + 6 + yOff;
            if (hovering) {
                if (p4 == 0) {
                    if (!p0.isBinding) {
                        p0.module.toggle();
                        p0.enabled = p0.module.isEnabled();
                    } else {
                        p0.isBinding = false;
                    }
                } else if (p4 == 1) {
                    if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        p0.module.setHeld(!p0.module.getHeld());
                        ChatUtil.printChat(Command.chatPrefix + p0.module.getName() + " is now activated \247a" + (!p0.module.getHeld() ? "on Toggle" : "on Held") + "\2478.");
                        ModuleManager.saveStatus();
                    } else {
                        if (p0.isBinding) {
                            p0.module.setKeybind(new Keybind(p0.module, Keyboard.getKeyIndex("NONE")));
                            p0.isBinding = false;
                        } else {
                            p0.isBinding = true;
                        }
                    }
                }
            } else if (p0.isBinding) {
                p0.isBinding = false;
            }
        }
    }

    @Override
    public void buttonDraw(Button p0, float p2, float p3, CategoryPanel panel) {
        if (panel.categoryButton.enabled) {
            GlStateManager.pushMatrix();
            float xOff = panel.categoryButton.panel.dragX;
            float yOff = panel.categoryButton.panel.dragY;
            RenderingUtil.rectangleBordered(p0.x + xOff + 0.6, p0.y + yOff + 0.6, p0.x + 6 + xOff + -0.6, p0.y + 6 + yOff + -0.6, 0.5, Colors.getColor(0, 0), Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.drawGradient(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + 6 + xOff + -1, p0.y + 6 + yOff + -1, Colors.getColor(76, (int) opacity.getOpacity()), Colors.getColor(51, (int) opacity.getOpacity()));
            p0.enabled = p0.module.isEnabled();
            boolean hovering = p2 >= p0.x + xOff && p3 >= p0.y + yOff && p2 <= p0.x + 35 + xOff && p3 <= p0.y + 6 + yOff;
            GlStateManager.pushMatrix();
            Client.fs.drawStringWithShadow(p0.module.getName(), (p0.x + xOff + 3), (p0.y + 0.5f + yOff - 7), Colors.getColor(220, (int) opacity.getOpacity()));
            Client.fss.drawStringWithShadow("Enable", (p0.x + 7.6f + xOff), (p0.y + 1 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));

            String meme = !p0.module.getKeybind().getKeyStr().equalsIgnoreCase("None") ? "[" + p0.module.getKeybind().getKeyStr() + "]" : "[-]";
            GlStateManager.pushMatrix();
            GlStateManager.translate((p0.x + xOff + 29), (p0.y + 1f + yOff), 0);
            GlStateManager.enableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            mc.fontRendererObj.drawStringWithShadow(meme, 0, 0, p0.isBinding ? Colors.getColor(216, 56, 56, (int) opacity.getOpacity()) : Colors.getColor(75, (int) opacity.getOpacity()));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
            if (p0.enabled) {
                RenderingUtil.drawGradient(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + xOff + 5, p0.y + yOff + 5, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) (120 * (opacity.getOpacity() / 255F))));
            }
            if (hovering && !p0.enabled) {
                RenderingUtil.rectangle(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + xOff + 5, p0.y + yOff + 5, Colors.getColor(255, 40));
            }

            if (hovering) {
                Client.fss.drawStringWithShadow(p0.module.getDescription() != null && !p0.module.getDescription().equalsIgnoreCase("") ? p0.module.getDescription() : "ERROR: No Description Found.", (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 9 + panel.categoryButton.panel.dragY), Colors.getColor(220, (int) opacity.getOpacity()));
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void buttonKeyPressed(Button button, int key) {
        if (button.isBinding && key != 0) {
            int keyToBind = key;
            if (key == 1 || key == Keyboard.KEY_BACK) {
                keyToBind = Keyboard.getKeyIndex("NONE");
            }
            Keybind keybind = new Keybind(button.module, keyToBind);
            button.module.setKeybind(keybind);
            ModuleManager.saveStatus();
            button.isBinding = false;
        }
    }

    @Override
    public void checkBoxMouseClicked(Checkbox p0, int p2, int p3, int p4, CategoryPanel panel) {
        if (panel.categoryButton.enabled) {
            float xOff = panel.categoryButton.panel.dragX;
            float yOff = panel.categoryButton.panel.dragY;
            boolean hovering = p2 >= p0.x + xOff && p3 >= p0.y + yOff && p2 <= p0.x + 35 + xOff && p3 <= p0.y + 6 + yOff;
            if (hovering && p4 == 0) {
                boolean xd = (Boolean) p0.setting.getValue();
                p0.setting.setValue(!xd);
                ModuleManager.saveSettings();
            }
        }
    }

    @Override
    public void checkBoxDraw(Checkbox p0, float p2, float p3, CategoryPanel panel) {
        if (panel.categoryButton.enabled) {

            GlStateManager.pushMatrix();
            float xOff = panel.categoryButton.panel.dragX;
            float yOff = panel.categoryButton.panel.dragY;
            GlStateManager.pushMatrix();
            String xd = p0.setting.getName().charAt(0) + p0.setting.getName().toLowerCase().substring(1);
            Client.fss.drawStringWithShadow(xd, (p0.x + 7.5f + xOff), (p0.y + 1 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));
            GlStateManager.popMatrix();
            RenderingUtil.rectangleBordered(p0.x + xOff + 0.6, p0.y + yOff + 0.6, p0.x + 6 + xOff + -0.6, p0.y + 6 + yOff + -0.6, 0.5, Colors.getColor(0, 0), Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.drawGradient(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + 6 + xOff + -1, p0.y + 6 + yOff + -1, Colors.getColor(76, (int) opacity.getOpacity()), Colors.getColor(51, (int) opacity.getOpacity()));
            p0.enabled = ((Boolean) p0.setting.getValue());
            boolean hovering = p2 >= p0.x + xOff && p3 >= p0.y + yOff && p2 <= p0.x + 35 + xOff && p3 <= p0.y + 6 + yOff;

            if (p0.enabled) {
                RenderingUtil.drawGradient(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + xOff + 5, p0.y + yOff + 5, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) (120 * (opacity.getOpacity() / 255F))));
            }
            if (hovering && !p0.enabled) {
                RenderingUtil.rectangle(p0.x + xOff + 1, p0.y + yOff + 1, p0.x + xOff + 5, p0.y + yOff + 5, Colors.getColor(255, 40));
            }

            if (hovering) {
                Client.fss.drawStringWithShadow(getDescription(p0.setting), (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 9 + panel.categoryButton.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void dropDownContructor(DropdownBox p0, float p2, float p3, CategoryPanel panel) {
        int y = 10;
        for (String value : p0.option.getOptions()) {
            p0.buttons.add(new DropdownButton(value, p2, p3 + y, p0));
            y += 9;
        }
    }

    @Override
    public void dropDownMouseClicked(DropdownBox dropDown, int mouseX, int mouseY, int mouse, CategoryPanel panel) {
        for (DropdownButton db : dropDown.buttons) {
            if (dropDown.active && dropDown.panel.visible) {
                db.mouseClicked(mouseX, mouseY, mouse);
            }
        }
        if ((mouseX >= panel.categoryButton.panel.dragX + dropDown.x) && (mouseY >= panel.categoryButton.panel.dragY + dropDown.y) && (mouseX <= panel.categoryButton.panel.dragX + dropDown.x + 40) && (mouseY <= panel.categoryButton.panel.dragY + dropDown.y + 8) &&
                (mouse == 0) && dropDown.panel.visible) {
            dropDown.active = (!dropDown.active);
        } else {
            dropDown.active = false;
        }
    }

    @Override
    public void dropDownDraw(DropdownBox p0, float p2, float p3, CategoryPanel panel) {
        float xOff = panel.categoryButton.panel.dragX;
        float yOff = panel.categoryButton.panel.dragY;//40
        boolean hovering = (p2 >= panel.categoryButton.panel.dragX + p0.x) && (p3 >= panel.categoryButton.panel.dragY + p0.y) && (p2 <= panel.categoryButton.panel.dragX + p0.x + 40) && (p3 <= panel.categoryButton.panel.dragY + p0.y + 9);

        RenderingUtil.rectangle(p0.x + xOff - 0.3, p0.y + yOff - 0.3, p0.x + xOff + 40 + 0.3, p0.y + yOff + 9 + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));

        RenderingUtil.drawGradient(p0.x + xOff, p0.y + yOff, p0.x + xOff + 40, p0.y + yOff + 9, Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        if (hovering) {
            RenderingUtil.rectangleBordered(p0.x + xOff, p0.y + yOff, p0.x + xOff + 40, p0.y + yOff + 9, 0.3, Colors.getColor(0, 0), Colors.getColor(90, (int) opacity.getOpacity()));
        }
        Client.fss.drawStringWithShadow(p0.option.getName(), (p0.x + xOff + 1), (p0.y - 6 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));
        GlStateManager.pushMatrix();
        GlStateManager.translate((p0.x + xOff + 38 - (p0.active ? 2.5 : 0)), (p0.y + 4.5 + yOff), 0);
        GlStateManager.rotate(p0.active ? 270 : 90, 0, 0, 90);

        RenderingUtil.rectangle(0 - 1, 0, 0.5 - 1, 2.5, Colors.getColor(0, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(0.5 - 1, 0, 0, 2.5, Colors.getColor(151, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(0, 0.5, 1.5 - 1, 2, Colors.getColor(151, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(1.5 - 1, 1, 2 - 1, 1.5, Colors.getColor(151, (int) opacity.getOpacity()));

        GlStateManager.popMatrix();
        Client.fss.drawString(p0.option.getSelected(), (p0.x + 4 + xOff) - 1, (p0.y + 3f + yOff), Colors.getColor(151, (int) opacity.getOpacity()));
        if (p0.option.getSelected().contains("180")) {
            mc.fontRendererObj.drawString("树屋", (p0.x + 3 + xOff) + Client.fss.getWidth(p0.option.getSelected()), (p0.y + yOff + 0.5f), Colors.getColor(151, (int) opacity.getOpacity()));
        }
        if (p0.active) {
            int i = p0.buttons.size();
            RenderingUtil.rectangle(p0.x + xOff - 0.3, p0.y + 10 + yOff - 0.3, p0.x + xOff + 40 + 0.3, p0.y + yOff + 9 + (9 * i) + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.drawGradient(p0.x + xOff, p0.y + yOff + 10, p0.x + xOff + 40, p0.y + yOff + 9 + (9 * i), Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        }
        if (hovering) {
            Client.fss.drawStringWithShadow(getDescription(p0.setting), (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 9 + panel.categoryButton.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
        }
    }


    @Override
    public void dropDownButtonMouseClicked(DropdownButton p0, DropdownBox p1, int x, int y, int mouse) {
        if ((x >= p1.panel.categoryButton.panel.dragX + p0.x) && (y >= p1.panel.categoryButton.panel.dragY + p0.y) && (x <= p1.panel.categoryButton.panel.dragX + p0.x + 40) && (y <= p1.panel.categoryButton.panel.dragY + p0.y + 8.5) && (mouse == 0)) {
            p1.option.setSelected(p0.name);
            p1.active = false;
        }
    }

    @Override
    public void dropDownButtonDraw(DropdownButton p0, DropdownBox p1, float x, float y) {
        float xOff = p1.panel.categoryButton.panel.dragX;
        float yOff = p1.panel.categoryButton.panel.dragY;
//        RenderingUtil.rectangle(p0.x +  xOff - 0.3, p0.y + yOff - 0.3, p0.x  + xOff + 40 + 0.3, p0.y + yOff + 6 + 0.3, Colors.getColor(010);
//        RenderingUtil.drawGradient(p0.x +  xOff, p0.y + yOff, p0.x  + xOff + 40, p0.y + yOff + 6, Colors.getColor(46), Colors.getColor(27));
        boolean hovering = (x >= xOff + p0.x) && (y >= yOff + p0.y) && (x <= xOff + p0.x + 40) && (y <= yOff + p0.y + 8.5);
        GlStateManager.pushMatrix();
        boolean active = p1.option.getSelected().equalsIgnoreCase(p0.name);
        TTFFontRenderer font = hovering ? Client.test2 : Client.test3;
        font.drawStringWithShadow((hovering || active ? "\247l" : "") + p0.name, (p0.x + 3 + xOff), (p0.y + 2f + yOff), active && !hovering ? Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()) : Colors.getColor(255, (int) opacity.getOpacity()));
        if (p0.name.contains("180")) {
            mc.fontRendererObj.drawStringWithShadow("树屋", (p0.x + 3 + xOff) + font.getWidth(p0.name), (p0.y + yOff - 1), active && !hovering ? Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()) : Colors.getColor(255, (int) opacity.getOpacity()));
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void multiDropDownContructor(MultiDropdownBox p0, float x, float u, CategoryPanel panel) {
        int y = 10;
        for (Setting value : p0.multiBool.getBooleans()) {
            p0.buttons.add(new MultiDropdownButton(value.getName(), x, u + y, p0, value));
            y += 9;
        }
    }

    @Override
    public void multiDropDownMouseClicked(MultiDropdownBox p0, int x, int u, int mouse, CategoryPanel panel) {
        for (MultiDropdownButton db : p0.buttons) {
            if (p0.active && p0.panel.visible) {
                db.mouseClicked(x, u, mouse);
            }
        }
        if (mouse == 0)
            if ((x >= panel.categoryButton.panel.dragX + p0.x) && (u >= panel.categoryButton.panel.dragY + p0.y) && (x <= panel.categoryButton.panel.dragX + p0.x + 40) && (u <= panel.categoryButton.panel.dragY + p0.y + 8) &&
                    (mouse == 0) && p0.panel.visible) {
                p0.active = (!p0.active);
            } else if (!((x >= panel.categoryButton.panel.dragX + p0.x) && (u >= panel.categoryButton.panel.dragY + p0.y + 8) && (x <= panel.categoryButton.panel.dragX + p0.x + 40) && (u <= panel.categoryButton.panel.dragY + p0.y + 8 + p0.buttons.size() * 9))) {
                p0.active = false;
            }
    }

    @Override
    public void multiDropDownDraw(MultiDropdownBox p0, float x, float y, CategoryPanel panel) {
        float xOff = panel.categoryButton.panel.dragX;
        float yOff = panel.categoryButton.panel.dragY;//40
        boolean hovering = (x >= panel.categoryButton.panel.dragX + p0.x) && (y >= panel.categoryButton.panel.dragY + p0.y) && (x <= panel.categoryButton.panel.dragX + p0.x + 40) && (y <= panel.categoryButton.panel.dragY + p0.y + 9);

        RenderingUtil.rectangle(p0.x + xOff - 0.3, p0.y + yOff - 0.3, p0.x + xOff + 40 + 0.3, p0.y + yOff + 9 + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));

        RenderingUtil.drawGradient(p0.x + xOff, p0.y + yOff, p0.x + xOff + 40, p0.y + yOff + 9, Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        if (hovering) {
            RenderingUtil.rectangleBordered(p0.x + xOff, p0.y + yOff, p0.x + xOff + 40, p0.y + yOff + 9, 0.3, Colors.getColor(0, 0), Colors.getColor(90, (int) opacity.getOpacity()));
        }
        Client.fss.drawStringWithShadow(p0.name, (p0.x + xOff + 1), (p0.y - 6 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));
        GlStateManager.pushMatrix();
        GlStateManager.translate((p0.x + xOff + 38 - (p0.active ? 2.5 : 0)), (p0.y + 4.5 + yOff), 0);
        GlStateManager.rotate(p0.active ? 270 : 90, 0, 0, 90);

        RenderingUtil.rectangle(0 - 1, 0, 0.5 - 1, 2.5, Colors.getColor(0, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(0.5 - 1, 0, 0, 2.5, Colors.getColor(151, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(0, 0.5, 1.5 - 1, 2, Colors.getColor(151, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(1.5 - 1, 1, 2 - 1, 1.5, Colors.getColor(151, (int) opacity.getOpacity()));

        GlStateManager.popMatrix();
        List<String> enabled = new ArrayList<>();
        p0.multiBool.getBooleans().forEach(set -> {
            if ((boolean) set.getValue())
                enabled.add(set.getName().charAt(0) + set.getName().toLowerCase().substring(1));
        });
        GlStateManager.pushMatrix();
        Depth.pre();
        Depth.mask();
        String str = enabled.isEmpty() ? "None" : enabled.toString().replace("[", "").replace("]", "");
        Client.fss.drawString(str, (p0.x + 4 + xOff) - 1, (p0.y + 3f + yOff), -1);
        Depth.render();
        RenderingUtil.rectangle(p0.x + xOff, p0.y + yOff, p0.x + xOff + 30, p0.y + yOff + 9, Colors.getColor(151, (int) opacity.getOpacity()));
        RenderingUtil.drawGradientSideways(p0.x + xOff + 30, p0.y + yOff, p0.x + xOff + 35, p0.y + yOff + 9, Colors.getColor(151, (int) opacity.getOpacity()), Colors.getColor(151, 0));

        Depth.post();
        GlStateManager.popMatrix();
        if (p0.active) {
            int i = p0.buttons.size();
            RenderingUtil.rectangle(p0.x + xOff - 0.3, p0.y + 10 + yOff - 0.3, p0.x + xOff + 40 + 0.3, p0.y + yOff + 9 + (9 * i) + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.drawGradient(p0.x + xOff, p0.y + yOff + 10, p0.x + xOff + 40, p0.y + yOff + 9 + (9 * i), Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        }
        if (hovering) {
            Client.fss.drawStringWithShadow(getDescription(p0.setting), (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 9 + panel.categoryButton.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
        }
    }

    @Override
    public void multiDropDownButtonMouseClicked(MultiDropdownButton p0, MultiDropdownBox p1, int x, int y, int mouse) {
        if ((x >= p1.panel.categoryButton.panel.dragX + p0.x) && (y >= p1.panel.categoryButton.panel.dragY + p0.y) && (x <= p1.panel.categoryButton.panel.dragX + p0.x + 40) && (y <= p1.panel.categoryButton.panel.dragY + p0.y + 8.5) && (mouse == 0)) {
            p0.setting.setValue(!(boolean) p0.setting.getValue());
        }
    }

    @Override
    public void multiDropDownButtonDraw(MultiDropdownButton p0, MultiDropdownBox p1, float x, float y) {
        float xOff = p1.panel.categoryButton.panel.dragX;
        float yOff = p1.panel.categoryButton.panel.dragY;
//        RenderingUtil.rectangle(p0.x +  xOff - 0.3, p0.y + yOff - 0.3, p0.x  + xOff + 40 + 0.3, p0.y + yOff + 6 + 0.3, Colors.getColor(010);
//        RenderingUtil.drawGradient(p0.x +  xOff, p0.y + yOff, p0.x  + xOff + 40, p0.y + yOff + 6, Colors.getColor(46), Colors.getColor(27));
        boolean hovering = (x >= xOff + p0.x) && (y >= yOff + p0.y) && (x <= xOff + p0.x + 40) && (y <= yOff + p0.y + 8.5);
        GlStateManager.pushMatrix();
        boolean active = (boolean) p0.setting.getValue();
        TTFFontRenderer font = hovering ? Client.test2 : Client.test3;
        font.drawStringWithShadow((hovering || active ? "\247l" : "") + p0.setting.getName().charAt(0) + p0.setting.getName().toLowerCase().substring(1), (p0.x + 3 + xOff), (p0.y + 2f + yOff), active && !hovering ? Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()) : Colors.getColor(255, (int) opacity.getOpacity()));
//        if(hovering) {
//            Client.fss.drawStringWithShadow(p0.multiBool.getDesc(), (p1.panel.categoryButton.panel.x + 2 + p1.panel.categoryButton.panel.dragX) + 55, (p1.panel.categoryButton.panel.y + 9 + p1.panel.categoryButton.panel.dragY), -1);
//        }
        GlStateManager.popMatrix();
    }


    @Override
    public void categoryButtonMouseReleased(CategoryButton categoryButton, int x, int y, int button) {
        categoryButton.categoryPanel.mouseReleased(x, y, button);
    }

    @Override
    public void slButtonDraw(SLButton slButton, float x, float y, MainPanel panel) {
        float xOff = panel.dragX;
        float yOff = panel.dragY + 75;
        boolean hovering = (x >= 55 + slButton.x + xOff && y >= slButton.y + yOff - 2 && x <= 55 + slButton.x + xOff + 40 && y <= slButton.y + 8 + yOff + 2);
        RenderingUtil.rectangleBordered(slButton.x + xOff + 55 - 0.3, slButton.y + yOff - 0.3 - 2, slButton.x + xOff + 40 + 55 + 0.3, slButton.y + 8 + yOff + 0.3 + 2, 0.3, Colors.getColor(10, (int) opacity.getOpacity()), Colors.getColor(10, (int) opacity.getOpacity()));

        RenderingUtil.drawGradient(slButton.x + xOff + 55, slButton.y + yOff - 2, slButton.x + xOff + 40 + 55, slButton.y + 8 + yOff + 2, Colors.getColor(46, (int) opacity.getOpacity()), Colors.getColor(27, (int) opacity.getOpacity()));
        if (hovering) {
            RenderingUtil.rectangleBordered(slButton.x + xOff + 55, slButton.y + yOff - 2, slButton.x + xOff + 40 + 55, slButton.y + 8 + yOff + 2, 0.6, Colors.getColor(0, 0), Colors.getColor(90, (int) opacity.getOpacity()));
        }
        float xOffset = Client.fs.getWidth(slButton.name) / 2;
        //(p0.x + 20 - offSet/2 + xOff)
        Client.fs.drawStringWithShadow(slButton.name, (xOff + 25 + 55) - xOffset, slButton.y + yOff + 1.5f, Colors.getColor(255, (int) opacity.getOpacity()));
    }

    @Override
    public void slButtonMouseClicked(SLButton slButton, float x, float y, int button, MainPanel panel) {
        float xOff = panel.dragX;
        float yOff = panel.dragY + 75;
        if (button == 0 &&
                x >= 55 + slButton.x + xOff &&
                y >= slButton.y + yOff - 2 &&
                x <= 55 + slButton.x + xOff + 40 &&
                y <= slButton.y + 8 + yOff + 2) {

            if (slButton.load) {
                ChatUtil.printChat("Settings have been loaded.");
                ModuleManager.loadSettings();
                ColorCommand.loadStatus();
                panel.typePanel.forEach(o -> o.sliders.forEach(slider -> {
                    slider.dragX = slider.lastDragX = ((Number) slider.setting.getValue()).doubleValue() * 40 / slider.setting.getMax();
                }));
            } else {
                ChatUtil.printChat("Settings have been saved.");
                ColorCommand.saveStatus();
                ModuleManager.saveSettings();
                panel.typePanel.forEach(o -> o.sliders.forEach(slider -> {
                    slider.dragX = slider.lastDragX = ((Number) slider.setting.getValue()).doubleValue() * 40 / slider.setting.getMax();
                }));
            }
        }
    }

    @Override
    public void colorConstructor(ColorPreview colorPreview, float x, float y) {
        colorPreview.sliders.add(new HSVColorPicker(x + 10, y, colorPreview, colorPreview.colorObject));
    }

    @Override
    public void colorPrewviewDraw(ColorPreview colorPreview, float x, float y) {
        float xOff = colorPreview.x + colorPreview.categoryPanel.panel.dragX;
        float yOff = colorPreview.y + colorPreview.categoryPanel.panel.dragY + 75;
        RenderingUtil.rectangleBordered(xOff - 80, yOff - 6, xOff + 1, yOff + 46, 0.3, Colors.getColor(48, (int) opacity.getOpacity()), Colors.getColor(10, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(xOff - 79, yOff - 5, xOff, yOff + 45, Colors.getColor(17, (int) opacity.getOpacity()));
        RenderingUtil.rectangle(xOff - 74, yOff - 6, xOff - 73 + Client.fs.getWidth(colorPreview.colorName) + 1, yOff - 4, Colors.getColor(17, (int) opacity.getOpacity()));
        Client.fs.drawStringWithShadow(colorPreview.colorName, xOff - 73, yOff - 8, Colors.getColor(255, (int) opacity.getOpacity()));
        colorPreview.sliders.get(0).draw(x, y);
    }

    @Override
    public void colorPickerConstructor(HSVColorPicker picker, float x, float y) {
        Color color = new Color(picker.colorPreview.colorObject.getColorHex());
        picker.opacity = (float) picker.colorPreview.colorObject.getAlpha() / 255;
        picker.hue = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
        picker.saturation = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[1];
        picker.brightness = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2];
    }

    @Override
    public void colorPickerDraw(HSVColorPicker cp, float x, float y) {
        float xOff = cp.x + cp.colorPreview.categoryPanel.panel.dragX - 85.5f;
        float yOff = cp.y + cp.colorPreview.categoryPanel.panel.dragY + 74;
        //Color Picker
        RenderingUtil.rectangle(xOff, yOff, xOff + 43, yOff + 43, Colors.getColor(32, (int) opacity.getOpacity()));
        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(xOff + 0.5, yOff + 0.5, xOff + 42.5, yOff + 42.5, -1);
        Depth.render();
        RenderingUtil.drawGradientSideways(xOff + 0.5, yOff + 0.5, xOff + 46.5f, yOff + 42.5, Colors.getColor(255, (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(cp.hue, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 0.5, yOff - 4, xOff + 42.5, yOff + 42.5, Colors.getColor(0, 0), Colors.getColor(0, (int) opacity.getOpacity()));
        Depth.post();
        RenderingUtil.rectangleBordered(xOff + (42.5 * cp.saturation) - 1, yOff + 42.5 - (42.5 * cp.brightness) - 1, xOff + (42.5 * cp.saturation) + 1, yOff + 42.5 - (42.5 * cp.brightness) + 1, 0.5, Colors.getColor(Color.getHSBColor(cp.hue, cp.saturation, cp.brightness), (int) opacity.getOpacity()), Colors.getColor(0, (int) opacity.getOpacity()));
        //Hue Slider Rainbow
        RenderingUtil.rectangle(xOff + 45, yOff, xOff + 48, yOff + 43, Colors.getColor(32, (int) opacity.getOpacity()));

        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 0.5f, xOff + 47.5f, yOff + 8, Colors.getColor(Color.getHSBColor(0, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.2f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 8, xOff + 47.5f, yOff + 13, Colors.getColor(Color.getHSBColor(0.2f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.3f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 13, xOff + 47.5f, yOff + 17, Colors.getColor(Color.getHSBColor(0.3f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.4f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 17, xOff + 47.5f, yOff + 22, Colors.getColor(Color.getHSBColor(0.4f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.5f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 22, xOff + 47.5f, yOff + 26, Colors.getColor(Color.getHSBColor(0.5f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.6f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 26, xOff + 47.5f, yOff + 30, Colors.getColor(Color.getHSBColor(0.6f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.7f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 30, xOff + 47.5f, yOff + 34, Colors.getColor(Color.getHSBColor(0.7f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(0.8f, 1, 1), (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(xOff + 45.5f, yOff + 34, xOff + 47.5f, yOff + 42.5, Colors.getColor(Color.getHSBColor(0.8f, 1, 1), (int) opacity.getOpacity()), Colors.getColor(Color.getHSBColor(1f, 1, 1), (int) opacity.getOpacity()));
        //Hue Slider Selector
        RenderingUtil.rectangleBordered(xOff + 45, yOff + (42.5 * cp.hue) - 1.5f, xOff + 48, yOff + (42.5 * cp.hue) + 1.5f, 0.5f, Colors.getColor(0, (int) opacity.getOpacity()), Colors.getColor(cp.selectingHue ? 255 : 200, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(xOff + 50, yOff, xOff + 53, yOff + 43, 0.5f, Colors.getColor(cp.color.getRed(), cp.color.getGreen(), cp.color.getBlue(), (int) opacity.getOpacity()), Colors.getColor(32, (int) opacity.getOpacity()));
        RenderingUtil.rectangleBordered(xOff + 50, yOff + (42.5 * cp.opacity) - 1.5f, xOff + 53, yOff + (42.5 * cp.opacity) + 1.5f, 0.5f, Colors.getColor(0, (int) opacity.getOpacity()), Colors.getColor(cp.selectingOpacity ? 255 : 200, (int) opacity.getOpacity()));
        boolean shouldUpdate = (cp.selectingHue || cp.selectingColor || cp.selectingOpacity);
        if (shouldUpdate) {
            Color tcolor = Color.getHSBColor((1 - cp.hue), cp.saturation, cp.brightness);
            cp.color.updateColors(tcolor.getRed(), tcolor.getBlue(), tcolor.getGreen(), (int) (255 * cp.opacity));
        } else {
            float[] bruh = new float[3];
            Color.RGBtoHSB(cp.color.getRed(), cp.color.getGreen(), cp.color.getBlue(), bruh);
            cp.opacity = cp.color.alpha / 255F;
            cp.hue = bruh[0];
            cp.saturation = bruh[1];
            cp.brightness = bruh[2];
        }


        if (cp.selectingOpacity) {
            float tempY = y;
            if (tempY > yOff + 42) {
                tempY = yOff + 42;
            } else if (tempY < yOff) {
                tempY = yOff;
            }
            tempY -= yOff;
            cp.opacity = tempY / 42;
        }
        if (cp.selectingHue) {
            float tempY = y;
            if (tempY > yOff + 42) {
                tempY = yOff + 42;
            } else if (tempY < yOff) {
                tempY = yOff;
            }
            tempY -= yOff;
            cp.hue = tempY / 42;
        }
        if (cp.selectingColor) {
            float tempY = y;
            float tempX = x;
            if (tempY > yOff + 43) {
                tempY = yOff + 43;
            } else if (tempY < yOff) {
                tempY = yOff;
            }
            tempY -= yOff;
            if (tempX > xOff + 43) {
                tempX = xOff + 43;
            } else if (tempX < xOff) {
                tempX = xOff;
            }
            tempX -= xOff;
            cp.brightness = 1 - tempY / 43;
            cp.saturation = tempX / 43;
        }

        if (x > xOff + 57 && y > yOff && x < xOff + 72 && y < yOff + 30) {
            String hex = String.format("#%02X%02X%02X%02X", cp.color.getAlpha(), cp.color.getRed(), cp.color.getGreen(), cp.color.getBlue());
            Client.fss.drawStringWithShadow(hex + String.format(" rgba(%d, %d, %d, %d)", cp.color.getRed(), cp.color.getGreen(), cp.color.getBlue(), cp.color.getAlpha()), (cp.colorPreview.categoryPanel.panel.x + 2 + cp.colorPreview.categoryPanel.panel.dragX) + 55, (cp.colorPreview.categoryPanel.panel.y + 9 + cp.colorPreview.categoryPanel.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
        }

        RenderingUtil.rectangle(xOff + 57, yOff, xOff + 72, yOff + 30, Colors.getColor(255, (int) opacity.getOpacity()));
        boolean offset = false;
        for (int yThing = 0; yThing < 30; yThing += 1) {
            for (int i = offset ? 0 : 1; i < 15; i += 2) {
                RenderingUtil.rectangle(xOff + 57 + i, yOff + yThing, xOff + 57 + i + 1, yOff + yThing + 1, Colors.getColor(190, (int) opacity.getOpacity()));
            }
            offset = !offset;
        }

        float scale = opacity.getOpacity() / 255;
        int colorXD = Colors.getColor(cp.colorPreview.colorObject.getRed(), cp.colorPreview.colorObject.getGreen(), cp.colorPreview.colorObject.getBlue(), (int) (cp.colorPreview.colorObject.getAlpha() * scale));
        RenderingUtil.rectangleBordered(xOff + 59, yOff + 2, xOff + 70, yOff + 28, 0.5f, colorXD, Colors.getColor(0, (int) opacity.getOpacity()));
        GlStateManager.pushMatrix();
        GlStateManager.translate(xOff + 65, yOff + 33, 0);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.enableBlend();
        mc.fontRendererObj.drawStringWithShadow("Copy", 0 - mc.fontRendererObj.getStringWidth("Copy") / 2F, 0, Colors.getColor(255, (int) opacity.getOpacity()));
        mc.fontRendererObj.drawStringWithShadow("Paste", 0 - mc.fontRendererObj.getStringWidth("Paste") / 2F, 12, Colors.getColor(255, (int) opacity.getOpacity()));
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private boolean hovering(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public static String copy() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {

            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf
                            .getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }


    public static void paste(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }

    @Override
    public void colorPickerClick(HSVColorPicker cp, float x, float y, int mouse) {
        if (!cp.colorPreview.categoryPanel.enabled)
            return;
        float xOff = cp.x + cp.colorPreview.categoryPanel.panel.dragX - 85.5f;
        float yOff = cp.y + cp.colorPreview.categoryPanel.panel.dragY + 74;

        if (mouse == 0) {
            try {
                if (hovering(x, y, xOff + 59, yOff + 33, 12, 4)) {
                    String hex = String.format("#%02X%02X%02X%02X", cp.color.getAlpha(), cp.color.getRed(), cp.color.getGreen(), cp.color.getBlue());
                    paste(hex);
                    DevNotifications.getManager().post("Copied to clipboard hex: " + hex);
                }
                if (hovering(x, y, xOff + 59, yOff + 39, 12, 4)) {
                    String hex = copy().trim();
                    if (!Objects.equals(hex, "")) {
                        //Color.decode(hex);
                        String s = hex.replace("#", "").replaceAll("0x", "").trim();
                        if (s.length() >= 6) {
                            String digits = "0123456789ABCDEF";
                            s = s.toUpperCase();
                            int hexValue = 0;
                            for (int i = 0; i < s.length(); i++) {
                                char c = s.charAt(i);
                                int d = digits.indexOf(c);
                                hexValue = 16 * hexValue + d;
                            }


                            int alpha = (hexValue >> 24 & 0xFF);
                            if (s.length() < 8) {
                                alpha = 255;
                            }
                            int red = (hexValue >> 16 & 0xFF);
                            int green = (hexValue >> 8 & 0xFF);
                            int blue = (hexValue & 0xFF);
                            cp.opacity = (float) (alpha / 255F);
                            cp.hue = (Color.RGBtoHSB(red, green, blue, null)[0]);
                            cp.saturation = Color.RGBtoHSB(red, green, blue, null)[1];
                            cp.brightness = Color.RGBtoHSB(red, green, blue, null)[2];
                            cp.color.updateColors(red, green, blue, alpha);
                        }
                    }
                    DevNotifications.getManager().post("Pasted color to " + cp.colorPreview.colorName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                DevNotifications.getManager().post("\247cInvalid color string!");
            }
        }
        if (!cp.selectingHue && !cp.selectingColor && !cp.selectingOpacity && mouse == 0) {
            if (hovering(x, y, xOff + 50, yOff, 3, 43)) cp.selectingOpacity = true;
            if (hovering(x, y, xOff + 45, yOff, 3, 43)) cp.selectingHue = true;
            if (hovering(x, y, xOff, yOff, 43, 43)) cp.selectingColor = true;
        }
    }

    @Override
    public void colorPickerMovedOrUp(HSVColorPicker slider, float x, float y, int mouse) {
        if (mouse == 0 && (slider.selectingHue || slider.selectingColor || slider.selectingOpacity)) {

            ColorCommand.saveStatus();
            slider.selectingOpacity = false;
            slider.selectingColor = false;
            slider.selectingHue = false;
        }
    }

    @Override
    public void SliderMouseMovedOrUp(Slider slider, int mouseX, int mouseY, int mouse, CategoryPanel panel) {
        if (mouse == 0) {
            slider.dragging = false;
        }
    }


    @Override
    public void SliderContructor(Slider p0, CategoryPanel panel) {
        double percent = (((Number) p0.setting.getValue()).doubleValue() - p0.setting.getMin()) / (p0.setting.getMax() - p0.setting.getMin());
        p0.dragX = 40 * percent;
    }

    @Override
    public void SliderDraw(Slider slider, float x, float y, CategoryPanel panel) {
        if (panel.visible) {
            GlStateManager.pushMatrix();
            float xOff = panel.categoryButton.panel.dragX;
            float yOff = panel.categoryButton.panel.dragY;

            final double percent = MathHelper.clamp_double(slider.dragX / 38, 0, 1);
            final double value = MathUtils.getIncremental((percent * 100) * (slider.setting.getMax() - slider.setting.getMin()) / 100 + slider.setting.getMin(), slider.setting.getInc());

            float sliderX = (float) (((((Number) slider.setting.getValue()).doubleValue() - slider.setting.getMin()) / (slider.setting.getMax() - slider.setting.getMin())) * 38);
            RenderingUtil.rectangle(slider.x + xOff - 0.3, slider.y + yOff - 0.3, slider.x + xOff + 38 + 0.3, slider.y + yOff + 2.5 + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
            RenderingUtil.drawGradient(slider.x + xOff, slider.y + yOff, slider.x + xOff + 38, slider.y + yOff + 2.5, Colors.getColor(46, (int) opacity.getOpacity()), Colors.getColor(27, (int) opacity.getOpacity()));

            if (slider.setting.getMin() < 0 && slider.setting.getMax() > 0) {
                if (sliderX >= 19) {
                    RenderingUtil.drawGradient(slider.x + xOff + 19, slider.y + yOff, slider.x + xOff + sliderX, slider.y + yOff + 2.5, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) (120 * (opacity.getOpacity() / 255F))));
                } else {
                    RenderingUtil.drawGradient(slider.x + xOff + sliderX, slider.y + yOff, slider.x + xOff + 19, slider.y + yOff + 2.5, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) (120 * (opacity.getOpacity() / 255F))));
                }
            } else {
                RenderingUtil.drawGradient(slider.x + xOff, slider.y + yOff, slider.x + xOff + sliderX, slider.y + yOff + 2.5, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) opacity.getOpacity()), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, (int) (120 * (opacity.getOpacity() / 255F))));
            }
            boolean hoverMinus = x >= panel.x + xOff + slider.x - 3 && y >= yOff + panel.y + slider.y && x <= xOff + panel.x + slider.x - 0.5 && y <= yOff + panel.y + slider.y + 2;
            boolean hoverPlus = x >= panel.x + xOff + slider.x + 38.5 && y >= yOff + panel.y + slider.y && x <= xOff + panel.x + slider.x + 41 && y <= yOff + panel.y + slider.y + 2;
            RenderingUtil.rectangle(slider.x + xOff - 2.5, slider.y + yOff + 1, slider.x + xOff - 1, slider.y + yOff + 1.5, Colors.getColor(hoverMinus ? 220 : 120, (int) opacity.getOpacity()));
            RenderingUtil.rectangle(slider.x + xOff + 39, slider.y + yOff + 1, slider.x + xOff + 40.5, slider.y + yOff + 1.5, Colors.getColor(hoverPlus ? 220 : 120, (int) opacity.getOpacity()));
            RenderingUtil.rectangle(slider.x + xOff + 39.5, slider.y + yOff + 0.5, slider.x + xOff + 40, slider.y + yOff + 2, Colors.getColor(hoverPlus ? 220 : 120, (int) opacity.getOpacity()));

            String xd = slider.setting.getName().charAt(0) + slider.setting.getName().toLowerCase().substring(1);
            double setting = ((Number) slider.setting.getValue()).doubleValue();
            GlStateManager.pushMatrix();
            String labelText = MathUtils.isInteger(setting) ? (setting + "").replace(".0", "") : setting + "";
            String a = slider.setting.getName().toLowerCase();
            if (a.contains("fov")) {
                labelText = labelText + "°";
            } else if ((a.contains("delay") || a.contains("switch")) && slider.setting.getInc() != 1) {
                labelText = labelText + "ms";
            } else if ((a.contains("delay") && slider.setting.getInc() == 1) || a.equals("existed")) {
                labelText += " tick";
            } else if (a.equals("range")) {
                labelText += "m";
            } else if ((a.equals("horizontal") || a.equals("vertical")) && slider.setting.getMin() == -100 && slider.setting.getMax() == 100) {
                labelText += "%";
            } else if (a.equals("health")) {
                labelText += (slider.setting.getMax() == 20 ? "hp" : "hp");
            }
            if (a.equalsIgnoreCase("Mxaxaps")) {
                xd = "Maxaps";
            }
            float strWidth = Client.fs.getWidth(labelText);
            Client.fsmallbold.drawBorderedString(labelText, (slider.x + xOff + 42) - strWidth, (slider.y - 6 + yOff), Colors.getColor(220, (int) opacity.getOpacity()), Colors.getColor(0, (int) opacity.getOpacity()));
            GlStateManager.scale(1, 1, 1);
            GlStateManager.popMatrix();
            Client.fss.drawStringWithShadow(xd, (slider.x + xOff), (slider.y - 6 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));

            if (slider.dragging) {
                float divide = Math.abs((y - (slider.y + yOff + 2))) / 4;
                if (divide < 1) {
                    divide = 1;
                }

                double mouseDiff = (x - slider.lastDragX);

                slider.dragX = slider.dragX + (mouseDiff / divide);
                slider.lastDragX = x;

                Object newValue = (StringConversions.castNumber(Double.toString(value), slider.setting.getInc()));
                slider.setting.setValue(newValue);
            }
            if (((Number) slider.setting.getValue()).doubleValue() <= slider.setting.getMin()) {
                Object newValue = (StringConversions.castNumber(Double.toString(slider.setting.getMin()), slider.setting.getInc()));
                slider.setting.setValue(newValue);
            }
            if (((Number) slider.setting.getValue()).doubleValue() >= slider.setting.getMax()) {
                Object newValue = (StringConversions.castNumber(Double.toString(slider.setting.getMax()), slider.setting.getInc()));
                slider.setting.setValue(newValue);
            }

            if ((x >= xOff + slider.x && y >= yOff + slider.y - 6 && x <= xOff + slider.x + 38 && y <= yOff + slider.y + 3)) {
                Client.fss.drawStringWithShadow(getDescription(slider.setting) + " Min: " + slider.setting.getMin() + " " + "Max: " + slider.setting.getMax(), (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 9 + panel.categoryButton.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void SliderMouseClicked(Slider slider, int mouseX, int mouseY, int mouse, CategoryPanel panel) {
        float xOff = panel.categoryButton.panel.dragX;
        float yOff = panel.categoryButton.panel.dragY;
        if (panel.visible && mouseX >= panel.x + xOff + slider.x && mouseY >= yOff + panel.y + slider.y - 6 && mouseX <= xOff + panel.x + slider.x + 38.0f && mouseY <= yOff + panel.y + slider.y + 3.5F && mouse == 0) {
            slider.dragging = true;
            slider.lastDragX = mouseX;
            slider.dragX = (mouseX - (slider.x + xOff));
        }
        // -Increment
        if (panel.visible && mouseX >= panel.x + xOff + slider.x - 3 && mouseY >= yOff + panel.y + slider.y && mouseX <= xOff + panel.x + slider.x - 0.5 && mouseY <= yOff + panel.y + slider.y + 2 && mouse == 0) {
            Setting setting = slider.setting;
            double value = ((Number) setting.getValue()).doubleValue();
            if (value - setting.getInc() >= setting.getMin()) {
                setting.setValue(MathUtils.getIncremental(value - setting.getInc(), setting.getInc()));
            } else {
                setting.setValue(setting.getMin());
            }
        } else // +Increment
            if (panel.visible && mouseX >= panel.x + xOff + slider.x + 38.5 && mouseY >= yOff + panel.y + slider.y && mouseX <= xOff + panel.x + slider.x + 41 && mouseY <= yOff + panel.y + slider.y + 2 && mouse == 0) {
                Setting setting = slider.setting;
                double value = ((Number) setting.getValue()).doubleValue();
                if (value + setting.getInc() <= setting.getMax()) {
                    setting.setValue(MathUtils.getIncremental(value + setting.getInc(), setting.getInc()));
                } else {
                    setting.setValue(setting.getMax());
                }
            }
    }

    @Override
    public void textBoxDraw(TextBox textBox, float x, float y) {
        CategoryPanel panel = textBox.panel;
        float xOff = panel.categoryButton.panel.dragX;
        float yOff = panel.categoryButton.panel.dragY;//40
        if (textBox.cursorPos > textBox.textString.length()) {
            textBox.cursorPos = textBox.textString.length();
        } else if (textBox.cursorPos < 0) {
            textBox.cursorPos = 0;
        }
        if (!textBox.isFocused && !textBox.isTyping && !textBox.textString.equals((String) textBox.setting.getValue())) {
            textBox.textString = (String) textBox.setting.getValue();
        }
        int selectedChar = textBox.cursorPos;
        boolean hovering = (x >= xOff + textBox.x) && (y >= yOff + textBox.y)
                && (x <= xOff + textBox.x + 84) && (y <= yOff + textBox.y + 9);

        RenderingUtil.rectangle(textBox.x + xOff - 0.3, textBox.y + yOff - 0.3, textBox.x + xOff + 84 + 0.3, textBox.y + yOff + 7.5F + 0.3, Colors.getColor(10, (int) opacity.getOpacity()));
        RenderingUtil.drawGradient(textBox.x + xOff, textBox.y + yOff, textBox.x + xOff + 84, textBox.y + yOff + 7.5F, Colors.getColor(31, (int) opacity.getOpacity()), Colors.getColor(36, (int) opacity.getOpacity()));
        if (hovering || textBox.isFocused) {
            RenderingUtil.rectangleBordered(textBox.x + xOff, textBox.y + yOff, textBox.x + xOff + 84, textBox.y + yOff + 7.5F, 0.3, Colors.getColor(0, 0), textBox.isFocused ? Colors.getColor(130, (int) opacity.getOpacity()) : Colors.getColor(90, (int) opacity.getOpacity()));
        }
        String xd = textBox.setting.getName().charAt(0) + textBox.setting.getName().toLowerCase().substring(1);
        Client.fss.drawStringWithShadow(xd, (textBox.x + xOff + 1), (textBox.y - 6 + yOff), Colors.getColor(220, (int) opacity.getOpacity()));

        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(textBox.x + xOff + 2, textBox.y + yOff, textBox.x + xOff + 82, textBox.y + yOff + 7.5F, Colors.getColor(90, (int) opacity.getOpacity()));
        Depth.render();
        Client.fss.drawString(textBox.textString, (textBox.x + 1.5F + xOff) - textBox.offset, (textBox.y + 2 + yOff), Colors.getColor(151, (int) opacity.getOpacity()));
        Depth.post();

        if (textBox.opacity.getOpacity() >= 270) {
            textBox.backwards = true;
        } else if (textBox.opacity.getOpacity() <= 40) {
            textBox.backwards = false;
        }
        textBox.opacity.interp(textBox.backwards ? 40 : 270, 7);

        if (textBox.isFocused) {
            float width = Client.fss.getWidth(textBox.textString.substring(0, selectedChar));
            float posX = textBox.x + xOff + width - textBox.offset;
            RenderingUtil.rectangle(posX - 0.5, textBox.y + yOff + 1.5, posX, textBox.y + yOff + 6, Colors.getColor(220, (int) textBox.opacity.getOpacity()));
        } else {
            textBox.opacity.setOpacity(255);
        }

        if (hovering) {
            Client.fss.drawStringWithShadow(getDescription(textBox.setting), (panel.categoryButton.panel.x + 2 + panel.categoryButton.panel.dragX) + 55, (panel.categoryButton.panel.y + 7.5F + panel.categoryButton.panel.dragY), Colors.getColor(255, (int) opacity.getOpacity()));
        }

    }

    @Override
    public void textBoxMouseClicked(TextBox textBox, int x, int y, int mouseID) {
        CategoryPanel panel = textBox.panel;
        float xOff = panel.categoryButton.panel.dragX;
        float yOff = panel.categoryButton.panel.dragY;//40
        boolean hovering = (x >= xOff + textBox.x) && (y >= yOff + textBox.y) && (x <= xOff + textBox.x + 84) && (y <= yOff + textBox.y + 9);

        if (hovering && mouseID == 0 && !textBox.isFocused) {
            float width = Client.fss.getWidth(textBox.textString.substring(0, textBox.cursorPos));
            float barOffset = (width - textBox.offset);
            if (barOffset < 0) {
                textBox.offset += barOffset;
            }

            if (barOffset > 82) {
                textBox.offset += (barOffset - 82);
            }

            textBox.isFocused = true;
            Keyboard.enableRepeatEvents(true);
            Keyboard.enableRepeatEvents(true);

            String currentString = textBox.textString;
            float mouseOffsetAdjusted = x - (xOff + textBox.x) - 2;
            textBox.cursorPos = Client.fss.trimStringToWidth(currentString, textBox.offset + (mouseOffsetAdjusted), false).length();
        } else {
            if (!hovering) {
                textBox.isFocused = false;
                textBox.isTyping = false;
                ModuleManager.saveSettings();
            } else if (mouseID == 0) {
                String currentString = textBox.textString;
                float mouseOffsetAdjusted = x - (xOff + textBox.x) - 2.5F;
                textBox.cursorPos = Client.fss.trimStringToWidth(currentString, textBox.offset + (mouseOffsetAdjusted), false).length();
            }
        }
    }

    @Override
    public void textBoxKeyPressed(TextBox textBox, int key) {
        char letter = Keyboard.getEventCharacter();
        if (letter == '\r') {
            textBox.isFocused = false;
            textBox.isTyping = false;
            textBox.setting.setValue(textBox.textString);
            ModuleManager.saveSettings();
            return;
        }

        if (textBox.isFocused) {
            if (GuiScreen.isKeyComboCtrlC(key)) {
                GuiScreen.setClipboardString(textBox.textString);
                return;
            }
            if (GuiScreen.isKeyComboCtrlV(key)) {
                String oldString = textBox.textString;

                StringBuilder stringBuilder = new StringBuilder(oldString);
                String input = ChatAllowedCharacters.filterAllowedCharacters(GuiScreen.getClipboardString());
                stringBuilder.insert(textBox.cursorPos, input);

                textBox.textString = ChatAllowedCharacters.filterAllowedCharacters(stringBuilder.toString());

                textBox.cursorPos += input.length();

                textBox.setting.setValue(textBox.textString);
                return;
            } else {

                switch (key) {
                    case Keyboard.KEY_LEFT: {

                        if (textBox.cursorPos > 0) {
                            if (GuiScreen.isShiftKeyDown())
                                textBox.cursorPos = 0;
                            else
                                textBox.cursorPos--;
                        }
                        float width = Client.fss.getWidth(textBox.textString.substring(0, textBox.cursorPos));
                        float barOffset = (width - textBox.offset);
                        barOffset -= 2;
                        if (barOffset < 0) {
                            textBox.offset += barOffset;
                        }
                        break;
                    }
                    case Keyboard.KEY_RIGHT: {

                        if (textBox.cursorPos < textBox.textString.length()) {
                            if (GuiScreen.isShiftKeyDown())
                                textBox.cursorPos = textBox.textString.length();
                            else
                                textBox.cursorPos++;
                        }
                        float width = Client.fss.getWidth(textBox.textString.substring(0, textBox.cursorPos));
                        float barOffset = (width - textBox.offset);
                        if (barOffset > 82) {
                            textBox.offset += (barOffset - 82);
                        }
                        break;
                    }
                    case Keyboard.KEY_DOWN: {
                        textBox.cursorPos = textBox.textString.length();
                        float width = Client.fss.getWidth(textBox.textString.substring(0, textBox.cursorPos));
                        float barOffset = (width - textBox.offset);
                        if (barOffset > 82) {
                            textBox.offset += (barOffset - 82);
                        }
                        break;
                    }
                    case Keyboard.KEY_UP: {
                        textBox.cursorPos = 0;
                        textBox.offset = 0;
                        break;
                    }
                    case Keyboard.KEY_BACK: {
                        try {
                            if (textBox.textString.length() == 0)
                                break;
                            String oldString = textBox.textString;

                            StringBuilder stringBuilder = new StringBuilder(oldString);
                            char del = stringBuilder.charAt(textBox.cursorPos - 1);
                            stringBuilder.deleteCharAt(textBox.cursorPos - 1);
                            textBox.textString = ChatAllowedCharacters.filterAllowedCharacters(stringBuilder.toString());
                            textBox.cursorPos--;
                            if (Client.fss.getWidth(oldString) > 82 && textBox.offset > 0) {
                                float newTextWidth = Client.fss.getWidth(textBox.textString);
                                float oldTextWidth = Client.fss.getWidth(oldString);
                                float charWidth = newTextWidth - oldTextWidth;
                                if (newTextWidth <= 82 && oldTextWidth - 82 > charWidth) {
                                    charWidth = 82 - oldTextWidth;
                                }

                                textBox.offset += charWidth;
                            }
                            if (textBox.cursorPos > textBox.textString.length()) {
                                textBox.cursorPos = textBox.textString.length();
                            }
                            textBox.setting.setValue(textBox.textString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }

        if (textBox.isFocused && ChatAllowedCharacters.isAllowedCharacter(letter)) {
            if (!Keyboard.areRepeatEventsEnabled())
                Keyboard.enableRepeatEvents(true);

            if (!textBox.isTyping)
                textBox.isTyping = true;

            String oldString = textBox.textString;

            StringBuilder stringBuilder = new StringBuilder(oldString);
            stringBuilder.insert(textBox.cursorPos, letter);

            textBox.textString = ChatAllowedCharacters.filterAllowedCharacters(stringBuilder.toString());

            if (textBox.cursorPos > textBox.textString.length()) {
                textBox.cursorPos = textBox.textString.length();
            } else if (textBox.cursorPos == oldString.length() && textBox.textString.startsWith(oldString)) {
                textBox.cursorPos += textBox.textString.length() - oldString.length();
            } else {
                textBox.cursorPos++;
                float width = Client.fss.getWidth(textBox.textString.substring(0, textBox.cursorPos));
                float barOffset = (width - textBox.offset);
                if (barOffset > 82) {
                    textBox.offset += (barOffset - 82);
                }
            }
            TTFFontRenderer f = Client.fss;
            float newTextWidth = f.getWidth(textBox.textString);
            float oldTextWidth = f.getWidth(oldString);
            if (newTextWidth > 82) {
                if (oldTextWidth < 82) {
                    oldTextWidth = 82;
                }
                float charWidth = (newTextWidth - oldTextWidth);
                if (textBox.cursorPos == textBox.textString.length())
                    textBox.offset += charWidth;
            }

            textBox.setting.setValue(textBox.textString);
        }

    }

    private String getDescription(Setting setting) {
        if (setting.getDesc() != null && !setting.getDesc().equalsIgnoreCase("")) {
            return setting.getDesc();
        }
        return "ERROR: No Description Found.";
    }

}
