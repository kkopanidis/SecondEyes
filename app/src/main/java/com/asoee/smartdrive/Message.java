package com.asoee.smartdrive;

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
    public Message(String sentence) {
        super(sentence);
        //Do something
        if (message == null) {

            return;
        }
        MainWindow.activity.approveAction("I will text: " + contactName + " " + message
                + "  is that correct?", true);
    }

    @Override
    protected void analyzeSentence() {
        getContacts();
        sentence = sentence.toLowerCase().trim();
        String[] tokens = sentence.split("\\s");
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
                    message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence = sentence.substring("message".length(), sentence.indexOf(last)).trim();
                    if (sentence.substring(sentence.lastIndexOf(' '), sentence.length()).contains("to")) {
                        sentence = sentence.substring(0, sentence.lastIndexOf(' ')).trim();
                    }

                    message = sentence;
                    contactName = last;
                } else {
                    //incorrect
                    message = null;
                }
                break;
            case "send":
                if (second.equals("to") && contacts.containsKey(third)) {
                    message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence = sentence.substring("send".length(), sentence.indexOf(last)).trim();
                    if (sentence.substring(sentence.lastIndexOf(' '), sentence.length()).contains("to")) {
                        sentence = sentence.substring(0, sentence.lastIndexOf(' ')).trim();
                    }
                    message = sentence;
                    contactName = last;
                } else {
                    //incorrect
                    message = null;
                }
                break;
            case "text":
                if (second.equals("to") && contacts.containsKey(third)) {
                    message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                    contactName = third;
                } else if (contacts.containsKey(second)) {
                    message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                    contactName = second;
                } else if (contacts.containsKey(last)) {
                    sentence = sentence.substring("text".length(), sentence.indexOf(last)).trim();
                    if (sentence.substring(sentence.lastIndexOf(' '), sentence.length()).contains("to")) {
                        sentence = sentence.substring(0, sentence.lastIndexOf(' ')).trim();
                    }

                    message = sentence;
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
    public void executeCommand() {
        SmsManager.getDefault().sendTextMessage(contacts.get(contactName),
                null, message, null, null);
    }

    void getContacts() {
        contacts = new HashMap<>();
        Cursor contacts = MainWindow.active_context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (contacts.moveToNext()) {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            this.contacts.put(name, phoneNumber);
        }

        contacts.close();
    }

}
