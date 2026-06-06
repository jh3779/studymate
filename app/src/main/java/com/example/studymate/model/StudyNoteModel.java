package com.example.studymate.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudyNoteModel {
    private String id;
    private String userId;
    private String title;
    private String subject;
    private String originalText;
    private List<String> summary;
    private List<String> keywords;
    private Date createdAt;

    public StudyNoteModel() {
        summary = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public StudyNoteModel(
            String id,
            String userId,
            String title,
            String subject,
            String originalText,
            List<String> summary,
            List<String> keywords,
            Date createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.subject = subject;
        this.originalText = originalText;
        this.summary = copyStrings(summary);
        this.keywords = copyStrings(keywords);
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", userId);
        values.put("title", title);
        values.put("subject", subject);
        values.put("originalText", originalText);
        values.put("summary", copyStrings(summary));
        values.put("keywords", copyStrings(keywords));
        values.put("createdAt", createdAt);
        return values;
    }

    public static StudyNoteModel fromMap(String documentId, Map<String, Object> values) {
        return new StudyNoteModel(
                documentId,
                stringValue(values.get("userId")),
                stringValue(values.get("title")),
                stringValue(values.get("subject")),
                stringValue(values.get("originalText")),
                stringListValue(values.get("summary")),
                stringListValue(values.get("keywords")),
                dateValue(values.get("createdAt"))
        );
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static List<String> stringListValue(Object value) {
        List<String> strings = new ArrayList<>();
        if (!(value instanceof List<?>)) {
            return strings;
        }
        for (Object item : (List<?>) value) {
            if (item instanceof String) {
                strings.add((String) item);
            }
        }
        return strings;
    }

    private static List<String> copyStrings(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private static Date dateValue(Object value) {
        return value instanceof Date ? (Date) value : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public List<String> getSummary() {
        return summary;
    }

    public void setSummary(List<String> summary) {
        this.summary = copyStrings(summary);
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = copyStrings(keywords);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
