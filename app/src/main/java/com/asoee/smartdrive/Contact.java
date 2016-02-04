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
        super(sentence,callback);
    }

    @Override
    protected void analyzeSentence() {
        int index = sentence.indexOf("contact");
        String details = sentence.substring(index + "contact".length()).trim();
        String[] tokens = details.split(" ");
        name = " ";
        number = " ";
        for (String token : tokens) {
            if (token.equals(""))
                continue;
            if (!token.matches("[0-9]+") && !token.contains("-"))
                this.name += token;
            else if(token.matches("[0-9]+")){
                this.number += token;
                break;
            }
            else{
                this.number = token;
            }
        }
        ((MainWindow)callback).approveAction("I will add a new contact with name " + this.name + " ,and number "
                        + this.number + " .Is that correct?", true);
    }

    @Override
    public void executeCommand() {
        this.number = this.number.replaceAll("-","");
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
            Toast.makeText( callback, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
