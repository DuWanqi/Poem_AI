package com.example.poemai.dto;

import com.example.poemai.model.MyWork;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class MyWorkResponse {
    private Long id;
    private String title;
    private String content;
    private String workType;
    private Map<String, Object> fontSetting;
    private Map<String, Object> backgroundInfo;
    private Map<String, Object> templateHighlightIndex;
    private Long associatedCipaiId;
    private String createdAt;
    private String updatedAt;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MyWorkResponse(MyWork work) {
        this.id = work.getId();
        this.title = work.getTitle();
        this.content = work.getContent();
        this.workType = work.getWorkType();
        this.fontSetting = jsonNodeToMap(work.getFontSetting());
        this.backgroundInfo = jsonNodeToMap(work.getBackgroundInfo());
        this.templateHighlightIndex = jsonNodeToMap(work.getTemplateHighlightIndex());
        this.associatedCipaiId = work.getAssociatedCipaiId();
        this.createdAt = work.getCreatedAt().toString();
        this.updatedAt = work.getUpdatedAt().toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return Collections.emptyMap();
        }
        
        try {
            return objectMapper.convertValue(jsonNode, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
    
    // Getter方法
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getWorkType() {
        return workType;
    }

    public Map<String, Object> getFontSetting() {
        return fontSetting;
    }

    public Map<String, Object> getBackgroundInfo() {
        return backgroundInfo;
    }

    public Map<String, Object> getTemplateHighlightIndex() {
        return templateHighlightIndex;
    }

    public Long getAssociatedCipaiId() {
        return associatedCipaiId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}