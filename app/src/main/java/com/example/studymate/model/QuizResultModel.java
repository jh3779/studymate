package com.example.studymate.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuizResultModel {
    private String id;
    private String userId;
    private String noteId;
    private int totalCount;
    private int correctCount;
    private int score;
    private Date createdAt;

    public QuizResultModel() {
    }

    public QuizResultModel(
            String id,
            String userId,
            String noteId,
            int totalCount,
            int correctCount,
            int score,
            Date createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.noteId = noteId;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.score = score;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", userId);
        values.put("noteId", noteId);
        values.put("totalCount", totalCount);
        values.put("correctCount", correctCount);
        values.put("score", score);
        values.put("createdAt", createdAt);
        return values;
    }

    public static QuizResultModel fromMap(String documentId, Map<String, Object> values) {
        return new QuizResultModel(
                documentId,
                stringValue(values.get("userId")),
                stringValue(values.get("noteId")),
                intValue(values.get("totalCount")),
                intValue(values.get("correctCount")),
                intValue(values.get("score")),
                dateValue(values.get("createdAt"))
        );
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static int intValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
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

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
