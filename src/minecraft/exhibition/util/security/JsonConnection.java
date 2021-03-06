package exhibition.util.security;

import com.google.gson.JsonParser;

public class JsonConnection {

    public static Object toJsonObject(Connection connection) {
        return JsonParser.parseString(AESCipher.decrypt("Jkg5NZ4tVxs8CD0n", connection.getResponse().trim()).getData());
    }

}
