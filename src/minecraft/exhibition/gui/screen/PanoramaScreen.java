package exhibition.gui.screen;

import exhibition.util.render.ColorContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class PanoramaScreen extends GuiScreen {
	private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[] { new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png") };
	protected static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
	private DynamicTexture viewportTexture;
	private ResourceLocation panoramaView;
	protected static int panoramaTimer;

	public void updateScreen() {

	}

	@Override
	public void initGui() {
		this.viewportTexture = new DynamicTexture(256, 256);
		this.panoramaView = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		panoramaTimer++;
		GL11.glColor3f(1F, 1F, 1F);
		drawButtons(mouseX, mouseY);
		GL11.glColor3f(1F, 1F, 1F);
		drawTitle();
	}

	protected void drawTitle() {

	}

	protected void drawButtons(int mouseX, int mouseY) {
//		int width = 150;
//		int hei = 26;
//		boolean override = false;
//		for (int i = 0; i < this.buttonList.size(); ++i) {
//			GuiButton g = ((GuiButton) this.buttonList.get(i));
//			if (override) {
//				int x = g.xPosition;
//				int y = g.yPosition;
//				boolean over = mouseX >= x && mouseY >= y && mouseX < x + g.getButtonWidth() && mouseY < y + hei;
//				if (over) fillHorizontalGrad(x, y, width, hei, new ColorContainer(5, 40, 85, 255), new ColorContainer(0, 0, 0, 0));
//				else fillHorizontalGrad(x, y, width, hei, new ColorContainer(0, 0, 0, 255), new ColorContainer(0, 0, 0, 0));
//				fontRendererObj.drawString(g.displayString, g.xPosition + 10, g.yPosition + (hei / 2) - 3, -1);
//			}else{
//				g.drawButton(mc, mouseX, mouseY);
//			}
//		}
	}

	public void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_)
	{
		Tessellator var4 = Tessellator.getInstance();
		WorldRenderer var5 = var4.getWorldRenderer();
		GlStateManager.matrixMode(5889);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		byte var6 = 8;

		for (int var7 = 0; var7 < var6 * var6; ++var7)
		{
			GlStateManager.pushMatrix();
			float var8 = ((float)(var7 % var6) / (float)var6 - 0.5F) / 64.0F;
			float var9 = ((float)(var7 / var6) / (float)var6 - 0.5F) / 64.0F;
			float var10 = 0.0F;
			GlStateManager.translate(var8, var9, var10);
			GlStateManager.rotate(MathHelper.sin(((float)(this.panoramaTimer) + p_73970_3_) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-((float)this.panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

			for (int var11 = 0; var11 < 6; ++var11)
			{
				GlStateManager.pushMatrix();

				if (var11 == 1)
				{
					GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				}

				if (var11 == 2)
				{
					GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				}

				if (var11 == 3)
				{
					GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				}

				if (var11 == 4)
				{
					GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				}

				if (var11 == 5)
				{
					GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				}

				this.mc.getTextureManager().bindTexture(titlePanoramaPaths[var11]);
				var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				int l = 255 / (var7 + 1);
				float f3 = 0.0F;
				var5.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
				var5.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
				var5.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
				var5.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
				var4.draw();
				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
			GlStateManager.colorMask(true, true, true, false);
		}

		var5.setTranslation(0.0D, 0.0D, 0.0D);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.matrixMode(5889);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.enableDepth();
	}

	/**
	 * Rotate and blurs the skybox view in the bruh menu
	 */
	private void rotateAndBlurSkybox(float p_73968_1_)
	{
		this.mc.getTextureManager().bindTexture(this.panoramaView);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.colorMask(true, true, true, false);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		GlStateManager.disableAlpha();
		int i = 3;

		for (int j = 0; j < i; ++j)
		{
			float f = 1.0F / (float)(j + 1);
			int k = this.width;
			int l = this.height;
			float f1 = (float)(j - i / 2) / 256.0F;
			worldrenderer.pos((double)k, (double)l, (double)this.zLevel).tex((double)(0.0F + f1), 1.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
			worldrenderer.pos((double)k, 0.0D, (double)this.zLevel).tex((double)(1.0F + f1), 1.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
			worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(1.0F + f1), 0.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
			worldrenderer.pos(0.0D, (double)l, (double)this.zLevel).tex((double)(0.0F + f1), 0.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
		}

		tessellator.draw();
		GlStateManager.enableAlpha();
		GlStateManager.colorMask(true, true, true, true);
	}

	/**
	 * Renders the skybox in the bruh menu
	 */
	public void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_)
	{
		this.mc.getFramebuffer().unbindFramebuffer();
		GlStateManager.viewport(0, 0, 256, 256);
		this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.rotateAndBlurSkybox(p_73971_3_);
		this.mc.getFramebuffer().bindFramebuffer(true);
		GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		float f = this.width > this.height ? 120.0F / (float)this.width : 120.0F / (float)this.height;
		float f1 = (float)this.height * f / 256.0F;
		float f2 = (float)this.width * f / 256.0F;
		int i = this.width;
		int j = this.height;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(0.0D, (double)j, (double)this.zLevel).tex((double)(0.5F - f1), (double)(0.5F + f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		worldrenderer.pos((double)i, (double)j, (double)this.zLevel).tex((double)(0.5F - f1), (double)(0.5F - f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		worldrenderer.pos((double)i, 0.0D, (double)this.zLevel).tex((double)(0.5F + f1), (double)(0.5F - f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(0.5F + f1), (double)(0.5F + f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
		tessellator.draw();
	}

}
