package exhibition.util.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static exhibition.util.security.AuthenticationUtil.getHwid;

public class InstrumentationCheck {

    @SuppressWarnings("unchecked")
    public static Object TEMPPROTECT0() { // Check for instrumentation
        // TODO: REMOVE ON UPDATE
        if (getHwid() != 32161752)
            try {
                final Field classesField = ClassLoader.class.getDeclaredField("classes");
                classesField.setAccessible(true);
                final Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(ClassLoader.getSystemClassLoader());

                boolean detected = false;
                List<String> detectedClasses = new ArrayList<>();
                for (final Class<?> clazz : new Vector<>(classes)) {
                    for (final Class<?> interfaceClazz : clazz.getInterfaces()) {
                        if (interfaceClazz.getName().startsWith("java.lang.instrument"))
                            if (!detectedClasses.contains(clazz.getName())) {
                                detectedClasses.add(clazz.getName());
                                detected = true;
                            }
                    }

                    try {
                        for (final Field field : clazz.getFields()) {
                            if (field.getType().getName().startsWith("java.lang.instrument"))
                                if (!detectedClasses.contains(clazz.getName())) {
                                    detectedClasses.add(clazz.getName());
                                    detected = true;
                                }
                        }
                    } catch (Throwable ignored) {

                    }

                    try {
                        for (final Method method : clazz.getMethods()) {
                            final List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
                            final List<Class<?>> exceptionTypes = Arrays.asList(method.getExceptionTypes());

                            if (parameterTypes.stream().anyMatch(c -> c.getName().startsWith("java.lang.instrument")) || exceptionTypes.stream().anyMatch(c -> c.getName().startsWith("java.lang.instrument")))
                                if (!detectedClasses.contains(clazz.getName())) {
                                    detectedClasses.add(clazz.getName());
                                    detected = true;
                                }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    if (detected) {
                        exhibition.util.security.Snitch.snitch(2, detectedClasses.toArray(new String[]{}));
                    }
                    return new AuthenticationUtil.Stupid(detectedClasses, detected);
                }
            } catch (Exception ignored) {
            }
        // TODO: REMOVE ON UPDATE
        return new AuthenticationUtil.Stupid(new ArrayList<>(), getHwid() != 32161752);
    }

}
