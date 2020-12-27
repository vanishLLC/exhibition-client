package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMotionUpdate;
import exhibition.event.impl.EventRender3D;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.util.RenderingUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class MotionPrediction extends Module {

    private HashMap<EntityPlayer, EntityDelta> deltaHashMap = new HashMap<>();

    public MotionPrediction(ModuleData data) {
        super(data);
    }

    @RegisterEvent(events = {EventRender3D.class, EventMotionUpdate.class})
    public void onEvent(Event event) {
        if (event instanceof EventMotionUpdate) {
            EventMotionUpdate em = event.cast();
            if (em.isPost())
                return;

            for (Entity e : mc.theWorld.getLoadedEntityList()) {
                if (e instanceof EntityPlayer) {
                    EntityPlayer entity = (EntityPlayer) e;
                    double xDelta = e.posX - e.lastTickPosX;
                    double zDelta = e.posZ - e.lastTickPosZ;
                    if (deltaHashMap.containsKey(entity)) {
                        deltaHashMap.get(entity).logDeltas(xDelta, zDelta, mc.thePlayer.ticksExisted);
                    } else {
                        deltaHashMap.put(entity, new EntityDelta(xDelta, zDelta));
                    }
                }
            }

        } else {
            GL11.glPushMatrix();
            RenderingUtil.pre3D();
            GL11.glLineWidth(1.5F);


            RenderingUtil.post3D();
            GL11.glPopMatrix();
        }
    }

    private class EntityDelta {
        private final ArrayBlockingQueue<double[]> deltas = new ArrayBlockingQueue<>(5);
        private int lastUpdatedTick;

        private EntityDelta(double initialDeltaX, double initialDeltaY) {
            deltas.add(new double[]{initialDeltaX, initialDeltaY});
        }

        private EntityDelta logDeltas(double deltaX, double deltaY, int currentTick) {
            if (currentTick - lastUpdatedTick > 5) {
                deltas.clear();
            }
            if (deltas.remainingCapacity() == 0) {
                deltas.remove();
            }

            lastUpdatedTick = currentTick;
            deltas.add(new double[]{deltaX, deltaY});
            return this;
        }

        public double[] getWeightedDeltas() {
            int denominator = deltas.size();
            double deltaX = 0, deltaY = 0;
            for (double[] deltas : deltas) {
                deltaX += deltas[0];
                deltaY += deltas[1];
            }
            return new double[]{deltaX / denominator, deltaY / denominator};
        }
    }

}
