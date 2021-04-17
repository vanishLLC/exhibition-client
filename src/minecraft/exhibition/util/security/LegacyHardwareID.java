package exhibition.util.security;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class LegacyHardwareID {

    //OS
    public String osFamily;

    //Processor
    public String cpuName;
    public String cpuIdentifier;
    public String processorID;
    public String cpuVendor;

    //Motherboard/Baseboard
    public String moboManufacturer;
    public String moboModel;
    public String serialNumber;

    //Disk
    public String modelAndSerial = "";

    public LegacyHardwareID(Object systemInfo) {
        try {
            Class systemInfoClazz = Class.forName("oshi.SystemInfo");

            OperatingSystem os = (OperatingSystem) systemInfoClazz.getMethod("getOperatingSystem").invoke(systemInfo);

            Class hw = Class.forName("oshi.hardware.HardwareAbstractionLayer");

            HardwareAbstractionLayer hardware = (HardwareAbstractionLayer) systemInfoClazz.getMethod("getHardware").invoke(systemInfo);

            CentralProcessor processor = (CentralProcessor) hw.getMethod("getProcessor").invoke(hardware);
            ComputerSystem computerSystem = (ComputerSystem) hw.getMethod("getComputerSystem").invoke(hardware);

            osFamily = os.getFamily().trim();

            cpuName = processor.getProcessorIdentifier().getName().trim();
            cpuIdentifier = processor.getProcessorIdentifier().getIdentifier().trim();
            processorID = processor.getProcessorIdentifier().getProcessorID().trim();
            cpuVendor = processor.getProcessorIdentifier().getVendor().trim();

            moboManufacturer = computerSystem.getManufacturer().trim();
            moboModel = computerSystem.getModel().trim();
            serialNumber = computerSystem.getBaseboard().getSerialNumber().trim();

            try {
                for (HWDiskStore disk : ((SystemInfo) systemInfo).getHardware().getDiskStores()) {
                    if (!disk.getModel().contains("USB") && !disk.getModel().toLowerCase().contains("flash") && !disk.getModel().toLowerCase().contains("generic"))
                        modelAndSerial = (modelAndSerial.trim().concat(disk.getModel().split(" \\(")[0].trim().concat("|").concat(disk.getSerial().replace(" ","").trim()))).trim().concat("|");
                }
            } catch (Exception ignored) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

