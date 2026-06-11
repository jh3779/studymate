package com.example.studymate.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
public class QuizModel implements Serializable {
    private static final long serialVersionUID = 1L; // 직렬화 버전 ID 안정성 확보
    private String id;
    private String noteId;
    private String userId;
    private String question;
    private List<String> options;
    private int answerIndex;
    private String explanation;
    private Date createdAt;
    private int userSelectedIndex = -1;

    public QuizModel() {
        options = new ArrayList<>();
    }

    public QuizModel(
            String id,
            String noteId,
            String userId,
            String question,
            List<String> options,
            int answerIndex,
            String explanation,
            Date createdAt
    ) {
        this.id = id;
        this.noteId = noteId;
        this.userId = userId;
        this.question = question;
        this.options = copyStrings(options);
        this.answerIndex = answerIndex;
        this.explanation = explanation;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("noteId", noteId);
        values.put("userId", userId);
        values.put("question", question);
        values.put("options", copyStrings(options));
        values.put("answerIndex", answerIndex);
        values.put("explanation", explanation);
        values.put("createdAt", createdAt);
        return values;
    }

    public static QuizModel fromMap(String documentId, Map<String, Object> values) {
        return new QuizModel(
                documentId,
                stringValue(values.get("noteId")),
                stringValue(values.get("userId")),
                stringValue(values.get("question")),
                stringListValue(values.get("options")),
                intValue(values.get("answerIndex")),
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

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public int getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(int answerIndex) {
        this.answerIndex = answerIndex;
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

    public int getUserSelectedIndex() {
        return userSelectedIndex;
    }

    public void setUserSelectedIndex(int userSelectedIndex) {
        this.userSelectedIndex = userSelectedIndex;
    }

    public boolean isCorrect() {
        return userSelectedIndex == answerIndex;
    }
}
