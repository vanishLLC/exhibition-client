/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

@SuppressWarnings("all")
public class AsymmetricalEncryptionUtils {

    private static String RSA = "RSA";

    public static byte[] performRSAEncryption(byte[] plainText, String publicKey) throws Exception {
        Class cipherClass = Class.forName("javax.crypto.Cipher");

        Class keyFactoryClass = Class.forName("java.security.KeyFactory");

        byte[] publicKeyDecoded = (byte[]) Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64").getMethod("decode", String.class).invoke(null, publicKey);

        Object keySpec = Class.forName("java.security.spec.X509EncodedKeySpec").getConstructor(byte[].class).newInstance((Object) publicKeyDecoded);

        Object publicKeyObject = keyFactoryClass.getMethod("generatePublic", KeySpec.class).invoke(keyFactoryClass.getMethod("getInstance", String.class).invoke(null, RSA), keySpec);

        Object cipherInstance = cipherClass.getMethod("getInstance", String.class).invoke(null, RSA);

        cipherClass.getMethod("init", int.class, Key.class).invoke(cipherInstance, 1, publicKeyObject);

        return (byte[]) cipherClass.getMethod("doFinal", byte[].class, int.class, int.class).invoke(cipherInstance, plainText, 0, plainText.length);
    }

    public static String performRSADecryption(byte[] cipherText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(cipherText, 0, cipherText.length);
        return decodeByteArray(result);
    }

    private static String decodeByteArray(byte[] bytes) {
        String str = "";
        for (byte b : bytes) {
            str += (char) (b & 0xFF);
        }
        return str;
    }

//
//    public static byte[] performRSADecryptionBytes(byte[] cipherText, String publicKey) throws Exception {
//        return performRSADecryption(new String(cipherText), publicKey).getBytes();
//    }
//

    public static String performRSADecryption(String cipherTextEncoded, String publicKeyEncoded) throws Exception {
        byte[] publicKeyDecoded = Base64.decode(publicKeyEncoded);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyDecoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PublicKey publicKeyObject = keyFactory.generatePublic(keySpec);
        return performRSADecryption(Base64.decode(cipherTextEncoded), publicKeyObject);
    }

    public static byte[] performRSADecryption(byte[] cipherTextEncoded, String publicKeyEncoded) throws Exception {
        byte[] publicKeyDecoded = Base64.decode(publicKeyEncoded);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyDecoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PublicKey publicKeyObject = keyFactory.generatePublic(keySpec);
        return performRSADecryptionByte(cipherTextEncoded, publicKeyObject);
    }


    public static byte[] performRSADecryptionByte(byte[] cipherText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(cipherText);
    }

}
