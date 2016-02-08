package com.asoee.secondeyes;

public class VocalResult {

    private String keyword, sentence;
    private int index;

    private static VocalResult ourInstance;

    public static VocalResult getInstance(String keyword, String sentence,
                                          int index) {
        if (ourInstance == null) {
            ourInstance = new VocalResult(keyword, sentence, index);
        }

        return ourInstance;
    }
    public static void destroy(){
        ourInstance = null;
    }

    private VocalResult(String keyword, String sentence, int index) {
        this.keyword = keyword;
        this.sentence = sentence;
        this.index = index;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getSentence() {
        return sentence;
    }

    public int getIndex() {
        return index;
    }
}
