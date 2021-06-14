/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import exhibition.module.Module;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ModuleClassLoader extends ClassLoader {

//    public static boolean loadModuleClass(String name, byte[] classBytes) {
//        boolean shouldLoad = !((AuthenticationUtil.Stupid) RuntimeVerification.isClassPathModified(name + new String(classBytes))).isThisJointDetected();
//        if (shouldLoad)
//            try {
//                Object _classLoader = Class.forName("java.lang.ClassLoader").getMethod("getSystemClassLoader").invoke(null);
//                byte[] rawBytes = new byte[classBytes.length];
//                for (int index = 0; index < rawBytes.length; index++)
//                    rawBytes[index] = (byte) classBytes[index];
//                Class classloaderClass = Class.forName(/*java.lang.ClassLoader*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114}));
//                Method defineClass = classloaderClass.getDeclaredMethod(/*defineClass*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {100, 101, 102, 105, 110, 101, 67, 108, 97, 115, 115}), String.class, byte[].class, int.class, int.class);
//                defineClass.setAccessible(true);
//                Class regeneratedClass = (Class)defineClass.invoke(_classLoader,name, rawBytes, 0, rawBytes.length);
//                return shouldLoad && (regeneratedClass.getSuperclass() == Module.class);
//            } catch (Exception e) {
//            }
//        return false;
//    }

    public static boolean loadClass(String className, byte[] encryptedClassBytes) {
        boolean shouldLoad = !((AuthenticationUtil.Stupid) RuntimeVerification.isClassPathModified(className + Arrays.toString(encryptedClassBytes))).isThisJointDetected();
        if (shouldLoad)
            try {
                Object _classLoader = Class.forName("java.lang.ClassLoader").getMethod("getSystemClassLoader").invoke(null);
                byte[] rawBytes = new byte[encryptedClassBytes.length];
                for (int index = 0; index < rawBytes.length; index++)
                    rawBytes[index] = (byte) encryptedClassBytes[index];
                Class classloaderClass = Class.forName(/*java.lang.ClassLoader*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114}));
                Method defineClass = classloaderClass.getDeclaredMethod(/*defineClass*/exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {100, 101, 102, 105, 110, 101, 67, 108, 97, 115, 115}), String.class, byte[].class, int.class, int.class);
                defineClass.setAccessible(true);
                Class regeneratedClass = (Class)defineClass.invoke(_classLoader,className, rawBytes, 0, rawBytes.length);
                return ((AuthenticationUtil.Stupid) RuntimeVerification.isClassPathModified(className + regeneratedClass.toString())).isThisJointDetected();
            } catch (Exception e) {
            }
        return false;
    }

}
