/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import exhibition.util.misc.ChatUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LibraryIntegrityChecker {

    // Returns the instance of SystemInfo
    public static Object checkOSHIIntegrity() {
        try {
            boolean isNative = false;

            List<String> list = null;
            Class cpClass = Class.forName("oshi.hardware.CentralProcessor");

            Class protectionDomain = Class.forName("java.security.ProtectionDomain");
            Object csInstance = protectionDomain.getMethod("getCodeSource").invoke(Class.class.getMethod("getProtectionDomain").invoke(cpClass));

            Class codeSourceClass = Class.forName("java.security.CodeSource");
            codeSourceClass.getMethod("getLocation").invoke(csInstance);

            File oshiJar = new File(((URL) codeSourceClass.getMethod("getLocation").invoke(csInstance)).toURI());

            Class md = Class.forName("java.security.MessageDigest");

            String expectedPath = "libraries/com/github/oshi/oshi-core/3.12.0/oshi-core-3.12.0.jar";

            Object mdInstance = md.getMethod("getInstance", String.class).invoke(null, "SHA-256");

            try (InputStream in = new FileInputStream(oshiJar)) {
                byte[] block = new byte[4096];
                int length;
                while ((length = in.read(block)) > 0) {
                    md.getMethod("update", byte[].class, int.class, int.class).invoke(mdInstance, block, 0, length);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Snitch.snitch(18, oshiJar.getAbsolutePath()); // ????
            }

            String checkSum = (String) Class.forName("javax.xml.bind.DatatypeConverter").getMethod("printHexBinary", byte[].class).invoke(null, (byte[]) md.getMethod("digest").invoke(mdInstance));
            if (new File(expectedPath).exists() && oshiJar.getAbsolutePath().equals(new File(expectedPath).getAbsolutePath())) {
                if (Integer.valueOf("-988826486").equals(checkSum.hashCode())) {
                    Class runtimeClazz = Class.forName("sun.management.ManagementFactoryHelper");
                    Field vmInstance = runtimeClazz.getDeclaredField("jvm");
                    vmInstance.setAccessible(true);
                    Class clazz = Class.forName("sun.management.VMManagementImpl");
                    Method nativeGetVmArguments = clazz.getMethod("getVmArguments0");
                    nativeGetVmArguments.setAccessible(true);
                    Class modifierClazz = Class.forName("java.lang.reflect.Modifier");
                    Method isNativeMethod = modifierClazz.getMethod("isNative", int.class);
                    if ((boolean) isNativeMethod.invoke(null, nativeGetVmArguments.getModifiers())) {
                        isNative = isNativeMethod.invoke(null, nativeGetVmArguments.getModifiers()).equals(true);
                    }

                    if (isNative)
                        list = Arrays.asList((String[]) nativeGetVmArguments.invoke(vmInstance.get(clazz)));

                    for (String arg : list) {
                        if (arg.equalsIgnoreCase("-XX:+DisableAttachMechanism")) {
                            return Class.forName("java.lang.Class").getMethod("newInstance").invoke(Class.forName("oshi.SystemInfo"));
                        }
                    }

                } else {
                    Snitch.snitch(17, checkSum, "" + checkSum.hashCode()); // Checksum mismatch
                }

            } else {
                Snitch.snitch(16, oshiJar.getAbsolutePath(), new File(expectedPath).getAbsolutePath(), checkSum); // Why isn't this the same path?
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snitch.snitch(15, e.getMessage()); // Okay
        }
        return null;
    }

}
