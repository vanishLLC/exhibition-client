package exhibition.util.security.hwid;

import oshi.hardware.Display;
import oshi.util.EdidUtil;

public class DisplayIdentifiers {

    private final DisplayContainer[] displayContainers;

    public DisplayIdentifiers(Display[] displays) {
        this.displayContainers = new DisplayContainer[displays.length];

        for (int i = 0; i < displays.length; i++) {
            Display display = displays[i];
            byte[] edid = display.getEdid();
            this.displayContainers[i] = new DisplayContainer(getMonitorName(edid).trim(), getMonitorSerial(edid).trim());
        }

    }

    public DisplayContainer[] getDisplayContainers() {
        return displayContainers;
    }

    public static class DisplayContainer {

        private final String name, serial;

        public DisplayContainer(String name, String serial) {
            this.name = name;
            this.serial = serial;
        }

        public String getName() {
            return name;
        }

        public String getSerial() {
            return serial;
        }
    }

    private String getMonitorName(byte[] edid) {
        byte[][] desc = EdidUtil.getDescriptors(edid);
        for (byte[] b : desc) {
            if (EdidUtil.getDescriptorType(b) == 252) {
                return EdidUtil.getDescriptorText(b);
            }
        }
        return "";
    }

    private String getMonitorSerial(byte[] edid) {
        byte[][] desc = EdidUtil.getDescriptors(edid);
        for (byte[] b : desc) {
            if (EdidUtil.getDescriptorType(b) == 255) {
                return EdidUtil.getDescriptorText(b);
            }
        }
        return "";
    }

}
