package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Call extends Action {

    String number;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Call(String sentence, Activity callback) {
        super(sentence, callback);
    }

    @Override
    protected void analyzeSentence() {
        String sentence_proc = sentence.toLowerCase();
        String[] tokens = sentence_proc.split("\\s");
        if (tokens[0].equals("call"))
            getContacts(tokens[1]);
    }

    @Override
    protected void dialog(int step) {

    }

    @Override
    public void executeCommand() {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        callback.startActivity(callIntent);
    }

    void getContacts(String contact) {
        if (!contact.matches("[a-zA-z]+")) {
            number = "tel:" + contact;
            ((MainWindow)callback).approveAction("I will call: " + contact
                    + " is that correct?", true);
            return;
        }
        Cursor contacts = ((MainWindow)callback).getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (contacts.moveToNext()) {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (name.equalsIgnoreCase(contact)) {
                number = "tel:" + phoneNumber;
                ((MainWindow)callback).approveAction("I will call: " + name + " on: " + phoneNumber
                        + " is that correct?", true);
                return;
            }
        }
        contacts.close();
    }
}
