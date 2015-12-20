package com.asoee.smartdrive;

import android.text.format.Time;

import java.util.Calendar;
import java.util.Locale;

public class DateTime extends Action {

    boolean isTime;
    boolean isDate;

    public DateTime(String sentence) {
        super(sentence);
    }

    @Override
    protected void analyzeSentence() {
        sentence = sentence.toLowerCase();
        if (sentence.contains("time"))
            isTime = true;
        if (sentence.contains("date"))
            isDate = true;
        executeCommand();
        MainWindow.activity.action = null;
    }

    @Override
    public void executeCommand() {
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();
        Calendar c = Calendar.getInstance();

        String tell;
        if (isTime && isDate) {
            tell = "Today is "+c.getDisplayName(Calendar.DAY_OF_WEEK,
                    Calendar.LONG, Locale.getDefault())+" "+c.get(Calendar.DAY_OF_MONTH)
                    +" "+c.get(Calendar.DAY_OF_MONTH) +" "+ c.get(Calendar.MONTH)
                    +" "+c.get(Calendar.YEAR)+" and the time is " + now.hour+":"+now.minute;
        } else if (isDate) {
            tell = "Today is "+c.getDisplayName(Calendar.DAY_OF_WEEK,
                    Calendar.LONG, Locale.getDefault())+" "+c.get(Calendar.DAY_OF_MONTH)
                    +"/"+ c.get(Calendar.MONTH) +"/"+c.get(Calendar.YEAR);
        } else if (isTime) {
            tell = "It's " + now.hour + ":" + now.minute;
        }
        else
            return;

        MainWindow.activity.approveAction(tell , false);

    }
}
