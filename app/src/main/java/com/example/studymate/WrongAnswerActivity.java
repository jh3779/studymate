package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class WrongAnswerActivity extends BaseActivity {
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
    private final String[] explanations = {
            "기본키는 테이블 내에서 중복될 수 없으며, NULL 값을 가질 수 없습니다.",
            "기본키는 각 행(레코드)을 고유하게 식별하기 위해 사용됩니다.",
            "관계형 데이터베이스에서 하나의 테이블에는 오직 하나의 기본키만 설정할 수 있습니다."
    };

    private ArrayList<Integer> wrongIndices = new ArrayList<>();
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private int currentWrongIndex = 0;

    private TextView tvWrongProgress;
    private TextView tvWrongQuestion;
    private TextView tvMyAnswer;
    private TextView tvRealAnswer;
    private TextView tvExplanation;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_answer);

        tvWrongProgress = findViewById(R.id.quizProgressText);
        tvWrongQuestion = findViewById(R.id.questionText);
        tvMyAnswer      = findViewById(R.id.optionOne);
        tvRealAnswer    = findViewById(R.id.optionTwo);
        tvExplanation   = findViewById(R.id.optionThree);
        actionButton    = findViewById(R.id.retryQuizButton);

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        userAnswers = receivedAnswers == null ? new ArrayList<>() : receivedAnswers;

        collectWrongAnswers();
        if (wrongIndices.isEmpty()) {
            displayEmptyState();
        } else {
            displayWrongAnswer();
        }

        bindClick(R.id.backResult, v -> finish());
        bindClick(R.id.wrongHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.wrongMyPageTab, v -> goTo(MyPageActivity.class));

        if (actionButton != null) {
            actionButton.setOnClickListener(v -> {
                if (wrongIndices.isEmpty()) {
                    goTo(QuizActivity.class);
                    finish();
                    return;
                }

                if (currentWrongIndex < wrongIndices.size() - 1) {
                    currentWrongIndex++;
                    displayWrongAnswer();
                    return;
                }

                showShortToast("모든 오답 확인 완료! 퀴즈 화면으로 돌아갑니다.");
                goTo(QuizActivity.class);
                finish();
            });
        }
    }

    private void collectWrongAnswers() {
        wrongIndices.clear();
        if (userAnswers.isEmpty()) {
            return;
        }

        for (int i = 0; i < answers.length && i < userAnswers.size(); i++) {
            if (userAnswers.get(i) != answers[i]) {
                wrongIndices.add(i);
            }
        }
    }

    private void displayEmptyState() {
        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 없음");
        }
        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText(userAnswers.isEmpty()
                    ? "저장된 오답 데이터가 없습니다."
                    : "모든 문제를 맞혔습니다.");
        }
        if (tvMyAnswer != null) {
            tvMyAnswer.setText(userAnswers.isEmpty()
                    ? "퀴즈를 풀면 틀린 문제만 이곳에 표시됩니다."
                    : "확인할 오답이 없습니다.");
        }
        if (tvRealAnswer != null) {
            tvRealAnswer.setVisibility(View.GONE);
        }
        if (tvExplanation != null) {
            tvExplanation.setVisibility(View.GONE);
        }
        if (actionButton != null) {
            actionButton.setText("퀴즈 풀러 가기");
        }
    }

    private void displayWrongAnswer() {
        if (wrongIndices.isEmpty()) return;

        int originalIdx = wrongIndices.get(currentWrongIndex);

        if (tvRealAnswer != null) {
            tvRealAnswer.setVisibility(View.VISIBLE);
        }
        if (tvExplanation != null) {
            tvExplanation.setVisibility(View.VISIBLE);
        }

        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + wrongIndices.size());
        }
        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q" + (originalIdx + 1) + ". " + questions[originalIdx]);
        }

        if (userAnswers != null && originalIdx < userAnswers.size()) {
            int userSelection = userAnswers.get(originalIdx);
            if (tvMyAnswer != null) {
                if (userSelection >= 0 && userSelection < options[originalIdx].length) {
                    tvMyAnswer.setText("✕ 내가 고른 답: " + options[originalIdx][userSelection]);
                } else {
                    tvMyAnswer.setText("✕ 내가 고른 답: 선택 정보 오류");
                }
            }
        } else {
            if (tvMyAnswer != null) tvMyAnswer.setText("✕ 내가 고른 답: 없음");
        }

        int correctSelection = answers[originalIdx];
        if (tvRealAnswer != null) {
            tvRealAnswer.setText("✓ 실제 정답: " + options[originalIdx][correctSelection]);
        }
        if (tvExplanation != null) {
            tvExplanation.setText("해설: " + explanations[originalIdx]);
        }
        if (actionButton != null) {
            actionButton.setText(currentWrongIndex < wrongIndices.size() - 1 ? "다음 오답 보기" : "퀴즈 다시 풀기");
        }
    }
}
