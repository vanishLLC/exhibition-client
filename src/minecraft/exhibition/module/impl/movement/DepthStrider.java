/**
 * Time: 12:38:19 AM
 * Date: Dec 30, 2016
 * Creator: cool1
 */
package exhibition.module.impl.movement;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventMove;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;

/**
 * @author cool1
 *
 */
public class DepthStrider extends Module {

	/**
	 * @param data
	 */
	public DepthStrider(ModuleData data) {
		super(data);
	}

	int waitTicks;
	
	/* (non-Javadoc)
	 * @see EventListener#onEvent(Event)
	 */
	@Override
	@RegisterEvent(events = {EventMove.class})
	public void onEvent(Event event) {
		if(event instanceof EventMove){
			EventMove em = (EventMove)event;
			if(mc.thePlayer.isInWater()){
				waitTicks++;
				if(waitTicks == 4){
			        double forward = mc.thePlayer.movementInput.moveForward;
					double strafe = mc.thePlayer.movementInput.moveStrafe;
					float yaw = mc.thePlayer.rotationYaw;
					if ((forward == 0.0D) && (strafe == 0.0D)) {
						em.setX(0.0D);
						em.setZ(0.0D);
					} else {
						if (forward != 0.0D) {
							if (strafe > 0.0D) {
								strafe = 1;
								yaw += (forward > 0.0D ? -45 : 45);
							} else if (strafe < 0.0D) {
								yaw += (forward > 0.0D ? 45 : -45);
							}
							strafe = 0.0D;
							if (forward > 0.0D) {
								forward = 1;
							} else if (forward < 0.0D) {
								forward = -1;
							}
						}
						em.setX(forward * 0.1000000059604645D * Math.cos(Math.toRadians(yaw + 90.0F))+ strafe * 0.1000000059604645D * Math.sin(Math.toRadians(yaw + 90.0F)));
						em.setZ(forward * 0.1000000059604645D * Math.sin(Math.toRadians(yaw + 90.0F))- strafe * 0.1000000059604645D * Math.cos(Math.toRadians(yaw + 90.0F)));
					}
				}
				if(waitTicks >= 5){
			        double forward = mc.thePlayer.movementInput.moveForward;
					double strafe = mc.thePlayer.movementInput.moveStrafe;
					float yaw = mc.thePlayer.rotationYaw;
					if ((forward == 0.0D) && (strafe == 0.0D)) {
						em.setX(0.0D);
						em.setZ(0.0D);
					} else {
						if (forward != 0.0D) {
							if (strafe > 0.0D) {
								strafe = 1;
								yaw += (forward > 0.0D ? -45 : 45);
							} else if (strafe < 0.0D) {
								yaw += (forward > 0.0D ? 45 : -45);
							}
							strafe = 0.0D;
							if (forward > 0.0D) {
								forward = 1;
							} else if (forward < 0.0D) {
								forward = -1;
							}
						}
						em.setX(forward * 0.21000001192092896D * Math.cos(Math.toRadians(yaw + 90.0F))+ strafe * 0.21000001192092896D * Math.sin(Math.toRadians(yaw + 90.0F)));
						em.setZ(forward * 0.21000001192092896D * Math.sin(Math.toRadians(yaw + 90.0F))- strafe * 0.21000001192092896D * Math.cos(Math.toRadians(yaw + 90.0F)));
					}
					waitTicks = 0;
				}
			}
		}
	}

}
