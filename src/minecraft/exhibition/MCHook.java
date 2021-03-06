package exhibition;


import exhibition.util.security.Castable;
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
			try {
				Client.instance = new Client(new Object[]{Float.NaN, System.class, progressScreenTask, "gibberish", "retard", "im skidding", "wtf?", null, null});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		super.drawSplashScreen(texMan);
	}

}
