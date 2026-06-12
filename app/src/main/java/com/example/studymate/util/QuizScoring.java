package com.example.studymate.util;

public final class QuizScoring {
    private QuizScoring() {
    }

    public static int scorePercent(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }
        int safeCorrect = Math.max(0, Math.min(correctCount, totalCount));
        return Math.round((safeCorrect * 100f) / totalCount);
    }

    public static int wrongCount(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }
        return totalCount - Math.max(0, Math.min(correctCount, totalCount));
    }
}
