package exhibition.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.util.List;

public class RotationUtils {
    public static float[] getRotations(EntityLivingBase ent) {
        double x = ent.posX;
        double z = ent.posZ;
        double y = ent.posY + ent.getEyeHeight() / 2.0F;
        return getRotationFromPosition(x, z, y);
    }

    public static float[] getAverageRotations(List<EntityLivingBase> targetList) {
        double posX = 0.0D;
        double posY = 0.0D;
        double posZ = 0.0D;
        for (Entity ent : targetList) {
            posX += ent.posX;
            posY += ent.boundingBox.maxY - 2.0D;
            posZ += ent.posZ;
        }
        posX /= targetList.size();
        posY /= targetList.size();
        posZ /= targetList.size();

        return new float[]{getRotationFromPosition(posX, posZ, posY)[0], getRotationFromPosition(posX, posZ, posY)[1]};
    }

    public static float[] getBowAngles(final Entity entity) {
        final double xDelta = entity.posX - entity.lastTickPosX;
        final double zDelta = entity.posZ - entity.lastTickPosZ;
        final double yDelta = entity.posY - entity.lastTickPosY;

        double d = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
        d -= d % 0.8;
        double xMulti = d / 0.8 * xDelta * 0.66;
        double zMulti = d / 0.8 * zDelta * 0.66;
        double yMulti = yDelta;
        if((!entity.onGround && !entity.isCollidedVertically) && yDelta < 0 && entity.fallDistance > 1) {
            xMulti *= 0.15;
            zMulti *= 0.15;
            yMulti += yMulti * 0.98 - 0.08;
        }
        double x = entity.posX + xMulti - Minecraft.getMinecraft().thePlayer.posX;
        double z = entity.posZ + zMulti - Minecraft.getMinecraft().thePlayer.posZ;
        final double y = (entity.posY + entity.getEyeHeight() + yMulti) - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());
        final float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        final float pitch = getTrajAngleSolutionLow(x, z, y, 2.93);
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2;

        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
        return new float[]{yaw, pitch};
    }

    public static float getTrajAngleSolutionLow(double d3, double d1, double dy, double velocity) {
        double gravity = 0.05000000074505806;
        double xDelta = d3;
        double zDelta = d1;
        double dist = Math.sqrt(xDelta * xDelta + zDelta * zDelta);
        double y = dy;
        double yv = 2.0 * y * (velocity * velocity);
        double gx = gravity * (dist * dist);
        double sqrt = Math.sqrt(velocity * velocity * velocity * velocity - gravity * (gx + yv));
        double v1 = velocity * velocity + sqrt;
        double v2 = velocity * velocity - sqrt;
        double a1 = Math.atan2(v1, gravity * dist);
        double a2 = Math.atan2(v2, gravity * dist);
        return (float) -Math.toDegrees(Math.min(a1, a2));
    }

    public static float getYawChange(double posX, double posZ) {
        return getYawChangeGiven(posX, posZ, Minecraft.getMinecraft().thePlayer.rotationYaw);
    }

    public static float getYawChangeGiven(double posX, double posZ, float yaw) {
        double deltaX = posX - Minecraft.getMinecraft().thePlayer.posX;
        double deltaZ = posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double yawToEntity;
        if ((deltaZ < 0.0D) && (deltaX < 0.0D)) {
            yawToEntity = 90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
        } else if ((deltaZ < 0.0D) && (deltaX > 0.0D)) {
            yawToEntity = -90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX));
        } else {
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }
        return MathHelper.wrapAngleTo180_float(-(yaw - (float) yawToEntity));
    }

    public static float getPitchChangeGiven(Entity entity, double posY) {
        double xDiff = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double yDiff = (entity.posY + entity.getEyeHeight()) - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double zDiff = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;

        double dist = Math.hypot(xDiff, zDiff);
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / 3.141592653589793D);
        return -MathHelper.wrapAngleTo180_float(Minecraft.getMinecraft().thePlayer.rotationPitch - pitch);
    }

    public static float getPitchChange(Entity entity, double posY) {
        double deltaX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double deltaZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double deltaY = posY - 2.2D + entity.getEyeHeight() - Minecraft.getMinecraft().thePlayer.posY;
        double distanceXZ = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));
        return -MathHelper.wrapAngleTo180_float(Minecraft.getMinecraft().thePlayer.rotationPitch - (float) pitchToEntity) - 2.5F;
    }

    public static float getNewAngle(float angle) {
        angle %= 360.0F;
        if (angle >= 180.0F) {
            angle -= 360.0F;
        }
        if (angle < -180.0F) {
            angle += 360.0F;
        }
        return angle;
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle = Math.abs(angle1 - angle2) % 360.0F;
        if (angle > 180.0F) {
            angle = 360.0F - angle;
        }
        return angle;
    }
}

