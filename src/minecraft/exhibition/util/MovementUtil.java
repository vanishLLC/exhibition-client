package exhibition.util;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class MovementUtil {

    public void moveEntityWithHeading(EntityPlayer player, float strafe, float forward) {
        World worldObj = Minecraft.getMinecraft().thePlayer.worldObj;
        if (!player.isInWater() || player.capabilities.isFlying) {
            if (!player.isInLava() || player.capabilities.isFlying) {
                float f4 = 0.91F;

                if (player.onGround) {
                    f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(player.posZ))).getBlock().slipperiness * 0.91F;
                }

                float f = 0.16277136F / (f4 * f4 * f4);
                float f5;

                if (player.onGround) {
                    f5 = player.getAIMoveSpeed() * f;
                } else {
                    f5 = player.jumpMovementFactor;
                }

                player.moveFlying(strafe, forward, f5);
                f4 = 0.91F;

                if (player.onGround) {
                    f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(player.posZ))).getBlock().slipperiness * 0.91F;
                }

                if (player.isOnLadder()) {
                    float f6 = 0.15F;
                    player.motionX = MathHelper.clamp_double(player.motionX, (double) (-f6), (double) f6);
                    player.motionZ = MathHelper.clamp_double(player.motionZ, (double) (-f6), (double) f6);
                    player.fallDistance = 0.0F;

                    if (player.motionY < -0.15D) {
                        player.motionY = -0.15D;
                    }

                    boolean flag = player.isSneaking() && player instanceof EntityPlayer;

                    if (flag && player.motionY < 0.0D) {
                        player.motionY = 0.0D;
                    }
                }

                player.moveEntity(player.motionX, player.motionY, player.motionZ);

                if (player.isCollidedHorizontally && player.isOnLadder()) {
                    player.motionY = 0.2D;
                }

                if (worldObj.isRemote && (!worldObj.isBlockLoaded(new BlockPos((int) player.posX, 0, (int) player.posZ)) || !worldObj.getChunkFromBlockCoords(new BlockPos((int) player.posX, 0, (int) player.posZ)).isLoaded())) {
                    if (player.posY > 0.0D) {
                        player.motionY = -0.1D;
                    } else {
                        player.motionY = 0.0D;
                    }
                } else {
                    player.motionY -= 0.08D;
                }

                player.motionY *= 0.9800000190734863D;
                player.motionX *= (double) f4;
                player.motionZ *= (double) f4;
            } else {
                double d1 = player.posY;
                player.moveFlying(strafe, forward, 0.02F);
                player.moveEntity(player.motionX, player.motionY, player.motionZ);
                player.motionX *= 0.5D;
                player.motionY *= 0.5D;
                player.motionZ *= 0.5D;
                player.motionY -= 0.02D;

                if (player.isCollidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + d1, player.motionZ)) {
                    player.motionY = 0.30000001192092896D;
                }
            }
        } else {
            double d0 = player.posY;
            float f1 = 0.8F;
            float f2 = 0.02F;
            float f3 = (float) EnchantmentHelper.getDepthStriderModifier(player);

            if (f3 > 3.0F) {
                f3 = 3.0F;
            }

            if (!player.onGround) {
                f3 *= 0.5F;
            }

            if (f3 > 0.0F) {
                f1 += (0.54600006F - f1) * f3 / 3.0F;
                f2 += (player.getAIMoveSpeed() * 1.0F - f2) * f3 / 3.0F;
            }

            player.moveFlying(strafe, forward, f2);
            player.moveEntity(player.motionX, player.motionY, player.motionZ);
            player.motionX *= (double) f1;
            player.motionY *= 0.800000011920929D;
            player.motionZ *= (double) f1;
            player.motionY -= 0.02D;

            if (player.isCollidedHorizontally && player.isOffsetPositionInLiquid(player.motionX, player.motionY + 0.6000000238418579D - player.posY + d0, player.motionZ)) {
                player.motionY = 0.30000001192092896D;
            }
        }

    }

}
