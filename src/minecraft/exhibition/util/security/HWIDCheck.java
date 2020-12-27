package exhibition.util.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static exhibition.util.security.AuthenticationUtil.*;
import static exhibition.util.security.BruhClass.*;
import static exhibition.util.security.BruhClass2.*;
import static exhibition.util.security.BruhClass3.*;


@SuppressWarnings("all")
public class HWIDCheck {


    private static String _89 = "LIiy";
    private static String _138 = "JqFL";
    private static String _90 = "jssk";
    private static String _165 = "fTsc";
    private static String _73 = "28v5";
    private static String _85 = "M+TG";
    private static String _20 = "bzin";
    private static String _106 = "jSuQ";
    private static String _105 = "uYol";
    private static String _170 = "qFx/";
    private static String _28 = "9sck";
    private static String _101 = "14gq";
    private static String _25 = "yt8A";
    private static String _37 = "tlgy";
    private static String _94 = "05dg";
    private static String _108 = "n9+W";
    private static String _87 = "S6NK";
    private static String _97 = "5Krm";
    private static String _146 = "tVuR";
    private static String _123 = "dtDx";
    private static String _159 = "JUMk";
    private static String _182 = "AwEA";
    private static String _70 = "p8uj";
    private static String _91 = "jqvp";
    private static String _149 = "ccr0";
    private static String _3 = "hkiG";
    private static String _147 = "BCco";
    private static String _180 = "eTly";
    private static String _71 = "DtN2";
    private static String _66 = "UldK";
    private static String _69 = "rNiz";
    private static String _13 = "bx2p";
    private static String _30 = "XsPG";
    private static String _10 = "AgEA";
    private static String _164 = "l90o";
    private static String _125 = "EMyy";
    private static String _49 = "XuVy";
    private static String _6 = "AAOC";
    private static String _4 = "9w0B";
    private static String _36 = "8U90";
    private static String _62 = "M+wf";
    private static String _75 = "b1+Y";
    private static String _166 = "MSg5";
    private static String _160 = "/FwG";
    private static String _107 = "ggNs";
    private static String _142 = "uHqa";
    private static String _129 = "QFhe";
    private static String _162 = "6jPI";
    private static String _63 = "wJgb";
    private static String _135 = "TiLT";
    private static String _76 = "UuYL";
    private static String _9 = "CgKC";
    private static String _56 = "edqE";
    private static String _120 = "M9Re";
    private static String _21 = "bZV0";
    private static String _173 = "BBUU";
    private static final Object byteArrayClazz;


    public static Object isHWIDValid(Object string, boolean b) {
        Object response = new AuthenticationUtil.Stupid(new ArrayList<>(), false);
        try {
            if (Boolean.parseBoolean(decodeByteArray(new byte[]{116, 114, 117, 101})) && !((AuthenticationUtil.Stupid) TEMPPROTECT0()).detected) {
                // Real Auth
                String URL = null;
                Connection connection = null;
                String hardware = SystemUtil.getHardwareIdentifiers();
                Object field = null;
                for (Field bruh : Class.forName("exhibition.util.security.AuthenticationUtil").getFields()) {
                    boolean isFinal = Boolean.parseBoolean(Class.forName("java.lang.reflect.Modifier").getMethod("isFinal", int.class).invoke(null, bruh.getModifiers()).toString());
                    if (bruh.getType().equals(byteArrayClazz) && isFinal) {
                        URL = Crypto.decryptPrivate("2HERJVjF6EBt5M4ohHRYEf7M8uAcxCzhBEiMJwl3vcpqH+yU4C7/d/bnvCWFbzYB");
                        field = bruh;
                        connection = new Connection("https://minesense.pub/nig/crypt".replace(URL, bruh.getName()));
                        break;
                    }
                }

                Class unsafeClass = Class.forName("sun.misc.Unsafe");
                Object bruh = unsafeClass.getDeclaredField("theUnsafe");

                Class fieldClass = Class.forName("java.lang.reflect.Field");

                fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
                Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);
                unsafeClass.getMethod("getAndSetObject", Object.class, long.class, Object.class).invoke(unsafeInstance, unsafeClass.getMethod("staticFieldBase", Field.class).invoke(unsafeInstance, field), unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafeInstance, field), (("MIIC"+_1+_2+_3+_4+_5+_6+_7+_8+_9+_10+_11+_12+_13+_14+_15+_16+_17+_18+_19+_20+_21+_22+_23+_24+_25+_26+_27+_28+_29+_30+_31+_32+_33+_34+_35+_36+_37+_38+_39+_40+_41+_42+_43+_44+_45+_46+_47+_48+_49+_50+_51+_52+_53+_54+_55+_56+_57+_58+_59+_60+_61+_62+_63+_64+_65+_66+_67+_68+_69+_70+_71+_72+_73+_74+_75+_76+_77+_78+_79+_80+_81+_82+_83+_84+_85+_86+_87+_88+_89+_90+_91+_92+_93+_94+_95+_96+_97+_98+_99+_100+_101+_102+_103+_104+_105+_106+_107+_108+_109+_110+_111+_112+_113+_114+_115+_116+_117+_118+_119+_120+_121+_122+_123+_124+_125+_126+_127+_128+_129+_130+_131+_132+_133+_134+_135+_136+_137+_138+_139+_140+_141+_142+_143+_144+_145+_146+_147+_148+_149+_150+_151+_152+_153+_154+_155+_156+_157+_158+_159+_160+_161+_162+_163+_164+_165+_166+_167+_168+_169+_170+_171+_172+_173+_174+_175+_176+_177+_178+_179+_180+_181+_182+_183).getBytes()));
                connection.setParameters("aooga", URLEncoder.encode(Base64.encode(AsymmetricalEncryptionUtils.performRSAEncryption(hardware.getBytes(), decodeByteArray(publicKeyEncoded))), "UTF-8".replace("90a8s90df8------------agsdfasdfi0aopsdjf09", "asdfagas-09-09-ohnoedfgasdfga")).replace("989sad09f80a9usjdfn09asdgagsdagasdaihfvcn08ihga809gbdv9ba890sdf", ""));

                int tCache = LoginUtil.getCachedLogin();
                if(tCache != -1) {
                    connection.setParameters("monkey", String.valueOf(tCache));
                }

                if(bruh != null) {
                    return response;
                }
                String result = SSLConnector.post(connection);

                //System.out.println(result);
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(result.trim());
                String ciphered = "";
                String pisition = "";

                for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()) {
                    try {
                        String decrypted = decodeByteArray(AsymmetricalEncryptionUtils.performRSADecryption(Base64.decode(stringJsonElementEntry.getKey()), decodeByteArray(publicKeyEncoded)));
                        if (decrypted.equals("ciphered")) {
                            ciphered = stringJsonElementEntry.getKey();
                        } else if (decrypted.equals("position")) {
                            pisition = stringJsonElementEntry.getKey();
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }

                if (jsonObject.has("response")) {
                    String parsedResponse = jsonObject.get("response").getAsString();
                    if (parsedResponse.equals("success")) {
                        if (jsonObject.has(ciphered)) {
                            String cipherText = jsonObject.get(ciphered).getAsString();

                            String decryptedData = AsymmetricalEncryptionUtils.performRSADecryption(cipherText, decodeByteArray(publicKeyEncoded));
                            String[] parsed = decryptedData.split(":");

                            try {
                                if (BCrypt.checkpw(hardware, parsed[2]).detected) {
                                    Class<?> boolClass = Class.forName(decodeByteArray(new byte[]{106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 66, 111, 111, 108, 101, 97, 110}));
                                    Constructor<?> constructor = boolClass.getConstructor(boolean.class);
                                    String[] arguments = null;
                                    Class var2 = Class.forName(decodeByteArray(new byte[]{106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 109, 97, 110, 97, 103, 101, 109, 101, 110, 116, 46, 77, 97, 110, 97, 103, 101, 109, 101, 110, 116, 70, 97, 99, 116, 111, 114, 121}));
                                    Object var3 = var2.getDeclaredMethod(decodeByteArray(new byte[]{103, 101, 116, 82, 117, 110, 116, 105, 109, 101, 77, 88, 66, 101, 97, 110})).invoke(null);
                                    Method method = var3.getClass().getMethod(decodeByteArray(new byte[]{103, 101, 116, 73, 110, 112, 117, 116, 65, 114, 103, 117, 109, 101, 110, 116, 115}));
                                    method.setAccessible(true);
                                    List<String> list = (List) method.invoke(var3);
                                    boolean isNative = !list.isEmpty();
                                    try {
                                        Class classClazz = Class.forName(/*java.lang.Class*/decodeByteArray(new byte[]{(byte) (0xcee39da8 >>> 0x2), (byte) (0xd35859dc >>> 0xe), (byte) (0x96cc760a >>> 0x8), (byte) (0xf7ac3404 >>> 0xd), (byte) (0xb05d7e8a >>> 0x11), (byte) (0x19a10d8c >>> 0x5), (byte) (0x11b0dd4d >>> 0xf), (byte) (0x13979b81 >>> 0x6), (byte) (0x19dcfee1 >>> 0x16), (byte) (0x2e543d33 >>> 0x18), (byte) (0x8bea1a39 >>> 0xb), (byte) (0x6c43ab75 >>> 0x18), (byte) (0xfcaa4c28 >>> 0x5), (byte) (0x8b9fe08a >>> 0x13), (byte) (0x9ce6e045 >>> 0x11)}));
                                        Object forNameMethodd = classClazz.getMethod(/*forName*/decodeByteArray(new byte[]{(byte) (0x6a5b357a >>> 0xb), (byte) (0x6b86fa06 >>> 0xc), (byte) (0x72315b0 >>> 0x14), (byte) (0xbcea7397 >>> 0xb), (byte) (0x761dac8b >>> 0x14), (byte) (0x51b538e7 >>> 0x12), (byte) (0x23366589 >>> 0x8)}), String.class);
                                        Class runtimeClazz = Class.forName(/*sun.management.ManagementFactoryHelper*/decodeByteArray(new byte[]{(byte) (0x499cf19d >>> 0xe), (byte) (0x51d5ff8f >>> 0x12), (byte) (0x8dda535d >>> 0x15), (byte) (0x9fc25d41 >>> 0x9), (byte) (0x4e79db7d >>> 0x6), (byte) (0x330e0ad7 >>> 0x13), (byte) (0xbbb77ca2 >>> 0xf), (byte) (0x8126a61d >>> 0x4), (byte) (0x33e33743 >>> 0x17), (byte) (0x1b5b2c5f >>> 0xb), (byte) (0xc202db7e >>> 0x6), (byte) (0x92cacef6 >>> 0x11), (byte) (0x2acdc94a >>> 0xd), (byte) (0xcbd3ae9b >>> 0x5), (byte) (0x5a370b96 >>> 0x6), (byte) (0xe4d78b3a >>> 0x14), (byte) (0xd26c2299 >>> 0xd), (byte) (0x5b8c5fe2 >>> 0x16), (byte) (0x9240b0bb >>> 0x7), (byte) (0x2670a92a >>> 0x14), (byte) (0x9ed46ca6 >>> 0x5), (byte) (0xe41576d2 >>> 0x4), (byte) (0xda919972 >>> 0x6), (byte) (0xdb6e6ad >>> 0xc), (byte) (0x51dd2e82 >>> 0x5), (byte) (0x24946567 >>> 0xc), (byte) (0x261e8928 >>> 0x14), (byte) (0xac7139a4 >>> 0x15), (byte) (0xc25f74e1 >>> 0x8), (byte) (0x6d85b7e >>> 0x3), (byte) (0xb469fee5 >>> 0x1), (byte) (0xd1e4d694 >>> 0x12), (byte) (0x4483691d >>> 0x5), (byte) (0x34b2c2db >>> 0xf), (byte) (0x9a9ed9ae >>> 0x9), (byte) (0x370b5182 >>> 0x14), (byte) (0x4f195262 >>> 0xe), (byte) (0x9d031ca9 >>> 0x6)}));
                                        Field vmInstance = runtimeClazz.getDeclaredField(/*jvm*/decodeByteArray(new byte[]{(byte) (0xc3355848 >>> 0xf), (byte) (0x6f76ce06 >>> 0x10), (byte) (0xcd388db >>> 0x1)}));
                                        vmInstance.setAccessible(true);
                                        Class clazz = Class.forName(/*sun.management.VMManagementImpl*/decodeByteArray(new byte[]{(byte) (0xac039897 >>> 0xb), (byte) (0x9019d6b7 >>> 0xa), (byte) (0x6413b9b9 >>> 0x2), (byte) (0x84bb1cc1 >>> 0x12), (byte) (0x1f2daaf9 >>> 0xd), (byte) (0xb10f6164 >>> 0x8), (byte) (0x976ebe9 >>> 0xc), (byte) (0x1857aa0e >>> 0x16), (byte) (0x67e68e40 >>> 0x18), (byte) (0x429f3aca >>> 0x1), (byte) (0x67fb5b75 >>> 0x6), (byte) (0x1ab4bb29 >>> 0x3), (byte) (0xdc132db >>> 0x15), (byte) (0x64cac1d3 >>> 0x2), (byte) (0x797245ab >>> 0x13), (byte) (0xeebcb567 >>> 0x4), (byte) (0x4d362031 >>> 0x12), (byte) (0x4d88576a >>> 0x18), (byte) (0x558490ff >>> 0x12), (byte) (0x6ff356e6 >>> 0x4), (byte) (0x65185898 >>> 0xe), (byte) (0x62467043 >>> 0xc), (byte) (0xab2a62d6 >>> 0x13), (byte) (0xfdcdbf61 >>> 0xd), (byte) (0x91948d13 >>> 0x12), (byte) (0xdb9d994c >>> 0x16), (byte) (0x3d186e81 >>> 0x5), (byte) (0xea4a1458 >>> 0x13), (byte) (0xe6d46d37 >>> 0x14), (byte) (0x6b6ae191 >>> 0x9), (byte) (0x6a733650 >>> 0x7)}));
                                        Method nativeGetVmArguments = clazz.getMethod(/*getVmArguments0*/decodeByteArray(new byte[]{(byte) (0x5a559ee8 >>> 0xa), (byte) (0x62d96a09 >>> 0xe), (byte) (0xe3a3edca >>> 0x13), (byte) (0x8adf93b0 >>> 0x15), (byte) (0xd906dba0 >>> 0x9), (byte) (0xefe83ed >>> 0x9), (byte) (0x56e5bef >>> 0xd), (byte) (0xae9aecfb >>> 0x5), (byte) (0xb3a8751b >>> 0x8), (byte) (0xeb6df0ac >>> 0x10), (byte) (0x2eb6566d >>> 0xc), (byte) (0xf4405b9a >>> 0x6), (byte) (0x821d2e40 >>> 0xe), (byte) (0xf4ac734d >>> 0x8), (byte) (0x6309b6ae >>> 0x14)}));
                                        nativeGetVmArguments.setAccessible(true);
                                        Class modifierClazz = Class.forName(/*java.lang.reflect.Modifier*/decodeByteArray(new byte[]{(byte) (0xdaa60ed5 >>> 0x16), (byte) (0xccc1b086 >>> 0x7), (byte) (0x6f3b0f12 >>> 0xf), (byte) (0x4c2c3f3a >>> 0xd), (byte) (0x532e522d >>> 0x10), (byte) (0xcd99d09e >>> 0x15), (byte) (0x488a4c28 >>> 0x5), (byte) (0x4f42dc6a >>> 0x9), (byte) (0xab3c2867 >>> 0x13), (byte) (0xd2edc7fc >>> 0x14), (byte) (0xdda105c8 >>> 0x2), (byte) (0xb7d95c36 >>> 0xe), (byte) (0xa133e663 >>> 0x4), (byte) (0x21cb0d93 >>> 0x5), (byte) (0x727997cf >>> 0xa), (byte) (0x98f01d88 >>> 0x16), (byte) (0x74596349 >>> 0x18), (byte) (0x23b97110 >>> 0xb), (byte) (0xc9b71168 >>> 0x15), (byte) (0xc6f8f020 >>> 0x14), (byte) (0x9d3358c8 >>> 0x1), (byte) (0x1ff34ce5 >>> 0xb), (byte) (0x9e38666b >>> 0x8), (byte) (0x9f11a448 >>> 0xa), (byte) (0xaca65a3b >>> 0xc), (byte) (0xdc15ca67 >>> 0xa)}));
                                        Method isNativeMethod = modifierClazz.getMethod(/*isNative*/decodeByteArray(new byte[]{(byte) (0x820832d2 >>> 0x1), (byte) (0x7b9f9db5 >>> 0x13), (byte) (0x89cf46b4 >>> 0x15), (byte) (0xbeb0587c >>> 0x6), (byte) (0x1e306740 >>> 0x4), (byte) (0xc469c2db >>> 0x10), (byte) (0x72d3b0e5 >>> 0xb), (byte) (0x30e2eb2c >>> 0x3)}), int.class);
                                        if ((boolean) isNativeMethod.invoke(null, nativeGetVmArguments.getModifiers())) {
                                            isNative = isNativeMethod.invoke(null, nativeGetVmArguments.getModifiers()).equals(true);
                                        }
                                        if (isNative)
                                            arguments = (String[]) nativeGetVmArguments.invoke(vmInstance.get(Class.forName(/*sun.management.VMManagementImpl*/decodeByteArray(new byte[]{(byte) (0xac039897 >>> 0xb), (byte) (0x9019d6b7 >>> 0xa), (byte) (0x6413b9b9 >>> 0x2), (byte) (0x84bb1cc1 >>> 0x12), (byte) (0x1f2daaf9 >>> 0xd), (byte) (0xb10f6164 >>> 0x8), (byte) (0x976ebe9 >>> 0xc), (byte) (0x1857aa0e >>> 0x16), (byte) (0x67e68e40 >>> 0x18), (byte) (0x429f3aca >>> 0x1), (byte) (0x67fb5b75 >>> 0x6), (byte) (0x1ab4bb29 >>> 0x3), (byte) (0xdc132db >>> 0x15), (byte) (0x64cac1d3 >>> 0x2), (byte) (0x797245ab >>> 0x13), (byte) (0xeebcb567 >>> 0x4), (byte) (0x4d362031 >>> 0x12), (byte) (0x4d88576a >>> 0x18), (byte) (0x558490ff >>> 0x12), (byte) (0x6ff356e6 >>> 0x4), (byte) (0x65185898 >>> 0xe), (byte) (0x62467043 >>> 0xc), (byte) (0xab2a62d6 >>> 0x13), (byte) (0xfdcdbf61 >>> 0xd), (byte) (0x91948d13 >>> 0x12), (byte) (0xdb9d994c >>> 0x16), (byte) (0x3d186e81 >>> 0x5), (byte) (0xea4a1458 >>> 0x13), (byte) (0xe6d46d37 >>> 0x14), (byte) (0x6b6ae191 >>> 0x9), (byte) (0x6a733650 >>> 0x7)}))));
                                    } catch (Exception ignored) {
                                        isNative = false;
                                    }
                                    response = new AuthenticationUtil.Stupid(new ArrayList<>(), !isNative || !Arrays.asList(arguments).equals(list) || !(((AuthenticationUtil.Stupid) RuntimeVerification.isClassPathModified(constructor.toString()))).isThisJointDetected() && (Boolean) (constructor.newInstance(boolClass.getDeclaredMethod(decodeByteArray(new byte[]{112, 97, 114, 115, 101, 66, 111, 111, 108, 101, 97, 110}), String.class).invoke((Object) null, decodeByteArray(new byte[]{84, 82, 85, 69})))));

                                    if(jsonObject.has(pisition)) {
                                        AuthenticationUtil.authListPos = (int)Math.sqrt(LoginUtil.cachedLogin = jsonObject.get(pisition).getAsInt());
                                    }
                                } else {
                                    Class.forName("exhibition.util.security.Snitch").getMethod("snitch", int.class, String[].class).invoke(null, 1992, new String[]{connection.getPayload()});
                                    LoginUtil.cachedLogin = -1;
                                    LoginUtil.LOGIN.deleteOnExit();
                                }
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

}
