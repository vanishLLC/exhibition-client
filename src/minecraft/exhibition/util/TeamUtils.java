package exhibition.util;

import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Arithmo on 1/19/2017.
 */
public class TeamUtils {

    public static boolean isTeam(final EntityLivingBase e, final EntityLivingBase e2) {
        // ChatUtil.printChat(e2.getDisplayName().getFormattedText().contains("§" + isTeam(e)) + " " + isTeam(e));
        boolean pitMode = HypixelUtil.isInGame("THE HYPIXEL PIT");
        if (pitMode && !HypixelUtil.scoreboardContains("event:")) {
            return e == e2;
        }

        return e == e2 || (e.getDisplayName().getFormattedText().contains("§" + isTeam(e)) && e2.getDisplayName().getFormattedText().contains("§" + isTeam(e)));
    }

    private static final Pattern p = Pattern.compile("§(.).*§r");

    private static String isTeam(EntityLivingBase player) {
        boolean pitMode = HypixelUtil.isInGame("THE HYPIXEL PIT") && HypixelUtil.scoreboardContains("event:");
        String nameFormated = player.getDisplayName().getFormattedText();
        if (HypixelUtil.scoreboardContains("Event: SQUADS")) {
            if (pitMode && nameFormated.contains("[")) {
                nameFormated = nameFormated.split("\\[")[1];
            }
        } else if(!HypixelUtil.scoreboardContains("PIZZA")) {
            if (pitMode && nameFormated.contains("] ")) {
                nameFormated = nameFormated.split("] ")[1];
            }
        }
        final Matcher m = p.matcher(nameFormated);
        if (m.find()) {
            return m.group(1);
        }
        return "f";
    }

}
