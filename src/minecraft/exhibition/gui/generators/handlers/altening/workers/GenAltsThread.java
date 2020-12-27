/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.generators.handlers.altening.workers;

import exhibition.Client;
import exhibition.gui.altmanager.Alt;
import exhibition.gui.altmanager.AltManager;
import exhibition.gui.altmanager.Alts;
import exhibition.gui.generators.gui.AlteningGeneratorGUI;
import exhibition.gui.generators.handlers.altening.AlteningGenHandler;
import exhibition.management.notifications.usernotification.Notification;
import exhibition.management.notifications.usernotification.Notifications;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GenAltsThread extends Thread implements Runnable {

    private boolean running;

    private AlteningGenHandler handler;

    private AlteningGeneratorGUI generatorGUI;

    private int generated;

    public GenAltsThread(AlteningGenHandler handler, AlteningGeneratorGUI generatorGUI) {
        this.generatorGUI = generatorGUI;
        this.handler = handler;
    }

    @Override
    public void run() {
        this.running = true;
        while (isRunning()) {
            if (handler.getUser().isLimited()) {
                stopThread();
                Notifications.getManager().post("Alt Limit Reached!", "You have reached your daily generating limit!", 5000, Notifications.Type.WARNING);
            }
            AlteningGenHandler.AlteningGenAlt currentAlt = handler.getAltLogin();
            if (currentAlt == null) {
                Notifications.getManager().post("Generating Error", "Could not generate alt!");
                continue;
            }
            generated++;
            generatorGUI.setThreadStatus("\2477Generated \247b" + generated + " \2477alts!");
            AltManager.registry.add(new Alt(currentAlt.getEmail(), "password", currentAlt.getUsername(), Alt.Status.Unchecked, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)));
            Notifications.getManager().post("Added Alt", "Added " + currentAlt.getUsername() + " to alt manager!");
            try {
                Client.getFileManager().getFile(Alts.class).saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        generatorGUI.setThreadStatus("\247cStopped after \247b" + generated + " \247calts.");
        Notifications.getManager().post("Generating Stopped!", "Generating thread has stopped!", 5000, Notifications.Type.NOTIFY);
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public void stopThread() {
        this.running = false;
    }
}
