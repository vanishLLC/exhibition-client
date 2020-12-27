package exhibition;


import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.LWJGLException;


public class MCHook extends Minecraft {
	public MCHook(GameConfiguration gc) {
		super(gc);
	}
	
	@Override
	protected void drawSplashScreen(TextureManager texMan) throws LWJGLException {
		try {
			ProgressScreen progressScreenTask = new ProgressScreen(texMan,this);
			Client.instance = new Client(progressScreenTask);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		super.drawSplashScreen(texMan);
	}

}
