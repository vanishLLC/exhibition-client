package exhibition.module.impl.player;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.impl.movement.Phase;
import exhibition.module.impl.render.Freecam;
import net.minecraft.block.BlockObsidian;
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
        if (!mc.thePlayer.isAllowEdit() || Client.getModuleManager().isEnabled(Phase.class) || Client.getModuleManager().isEnabled(Freecam.class)/* || Client.getModuleManager().isEnabled(FreecamTP.class)*/)
            return;

        IBlockState headblock = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ));

        if(headblock.getBlock() instanceof BlockObsidian) {
            double[][] offsets = new double[][]{new double[]{0, 0}, new double[]{-0.35, -0.35}, new double[]{-0.35, 0.35}, new double[]{0.35, 0.35}, new double[]{0.35, -0.35}};
            for (double[] offset : offsets) {
                BlockPos belowPos = new BlockPos(mc.thePlayer.posX + offset[0], mc.thePlayer.posY - 0.1, mc.thePlayer.posZ + offset[1]);
                if (headblock.getBlock() != Blocks.air && mc.theWorld.getBlockState(belowPos).getBlock() != Blocks.air) {
                    if (headblock.getBlock().isBlockNormalCube()) {
                        if (em.isPre()) {
                            em.setPitch(89.5F);
                            break;
                        } else {
                            mc.playerController.onPlayerDamageBlock(belowPos, EnumFacing.UP);
                            break;
                        }
                    }
                }
            }
        }
    }

}
