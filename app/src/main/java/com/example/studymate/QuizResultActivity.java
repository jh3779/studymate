package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class QuizResultActivity extends BaseActivity {
    private ArrayList<Integer> userAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 3);

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        int wrongCount = Math.max(0, totalCount - correctCount);
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);
        TextView resultSaveStatusText = findViewById(R.id.resultSaveStatusText);
        Button showWrongButton = findViewById(R.id.showWrongButton);

        if (scoreCircle != null) scoreCircle.setText(score + "%\n정답률");
        if (summaryText != null) summaryText.setText(totalCount + "문제 중 " + correctCount + "문제 정답");

        if (wrongSummaryBox != null) {
            if (wrongCount == 0) {
                wrongSummaryBox.setText("틀린 문제가 없어요. 모두 정답이에요!");
            } else {
                wrongSummaryBox.setText("틀린 문제가 " + wrongCount + "개 있습니다. 오답노트에서 확인해 보세요.");
            }
        }

        if (resultSaveStatusText != null) {
            resultSaveStatusText.setText(wrongCount == 0
                    ? "저장할 오답이 없습니다."
                    : "오답노트에서 틀린 문제를 확인할 수 있습니다.");
        }

        if (showWrongButton != null) {
            showWrongButton.setEnabled(wrongCount > 0);
            showWrongButton.setAlpha(wrongCount > 0 ? 1f : 0.5f);
            showWrongButton.setText(wrongCount > 0 ? "오답 상세 보기" : "오답이 없습니다");
        }

        bindClick(R.id.showWrongButton, v -> {
            if (wrongCount == 0) {
                showShortToast("틀린 문제가 없습니다.");
                return;
            }
            if (userAnswers == null || userAnswers.isEmpty()) {
                showShortToast("전달된 유저 답안 데이터가 없습니다. 퀴즈를 다시 풀어주세요.");
                return;
            }
            Intent intent = new Intent(this, WrongAnswerActivity.class);
            intent.putIntegerArrayListExtra("userAnswers", userAnswers);
            startActivity(intent);
        });

        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }
}
