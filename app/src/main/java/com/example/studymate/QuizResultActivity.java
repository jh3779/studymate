package com.example.studymate;

import android.os.Bundle;
import android.widget.TextView;

public class QuizResultActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        int correctCount = getIntent().getIntExtra("correctCount", 2);
        int totalCount = getIntent().getIntExtra("totalCount", 3);
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);

        scoreCircle.setText(score + "%\n정답률");
        summaryText.setText(totalCount + "문제 중 " + correctCount + "문제 정답");
        if (correctCount == totalCount) {
            wrongSummaryBox.setText("틀린 문제가 없어요. 모두 정답이에요!");
        }

        bindClick(R.id.showWrongButton, v -> goTo(WrongAnswerActivity.class));
        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }
}
