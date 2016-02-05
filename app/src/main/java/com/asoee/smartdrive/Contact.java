package com.asoee.smartdrive;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.util.ArrayList;

public class Contact extends Action {
    String name;
    String number;

    public Contact(String sentence, Activity callback) {
        super(sentence, callback);

    }

    @Override
    protected void analyzeSentence() {
        this.number = "";
        this.name = "";
        int index = sentence.indexOf("contact");
        String details = sentence.substring(index + "contact".length()).trim();
        String[] tokens = details.split(" ");
        for (String token : tokens) {
            if (token.equals(""))
                continue;
            if (!token.matches("[0-9]+"))
                this.name += token;
            else if (token.matches("[0-9]+")) {
                this.number += token;
            } else if (token.contains("-")) {
                this.number = token;
            }
        }
        if (name.equals("") && number.equals(""))
            dialog("");
        else if (!name.equals("")) {
            if (number.equals("")) {
                dialog_step++;
                dialog(name);
            } else {
                dialog_step = 2;
                ((MainWindow) callback).approveAction("I will add a new contact with name "
                        + this.name + " ,and number "
                        + this.number + " .Is that correct?", true);
            }
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
                    ((MainWindow) callback).approveAction("The contact name is:"
                            + this.name + " is that correct?"
                            , true);
                    return false;
                case 2:
                    this.number = answer;
                    ((MainWindow) callback).approveAction("The contact's number is:"
                            + this.number + " is that correct?"
                            , true);
                    return false;
            }
        } else if (answer.equalsIgnoreCase("no")) {
            if (dialog_step == 0)
                return true;
            dialog_step = 0;
            ((MainWindow) callback).approveAction("Oh, it seems i was wrong," +
                    " would you like to try again?"
                    , true);
            return false;
        }

        dialog_step++;
        switch (dialog_step) {
            case 1:
                ((MainWindow) callback).approveAction("Ok, what will the name of the contact be?"
                        , true);
                return false;
            case 2:
                ((MainWindow) callback).approveAction("Great! And what will the number be?"
                        , true);
                return false;
            case 3:
                ((MainWindow) callback).approveAction("OK adding the new contact now!"
                        , false);
                executeCommand();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void executeCommand() {
        this.number = this.number.replaceAll("-", "");
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        this.name).build());


        //------------------------------------------------------ Mobile Number
        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, this.number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // Asking the Contact provider to create a new contact
        try {
            callback.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(callback, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
