package exhibition.util.security.hwid;

import oshi.hardware.GraphicsCard;

import java.util.List;

public class GPUIdentifiers implements Identifier {

    private final GPUContainer[] gpuContainers;

    public GPUIdentifiers(List<GraphicsCard> gpus) {
        this.gpuContainers = new GPUContainer[gpus.size()];

        for (int i = 0; i < gpus.size(); i++) {
            GraphicsCard gc = gpus.get(i);
            this.gpuContainers[i] = new GPUContainer(trim(gc.getName()), trim(gc.getVersionInfo()), trim(gc.getDeviceId()), trim(gc.getVendor()));
        }

    }

    public GPUContainer[] getDisplayContainers() {
        return gpuContainers;
    }

    public static class GPUContainer {

        private final String name, versionInfo, deviceId, vendor;

        public GPUContainer(String name, String versionInfo, String deviceId, String vendor) {
            this.name = name;
            this.versionInfo = versionInfo;
            this.deviceId = deviceId;
            this.vendor = vendor;
        }

        public String getName() {
            return name;
        }

        public String getVersionInfo() {
            return versionInfo;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getVendor() {
            return vendor;
        }

    }

}
