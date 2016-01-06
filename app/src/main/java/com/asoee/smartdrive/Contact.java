package com.asoee.smartdrive;

import android.content.Intent;
import android.provider.ContactsContract;

public class Contact extends Action {
    String name;
    String number;
    public Contact(String sentence){
        super(sentence);
    }

    @Override
    protected void analyzeSentence() {
        int index = sentence.indexOf("contact");
        String details = sentence.substring(index + "contact".length()).trim();
        String[] tokens = details.split(" ");
        for (String token : tokens){
            if (token.equals(""))
                continue;
            if(!token.matches("[0-9]+"))
                this.name+=token;
            else {
                this.number = token;
                break;
            }
        }
        MainWindow.activity
                .approveAction("I will add a new contact with name "+this.name+" ,and number "
                        +this.number+" .Is that correct?",true);
    }

    @Override
    public void executeCommand() {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            // Sets the MIME type to match the Contacts Provider
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, false );
        MainWindow.activity.startActivity(intent);
    }
}
