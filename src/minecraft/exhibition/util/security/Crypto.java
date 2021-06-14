package exhibition.util.security;

import net.minecraft.util.CryptManager;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Crypto {

	private static String AES = "AES/ECB/PKCS5Padding";

	/**
	 * Encrypts text with a given key.
	 *
	 * @param key
	 * @param text
	 * @throws Exception
	 *             Thrown if the key is invalid
	 */
	public static String encrypt(Key key, String text) {
		try {
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8), 0, text.getBytes(StandardCharsets.UTF_8).length);
			byte[] encryptedValue = Base64.encodeBase64(encrypted);
			return AuthenticationUtil.decodeByteArray(encryptedValue);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Decrypts text with a given key.
	 *
	 * @param key
	 * @param text
	 * @return
	 * @throws Exception
	 *             Thrown if the key is invalid
	 */
	public static String decrypt(Key key, String text) {
		try {
			Cipher cipher = Cipher.getInstance(AES);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decodedBytes = Base64.decodeBase64(text.getBytes(StandardCharsets.UTF_8));
			byte[] original = cipher.doFinal(decodedBytes, 0, decodedBytes.length);
			return new String(original, StandardCharsets.UTF_8);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptPrivate(String str) {
		try {
			return decrypt(CryptManager.getDecrypt(), str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public static String decryptPrivateNew(String str) {
//		try {
//			SecretKeySpec secretKeySpec = CryptManager.getDecryptNew();
//			return decrypt(secretKeySpec, str);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	public static String decryptPublicNew(String str) {
		try {
			return decrypt(CryptManager.getSecretNew(), str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a unique byte array (for key generation) that is user-specific.
	 *
	 * @param size
	 * @return
	 */
	public static byte[] getUserKey(int size) {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; i++) {
			String yourMom = exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {115, 62, 53, 83, 110, 122, 72, 119, 119, 65, 110, 113, 69, 53, 47, 101, 86, 57, 62, 59, 50, 60, 44, 112, 104, 82, 114, 47, 37, 97, 37, 116, 41, 98, 101, 60, 42, 112, 87, 61, 115, 69, 88, 70, 104, 62, 52, 64, 85, 56, 67, 112, 56, 36, 77, 102, 115, 64, 117, 62, 63, 93, 113, 115, 123, 58, 52, 66, 72, 98, 87, 76, 50, 39, 40, 122, 87, 51, 98, 72, 113, 102, 115, 78, 115, 99, 95, 122, 83, 57, 70, 121, 80, 66, 109, 58, 118, 72, 62, 104, 83, 91, 68, 109, 41, 77, 41, 95, 91, 121, 41, 93, 33, 63, 45, 78, 40, 62, 87, 99, 45, 123, 95, 85, 109, 43, 125});
			ret[i] = (byte) ((yourMom.split("(?<=\\G.{4})"))[i].hashCode() % 256);
		}
		return ret;
	}

	public static byte[] getUserKeySet(int size) {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; i++) {
			String str = "^J*tEhD4EXFKS6m%nJvaB13GePU*T@bUdn*ND@*K*5N#0!1pEU5WSbNGg^xwcSjNN^*^H7UbMCxxtx5fu*Q5#b#MJv$!!v4Y6MYtbbQaU%ryN%5tpv0y*fv9";
			ret[i] = (byte) ((str.split("(?<=\\G.{4})"))[i].hashCode() % 256);
		}
		return ret;
	}

	public static byte[] getUserKeyOLD(int size) {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; i++) {
			String yourMom = exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {79, 121, 91, 87, 112, 77, 45, 107, 88, 62, 80, 85, 64, 45, 120, 99, 64, 52, 61, 66, 37, 69, 100, 45, 90, 71, 103, 71, 98, 120, 106, 72, 97, 101, 104, 64, 64, 79, 121, 91, 87, 112, 77, 45, 107, 88, 62, 80, 85, 64, 45, 120, 99, 64, 52, 61, 66, 37, 69, 100, 45, 90, 71, 103, 71, 98, 120, 106, 72, 97, 101, 104, 64, 64});
			ret[i] = (byte) ((yourMom.split("(?<=\\G.{4})"))[i].hashCode() % 256);
		}
		return ret;
	}

	public static byte[] getUserKeySetOLD(int size) {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; i++) {
			String str = exhibition.util.security.AuthenticationUtil.decodeByteArray(new byte[] {65, 52, 51, 115, 49, 65, 83, 68, 97, 45, 97, 115, 100, 97, 51, 50, 61, 50, 61, 51, 102, 115, 102, 50, 52, 97, 83, 65, 68, 65, 109, 79, 80, 43, 45, 97, 69, 122, 120, 49, 65, 83, 68, 77, 83, 43, 115, 97, 115, 100, 100, 97, 48, 45, 97, 57, 97, 117, 106, 115, 100, 48, 97, 45, 115, 97, 100, 48, 57, 97, 115, 95, 65, 83, 65, 83, 68, 45, 97, 100, 48, 45, 97, 102, 107, 97, 115, 102, 45, 75, 70, 95, 97, 48, 65, 115, 45, 48, 100, 95, 74, 95, 95, 111, 111, 112, 53, 49, 119, 57, 49, 50});
			ret[i] = (byte) ((str.split("(?<=\\G.{4})"))[i].hashCode() % 256);
		}
		return ret;
	}

}
