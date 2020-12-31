package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.movement.Phase;
import exhibition.module.impl.render.Freecam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AntiObby extends Module {

    public AntiObby(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = EventMotionUpdate.class)
    public void onEvent(Event event) {
        EventMotionUpdate em = event.cast();
        if (!mc.thePlayer.isAllowEdit() || Client.getModuleManager().isEnabled(Phase.class) || Client.getModuleManager().isEnabled(Freecam.class))
            return;

        IBlockState headblock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ));
        BlockPos belowPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ);

        if (headblock.getBlock() != Blocks.air && mc.theWorld.getBlockState(belowPos).getBlock() != Blocks.air) {
            if (headblock.getBlock().isBlockNormalCube()) {
                if (em.isPre()) {
                    em.setPitch(89.5F);
                } else {
                    mc.playerController.onPlayerDamageBlock(belowPos, EnumFacing.UP);
                }
            }
        }
    }

}
