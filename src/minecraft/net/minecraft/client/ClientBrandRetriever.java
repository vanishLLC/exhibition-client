package net.minecraft.client;

public class ClientBrandRetriever
{
    public static String getClientModName() {
        return Boolean.parseBoolean(System.getProperty("forgeSpoof")) ? "fml,forge" :  "vanilla";
    }
}
