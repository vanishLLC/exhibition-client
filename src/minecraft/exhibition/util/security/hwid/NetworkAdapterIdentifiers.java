package exhibition.util.security.hwid;

import oshi.hardware.NetworkIF;

import java.util.List;

public class NetworkAdapterIdentifiers implements Identifier {

    private final NetworkAdapter[] networkAdapters;

    public NetworkAdapterIdentifiers(List<NetworkIF> networkIFs) {
        this.networkAdapters = new NetworkAdapter[networkIFs.size()];

        for (int i = 0; i < networkIFs.size(); i++) {
            NetworkIF networkIF = networkIFs.get(i);
            String displayName = networkIF.getDisplayName().trim();
            boolean vpnAdapter = displayName.toLowerCase().contains("virtual") || displayName.toLowerCase().contains("adapter");

            this.networkAdapters[i] = new NetworkAdapter(vpnAdapter, trim(displayName), trim(networkIF.getMacaddr()));
        }
    }

    public NetworkAdapter[] getNetworkAdapters() {
        return networkAdapters;
    }

    public static class NetworkAdapter {

        private final boolean isVirtualAdapter;
        private final String name;
        private final String mac;

        public NetworkAdapter(boolean isVirtualAdapter, String name, String mac) {
            this.isVirtualAdapter = isVirtualAdapter;
            this.name = name;
            this.mac = mac;
        }

        public boolean isVirtualAdapter() {
            return isVirtualAdapter;
        }

        public String getName() {
            return name;
        }

        public String getMac() {
            return mac;
        }

    }

}
