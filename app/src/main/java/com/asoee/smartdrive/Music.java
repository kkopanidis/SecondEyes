package com.asoee.smartdrive;

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
            populateMusic(); //to check if its best after or before analyzeSentence or dialog in terms of
            //usability
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
        //hardcoded /sdcard seems like a bad choice, so let's use the API for safety
        // File sdcard = Environment.getExternalStorageDirectory(); //this also looks like a bad idea
        //as it relies on absolute sdcard paths

        //not sure if possible
        //maybe have a Context musicQ as argument, don't know how to use it

        //((MainWindow) callback).approveAction("Ok, please be patient while I load your music.", true);

        try (Cursor cursor = callback.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                COLS,
                MUSIC_ONLY,
                null,
                null
        )) {
        /*
        for absolute path maybe we'll need this
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         ->>> cursor.getString(column_index);
        */
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
        //((MainWindow) callback).approveAction("Ok, done!", true);
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
        //let's assume (s)he'll choose a song and not something containing the artist, album, or all of them
        for (int i = 0; i < tokens.length; ++i) {
            word = tokens[i];
            switch (word) {
                case "from":
                case "by":
                case "artist":
                    for (int j = i + 1; j < tokens.length; j++) {
                        if (!tokens[j].equals("song")) {
                            this.artistToPlay += tokens[j] + " ";
                        }
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

        if (!answer.equals("") && !answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
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
        } else if (answer.equalsIgnoreCase("no")) {
            if (dialog_step == 0 && this.artistToPlay.equals("")) {
                return true;
            } else if (dialog_step == 1) {
                this.artistToPlay = "";
                ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                        " what would you like the artist to be?", true);
            }

            if (dialog_step == 2) {
                this.songToPlay = "";
                ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                        " what would you like the song to be?", true);
            }
            return false;
        } else if (answer.equalsIgnoreCase("yes") && dialog_step == 1) {
            if (!music.containsKey(this.artistToPlay)) {
                dialog_step = 0;
                this.artistToPlay = "";
                this.songToPlay = "";
                ((MainWindow) callback).approveAction("Sorry but I could not " +
                        "find that artist in your music library. Would you like to give another one?"
                        , true);
                return false;
            }
        } else if (answer.equalsIgnoreCase("yes") && dialog_step == 2) {
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
                findSongPath(); //find dat song path pls
                if (songPath.equals("")) {
                    ((MainWindow) callback).approveAction("Sorry, couldn't locate the song in your phone" +
                            "for some reason.", false); //or maybe restart the dialog, whatever
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

    //Changed the functionality of the method you need to make changes(duh..)
    @Override
    public void executeCommand() {
        player = MediaPlayer.create(callback, Uri.parse(songPath));
        player.start();
    }

    public void pause() {
        if (player != null)
            if (!player.isPlaying())
                player.pause();
    }

    @Override
    protected boolean inputCheck(String input) {
        return false;
    }
}
