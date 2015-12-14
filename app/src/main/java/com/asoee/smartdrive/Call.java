package com.asoee.smartdrive;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.HashMap;

public class Call extends Action {

    /**
     * Does constructor stuff
     * @param sentence the sentence given
     */
    public Call(String sentence){
        super(sentence);
    }

    @Override
    protected void analyzeSentence() {
        sentence = sentence.toLowerCase();
        String[] tokens = sentence.split("\\s");
        if(tokens[0].equals("call"))
            getContacts(tokens[1]);
    }

    @Override
    public Intent executeCommandIntent() {
        return null;
    }

    void getContacts(String contact) {;
        Cursor contacts = MainWindow.active_context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (contacts.moveToNext()) {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(name.equalsIgnoreCase(contact)){
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                MainWindow.active_context.startActivity(callIntent);
                return;
            }
        }

        contacts.close();
    }
}
