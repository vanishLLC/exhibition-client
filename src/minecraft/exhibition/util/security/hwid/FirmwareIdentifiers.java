package exhibition.util.security.hwid;

import oshi.hardware.Firmware;

public class FirmwareIdentifiers implements Identifier {

    private final String manufacturer;
    private final String version;
    private final String date;

    public FirmwareIdentifiers(Firmware firmware) {
        this.manufacturer = trim(firmware.getManufacturer());
        this.version = trim(firmware.getVersion());
        this.date = trim(firmware.getReleaseDate());
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
