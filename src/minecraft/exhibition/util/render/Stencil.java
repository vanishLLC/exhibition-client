package exhibition.util.render;

/**
 * Created by cool1 on 1/16/2017.
 */

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_STENCIL;
import static org.lwjgl.opengl.GL30.*;

public final class Stencil {
    private static final Stencil INSTANCE;
    private final HashMap<Integer, StencilFunc> stencilFuncs;
    private int layers;
    private boolean renderMask;

    static {
        INSTANCE = new Stencil();
    }

    public Stencil() {
        this.stencilFuncs = new HashMap<>();
        this.layers = 1;
    }

    public static Stencil getInstance() {
        return Stencil.INSTANCE;
    }

    public void setRenderMask(final boolean renderMask) {
        this.renderMask = renderMask;
    }

    public static void checkSetupFBO() {
        final Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    public static void setupFBO(final Framebuffer fbo) {
        //Deletes old render buffer extensions such as depth
        //Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        //Generates a new render buffer ID for the depth and stencil extension
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        //Binds new render buffer by ID
        //Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        //Adds the depth and stencil extension
        //Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT), Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        //Adds the stencil attachment
        //Args: Target (GL_FRAMEBUFFER_EXT), Attachment (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        //Adds the depth attachment
        //Args: Target (GL_FRAMEBUFFER_EXT), Attachment (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);

    }

    public void startLayer() {
        if (this.layers == 1) {
            GL11.glClearStencil(0);
            GL11.glClear(1024);
        }
        GL11.glEnable(2960);
        ++this.layers;
        if (this.layers > this.getMaximumLayers()) {
            System.out.println("StencilUtil: Reached maximum amount of layers! " + this.getStencilBufferSize());
            this.layers = 1;
        }
    }

    public void stopLayer() {
        if (this.layers == 1) {
            System.out.println("StencilUtil: No layers found!");
            return;
        }
        --this.layers;
        if (this.layers == 1) {
            GL11.glDisable(2960);
        } else {
            final StencilFunc lastStencilFunc = this.stencilFuncs.remove(this.layers);
            if (lastStencilFunc != null) {
                lastStencilFunc.use();
            }
        }
    }

    public void clear() {
        GL11.glClearStencil(0);
        GL11.glClear(1024);
        this.stencilFuncs.clear();
        this.layers = 1;
    }

    public void setBuffer() {
        this.setStencilFunc(new StencilFunc(this, this.renderMask ? 519 : 512, this.layers, this.getMaximumLayers(), 7681, 7680, 7680));
    }

    public void setBuffer(final boolean set) {
        this.setStencilFunc(new StencilFunc(this, this.renderMask ? 519 : 512, set ? this.layers : (this.layers - 1), this.getMaximumLayers(), 7681, 7681, 7681));
    }

    public void cropOutside() {
        this.setStencilFunc(new StencilFunc(this, 517, this.layers, this.getMaximumLayers(), 7680, 7680, 7680));
    }

    public void cropInside() {
        this.setStencilFunc(new StencilFunc(this, 514, this.layers, this.getMaximumLayers(), 7680, 7680, 7680));
    }

    public void setStencilFunc(final StencilFunc stencilFunc) {
        GL11.glStencilFunc(StencilFunc.func_func, StencilFunc.func_ref, StencilFunc.func_mask);
        GL11.glStencilOp(StencilFunc.op_fail, StencilFunc.op_zfail, StencilFunc.op_zpass);
        this.stencilFuncs.put(this.layers, stencilFunc);
    }

    public StencilFunc getStencilFunc() {
        return this.stencilFuncs.get(this.layers);
    }

    public int getLayer() {
        return this.layers;
    }

    public int getStencilBufferSize() {
        return glGetFramebufferAttachmentParameteri(GL_DRAW_FRAMEBUFFER, GL_STENCIL, GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE);
    }

    public int getMaximumLayers() {
        return (int) (Math.pow(2.0, this.getStencilBufferSize()) - 1.0);
    }

    public void createCirlce(final double x, final double y, final double radius) {
        GL11.glBegin(6);
        for (int i = 0; i <= 360; ++i) {
            final double sin = Math.sin(i * 3.141592653589793 / 180.0) * radius;
            final double cos = Math.cos(i * 3.141592653589793 / 180.0) * radius;
            GL11.glVertex2d(x + sin, y + cos);
        }
        GL11.glEnd();
    }

    public void createRect(final double x, final double y, final double x2, final double y2) {
        GL11.glBegin(7);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
    }

    public static class StencilFunc {
        public static int func_func;
        public static int func_ref;
        public static int func_mask;
        public static int op_fail;
        public static int op_zfail;
        public static int op_zpass;

        public StencilFunc(final Stencil paramStencil, final int func_func, final int func_ref, final int func_mask, final int op_fail, final int op_zfail, final int op_zpass) {
            StencilFunc.func_func = func_func;
            StencilFunc.func_ref = func_ref;
            StencilFunc.func_mask = func_mask;
            StencilFunc.op_fail = op_fail;
            StencilFunc.op_zfail = op_zfail;
            StencilFunc.op_zpass = op_zpass;
        }

        public void use() {
            GL11.glStencilFunc(StencilFunc.func_func, StencilFunc.func_ref, StencilFunc.func_mask);
            GL11.glStencilOp(StencilFunc.op_fail, StencilFunc.op_zfail, StencilFunc.op_zpass);
        }
    }
}

