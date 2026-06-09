package com.example.studymate;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_answer);

        tvWrongProgress = findViewById(R.id.quizProgressText);
        tvWrongQuestion = findViewById(R.id.questionText);
        tvMyAnswer      = findViewById(R.id.optionOne);
        tvRealAnswer    = findViewById(R.id.optionTwo);
        tvExplanation   = findViewById(R.id.optionThree);

        // 인텐트로 전달받은 실제 유저 답안 데이터를 진짜로 읽어옵니다.
        userAnswers = (ArrayList<Integer>) getIntent().getSerializableExtra("userAnswers");

        // 실제 유저 답안지가 넘어온 경우에만 틀린 문제 필터링 알고리즘 작동
        if (userAnswers != null && !userAnswers.isEmpty()) {
            wrongIndices.clear();
            for (int i = 0; i < answers.length; i++) {
                if (i < userAnswers.size() && userAnswers.get(i) != answers[i]) {
                    wrongIndices.add(i); // 틀린 문제 원본 인덱스 기록
                }
            }
        }

        // 만약 데이터가 없거나 전부 맞았다면 예외 방지용 기본 가이드 설정
        if (wrongIndices.isEmpty()) {
            wrongIndices.add(0);
        }

        // 화면에 실제 데이터 바인딩해서 텍스트 그려주기
        displayWrongAnswer();

        bindClick(R.id.backResult, v -> finish());
        bindClick(R.id.wrongHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.wrongMyPageTab, v -> goTo(MyPageActivity.class));

        if (findViewById(R.id.retryQuizButton) != null) {
            findViewById(R.id.retryQuizButton).setOnClickListener(v -> {
                if (currentWrongIndex < wrongIndices.size() - 1) {
                    currentWrongIndex++;
                    displayWrongAnswer();
                } else {
                    showShortToast("모든 오답 확인 완료! 퀴즈 화면으로 돌아갑니다.");
                    goTo(QuizActivity.class);
                    finish();
                }
            });
        }
    }

    private void displayWrongAnswer() {
        if (wrongIndices.isEmpty()) return;

        int originalIdx = wrongIndices.get(currentWrongIndex);

        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + wrongIndices.size());
        }
        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q" + (originalIdx + 1) + ". " + questions[originalIdx]);
        }

        // 실제 유저가 고른 보기 데이터 렌더링
        if (userAnswers != null && originalIdx < userAnswers.size()) {
            int userSelection = userAnswers.get(originalIdx);
            if (tvMyAnswer != null && userSelection >= 0 && userSelection < options[originalIdx].length) {
                tvMyAnswer.setText("❌ 내가 고른 답: " + options[originalIdx][userSelection]);
            }
        } else {
            if (tvMyAnswer != null) tvMyAnswer.setText("❌ 내가 고른 답: 없음");
        }

        int correctSelection = answers[originalIdx];
        if (tvRealAnswer != null) {
            tvRealAnswer.setText("⭕ 실제 정답: " + options[originalIdx][correctSelection]);
        }
        if (tvExplanation != null) {
            tvExplanation.setText("💡 해설: " + explanations[originalIdx]);
        }
    }
}