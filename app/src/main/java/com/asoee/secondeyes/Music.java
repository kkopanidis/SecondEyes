package com.asoee.secondeyes;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Music extends Action {

    private static final String MUSIC_ONLY = MediaStore.Audio.Media.IS_MUSIC + " != 0"; //accept only music files
    private static final String[] COLS = { //what data to store
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, //path
    };
    private static final int SONG_PATH = 1;
    private static final int SONG_NAME = 0;
    //key: artist, value: song and datapath
    static HashMap<String, ArrayList<String[]>> music;
    public boolean isPLaying = false;

    String songToPlay = "";
    String artistToPlay = "";
    String songPath = "";

    MediaPlayer player;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Music(String sentence, Activity callback) {
        super(sentence, callback);
        if (music == null) {
            music = new HashMap<>();
            populateMusic();
        } else {
            if (music.isEmpty()) {
                populateMusic();
            }
        }
    }

    /**
     * Populates the hashmap with all the songs
     */
    private void populateMusic() {
        try (Cursor cursor = callback.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                COLS,
                MUSIC_ONLY,
                null,
                null
        )) {
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                ArrayList<String[]> values = new ArrayList<>();
                String cursorArtist = cursor.getString(0).toLowerCase();

                if (music.containsKey(cursorArtist)) {
                    values = music.get(cursorArtist);
                    values.add(new String[]{cursor.getString(1), cursor.getString(2)});
                    music.put(cursorArtist, values);
                } else {
                    values.add(new String[]{cursor.getString(1), cursor.getString(2)});
                    music.put(cursorArtist, values);
                }
            }
        }
    }

    /**
     * Analyzes the spoken sentence even more to retrieve information.
     */
    @Override
    protected void analyzeSentence() {
        String[] tokens = sentence.split("\\s+");
        String word;
        this.artistToPlay = "";
        this.songToPlay = "";
        for (int i = 0; i < tokens.length; ++i) {
            word = tokens[i];
            switch (word) {
                case "from":
                case "by":
                case "artist":
                    for (int j = i + 1; j < tokens.length; j++) {
                        if (!tokens[j].equals("song"))
                            this.artistToPlay += tokens[j] + " ";
                        else
                            break;
                    }
                    this.artistToPlay = this.artistToPlay.trim();
                    break;
                case "song":
                    for (int j = i + 1; j < tokens.length; j++) {
                        this.songToPlay += tokens[j] + " ";
                    }
                    this.songToPlay = this.songToPlay.trim();
                    break;
            }
        }

        if (!artistToPlay.equals("")) {
            if (songToPlay.equals("")) {
                dialog_step = 1;
                dialog(artistToPlay);
            } else {
                dialog_step = 2;
                dialog(artistToPlay);
            }
        } else {
            dialog("");
        }
    }

    @Override
    protected boolean dialog(String answer) {
        if (answer.trim().equalsIgnoreCase("cancel")
                || answer.trim().equalsIgnoreCase("cancelled")
                || answer.trim().equalsIgnoreCase("canceled")) {

            ((MainWindow) callback).approveAction("Cancelled!"
                    , false);
            return true;
        }

        if (!answer.equals("") && !answer.contains("yes") && !answer.contains("no")) {
            switch (dialog_step) {
                case 1:
                    if (this.artistToPlay.equals("")) //otherwise he has specified the artist
                        this.artistToPlay = answer;
                    ((MainWindow) callback).approveAction("You want to listen to:"
                            + this.artistToPlay + " is that correct?", true);
                    return false;
                case 2:
                    if (this.songToPlay.equals("")) //otherwise he has specified the song
                        this.songToPlay = answer;
                    ((MainWindow) callback).approveAction("You want to listen to:"
                            + this.songToPlay + " by " + this.artistToPlay
                            + " is that correct?"
                            , true);
                    return false;
            }
        } else if (answer.contains("no")) {
            if (dialog_step == 0 && this.artistToPlay.equals("")) {
                return true;
            } else if (dialog_step == 1) {
                this.artistToPlay = "";
                ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                        " what would you like the artist to be?", true);
                return false;
            }
            if (dialog_step == 2) {
                this.songToPlay = "";
                ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                        " what would you like the song to be?", true);
            }
            return false;
        } else if (answer.contains("yes") && dialog_step == 1) {
            if (!music.containsKey(this.artistToPlay)) {
                dialog_step = 0;
                this.artistToPlay = "";
                this.songToPlay = "";
                ((MainWindow) callback).approveAction("Sorry but I could not " +
                        "find that artist in your music library. Would you like to give another one?"
                        , true);
                return false;
            }
        } else if (answer.contains("yes") && dialog_step == 2) {
            //check if song exists
            boolean found = false;
            ArrayList<String[]> values = music.get(this.artistToPlay);
            for (String[] value : values) {
                if (value[SONG_NAME].equalsIgnoreCase(this.songToPlay)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                ((MainWindow) callback).approveAction("Sorry but I could not " +
                        "find " + this.songPath + " by " + this.artistToPlay +
                        "in your music library. Would you like to give another one?"
                        , true);
                dialog_step = 1;
                return false;
            }
        }

        dialog_step++;
        switch (dialog_step) {
            case 1:
                if (this.artistToPlay.equals(""))
                    ((MainWindow) callback).approveAction("Ok, what artist?", true);
                return false;
            case 2:
                if (this.songToPlay.equals(""))
                    ((MainWindow) callback).approveAction("Great! What song would you like?", true);
                return false;
            case 3:
                ((MainWindow) callback).approveAction("Nice choice! Enjoy!", false);
                findSongPath();
                if (songPath.equals("")) {
                    ((MainWindow) callback).approveAction("Sorry, couldn't locate the song in your phone" +
                            "for some reason.", false);
                    return false;
                } else {
                    executeCommand();
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * Sets the path of the song to play.
     */
    private void findSongPath() {
        if (artistToPlay != null && music.containsKey(artistToPlay)) {
            ArrayList<String[]> qifsha = music.get(artistToPlay);
            for (String[] ro : qifsha) {
                if (ro[SONG_NAME].equalsIgnoreCase(songToPlay)) {
                    this.songPath = ro[SONG_PATH];
                    break;
                }
            }
        } else {
            Collection<ArrayList<String[]>> lists = music.values();
            for (ArrayList<String[]> qifsha : lists) {
                for (String[] ro : qifsha) {
                    if (ro[SONG_NAME].equalsIgnoreCase(songToPlay)) {
                        this.songPath = ro[SONG_PATH];
                        break;
                    }
                }
            }
        }
    }

    //Changed the functionality of the method
    @Override
    public void executeCommand() {
        player = MediaPlayer.create(callback, Uri.parse(songPath));
        player.start();
        isPLaying = true;
    }

    public void pause() {
        isPLaying = false;
        if (player != null)
            if (player.isPlaying())
                player.pause();
    }

    @Override
    protected boolean inputCheck(String input) {
        return false;
    }
}
