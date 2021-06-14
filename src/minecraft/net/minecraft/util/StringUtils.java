package net.minecraft.util;

import java.util.regex.Pattern;

public class StringUtils
{
    private static final Pattern colorCodesPattern = Pattern.compile("(?i)\\u00A7[0-9A-F]");
    private static final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern hypixelControlCode = Pattern.compile("(?i)\\u00A7[0-9A-Za-z]");
    private static final Pattern invalidControlCode = Pattern.compile("(?i)\\u00A7[G-JP-QS-Zg-jp-qs-z]");


    /**
     * Returns the time elapsed for the given number of ticks, in "mm:ss" format.
     */
    public static String ticksToElapsedTime(int ticks)
    {
        int i = ticks / 20;
        int j = i / 60;
        i = i % 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }

    public static String stripControlCodes(String p_76338_0_)
    {
        return patternControlCode.matcher(p_76338_0_).replaceAll("");
    }

    public static String stripColorKeepControl(String str) {
        return colorCodesPattern.matcher(str).replaceAll("\247r");
    }

    public static String stripHypixelControlCodes(String p_76338_0_)
    {
        return hypixelControlCode.matcher(p_76338_0_).replaceAll("");
    }

    public static String stripInvalidControlCodes(String p_76338_0_)
    {
        return invalidControlCode.matcher(p_76338_0_).replaceAll("");
    }

    /**
     * Returns a value indicating whether the given string is null or empty.
     */
    public static boolean isNullOrEmpty(String string)
    {
        return org.apache.commons.lang3.StringUtils.isEmpty(string);
    }
}
