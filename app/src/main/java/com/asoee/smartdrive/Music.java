package com.asoee.smartdrive;

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
    String songToPlay;
    String artistToPlay;
    String songPath;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Music(String sentence) {
        super(sentence);
        music = new HashMap<>();
        analyzeSentence();
        populateMusic();
        findSongPath();
        executeCommand();
    }

    /**
     * Populates the hashmap with all the songs
     */
    private static void populateMusic() {
        //hardcoded /sdcard seems like a bad choice, so let's use the API for safety
        // File sdcard = Environment.getExternalStorageDirectory(); //this also looks like a bad idea
        //as it relies on absolute sdcard paths


        //not sure if possible
        //maybe have a Context musicQ as argument, don't know how to use it

        Cursor cursor = MainWindow.activeContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                COLS,
                MUSIC_ONLY,
                null,
                null
        );

        /*
        for absolute path maybe we'll need this
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         ->>> cursor.getString(column_index);
        */


        try {
            while (cursor.moveToNext()) {
                ArrayList<String[]> values = new ArrayList<>();

                if (music.containsKey(cursor.getString(0))) {
                    values = music.get(cursor.getString(0));
                    values.add(new String[]{cursor.getString(1), cursor.getString(2)});
                    music.put(cursor.getString(0), values);
                } else {
                    values.add(new String[]{cursor.getString(1), cursor.getString(2)});
                    music.put(cursor.getString(0), values);
                }

            }
        } catch (NullPointerException ignore) {
        }

    }

    /**
     * Analyzes the spoken sentence even more to retrieve information.
     */
    @Override
    protected void analyzeSentence() {
        String[] tokens = sentence.split(" ");
        String word;
        //let's assume (s)he'll choose a song and not something containing the artist, album, or all of them
        for (int i = 0; i < tokens.length; ++i) {
            word = tokens[i];
            switch (word) {
                case "play":
                    this.songToPlay = this.artistToPlay = sentence.substring(i + "play".length()).trim();
                    break;
                case "listen":
                    if (tokens[i + 1].equalsIgnoreCase("to"))
                        this.songToPlay = this.artistToPlay = sentence.substring(i + 2);
                    break;
                case "from":
                    this.artistToPlay = sentence.substring(i + 1);
                    break;
                case "song":
                    this.songToPlay = sentence.substring(i + 1);
                    break;
                case "artist":
                    this.artistToPlay = sentence.substring(i + 1);
                    break;
            }
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

        if (songPath == null)
            return;
        MediaPlayer player = MediaPlayer.create(MainWindow.activeContext, Uri.parse(songPath));
        player.start();

    }
}
