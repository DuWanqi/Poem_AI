package com.example.poemai.dto;

import java.util.List;

public class CiPaiMatchRequest {
    private List<List<Integer>> lengths;

    public List<List<Integer>> getLengths() {
        return lengths;
    }

    public void setLengths(List<List<Integer>> lengths) {
        this.lengths = lengths;
    }
}