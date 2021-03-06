package exhibition.util.security.hwid;

import oshi.hardware.Firmware;

public class FirmwareIdentifiers {

    private final String manufacturer;
    private final String version;
    private final String date;

    public FirmwareIdentifiers(Firmware firmware) {
        this.manufacturer = firmware.getManufacturer().trim();
        this.version = firmware.getVersion().trim();
        this.date = firmware.getReleaseDate().trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

}
