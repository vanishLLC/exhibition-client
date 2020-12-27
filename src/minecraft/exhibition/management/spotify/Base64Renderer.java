package exhibition.management.spotify;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import optifine.TextureUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;

public class Base64Renderer {
    private static Cache<String, Base64Renderer> CACHE = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).maximumWeight(1000000L).weigher((Weigher<String, Base64Renderer>) (s, base64Renderer) -> {
        String base64String = base64Renderer.getURLString();
        return base64String == null ? 0 : base64String.length();
    })

            .removalListener((RemovalListener<String, Base64Renderer>) removalNotification -> {
                Base64Renderer renderer = (Base64Renderer) removalNotification.getValue();
                if (renderer != null) {
                    renderer.reset();
                }
            })

            .build();
    private String base64String;
    private ITextureObject dynamicImage;
    private ResourceLocation resourceLocation;
    private ResourceLocation fallbackResource;
    private boolean interpolateLinear = false;
    private int x;
    private int y;
    private int width;
    private int height;
    private ThreadDownloadImageData albumCover;
    private final Map<ResourceLocation, DynamicTexture> dynamicTextures = Maps.newHashMap();

    public Base64Renderer() {
        this(new ResourceLocation("textures/track.png"));
    }

    public Base64Renderer(ResourceLocation fallbackResource) {
        this(fallbackResource, 64, 64);
    }

    public Base64Renderer(int width, int height) {
        this(new ResourceLocation("textures/track.png"), width, height);
    }

    public Base64Renderer(ResourceLocation fallbackResource, int width, int height) {
        this.fallbackResource = fallbackResource;
        this.width = width;
        this.height = height;
    }

    public void renderImage() {
        renderImage(this.x, this.y, this.width, this.height);
    }

    public void renderImage(int x, int y, int width, int height) {
        renderImage(x, y, width, height, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderImage(int x, int y, int width, int height, float r, float g, float b, float a) {
        if (this.dynamicImage == null) {
            if (this.fallbackResource != null) {
                render(x, y, width, height, this.fallbackResource, r, g, b, a);
            }
        } else {
            render(x, y, width, height, this.resourceLocation, r, b, g, a);
        }
    }

    private void render(int x, int y, int width, int height, ResourceLocation resource, float r, float g, float b, float a) {
        bindTexture(resource);
        GlStateManager.color(r, g, b, a);
        GlStateManager.disableBlend();
        if (this.interpolateLinear) {
            GL11.glTexParameteri(3553, 10240, 9729);
            GL11.glTexParameteri(3553, 10241, 9986);
        }
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, width, height);
        if (this.interpolateLinear) {
            GL11.glTexParameteri(3553, 10240, 9728);
            GL11.glTexParameteri(3553, 10241, 9984);
        }
    }

    public String getURLString() {
        return this.base64String;
    }

    public void setBase64String(String base64String, String resourceLocation) {
        this.base64String = base64String;
        this.resourceLocation = new ResourceLocation("exhi", resourceLocation);
        this.dynamicImage = (ITextureObject) getTexture(this.resourceLocation);
        prepareImage();
    }

    public Object getTexture(Object resourceLocation) {
        if (((resourceLocation instanceof ResourceLocation)) && (((ResourceLocation) resourceLocation).getResourceDomain().equals("exhi")) && (this.dynamicTextures.containsKey(resourceLocation))) {
            return this.dynamicTextures.get(resourceLocation);
        }
        return TextureUtils.getTexture((ResourceLocation) resourceLocation);
    }

    public void setBase64String(String base64String, ResourceLocation resourceLocation) {
        this.base64String = base64String;
        this.resourceLocation = resourceLocation;
        this.dynamicImage = (ITextureObject) getTexture(this.resourceLocation);
        prepareImage();
    }

    public void reset() {
        delete(this.resourceLocation);
        this.base64String = null;
        this.resourceLocation = null;
        this.dynamicImage = null;
    }

    private void prepareImage() {
        if (this.base64String == null) {
            this.delete(this.resourceLocation);
            this.dynamicImage = null;
            return;
        }
        final ByteBuf localByteBuf1 = Unpooled.copiedBuffer((CharSequence) this.base64String, Charsets.UTF_8);
        ByteBuf localByteBuf2 = null;
        BufferedImage localBufferedImage;
        try {
            localByteBuf2 = Base64.decode(localByteBuf1);
            localBufferedImage = read((InputStream) new ByteBufInputStream(localByteBuf2));
            Validate.validState(localBufferedImage.getWidth() == this.width, "Must be " + this.width + " pixels wide", new Object[0]);
            Validate.validState(localBufferedImage.getHeight() == this.height, "Must be " + this.height + " pixels high", new Object[0]);
        } catch (Exception e) {
            System.out.println("Could not prepare base64 renderer image");
            e.printStackTrace();
            this.delete(this.resourceLocation);
            this.dynamicImage = null;
            return;
        } finally {
            localByteBuf1.release();
            if (localByteBuf2 != null) {
                localByteBuf2.release();
            }
        }
        if (this.dynamicImage == null) {
            this.dynamicImage = (ITextureObject) createDynamicImage(this.resourceLocation, this.width, this.height);
        }
        fillDynamicImage(this.dynamicImage, localBufferedImage);
    }

    public void fillDynamicImage(Object dynamicImage, BufferedImage image) {
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), ((DynamicTexture) dynamicImage).getTextureData(), 0, image.getWidth());
        ((DynamicTexture) dynamicImage).updateDynamicTexture();
    }

    public Object createDynamicImage(Object resourceLocation, int width, int height) {
        DynamicTexture dynamicImage = new DynamicTexture(width, height);
        if (((ResourceLocation) resourceLocation).getResourceDomain().equals("exhi")) {
            this.dynamicTextures.put((ResourceLocation) resourceLocation, dynamicImage);
        } else {
            Minecraft.getMinecraft().getTextureManager().loadTexture((ResourceLocation) resourceLocation, dynamicImage);
        }
        return dynamicImage;
    }

    public void bindTexture(Object resourceLocation) {
        if (((resourceLocation instanceof ResourceLocation)) && (((ResourceLocation) resourceLocation).getResourceDomain().equals("exhi")) && (this.dynamicTextures.containsKey(resourceLocation))) {
            GlStateManager.bindTexture(this.dynamicTextures.get(resourceLocation).getGlTextureId());
        } else {
            if (resourceLocation instanceof ResourceLocation) {
                Minecraft.getMinecraft().getTextureManager().bindTexture((ResourceLocation) resourceLocation);
            }
        }
    }

    public void deleteTexture(Object resourceLocation) {
        if (((resourceLocation instanceof ResourceLocation)) && (((ResourceLocation) resourceLocation).getResourceDomain().equals("exhi")) && (this.dynamicTextures.containsKey(resourceLocation))) {
            this.dynamicTextures.remove(resourceLocation);
            Minecraft.getMinecraft().getTextureManager().deleteTexture((ResourceLocation) resourceLocation);
        }
    }

    private static BufferedImage read(InputStream byteBuf)
            throws IOException {
        try {
            return ImageIO.read(byteBuf);
        } finally {
            IOUtils.closeQuietly(byteBuf);
        }
    }

    private void delete(ResourceLocation resource) {
        deleteTexture(resource);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidthAndHeight(int size) {
        this.width = size;
        this.height = size;
    }

    public boolean isInterpolateLinear() {
        return this.interpolateLinear;
    }

    public void setInterpolateLinear(boolean interpolateLinear) {
        this.interpolateLinear = interpolateLinear;
    }

    public static Base64Renderer getRenderer(BufferedImage icon, String id) {
        Base64Renderer renderer = (Base64Renderer) CACHE.getIfPresent(id);
        if (renderer != null) {
            return renderer;
        }
        Base64Renderer finalRenderer = new Base64Renderer(null, icon.getWidth(), icon.getHeight());
        CACHE.put(id, finalRenderer);
        try {
            ByteBuf decodedBuffer = Unpooled.buffer();
            ImageIO.write(icon, "PNG", new ByteBufOutputStream(decodedBuffer));
            ByteBuf encodedBuffer = Base64.encode(decodedBuffer);
            String imageDataString = encodedBuffer.toString(Charsets.UTF_8);

            finalRenderer.setBase64String(imageDataString, id);
        } catch (Exception e) {
            System.out.println("Could not load icon " + id + e);
        }
        return finalRenderer;
    }
}

