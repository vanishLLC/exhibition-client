package exhibition.util.security.hwid;

import oshi.software.os.OperatingSystem;

public class OperatingSystemIdentifiers {

    private final String family;
    private final String version;

    public OperatingSystemIdentifiers(OperatingSystem os) {
        this.family = os.getFamily().trim();
        this.version = os.getVersion().getVersion().trim();
    }

    public String getFamily() {
        return family;
    }

    public String getVersion() {
        return version;
    }

}
