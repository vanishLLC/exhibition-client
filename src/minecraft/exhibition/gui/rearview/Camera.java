/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.rearview;

import exhibition.Client;
import exhibition.module.impl.render.TestRearCamera;
import exhibition.util.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;

import static org.lwjgl.opengl.GL11.glColor4f;

public class Camera {

    private static boolean capturing;
    private static int fontRendererID;

    private int width = 360, height = 200;
    private Framebuffer frameBuffer;

    public float cameraRotationYaw, cameraRotationPitch;
    public double cameraPosX, cameraPosY, cameraPosZ;
    private boolean frameBufferUpdated, reflected;

    private Minecraft mc;

    public Camera(){
        this(false);
    }

    public Camera(boolean reflected){

        this.reflected = reflected;

        mc = Minecraft.getMinecraft();

        frameBuffer = new Framebuffer(width, height, true);
        makeNewFrameBuffer();


        if(fontRendererID == 0){
            //fontRendererID = mc.getTextureManager().getTexture(mc.fontRendererObj.locationFontTexture).getGlTextureId();
        }
    }

    protected void setCapture(boolean capture){

        if(capture){
            frameBuffer.bindFramebuffer(true);
        }else{
            frameBuffer.unbindFramebuffer();
        }

        capturing = capture;
    }

    public void updateFramebuffer(){
//
//        //So we don't make a loop of rendering the cameras
//        //TODO: make this work when the game isn't in focus
//
//        if(capturing || (!mc.inGameHasFocus) || !isCameraVisible()){
//            return;
//        }
//
//        setCapture(true);
//        //Saves the player's current position and game settings
//
//        double posX, posY, posZ, prevPosX, prevPosY, prevPosZ, lastTickPosX, lastTickPosY, lastTickPosZ;
//        int displayWidth, displayHeight, thirdPersonView;
//        float rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch;
//        boolean hideGUI, viewBobbing;
//
//        displayWidth = mc.displayWidth;
//        displayHeight = mc.displayHeight;
//        rotationYaw = mc.getRenderViewEntity().rotationYaw;
//        prevRotationYaw = mc.getRenderViewEntity().prevRotationYaw;
//        rotationPitch = mc.getRenderViewEntity().rotationPitch;
//        prevRotationPitch = mc.getRenderViewEntity().prevRotationPitch;
//        hideGUI = mc.gameSettings.hideGUI;
//        thirdPersonView = mc.gameSettings.thirdPersonView;
//        viewBobbing = mc.gameSettings.viewBobbing;
//
//        posX = mc.getRenderViewEntity().posX;
//        prevPosX = mc.getRenderViewEntity().prevPosX;
//        lastTickPosX = mc.getRenderViewEntity().lastTickPosX;
//
//        posY = mc.getRenderViewEntity().posY;
//        prevPosY = mc.getRenderViewEntity().prevPosY;
//        lastTickPosY = mc.getRenderViewEntity().lastTickPosY;
//
//        posZ = mc.getRenderViewEntity().posZ;
//        prevPosZ = mc.getRenderViewEntity().prevPosZ;
//        lastTickPosZ = mc.getRenderViewEntity().lastTickPosZ;
//
//        //Sets the player's position to the camera position
//
//        mc.getRenderViewEntity().posX = cameraPosX;
//        mc.getRenderViewEntity().prevPosX = cameraPosX;
//        mc.getRenderViewEntity().lastTickPosX = cameraPosX;
//
//        mc.getRenderViewEntity().posY = cameraPosY;
//        mc.getRenderViewEntity().prevPosY = cameraPosY;
//        mc.getRenderViewEntity().lastTickPosY = cameraPosY;
//
//        mc.getRenderViewEntity().posZ = cameraPosZ;
//        mc.getRenderViewEntity().prevPosZ = cameraPosZ;
//        mc.getRenderViewEntity().lastTickPosZ = cameraPosZ;
//
//        mc.displayWidth = width;
//        mc.displayHeight = height;
//        mc.getRenderViewEntity().rotationYaw = cameraRotationYaw;
//        mc.getRenderViewEntity().prevRotationYaw = cameraRotationYaw;
//        mc.getRenderViewEntity().rotationPitch = cameraRotationPitch;
//        mc.getRenderViewEntity().prevRotationPitch = cameraRotationPitch;
//        mc.gameSettings.thirdPersonView = 0;
//        mc.gameSettings.viewBobbing = false;
//        mc.gameSettings.hideGUI = true;
//
//
//        mc.entityRenderer.updateCameraAndRender(mc.timer.renderPartialTicks);
//
//        setCapture(false);
//
//        //Sets the player's position back to the saved position and reverses the game settings changes
//
//        mc.displayWidth = displayWidth;
//        mc.displayHeight = displayHeight;
//        mc.getRenderViewEntity().rotationYaw = rotationYaw;
//        mc.getRenderViewEntity().prevRotationYaw = prevRotationYaw;
//        mc.getRenderViewEntity().rotationPitch = rotationPitch;
//        mc.getRenderViewEntity().prevRotationPitch = prevRotationPitch;
//        mc.gameSettings.thirdPersonView = thirdPersonView;
//        mc.gameSettings.hideGUI = hideGUI;
//        mc.gameSettings.viewBobbing = viewBobbing;
//
//        mc.getRenderViewEntity().posX = posX;
//        mc.getRenderViewEntity().prevPosX = prevPosX;
//        mc.getRenderViewEntity().lastTickPosX = lastTickPosX;
//
//        mc.getRenderViewEntity().posY = posY;
//        mc.getRenderViewEntity().prevPosY = prevPosY;
//        mc.getRenderViewEntity().lastTickPosY = lastTickPosY;
//
//        mc.getRenderViewEntity().posZ = posZ;
//        mc.getRenderViewEntity().prevPosZ = prevPosZ;
//        mc.getRenderViewEntity().lastTickPosZ = lastTickPosZ;
//
//
//        frameBufferUpdated = true;
    }

    private boolean isCameraVisible(){
        return Client.getModuleManager().isEnabled(TestRearCamera.class);
    }

    public void draw(double x, double y, double x1, double y1){
//
//        //Taken from Framebuffer.java in method func_178038_a on line 234
//        GlStateManager.enableTextures();
//        GlStateManager.disableLighting();
//        GlStateManager.disableAlpha();
//
//        GlStateManager.disableBlend();
//        GlStateManager.enableColorMaterial();
//
//        glColor4f(1f, 1f, 1f, 1f);
//        frameBuffer.bindFramebufferTexture();
//
//        if(reflected){
//            RenderingUtil.drawReflectedTexturedRect(x, y, x1, y1);
//        }else{
//            RenderingUtil.drawFlippedTexturedModalRect(x, y, x1, y1);
//        }
//
//        frameBuffer.unbindFramebufferTexture();
    }

    protected void setToEntityPosition(Entity e){
        cameraPosX = e.lastTickPosX - (e.lastTickPosX-e.posX)*mc.timer.elapsedPartialTicks;
        cameraPosY = e.lastTickPosY - (e.lastTickPosY-e.posY)*mc.timer.elapsedPartialTicks;
        cameraPosZ = e.lastTickPosZ - (e.lastTickPosZ-e.posZ)*mc.timer.elapsedPartialTicks;
    }

    protected void setToEntityPositionAndRotation(Entity e){
        setToEntityPosition(e);
        cameraRotationYaw = e.rotationYaw;
        cameraRotationPitch = e.rotationPitch;
    }

    public void makeNewFrameBuffer(){
        frameBuffer.createFramebuffer(width, height);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        frameBufferUpdated = false;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        frameBufferUpdated = false;
    }

    public boolean isFrameBufferUpdated() {
        return frameBufferUpdated;
    }

    public Camera setReflected(boolean reflected){
        this.reflected = reflected;
        return this;
    }

    public static boolean isCapturing(){
        return capturing;
    }

    public static void setCapturing(boolean capturing){
        Camera.capturing = capturing;
    }
}
