

package exhibition.gui.generators.handlers.altening.stupidaltserviceshit;

import exhibition.Client;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.AltService;

import javax.net.ssl.*;

public class SSLVerification {

    private boolean verified = false;

    private SSLSocketFactory oldSocketFactory;
    private HostnameVerifier oldVerifier;

    public void verify() {

        if (!verified && Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING) {
            if(oldSocketFactory == null) {
                oldSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
                oldVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            }
            bypassSSL();
            whitelistTheAltening();
            verified = true;
        }

    }

    public boolean revertChanges() {
        boolean disabled = verified;
        if(verified) {
            HttpsURLConnection.setDefaultSSLSocketFactory(oldSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(oldVerifier);
            verified = false;
        }
        return disabled;
    }

    private void bypassSSL() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    private void whitelistTheAltening() {

        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, sslSession) -> hostname.equals("authserver.thealtening.com") || hostname.equals("sessionserver.thealtening.com"));

    }
}