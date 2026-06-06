package com.example.studymate.model;

public class UserStatsModel {
    private final int studyNoteCount;
    private final int quizResultCount;
    private final int averageScore;

    public UserStatsModel(int studyNoteCount, int quizResultCount, int averageScore) {
        this.studyNoteCount = studyNoteCount;
        this.quizResultCount = quizResultCount;
        this.averageScore = averageScore;
    }

    public int getStudyNoteCount() {
        return studyNoteCount;
    }

    public int getQuizResultCount() {
        return quizResultCount;
    }

    public int getAverageScore() {
        return averageScore;
    }
}
