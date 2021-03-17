/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exhibition.Client;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static exhibition.util.security.AuthenticationUtil.getHwid;

@SuppressWarnings("Duplicates")
public class RuntimeVerification {

    private static String[] classesToVerify = new String[]{
            "Runtime.class", "String.class", "StringBuilder.class", "SystemClassLoaderAction.class", "System.class"
    };

    public static List<String> getNativeVMArguments() {
        List<String> list = null;
        boolean isNative = false;
        try {
            Class runtimeClazz = Class.forName("sun.management.ManagementFactoryHelper");
            Field vmInstance = runtimeClazz.getDeclaredField("jvm");
            vmInstance.setAccessible(true);
            Class clazz = Class.forName("sun.management.VMManagementImpl");
            Method nativeGetVmArguments = clazz.getMethod("getVmArguments0");
            nativeGetVmArguments.setAccessible(true);
            Class modifierClazz = Class.forName("java.lang.reflect.Modifier");
            Object isNativeMethod = modifierClazz.getMethod("isNative", int.class);
            if ((boolean) ((Method) isNativeMethod).invoke(null, nativeGetVmArguments.getModifiers())) {
                isNative = ((Method) isNativeMethod).invoke(null, nativeGetVmArguments.getModifiers()).equals(true);
            }
            if (isNative)
                list = Arrays.asList((String[]) nativeGetVmArguments.invoke(vmInstance.get(clazz)));
        } catch (Exception ignored) {
            isNative = false;
        }
        return list;
    }

    public static Object isClassPathModified(final String argsPaassedAsFake) {
        boolean invalidSystemClassFiles = false;//-
        boolean argumentsOkay = false;
        boolean foundDisableAttach = argumentsOkay;
        boolean argumentMismatch = false;
        Class var2 = null;
        Object var3 = null;
        Method method = null;
        List<String> list = null;
        boolean noAgent = true;
        boolean noClassPath = true;
        try {
            var2 = Class.forName("java.lang.management.ManagementFactory");
            var3 = var2.getDeclaredMethod("getRuntimeMXBean").invoke((Object) null);
            method = var3.getClass().getMethod("getInputArguments");
            method.setAccessible(true);
            list = (List) method.invoke(var3, new Object[0]);

            for (String clazz : classesToVerify) {
                String location = System.class.getResource(clazz).toString();
                String contains = "contains";
                String rtJar = "rt.jar!";
                boolean b2 = (boolean) location.getClass().getDeclaredMethod(contains, CharSequence.class).invoke(location, rtJar);
                String runtime = "runtime";
                boolean b4 = (boolean) location.getClass().getDeclaredMethod(contains, CharSequence.class).invoke(location, runtime);
                String minecraft = "minecraft";
                boolean b3 = (boolean) location.getClass().getDeclaredMethod(contains, CharSequence.class).invoke(location, minecraft);
                String libRtJar = "lib/rt.jar";
                boolean b1 = (boolean) location.getClass().getDeclaredMethod(contains, CharSequence.class).invoke(location, libRtJar);

                if (!b1 || !b2 || (b3 && !b4)) {
                    exhibition.util.security.Snitch.snitch(5, location, Boolean.toString(b1), Boolean.toString(b2), Boolean.toString(b3), Boolean.toString(b4), Client.parsedVersion);
                    invalidSystemClassFiles = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            exhibition.module.impl.combat.AutoPot.snitch(42069);
        }
        List<String> mismatchingArgs = new ArrayList<>();

        // TODO: REMOVE ON UPDATE
        if (/* getHwid() != 32161752 */ true)
            try {
                String LAUNCHJSONLOCATION = "launcher_profiles.json";
                FileReader fileReader = new FileReader(new File(LAUNCHJSONLOCATION));
                Object obj = JsonParser.parseReader(fileReader);
                argsPaassedAsFake.getBytes();
                //String json = new String(AsymmetricalEncryptionUtils.performRSAEncryption(Files.readAllBytes(new File(LAUNCHJSONLOCATION).toPath()), new String(AuthenticationUtil.publicKeyEncoded)));

                JsonObject object = (JsonObject) obj;
                JsonElement jsonArray = object.get("profiles");
                for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonArray.getAsJsonObject().entrySet()) {
                    JsonElement lastVersionId = stringJsonElementEntry.getValue().getAsJsonObject().get("lastVersionId");
                    JsonElement javaArgObject = stringJsonElementEntry.getValue().getAsJsonObject().get("javaArgs");
                    List<String> arguments = new ArrayList<>();
                    if (lastVersionId != null && javaArgObject != null) {
                        String javaArgs = javaArgObject.getAsString().trim();

                        boolean classNameEquals = Arrays.equals(lastVersionId.getAsString().getBytes(), ("Exhibition").getBytes());
                        if (classNameEquals) {
                            for (String s : javaArgs.split(" ")) {
                                arguments.add(s);
                                if (!foundDisableAttach)
                                    foundDisableAttach = Arrays.equals(s.getBytes(), "-XX:+DisableAttachMechanism".getBytes());

                                if (s.contains("javaagent")) {
                                    noAgent = false;
                                }
                                if (s.contains("classpath")) {
                                    noClassPath = false;
                                }
                            }
                            if (!foundDisableAttach)
                                try {
                                    exhibition.util.security.Snitch.snitch(0, getNativeVMArguments().toArray(new String[]{}));
                                } catch (Exception e) {
                                    return null;
                                }

                            if (!noClassPath)
                                Snitch.snitch(10, javaArgs.split(" "));
                            if (!noAgent)
                                Snitch.snitch(9, javaArgs.split(" "));


                            for (String argument : arguments) {
                                if (!list.contains(argument)) {
                                    if (argument.startsWith("-XX:HeapDumpPath=") || argument.startsWith("-Dminecraft.") || argument.startsWith("-Djava.") || argument.startsWith("-Dos.") || argument.equals(""))
                                        continue;
                                    argumentMismatch = true;
                                    mismatchingArgs.add(argument);
                                }
                            }
                            for (String t : getNativeVMArguments()) {
                                String str = t.trim();
                                if (str.startsWith("-XX:HeapDumpPath=") || str.startsWith("-Dminecraft.") || str.startsWith("-Djava.") || str.startsWith("-Dos.") || str.equals(""))
                                    continue;
                                if (!arguments.contains(str)) {
                                    argumentMismatch = true;
                                    mismatchingArgs.add(str);
                                }
                            }
                            argumentsOkay = foundDisableAttach && noAgent && noClassPath && !argumentMismatch;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

        if (argumentMismatch) {
            argumentsOkay = false;
            try {
                exhibition.util.security.Snitch.snitch(8, mismatchingArgs.toArray(new String[]{}));
            } catch (Exception e) {

            }
        }
        List<String> classPaths = null;
        try {
            classPaths = Arrays.asList(((String) Class.forName("java.lang.System").getDeclaredMethod("getProperty", String.class).invoke(null, "java.class.path")).split(";"));
        } catch (Exception ignored) {
        }
        List<String> invalid = new ArrayList<>();
        for (String classpath : classPaths) {
            try {
                boolean isClientJar = classpath.contains(new File(Client.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath());
                if (!isClientJar)
                    // TODO: REMOVE ON UPDATE
                    if (/* getHwid() != 32161752 */ true && ((!classpath.contains(System.getProperty("user.home")) && !(classpath.contains(new File("").getAbsolutePath())) && classpath.contains(".minecraft\\libraries")))) {
                        invalidSystemClassFiles = true;
                        invalid.add(classpath);
                    }
            } catch (Exception e) {
                invalidSystemClassFiles = true;
            }
        }
        if (invalidSystemClassFiles) {
            Snitch.snitch(5, invalid.toArray(new String[0]));
        } else if (!argumentsOkay) {
            Snitch.snitch(7, String.valueOf(foundDisableAttach), String.valueOf(noAgent), String.valueOf(noClassPath), String.valueOf(!argumentMismatch));
        } else if (!foundDisableAttach) {
            try {
                exhibition.util.security.Snitch.snitch(0, getNativeVMArguments().toArray(new String[]{}));
            } catch (Exception e) {
                return null;
            }
        }
        final boolean detected = (invalidSystemClassFiles || !argumentsOkay || !foundDisableAttach || argumentMismatch || !argumentsMatch(list).isEmpty());

        return new AuthenticationUtil.Stupid(classPaths, detected);
    }

    public static List<String> argumentsMatch(final List<String> list) {
        String[] arguments = null;
        boolean isNative = !list.isEmpty();
        List<String> mismatching = new ArrayList<>();
        try {
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
                arguments = (String[]) nativeGetVmArguments.invoke(vmInstance.get(clazz));
        } catch (Exception ignored) {
            isNative = false;
            return mismatching;
        }
        for (String argument : arguments) {
            if (!list.contains(argument)) {
                mismatching.add(argument);
            }
        }
        return mismatching;
    }

}
