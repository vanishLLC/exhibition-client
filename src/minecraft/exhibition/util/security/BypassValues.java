package exhibition.util.security;

import exhibition.module.impl.combat.Bypass;
import exhibition.util.HypixelUtil;

public class BypassValues {

    public static double getJumpValue() {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage())
            return 0.424F;
        return 0.42F;
    }

    public static double getOffsetValue() {
        if (HypixelUtil.isVerifiedHypixel() && !Bypass.shouldSabotage())
            return 0.005F;
        return 0.0D;
    }

}
