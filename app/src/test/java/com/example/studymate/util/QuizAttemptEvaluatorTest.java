package com.example.studymate.util;

import com.example.studymate.model.QuizModel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuizAttemptEvaluatorTest {
    @Test
    public void countsCorrectAnswersAndFindsWrongQuestions() {
        List<QuizModel> quizzes = quizzesWithAnswers(1, 2, 0);
        List<Integer> selectedAnswers = Arrays.asList(1, 0, 0);

        assertEquals(2, QuizAttemptEvaluator.countCorrect(quizzes, selectedAnswers));
        assertEquals(
                Collections.singletonList(1),
                QuizAttemptEvaluator.wrongQuestionIndices(quizzes, selectedAnswers)
        );
    }

    @Test
    public void ignoresMissingAndNullAnswersWithoutCrashing() {
        List<QuizModel> quizzes = quizzesWithAnswers(0, 1, 2);
        List<Integer> selectedAnswers = Arrays.asList(0, null);

        assertEquals(1, QuizAttemptEvaluator.countCorrect(quizzes, selectedAnswers));
        assertEquals(
                Collections.emptyList(),
                QuizAttemptEvaluator.wrongQuestionIndices(quizzes, selectedAnswers)
        );
    }

    @Test
    public void handlesNullInputAsEmptyAttempt() {
        assertEquals(0, QuizAttemptEvaluator.countCorrect(null, null));
        assertEquals(
                Collections.emptyList(),
                QuizAttemptEvaluator.wrongQuestionIndices(null, null)
        );
    }

    private List<QuizModel> quizzesWithAnswers(int... answerIndices) {
        List<QuizModel> quizzes = new ArrayList<>();
        for (int i = 0; i < answerIndices.length; i++) {
            QuizModel quiz = new QuizModel();
            quiz.setId("quiz-" + i);
            quiz.setQuestion("question-" + i);
            quiz.setOptions(Arrays.asList("A", "B", "C", "D"));
            quiz.setAnswerIndex(answerIndices[i]);
            quiz.setExplanation("explanation-" + i);
            quizzes.add(quiz);
        }
        return quizzes;
    }
}
