package exhibition.management;

import java.util.ArrayList;
import java.util.List;

public class DebugRender {

    public static List<String> debugView = new ArrayList<>();

    public static void clear() {
        debugView.clear();
    }

    public static void show(String str) {
        debugView.add(str);
    }

}
