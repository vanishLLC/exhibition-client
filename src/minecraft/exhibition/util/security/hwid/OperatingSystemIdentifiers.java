package exhibition.util.security.hwid;

import oshi.software.os.OperatingSystem;

public class OperatingSystemIdentifiers implements Identifier {

    private final String family;
    private final String version;
    private final String build;

    public OperatingSystemIdentifiers(OperatingSystem os) {
        this.family = trim(os.getFamily());
        this.version = trim(os.getVersionInfo().getVersion());
        this.build = trim(os.getVersionInfo().getBuildNumber());
    }

    public String getFamily() {
        return family;
    }

    public String getVersion() {
        return version;
    }

    public String getBuild() {
        return build;
    }

}
