package com.example.poemai.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RhymeResponse {
    private int code;
    private String message;
    
    // 实际数据在data字段中
    private Data data;
    
    public static class Data {
        @SerializedName("rhymeGroup")
        private String rhymeGroup;
        private List<String> words;

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
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    
    // 为了兼容现有代码，提供直接访问words和rhymeGroup的方法
    public List<String> getWords() {
        return data != null ? data.getWords() : null;
    }
    
    public String getRhymeGroup() {
        return data != null ? data.getRhymeGroup() : null;
    }
}