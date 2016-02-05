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
        time = "";
        String sentence_proc = sentence.toLowerCase();
        String[] tokens = sentence_proc.substring(sentence_proc.indexOf("alarm") + "alarm".length()).trim().split("\\s");
        if (tokens[0].equals("for")) {
            time = tokens[1];
        } else {
            time = sentence_proc.substring(sentence_proc.indexOf("alarm") + "alarm".length());
        }
        if (time.equals("")) {
            dialog("");
            return;
        }
        dialog_step++;
        dialog(time);

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
                    if (inputCheck(answer)) {
                        this.time = answer;
                        ((MainWindow) callback).approveAction("The alarm will be set for:"
                                + this.time + " is that correct?"
                                , true);
                    } else {
                        ((MainWindow) callback).approveAction("Sorry, i didn't catch the time, " +
                                "please say it again"
                                , true);
                    }
                    return false;
            }
        } else if (answer.equalsIgnoreCase("no")) {
            ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                    " what would you like it to be?"
                    , true);
            return false;
        }

        dialog_step++;
        switch (dialog_step) {
            case 1:
                ((MainWindow) callback).approveAction("Waking up early are we?" +      // lel
                        " Tell me the hour and the minute that you want the alarm"
                        , true);
                return false;
            case 2:
                ((MainWindow) callback).approveAction("Great! Adding alarm now!"
                        , false);
                executeCommand();
                return true;
            default:
                return false;
        }
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

    @Override
    protected boolean inputCheck(String input) {
        if (input.equals(""))
            return false;
        if (input.contains("and"))
            input.replace("and", ":");
        input.replaceAll(" ", "");
        if (!input.contains(":"))
            return false;
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c) && c != ':')
                return false;
        }
        return true;
    }


}
