package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class QuizActivity extends BaseActivity {
    private final String[] questions = {
            "기본키가 가질 수 없는 값은 무엇인가?",
            "기본키의 주요 역할은 무엇인가?",
            "기본키의 개수 제한은?"
    };
    private final String[][] options = {
            {"① 숫자 값", "② NULL 값", "③ 문자열 값", "④ 날짜 값"},
            {"① 테이블 삭제", "② 행 고유 식별", "③ 색인 제거", "④ 중복 허용"},
            {"① 여러 개 존재 가능", "② 테이블당 하나", "③ 컬럼마다 하나", "④ 없어야 함"}
    };
    private final int[] answers = {1, 1, 1};

    private TextView progressText;
    private TextView questionText;
    private TextView[] optionViews;
    private Button nextButton;
    private int currentIndex = 0;
    private int selectedIndex = -1;
    private int correctCount = 0;

    private ArrayList<Integer> userAnswers = new ArrayList<>();

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

        bindClick(R.id.backSummary, v -> finish());
        for (int i = 0; i < optionViews.length; i++) {
            final int index = i;
            optionViews[i].setOnClickListener(v -> selectOption(index));
        }
        nextButton.setOnClickListener(v -> moveNext());

        renderQuestion();
    }

    private void renderQuestion() {
        selectedIndex = -1;
        nextButton.setEnabled(false);
        nextButton.setText(currentIndex == questions.length - 1 ? "결과 보기" : "다음 문제");
        progressText.setText((currentIndex + 1) + "/" + questions.length);
        questionText.setText(questions[currentIndex]);
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setText(options[currentIndex][i]);
            optionViews[i].setBackgroundResource(R.drawable.bg_option);
            optionViews[i].setContentDescription(options[currentIndex][i]);
        }
    }

    private void selectOption(int index) {
        selectedIndex = index;
        nextButton.setEnabled(true);
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setBackgroundResource(i == index ? R.drawable.bg_option_selected : R.drawable.bg_option);
            optionViews[i].setContentDescription(options[currentIndex][i] + (i == index ? ", 선택됨" : ""));
        }
    }

    private void moveNext() {
        userAnswers.add(selectedIndex);

        if (selectedIndex == answers[currentIndex]) {
            correctCount++;
        }

        if (currentIndex == questions.length - 1) {
            Intent intent = new Intent(this, QuizResultActivity.class);
            intent.putExtra("correctCount", correctCount);
            intent.putExtra("totalCount", questions.length);
            intent.putExtra("userAnswers", userAnswers);
            startActivity(intent);
            return;
        }

        currentIndex++;
        renderQuestion();
    }
}
