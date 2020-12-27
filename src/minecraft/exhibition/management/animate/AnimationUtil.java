package exhibition.management.animate;

import net.minecraft.util.MathHelper;

/**
 * Created by Arithmo on 4/11/2017 at 2:12 PM.
 */
public class AnimationUtil {

    public static double calculateCompensation(double target, double current, long delta, double speed) {
        if(target == current)
            return target;
        double calculatedStep = (Math.max(speed * Math.max(1, delta) / (1000D / 60D), 0.1));
        return current + MathHelper.clamp_double(target - current, -calculatedStep, calculatedStep);
    }

}
