package com.mvm.learnworddaily.model;

public class WordDataModel {
    private String word;
    private String meanings;

    public WordDataModel (String word, String meanings) {
        this.word = word;
        this.meanings = meanings;
    }

    public String getWord() {
        return word;
    }

    public String getMeanings() {
        return meanings;
    }
}
