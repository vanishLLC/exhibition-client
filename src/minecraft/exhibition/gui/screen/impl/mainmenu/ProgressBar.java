/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.screen.impl.mainmenu;

import exhibition.Client;
import exhibition.management.ColorManager;
import exhibition.management.animate.Translate;
import exhibition.util.MathUtils;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;

public class ProgressBar {

    private double x;
    private double y;
    private double width;
    private double height;

    private Translate translate;

    private double percent;

    public ProgressBar(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.percent = 0;

        this.translate = new Translate(x, 0);
    }

    public void draw() {
        this.translate.updatePos((width - 2) * getPercent(), 0, 2);
        RenderingUtil.rectangleBordered(x, y, x + width, y + height, 1, Colors.getColor(22, 100), Colors.getColor(0, 125));
        RenderingUtil.rectangle(x + 1, y + 1, x + 1 + translate.getX(), y + height - 1, Colors.getColor(60,255,60, 200));
        String percent = (int)(MathUtils.roundToPlace(getPercent(), 2) * 100) + "%";
        Client.nametagsFontscaled.drawStringWithShadow(percent, (float)(x + width) + 2, (float)(y) - 0.5F, Colors.getColor(255));

    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getPercent() {
        return this.percent;
    }

    public void updatePosition(double x, double y) {
        this.translate.setX((width - 2) * getPercent());
        this.x = x;
        this.y = y;
    }

}
