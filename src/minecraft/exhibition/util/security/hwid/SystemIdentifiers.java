package exhibition.util.security.hwid;

import oshi.hardware.ComputerSystem;

public class SystemIdentifiers {

    private final String manufacturer;
    private final String serial;
    private final String model;

    private int totalRam;

    public SystemIdentifiers(ComputerSystem computerSystem, int totalRam) {
        this.manufacturer = computerSystem.getManufacturer().trim();
        this.serial = computerSystem.getSerialNumber().trim();
        this.model = computerSystem.getModel().trim();

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
