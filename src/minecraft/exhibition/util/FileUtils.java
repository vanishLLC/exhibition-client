package exhibition.util;

import exhibition.Client;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils
{
    public static List<String> read(final File inputFile) {
        final List<String> readContent = new ArrayList<>();
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                readContent.add(str);
            }
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return readContent;
    }
    
    public static void write(final File outputFile, final List<String> writeContent, final boolean overrideContent) {
        try {
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
            for (final String outputLine : writeContent) {
                out.write(outputLine + System.getProperty("line.separator"));
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static File getConfigDir() {
        final File file = new File(Minecraft.getMinecraft().mcDataDir, Client.clientName);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
    
    public static File getConfigFile(final String name) {
        final File file = new File(getConfigDir(), String.format("%s.txt", name));
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}

