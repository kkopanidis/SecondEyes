package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainWindow extends Activity {

    HashMap<String, String> keywords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
        keywords = new HashMap<>();
        populateMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSpeechDetection(View view) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, 1);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            VocalResult voiceResult = analyzeVocalCommand(thingsYouSaid);
            System.out.println("such wow");
        }
    }

    protected VocalResult analyzeVocalCommand(ArrayList<String> list) {
        String[] tokens;
        String keyword;
        for (String line : list) {
            tokens = line.split("\\s");
            for (int i = 0; i < tokens.length; i++) {
                keyword = getRecognizedKeyword(tokens[i].toLowerCase());
                if (keyword != null)
                    return VocalResult.getInstance(keyword, line, i);
            }
        }
        return null;
    }

    protected String getRecognizedKeyword(String str) {
        if (keywords.containsKey(str))
            return keywords.get(str);

        return null;
    }

    void populateMap() {

        URL keywordsFile = MainWindow.class.getResource("../res/commands/keywords");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(
                    getResources().getIdentifier("raw/keywords",
                            "raw", getPackageName()))));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                keywords.put(tokens[0], tokens[1]);
            }

        } catch (Exception ignore) {
        }
    }
}
