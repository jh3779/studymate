package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.studymate.model.QuizModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizActivity extends BaseActivity {
    private final List<QuizModel> quizzes = new ArrayList<>();

    private TextView progressText;
    private TextView questionText;
    private TextView[] optionViews;
    private Button nextButton;
    private int currentIndex = 0;
    private int selectedIndex = -1;
    private int correctCount = 0;
    private int[] selectedAnswers;
    private String quizzesJson;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        progressText = findViewById(R.id.quizProgressText);
        questionText = findViewById(R.id.questionText);
        optionViews = new TextView[]{
                findViewById(R.id.optionOne),
                findViewById(R.id.optionTwo),
                findViewById(R.id.optionThree),
                findViewById(R.id.optionFour)
        };
        nextButton = findViewById(R.id.nextQuizButton);

        noteId = getIntent().getStringExtra("noteId");
        quizzesJson = getIntent().getStringExtra("quizzesJson");
        if (!parseQuizzes(quizzesJson)) {
            showShortToast("퀴즈 데이터를 불러올 수 없습니다.");
            finish();
            return;
        }
        selectedAnswers = new int[quizzes.size()];
        Arrays.fill(selectedAnswers, -1);

        bindClick(R.id.backSummary, v -> finish());
        for (int i = 0; i < optionViews.length; i++) {
            final int index = i;
            optionViews[i].setOnClickListener(v -> selectOption(index));
        }
        nextButton.setOnClickListener(v -> moveNext());

        renderQuestion();
    }

    private void renderQuestion() {
        QuizModel quiz = quizzes.get(currentIndex);
        selectedIndex = -1;
        nextButton.setEnabled(false);
        nextButton.setText(currentIndex == quizzes.size() - 1 ? "결과 보기" : "다음 문제");
        progressText.setText((currentIndex + 1) + "/" + quizzes.size());
        questionText.setText(quiz.getQuestion());
        for (int i = 0; i < optionViews.length; i++) {
            String option = quiz.getOptions().get(i);
            optionViews[i].setText(option);
            optionViews[i].setBackgroundResource(R.drawable.bg_option);
            optionViews[i].setContentDescription(option);
        }
    }

    private void selectOption(int index) {
        QuizModel quiz = quizzes.get(currentIndex);
        selectedIndex = index;
        nextButton.setEnabled(true);
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setBackgroundResource(i == index ? R.drawable.bg_option_selected : R.drawable.bg_option);
            optionViews[i].setContentDescription(
                    quiz.getOptions().get(i) + (i == index ? ", 선택됨" : "")
            );
        }
    }

    private void moveNext() {
        QuizModel quiz = quizzes.get(currentIndex);
        selectedAnswers[currentIndex] = selectedIndex;
        if (selectedIndex == quiz.getAnswerIndex()) {
            correctCount++;
        }

        if (currentIndex == quizzes.size() - 1) {
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("noteId", noteId);
            intent.putExtra("quizzesJson", quizzesJson);
            intent.putExtra("selectedAnswers", selectedAnswers);
            intent.putExtra("correctCount", correctCount);
            intent.putExtra("totalCount", quizzes.size());
            startActivity(intent);
            return;
        }

        currentIndex++;
        renderQuestion();
    }

    private boolean parseQuizzes(String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) {
            return false;
        }

        try {
            JSONArray array = new JSONArray(rawJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                JSONArray optionsJson = object.getJSONArray("options");
                if (optionsJson.length() != 4) {
                    return false;
                }

                List<String> options = new ArrayList<>();
                for (int optionIndex = 0; optionIndex < optionsJson.length(); optionIndex++) {
                    options.add(optionsJson.getString(optionIndex));
                }

                QuizModel quiz = new QuizModel(
                        object.optString("id"),
                        object.optString("noteId"),
                        object.optString("userId"),
                        object.getString("question"),
                        options,
                        object.getInt("answerIndex"),
                        object.getString("explanation"),
                        null
                );
                if (quiz.getQuestion().isEmpty()
                        || quiz.getAnswerIndex() < 0
                        || quiz.getAnswerIndex() > 3) {
                    return false;
                }
                quizzes.add(quiz);
            }
            return !quizzes.isEmpty();
        } catch (Exception error) {
            quizzes.clear();
            return false;
        }
    }
}
