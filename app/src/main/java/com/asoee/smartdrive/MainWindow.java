package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainWindow extends Activity implements TextToSpeech.OnInitListener {

    public static boolean approval = false;
    HashMap<String, String> keywords = new HashMap<>();
    Action action;
    private TextToSpeech mTts;
    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);
        populateMap();
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 567);
    }

    protected void onDestroy() {
        super.onDestroy();
        mTts.shutdown();
    }

    public void onClickSpeechDetection(View view) {
        // Vibrate for 500 milliseconds
        v.vibrate(500);//*/
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
            if (action != null) {
                if (action.dialog(thingsYouSaid.get(0).toLowerCase()))
                    action = null;
                return;
            }
            VocalResult voiceResult = analyzeVocalCommand(thingsYouSaid);
            if (voiceResult == null)
                return;
            String sentence = voiceResult.getSentence();
            MainWindow thisActivity = this;
            String className = voiceResult.getKeyword();
            className = className.substring(0, 1).toUpperCase() + className.substring(1); //Capitalize first letter
            Class resultClass = null;
            try {
                resultClass = Class.forName("com.asoee.smartdrive." + className);
                Constructor constructor = resultClass.getConstructor(String.class, Activity.class);
                action = (Action) constructor.newInstance(sentence, thisActivity);
            } catch (Exception e) {
                e.printStackTrace();
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
        if (status == TextToSpeech.SUCCESS) {
            SharedPreferences pref = getSharedPreferences("com.asoee.smartdrive", MODE_PRIVATE);
            if (pref.contains("init"))
                mTts.speak("Welcome! Tap on the screen and tell me how i can help you!",
                        TextToSpeech.QUEUE_FLUSH, null, null);
            else {
                pref.edit().putBoolean("init", true).commit();
                String greeting = "";
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(
                            getResources().getIdentifier("raw/first_greet",
                                    "raw", getPackageName()))));
                    String line = br.readLine().trim();
                    do {
                        greeting += line;
                        line = br.readLine().trim();
                    } while (line != null && line.length() != 0);
                } catch (Exception ignore) {
                }
                mTts.speak(greeting,
                        TextToSpeech.QUEUE_FLUSH, null, null);
            }

        }
    }

    public void approveAction(String approval_request, boolean approval) {
        MainWindow.approval = approval;
        mTts.speak(approval_request,
                TextToSpeech.QUEUE_FLUSH, null, null);
        if (approval) {
            while (mTts.isSpeaking()) ; //jesus fuck..
            answer();
        } else {
            action = null;
        }
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
