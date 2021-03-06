package exhibition.util;

import net.minecraft.entity.EntityLivingBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Arithmo on 1/19/2017.
 */
public class TeamUtils {

    public static boolean isTeam(final EntityLivingBase e, final EntityLivingBase e2) {
        // ChatUtil.printChat(e2.getDisplayName().getFormattedText().contains("§" + isTeam(e)) + " " + isTeam(e));
        boolean pitMode = HypixelUtil.isInGame("THE HYPIXEL PIT");
        if (pitMode) {
            if ((!HypixelUtil.scoreboardContains("event:") || HypixelUtil.scoreboardContains("SPIRE") || HypixelUtil.scoreboardContains("BLOCKHEAD") || HypixelUtil.scoreboardContains("RAFFLE") || HypixelUtil.scoreboardContains("PIZZA")))
                return e == e2;
            if(HypixelUtil.scoreboardContains("event:") && HypixelUtil.scoreboardContains("SQUAD")) {
                String first = e.getDisplayName().getFormattedText();
                String second = e2.getDisplayName().getFormattedText();
                return e == e2 || (first.substring(0, Math.min(first.length(), 6)).equals(second.substring(0, Math.min(second.length(), 6))));
            }
            if(HypixelUtil.scoreboardContains("event:") && HypixelUtil.scoreboardContains("BEAST")) {
                String first = e.getDisplayName().getFormattedText();
                String second = e2.getDisplayName().getFormattedText();
                return e == e2 || (first.contains("BEAST") && second.contains("BREACH"));
            }
        }

        return e == e2 || (e.getDisplayName().getFormattedText().contains("§" + isTeam(e)) && e2.getDisplayName().getFormattedText().contains("§" + isTeam(e)));
    }

    private static final Pattern p = Pattern.compile("§(.).*§r");

    private static String isTeam(EntityLivingBase player) {
        boolean pitMode = HypixelUtil.isInGame("HYPIXEL PIT") && HypixelUtil.scoreboardContains("Event:");
        String nameFormated = player.getDisplayName().getFormattedText();
        if (pitMode)
            if (HypixelUtil.scoreboardContains("TDM")) {
                if (nameFormated.contains("]")) {
                    nameFormated = nameFormated.split("]")[1];
                }
            }
        final Matcher m = p.matcher(nameFormated);
        if (m.find()) {
            return m.group(1);
        }
        return "f";
    }

}
