package com.example.poemai.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RhymeResponse {
    @SerializedName("rhymeGroup")
    private String rhymeGroup;
    
    private List<String> words;
    private String message;
    private String error;

    public String getRhymeGroup() {
        return rhymeGroup;
    }

    public void setRhymeGroup(String rhymeGroup) {
        this.rhymeGroup = rhymeGroup;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}