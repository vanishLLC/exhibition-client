package exhibition.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import exhibition.Client;
import exhibition.util.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HypixelUtil {

    public static boolean verifiedHypixel = false;

    public static boolean isVerifiedHypixel() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!verifiedHypixel) {
            if (scoreboardContains("www.hypixel.net")
                    && mc.ingameGUI.getTabList().getHeader() != null && mc.ingameGUI.getTabList().getHeader().getUnformattedText().contains("You are playing on MC.HYPIXEL.NET")
                    && mc.ingameGUI.getTabList().getFooter() != null && mc.ingameGUI.getTabList().getFooter().getUnformattedText().contains("STORE.HYPIXEL.NET"))
                verifiedHypixel = true;
        }
        return verifiedHypixel && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.toLowerCase().contains(".hypixel.net") || mc.getCurrentServerData().serverIP.toLowerCase().equals("hypixel.net"));
    }

    public static List<String> getPitEnchants(ItemStack stack) {
        List<String> list = new ArrayList<>();
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey("display", 10)) {
                NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
                if (nbttagcompound.getTagId("Lore") == 9) {
                    NBTTagList tagList = nbttagcompound.getTagList("Lore", 8);
                    if (tagList.tagCount() > 0) {
                        for (int i = 0; i < tagList.tagCount(); ++i) {
                            String tag = tagList.getStringTagAt(i);
                            if (!tag.equals("\247f\2477") && (tag.startsWith("\247f\2477\2479") || (tag.contains("RARE"))) && !tag.replace("\247f\2477", "").contains("\2477") &&
                                    !tag.endsWith("Attack Damage") && !tag.endsWith("Unbreakable") && !tag.toLowerCase().contains("fashion") && !tag.toLowerCase().contains("mystic") &&
                                    !tag.contains("As strong") && !tag.contains("Gotta go") && !tag.contains("Purple Gold")) {
                                list.add(tag);
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public static boolean isInGame(String gameString) {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreboardPlayersTeam = scoreboard.getPlayersTeam(Minecraft.getMinecraft().thePlayer.getName());

        if (scoreboardPlayersTeam != null) {
            int j1 = scoreboardPlayersTeam.getChatFormat().getColorIndex();

            if (j1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + j1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            String bruh = StringUtils.stripHypixelControlCodes(scoreobjective1.getDisplayName());
            return bruh.toLowerCase().contains(gameString.toLowerCase());
        }

        return false;
    }

    public static boolean isGameActive() {
        return scoreboardContains("left:") || scoreboardContains("next event:");
    }

    public static boolean isGameStarting() {
        return scoreboardContains("starting in") || scoreboardContains("waiting..");
    }

    /*
     * Use this method to parse information such as
     * split("Event:", ": ") in order to read stuff like "TDM" in "Event: TDM"
     */
    public static String[] scoreboardSplit(String contains, String regex) {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreboardPlayersTeam = scoreboard.getPlayersTeam(Minecraft.getMinecraft().thePlayer.getName());

        if (scoreboardPlayersTeam != null) {
            int j1 = scoreboardPlayersTeam.getChatFormat().getColorIndex();

            if (j1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + j1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            try {
                Scoreboard scoreboardBruh = scoreobjective1.getScoreboard();
                Collection<Score> collection = scoreboardBruh.getSortedScores(scoreobjective1);
                ArrayList<Score> arraylist = collection.stream().filter(new Predicate<Score>() {
                    @Override
                    public boolean test(Score score) {
                        return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
                    }
                }).collect(Collectors.toCollection(Lists::newArrayList));
                ArrayList<Score> arraylist1;

                if (arraylist.size() > 15) {
                    arraylist1 = Lists.newArrayList(Iterables.skip(arraylist, collection.size() - 15));
                } else {
                    arraylist1 = arraylist;
                }

                for (Score score : arraylist1) {
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName()).replace("\uD83D\uDC7D", "").replace("\uD83C\uDF82", "");

                    String cleaned = StringUtils.stripHypixelControlCodes(s1);
                    if (cleaned.toLowerCase().contains(contains.toLowerCase())) {
                        return cleaned.split(regex);
                    }
                }
            } catch (Exception e) {

            }
        }

        return null;
    }

    public static boolean scoreboardContains(String str) {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreboardPlayersTeam = scoreboard.getPlayersTeam(Minecraft.getMinecraft().thePlayer.getName());

        if (scoreboardPlayersTeam != null) {
            int j1 = scoreboardPlayersTeam.getChatFormat().getColorIndex();

            if (j1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + j1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            try {
                Scoreboard scoreboardBruh = scoreobjective1.getScoreboard();
                Collection<Score> collection = scoreboardBruh.getSortedScores(scoreobjective1);
                ArrayList<Score> arraylist = collection.stream().filter(new Predicate<Score>() {
                    @Override
                    public boolean test(Score score) {
                        return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
                    }
                }).collect(Collectors.toCollection(Lists::newArrayList));
                ArrayList<Score> arraylist1;

                if (arraylist.size() > 15) {
                    arraylist1 = Lists.newArrayList(Iterables.skip(arraylist, collection.size() - 15));
                } else {
                    arraylist1 = arraylist;
                }

                for (Score score : arraylist1) {
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName()).replace("\uD83D\uDC7D", "").replace("\uD83C\uDF82", "");

                    if (StringUtils.stripHypixelControlCodes(s1).toLowerCase().contains(str.toLowerCase())) {
                        return true;
                    }
                }
            } catch (Exception e) {

            }
        }

        return false;
    }

}
