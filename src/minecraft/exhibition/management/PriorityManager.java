package exhibition.management;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class PriorityManager {

    public final static List<String> priorityList = new ArrayList<>();

    public static void setAsPriority(EntityPlayer entity) {
        if (!priorityList.contains(entity.getName())) {
            priorityList.add(entity.getName());
        }
    }

    public static boolean isPriority(EntityPlayer player) {
        return priorityList.contains(player.getName());
    }

    public static void removePriority(EntityPlayer player) {
        priorityList.remove(player.getName());
    }

    public static void clearPriorityList() {
        priorityList.clear();
    }

}
