package exhibition.management.animate;

/**
 * Created by cool1 on 4/9/2017.
 */
public class Rotate {

    private float angle;

    public Rotate(float angle) {
        this.angle = angle;
    }

    public void interpolate(float targetAngle) {
        float diffA = (angle - targetAngle) * 0.6f;
        float tempAngle = angle + diffA;
        tempAngle %= 360.0F;
        if (tempAngle >= 180.0F) {
            tempAngle -= 360.0F;
        }
        if (tempAngle < -180.0F) {
            tempAngle += 360.0F;
        }
        angle = tempAngle;
    }

    public float getAngle() {
        return angle;
    }

}
