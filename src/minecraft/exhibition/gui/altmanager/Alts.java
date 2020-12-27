// 
// Decompiled by Procyon v0.5.30
// 

package exhibition.gui.altmanager;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Alts extends FileManager.CustomFile
{
    public Alts(final String name, final boolean Module, final boolean loadOnStart) {
        super(name, Module, loadOnStart);
    }
    
    @Override
    public void loadFile() throws IOException {
        Scanner scanner = new Scanner(this.getFile());
        String altName = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(!line.startsWith("lastalt:")) {
                final String[] arguments = line.split(" \\(")[0].split(":");
                if (arguments.length == 5) {
                    long time = (Long.valueOf(arguments[4]) > System.currentTimeMillis() ? Long.valueOf(arguments[4]) : -1);
                    AltManager.registry.add(new Alt(arguments[0], arguments[1], arguments[2], arguments.length > 3 ? Alt.Status.valueOf(arguments[3]) : Alt.Status.Unchecked, time));
                } else {
                    AltManager.registry.add(new Alt(arguments[0], arguments[1]));
                }
            } else if(line.startsWith("lastalt:")) {
                altName = line.substring(8, line.length());
            }
        }
        if(altName != null) {
            for(Alt alt : AltManager.registry) {
                if(alt.getMask().equals(altName)) {
                    AltManager.lastAlt = alt;
                }
            }
        }
        scanner.close();
        LogManager.getLogger().info("Loaded " + this.getName() + " File!");
    }
    
    @Override
    public void saveFile() throws IOException {
        final PrintWriter alts = new PrintWriter(new FileWriter(this.getFile()));
        for (final Alt alt : AltManager.registry) {
            if (alt.getMask().equals("")) {
                alts.println(String.valueOf(alt.getUsername()) + ":" + alt.getPassword() + ":" + alt.getUsername() + ":" + alt.getStatus() + ":" + alt.getUnbanDate());
            } else {
                alts.println(String.valueOf(alt.getUsername()) + ":" + alt.getPassword() + ":" + alt.getMask() + ":" + alt.getStatus() + ":" + alt.getUnbanDate());
            }
        }
        if(AltManager.lastAlt != null) {
            alts.println(String.valueOf("lastalt:" + AltManager.lastAlt.getMask()));
        }
        alts.close();
    }
}
