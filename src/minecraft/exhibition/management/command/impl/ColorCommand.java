package exhibition.management.command.impl;

import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.command.Command;
import exhibition.util.FileUtils;
import exhibition.util.misc.ChatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cool1 on 1/13/2017.
 */
public class ColorCommand extends Command {

    private static final File COLOR_DIR = FileUtils.getConfigFile("Colors");

    public static void saveStatus() {
        List<String> fileContent = new ArrayList<>();
        fileContent.add(String.format("%s:%s:%s:%s:%s", "friendlyVisible", ColorManager.fVis.getRed(), ColorManager.fVis.getGreen(), ColorManager.fVis.getBlue(), ColorManager.fVis.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "friendlyInvisible", ColorManager.fInvis.getRed(), ColorManager.fInvis.getGreen(), ColorManager.fInvis.getBlue(), ColorManager.fInvis.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "enemyVisible", ColorManager.eVis.getRed(), ColorManager.eVis.getGreen(), ColorManager.eVis.getBlue(), ColorManager.eVis.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "enemyInvisible", ColorManager.eInvis.getRed(), ColorManager.eInvis.getGreen(), ColorManager.eInvis.getBlue(), ColorManager.eInvis.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "friendlyTeam", ColorManager.fTeam.getRed(), ColorManager.fTeam.getGreen(), ColorManager.fTeam.getBlue(), ColorManager.fTeam.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "enemyTeam", ColorManager.eTeam.getRed(), ColorManager.eTeam.getGreen(), ColorManager.eTeam.getBlue(), ColorManager.eTeam.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "hudColor", ColorManager.hudColor.getRed(), ColorManager.hudColor.getGreen(), ColorManager.hudColor.getBlue(), ColorManager.hudColor.getAlpha()));
        fileContent.add(String.format("%s:%s:%s:%s:%s", "xhair", ColorManager.xhair.getRed(), ColorManager.xhair.getGreen(), ColorManager.xhair.getBlue(), ColorManager.xhair.getAlpha()));
        ColorObject c = ColorManager.chamsVis;
        fileContent.add(String.format("%s:%s:%s:%s:%s", "chamsVis", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
        c = ColorManager.chamsInvis;
        fileContent.add(String.format("%s:%s:%s:%s:%s", "chamsInvis", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));

        fileContent.add(String.format("%s:%s:%s:%s:%s", "chestESP", (c = ColorManager.chestESPColor).getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));

        fileContent.add(String.format("%s:%s:%s:%s:%s", "targetStrafe", (c = ColorManager.strafeColor).getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));


        FileUtils.write(COLOR_DIR, fileContent, true);
    }

    public static void loadStatus() {
        try {
            List<String> fileContent = FileUtils.read(COLOR_DIR);
            for (String line : fileContent) {
                String[] split = line.split(":");
                String object = split[0];
                int red = Integer.parseInt(split[1]);
                int green = Integer.parseInt(split[2]);
                int blue = Integer.parseInt(split[3]);
                int alpha = Integer.parseInt(split[4]);
                switch (object) {
                    case "friendlyVisible":
                        ColorManager.fVis.updateColors(red, green, blue, alpha);
                        break;
                    case "friendlyInvisible":
                        ColorManager.fInvis.updateColors(red, green, blue, alpha);
                        break;
                    case "enemyVisible":
                        ColorManager.eVis.updateColors(red, green, blue, alpha);
                        break;
                    case "enemyInvisible":
                        ColorManager.eInvis.updateColors(red, green, blue, alpha);
                        break;
                    case "friendlyTeam":
                        ColorManager.fTeam.updateColors(red, green, blue, alpha);
                        break;
                    case "enemyTeam":
                        ColorManager.eTeam.updateColors(red, green, blue, alpha);
                        break;
                    case "hudColor":
                        ColorManager.hudColor.updateColors(red, green, blue, alpha);
                        break;
                    case "xhair":
                        ColorManager.xhair.updateColors(red, green, blue, alpha);
                        break;
                    case "chamsVis":
                        ColorManager.chamsVis.updateColors(red, green, blue, alpha);
                        break;
                    case "chamsInvis":
                        ColorManager.chamsInvis.updateColors(red, green, blue, alpha);
                        break;
                    case "chestESP":
                        ColorManager.chestESPColor.updateColors(red, green, blue, alpha);
                        break;
                    case "targetStrafe":
                        ColorManager.strafeColor.updateColors(red, green, blue, alpha);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ColorCommand(String[] names, String description) {
        super(names, description);
        loadStatus();
    }


    @Override
    public void fire(String[] args) {
        if (args == null) {
            printUsage();
            return;
        }

        if (args.length < 2) {
            printUsage();
            return;
        }
        String[] color = args[1].split(":");
        if (color.length < 4) {
            printUsage();
            return;
        }
        int red = Integer.parseInt(color[0]);
        int green = Integer.parseInt(color[1]);
        int blue = Integer.parseInt(color[2]);
        int alpha = Integer.parseInt(color[3]);
        switch (args[0]) {
            case "fv":
                ColorManager.fVis.updateColors(red, green, blue, alpha);
                break;
            case "fi":
                ColorManager.fInvis.updateColors(red, green, blue, alpha);
                break;
            case "ev":
                ColorManager.eVis.updateColors(red, green, blue, alpha);
                break;
            case "ei":
                ColorManager.eInvis.updateColors(red, green, blue, alpha);
                break;
            case "et":
                ColorManager.eTeam.updateColors(red, green, blue, alpha);
                break;
            case "ft":
                ColorManager.fTeam.updateColors(red, green, blue, alpha);
                break;
            case "hc":
                ColorManager.hudColor.updateColors(red, green, blue, alpha);
                break;
            case "ce":
                ColorManager.chestESPColor.updateColors(red, green, blue, alpha);
                break;
            default:
                printUsage();
                return;
        }

        ChatUtil.printChat(chatPrefix + "Color set: \247c" + red + "  \247a" + green + "  \247b" + blue + "  \247f" + alpha);
        saveStatus();
    }

    @Override
    public String getUsage() {
        return "object <fv | ev | fi | ei | hd | et | ft> color <r:g:b:a>";
    }

}
