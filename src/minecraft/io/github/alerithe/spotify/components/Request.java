package io.github.alerithe.spotify.components;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * This is a heavily simplified version of my server's Request class for the sake of only needing to read the first line
 *
 * @author Summer/Alerithe
 */
public class Request implements AutoCloseable {
    public Socket client;
    public String path;
    public String query;

    public Request(Socket client) throws Exception {
        this.client = client;
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String[] header = reader.readLine().split(" ");
        this.path = header[1];
        this.query = "";

        if (this.path.contains("?")) {
            this.query = this.path.substring(this.path.indexOf('?') + 1);
            this.path = this.path.substring(0, this.path.indexOf('?'));
        }
    }

    public void write(String str) throws Exception {
        write(str.getBytes(StandardCharsets.UTF_8));
    }

    public void write(byte[] bytes) throws Exception {
        this.client.getOutputStream().write(bytes);
    }

    @Override
    public void close() throws Exception {
        this.client.getOutputStream().flush();
        this.client.close();
    }
}