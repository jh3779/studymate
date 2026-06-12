package com.example.studymate.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuizScoringTest {
    @Test
    public void calculatesRoundedPercentage() {
        assertEquals(67, QuizScoring.scorePercent(2, 3));
    }

    @Test
    public void calculatesWrongCount() {
        assertEquals(1, QuizScoring.wrongCount(2, 3));
    }

    @Test
    public void clampsInvalidCounts() {
        assertEquals(100, QuizScoring.scorePercent(5, 3));
        assertEquals(0, QuizScoring.wrongCount(5, 3));
        assertEquals(0, QuizScoring.scorePercent(1, 0));
    }
}
