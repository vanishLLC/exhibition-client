package exhibition.util.security.hwid;

import oshi.hardware.Display;
import oshi.util.EdidUtil;

import java.util.List;

public class DisplayIdentifiers implements Identifier {

    private final DisplayContainer[] displayContainers;

    public DisplayIdentifiers(List<Display> displays) {
        this.displayContainers = new DisplayContainer[displays.size()];

        for (int i = 0; i < displays.size(); i++) {
            Display display = displays.get(i);
            byte[] edid = display.getEdid();
            this.displayContainers[i] = new DisplayContainer(trim(getMonitorName(edid)), trim(getMonitorSerial(edid)));
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
