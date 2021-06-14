/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.management.spotify;

import com.google.gson.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import exhibition.Client;
import exhibition.management.notifications.usernotification.Notifications;
import exhibition.module.impl.other.Spotify;
import exhibition.util.Timer;
import io.github.alerithe.spotify.LocalSpotifyServer;
import org.apache.hc.core5.http.ParseException;

public class SpotifyManager {
    public static final Gson gson = new Gson();

    static URI redirectURI = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
    static String clientID = "473322a7225b47418fdeecad4d45eb8b";
    static String clientSecret = "cdc7e1bab70140c98104dc7ffffd3616";

    private boolean connected;

    private static SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientID)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirectURI)
            .build();

    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .scope("user-read-playback-state," +
                    "user-read-currently-playing," +
                    "user-read-recently-played," +
                    "user-read-playback-position," +
                    "user-modify-playback-state")
            .build();


    private Timer timer = new Timer();

    public SpotifyManager() {
        System.out.println("Initializing Spotify Manager!");
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientID)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectURI)
                .build();
        new Thread("Spotify Auth Thread") {
            @Override
            public void run() {
                Notifications.getManager().post("Spotify Enabled", "Opening Spotify Auth page in {s}s!", 5_000, Notifications.Type.SPOTIFY);
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {

                }
                authorizationCodeUri_Sync();
                while (!authorizationCode_Sync() && Spotify.spotifyManager != null && Client.getModuleManager().isEnabled(Spotify.class)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Spotify.spotifyManager == null)
                        break;
                }
                setConnected(true);
            }
        }.start();
    }

    boolean threadStarted = false;

    public void onEvent() {
        if (isConnected() && !threadStarted && Spotify.spotifyManager != null) {
            try {
                threadStarted = true;
                new Thread(() -> {
                    while (isConnected() && Client.getModuleManager().isEnabled(Spotify.class)) {
                        synchronized (SpotifyManager.class) {
                            currentTrackInfo();
                        }
                        try {
                            Thread.sleep(450);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    setConnected(false);
                    threadStarted = false;
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void authorizationCodeUri_Sync() {
        final URI uri = authorizationCodeUriRequest.execute();
        try {
            LocalSpotifyServer.init();
            LocalSpotifyServer.listen();
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Spotify Error opening auth url.");
        }
    }

    public boolean authorizationCode_Sync() {
        if (LocalSpotifyServer.AUTHENTICATION_CODE == null || LocalSpotifyServer.AUTHENTICATION_CODE.equals(""))
            return false;
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCode(LocalSpotifyServer.AUTHENTICATION_CODE).build().execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            Notifications.getManager().post("Spotify Linked", "Token expires in " + authorizationCodeCredentials.getExpiresIn() + "s!", Notifications.Type.SPOTIFY);

            expires = System.currentTimeMillis() + authorizationCodeCredentials.getExpiresIn() * 1000;
            needsReauth = false;
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Spotify Error: " + e.getMessage());
        }
        return false;
    }

    private CurrentlyPlayingContext currentlyPlayingContext = null;

    private GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest = null;

    private boolean isPlaying = false;

    private static long expires = 0;

    private int volume = 100;

    private String repeat = "off";

    private boolean needsReauth;

    private boolean isChanging;

    private void currentTrackInfo() {
        if (needsReauth) {
            try {
                Thread.sleep(10_000);
                setConnected(false);
                currentlyPlayingContext = null;
                getInformationAboutUsersCurrentPlaybackRequest = null;
                if (LocalSpotifyServer.AUTHENTICATION_CODE != null) {
                    LocalSpotifyServer.AUTHENTICATION_CODE = null;
                    authorizationCodeUri_Sync();
                    int attempts = 0;
                    while (!authorizationCode_Sync() && Client.getModuleManager().isEnabled(Spotify.class)) {
                        isChanging = false;
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignore) {
                        }
                        if (Spotify.spotifyManager == null)
                            break;
                        attempts++;
                        if (attempts > 3) {
                            break;
                        }
                    }
                    if (attempts <= 3) {
                        setConnected(true);
                    } else {
                        Notifications.getManager().post("Spotify Error", "Could not authenticate, toggle Spotify to try again!", 5000, Notifications.Type.SPOTIFY);
                    }
                }
            } catch (Exception e) {

            }
            return;
        }

        try {
            if (System.currentTimeMillis() >= (expires) && !isChanging) {
                try {
                    final AuthorizationCodeCredentials authorizationCodeCredentials = spotifyApi.authorizationCodeRefresh().build().execute();

                    spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                    spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                    expires = System.currentTimeMillis() + (authorizationCodeCredentials.getExpiresIn() / 2 * 1000);

                    Notifications.getManager().post("Token Refresh", "New token expires in " + authorizationCodeCredentials.getExpiresIn() + "s!", 2500, Notifications.Type.SPOTIFY);
                    getInformationAboutUsersCurrentPlaybackRequest = null;
                } catch (IOException | SpotifyWebApiException | ParseException e2) {
                    e2.printStackTrace();
                    System.out.println("Spotify Refresh Error: " + e2.getMessage());
                    isChanging = true;
                }
            }

            if (getInformationAboutUsersCurrentPlaybackRequest == null) {
                getInformationAboutUsersCurrentPlaybackRequest = spotifyApi.getInformationAboutUsersCurrentPlayback().build();
            }

            CurrentlyPlayingContext lastPlaying = currentlyPlayingContext;

            long currentTimeMS = System.currentTimeMillis();
            currentlyPlayingContext = getInformationAboutUsersCurrentPlaybackRequest.execute();

            if (currentlyPlayingContext != null) {
                if (currentlyPlayingContext.getDevice() != null) {
                    int newVolume = currentlyPlayingContext.getDevice().getVolume_percent();
                    if (volume != newVolume) {
                        volume = newVolume;
                    }
                }

                repeat = currentlyPlayingContext.getRepeat_state();

                boolean wasPlaying = isPlaying;
                isPlaying = currentlyPlayingContext.getIs_playing();
                if (lastTimeStamp != currentlyPlayingContext.getTimestamp() && Math.abs((currentlyPlayingContext.getTimestamp() - lastTimeStamp)) > 150) {
                    this.lastTimeStamp = currentlyPlayingContext.getTimestamp();
                    this.lastAdjustedTimeStamp = 1500 + (System.currentTimeMillis() - currentTimeMS);
                    this.lastProgressMS = currentlyPlayingContext.getProgress_ms();
                    long estimatedProgress = lastProgressMS + (System.currentTimeMillis() - lastTimeStamp) - lastAdjustedTimeStamp;
                    this.lastProgressMS += (lastProgressMS - estimatedProgress);

                } else if (currentlyPlayingContext.getTimestamp() != lastTimeStamp) {
                    this.lastTimeStamp += (currentlyPlayingContext.getTimestamp() - lastTimeStamp);
                }
                if (currentlyPlayingContext.getItem() instanceof Track && (lastPlaying == null || lastPlaying.getItem() instanceof Track))
                    if (lastPlaying == null || !((Track) currentlyPlayingContext.getItem()).getUri().equals(((Track) lastPlaying.getItem()).getUri())) {
                        Track track = (Track) currentlyPlayingContext.getItem();
                        String artist = "";
                        if (track.getArtists() != null) {
                            artist = " by " + track.getArtists()[0].getName();
                        }

                        String trackName = track.getName();
                        if (trackName.contains(" (")) {
                            trackName = trackName.split(" \\(")[0];
                        }
                        if (trackName.contains(" -")) {
                            trackName = trackName.split(" -")[0];
                        }
                        Notifications.getManager().post("Now Playing", trackName + artist, 1250, Notifications.Type.SPOTIFY);
                    }

            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Info Error: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("expired")) {
                Notifications.getManager().post("Token Expired", "Re-authenticating in 10 seconds!", 10_000, Notifications.Type.SPOTIFY);
                needsReauth = true;
            }
        }
    }

    public CurrentlyPlayingContext getCurrentlyPlaying() {
        return currentlyPlayingContext;
    }

    public void startResumeUsersPlayback_Sync() {
        try {
            final String string = spotifyApi.startResumeUsersPlayback().build().execute();
            Notifications.getManager().post("Status", "Spotify is now playing.", Notifications.Type.SPOTIFY);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void pauseUsersPlayback_Sync() {
        try {
            final String string = spotifyApi.pauseUsersPlayback().build().execute();
            Notifications.getManager().post("Status", "Spotify is now paused.", 1250, Notifications.Type.SPOTIFY);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void setShuffleState(boolean state) {
        try {
            final String string = spotifyApi.toggleShuffleForUsersPlayback(state).build().execute();
            Notifications.getManager().post("Status", "Spotify is " + (state ? "now" : "no longer") + " shuffled.", 1250, Notifications.Type.SPOTIFY);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void nextTrack() {
        try {
            final String string = spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void previousTrack() {
        try {
            if (currentlyPlayingContext.getProgress_ms() > 2500) {
                final String string = spotifyApi.seekToPositionInCurrentlyPlayingTrack(0).build().execute();
            } else {

                final String string = spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void toggleRepeat() {
        try {
            String newState = "off";

            switch (this.repeat) {
                case "context":
                    newState = "track";
                    break;
                case "off":
                    newState = "context";
                    break;
            }

            final String string = spotifyApi.setRepeatModeOnUsersPlayback(newState).build().execute();

            Notifications.getManager().post("Status", "Spotify repeat is " + newState + ".", 1250, Notifications.Type.SPOTIFY);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void pauseSong() {
        if (isPlaying) {
            pauseUsersPlayback_Sync();
            isPlaying = false;
        } else {
            startResumeUsersPlayback_Sync();
            isPlaying = true;
        }
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public int getVolume() {
        return this.volume;
    }

    private long lastTimeStamp, lastAdjustedTimeStamp, lastProgressMS;

    public long getLastTimeStamp() {
        return lastAdjustedTimeStamp;
    }

    public long getLastProgressMS() {
        return lastProgressMS;
    }

    public void setVolume(int volume) {
        try {
            this.volume = volume;
            spotifyApi.setVolumeForUsersPlayback(volume).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }

    public void seekTo(long bruh) {
        try {
            spotifyApi.seekToPositionInCurrentlyPlayingTrack(Math.max(0, (int) bruh)).build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
            Notifications.getManager().post("Spotify Error", e.getMessage(), Notifications.Type.SPOTIFY);
        }
    }
}
