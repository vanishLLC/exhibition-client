package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.pathfinding.Node;
import exhibition.pathfinding.TeleportResult;
import exhibition.pathfinding.Utils;
import exhibition.util.MovementUtil;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

public class MotionPrediction extends Module {

    private final List<Vec3> positions = new ArrayList<>();

    public MotionPrediction(ModuleData data) {
        super(data);
    }

    private EntityPlayer fakePlayer;

    @RegisterEvent(events = {EventRender3D.class, EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost())
                return;

            this.positions.clear();

            this.fakePlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());

            this.fakePlayer.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            this.fakePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            this.fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
            this.fakePlayer.onGround = mc.thePlayer.onGround;
            this.fakePlayer.motionX = mc.thePlayer.motionX;
            this.fakePlayer.motionY = mc.thePlayer.motionY;
            this.fakePlayer.motionZ = mc.thePlayer.motionZ;

            NBTTagCompound bruh = new NBTTagCompound();
            mc.thePlayer.capabilities.writeCapabilitiesToNBT(bruh);
            this.fakePlayer.capabilities.readCapabilitiesFromNBT(bruh);

            this.fakePlayer.stepHeight = mc.thePlayer.stepHeight;

            this.fakePlayer.moveForward = mc.thePlayer.moveForward;
            this.fakePlayer.moveStrafing = mc.thePlayer.moveStrafing;
            this.fakePlayer.worldObj = mc.thePlayer.worldObj;
            this.fakePlayer.noClip = false;

            // Some pre-defined values for testing purposes
            this.fakePlayer.rotationYaw = -351.24377F;
            this.fakePlayer.rotationPitch = 24.723082F;
            this.fakePlayer.onGround = false;
            this.fakePlayer.motionX = 0.53163250483572483;
            //this.fakePlayer.motionY = 0.42F;
            this.fakePlayer.motionY = 0.5F;
            this.fakePlayer.motionZ = 0.3184545369893313;
            this.fakePlayer.moveForward = 0.0F;
            this.fakePlayer.moveStrafing = 0.0F;

            double velocity = MathHelper.sqrt_double(this.fakePlayer.motionX * this.fakePlayer.motionX + this.fakePlayer.motionZ * this.fakePlayer.motionZ);

            // Allow for the rotation of the X/Z velocity
            float degrees = 120;

            double motionX = velocity * Math.sin(Math.toRadians(degrees));
            double motionZ = velocity * Math.cos(Math.toRadians(degrees));

            this.fakePlayer.motionX = motionX;
            this.fakePlayer.motionZ = motionZ;

            // Generate 11 ticks worth of movement prediction
            for (int i = 0; i < 11; i++) {
                MovementUtil.simulateMovementTick(this.fakePlayer);
                Vec3 posDifference = this.fakePlayer.getPositionVector().subtract(mc.thePlayer.getPositionVector());

                if(fakePlayer.motionY <= 0) {
                    this.positions.add(posDifference);
                }
            }

        } else {
            if (this.positions.isEmpty())
                return;

            GL11.glPushMatrix();
            RenderingUtil.pre3D();
            GL11.glLineWidth(1.5F);

            BlockPos firstTargetPos = null;
            BlockPos finalPos = null;
            Vec3 finalVec = null;

            List<BlockPos> shortestPath = null;

            for (Vec3 predPos : this.positions) {

                AxisAlignedBB playerBB = mc.thePlayer.getEntityBoundingBox().offset(predPos.getX(), predPos.getY(), predPos.getZ());

                BlockPos playerPos = new BlockPos(mc.thePlayer.posX + predPos.getX(), mc.thePlayer.posY + predPos.getY() - 0.5, mc.thePlayer.posZ + predPos.getZ());

                IBlockState blockState = mc.theWorld.getBlockState(playerPos);

                AxisAlignedBB bb = new AxisAlignedBB(playerPos.getX(), playerPos.getY(), playerPos.getZ(),
                        playerPos.getX() + 1, playerPos.getY() + 1, playerPos.getZ() + 1);

                if (!bb.intersectsWith(playerBB)) {
                    if (blockState.getMaterial().isReplaceable()) {

//                        RenderingUtil.glColor(Colors.getColor(255, 150, 160,50));
//                        RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(playerPos.getX(), playerPos.getY(), playerPos.getZ(),
//                                playerPos.getX() + 1, playerPos.getY() + 1, playerPos.getZ() + 1).
//                                offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));

//                        List<BlockPos> pathToTarget = new ArrayList<>();
//
//                        // Ghetto way to search for neighbor blocks
//                        EnumFacing currentFacing = fakePlayer.getHorizontalFacing();
//                        EnumFacing[] values = new EnumFacing[5];
//                        values[0] = EnumFacing.DOWN;
//                        for(int i = 1; i < 5; i++) {
//                            values[i] = currentFacing;
//                            currentFacing = currentFacing.rotateY();
//                        }
//
//                        // Look for the closest neighbor
//                        outer:
//                        for (EnumFacing f1 : values) {
//
//                            BlockPos f1Pos = playerPos.offset(f1);
//
//                            IBlockState f1State = mc.theWorld.getBlockState(f1Pos);
//
//                            if (!f1State.getMaterial().isReplaceable()) {
//                                pathToTarget.add(playerPos);
//                                pathToTarget.add(f1Pos);
//                                break;
//                            } else {
//
//                                // Instead of the values array being used, start from the f1 facing?
//                                for (EnumFacing f2 : values) {
//
//                                    BlockPos f2Pos = f1Pos.offset(f2);
//
//                                    IBlockState f2State = mc.theWorld.getBlockState(f2Pos);
//
//                                    if (!f2State.getMaterial().isReplaceable()) {
//                                        pathToTarget.add(playerPos);
//                                        pathToTarget.add(f1Pos);
//                                        pathToTarget.add(f2Pos);
//                                        break outer;
//                                    } else {
//                                        for (EnumFacing f3 : values) {
//                                            BlockPos f3Pos = f2Pos.offset(f3);
//
//                                            IBlockState f3State = mc.theWorld.getBlockState(f3Pos);
//
//                                            if (!f3State.getMaterial().isReplaceable()) {
//                                                pathToTarget.add(playerPos);
//                                                pathToTarget.add(f1Pos);
//                                                pathToTarget.add(f2Pos);
//                                                pathToTarget.add(f3Pos);
//                                                break outer;
//                                            }
//                                        }
//                                    }
//
//                                }
//                            }
//                        }
//
//                        // Just shows the lines
//                        if (!pathToTarget.isEmpty()) {
//                            RenderingUtil.glColor(-1);
//                            GL11.glBegin(GL11.GL_LINE_STRIP);
//                            for (BlockPos blockPos : pathToTarget) {
//                                GL11.glVertex3d(blockPos.getX() + 0.5 - RenderManager.renderPosX,
//                                        blockPos.getY() + 0.5 - RenderManager.renderPosY,
//                                        blockPos.getZ() + 0.5 - RenderManager.renderPosZ);
//                            }
//                            GL11.glEnd();
//
//                            if(shortestPath == null || shortestPath.size() > pathToTarget.size()) {
//                                shortestPath = pathToTarget;
//                            }
//                        }

                        finalPos = playerPos;
                        finalVec = predPos.add(mc.thePlayer.getPositionVector());
                    }
                }

                RenderingUtil.glColor(finalPos == null ? 0xbbff4444 : -1);
                RenderingUtil.drawOutlinedBoundingBox(playerBB.offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));

            }

            if (finalPos != null) {
                BlockPos closestPos = null;
                for(int x = -3; x <= 3; x++) {
                    for(int z = -3; z <= 3; z++) {
                        for(int y = 0; y > -3; y--) {

                            int px = MathHelper.floor_double(finalVec.getX() + x), py = MathHelper.floor_double(finalVec.getY() + y), pz = MathHelper.floor_double(finalVec.getZ() + z);

                            BlockPos playerPos = new BlockPos(px, py, pz);

                            boolean a = mc.theWorld.getBlockState(playerPos).getMaterial().isReplaceable();


                            if(!a)
                            if(closestPos == null || fakePlayer.getDistance(playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5) <
                                    fakePlayer.getDistance(closestPos.getX() + 0.5, closestPos.getY() + 0.5, closestPos.getZ() + 0.5)) {
                                closestPos = playerPos;
                            }


                            RenderingUtil.glColor(Colors.getColor(0, 160, 255, a ? 50 : 255));
                            RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(px, py, pz, px + 1, py + 1, pz + 1).
                                    offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));
                        }
                    }
                }

                if(closestPos != null) {
                    RenderingUtil.glColor(Colors.getColor(0, 255, 0));
                    RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(closestPos.getX(), closestPos.getY(), closestPos.getZ(), closestPos.getX() + 1,
                            closestPos.getY() + 1, closestPos.getZ() + 1).
                            offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));

                    TeleportResult result = Utils.pathFinderTeleportTo(new Vec3(finalPos), new Vec3(closestPos));

                    if(result.foundPath) {
                        RenderingUtil.glColor(Colors.getColor(0,255,0,150));
                        GL11.glLineWidth(3F);
                        GL11.glBegin(GL_LINE_STRIP);
                        for (Vec3 position : result.positions) {
                            double x = position.getX() - RenderManager.renderPosX;
                            double y = position.getY() - RenderManager.renderPosY - 0.5;
                            double z = position.getZ() - RenderManager.renderPosZ;
                            GL11.glVertex3d(x, y, z);
                        }
                        GL11.glEnd();

                        GL11.glLineWidth(1.5F);
                        for (Node triedPath : result.triedPaths) {
                            RenderingUtil.glColor(Colors.getColor(255, 255, 160));
                            Vec3 pos = new Vec3(triedPath.getBlockpos());
                            RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(pos.getX() + 0.45, pos.getY() + 0.45 - 1, pos.getZ() + 0.45,
                                    pos.getX() + 0.55, pos.getY() + 0.55 - 1, pos.getZ() + 0.55).
                                    offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));
                        }
                    }
                }

                GL11.glLineWidth(1.5F);
                RenderingUtil.glColor(Colors.getColor(255, 255, 160));
                RenderingUtil.drawOutlinedBoundingBox(new AxisAlignedBB(finalPos.getX(), finalPos.getY(), finalPos.getZ(),
                        finalPos.getX() + 1, finalPos.getY() + 1, finalPos.getZ() + 1).
                        offset(-RenderManager.renderPosX, -RenderManager.renderPosY, -RenderManager.renderPosZ));

            }

            RenderingUtil.post3D();
            GL11.glPopMatrix();
        }
    }

}
