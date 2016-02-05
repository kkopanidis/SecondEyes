package com.asoee.smartdrive;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Call extends Action {
    String name;
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
        this.name = "";
        String sentence_proc = sentence.toLowerCase();
        String[] tokens = sentence_proc.split("\\s");
        for (int i = 0; i < tokens.length; i++)
            if (tokens[i].equals("call"))
                if (i + 2 < tokens.length && tokens[i + 1].equalsIgnoreCase("to")) {
                    this.name = tokens[i + 2];
                    break;
                } else if (i + 1 < tokens.length) {
                    this.name = tokens[i + 1];
                    break;
                }
        if (!this.name.equals("")) {
            dialog_step += 1;
            dialog(this.name);
        }
        else
            dialog("");
    }

    @Override
    protected boolean dialog(String answer) {
        if (!answer.equals("") && !answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
            switch (dialog_step) {
                case 1:
                    this.name = answer;
                    ((MainWindow) callback).approveAction("You want to call:"
                            + this.name + " is that correct?"
                            , true);
                    return false;
            }
        } else if (answer.equalsIgnoreCase("no")) {
            if (dialog_step == 0)
                return true;
            ((MainWindow) callback).approveAction("Hm i thought i got it right," +
                    " can you repeat the name again?"
                    , true);
            return false;
        }

        dialog_step++;
        switch (dialog_step) {
            case 1:
                ((MainWindow) callback).approveAction("Ok, who would you like to call?"
                        , true);
                return false;
            case 2:
                if (!getContacts()) {
                    dialog_step = 0;
                    ((MainWindow) callback).approveAction("It seems that i could not find the number," +
                            "would you like to give me another name?"
                            , true);
                    return false;
                } else {
                    executeCommand();
                    return true;
                }
            default:
                return false;
        }
    }

    @Override
    public void executeCommand() {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        callback.startActivity(callIntent);
    }

    boolean getContacts() {
        try (
                Cursor contacts = callback.getContentResolver() // it doesn't need a cast, it's an Activity method
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        ) {
            if (contacts == null) return false;
            while (contacts.moveToNext()) {
                String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase();
                String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (name.equalsIgnoreCase(this.name)) {
                    number = "tel:" + phoneNumber;
                    return true;
                }
            }
        }
        return false;
    }
}
