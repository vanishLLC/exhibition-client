/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.management.animate.particles;

import exhibition.management.animate.Opacity;
import exhibition.management.animate.Translate;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class ParticleContainer {

    private double posX, posY, width, height;

    private List<Particle> particleList = new ArrayList<>();

    public ParticleContainer(double posX, double posY, double width, double height, int particles, double radius, int color) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        for (int i = 0; i < particles; i++) {
            particleList.add(new Particle(2 + Math.random() * (width - 2), Math.random() * height, radius, color));
        }
    }

    private double lastMouseX = -1337, lastMouseY = -1337;

    public void updateAndRender(double centerX, double centerY, double mouseX, double mouseY) {
        if(lastMouseX == -1337) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 0);
        Depth.pre();
        Depth.mask();
        RenderingUtil.rectangle(0, 0, width, height, -1);
        Depth.render();
        double mouseDiffX = mouseX - lastMouseX;
        double mouseDiffY = mouseY - lastMouseY;

        double offsetX = (mouseX - centerX) / 20;
        double offsetY = (mouseY - centerY) / 20;

        for (Particle particle : particleList) {
            particle.render(mouseDiffX, mouseDiffY, offsetX, offsetY);
            if (particle.getOpacity().getOpacity() <= 0) {
                particle.reset();
            }
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        Depth.post();
        GlStateManager.popMatrix();
    }

    private class Particle {

        private double motionX, motionY;
        private Translate translate;
        private Translate scalar;
        private double radius;
        private int color;
        private int seed;
        private double scale;
        private Opacity opacity;
        private float targetNewValue;

        public Particle(double initX, double initY, double radius, int color) {
            this.translate = new Translate(initX, initY);
            this.scalar = new Translate(0, 0);
            this.radius = radius;
            int alpha = ((color >> 16 & 0xFF));
            int red = ((color >> 16 & 0xFF));
            int green = ((color >> 8 & 0xFF));
            int blue = ((color & 0xFF));
            this.scale = (2 * Math.random());
            this.opacity = new Opacity((float) (-2550 * Math.random()));
            this.targetNewValue = (float) (1050 + (1500 * scale));
            this.color = Colors.getColor(red, green, blue, (int) ((alpha * (scale - 0.35))));
            this.seed = 1;
        }

        // y(x) = Acos(x)

        public void render(double xOffset, double yOffset, double scalarX, double scalarY) {
            double xScalar = scalarX * scale;
            double yScalar = scalarY * scale;
            if(seed == 1) {
                opacity.interp(targetNewValue + 1, 50);
            } else {
                opacity.interp(0, 15);
            }
            if(opacity.getOpacity() >= targetNewValue && seed == 1) {
                seed = 0;
            }

            boolean e = Mouse.isButtonDown(0);
            if(e) {
                setMotionX(getMotionX() + (xOffset / 55) * scale);
                setMotionY(getMotionY() + (yOffset / 55) * scale);
            }

            double motX = motionX *= e ? 0.98 : 0.96;
            double motY = motionY *= e ? 0.98 : 0.96;

            scalar.interpolate(xScalar * 1000, yScalar * 1000, 0.1f);

            translate.updatePos(translate.getX() + motX, translate.getY() + motY, 100);

            scalar.interpolate(0,0, 0.1f);
            float value = (opacity.getOpacity() / 2550) > 1 ? 1 : (opacity.getOpacity() / 2550);
            int alpha = (int) ((color >> 16 & 0xFF) * value);
            int red = ((color >> 16 & 0xFF));
            int green = ((color >> 8 & 0xFF));
            int blue = ((color & 0xFF));

            double radius = this.radius + 0.25 * scale;

            RenderingUtil.rectangle(translate.getX() - radius + scalar.getX() / 1000, translate.getY() - radius + scalar.getY() / 1000,
                    translate.getX() + radius + scalar.getX() / 1000, translate.getY() + radius + scalar.getY() / 1000, Colors.getColor(red, green, blue, alpha));
        }

        public double getMotionX() {
            return motionX;
        }

        public void setMotionX(double motionX) {
            this.motionX = motionX;
        }

        public double getMotionY() {
            return motionY;
        }

        public void setMotionY(double motionY) {
            this.motionY = motionY;
        }

        public Opacity getOpacity() {
            return opacity;
        }

        public double getX() {
            return translate.getX();
        }

        public void setX(double x) {
            translate.setX(x);
        }

        public double getY() {
            return translate.getY();
        }

        public void setY(double y) {
            translate.setY(y);
        }

        public int getColor() {
            return color;
        }

        public double getRadius() {
            return radius;
        }

        public void reset() {
            setY(Math.random() * height);
            setX(2 + Math.random() * (width - 2));
            targetNewValue = (float) (1050 + (1500 * scale));
            seed = 1;
        }
    }

}
