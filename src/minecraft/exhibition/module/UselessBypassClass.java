/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module;

import java.lang.reflect.Method;

public class UselessBypassClass {

    private static String UNKNOWNCLASS = "org.sun.verification.Unknown";

    public static double getCriticalValue() {
        try {
            Class clazz = Class.forName(UNKNOWNCLASS);
            Method method = clazz.getMethod("c");
            return (double)method.invoke(null);
        } catch (Exception ignored) {
        }
        return -Double.MAX_VALUE;
    }

    public static double getDamageFirst() {
        try {
            Class clazz = Class.forName(UNKNOWNCLASS);
            Method method = clazz.getMethod("d", boolean.class);
            return (double)method.invoke(null, true);
        } catch (Exception ignored) {
        }
        return -Double.MAX_VALUE;
    }

    public static double getDamageSecond() {
        try {
            Class clazz = Class.forName(UNKNOWNCLASS);
            Method method = clazz.getMethod("d", int.class);
            return (double)method.invoke(null, 0);
        } catch (Exception ignored) {
        }
        return -Double.MAX_VALUE;
    }


    public static double getBypassSpeed() {
        try {
            Class clazz = Class.forName(UNKNOWNCLASS);
            Method method = clazz.getMethod("e", int.class);
            return (double)method.invoke(null, 0);
        } catch (Exception ignored) {
        }
        return Double.NaN;
    }

    public static double getBypassJumpHeight() {
        try {
            Class clazz = Class.forName(UNKNOWNCLASS);
            Method method = clazz.getMethod("e", boolean.class);
            return (double)method.invoke(null, false);
        } catch (Exception ignored) {
        }
        return Double.NaN;
    }

}
