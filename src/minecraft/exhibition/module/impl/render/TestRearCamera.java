/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.gui.rearview.RearviewCamera;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;

public class TestRearCamera extends Module {

    private RearviewCamera rearviewCamera;

    public TestRearCamera(ModuleData data) {
        super(data);
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @Override
    public void onEnable() {
        if(mc.thePlayer == null || mc.theWorld == null) {
            this.toggle();
            return;
        }
        rearviewCamera = new RearviewCamera();
    }

    @RegisterEvent(events = EventRenderGui.class)
    public void onEvent(Event event) {
        if(rearviewCamera == null)
            return;

        if(rearviewCamera.isFrameBufferUpdated()){
            rearviewCamera.draw(100,100, 300, 200);
        }else{
            mc.fontRendererObj.drawStringWithShadow("Frame buffer isn't updated", 100,100,-1);
        }
    }
}
