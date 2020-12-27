package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.management.ColorManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.ScaledResolution;

public class Crosshair extends Module {

	private String GAP = "GAP";
	private String WIDTH = "WIDTH";
	private String SIZE = "SIZE";
	private String DYNAMIC = "DYNAMIC";


	public Crosshair(ModuleData data) {
		super(data);
		settings.put(DYNAMIC, new Setting<>(DYNAMIC, true, "Expands when moving."));
		settings.put(GAP, new Setting<>(GAP, 5,"Crosshair Gap", 0.25, 0.25, 15));
		settings.put(WIDTH, new Setting<>(WIDTH, 2,"Crosshair Width", 0.25, 0.25, 10));
		settings.put(SIZE, new Setting<>(SIZE, 7,"Crosshair Size/Length", 0.25, 0.25, 15));
	}

	@Override
	public Priority getPriority() {
		return Priority.HIGH;
	}

	@RegisterEvent(events = { EventRenderGui.class })
	public void onEvent(Event event) {
		double gap = ((Number)settings.get(GAP).getValue()).doubleValue();
		double width = ((Number)settings.get(WIDTH).getValue()).doubleValue();
		double size = ((Number)settings.get(SIZE).getValue()).doubleValue();

		ScaledResolution scaledRes = new ScaledResolution(mc);
		// Top
		RenderingUtil.rectangleBordered(
				scaledRes.getScaledWidth() / 2F - width,
				scaledRes.getScaledHeight() / 2F - gap - size  - (isMoving() ? 2 : 0) ,
				scaledRes.getScaledWidth() / 2F + 1.0f + width,
				scaledRes.getScaledHeight() / 2F - gap - (isMoving() ? 2 : 0) ,
				0.5f, ColorManager.xhair.getColorHex(),
				Colors.getColor(0,0,0, ColorManager.xhair.alpha));
		// Bottom
		RenderingUtil.rectangleBordered(
				scaledRes.getScaledWidth() / 2F - width,
				scaledRes.getScaledHeight() / 2F + gap + 1 + (isMoving() ? 2 : 0) - 0.15,
				scaledRes.getScaledWidth() / 2F + 1.0f + width,
				scaledRes.getScaledHeight() / 2F + 1 + gap + size + (isMoving() ? 2 : 0) - 0.15, 0.5f, ColorManager.xhair.getColorHex(),
				Colors.getColor(0,0,0, ColorManager.xhair.alpha));
		// Left
		RenderingUtil.rectangleBordered(
				scaledRes.getScaledWidth() / 2F - gap - size - (isMoving() ? 2 : 0) + 0.15,
				scaledRes.getScaledHeight() / 2F - width,
				scaledRes.getScaledWidth() / 2F - gap - (isMoving() ? 2 : 0) + 0.15,
				scaledRes.getScaledHeight() / 2F + 1.0f + width, 0.5f, ColorManager.xhair.getColorHex() ,
				Colors.getColor(0,0,0, ColorManager.xhair.alpha));
		// Right
		RenderingUtil.rectangleBordered(
				scaledRes.getScaledWidth() / 2F + 0.5 + gap + (isMoving() ? 2 : 0),
				scaledRes.getScaledHeight() / 2F - width,
				scaledRes.getScaledWidth() / 2F + size + gap + 0.5 + (isMoving() ? 2 : 0),
				scaledRes.getScaledHeight() / 2F + 1.0f +  width, 0.5f, ColorManager.xhair.getColorHex(),
				Colors.getColor(0,0,0, ColorManager.xhair.alpha));
	}

	public boolean isMoving() {
		return (Boolean) settings.get(DYNAMIC).getValue() && (!mc.thePlayer.isCollidedHorizontally) && (!mc.thePlayer.isSneaking()) && ((mc.thePlayer.movementInput.moveForward != 0.0F) || (mc.thePlayer.movementInput.moveStrafe != 0.0F));
	}

}
