package exhibition.management.animate;

/**
 * Created by cool1 on 4/9/2017.
 */
public class Opacity {

    private float opacity;
    private long lastMS;

    public Opacity(float opacity) {
        this.opacity = opacity;
        lastMS = System.currentTimeMillis();
    }

    public void interpolate(float targetOpacity) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        opacity = (float) AnimationUtil.calculateCompensation(targetOpacity, opacity, delta, 20);
    }

    public void interp(float targetOpacity, int speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        opacity = (float) AnimationUtil.calculateCompensation(targetOpacity, opacity, delta, speed);
    }

    public float getOpacity() {
        return (int) opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public float getScale() {
        if(opacity > 255) {
            return 1;
        }
        if (opacity < 0) {
            return 0;
        }
        return (opacity/255);
    }

}

