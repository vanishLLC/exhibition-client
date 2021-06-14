/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

public class Heartbeat {

    public static String[] classesToLoad;

//    public static void loadUselessClass(String className, String encodedClass) {
//        try {
////            Object oldInstance = Client.instance;
////            byte[] decipheredChunk1 = AsymmetricalEncryptionUtils.performRSADecryption(unknownEncryypted, decodeByteArray(AuthenticationUtil.publicKeyEncoded));
////
////            String[] bytesSeperated2 = decodeByteArray(Base64.getDecoder().decode(cachedThingyXD.split(/*~~~*/decodeByteArray(new byte[]{(byte) (0xbfcc2fdf >>> 0x5), (byte) (0xbf2586b8 >>> 0x17), (byte) (0xaf7d67e4 >>> 0x4)}))[1].trim())).split(/*,*/AuthenticationUtil.decodeByteArray(new byte[]{(byte) (0xc4d4b0c >>> 0x6)}));
////
////            byte[] unknownEncryypted2 = new byte[512];
////            for (int index = 0; index < 512; index++) {
////                unknownEncryypted2[index] = Byte.parseByte(bytesSeperated2[index].trim());
////            }
////            byte[] decipheredChunk2 = AsymmetricalEncryptionUtils.performRSADecryption(unknownEncryypted2, decodeByteArray(AuthenticationUtil.publicKeyEncoded));
////
////            byte[] classBytes = new byte[decipheredChunk1.length + decipheredChunk2.length];
////            System.arraycopy(decipheredChunk1, 0, classBytes, 0, decipheredChunk1.length);
////            System.arraycopy(decipheredChunk2, 0, classBytes, decipheredChunk1.length, decipheredChunk2.length);
////
////            System.out.println(Base64.getEncoder().encodeToString(classBytes));
//
//            ModuleClassLoader.loadClass(className, Base64.getDecoder().decode(encodedClass));
//        } catch (Exception ignored) {
//        }
//    }

}
