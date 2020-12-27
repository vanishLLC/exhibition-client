package exhibition.management.animate;

/**
 * Created by cool1 on 4/9/2017.
 */
public class Expand {

    private float x;
    private float y;
    private float expandX;
    private float expandY;
    private long lastMS;

    public Expand(float x, float y, float expandX, float expandY) {
        this.x = x;
        this.y = y;
        this.expandX = expandX;
        this.expandY = expandY;
    }

    public void interpolate(float targetX, float targetY, int speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        x = (float) AnimationUtil.calculateCompensation(targetX, x, delta, speed);
        y = (float) AnimationUtil.calculateCompensation(targetY, y, delta, speed);
    }

    public float getExpandX() {
        return expandX;
    }

    public float getExpandY() {
        return expandY;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

}
