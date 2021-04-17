package exhibition.util.security.hwid;

public interface Identifier {

    default String trim(String str) {
        if(str.equals("unknown")) {
            return "";
        }
        return str.trim();
    }

}
