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
        String identifier = hardwareIdentification.osFamily + "|" +
                hardwareIdentification.cpuName + "|" +
                hardwareIdentification.moboManufacturer + "|" +
                hardwareIdentification.moboModel + "|" +
                hardwareIdentification.serialNumber + "|" +
                hardwareIdentification.modelAndSerial;
        return identifier.trim();
    }

    public static String getQuickIdentifier() {
        try {
            String nigro = hardwareIdentification == null ? "Not Grabbed" : getHardwareIdentifiers();
            if (nigro.toLowerCase().contains("vmware") || nigro.toLowerCase().contains("vm ware") || (nigro.contains("Intel(R) Core(TM) i5-8400 CPU @ 2.80GHz") && (nigro.contains("985B2ALNS") || nigro.contains("L731NRCX00H56SMB") || nigro.contains("SCRW18092104G2001")))) {
                SilentSnitch.snitch(405, nigro.substring(0, Math.min(200, nigro.length())));
            }
            return nigro.substring(0, Math.min(500, nigro.length()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

}
