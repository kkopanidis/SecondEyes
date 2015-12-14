package com.asoee.smartdrive;


import android.content.Intent;

public abstract class Action {

    String sentence;

    /**
     * Does constructor stuff
     * @param sentence the sentence given
     */
    public Action(String sentence){
        this.sentence = sentence;
        analyzeSentence();
    }

    /**
     * Analyzes the spoken sentence even more to retrieve information.
     */
    protected abstract void analyzeSentence();

    /**
     * Executes the finally retrieved command.
     */
    public abstract Intent executeCommandIntent();

}
