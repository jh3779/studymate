package com.example.studymate.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserModel {
    private String id;
    private String email;
    private String nickname;
    private Date createdAt;

    public UserModel() {
    }

    public UserModel(String id, String email, String nickname, Date createdAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("email", email);
        values.put("nickname", nickname);
        values.put("createdAt", createdAt);
        return values;
    }

    public static UserModel fromMap(String documentId, Map<String, Object> values) {
        return new UserModel(
                documentId,
                stringValue(values.get("email")),
                stringValue(values.get("nickname")),
                dateValue(values.get("createdAt"))
        );
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
