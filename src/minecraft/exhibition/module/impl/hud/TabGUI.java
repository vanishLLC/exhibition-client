package exhibition.module.impl.hud;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventKeyPress;
import exhibition.event.impl.EventRenderGui;
import exhibition.event.impl.EventTick;
import exhibition.management.ColorManager;
import exhibition.management.animate.Expand;
import exhibition.management.animate.Translate;
import exhibition.management.font.TTFFontRenderer;
import exhibition.module.Module;
import exhibition.module.ModuleManager;
import exhibition.module.data.ModuleData;
import exhibition.module.data.MultiBool;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.StringConversions;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabGUI extends Module {

    private int selectedTypeX, selectedModuleY, selectedSetY, moduleBoxY, currentSetting,
            settingBoxX, categoryBoxY, categoryBoxX, currentCategory, targetY, targetModY, targetSetY, moduleBoxX,
            targetX, targetSetX;
    private boolean inModules, inModSet, inSet;
    private Module selectedModule;
    private Timer timer = new Timer();
    private int opacity = 45;
    private int targetOpacity = 45;
    private boolean isActive;

    public TabGUI(ModuleData data) {
        super(data);
    }

    private Translate selectedType = new Translate(0, 14);
    private Translate selectedModuleT = new Translate(0, 14);
    private Translate selectedSettingT = new Translate(0, 14);
    private Expand moduleExpand;

    @Override
    public void onEnable() {
        targetY = 12;
        categoryBoxY = 0;
        currentCategory = 0;
        inModules = false;
        inModSet = false;
        inSet = false;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGH;
    }

    /*
     * (non-Javadoc)
     *
     * @see EventListener#onEvent(Event)
     */
    @RegisterEvent(events = {EventRenderGui.class, EventTick.class, EventKeyPress.class})
    public void onEvent(Event event) {
        if (mc.gameSettings.showDebugInfo) return;
        TTFFontRenderer font = Client.nametagsFont;

        if (event instanceof EventKeyPress) {
            EventKeyPress ek = (EventKeyPress) event;
            if (!isActive && this.keyCheck(ek.getKey())) {
                isActive = true;
                targetOpacity = 200;
                timer.reset();
            }
            if (isActive && this.keyCheck(ek.getKey())) {
                timer.reset();
            }
            if (!inModules) {
                if (ek.getKey() == Keyboard.KEY_DOWN) {
                    targetY += 12;
                    currentCategory++;
                    if (currentCategory > ModuleData.Type.values().length - 1) {
                        targetY = 14;
                        currentCategory = 0;
                    }
                } else if (ek.getKey() == Keyboard.KEY_UP) {
                    targetY -= 12;
                    currentCategory--;
                    if (currentCategory < 0) {
                        targetY = categoryBoxY - 11;
                        currentCategory = ModuleData.Type.values().length - 1;
                    }
                } else if (ek.getKey() == Keyboard.KEY_RIGHT) {
                    inModules = true;
                    // ChatUtil.printChat("Modules in " +
                    // ModuleData.Type.values()[currentCategory] + ":" +
                    // getModules(ModuleData.Type.values()[currentCategory]));
                    moduleBoxY = 0;
                    selectedModuleT.setY(14);
                    targetModY = 14;
                    int longestString = 0;
                    for (Module modxd : Client.getModuleManager().getArray()) {
                        if (modxd.getType() == ModuleData.Type.values()[currentCategory]) {
                            if (longestString < font.getWidth(modxd.getName())) {
                                longestString = (int) font.getWidth(modxd.getName());
                            }
                        }
                    }
                    targetX = categoryBoxX + 3 + longestString + 7;
                    moduleBoxX = categoryBoxX + 3;
                }
            } else if (!inModSet) {
                if (ek.getKey() == Keyboard.KEY_LEFT) {
                    targetX = categoryBoxX + 3;
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(110);
                        } catch (InterruptedException e) {
                        }
                        inModules = false;
                    });
                    thread.start();
                }
                if (ek.getKey() == Keyboard.KEY_DOWN) {
                    targetModY += 12;
                    moduleBoxY++;
                    // ChatUtil.printChat("Current Module Index: " +
                    // moduleBoxY
                    // + " Current Module Size: " +
                    // getModules(ModuleData.Type.values()[currentCategory]).size());
                    if (moduleBoxY > getModules(ModuleData.Type.values()[currentCategory]).size() - 1) {
                        targetModY = 14;
                        moduleBoxY = 0;
                    }
                } else if (ek.getKey() == Keyboard.KEY_UP) {
                    targetModY -= 12;
                    moduleBoxY--;
                    if (moduleBoxY < 0) {
                        targetModY = (((getModules(ModuleData.Type.values()[currentCategory]).size() - 1) * 12) + 14);
                        moduleBoxY = getModules(ModuleData.Type.values()[currentCategory]).size() - 1;
                    }
                } else if (ek.getKey() == Keyboard.KEY_RETURN) {
                    try {
                        Module mod = getModules(ModuleData.Type.values()[currentCategory]).get(moduleBoxY);
                        mod.toggle();
                    } catch (Exception e) {
                        ChatUtil.printChat(getModules(ModuleData.Type.values()[currentCategory]).size() + ", "
                                + moduleBoxY + ", ");
                    }
                } else if (ek.getKey() == Keyboard.KEY_RIGHT) {
                    selectedModule = getModules(ModuleData.Type.values()[currentCategory]).get(moduleBoxY);

                    if (!(getSettings(selectedModule) == null)) {
                        inModSet = true;
                        selectedSettingT.setY(14);
                        targetSetY = 14;
                        currentSetting = 0;
                        int longestString = 0;
                        for (Setting modxd : getSettings(selectedModule)) {
                            String faggotXD = modxd.getValue() instanceof Options ? ((Options) modxd.getValue()).getSelected() : modxd.getValue() instanceof MultiBool ? "N/A" :modxd.getValue().toString();
                            if (longestString < font.getWidth(modxd.getName() + ": \247l" + faggotXD)) {
                                longestString = (int) font.getWidth(modxd.getName() + ": \247l" + faggotXD);
                            }
                        }
                        targetSetX = moduleBoxX + longestString + 5;
                        settingBoxX = moduleBoxX + 1;
                    }
                }
            } else if (!inSet) {
                if (ek.getKey() == Keyboard.KEY_LEFT) {
                    targetSetX = moduleBoxX + 1;
                    Thread thread = new Thread(() -> {
                        try {
                            Thread.sleep(110);
                        } catch (InterruptedException e) {
                        }
                        inModSet = false;
                        selectedModule = null;
                    });
                    thread.start();
                } else if (ek.getKey() == Keyboard.KEY_DOWN) {
                    targetSetY += 12;
                    currentSetting++;
                    if (currentSetting > getSettings(selectedModule).size() - 1) {
                        currentSetting = 0;
                        targetSetY = 14;
                    }
                } else if (ek.getKey() == Keyboard.KEY_UP) {
                    targetSetY -= 12;
                    currentSetting--;
                    if (currentSetting < 0) {
                        targetSetY = ((getSettings(selectedModule).size() - 1) * 12) + 14;
                        currentSetting = getSettings(selectedModule).size() - 1;
                    }
                } else if (ek.getKey() == Keyboard.KEY_RIGHT) {
                    inSet = true;
                }
            } else if (inSet) {
                if (ek.getKey() == Keyboard.KEY_LEFT) {
                    inSet = !inSet;
                } else if (ek.getKey() == Keyboard.KEY_UP) {
                    Setting set = getSettings(selectedModule).get(currentSetting);
                    if (set.getValue() instanceof Number) {
                        double increment = (set.getInc());
                        String str = MathUtils.isInteger(MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() + increment), increment)) ?
                                (MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() + increment), increment) + "").replace(".0", "") : MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() + increment), increment) + "";
                        if (Double.parseDouble(str) > set.getMax() && set.getInc() != 0) {
                            return;
                        }
                        Object newValue = (StringConversions.castNumber(str, increment));
                        if (newValue != null) {
                            set.setValue(newValue);
                            ModuleManager.saveSettings();
                            return;
                        }
                    } else if (set.getValue().getClass().equals(Boolean.class)) {
                        boolean xd = ((Boolean) set.getValue());
                        set.setValue(!xd);
                        ModuleManager.saveSettings();
                    } else if (set.getValue() instanceof Options) {
                        List<String> options = new ArrayList<>();
                        Collections.addAll(options, ((Options) set.getValue()).getOptions());
                        for (int i = 0; i <= options.size() - 1; i++) {
                            if (options.get(i).equalsIgnoreCase(((Options) set.getValue()).getSelected())) {
                                if (i + 1 > options.size() - 1) {
                                    ((Options) set.getValue()).setSelected(options.get(0));
                                } else {
                                    ((Options) set.getValue()).setSelected(options.get(i + 1));
                                }
                                break;
                            }
                        }
                    }
                } else if (ek.getKey() == Keyboard.KEY_DOWN) {
                    Setting set = getSettings(selectedModule).get(currentSetting);
                    if (set.getValue() instanceof Number) {
                        double increment = (set.getInc());

                        String str = MathUtils.isInteger(MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() - increment), increment)) ?
                                (MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() - increment), increment) + "").replace(".0", "") : MathUtils.getIncremental((((Number) (set.getValue())).doubleValue() - increment), increment) + "";
                        if (Double.parseDouble(str) < set.getMin() && increment != 0) {
                            return;
                        }
                        Object newValue = (StringConversions.castNumber(str, increment));
                        if (newValue != null) {
                            set.setValue(newValue);
                            ModuleManager.saveSettings();
                            return;
                        }
                    } else if (set.getValue().getClass().equals(Boolean.class)) {
                        boolean xd = ((Boolean) set.getValue());
                        set.setValue(!xd);
                        ModuleManager.saveSettings();
                    } else if (set.getValue() instanceof Options) {
                        List<String> options = new ArrayList<>();
                        Collections.addAll(options, ((Options) set.getValue()).getOptions());
                        for (int i = options.size() - 1; i >= 0; i--) {
                            if (options.get(i).equalsIgnoreCase(((Options) set.getValue()).getSelected())) {
                                if (i - 1 < 0) {
                                    ((Options) set.getValue()).setSelected(options.get(options.size() - 1));
                                } else {
                                    ((Options) set.getValue()).setSelected(options.get(i - 1));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (event instanceof EventTick) {
            if (categoryBoxY == 0) {
                int y = 13;
                int largestString = -1;
                for (ModuleData.Type type : ModuleData.Type.values()) {
                    y += 12;
                    if (Minecraft.getMinecraft().fontRendererObj.getStringWidth(type.name()) > largestString) {
                        largestString = Minecraft.getMinecraft().fontRendererObj.getStringWidth(type.name());
                    }
                }
                categoryBoxY = y;
                categoryBoxX = largestString + 7;
                selectedTypeX = 2;
                targetY = 14;
            }
        }
        if (event instanceof EventRenderGui) {
            EventRenderGui er = (EventRenderGui) event;
            if (timer.delay(4500)) {
                targetOpacity = 30;
                isActive = false;
            }
            int diff3 = (targetX) - (moduleBoxX);
            int diff5 = (targetSetX) - settingBoxX;
            int opacityDiff = (targetOpacity) - (opacity);
            opacity += opacityDiff * 0.1;

            selectedType.interpolate(selectedTypeX, targetY, 0.35F);
            selectedModuleT.interpolate(categoryBoxX + 3, targetModY, 0.35F);
            selectedSettingT.interpolate(0, targetSetY, 0.35F);
            moduleBoxX += MathUtils.roundToPlace(diff3 * 0.25, 0);
            if (diff3 == 1) {
                moduleBoxX++;
            } else if (diff3 == -1) {
                moduleBoxX--;
            }
            settingBoxX += MathUtils.roundToPlace(diff5 * 0.25, 0);
            if (diff5 == 1) {
                settingBoxX++;
            } else if (diff5 == -1) {
                settingBoxX--;
            }

            // GlStateManager.pushMatrix();
            // GlStateManager.scale(0.9, 0.9, 0.9);
            RenderingUtil.rectangle(2, 14, categoryBoxX + 2, categoryBoxY + 1, Colors.getColor(0, 0, 0, opacity));
            RenderingUtil.rectangle(selectedTypeX + 0.3, selectedType.getY() + 0.3, categoryBoxX + 2 - 0.3, selectedType.getY() + 12 - 0.3, Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, opacity + 64));
            int y = 15;
            for (ModuleData.Type type : ModuleData.Type.values()) {
                boolean isSelected = Math.abs(y - selectedType.getY()) < 6 || y - selectedType.getY() == 6;
                // Client.cf.drawString(type.name(), isSelected ? 7 : 5, y + 1,
                // Colors.getColor(0, 0, 0, 200));
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                font.drawStringWithShadow(type.name(), isSelected ? 6 : 4, y + 0.5F, Colors.getColor(175, opacity + 64));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                y += 12;
            }
            y = 15;

            Depth.pre();
            Depth.mask();
            RenderingUtil.rectangle(selectedTypeX + 0.3, selectedType.getY() - 1, categoryBoxX + 2 - 0.3, selectedType.getY() + 13, Colors.getColor(255, 255));
            Depth.render(GL11.GL_EQUAL);
            for (ModuleData.Type type : ModuleData.Type.values()) {
                boolean isSelected = Math.abs(y - selectedType.getY()) < 6 || y - selectedType.getY() == 6;
                font.drawString(type.name(), isSelected ? 6 : 4, y, Colors.getColor(255, opacity + 64));
                y += 12;
            }
            Depth.post();


            if (inModules) {
                List<Module> xd = getModules(ModuleData.Type.values()[currentCategory]);
                y = 15;
                RenderingUtil.rectangle(categoryBoxX + 3, 14, moduleBoxX, ((xd.size()) * 12) + 14,
                        Colors.getColor(0, 0, 0, opacity));
                RenderingUtil.rectangle(categoryBoxX + 3 + 0.3, selectedModuleT.getY() + 0.3, moduleBoxX - 0.3, selectedModuleT.getY() + 12 - 0.3,
                        Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, opacity + 64));
                if (diff3 == 0 && moduleBoxX > categoryBoxX + 3) {
                    for (Module mod : getModules(ModuleData.Type.values()[currentCategory])) {
                        if (!(getSettings(mod) == null) && !inModSet) {
                            RenderingUtil.rectangle(moduleBoxX + 1, y, moduleBoxX + 11, y + 10,
                                    Colors.getColor(0, 0, 0, opacity));
                            GlStateManager.pushMatrix();
                            GlStateManager.enableBlend();
                            font.drawStringWithShadow("+", moduleBoxX + 2f, y + 0.5f,
                                    Colors.getColor(255, opacity + 64));
                            GlStateManager.disableBlend();
                            GlStateManager.popMatrix();
                        }
                        boolean isSelected = Math.abs(y - selectedModuleT.getY()) < 6 || y - selectedModuleT.getY() == 6;
                        // Client.cf.drawString(mod.getName(), categoryBoxX +
                        // (isSelected ? 9 : 7), y + 1,Colors.getColor(0, 0, 0,
                        // 200));
                        GlStateManager.pushMatrix();
                        GlStateManager.enableBlend();
                        font.drawStringWithShadow(mod.getName(), categoryBoxX + (isSelected ? 8 : 6), y,
                                mod.isEnabled() ? Colors.getColor(255, opacity + 64)
                                        : Colors.getColor(175, opacity + 64));
                        GlStateManager.disableBlend();
                        GlStateManager.popMatrix();
                        y += 12;
                    }
                }
            }
            if (inModSet) {
                RenderingUtil.rectangle(moduleBoxX + 1, 14, settingBoxX, 14 + selectedModule.getSettings().size() * 12,
                        Colors.getColor(0, 0, 0, opacity));
                RenderingUtil.rectangle(moduleBoxX + 1 + 0.3, selectedSettingT.getY() + 0.3, settingBoxX - 0.3, selectedSettingT.getY() + 12 - 0.3,
                        Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, opacity + 64));
                int y1 = 15;
                try {
                    for (Setting setting : selectedModule.getSettings().values()) {
                        if (setting != null && diff5 == 0 && settingBoxX > moduleBoxX + 3) {
                            boolean isSelected = Math.abs(y1 - selectedSettingT.getY()) < 6 || y1 - selectedSettingT.getY() == 6;
                            String xd = setting.getName().charAt(0) + setting.getName().toLowerCase().substring(1);
                            String fagniger = setting.getValue() instanceof Options ? ((Options) setting.getValue()).getSelected() : setting.getValue() instanceof MultiBool ? "N/A" :setting.getValue().toString();
                            font.drawStringWithShadow(xd + ": \247l" + fagniger, moduleBoxX + (isSelected ? 6 : 4), y1, Colors.getColor(175, opacity + 64));
                            y1 += 12;
                        }
                    }

                    Depth.pre();
                    Depth.mask();
                    RenderingUtil.rectangle(moduleBoxX + 1 + 0.3, selectedSettingT.getY() + 0.3, settingBoxX - 0.3, selectedSettingT.getY() + 12 - 0.3, Colors.getColor(255, 255));
                    Depth.render(GL11.GL_EQUAL);
                    y1 = 15;
                    for (Setting setting : selectedModule.getSettings().values()) {
                        if (setting != null && diff5 == 0 && settingBoxX > moduleBoxX + 3) {
                            boolean isSelected = Math.abs(y1 - selectedSettingT.getY()) < 6 || y1 - selectedSettingT.getY() == 6;
                            String fagniger = setting.getValue() instanceof Options ? ((Options) setting.getValue()).getSelected() : setting.getValue() instanceof MultiBool ? "N/A" :setting.getValue().toString();
                            font.drawStringWithShadow(setting.getName().charAt(0) + setting.getName().toLowerCase().substring(1) + ": \247l" + fagniger, moduleBoxX + (isSelected ? 6 : 4), y1, Colors.getColor(255, opacity + 64));
                            y1 += 12;
                        }
                    }
                    Depth.post();
                } catch (Exception e) {
                }
            }
            if (inSet) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                RenderingUtil.rectangleBordered(settingBoxX + 1, selectedSettingT.getY() + 1, settingBoxX + 11, selectedSettingT.getY() + 11, 0.5, Colors.getColor(0, opacity), Colors.getColor(ColorManager.hudColor.red, ColorManager.hudColor.green, ColorManager.hudColor.blue, opacity + 64));
                font.drawStringWithShadow("\247l<", settingBoxX + 2f, selectedSettingT.getY() + 1.5f,
                        Colors.getColor(255, 255, 255, opacity + 64));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private boolean keyCheck(int key) {
        boolean active = false;
        switch (key) {
            case Keyboard.KEY_DOWN:
                active = true;
                break;
            case Keyboard.KEY_UP:
                active = true;
                break;
            case Keyboard.KEY_RETURN:
                active = true;
                break;
            case Keyboard.KEY_LEFT:
                active = true;
                break;
            case Keyboard.KEY_RIGHT:
                active = true;
                break;
            default:
                break;
        }
        return active;
    }

    private List<Setting> getSettings(Module mod) {
        List<Setting> settings = new ArrayList<>();
        settings.addAll(mod.getSettings().values());
        if (settings.isEmpty()) {
            return null;
        }
        return settings;
    }

    private List<Module> getModules(ModuleData.Type type) {
        List<Module> modulesInType = new ArrayList<>();
        float width = 0;
        TTFFontRenderer font = Client.nametagsFont;
        for (Module mod : Client.getModuleManager().getArray()) {
            if (mod.getType() == type) {
                modulesInType.add(mod);
                if (font.getWidth(mod.getName()) > width) {
                    width = font.getWidth(mod.getName());
                    selectedModuleT.setX(font.getWidth(mod.getName()));
                }
            }
        }
        if (modulesInType.isEmpty()) {
            return null;
        }
        modulesInType.sort(Comparator.comparing(Module::getName));
        return modulesInType;
    }
}
