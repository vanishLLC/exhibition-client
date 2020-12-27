package exhibition.management;

import exhibition.management.command.impl.ColorCommand;
import exhibition.util.render.Colors;
import exhibition.util.security.Crypto;

import java.security.MessageDigest;

/**
 * Created by cool1 on 3/7/2017.
 */
public class ColorObject {

    public int red;
    public int green;
    public int blue;
    public int alpha;

    public ColorObject(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getColorHex() {
        return Colors.getColor(red,green,blue,alpha);
    }

    public void updateColors(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        ColorCommand.saveStatus();
    }

}
