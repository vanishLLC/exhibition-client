package exhibition.util.security.hwid;

import oshi.hardware.Baseboard;

public class BaseboardIdentifiers implements Identifier {

    private final String manufacturer;
    private final String serial;
    private final String model;

    public BaseboardIdentifiers(Baseboard baseboard) {
        this.manufacturer = trim(baseboard.getManufacturer());
        this.serial = trim(baseboard.getSerialNumber());
        this.model = trim(baseboard.getModel());
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
