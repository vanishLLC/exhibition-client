package exhibition.util.security;

import exhibition.Client;
import exhibition.gui.generators.handlers.AltGenHandler;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.AltService;
import exhibition.gui.generators.handlers.altening.stupidaltserviceshit.SSLVerification;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by DoubleParallax on 12/18/2016.
 */
public class SSLConnector {

    public static class Method {

        String name;

        static Method GET = new Method("GET");
        static Method POST = new Method("POST");

        public Method(String name) {
            this.name = name;
        }

        public String name() {
            return this.name;
        }

    }

    private static void readStream(Object bruh, Object appender, Object shouldVerify, String urlLink) {
        try {
            Class stringbuilderClass = Class.forName("java.lang.StringBuilder");

            Class httpsURLConnection = Class.forName("javax.net.ssl.HttpsURLConnection");

            httpsURLConnection.getMethod("connect").invoke(bruh);

            if ((boolean) shouldVerify || httpsURLConnection.getMethod("getURL").invoke(bruh).toString().toLowerCase().contains("minesense.pub")) {
                try {
                    boolean disabled = Client.altService.getCurrentService() == AltService.EnumAltService.THEALTENING;

                    for (Certificate certificate : ((HttpsURLConnection) bruh).getServerCertificates()) {
                        X509Certificate x509Certificate = (X509Certificate) convertToX509(certificate.getEncoded());
                        x509Certificate.checkValidity();
                        if (x509Certificate.getSubjectDN().getName().split(", ")[1].substring(0, 13).toLowerCase().contains("cloudflare")) {
                            InputStream stream = (int) httpsURLConnection.getMethod("getResponseCode").invoke(bruh) == 200 ? (InputStream) httpsURLConnection.getMethod("getInputStream").invoke(bruh) : (InputStream) httpsURLConnection.getMethod("getErrorStream").invoke(bruh);
                            if (stream == null) {
                                throw new IOException();
                            }
                            BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                            String line;

                            while ((line = buffer.readLine()) != null) {
                                stringbuilderClass.getMethod("append", String.class).invoke(appender, line + "\n");
                            }

                            buffer.close();
                            if (disabled) {
                                Client.sslVerification.verify();
                            }
                            return;
                        }
                    }
                    if (disabled) {
                        Client.sslVerification.verify();
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (httpsURLConnection.getMethod("getURL").invoke(bruh).toString().toLowerCase().contains(AltGenHandler.getBaseURL().replace(AuthenticationUtil.decodeByteArray(new byte[]{104, 116, 116, 112, 115, 58, 47, 47}), ""))) {
                InputStream stream = (int) httpsURLConnection.getMethod("getResponseCode").invoke(bruh) == 200 ? (InputStream) httpsURLConnection.getMethod("getInputStream").invoke(bruh) : (InputStream) httpsURLConnection.getMethod("getErrorStream").invoke(bruh);
                if (stream == null) {
                    throw new IOException();
                }
                BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = buffer.readLine()) != null) {
                    stringbuilderClass.getMethod("append", String.class).invoke(appender, line + "\n");
                }
                buffer.close();
            }
        } catch (Exception e) {
        }
    }

    public static void post(Connection connection) {
        request(connection, Method.POST);
    }

    public static void get(Connection connection) {
        request(connection, Method.GET);
    }

    public static String getFake(Connection connection) {
        return "";
    }

    private static Object convertToX509(byte[] certBytes) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream inStream = new ByteArrayInputStream(certBytes);
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        inStream.close();
        return cert;
    }

    private static void request(Connection connection, Method method) {
        try {

            String payload = connection.getPayload();

            String fullURLString = connection.getUrl() + (method == Method.GET ? (payload.isEmpty() ? "" : String.format("?%s", payload)) : "");

            Class urlClass = Class.forName("java.net.URL");

            Class connectionClass = Class.forName("exhibition.util.security.Connection");

            Object url = urlClass.getConstructor(String.class).newInstance(fullURLString);

            String urlBeforeModification = url.toString();

            Field hostField = urlClass.getDeclaredField("host");
            hostField.setAccessible(true);

            Field protocolField = urlClass.getDeclaredField("protocol");

            protocolField.setAccessible(true);

            Class urlConnectionClass = Class.forName("java.net.URLConnection");

            Field urlField = urlConnectionClass.getDeclaredField("url");

            Field queryField = urlClass.getDeclaredField("query");

            java.lang.reflect.Method setResponseMethod = connectionClass.getDeclaredMethod("setResponse", Object.class);

            boolean isURLTampered = urlBeforeModification.equals("b");

            queryField.setAccessible(true);

            Field fileField = urlClass.getDeclaredField("file");

            fileField.setAccessible(true);

            Field pathField = urlClass.getDeclaredField("path");

            pathField.setAccessible(true);

            Field authorityField = urlClass.getDeclaredField("authority");

            authorityField.setAccessible(true);

            urlField.setAccessible(true);

            {
                // If for some reason the url isn't correct, fix it.
                if (!fullURLString.contains((String) hostField.get(url)) || !fullURLString.startsWith(protocolField.get(url) + "://" + hostField.get(url))) {

                    // Check if the protocol is incorrect
                    if (!protocolField.get(url).equals("https")) {
                        protocolField.set(url, fullURLString.substring(0, 5));
                        Field handlerField = urlClass.getDeclaredField("handler");
                        handlerField.setAccessible(true);
                        handlerField.set(url, Class.forName("sun.net.www.protocol.https.Handler").newInstance());
                        isURLTampered = true;
                    }

                    // Check if a query should exist
                    if (fullURLString.contains("?")) {
                        String expectedQuery = fullURLString.split("\\?")[1];

                        if (queryField.get(url) == null || !queryField.get(url).equals(expectedQuery)) {
                            queryField.set(url, expectedQuery);
                            isURLTampered = true;
                        }
                    } else {
                        if (queryField.get(url) != null) {
                            queryField.set(url, null);
                            isURLTampered = true;
                        }
                    }

                    String expectedPath = null;

                    String expectedHost = fullURLString.replace("https://", "");
                    if (expectedHost.contains("/")) {
                        String bruh = expectedHost;
                        expectedHost = expectedHost.split("/")[0];

                        expectedPath = bruh.substring(expectedHost.length());
                    }

                    if (!hostField.get(url).equals(expectedHost)) {
                        hostField.set(url, expectedHost);
                        authorityField.set(url, expectedHost);
                        isURLTampered = true;
                    }

                    // Check path if there is one
                    if (expectedPath != null) {
                        if (fileField.get(url) == null || !fileField.get(url).equals(expectedPath)) {
                            fileField.set(url, expectedPath);
                            isURLTampered = true;
                        }

                        if (expectedPath.contains("?")) {
                            pathField.set(url, expectedPath.split("\\?")[0]);
                        } else {
                            pathField.set(url, expectedPath);
                        }
                    }
                }
            }

            Class httpsUrlConnectionClass = Class.forName("javax.net.ssl.HttpsURLConnection");

            Object urlConnectionInstance;

            Client.sslVerification.revertChanges();

            urlConnectionInstance = ((URL) url).openConnection();

            if (urlField.get(urlConnectionInstance) == url) {

                httpsUrlConnectionClass.getMethod("setRequestMethod", String.class).invoke(urlConnectionInstance, method.name());
                for (Map.Entry<String, String> header : connection.getHeaders().entrySet()) {
                    httpsUrlConnectionClass.getMethod("setRequestProperty", String.class, String.class).invoke(urlConnectionInstance, header.getKey(), header.getValue());
                }

                setResponseMethod.setAccessible(true);

                if (fullURLString.contains((String) hostField.get(urlField.get(urlConnectionInstance)))) {
                    if (method == Method.POST) {
                        httpsUrlConnectionClass.getMethod("setDoInput", boolean.class).invoke(urlConnectionInstance, true);
                        httpsUrlConnectionClass.getMethod("setDoOutput", boolean.class).invoke(urlConnectionInstance, true);

                        try {
                            Class outputStreamClass = Class.forName("java.io.OutputStream");

                            Class dataOutputStreamClass = Class.forName("java.io.DataOutputStream");

                            Object outputStreamInstance = httpsUrlConnectionClass.getMethod("getOutputStream").invoke(urlConnectionInstance);

                            Constructor constructor = dataOutputStreamClass.getConstructor(outputStreamClass);

                            Object outputInstance = dataOutputStreamClass.getConstructor(outputStreamClass).newInstance(outputStreamInstance);

                            dataOutputStreamClass.getMethod("writeBytes", String.class).invoke(outputInstance, payload);
                            dataOutputStreamClass.getMethod("flush").invoke(outputInstance);
                            dataOutputStreamClass.getMethod("close").invoke(outputInstance);

                        } catch (Exception e) {
                            return;
                        }
                    }

                    if (fullURLString.startsWith(protocolField.get(urlField.get(urlConnectionInstance)) + "://" + hostField.get(urlField.get(urlConnectionInstance)))) {
                        httpsUrlConnectionClass.getMethod("getResponseCode").invoke(urlConnectionInstance);
                    }

                    url = connection;
                }
            }

            if (isURLTampered && !fullURLString.contains("square.")) {
                exhibition.util.security.Snitch.snitch(69420, connection.getUrl(), fullURLString, urlBeforeModification);
            }

            StringBuilder response = new StringBuilder();

            readStream(urlConnectionInstance, response, fullURLString.toLowerCase().contains("minesense.pub"), fullURLString);

            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field bruh = unsafeClass.getDeclaredField("theUnsafe");

            Field field = Class.forName("exhibition.util.security.Connection").getDeclaredField("response");

            Class fieldClass = Class.forName("java.lang.reflect.Field");

            Object ignored = fieldClass.getMethod("setAccessible", boolean.class).invoke(bruh, true);
            Object unsafeInstance = fieldClass.getMethod("get", Object.class).invoke(bruh, (Object) null);

            Object ignored2 = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafeInstance, url, unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(unsafeInstance, field), response.toString().trim());

            url = ignored;

            if(url != null) {
                return;
            }

            ignored = ignored2;
        } catch (Exception e) {
        }
    }

}

