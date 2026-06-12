package com.example.studymate.util;

import com.example.studymate.model.QuizModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuizAttemptEvaluator {
    private QuizAttemptEvaluator() {
    }

    public static int countCorrect(
            List<QuizModel> quizzes,
            List<Integer> selectedAnswers
    ) {
        int correctCount = 0;
        for (int index : answeredQuestionIndices(quizzes, selectedAnswers)) {
            if (selectedAnswers.get(index) == quizzes.get(index).getAnswerIndex()) {
                correctCount++;
            }
        }
        return correctCount;
    }

    public static List<Integer> wrongQuestionIndices(
            List<QuizModel> quizzes,
            List<Integer> selectedAnswers
    ) {
        List<Integer> wrongIndices = new ArrayList<>();
        for (int index : answeredQuestionIndices(quizzes, selectedAnswers)) {
            if (selectedAnswers.get(index) != quizzes.get(index).getAnswerIndex()) {
                wrongIndices.add(index);
            }
        }
        return wrongIndices;
    }

    private static List<Integer> answeredQuestionIndices(
            List<QuizModel> quizzes,
            List<Integer> selectedAnswers
    ) {
        if (quizzes == null || selectedAnswers == null) {
            return Collections.emptyList();
        }

        List<Integer> indices = new ArrayList<>();
        int answerCount = Math.min(quizzes.size(), selectedAnswers.size());
        for (int i = 0; i < answerCount; i++) {
            QuizModel quiz = quizzes.get(i);
            Integer selectedAnswer = selectedAnswers.get(i);
            if (quiz == null || selectedAnswer == null) {
                continue;
            }
            indices.add(i);
        }
        return indices;
    }
}
