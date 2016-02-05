package com.asoee.smartdrive;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import java.util.HashMap;

public class Message extends Action {

    private String message;
    private String contactName = "";
    private HashMap<String, String> contacts;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Message(String sentence, Activity callback) {
        super(sentence, callback);
        dialog("");
    }

    @Override
    protected void analyzeSentence() {
    }

    @Override
    protected boolean dialog(String answer) {
        if (!answer.equals("") && !answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
            switch (dialog_step) {
                case 1:
                    this.contactName = answer;
                    ((MainWindow) callback).approveAction("You want to send a message to:"
                            + this.contactName + " is that correct?"
                            , true);
                    return false;
                case 2:
                    this.message = answer;
                    ((MainWindow) callback).approveAction("You want the message to say:"
                            + this.message + " is that correct?"
                            , true);
                    return false;
            }
        } else if (answer.equalsIgnoreCase("no")) {
            if (dialog_step == 0 && this.contactName.equals("")) {
                return true;
            }
            ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                    " what would you like it to be?"
                    , true);
            return false;
        } else if (answer.equalsIgnoreCase("yes") && dialog_step == 1) {
            getContacts();
            if (!contacts.containsKey(this.contactName)) {
                ((MainWindow) callback).approveAction("Sorry but i could not " +
                        "find that name in your contact list. Would you like to give another one?"
                        , true);
                dialog_step = 0;
                return false;
            }

        }

        dialog_step++;
        switch (dialog_step) {
            case 1:
                ((MainWindow) callback).approveAction("Ok, to whom?"
                        , true);
                return false;
            case 2:
                ((MainWindow) callback).approveAction("Great! And what do you want to say?"
                        , true);
                return false;
            case 3:
                ((MainWindow) callback).approveAction("OK sending the message now!"
                        , false);
                executeCommand();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void executeCommand() {
        SmsManager.getDefault().sendTextMessage(contacts.get(contactName),
                null, message, null, null);
    }

    void getContacts() {
        contacts = new HashMap<>();
        //Cursor contacts = callback.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        try ( //advanced Java 7.0+ Project Coin resource management
              Cursor contacts = callback.getContentResolver()
                      .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        ) {
            if (contacts == null) return; //maybe assert contacts != null; ??
            while (contacts.moveToNext()) {
                String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
                String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                this.contacts.put(name.toLowerCase(), phoneNumber);
            }
        } // contacts if going to be closed anyway
    }

}
