package exhibition.management.spotify;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.wrapper.spotify.model_objects.specification.Track;
import java.util.regex.Pattern;

public class SpotifyWebAPI
{
    private static final String BASE_URL = "https://api.spotify.com/v1";
    private static final int TIMEOUT = 10000;
    private static final Pattern JS_PATTERN = Pattern.compile("\\s+Spotify\\.Entity = (.*);");
    private final Cache<String, String> TRACK_IMAGE_LOOKUP = CacheBuilder.newBuilder().maximumSize(500L).build();
    private final Cache<String, SpotifyTrack> SPOTIFY_SEARCH_RESULTS = CacheBuilder.newBuilder().maximumSize(500L).build();

    public void resolveTrackImage(Track track)
    {

    }

    private boolean resolveTrackImage(Track track, String trackId, JsonElement element)
    {
        return false;
    }


}