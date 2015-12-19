package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainWindow extends Activity implements TextToSpeech.OnInitListener {

    HashMap<String, String> keywords;
    static Context active_context;
    static MainWindow activity;
    private TextToSpeech mTts;
    private boolean locked = false;
    private Action action;
    private boolean approval = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
        keywords = new HashMap<>();
        activity = this;
        active_context = this;
        populateMap();
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 567);
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

        if (requestCode == 567) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);
                mTts.setLanguage(Locale.UK);

            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //Retrieve the user's answer to the approval
            if (action != null)
                for (String answer : thingsYouSaid) {
                    if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("ok")) {
                        action.executeCommand();
                        action = null;
                        return;
                    } else {
                        action = null;
                        return;
                    }
                }
            VocalResult voiceResult = analyzeVocalCommand(thingsYouSaid);
            if (voiceResult == null)
                return;
            locked = true;
            //Currently not convenient, will be refined
            switch (voiceResult.getKeyword()) {
                case "message":
                    action = new Message(voiceResult.getSentence());
                    break;
                case "music":
                    action = new Music(voiceResult.getSentence());
                    break;
                case "map":
                    action = new Map(voiceResult.getSentence());
                    break;
                case "alarm":
                    action = new Alarm(voiceResult.getSentence());
                    break;
                case "call":
                    action = new Call(voiceResult.getSentence());
                    break;
                default:
                    break;
            }
            VocalResult.destroy();
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

    @Override
    public void onInit(int status) {
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                if (approval)
                    answer();
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        if (status == TextToSpeech.SUCCESS) {
            mTts.speak("Welcome to Blind Assist or SecondEyes. We can't decide." +
                            "So anyway how may i help you?",
                    TextToSpeech.QUEUE_FLUSH, null, null);

        }
    }

    public void approveAction(String approval_request) {
        approval = true;
        mTts.speak(approval_request,
                TextToSpeech.QUEUE_FLUSH, null, null);

    }

    public void answer() {
        approval = false;
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, 1);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }
}
