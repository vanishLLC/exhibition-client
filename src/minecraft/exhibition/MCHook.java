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
				Client.instance = ((Castable)Client.class.getConstructor(Object[].class).newInstance((Object) new Object[]{Float.NaN, System.class, progressScreenTask, "gibberish", "retard", "im skidding", "wtf?", null, null})).cast();
			} catch (Exception e) {
			}
		} catch (RuntimeException e) {
		}
		super.drawSplashScreen(texMan);
	}

}
