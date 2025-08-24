package exhibition.util.security;

import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by DoubleParallax on 12/18/2016.
 */
public class Connector {

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

    private static void readStream(HttpURLConnection connection, StringBuilder appender) {
        try {
            InputStream stream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) {
                throw new IOException();
            }
            BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = buffer.readLine()) != null) {
                appender.append(line).append("\n");
            }
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void get(Connection connection) {
        request(connection, Method.GET);
    }

    public static void post(Connection connection) {
        request(connection, Method.POST);
    }


    private static void request(Connection connection, Method method) {
        try {
            String payload = connection.getPayload();

            URL url = Urls.create(connection.getUrl() + (method == Method.GET ? (payload.isEmpty() ? "" : String.format("?%s", payload)) : ""), Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod(method.name());
            for (Map.Entry<String, String> header : connection.getHeaders().entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }

            if (method == Method.POST) {
                urlConnection.setUseCaches(false);

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                try {
                    DataOutputStream output = new DataOutputStream(urlConnection.getOutputStream());
                    output.writeBytes(payload);
                    output.flush();
                    output.close();
                } catch (Exception e) {
                    return;
                }
            }

            StringBuilder response = new StringBuilder();

            if (urlConnection.getResponseCode() == 204) {
                connection.setResponse(response.toString());
                return;
            }

            readStream(urlConnection, response);

            connection.setResponse(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openURL(String url) {
        try {
            Class var2 = Class.forName("java.awt.Desktop");
            Object var3 = var2.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            var2.getMethod("browse", new Class[]{URI.class}).invoke(var3, new Object[]{new URI(url)});
        } catch (Throwable var4) {
            //Minecraft.getLogger().error("Couldn\'t open link", var4);
        }
    }

}

