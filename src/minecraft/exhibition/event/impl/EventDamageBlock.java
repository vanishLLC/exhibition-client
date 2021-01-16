/**
 * Time: 8:46:54 PM
 * Date: Jan 3, 2017
 * Creator: cool1
 */
package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class EventDamageBlock extends Event {

	private BlockPos currentBlock;
	private EnumFacing direction;
	private float progress;

	public void fire(BlockPos b, EnumFacing direction, float progress) {
		setCurrentBlock(b);
		setDirection(direction);
		setProgress(progress);
		super.fire();
	}

	public BlockPos getBlockPos() {
		return this.currentBlock;
	}

	public void setCurrentBlock(BlockPos currentBlock) {
		this.currentBlock = currentBlock;
	}

	public EnumFacing getDirection() {
		return direction;
	}

	public void setDirection(EnumFacing direction) {
		this.direction = direction;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

}
