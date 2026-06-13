package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.example.studymate.model.QuizModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuizActivity extends BaseActivity {
    private static final String STATE_CURRENT_INDEX = "currentIndex";
    private static final String STATE_SELECTED_INDEX = "selectedIndex";
    private static final String STATE_CORRECT_COUNT = "correctCount";
    private static final String STATE_USER_ANSWERS = "userAnswers";
    private static final String STATE_ATTEMPT_ID = "attemptId";

    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private TextView progressText;
    private TextView questionText;
    private RadioButton[] optionViews;
    private Button nextButton;

    private int currentIndex = 0;
    private int selectedIndex = -1;
    private int correctCount = 0;
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private String noteId = "";
    private String attemptId = "";
    private boolean retryMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        progressText = findViewById(R.id.quizProgressText);
        questionText = findViewById(R.id.questionText);
        optionViews = new RadioButton[]{
                findViewById(R.id.optionOne),
                findViewById(R.id.optionTwo),
                findViewById(R.id.optionThree),
                findViewById(R.id.optionFour)
        };
        nextButton = findViewById(R.id.nextQuizButton);

        TextView backNavigation = findViewById(R.id.backSummary);
        bindClick(R.id.backSummary, v -> finish());

        for (int i = 0; i < optionViews.length; i++) {
            final int index = i;
            optionViews[i].setOnClickListener(v -> selectOption(index));
        }
        nextButton.setOnClickListener(v -> moveNext());

        noteId = getIntent().getStringExtra("noteId");
        retryMode = getIntent().getBooleanExtra("retryMode", false);
        if (retryMode) {
            backNavigation.setText("오답");
        }

        ArrayList<QuizModel> receivedQuizList =
                (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        } else {
            String quizzesJson = getIntent().getStringExtra("quizzesJson");
            parseQuizzesJson(quizzesJson);
        }

        if (isValidQuizList()) {
            restoreState(savedInstanceState);
            renderQuestion(false);
        } else {
            showShortToast("퀴즈 데이터를 불러오지 못했습니다.");
            finish();
        }
    }

    private void parseQuizzesJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return;
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String question = obj.optString("question", "질문 로딩 실패");
                JSONArray optsArray = obj.optJSONArray("options");
                List<String> optionsList = new ArrayList<>();
                if (optsArray != null) {
                    for (int j = 0; j < optsArray.length(); j++) {
                        optionsList.add(optsArray.getString(j));
                    }
                }
                Object rawAnswerIndex = obj.opt("answerIndex");
                if (!(rawAnswerIndex instanceof Number)) {
                    continue;
                }
                double numericAnswerIndex = ((Number) rawAnswerIndex).doubleValue();
                if (!Double.isFinite(numericAnswerIndex)
                        || numericAnswerIndex != Math.rint(numericAnswerIndex)) {
                    continue;
                }
                int answerIndex = (int) numericAnswerIndex;
                String explanation = obj.optString("explanation", "해설이 제공되지 않는 문제입니다.");
                String quizId = obj.optString("id", null);

                if (question.trim().isEmpty()
                        || optionsList.size() != 4
                        || answerIndex < 0
                        || answerIndex > 3
                        || explanation.trim().isEmpty()
                        || quizId == null
                        || quizId.trim().isEmpty()) {
                    continue;
                }

                QuizModel quiz = new QuizModel();
                quiz.setId(quizId);
                quiz.setQuestion(question);
                quiz.setOptions(optionsList);
                quiz.setAnswerIndex(answerIndex);
                quiz.setExplanation(explanation);

                quizList.add(quiz);
            }
            if (quizList.size() != 3) {
                quizList.clear();
            }
        } catch (JSONException e) {
            android.util.Log.w("QuizActivity", "퀴즈 JSON 파싱 실패", e);
        }
    }

    private void renderQuestion(boolean announceQuestion) {
        nextButton.setEnabled(selectedIndex >= 0);

        QuizModel currentQuiz = quizList.get(currentIndex);
        nextButton.setText(currentIndex == quizList.size() - 1 ? "결과 보기" : "다음 문제");
        progressText.setText((currentIndex + 1) + "/" + quizList.size());
        progressText.setContentDescription(getString(
                R.string.quiz_progress_accessibility,
                currentIndex + 1,
                quizList.size()
        ));
        questionText.setText(currentQuiz.getQuestion());
        questionText.setContentDescription(getString(
                R.string.quiz_question_accessibility,
                currentIndex + 1,
                currentQuiz.getQuestion()
        ));
        ViewCompat.setAccessibilityHeading(questionText, true);

        for (int i = 0; i < optionViews.length; i++) {
            if (i < currentQuiz.getOptions().size()) {
                optionViews[i].setText(currentQuiz.getOptions().get(i));
                boolean selected = i == selectedIndex;
                optionViews[i].setBackgroundResource(
                        selected ? R.drawable.bg_option_selected : R.drawable.bg_option
                );
                updateOptionAccessibility(i, selected);
            }
        }
        updateNextButtonAccessibility();

        if (announceQuestion) {
            questionText.announceForAccessibility(
                    progressText.getContentDescription() + ". "
                            + questionText.getContentDescription()
            );
        }
    }

    private void selectOption(int index) {
        selectedIndex = index;
        nextButton.setEnabled(true);
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setBackgroundResource(i == index ? R.drawable.bg_option_selected : R.drawable.bg_option);
            updateOptionAccessibility(i, i == index);
        }
        updateNextButtonAccessibility();
    }

    private void updateOptionAccessibility(int index, boolean selected) {
        String optionText = optionViews[index].getText().toString();
        optionViews[index].setSelected(selected);
        optionViews[index].setChecked(selected);
        optionViews[index].setContentDescription(getString(
                R.string.quiz_option_accessibility,
                index + 1,
                optionText
        ));
        ViewCompat.setStateDescription(
                optionViews[index],
                getString(selected
                        ? R.string.quiz_option_selected
                        : R.string.quiz_option_not_selected)
        );
    }

    private void updateNextButtonAccessibility() {
        ViewCompat.setStateDescription(
                nextButton,
                getString(nextButton.isEnabled()
                        ? R.string.quiz_next_enabled
                        : R.string.quiz_next_disabled)
        );
    }

    private boolean isValidQuizList() {
        if (quizList.isEmpty() || (!retryMode && quizList.size() != 3)) {
            return false;
        }
        for (QuizModel quiz : quizList) {
            if (quiz == null
                    || quiz.getId() == null
                    || quiz.getId().trim().isEmpty()
                    || quiz.getQuestion() == null
                    || quiz.getQuestion().trim().isEmpty()
                    || quiz.getOptions() == null
                    || quiz.getOptions().size() != 4
                    || quiz.getAnswerIndex() < 0
                    || quiz.getAnswerIndex() > 3
                    || quiz.getExplanation() == null
                    || quiz.getExplanation().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void moveNext() {
        if (selectedIndex < 0) {
            return;
        }
        userAnswers.add(selectedIndex);
        QuizModel currentQuiz = quizList.get(currentIndex);

        if (selectedIndex == currentQuiz.getAnswerIndex()) {
            correctCount++;
        }

        if (currentIndex == quizList.size() - 1) {
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("correctCount", correctCount);
            intent.putExtra("totalCount", quizList.size());
            intent.putExtra("noteId", noteId);
            intent.putExtra("attemptId", attemptId);
            intent.putIntegerArrayListExtra("userAnswers", userAnswers);
            intent.putExtra("quizListSerializable", quizList);
            startActivity(intent);
            finish();
            return;
        }

        currentIndex++;
        selectedIndex = -1;
        renderQuestion(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_INDEX, currentIndex);
        outState.putInt(STATE_SELECTED_INDEX, selectedIndex);
        outState.putInt(STATE_CORRECT_COUNT, correctCount);
        outState.putIntegerArrayList(STATE_USER_ANSWERS, userAnswers);
        outState.putString(STATE_ATTEMPT_ID, attemptId);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            attemptId = UUID.randomUUID().toString();
            return;
        }

        currentIndex = savedInstanceState.getInt(STATE_CURRENT_INDEX, 0);
        selectedIndex = savedInstanceState.getInt(STATE_SELECTED_INDEX, -1);
        correctCount = savedInstanceState.getInt(STATE_CORRECT_COUNT, 0);
        ArrayList<Integer> restoredAnswers =
                savedInstanceState.getIntegerArrayList(STATE_USER_ANSWERS);
        if (restoredAnswers != null) {
            userAnswers = restoredAnswers;
        }
        attemptId = savedInstanceState.getString(STATE_ATTEMPT_ID, "");
        if (attemptId == null || attemptId.trim().isEmpty()) {
            attemptId = UUID.randomUUID().toString();
        }
        if (currentIndex < 0 || currentIndex >= quizList.size()) {
            currentIndex = 0;
            selectedIndex = -1;
            correctCount = 0;
            userAnswers.clear();
        }
    }
}
