package exhibition.module.impl.render;

import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRender3D;
import exhibition.event.impl.EventRenderEntity;
import exhibition.management.ColorManager;
import exhibition.management.ColorObject;
import exhibition.management.animate.Opacity;
import exhibition.management.friend.FriendManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.Options;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.TeamUtils;
import exhibition.util.render.Stencil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Outline extends Module {

    private Options colors = new Options("Color", "Friend", new String[]{"Friend", "TeamColor", "Team", "Custom", "Rainbow"});
    private Setting WIDTH = new Setting("WIDTH", 2, "The width of the outline.", 0.5, 0.5, 5);
    private boolean draw = false;
    private int draws = 0;

    private Opacity hue = new Opacity(0);

    public Outline(ModuleData data) {
        super(data);
        settings.put("COLOR", new Setting<>("COLOR", colors, "Sets the color for chams. (Bugged/Broken)"));
        settings.put("WIDTH", WIDTH);
    }

    @Override
    public Priority getPriority() {
        return Priority.MEDIUM;
    }

    @RegisterEvent(events = {EventRender3D.class, EventRenderEntity.class})
    public void onEvent(Event event) {

    }
}