package exhibition.util.security.mtls;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;

public class CFKeyManagerFactory {

    private static KeyManagerFactory keyManagerFactory;

    public static KeyManagerFactory getKeyManager() {
        if (keyManagerFactory == null) {
            try {
                String CERT = CertificateContainer.getCertificate();

                String PRIVATE_KEY = PrivateKeyContainer.getPrivateKey();

                PRIVATE_KEY = PRIVATE_KEY.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replaceAll("\n", "")
                        .replace("-----END PRIVATE KEY-----", "");

                byte[] encoded = Base64.getDecoder().decode(PRIVATE_KEY);

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                Collection<? extends Certificate> chain = certificateFactory.generateCertificates(new ByteArrayInputStream(CERT.getBytes()));

                Key key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));

                KeyStore clientKeyStore = KeyStore.getInstance("jks");
                final char[] pwdChars = "Cloudflare".toCharArray();
                clientKeyStore.load(null, null);
                clientKeyStore.setKeyEntry("Cloudflare", key, pwdChars, chain.toArray(new Certificate[0]));

                keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(clientKeyStore, pwdChars);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return keyManagerFactory;
    }

    public static SSLContext getContext(KeyManagerFactory keyManagerFactory) {
        SSLContext sslContext = null;
        try {
            TrustManager[] acceptAllTrustManager = {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), acceptAllTrustManager, new java.security.SecureRandom());
        } catch (Exception e) {
        }
        return sslContext;
    }

    public static Object getVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals("api1.minesense.pub");
            }
        };
    }

}

