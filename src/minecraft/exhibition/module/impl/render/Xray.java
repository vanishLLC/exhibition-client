package exhibition.module.impl.render;

import com.google.common.collect.Lists;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventTick;
import exhibition.module.Module;
import exhibition.module.data.BlockList;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Xray extends Module {
	private String KEY_OPACITY = "OPACITY";
	private String CAVEFINDER = "CAVE";
	private String BLOCKLIST = "BLOCKLIST";
	private int opacity = 160;
	List<Integer> KEY_IDS = Lists.newArrayList(10, 11, 8, 9, 14, 15, 16, 21, 41, 42, 46, 48, 52, 56, 57, 61, 62, 73, 74, 84,89, 103, 116, 117, 118, 120,129, 133, 137,145, 152, 153, 154);
	private BlockList blockList;


	public Xray(ModuleData data) {
		super(data);
		settings.put(KEY_OPACITY, new Setting(KEY_OPACITY, 160, "Opacity for blocks you want to ignore.",  5, 0, 255));
		settings.put(CAVEFINDER, new Setting<>(CAVEFINDER, false, "Only show blocks touching air."));
		List<Block> list = new ArrayList<>();
		KEY_IDS.forEach(o -> list.add(Block.getBlockById(o)));
		blockList = new BlockList(list);
		settings.put(BLOCKLIST, new Setting<>(BLOCKLIST, blockList, "Blocks to only show when xraying."));
	}

	@Override
	public void onEnable() {
		opacity = ((Number) settings.get(KEY_OPACITY).getValue()).intValue();
		mc.renderGlobal.loadRenderers();
	}

	@Override
	public void onDisable() {
		mc.renderGlobal.loadRenderers();
	}

	@Override
	@RegisterEvent(events = { EventTick.class })
	public void onEvent(Event event) {

	}

	public boolean containsID(int id) {
		return blockList.isBlockInList(Block.getBlockById(id));
	}

	public int getOpacity() {
		return opacity;
	}
}
