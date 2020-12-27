package exhibition.gui.altmanager;

import java.util.ArrayList;
import java.util.List;

public class AltManager
{
    public static Alt lastAlt;
    public static List<Alt> registry;
    
    static {
        AltManager.registry = new ArrayList<Alt>();
    }
}
