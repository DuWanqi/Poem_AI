package com.example.poemai.model;

import com.google.gson.annotations.SerializedName;

public class WorkSaveResponse {
    private int code;
    private String message;
    
    // 实际数据在data字段中
    private Data data;
    
    public static class Data {
        @SerializedName("workType")
        private String workType;
        
        private Long id;
        private String title;
        private String content;
        private String updatedAt;

        public String getWorkType() {
            return workType;
        }

        public void setWorkType(String workType) {
            this.workType = workType;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
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
    
    // 为了兼容现有代码，提供直接访问id的方法
    public Long getId() {
        return data != null ? data.getId() : null;
    }
}