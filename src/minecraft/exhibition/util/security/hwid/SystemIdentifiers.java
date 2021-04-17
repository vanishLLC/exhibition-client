package exhibition.util.security.hwid;

import oshi.hardware.ComputerSystem;

public class SystemIdentifiers implements Identifier {

    private final String manufacturer;
    private final String serial;
    private final String model;

    private final int totalRam;

    public SystemIdentifiers(ComputerSystem computerSystem, int totalRam) {
        this.manufacturer = trim(computerSystem.getManufacturer());
        this.serial = trim(computerSystem.getSerialNumber());
        this.model = trim(computerSystem.getModel());

        this.totalRam = totalRam;
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

    public int getTotalRam() {
        return totalRam;
    }

}
