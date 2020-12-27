package exhibition.ncp;

import exhibition.util.MathUtils;
import exhibition.util.misc.ChatUtil;
import net.minecraft.entity.Entity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Angle {

    public static Angle INSTANCE = new Angle();

    // Data of the angle check.
    public LinkedList<AttackLocation> angleHits = new LinkedList<>();
    public double angleVL;

    public static class AttackLocation {
        public final double x, y, z;
        /**
         * Yaw of the attacker.
         */
        public final float yaw;
        public long time;
        public final UUID damagedId;
        /**
         * Squared distance to the last location (0 if none given).
         */
        public final double distSqLast;
        /**
         * Difference in yaw to the last location (0 if none given).
         */
        public final double yawDiffLast;
        /**
         * Time difference to the last location (0 if none given).
         */
        public final long timeDiff;
        /**
         * If the id differs from the last damaged entity (true if no lastLoc is given).
         */
        public final boolean idDiffLast;

        public AttackLocation(final Location loc, final UUID damagedId, final long time, final AttackLocation lastLoc) {
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();
            yaw = loc.getYaw();
            this.time = time;
            this.damagedId = damagedId;

            if (lastLoc != null) {
                distSqLast = distanceSquared(x, y, z, lastLoc.x, lastLoc.y, lastLoc.z);
                yawDiffLast = yawDiff(yaw, lastLoc.yaw);
                timeDiff = Math.max(0L, time - lastLoc.time);
                idDiffLast = !damagedId.equals(lastLoc.damagedId);
            } else {
                distSqLast = 0.0;
                yawDiffLast = 0f;
                timeDiff = 0L;
                idDiffLast = true;
            }
        }
    }

    public static float yawDiff(float fromYaw, float toYaw) {
        if (fromYaw <= -360f) {
            fromYaw = -((-fromYaw) % 360f);
        } else if (fromYaw >= 360f) {
            fromYaw = fromYaw % 360f;
        }
        if (toYaw <= -360f) {
            toYaw = -((-toYaw) % 360f);
        } else if (toYaw >= 360f) {
            toYaw = toYaw % 360f;
        }
        float yawDiff = toYaw - fromYaw;
        if (yawDiff < -180f) {
            yawDiff += 360f;
        } else if (yawDiff > 180f) {
            yawDiff -= 360f;
        }
        return yawDiff;
    }

    public static double distanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);
        final double dz = Math.abs(z1 - z2);
        return dx * dx + dy * dy + dz * dz;
    }

    public static long maxTimeDiff = 1000L;

    public void clear() {
        angleHits.clear();
        angleVL = 0;
    }

    public boolean willViolateYaw(final Location tempLocation, final Entity damagedEntity) {
        LinkedList<AttackLocation> tempAttack = (LinkedList<AttackLocation>) angleHits.clone();

        // Quick check for expiration of all entries.
        final long time = System.currentTimeMillis();
        AttackLocation lastLoc = tempAttack.isEmpty() ? null : tempAttack.getLast();
        if (lastLoc != null && time - lastLoc.time > maxTimeDiff) {
            tempAttack.clear();
            lastLoc = null;
        }

        // Add the new location.
        tempAttack.add(new AttackLocation(tempLocation, damagedEntity.getUniqueID(), System.currentTimeMillis(), lastLoc));

        // Calculate the sums of differences.
        double deltaMove = 0D;
        long deltaTime = 0L;
        float deltaYaw = 0f;
        int deltaSwitchTarget = 0;
        final Iterator<AttackLocation> it = angleHits.iterator();
        while (it.hasNext()) {
            final AttackLocation refLoc = it.next();
            if (time - refLoc.time > maxTimeDiff) {
                it.remove();
                continue;
            }
            deltaMove += refLoc.distSqLast;
            final double yawDiff = Math.abs(refLoc.yawDiffLast);
            deltaYaw += yawDiff;
            deltaTime += refLoc.timeDiff;
            if (refLoc.idDiffLast && yawDiff > 30.0) {
                // TODO: Configurable sensitivity ? Scale with yawDiff?
                deltaSwitchTarget += 1;
            }
        }

        // Check if there is enough data present.
        if (angleHits.size() < 2) {
            return false;
        }

        final double n = angleHits.size() - 1;

        // Let's calculate the average move.
        final double averageMove = deltaMove / n;

        // And the average time elapsed.
        final double averageTime = (double) deltaTime / n;

        // And the average yaw delta.
        final double averageYaw = (double) deltaYaw / n;

        // Average target switching.
        final double averageSwitching = (double) deltaSwitchTarget / n;

        // Declare the variable.
        double violation = 0.0;

        // If the average move is between 0 and 0.2 block(s), add it to the violation.
        if (averageMove >= 0.0 && averageMove < 0.2D) {
            violation += 20.0 * (0.2 - averageMove) / 0.2;
        }

        // If the average time elapsed is between 0 and 150 millisecond(s), add it to the violation.
        if (averageTime >= 0.0 && averageTime < 150.0) {
            violation += 30.0 * (150.0 - averageTime) / 150.0;
        }

        // If the average difference of yaw is superior to 50 degrees, add it to the violation.
        if (averageYaw > 50.0) {
            violation += 30.0 * averageYaw / 180.0;
        }

        if (averageSwitching > 0.0) {
            violation += 20.0 * averageSwitching;
        }

        return violation > 50;

    }

    public boolean check(final Location loc, final Entity damagedEntity) {
        boolean cancel = false;

        // Quick check for expiration of all entries.
        final long time = System.currentTimeMillis();
        AttackLocation lastLoc = angleHits.isEmpty() ? null : angleHits.getLast();
        if (lastLoc != null && time - lastLoc.time > maxTimeDiff) {
            angleHits.clear();
            lastLoc = null;
        }

        // Add the new location.
        angleHits.add(new AttackLocation(loc, damagedEntity.getUniqueID(), System.currentTimeMillis(), lastLoc));

        // Calculate the sums of differences.
        double deltaMove = 0D;
        long deltaTime = 0L;
        float deltaYaw = 0f;
        int deltaSwitchTarget = 0;
        final Iterator<AttackLocation> it = angleHits.iterator();
        while (it.hasNext()) {
            final AttackLocation refLoc = it.next();
            if (time - refLoc.time > maxTimeDiff) {
                it.remove();
                continue;
            }
            deltaMove += refLoc.distSqLast;
            final double yawDiff = Math.abs(refLoc.yawDiffLast);
            deltaYaw += yawDiff;
            deltaTime += refLoc.timeDiff;
            if (refLoc.idDiffLast && yawDiff > 30.0) {
                // TODO: Configurable sensitivity ? Scale with yawDiff?
                deltaSwitchTarget += 1;
            }
        }

        // Check if there is enough data present.
        if (angleHits.size() < 2) {
            return false;
        }

        final double n = angleHits.size() - 1;

        // Let's calculate the average move.
        final double averageMove = deltaMove / n;

        // And the average time elapsed.
        final double averageTime = (double) deltaTime / n;

        // And the average yaw delta.
        final double averageYaw = (double) deltaYaw / n;

        // Average target switching.
        final double averageSwitching = (double) deltaSwitchTarget / n;

        // Declare the variable.
        double violation = 0.0;

        // If the average move is between 0 and 0.2 block(s), add it to the violation.

        double moveFlag = 0, timeFlag = 0, yawFlag = 0, switchFlag = 0;

        if (averageMove >= 0.0 && averageMove < 0.2D) {
            violation += moveFlag = 20.0 * (0.2 - averageMove) / 0.2;
        }

        // If the average time elapsed is between 0 and 150 millisecond(s), add it to the violation.
        if (averageTime >= 0.0 && averageTime < 150.0) {
            violation += timeFlag = 30.0 * (150.0 - averageTime) / 150.0;
            //ChatUtil.printChat("Flagged Time " + violation);
        }

        // If the average difference of yaw is superior to 50 degrees, add it to the violation.
        if (averageYaw > 50.0) {
            violation += yawFlag = 30.0 * averageYaw / 180.0;
            //ChatUtil.printChat("Flagged Yaw " + violation);
        }

        if (averageSwitching > 0.0) {
            violation += switchFlag = 20.0 * averageSwitching;
            //ChatUtil.printChat("Flagged Switching " + violation);
        }

        // Is the violation is superior to the threshold defined in the configuration?
        if (violation > 50) {
            ChatUtil.printChat("\2474[\247cNCP\2474] \247fFIGHT_ANGLE VL: \2477" + MathUtils.roundToPlace(violation, 1) +
                    " \2478|\247f M: \2477" + MathUtils.roundToPlace(moveFlag, 1) +
                    " \2478|\247f T: \2477" + MathUtils.roundToPlace(timeFlag, 1) +
                    " \2478|\247f Y: \2477" + MathUtils.roundToPlace(yawFlag, 1) +
                    " \2478|\247f S: \2477" + MathUtils.roundToPlace(switchFlag, 1));
            // Has the server lagged?
            // TODO: 1.5 is a fantasy value.
            // If it hasn't, increment the violation level.
            angleVL += violation;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = angleVL > 50;
        } else {
            // Reward the player by lowering their violation level.
            angleVL *= 0.98D;
        }

        return cancel;
    }
}
