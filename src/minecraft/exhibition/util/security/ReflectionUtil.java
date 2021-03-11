package exhibition.util.security;

import java.lang.reflect.Field;

public class ReflectionUtil {

    public static void setStaticField(Object field, Object newFieldInstance) {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field bruh = unsafeClass.getDeclaredField("theUnsafe");

            Class fieldClass = Class.forName("java.lang.reflect.Field");

            fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
            Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);

            unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class).invoke(unsafeInstance, unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafeInstance, field), unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafeInstance, field), newFieldInstance);
        } catch (Exception e) {
        }
    }

    public static void setField(Object field, Object objectInstance, Object newFieldInstance) {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field bruh = unsafeClass.getDeclaredField("theUnsafe");

            Class fieldClass = Class.forName("java.lang.reflect.Field");

            fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
            Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);

            unsafeClass.getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafeInstance, objectInstance, unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(unsafeInstance, field), newFieldInstance);
        } catch (Exception e) {
        }
    }

    public static Object getField(Object field, Object objectInstance) {
        Object object = null;
        try {
            Class fieldClass = Class.forName("java.lang.reflect.Field");

            fieldClass.getMethod("setAccessible", boolean.class).invoke(field, true);

            object = fieldClass.getMethod("get", Object.class).invoke(field, objectInstance);
        } catch (Exception e) {
        }

        return object;

    }

}
