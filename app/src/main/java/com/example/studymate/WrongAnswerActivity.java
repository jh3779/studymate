package com.example.studymate;

import android.os.Bundle;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import java.util.ArrayList;

/**
 *  원본 AI 생성 퀴즈 기반 오답노트 동적 매핑 핸들러
 */
public class WrongAnswerActivity extends BaseActivity {

    // 로컬 하드코딩 배열 완전 파괴 ➡️ 동적 인텐트 유동형 리스트로 전환
    private ArrayList<QuizModel> quizList = new ArrayList<>();
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

        userAnswers = (ArrayList<Integer>) getIntent().getSerializableExtra("userAnswers");

        // 결과창으로부터 배달 완료된 원본 AI 생성형 퀴즈 데이터 세트 수신
        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        // 실데이터 검증을 기반으로 한 런타임 오답 선별 처리
        if (userAnswers != null && !userAnswers.isEmpty() && !quizList.isEmpty()) {
            wrongIndices.clear();
            for (int i = 0; i < quizList.size(); i++) {
                if (i < userAnswers.size() && userAnswers.get(i) != quizList.get(i).getAnswerIndex()) {
                    wrongIndices.add(i);
                }
            }
        }

        if (wrongIndices.isEmpty()) {
            wrongIndices.add(0);
        }

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

    /**
     * AI 데이터 주입형 실시간 렌더링 함수
     */
    private void displayWrongAnswer() {
        if (wrongIndices.isEmpty() || quizList.isEmpty()) return;

        int originalIdx = wrongIndices.get(currentWrongIndex);
        QuizModel currentQuiz = quizList.get(originalIdx);

        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + wrongIndices.size());
        }

        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q" + (originalIdx + 1) + ". " + currentQuiz.getQuestion());
        }

        // [교정완료] scope 이탈 버그 및 구 options 배열 잔재 수정 (178, 179라인 해결)
        if (userAnswers != null && originalIdx < userAnswers.size()) {
            int userSelection = userAnswers.get(originalIdx);
            if (tvMyAnswer != null && userSelection >= 0 && userSelection < currentQuiz.getOptions().size()) {
                tvMyAnswer.setText("✕ 내 답: " + currentQuiz.getOptions().get(userSelection));
            }
        } else {
            if (tvMyAnswer != null) tvMyAnswer.setText("✕ 내 답: 선택 안 함");
        }

        int correctIndex = currentQuiz.getAnswerIndex();
        if (tvRealAnswer != null && correctIndex >= 0 && correctIndex < currentQuiz.getOptions().size()) {
            tvRealAnswer.setText("✓ 정답: " + currentQuiz.getOptions().get(correctIndex));
        }

        if (tvExplanation != null) {
            tvExplanation.setText("💡 해설: " + currentQuiz.getExplanation());
        }
    }
}