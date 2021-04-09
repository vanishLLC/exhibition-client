package exhibition.util.render.font;

import exhibition.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class FontCache {

    public static FontCache instance = new FontCache();

    private HashMap<String, StringCache> stringCache = new HashMap<>();
    private HashMap<String, StringCache> stringCacheShadow = new HashMap<>();
    private StringCache curString = null;

    private TextureManager textureManager;

    private Timer throttleTimer = new Timer();
    private Timer freeTimer = new Timer();

    public FontCache () {
        textureManager = Minecraft.getMinecraft().getTextureManager();
    }

    private void checkFree(HashMap<String, StringCache> cache) {
        if (freeTimer.delay(5000)) {
            ArrayList<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, StringCache> set : cache.entrySet()) {
                if (set.getValue().lastUsed.delay(10_000)) {
                    toRemove.add(set.getKey());
                }
            }
            for (String s : toRemove) {
                cache.remove(s);
                //Vape.print("Removed " + s);
            }
        }
    }

    public boolean render(FontRenderer fr, String text, float x, float y, int color, boolean dropShadow) {
        curString = null;

//        if (true)
//            return false;

        HashMap<String, StringCache> cache = dropShadow ? stringCacheShadow : stringCache;
        String key = text + color;
        if (!cache.containsKey(key)) {
            checkFree(cache);
            if (throttleTimer.delay(100)) {
                //Vape.print("adding " + text);
                StringCache str = new StringCache(text, color, x, y);
                cache.put(key, str);
                curString = str;
                throttleTimer.reset();
            }
            return false;
        }

        StringCache str = cache.get(key);
//        if (str.hasUnicode)
//            return false;
        try {
            textureManager.bindTexture(fr.locationFontTexture);
        } catch (Exception e) {
        }
        str.draw(x, y);
        return true;

    }

    public void renderChar(FontRenderer fr, int character, boolean italic) {
        if (curString != null) {
            curString.addCharacter(character, italic, fr.boldStyle, fr.posX, fr.posY);
        }
    }

    static class StringCache {

        private String string;
        private float lastPosX = -1, lastPosY, originalX, originalY, startOffsetX, startOffsetY;
        private int curIndex, stringIndex;
        private int color;
        private boolean multiColored = false;
        private float lastR = -1, lastG = -1, lastB = -1, lastA = -1;
        private boolean hasUnicode, firstBold;
        private Timer lastUsed = new Timer();

        private FloatBuffer vertexBuffer;
        private FloatBuffer texBuffer;
        private FloatBuffer colorBuffer;


        public StringCache(String s, int color, float posX, float posY) {
            this.string = s;
            this.color = color;

            this.originalX = posX;
            this.originalY = posY;

            for (char c : string.toCharArray()) {
                if (c > 128 && c != 167)
                    hasUnicode = true;
            }

            vertexBuffer = BufferUtils.createFloatBuffer(8 * 2 * s.length());
            texBuffer = BufferUtils.createFloatBuffer(8 * 2 * s.length());
            colorBuffer = BufferUtils.createFloatBuffer(8 * 4 * s.length());
        }

        void addAt(int index, float in[], FloatBuffer buffer) {
            for (int i = 0; i < in.length; i++) {
                buffer.put(index + i, in[i]);
            }
        }

        void checkString(boolean italic, boolean bold, float posX, float posY) {
            stringIndex++;

            if (bold) {
                firstBold = !firstBold;
                if (firstBold) {
                    stringIndex--;
                }
            }

            char nextChar = stringIndex + 1 < string.length() ? string.charAt(stringIndex) : 0;
            if (nextChar == 167) {
                stringIndex++;
                checkString(italic, bold, posX, posY);
                return;
            }

            if (nextChar == 32) {
                curIndex += 1;
                addCharacter(32, italic, false,posX + 4, posY);
            } else {
                curIndex += 1;
            }
        }

        void getColor(int character) {
            FloatBuffer color = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);

            if (lastR != -1) {
                if (lastR != color.get(0) || lastG != color.get(1) || lastB != color.get(2) || lastA != color.get(3)) {
                    multiColored = true;
                }
            }

            if (character == 32)
                color.put(3, 0);

            float colorVertices[] = {
                    0,0,0,0,
                    0,0,0,0,
                    color.get(0), color.get(1), color.get(2), color.get(3),
                    color.get(0), color.get(1), color.get(2), color.get(3)
            };

            lastR = color.get(0);
            lastG = color.get(1);
            lastB = color.get(2);
            lastA = color.get(3);

            addAt(curIndex * 4 * 4, colorVertices, colorBuffer);
        }

        public void addCharacter(int character, boolean italic, boolean bold, float posX, float posY) {

            if (this.lastPosX == -1) {
                this.lastPosX = posX;
                this.lastPosY = posY;
                startOffsetX = posX - originalX;
                int index = 0;
                if (string.toCharArray()[0] == ' ' || string.toCharArray()[0] == 167) {
                    for(; index < string.toCharArray().length; index++) {
                        char c = string.toCharArray()[index];
                        if (c != ' ' && c != 167) {
                            break;
                        }
                        if (c == 167)
                            index++;
                    }
                }
                stringIndex = index;
            }

            float xDif = posX - this.lastPosX;
            float yDif = posY - this.lastPosY;

            int i = character % 16 * 8;
            int j = character / 16 * 8;
            int k = italic ? 1 : 0;
            float f1 = 7.99F;
            float vertices[] = {
                    xDif + k, yDif,
                    xDif - k, yDif + 7.99F,
                    xDif + f1 - 1.0F + (float) k, yDif,
                    xDif + f1 - 1.0F - (float) k, yDif + 7.99F
            };
            addAt(curIndex * 8, vertices, vertexBuffer);
            float texVertices[] = {
                    (float) i / 128.0F, (float) j / 128.0F,
                    (float) i / 128.0F, ((float) j + 7.99F) / 128.0F,
                    ((float) i + f1 - 1.0F) / 128.0F, (float) j / 128.0F,
                    ((float) i + f1 - 1.0F) / 128.0F, ((float) j + 7.99F) / 128.0F
            };

            if (character == 32)
                Arrays.fill(texVertices, 0);

            addAt(curIndex * 8, texVertices, texBuffer);

            getColor(character);

            checkString(italic, bold, posX, posY);
        }

        public void draw(float x, float y) {
            lastUsed.reset();

            if (!glIsEnabled(GL_VERTEX_ARRAY)) {
                glEnableClientState(GL_VERTEX_ARRAY);
            }

            if (!glIsEnabled(GL_TEXTURE_COORD_ARRAY)) {
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            }

            glEnableClientState(GL_COLOR_ARRAY);

            glVertexPointer(2, 0, vertexBuffer);
            glTexCoordPointer(2, 0, texBuffer);


            glColorPointer(4, 0, colorBuffer);

            glTranslatef(x + startOffsetX, y, 0);
            glDrawArrays(GL_TRIANGLE_STRIP, 0, curIndex * 4);
            glTranslatef(-(x + startOffsetX), -y, 0);

            glDisableClientState(GL_COLOR_ARRAY);

            //glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            //glDisableClientState(GL_VERTEX_ARRAY);
        }


    }

}
