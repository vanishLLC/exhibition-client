package exhibition.module.impl.other;

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventRenderGui;
import exhibition.util.render.Colors;
import exhibition.util.render.Depth;
import exhibition.util.render.ScrollingText;
import exhibition.management.spotify.SpotifyManager;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.RenderingUtil;
import exhibition.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Spotify extends Module {
    private static final ResourceLocation e = new ResourceLocation("textures/spotify.png");

    private static final ResourceLocation icons = new ResourceLocation("textures/icons.png");

    private ScrollingText scrollingTrack;
    private ScrollingText scrollingArtist;
    public static SpotifyManager spotifyManager;
    private final Timer timer = new Timer();

    private String X = "X";
    private String Y = "Y";

    private boolean isDragging;
    private double draggedX, draggedY, startedX, startedY;

    public Spotify(ModuleData data) {
        super(data);
        settings.put(X, new Setting<>(X, 1920 - 40, "X position for Spotify Player.", 1, 1, 2560));
        settings.put(Y, new Setting<>(Y, 1080 - 130, "Y position for Spotify Player.", 1, 1, 1440));
    }

    @Override
    public void onEnable() {
        if (spotifyManager == null)
            spotifyManager = new SpotifyManager();
    }

    @Override
    public void onDisable() {
        spotifyManager = null;
    }

    @Override
    public Priority getPriority() {
        return Priority.LAST;
    }

    public void render(float x, float y, ScaledResolution resolution) {
        if (spotifyManager == null)
            return;

        int var141 = resolution.getScaledWidth();
        int var151 = resolution.getScaledHeight();
        final int mouseX = Mouse.getX() * var141 / this.mc.displayWidth;
        final int mouseY = var151 - Mouse.getY() * var151 / this.mc.displayHeight - 1;

        if (!Mouse.isButtonDown(0)) {
            isDragging = false;
        }

        boolean hoveringDrag = mc.currentScreen instanceof GuiChat && mouseX >= x && mouseX <= x + getWidth() && mouseY >= y - 2 && mouseY <= y + 17;

        if (hoveringDrag && !isDragging && Mouse.isButtonDown(0)) {
            double twoDscale = resolution.getScaleFactor() / Math.pow(resolution.getScaleFactor(), 2.0D);
            isDragging = true;
            startedX = x / twoDscale;
            startedY = y / twoDscale;

            final int realMouseX = Mouse.getX();
            final int realMouseY = mc.displayHeight - Mouse.getY();
            draggedX = realMouseX;
            draggedY = realMouseY;
        }

        if (isDragging) {
            final int realMouseX = Mouse.getX();
            final int realMouseY = mc.displayHeight - Mouse.getY();

            int movedX = (int) (realMouseX - draggedX);
            int movedY = (int) (realMouseY - draggedY);

            settings.get(X).setValue((int) (startedX + movedX));
            settings.get(Y).setValue((int) (startedY + movedY));
        }

        CurrentlyPlayingContext status = (spotifyManager.isConnected()) ? spotifyManager.getCurrentlyPlaying() : null;
        Track track = (status != null) && (status.getItem() instanceof Track) ? (Track) status.getItem() : null;
        float trackRight = x + getWidth() - 4;
        int albumSize = getHeight() - 2;
        // 0xFF282828
        RenderingUtil.rectangle(x, y, x + getWidth(), y + getHeight() - 2, 0xFF121212);
        RenderingUtil.rectangle(x, y + getHeight() - 2, x + getWidth(), y + getHeight() - 2 + (track != null ? 16 : 0), 0xFF181818);
        RenderingUtil.rectangle(x, y + getHeight() - 2, x + getWidth(), y + getHeight() - 1.5, 0xFF282828);


//        if (track != null && track.getAlbum() != null && track.getAlbum().getImages() != null) {
//            Image image = track.getAlbum().getImages()[0];
//            if ((image != null) && (!image.getUrl().equals(base64Renderer.getURLString()))) {
//                base64Renderer.setBase64String(track.getImage(), "spotify/track_" + track.getTrackInformation().getId());
//            } else if ((image == null) && (base64Renderer.getURLString() != null)) {
//                base64Renderer.reset();
//            }
//        } else if (base64Renderer.getURLString() != null) {
//            base64Renderer.reset();
//        }

        RenderingUtil.rectangle(x, y, x + albumSize, y + albumSize, 0xFF000000);

        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(e);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.translate(x, y, 0);
        GlStateManager.pushMatrix();
        Gui.drawModalRectWithCustomSizedTexture(albumSize / 2 - 9, albumSize / 2 - 9, 0, 0, 18, 18, 18, 18);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();

        float infoLeft = x + albumSize + 4;
        float infoTop = y + 3;

        boolean mouseClicked = Mouse.getEventButton() == 0 && Mouse.getEventButtonState();

        if (track != null) {
            // Pause Button
            {

                double pauseX = x + getWidth() / 2F - 4,
                        pauseY = y + getHeight() + 2;

                boolean hovering = mouseX >= pauseX && mouseX <= pauseX + 8 && mouseY >= pauseY && mouseY <= pauseY + 8 && (mc.currentScreen instanceof GuiChat);

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderingUtil.glColor(-1);

                double scale = hovering && !mouseClicked ? 1.25 : 1.15;

                GlStateManager.translate(pauseX + 4, pauseY + 4, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.pushMatrix();
                Gui.drawModalRectWithCustomSizedTexture(-4, -4, status.getIs_playing() ? 8 : 16, 8, 8, 8, 48 / 2F, 32 / 2F);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                boolean clicked = hovering && mouseClicked && Display.isActive();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.pauseSong()).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }

            // Previous Button
            {
                double pauseX = x + getWidth() / 2F - 4 - 16,
                        pauseY = y + getHeight() + 2;

                boolean hovering = mouseX >= pauseX && mouseX <= pauseX + 8 && mouseY >= pauseY && mouseY <= pauseY + 8 && (mc.currentScreen instanceof GuiChat);

                float a = 1 / 5;

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

                RenderingUtil.glColor(hovering && !mouseClicked ? Colors.getColor(255) : Colors.getColor(179));
                GlStateManager.translate(pauseX, pauseY, 0);
                GlStateManager.pushMatrix();
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 8, 8, 8, 48 / 2F, 32 / 2F);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                boolean clicked = hovering && mouseClicked && Display.isActive();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.previousTrack()).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }

            // Next Button
            {
                double pauseX = x + getWidth() / 2F - 4 + 16,
                        pauseY = y + getHeight() + 2;

                boolean hovering = mouseX >= pauseX && mouseX <= pauseX + 8 && mouseY >= pauseY && mouseY <= pauseY + 8 && (mc.currentScreen instanceof GuiChat);

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

                RenderingUtil.glColor(hovering && !mouseClicked ? Colors.getColor(255) : Colors.getColor(179));
                GlStateManager.translate(pauseX, pauseY, 0);
                GlStateManager.rotate(180, 0, 0, 1);
                GlStateManager.pushMatrix();
                Gui.drawModalRectWithCustomSizedTexture(-8, -8, 0, 8, 8, 8, 48 / 2F, 32 / 2F);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                boolean clicked = hovering && mouseClicked && Display.isActive();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.nextTrack()).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }

            // Shuffle Button
            {
                double pauseX = x + getWidth() / 2F - 4 - 32,
                        pauseY = y + getHeight() + 2;

                boolean hovering = mouseX >= pauseX && mouseX <= pauseX + 8 && mouseY >= pauseY && mouseY <= pauseY + 8 && (mc.currentScreen instanceof GuiChat);

                boolean enabled = status.getShuffle_state();

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

                GlStateManager.translate(pauseX, pauseY, 0);
                GlStateManager.pushMatrix();
                RenderingUtil.glColor(enabled ? 0xff1db954 : Colors.getColor(179));
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 16, 0, 8, 8, 48 / 2F, 32 / 2F);
                RenderingUtil.glColor(-1);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                if (enabled) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(pauseX + 4, pauseY + 9, 0);
                    RenderingUtil.rectangle(-1, -1, 1, 1, Colors.getColorOpacity(0xff1db954, 125));
                    RenderingUtil.rectangle(-0.5, -1, 0.5, 1, Colors.getColorOpacity(0xff1db954, 200));
                    RenderingUtil.rectangle(-1, -0.5, 1, 0.5, Colors.getColorOpacity(0xff1db954, 200));
                    GlStateManager.popMatrix();
                }

                boolean clicked = hovering && mouseClicked && Display.isActive();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.setShuffleState(!status.getShuffle_state())).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }

            // Repeat Button
            {
                double pauseX = x + getWidth() / 2F - 4 - 48,
                        pauseY = y + getHeight() + 2;

                boolean hovering = mouseX >= pauseX && mouseX <= pauseX + 8 && mouseY >= pauseY && mouseY <= pauseY + 8 && (mc.currentScreen instanceof GuiChat);

                boolean enabled = !status.getRepeat_state().equals("off");

                GlStateManager.pushMatrix();
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GlStateManager.translate(pauseX, pauseY, 0);
                GlStateManager.pushMatrix();
                int offset = status.getRepeat_state().equals("track") ? 0 : 8;
                RenderingUtil.glColor(enabled ? 0xff1db954 : Colors.getColor(179));
                Gui.drawModalRectWithCustomSizedTexture(0, 0, offset, 0, 8, 8, 48 / 2F, 32 / 2F);
                RenderingUtil.glColor(-1);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
                GlStateManager.popMatrix();

                if (enabled) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(pauseX + 4, pauseY + 9, 0);
                    RenderingUtil.rectangle(-1, -1, 1, 1, Colors.getColorOpacity(0xff1db954, 125));
                    RenderingUtil.rectangle(-0.5, -1, 0.5, 1, Colors.getColorOpacity(0xff1db954, 200));
                    RenderingUtil.rectangle(-1, -0.5, 1, 0.5, Colors.getColorOpacity(0xff1db954, 200));
                    GlStateManager.popMatrix();
                }

                boolean clicked = hovering && mouseClicked && Display.isActive();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.toggleRepeat()).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }
        }

        int maxInfoWidth = (int) (trackRight - infoLeft);
        String trackName = (track != null) && track.getName() != null ? track.getName() : spotifyManager.isConnected() ? "No song." : "Spotify Connection Error?";

        if ((!trackName.equals("")) && ((this.scrollingTrack == null) || (!trackName.equals(this.scrollingTrack.getText())))) {
            this.scrollingTrack = new ScrollingText(trackName, maxInfoWidth, 8, 0xFF121212, -1);
        } else if ((this.scrollingTrack != null) && (trackName.equals(""))) {
            this.scrollingTrack = null;
        }

        StringBuilder artists = new StringBuilder();

        if ((track != null) && (track.getAlbum() != null && track.getAlbum().getArtists() != null)) {
            Iterator<ArtistSimplified> iterator = Arrays.asList(track.getArtists()).iterator();

            while (iterator.hasNext()) {
                ArtistSimplified artist = iterator.next();
                artists.append(artist.getName()).append(iterator.hasNext() ? ", " : "");
            }
        }

        String artistName = artists.toString();


        if ((!artistName.equals("")) && ((this.scrollingArtist == null) || (!artistName.equals(this.scrollingArtist.getText())))) {
            this.scrollingArtist = new ScrollingText(artistName, maxInfoWidth, 5, 0xFF121212, -1);
        } else if ((this.scrollingArtist != null) && (artistName.equals(""))) {
            this.scrollingArtist = null;
        }
        if (this.scrollingTrack != null) {
            this.scrollingTrack.setChild(this.scrollingArtist);
            this.scrollingTrack.setScale(resolution.getScaleFactor());
            this.scrollingTrack.render(infoLeft, infoTop);
        }
        if (this.scrollingArtist != null) {
            this.scrollingArtist.setParent(this.scrollingTrack);
            this.scrollingArtist.setScale(resolution.getScaleFactor());
            this.scrollingArtist.render(infoLeft, infoTop + 9);
        }
        float positionLineTop = y + getHeight() - 8;
        float positionLineBottom = positionLineTop + 2.5F;
        float positionLineLeft = infoLeft + 13;
        float positionLineRight = trackRight - 13;
        float positionWidth = positionLineRight - positionLineLeft;

        Depth.pre();
        {
            Depth.mask();
            RenderingUtil.rectangle(positionLineLeft + 0.5, positionLineTop, positionLineRight - 0.5, positionLineBottom, -1);
            RenderingUtil.rectangle(positionLineLeft, positionLineTop + 0.5, positionLineRight, positionLineBottom - 0.5, -1);
            Depth.render();
            RenderingUtil.rectangle(positionLineLeft, positionLineTop, positionLineRight, positionLineBottom, 0xFF535353);

            boolean hovering = mouseX >= positionLineLeft - 1 && mouseX <= positionLineRight && mouseY >= positionLineTop && mouseY <= positionLineBottom && (mc.currentScreen instanceof GuiChat);
            if (track != null) {
                boolean clicked = hovering && mouseClicked && Display.isActive();
                if (hovering) {
                    double position = (mouseX - positionLineLeft) / (positionWidth);

                    RenderingUtil.rectangle(positionLineLeft, positionLineTop, positionLineLeft + position * positionWidth, positionLineBottom, Colors.getColor(255, 35));
                    Depth.render(GL11.GL_LEQUAL);

                    long bruh = Math.round(track.getDurationMs() * position);
                    String pp = "Jump to " + convertToClock(bruh);
                    float width = mc.fontRendererObj.getStringWidth(pp);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(mouseX - width / 4, mouseY - 8, 1000);
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    mc.fontRendererObj.drawStringWithShadow(pp, 0, 0, clicked ? -1 : Colors.getColor(240));
                    GlStateManager.popMatrix();

                    if (clicked && timer.delay(300)) {
                        timer.reset();
                        new Thread(() -> spotifyManager.seekTo(bruh)).start();
                    } else if (clicked && mouseClicked) {
                        timer.reset();
                    }
                }
            }
            Depth.render();

            long adjustedProgress = status != null ? (System.currentTimeMillis() - status.getTimestamp()) - (long) (spotifyManager.getLastTimeStamp() * 1.5D) : 0;
            double trackPosition = status != null ? status.getIs_playing() ? spotifyManager.getLastProgressMS() + adjustedProgress : status.getProgress_ms() : 0.0D;
            long trackLength = track != null ? track.getDurationMs() : 1000L;
            double trackPercentage = Math.max(0, Math.min(1, trackPosition / trackLength));
            RenderingUtil.rectangle(positionLineLeft, positionLineTop, positionLineLeft + trackPercentage * positionWidth - 0.5, positionLineBottom, hovering ? 0xff1db954 : 0xFFb3b3b3);
            RenderingUtil.rectangle(trackPercentage * positionWidth - 0.5, positionLineTop + 0.5, positionLineLeft + trackPercentage * positionWidth, positionLineBottom - 0.5, hovering ? 0xff1db954 : 0xFFb3b3b3);

            if(hovering) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(positionLineLeft + trackPercentage * positionWidth, positionLineBottom - 1.25,0);
                RenderingUtil.drawFullCircle(0,0, 2, -1);
                GlStateManager.popMatrix();
            }

            Depth.post();
            drawScaledString(convertToClock((long) trackPosition), infoLeft - 2.5F, positionLineTop - 0.5F, 0xFFb3b3b3, 0.5F);
            drawScaledString(convertToClock(trackLength), positionLineRight + 3, positionLineTop - 0.5F, 0xFFb3b3b3, 0.5F);
        }

        if (track != null) {
            double volumeLeft = x + getWidth() - 1.5 - 70 / 2F;
            double volumeRight = x + getWidth() - 1.5F;
            double volumeTop = positionLineBottom + 12.5F;
            double volumeBottom = volumeTop + 2.5F;

            boolean hovering = mouseX >= volumeLeft && mouseX <= volumeRight && mouseY >= volumeTop && mouseY <= volumeBottom && (mc.currentScreen instanceof GuiChat);

            Depth.pre();
            Depth.mask();
            RenderingUtil.rectangle(volumeLeft + 0.5, volumeTop, volumeRight - 0.5, volumeBottom, -1);
            RenderingUtil.rectangle(volumeLeft, volumeTop + 0.5, volumeRight, volumeBottom - 0.5, -1);
            Depth.render();
            RenderingUtil.rectangle(volumeLeft, volumeTop, volumeRight, volumeBottom, 0xFF535353);
            double volumePercent = (volumeRight - volumeLeft) * (spotifyManager.getVolume() / 100F);
            RenderingUtil.rectangle(volumeLeft, volumeTop, volumeLeft + volumePercent - 0.5, volumeBottom, hovering ? 0xff1db954 : 0xFFb3b3b3);
            RenderingUtil.rectangle(volumeLeft + volumePercent - 0.5, volumeTop + 0.5, volumeLeft + volumePercent, volumeBottom - 0.5, hovering ? 0xff1db954 : 0xFFb3b3b3);
            Depth.post();

            if(hovering) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(volumeLeft + volumePercent, volumeBottom - 1.25,0);
                RenderingUtil.drawFullCircle(0,0, 2, -1);
                GlStateManager.popMatrix();
            }

            GlStateManager.pushMatrix();
            float stringWidth = mc.fontRendererObj.getStringWidth(spotifyManager.getVolume() + "%");
            GlStateManager.translate(x + getWidth() - 1.5 - 70 / 4F - stringWidth / 4F, volumeBottom - 9, 0);
            GlStateManager.scale(0.5, 0.5, 0.5);
            mc.fontRendererObj.drawStringWithShadow(spotifyManager.getVolume() + "%", 0, 0, 0xFFb3b3b3);
            GlStateManager.scale(2, 2, 2);
            GlStateManager.popMatrix();

            boolean clicked = hovering && mouseClicked && Display.isActive();
            if (hovering) {
                double position = (mouseX - volumeLeft) / (volumeRight - volumeLeft);
                int newVolume = Math.round((float) (100 * position));
                String pp = "Set volume " + newVolume + "%";
                float width = mc.fontRendererObj.getStringWidth(pp);
                GlStateManager.pushMatrix();
                GlStateManager.translate(mouseX - width / 2 * 0.5, mouseY + 12, 0);
                GlStateManager.scale(0.51, 0.51, 0.51);
                mc.fontRendererObj.drawStringWithShadow(pp, 0, 0, clicked ? -1 : Colors.getColor(240));
                GlStateManager.popMatrix();

                if (clicked && timer.delay(300)) {
                    timer.reset();
                    new Thread(() -> spotifyManager.setVolume(newVolume)).start();
                } else if (clicked && mouseClicked) {
                    timer.reset();
                }
            }
        }

    }

    public String convertToClock(long millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void drawScaledString(String string, float x, float y, int color, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, scale);
        Minecraft.getMinecraft().fontRendererObj.drawString(string, 0, 0, color);
        GlStateManager.popMatrix();
    }

    public int getWidth() {
        return 120;
    }

    public int getHeight() {
        return 28;
    }

    @RegisterEvent(events = EventRenderGui.class)
    public void onEvent(Event event) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDscale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D);

        float xOffset = ((Number) settings.get(X).getValue()).floatValue() * (float) twoDscale;
        float yOffset = ((Number) settings.get(Y).getValue()).floatValue() * (float) twoDscale;
        EventRenderGui er = event.cast();
        GlStateManager.pushMatrix();
        try {
            render(xOffset, yOffset, er.getResolution());
        } catch (Exception e) {

        }
        GlStateManager.popMatrix();
    }

}
