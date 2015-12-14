package com.asoee.smartdrive;

import android.app.AlarmManager;
import android.content.Intent;
import android.provider.AlarmClock;

public class Alarm extends Action {

    /**
     * Does constructor stuff
     * @param sentence the sentence given
     */
    public Alarm(String sentence){
        super(sentence);
    }

    @Override
    protected void analyzeSentence() {
        sentence = sentence.toLowerCase();
        String[] tokens = sentence.split("\\s");
        if(tokens[0].equals("alarm"))
            if(tokens[1].equals("for"))
                setAlarm(tokens[2]);
            else
                setAlarm(tokens[1]);
        else if(tokens[0].equals("set") && tokens[1].equals("alarm"))
            if(tokens[2].equals("for"))
                setAlarm(tokens[3]);
            else
                setAlarm(tokens[2]);

    }

    @Override
    public Intent executeCommandIntent() {

        return null;
    }

    void setAlarm(String time){
        String hour = time.split(":")[0];
        String minutes = time.split(":")[1];

        Intent alarm = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarm.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(hour));
        alarm.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(minutes));
        alarm.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        MainWindow.active_context.startActivity(alarm);
    }

}
