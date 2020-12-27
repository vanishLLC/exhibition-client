package exhibition.management;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by cool1 on 3/9/2017.
 */
public class ColorManager {

    public static ColorObject getFriendlyVisible() {
        return fVis;
    }

    public static ColorObject getFriendlyInvisible() {
        return fInvis;
    }

    public static ColorObject getEnemyVisible() {
        return eVis;
    }

    public static ColorObject getEnemyInvisible() {
        return eInvis;
    }

    public ColorObject getHudColor() {
        return hudColor;
    }

    public static ColorObject fTeam = new ColorObject(0, 255, 0, 255);
    public static ColorObject eTeam = new ColorObject(255, 0, 0, 255);
    public static ColorObject fVis = new ColorObject(0, 0, 255, 255);
    public static ColorObject fInvis = new ColorObject(0, 255, 0, 255);
    public static ColorObject eVis = new ColorObject(255, 0, 0, 255);
    public static ColorObject eInvis = new ColorObject(255, 255, 0, 255);
    public static ColorObject hudColor = new ColorObject(0, 192, 255, 255);
    public static ColorObject xhair = new ColorObject(0, 255, 0, 200);
    public static ColorObject chamsVis = new ColorObject(255, 0, 0, 255);
    public static ColorObject chamsInvis = new ColorObject(255, 255, 0, 255);
    public static ColorObject strafeColor = new ColorObject(120, 255, 120, 255);
    public static ColorObject chestESPColor = new ColorObject(89, 128, 230, 77);

}
