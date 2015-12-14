package com.asoee.smartdrive;

import android.content.Intent;

public class Message extends Action {

    /**
     * Does constructor stuff
     * @param sentence the sentence given
     */
    public Message(String sentence){
        super(sentence);
    }

    @Override
    protected void analyzeSentence() {

    }

    @Override
    public Intent executeCommandIntent() {
        return null;
    }
}
