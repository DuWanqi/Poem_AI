package com.example.poemai.dto;

import java.util.List;

public class SentenceLengthRequest {
    private List<Integer> lengths;

    public List<Integer> getLengths() {
        return lengths;
    }

    public void setLengths(List<Integer> lengths) {
        this.lengths = lengths;
    }
}