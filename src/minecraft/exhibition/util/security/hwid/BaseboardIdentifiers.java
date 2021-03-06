package exhibition.util.security.hwid;

import oshi.hardware.Baseboard;

public class BaseboardIdentifiers {

    private final String manufacturer;
    private final String serial;
    private final String model;

    public BaseboardIdentifiers(Baseboard baseboard) {
        this.manufacturer = baseboard.getManufacturer().trim();
        this.serial = baseboard.getSerialNumber().trim();
        this.model = baseboard.getModel().trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getSerial() {
        return serial;
    }

    public String getModel() {
        return model;
    }

}
