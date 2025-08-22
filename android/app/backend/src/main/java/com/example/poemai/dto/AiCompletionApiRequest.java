package com.example.poemai.dto;

public class AiCompletionApiRequest {
    private String partialText;
    private String userPrompt;

    public String getPartialText() {
        return partialText;
    }

    public void setPartialText(String partialText) {
        this.partialText = partialText;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }
}