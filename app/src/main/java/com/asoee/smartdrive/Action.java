package com.asoee.smartdrive;


import android.app.Activity;
import android.content.Intent;

public abstract class Action {

    final String sentence;
    final Activity callback;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Action(String sentence, Activity callback) {
        this.sentence = sentence;
        this.callback = callback;
        analyzeSentence();
    }

    /**
     * Analyzes the spoken sentence even more to retrieve information.
     */
    protected abstract void analyzeSentence();

    /**
     * Executes the finally retrieved command.
     */
    public abstract void executeCommand();

}
