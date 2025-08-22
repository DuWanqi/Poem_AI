package com.example.poemai.dto;

import java.util.Map;

public class SaveMyWorkRequest {
    private String title;
    private String content;
    private String workType;
    private Map<String, Object> fontSetting;
    private Map<String, Object> backgroundInfo;
    private Long associatedCipaiId;
    private Map<String, Object> templateHighlightIndex;

    // Getter 和 Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public Map<String, Object> getFontSetting() { return fontSetting; }
    public void setFontSetting(Map<String, Object> fontSetting) { this.fontSetting = fontSetting; }

    public Map<String, Object> getBackgroundInfo() { return backgroundInfo; }
    public void setBackgroundInfo(Map<String, Object> backgroundInfo) { this.backgroundInfo = backgroundInfo; }

    public Long getAssociatedCipaiId() { return associatedCipaiId; }
    public void setAssociatedCipaiId(Long associatedCipaiId) { this.associatedCipaiId = associatedCipaiId; }

    public Map<String, Object> getTemplateHighlightIndex() { return templateHighlightIndex; }
    public void setTemplateHighlightIndex(Map<String, Object> templateHighlightIndex) { this.templateHighlightIndex = templateHighlightIndex; }
}