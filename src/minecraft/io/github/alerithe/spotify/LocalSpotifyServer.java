package io.github.alerithe.spotify;

import exhibition.Client;
import exhibition.module.impl.other.Spotify;
import io.github.alerithe.spotify.components.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * A small bare-bones web server implementation to act as a Spotify callback
 *
 * @author Summer/Alerithe
 */
public class LocalSpotifyServer {
    /**
     * The port this local server should listen on.
     */
    public static int PORT = 8888;

    /**
     * Toggle the listeners running status.
     */
    public static boolean LISTENING;

    /**
     * The path Spotify will redirect to.
     */
    public static String CALLBACK_PATH = "/callback";

    /**
     * The authentication code retrieved from Spotify's request.
     */
    public static String AUTHENTICATION_CODE = "";

    private static ServerSocket serverSocket; // The ServerSocket instance to listen on

    /**
     * Initializes the server and attempts to bind it to the port specified by {@link LocalSpotifyServer#PORT}.
     * Run this BEFORE starting the listener!
     */
    public static void init() throws IOException {
        if(serverSocket == null) {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", PORT));
        }
    }

    /**
     * Starts the listener for incoming web requests.
     *
     * @throws Exception In the event of any error that occurs
     */
    public static void listen() {
        if (!LISTENING) {
            LISTENING = true;

            new Thread(() -> {
                while (LISTENING && Client.getModuleManager().isEnabled(Spotify.class)) {
                    try {
                        try (Request request = new Request(serverSocket.accept())) {
                            if (request.path.startsWith(LocalSpotifyServer.CALLBACK_PATH)) {
                                if (request.query.startsWith("code=")) {
                                    LocalSpotifyServer.AUTHENTICATION_CODE = request.query.substring(5);
                                    LocalSpotifyServer.stop();
                                    request.write("HTTP/1.1 200 OK\n\nSpotify is now linked!\nYou may now close this tab.");
                                } else {
                                    request.write("HTTP/1.1 400 Bad Request\n\nNo authentication code.");
                                }
                            } else {
                                request.write("HTTP/1.1 401 Forbidden\n\nNon-callback resource access attempt.");
                            }
                        }
                    } catch (Exception e) {
                        // This can be ignored
                    }
                }
            }).start();
        } else {
            throw new RuntimeException("Local Spotify server is already listening!");
        }
    }

    /**
     * Stops the listener's thread gracefully (hopefully).
     */
    public static void stop() {
        LISTENING = false;
    }
}
