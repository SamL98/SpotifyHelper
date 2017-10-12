import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.javafx.geom.RoundRectangle2D;

public class SpotifyHelper {

    private static boolean paused = false;

    private static Timer timer = new Timer();

    private static JFrame frame;
    private static JButton prevButton = new JButton();
    private static JButton pausePlayButton = new JButton();
    private static JButton nextButton = new JButton();

    private static void setDisplayToPaused() {
        ImageIcon icon = new ImageIcon("resources/play-button.png");
        pausePlayButton.setIcon(icon);
    }

    private static void setDisplayToPlaying() {
        ImageIcon icon = new ImageIcon("resources/pause-button.png");
        pausePlayButton.setIcon(icon);
    }

    private static void pause() {
        setDisplayToPaused();
        performCommand(PAUSE_COMMAND);
    }

    private static void resume() {
        setDisplayToPlaying();
        performCommand(PLAY_COMMAND);
    }

    private static void formatTitle() {
        String title = performCommand("name of " + CURRENT_TRACK_COMMAND);
        String artist = performCommand("artist of " + CURRENT_TRACK_COMMAND);
        frame.setTitle(title + " - " + artist);
    }

    private static void scheduleTrackChange() {
        timer.schedule(new TimerTask() {
            public void run() {
                checkForEndOfSong();
            }
        }, (long) 0, (long) 1000);
    }

    private static void checkForEndOfSong() {
        int currPos = (int) Double.parseDouble(performCommand(POSITION_COMMAND));
        if (currPos == 0) {
            timer.schedule(new TimerTask() {
                public void run() {
                    formatTitle();
                }
            }, (long) 1000);
        }
    }

    private static class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(prevButton)) {
                performCommand(PREVIOUS_COMMAND);
                if (paused) {
                    paused = false;
                    resume();
                }
                formatTitle();
            } else if (source.equals(pausePlayButton)) {
                if (paused) {
                    resume();
                } else {
                    pause();
                }
                paused = !paused;
            } else {
                performCommand(NEXT_COMMAND);
                if (paused) {
                    paused = false;
                    resume();
                }
                formatTitle();
            }
        }
    }

    private static String performCommand(String command) {
        String[] args = {"osascript", "-e", BASE_COMMAND + command};
        InputStream stream;
        try {
            stream = Runtime.getRuntime().exec(args).getInputStream();
        } catch (IOException e) {
            System.out.println("Something went wrong performing command");
            return "";
        }
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next().trim() : "";
    }

    private static final ActionListener bl = new ButtonListener();

    private static final String PLAYER_STATUS_PAUSED = "paused";
    private static final String PLAYER_STATUS_PLAYING = "playing";

    private static final String BASE_COMMAND = "tell application \"Spotify\" to ";
    private static final String PAUSE_COMMAND = "pause";
    private static final String PLAY_COMMAND = "play";
    private static final String NEXT_COMMAND = "next track";
    private static final String PREVIOUS_COMMAND = "previous track";
    private static final String STATUS_COMMAND = "player state";
    private static final String CURRENT_TRACK_COMMAND = "current track";
    private static final String POSITION_COMMAND = "player position";
    private static void displayGui() {
        frame = new JFrame("Spotify Helper");
        scheduleTrackChange();
        formatTitle();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setBackground(new Color(255, 255, 255));
        frame.setSize(250, 100);

        Container pane = frame.getContentPane();
        pane.setLayout(new GridLayout());

        ImageIcon prevIcon = new ImageIcon("resources/previous-track.png");
        prevButton.setIcon(prevIcon);

        ImageIcon pausePlayIcon = new ImageIcon("resources/pause-button.png");
        if (performCommand(STATUS_COMMAND).equals(PLAYER_STATUS_PAUSED)) {
            paused = true;
            pausePlayIcon = new ImageIcon("resources/play-button.png");
        }
        pausePlayButton.setIcon(pausePlayIcon);

        ImageIcon nextIcon = new ImageIcon("resources/next-track-button.png");
        nextButton.setIcon(nextIcon);

        JButton[] buttons = {prevButton, pausePlayButton, nextButton};
        for (JButton button: buttons) {
            button.addActionListener(bl);
        }

        pane.add(prevButton);
        pane.add(pausePlayButton);
        pane.add(nextButton);

        frame.setAlwaysOnTop(true);
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayGui();
            }
        });
    }
}