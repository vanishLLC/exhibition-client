package exhibition.module.data;

import exhibition.module.data.settings.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiBool {

    private List<Setting> booleans;
    private String name;

    public MultiBool(String name, Setting... settings) {
        this.booleans   = Arrays.asList(settings);
        this.name       = name;
    }

    public String getName() {
        return name;
    }

    public List<Setting> getBooleans() {
        return booleans;
    }

    public Setting getSetting(String name) {
        for(Setting setting : booleans) {
            if(setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public boolean getValue(String name) {
        for(Setting setting : booleans) {
            if(setting.getName().equalsIgnoreCase(name)) {
                return (boolean)setting.getValue();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        List<String> bools = new ArrayList<>();
        for (Setting aBoolean : booleans) {
            if((boolean)aBoolean.getValue()) {
                bools.add(aBoolean.getName().charAt(0) + aBoolean.getName().toLowerCase().substring(1));
            }
        }
        return Arrays.toString(bools.toArray());
    }
}
