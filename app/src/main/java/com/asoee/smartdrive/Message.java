package com.asoee.smartdrive;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import java.util.HashMap;

public class Message extends Action {

    private String message;
    private String contactName;
    private HashMap<String, String> contacts;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Message(String sentence, Activity callback) {
        super(sentence, callback);
        //Do something
        if (message == null) {
            return;
        }
        ((MainWindow) callback).approveAction("I will text: " + contactName + " " + message
                + "  is that correct?", true);
    }

    @Override
    protected void analyzeSentence() {
        getContacts();
        String sentence_proc = sentence.toLowerCase().trim();
        String[] tokens = sentence_proc.split("\\s");
        int token;
        String first = tokens[0].trim();
        String second = " ";
        String third = " ";
        String last = tokens[tokens.length - 1].trim();
        if (tokens.length > 1)
            second = tokens[1].trim();
        if (tokens.length > 2)
            third = tokens[2].trim();

        switch (first) {
            case "message":
                if (second.equals("to") && contacts.containsKey(third)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(third) + third.length(), sentence_proc.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(second) + second.length(), sentence_proc.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence_proc = sentence_proc.substring("message".length(), sentence_proc.indexOf(last)).trim();
                    if (sentence_proc.substring(sentence_proc.lastIndexOf(' '), sentence_proc.length()).contains("to")) {
                        sentence_proc = sentence_proc.substring(0, sentence_proc.lastIndexOf(' ')).trim();
                    }

                    message = sentence_proc;
                    contactName = last;
                } else {
                    //incorrect
                    message = null;
                }
                break;
            case "send":
                if (second.equals("to") && contacts.containsKey(third)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(third) + third.length(), sentence_proc.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(second) + second.length(), sentence_proc.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence_proc = sentence_proc.substring("send".length(), sentence_proc.indexOf(last)).trim();
                    if (sentence_proc.substring(sentence_proc.lastIndexOf(' '), sentence_proc.length()).contains("to")) {
                        sentence_proc = sentence_proc.substring(0, sentence_proc.lastIndexOf(' ')).trim();
                    }
                    message = sentence_proc;
                    contactName = last;
                } else {
                    //incorrect
                    message = null;
                }
                break;
            case "text":
                if (second.equals("to") && contacts.containsKey(third)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(third) + third.length(), sentence_proc.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence_proc.substring(sentence_proc.indexOf(second) + second.length(), sentence_proc.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence_proc = sentence_proc.substring("text".length(), sentence_proc.indexOf(last)).trim();
                    if (sentence_proc.substring(sentence_proc.lastIndexOf(' '), sentence_proc.length()).contains("to")) {
                        sentence_proc = sentence_proc.substring(0, sentence_proc.lastIndexOf(' ')).trim();
                    }

                    message = sentence_proc;
                    contactName = last;
                } else {
                    //incorrect
                    message = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void dialog(int step) {

    }

    @Override
    public void executeCommand() {
        SmsManager.getDefault().sendTextMessage(contacts.get(contactName),
                null, message, null, null);
    }

    void getContacts() {
        contacts = new HashMap<>();
        Cursor contacts = callback.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (contacts.moveToNext()) {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            this.contacts.put(name, phoneNumber);
        }

        contacts.close();
    }

}
