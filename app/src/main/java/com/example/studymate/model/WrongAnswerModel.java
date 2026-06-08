package com.example.studymate.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WrongAnswerModel {
    private String id;
    private String userId;
    private String quizId;
    private String noteId;
    private int selectedIndex;
    private int correctIndex;
    private String question;
    private List<String> options;
    private String explanation;
    private Date createdAt;

    public WrongAnswerModel() {
        options = new ArrayList<>();
    }

    public WrongAnswerModel(
            String id,
            String userId,
            String quizId,
            String noteId,
            int selectedIndex,
            int correctIndex,
            String question,
            List<String> options,
            String explanation,
            Date createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.noteId = noteId;
        this.selectedIndex = selectedIndex;
        this.correctIndex = correctIndex;
        this.question = question;
        this.options = copyStrings(options);
        this.explanation = explanation;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", userId);
        values.put("quizId", quizId);
        values.put("noteId", noteId);
        values.put("selectedIndex", selectedIndex);
        values.put("correctIndex", correctIndex);
        values.put("question", question);
        values.put("options", copyStrings(options));
        values.put("explanation", explanation);
        values.put("createdAt", createdAt);
        return values;
    }

    public static WrongAnswerModel fromMap(String documentId, Map<String, Object> values) {
        return new WrongAnswerModel(
                documentId,
                stringValue(values.get("userId")),
                stringValue(values.get("quizId")),
                stringValue(values.get("noteId")),
                intValue(values.get("selectedIndex")),
                intValue(values.get("correctIndex")),
                stringValue(values.get("question")),
                stringListValue(values.get("options")),
                stringValue(values.get("explanation")),
                dateValue(values.get("createdAt"))
        );
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private static int intValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
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

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = copyStrings(options);
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
