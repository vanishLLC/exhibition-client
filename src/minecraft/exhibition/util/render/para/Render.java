package exhibition.util.render.para;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by DoubleParallax on 1/1/2017.
 */
public class Render {

    private static final List<Integer> STATES = Lists.newArrayList();

    protected static final List<Map<Integer, Boolean>> GL_STATES = Lists.newArrayList();
    protected static final List<Integer[]> BLEND_FUNCS = Lists.newArrayList();

    protected static final List<Double> ALPHAS = Lists.newArrayList();
    protected static double alphaMult = 1;

    static {
        STATES.add(GL_ALPHA_TEST);
        STATES.add(GL_BLEND);
        STATES.add(GL_COLOR_LOGIC_OP);
        STATES.add(GL_COLOR_MATERIAL);
        STATES.add(GL_CULL_FACE);
        STATES.add(GL_DEPTH_TEST);
        STATES.add(GL_FOG);
        STATES.add(GL_LIGHTING);
        STATES.add(GL_NORMALIZE);
        STATES.add(GL_POLYGON_OFFSET_FILL);
        STATES.add(32826);
        STATES.add(GL_TEXTURE_2D);
        STATES.add(GL_LINE_SMOOTH);
        STATES.add(GL_POINT_SMOOTH);
    }

    public static void pushAlpha(double alpha) {
        ALPHAS.add(0, alpha);
        alphaMult = alpha;
    }

    public static void popAlpha() {
        ALPHAS.remove(0);
        alphaMult = ALPHAS.isEmpty() ? 1 : ALPHAS.get(0);
    }

    public static void pre() {
        Map<Integer, Boolean> glStateList = Maps.newHashMap();
        for (int i : STATES) {
            glStateList.put(i, glGetBoolean(i));
        }
        GL_STATES.add(0, glStateList);

        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POINT_SMOOTH);

        GlStateManager.alphaFunc(GL_GREATER, 0.001f);

        //BLEND_FUNCS.add(0, new Integer[]{GlStateManager.glGetInteger(GL_BLEND_SRC), GlStateManager.glGetInteger(GL_BLEND_DST)});
    }

    public static void post() {

        GlStateManager.alphaFunc(GL_GREATER, 0.1f);

        Map<Integer, Boolean> stateList = GL_STATES.get(0);
        GL_STATES.remove(0);

        if (stateList.get(GL_ALPHA_TEST)) GlStateManager.enableAlpha();
        else GlStateManager.disableAlpha();
        if (stateList.get(GL_BLEND)) GlStateManager.enableBlend();
        else GlStateManager.disableBlend();
        if (stateList.get(GL_COLOR_LOGIC_OP)) GlStateManager.enableColorLogic();
        else GlStateManager.disableColorLogic();
        if (stateList.get(GL_COLOR_MATERIAL)) GlStateManager.enableColorMaterial();
        else GlStateManager.disableColorMaterial();
        if (stateList.get(GL_CULL_FACE)) GlStateManager.enableCull();
        else GlStateManager.disableCull();
        if (stateList.get(GL_DEPTH_TEST)) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();
        if (stateList.get(GL_FOG)) GlStateManager.enableFog();
        else GlStateManager.disableFog();
        if (stateList.get(GL_LIGHTING)) GlStateManager.enableLighting();
        else GlStateManager.disableLighting();
        if (stateList.get(GL_NORMALIZE)) GlStateManager.enableNormalize();
        else GlStateManager.disableNormalize();
        if (stateList.get(GL_POLYGON_OFFSET_FILL)) GlStateManager.enablePolygonOffset();
        else GlStateManager.disablePolygonOffset();
        if (stateList.get(32826)) GlStateManager.enableRescaleNormal();
        else GlStateManager.disableRescaleNormal();
        if (stateList.get(GL_TEXTURE_2D)) GlStateManager.enableTexture2D();
        else GlStateManager.disableTexture2D();
        if (stateList.get(GL_LINE_SMOOTH)) glEnable(GL_LINE_SMOOTH);
        else glDisable(GL_LINE_SMOOTH);
        if (stateList.get(GL_POINT_SMOOTH)) glEnable(GL_POINT_SMOOTH);
        else glDisable(GL_POINT_SMOOTH);
    }

}
