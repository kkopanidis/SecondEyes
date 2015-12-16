package com.asoee.smartdrive;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Music extends Action {

    String songToPlay;
    String artistToPlay;
    String songPath;

    //key: artist, value: song and datapath
    static HashMap<String, ArrayList<String>> music;

    private static final String MUSIC_ONLY = MediaStore.Audio.Media.IS_MUSIC + " != 0"; //accept only music files

    private static final String[] COLS = { //what data to store
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, //path
    };

    private static final int SONG_PATH = 1;
    private static final int SONG_NAME = 0;

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
                    this.songToPlay = this.artistToPlay = sentence.substring(i + 1);
                    break;
                case "listen":
                    if (tokens[i + 1].equals("to"))
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
     * Populates the hashmap with all the songs
     */
    private static void populateMusic() {
        //hardcoded /sdcard seems like a bad choice, so let's use the API for safety
        // File sdcard = Environment.getExternalStorageDirectory(); //this also looks like a bad idea
        //as it relies on absolute sdcard paths


        Activity musicQ = new Activity(); //not sure if possible
        //maybe have a Context musicQ as argument, don't know how to use it

        Cursor cursor = musicQ.getContentResolver().query(
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

        ArrayList<String> value = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                value.add(cursor.getString(1));
                value.add(cursor.getString(2));
                music.put(cursor.getString(0), value);
                value = new ArrayList<>();
            }
        } catch (NullPointerException ignore) {
        }

    }

    /**
     * Sets the path of the song to play.
     */
    private void findSongPath() {
        if (artistToPlay != null && music.containsKey(artistToPlay))
            this.songPath = music.get(artistToPlay).get(SONG_PATH);
        else {
            Collection<ArrayList<String>> lists = music.values();
            for (ArrayList<String> list : lists)
                if (list.get(SONG_NAME).equals(songToPlay))
                    this.songPath = list.get(SONG_PATH);
        }
    }

    //Changed the functionality of the method you need to make changes(duh..)
    @Override
    public void executeCommand() {
        //not even sure about this shit
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.android.music", "com.android.music.MediaPlaybackActivity");
        intent.setComponent(comp);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(songPath); //maybe use URI
        intent.setDataAndType(Uri.fromFile(file), "audio/*");

    }
}
