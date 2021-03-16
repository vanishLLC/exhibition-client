package exhibition.util.security;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static exhibition.util.security.BruhClass.*;
import static exhibition.util.security.BruhClass2.*;
import static exhibition.util.security.BruhClass3.*;
import static exhibition.util.security.InstrumentationCheck.TEMPPROTECT0;


@SuppressWarnings("all")
public class HWIDCheck {

    private static final Object byteArrayClazz;

    public static Object isHWIDValid(Object... array) {
        Object response = new AuthenticationUtil.Stupid(new ArrayList<>(), false);
        try {
            if (!((AuthenticationUtil.Stupid) TEMPPROTECT0()).detected) {
                // Real Auth
                String URL = null;
                String hardware = SystemUtil.getHardwareIdentifiers();
                Object field = null;
                for (Field bruh : AuthenticationUtil.class.getFields()) {
                    boolean isFinal = Boolean.parseBoolean(Class.forName("java.lang.reflect.Modifier").getMethod("isFinal", int.class).invoke(null, bruh.getModifiers()).toString());
                    if (bruh.getType().equals(array.getClass().getComponentType()) && isFinal) {
                        field = bruh;
                        break;
                    }
                }


                Class unsafeClass = Class.forName("sun.misc.Unsafe");
                Object bruh = unsafeClass.getDeclaredField("theUnsafe");

                Class fieldClass = Class.forName("java.lang.reflect.Field");

                fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
                Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);

                String randomString = "";

                String randomString2 = "-";

                String getAndSetObject = "getAndSetObject";

                unsafeClass.getMethod(getAndSetObject, Object.class, long.class, Object.class).invoke(unsafeInstance, unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafeInstance, field), unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafeInstance, field),
                        AESCipher.decryptToByteArray(getAndSetObject.charAt(0) + "H68s" + getAndSetObject.charAt(14) + "Z" + getAndSetObject.charAt(3) + "84Q",_1() + _2() + _3() + _4() + _5() + _6() + _7() + _8() + _9() + _10() + _11() + _12() + _13() + _14() + _15() + _16() + _17() + _18() + _19() + _20() + _21() + _22() + _23() + _24() + _25() + _26() + _27() + _28() + _29() + _30() + _31() + _32() + _33() + _34() + _35() + _36() + _37() + _38() + _39() + _40() + _41() + _42() + _43() + _44() + _45() + _46() + _47() + _48() + _49() + _50() + _51() + _52()));


                if (bruh != null) {
                    return (Object)(response.equals(array[1]));
                }
            }
        } catch (Exception e) {
        }
        return response;
    }

    static {
        Object bruh = null;
        try {
            bruh = Class.forName("java.lang.String").getMethod("getBytes").getReturnType();
        } catch (Exception e) {
        }
        byteArrayClazz = bruh;
    }

    public static String _7() { return "ObPMQLfSKbrRJabxaU83"; }
    public static String _43() { return "EIFk+yTtdMCL7TnysvKe"; }
    public static String _22() { return "Ssk5bE9wCeuP74UG+u2O"; }
    public static String _16() { return "huNPMkqbld4afGNxfHUj"; }
    public static String _18() { return "vGQ4JaSn93c+65ujlYxZ"; }
    public static String _42() { return "ab64cvlVXQguoBU6Hdea"; }
    public static String _28() { return "LV1mP/A82pBU3A+N8lCR"; }
    public static String _2() { return "MpCnn2SzmlY5/eqQjG9e"; }
    public static String _34() { return "OWXrCRwQT4VTnRFBJbH8"; }
    public static String _52() { return "/kjF"; }
    public static String _17() { return "Cvybdx+JTeq1/PhIMR7j"; }
    public static String _40() { return "NkcoS75R2Er8L6wPcFgw"; }
    public static String _9() { return "pk6Q9MZx8Cphm+mHwpmM"; }

}
