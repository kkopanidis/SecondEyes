package com.asoee.smartdrive;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import java.util.HashMap;

public class Message extends Action {

    private String message;
    private String contactName;
    private HashMap<String, String> contacts;
    int mode = 0;

    /**
     * Does constructor stuff
     *
     * @param sentence the sentence given
     */
    public Message(String sentence, Activity callback) {
        super(sentence, callback);
        //dialog("");
    }

    @Override
    protected void analyzeSentence() {
        getContacts();
        String sentence = this.sentence.toLowerCase().trim();
        String[] tokens = sentence.split("\\s");
        int token;
        String first = tokens[0].trim();
        String second = " ";
        String third = " ";
        message = "";
        String last = tokens[tokens.length - 1].trim();
        if (tokens.length > 1)
            second = tokens[1].trim();
        if (tokens.length > 2)
            third = tokens[2].trim();

        switch (first) {
            case "message":
                if (second.equals("to") && contacts.containsKey(third)) {
                    contactName = third;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                        dialog_step = 3;
                        dialog("");
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }

                } else if (contacts.containsKey(second)) {
                    contactName = second;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                        dialog_step = 3;
                        dialog("");
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else if (contacts.containsKey(last)) {
                    contactName = last;

                    if (tokens.length > 3) {
                        message = sentence.substring("message".length(), sentence.indexOf(last)).trim();
                        if (message.substring(message.lastIndexOf(' '), message.length()).contains("to")) {
                            message = message.substring(0, message.lastIndexOf(' ')).trim();
                        }
                        dialog_step = 3;
                        dialog("");
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }

                } else {
                    dialog("");
                }
                break;
            case "send":
                if (second.equals("to") && contacts.containsKey(third)) {
                    contactName = third;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                        approveMessage();
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else if (contacts.containsKey(second)) {
                    contactName = second;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                        approveMessage();
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else if (contacts.containsKey(last)) {
                    contactName = last;
                    if (tokens.length > 3) {
                        message = sentence.substring("send".length(), sentence.indexOf(last)).trim();
                        if (message.substring(message.lastIndexOf(' '), message.length()).contains("to")) {
                            message = message.substring(0, message.lastIndexOf(' ')).trim();
                        }
                        approveMessage();
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else {
                    dialog("");
                }
                break;
            case "text":
                if (second.equals("to") && contacts.containsKey(third)) {
                    contactName = third;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(third) + third.length(), sentence.length());
                        approveMessage();
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else if (contacts.containsKey(second)) {
                    contactName = second;
                    if (tokens.length > 3) {
                        message = sentence.substring(sentence.indexOf(second) + second.length(), sentence.length());
                        approveMessage();
                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else if (contacts.containsKey(last)) {
                    contactName = last;
                    if (tokens.length > 3) {
                        message = sentence.substring("text".length(), sentence.indexOf(last)).trim();
                        if (message.substring(message.lastIndexOf(' '), message.length()).contains("to")) {
                            message = message.substring(0, message.lastIndexOf(' ')).trim();
                        }
                        approveMessage();

                    } else {
                        dialog_step++;
                        dialog(contactName);
                    }
                } else {
                    dialog("");
                }
                break;
            default:
                dialog("");
                break;
        }
    }

    @Override
    protected boolean dialog(String answer) {
        if (answer.equals("cancel")) {
            ((MainWindow) callback).approveAction("Cancelled!"
                    , false);
            return true;
        }
        if (!answer.equals("") && !answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
            switch (dialog_step) {
                case 1:
                    if (inputCheck(answer)) {
                        this.contactName = answer;
                        ((MainWindow) callback).approveAction("You want to send a message to:"
                                + this.contactName + " is that correct?"
                                , true);
                        return false;
                    } else {
                        ((MainWindow) callback).approveAction("I think i heard the receiver wrong," +
                                "can you repeat it for me please?"
                                , true);
                    }
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
        } else if (answer.equalsIgnoreCase("yes") && dialog_step == 1 && mode == 0) {
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
        if (mode == 1) {
            SmsManager.getDefault().sendTextMessage("tel:" + contactName,
                    null, message, null, null);
            return;
        }
        SmsManager.getDefault().sendTextMessage(contacts.get(contactName),
                null, message, null, null);
    }

    @Override
    protected boolean inputCheck(String input) {
        boolean pureAlpha = true;
        boolean pureDigit = true;
        for (char c : input.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                pureDigit = false;
                mode = 0;
            } else if (Character.isDigit(c)) {
                pureAlpha = false;
                mode = 1;
            }

        }

        return pureDigit != pureAlpha;
    }

    protected void approveMessage() {
        dialog_step = 2;
        if (!contacts.containsKey(this.contactName)) {
            ((MainWindow) callback).approveAction("Sorry but i could not " +
                    "find that name in your contact list. Would you like to give another one?"
                    , true);
            dialog_step = 0;
            return;
        }
        ((MainWindow) callback).approveAction("You want to text:"
                + this.contactName + ", " + message + " is that correct?"
                , true);
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
