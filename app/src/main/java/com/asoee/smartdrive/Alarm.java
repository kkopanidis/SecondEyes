package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Intent;
import android.provider.AlarmClock;

public class Alarm extends Action {
    String time;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Alarm(String sentence, Activity callback) {
        super(sentence, callback);
    }

    @Override
    protected void analyzeSentence() {
        String sentence_proc = sentence.toLowerCase();
        String[] tokens = sentence_proc.split("\\s");
        if (tokens[0].equals("alarm"))
            if (tokens[1].equals("for"))
                time = tokens[2];
            else
                time = tokens[1];
        else if (tokens[0].equals("set") && tokens[1].equals("alarm"))
            if (tokens[2].equals("for"))
                time = tokens[3];
            else
                time = tokens[2];

        if (time != null)
            ((MainWindow) callback).approveAction("I will set an alarm for: " + time + " is that correct?", true);
    }

    @Override
    public void executeCommand() {

        String hour = time.split(":")[0];
        String minutes = time.split(":")[1];
        Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarm.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(hour));
        alarm.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(minutes));
        alarm.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        callback.startActivity(alarm);
        time = null;
    }


}
