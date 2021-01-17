package exhibition.gui.console;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Arithmo on 9/22/2017 at 5:53 PM.
 */
public class SourceConsole {

    private List<String> stringList;

    public SourceConsole(){
        this.stringList = new CopyOnWriteArrayList<>();
    }

    public void addStringList(String str) {
        if(stringList.size() > 85)
            stringList.remove(stringList.get(0));
        stringList.add(str);
    }

    public void clearStringList() {
        stringList.clear();
    }

    public List<String> getStringList() {
        return stringList;
    }

    public String processCommand(String input) {
        String output = "\247cERROR: Invalid Input.";
        //Check command manager
        //Add developer commands or something idfk
        return output;
    }

}
