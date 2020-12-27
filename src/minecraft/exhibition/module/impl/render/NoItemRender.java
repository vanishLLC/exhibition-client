/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

import java.util.Iterator;

public class NoItemRender extends Module {

    public NoItemRender(ModuleData data) {
        super(data);
    }

    public void onEvent(Event event) {
        for (Iterator<Entity> it = mc.theWorld.loadedEntityList.iterator(); it.hasNext(); ) {
            Entity ent = it.next();
            if (ent instanceof EntityItem) {
                mc.theWorld.removeEntity(ent);
            }
        }
    }

}
