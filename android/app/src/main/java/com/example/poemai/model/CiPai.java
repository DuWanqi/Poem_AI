package com.example.poemai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CiPai implements Serializable {
    private Long id;
    private String name;
    
    @SerializedName("exampleText")
    private String exampleText;
    
    @SerializedName("sentence_lengths")
    private String[] sentenceLengths;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExampleText() {
        return exampleText;
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    public String[] getSentenceLengths() {
        return sentenceLengths;
    }

    public void setSentenceLengths(String[] sentenceLengths) {
        this.sentenceLengths = sentenceLengths;
    }
}