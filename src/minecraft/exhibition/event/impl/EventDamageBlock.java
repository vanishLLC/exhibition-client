/**
 * Time: 8:46:54 PM
 * Date: Jan 3, 2017
 * Creator: cool1
 */
package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.util.BlockPos;

public class EventDamageBlock extends Event {

	private BlockPos currentBlock;

	public void fire(BlockPos b) {
		setCurrentBlock(b);
		super.fire();
	}

	public BlockPos getCurrentBlock() {
		return this.currentBlock;
	}

	public void setCurrentBlock(BlockPos currentBlock) {
		this.currentBlock = currentBlock;
	}

}
