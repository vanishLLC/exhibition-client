package exhibition.module.impl.other;

import exhibition.event.Event;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;

public class PlayPause extends Module {

    public PlayPause(ModuleData data) {
        super(data);
    }

    public void onEvent(Event event) {

    }

    public void onEnable() {
        if(Spotify.spotifyManager != null && Spotify.spotifyManager.isConnected()) {
            Spotify.spotifyManager.pauseSong();
        }
        toggle();
    }

}
