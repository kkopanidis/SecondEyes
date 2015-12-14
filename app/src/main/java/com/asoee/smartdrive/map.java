package com.asoee.smartdrive;

import android.content.Intent;

public class map extends Action {

    /**
     * Does constructor stuff
     * @param sentence the sentence given
     */
    public map(String sentence){
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
