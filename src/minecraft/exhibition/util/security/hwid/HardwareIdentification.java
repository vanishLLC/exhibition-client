package exhibition.util.security.hwid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import exhibition.Client;
import exhibition.util.security.DiscordUtil;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

public class HardwareIdentification {

    //OS
    public final OperatingSystemIdentifiers operatingSystemIdentifiers;

    //Processor
    public final String cpuName;

    // System
    public final SystemIdentifiers systemIdentifiers;

    // Baseboard
    public final BaseboardIdentifiers baseboardIdentifiers;

    // Firmware
    public final FirmwareIdentifiers firmwareIdentifiers;

    // Display
    public final DisplayIdentifiers displayIdentifiers;

    // Network Adapters
    public final NetworkAdapterIdentifiers networkAdapterIdentifiers;

    // Drive
    public final DiskIdentifiers diskIdentifiers;

    public HardwareIdentification(Object systemInfo) {

            OperatingSystem os = ((SystemInfo)systemInfo).getOperatingSystem();

            HardwareAbstractionLayer hardware = ((SystemInfo)systemInfo).getHardware();

            CentralProcessor centralProcessor = hardware.getProcessor();

            this.operatingSystemIdentifiers = new OperatingSystemIdentifiers(os);

            this.cpuName = centralProcessor.getName().trim();

            GlobalMemory memory = hardware.getMemory();
            ComputerSystem computerSystem = hardware.getComputerSystem();

            int totalRam = (int)Math.round(memory.getTotal() / Math.pow(1024, 3));

            this.systemIdentifiers = new SystemIdentifiers(computerSystem, totalRam);

            Baseboard baseboard = computerSystem.getBaseboard();

            this.baseboardIdentifiers = new BaseboardIdentifiers(baseboard);

            this.firmwareIdentifiers = new FirmwareIdentifiers(computerSystem.getFirmware());

            this.displayIdentifiers = new DisplayIdentifiers(hardware.getDisplays());

            this.networkAdapterIdentifiers = new NetworkAdapterIdentifiers(hardware.getNetworkIFs());

            this.diskIdentifiers = new DiskIdentifiers(hardware.getDiskStores());
    }

    public String getIdentifiersAsJson() {
        Gson gson = new GsonBuilder().create();

        JsonObject jsonObject = new JsonObject();

        if(Client.isDiscordReady) {
            JsonObject discordObject = new JsonObject();

            discordObject.addProperty("username", DiscordUtil.getDiscordUsername(discordObject).toString());
            discordObject.addProperty("id", DiscordUtil.getDiscordID(discordObject).toString());

            jsonObject.add("Discord", discordObject);
        }

        JsonObject osObject = new JsonObject();
        osObject.addProperty("name", operatingSystemIdentifiers.getFamily() + " " + operatingSystemIdentifiers.getVersion());

        jsonObject.add("OS", osObject);

        JsonObject cpuObject = new JsonObject();
        cpuObject.addProperty("name", cpuName);

        jsonObject.add("CPU", cpuObject);

        JsonObject baseboardObject = new JsonObject();
        baseboardObject.addProperty("manufacturer", baseboardIdentifiers.getManufacturer());
        baseboardObject.addProperty("model", baseboardIdentifiers.getModel());
        baseboardObject.addProperty("serial", baseboardIdentifiers.getSerial());

        jsonObject.add("Baseboard", baseboardObject);

        JsonObject systemObject = new JsonObject();
        systemObject.addProperty("manufacturer", systemIdentifiers.getManufacturer());
        systemObject.addProperty("model", systemIdentifiers.getModel());
        systemObject.addProperty("serial", systemIdentifiers.getSerial());
        systemObject.addProperty("totalram", systemIdentifiers.getTotalRam());

        jsonObject.add("System", systemObject);

        JsonObject firmwareObject = new JsonObject();
        firmwareObject.addProperty("manufacturer", firmwareIdentifiers.getManufacturer());
        firmwareObject.addProperty("date", firmwareIdentifiers.getDate());
        firmwareObject.addProperty("version", firmwareIdentifiers.getVersion());

        jsonObject.add("Firmware", firmwareObject);

        JsonArray displayArray = new JsonArray();

        for (DisplayIdentifiers.DisplayContainer displayContainer : displayIdentifiers.getDisplayContainers()) {
            JsonObject displayObject = new JsonObject();
            displayObject.addProperty("name", displayContainer.getName());
            displayObject.addProperty("serial", displayContainer.getSerial());

            displayArray.add(displayObject);
        }

        jsonObject.add("Displays", displayArray);

        JsonArray diskArray = new JsonArray();

        for (DiskIdentifiers.DiskContainer diskContainer : diskIdentifiers.getDiskContainers()) {
            JsonObject diskObject = new JsonObject();
            diskObject.addProperty("model", diskContainer.getModel());
            diskObject.addProperty("serial", diskContainer.getSerial());

            diskArray.add(diskObject);
        }

        jsonObject.add("Disks", diskArray);

        JsonArray vadaptersArray = new JsonArray();
        JsonArray nadaptersArray = new JsonArray();

        for (NetworkAdapterIdentifiers.NetworkAdapter networkAdapter : networkAdapterIdentifiers.getNetworkAdapters()) {
            JsonObject adapterObject = new JsonObject();

            adapterObject.addProperty("name", networkAdapter.getName());
            adapterObject.addProperty("mac", networkAdapter.getMac());

            if(networkAdapter.isVirtualAdapter()) {
                vadaptersArray.add(adapterObject);
            } else {
                nadaptersArray.add(adapterObject);
            }
        }

        if(nadaptersArray.size() > 0) {
            jsonObject.add("Network_Adapters", nadaptersArray);
        }

        if(vadaptersArray.size() > 0) {
            jsonObject.add("Virtual_Adapters", nadaptersArray);
        }

        return gson.toJson(jsonObject);
    }

    //     $hwidStr = $jsonObj['CPU']['name'] . $jsonObj['Baseboard']['model'] . $jsonObj['Baseboard']['serial'] . $jsonObj['System']['model'] . $jsonObj['System']['serial']
    public String getHashedHardware() {
        String str = cpuName + baseboardIdentifiers.getModel() + baseboardIdentifiers.getSerial() + systemIdentifiers.getModel() + systemIdentifiers.getSerial();

        for (DisplayIdentifiers.DisplayContainer displayContainer : displayIdentifiers.getDisplayContainers()) {
            str += displayContainer.getSerial();
        }

        for (DiskIdentifiers.DiskContainer diskContainer : diskIdentifiers.getDiskContainers()) {
            str += diskContainer.getSerial();
        }

        return str.trim();
    }

}
