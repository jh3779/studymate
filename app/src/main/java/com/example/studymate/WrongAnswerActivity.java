package com.example.studymate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import java.util.ArrayList;

/**
 *  원본 AI 생성 퀴즈 기반 오답노트 동적 매핑 핸들러
 */
public class WrongAnswerActivity extends BaseActivity {

    // 결과 화면에서 전달된 오답 또는 Firestore에 저장된 오답을 동적으로 표시한다.
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private ArrayList<Integer> wrongIndices = new ArrayList<>();
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private int currentWrongIndex = 0;

    private TextView tvWrongProgress;
    private TextView tvWrongQuestion;
    private TextView tvMyAnswer;
    private TextView tvRealAnswer;
    private TextView tvExplanation;
    private Button retryQuizButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_answer);

        tvWrongProgress = findViewById(R.id.quizProgressText);
        tvWrongQuestion = findViewById(R.id.questionText);
        tvMyAnswer      = findViewById(R.id.optionOne);
        tvRealAnswer    = findViewById(R.id.optionTwo);
        tvExplanation   = findViewById(R.id.optionThree);
        retryQuizButton = findViewById(R.id.retryQuizButton);

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

        displayWrongAnswer();

        bindClick(R.id.backResult, v -> finish());
        bindClick(R.id.wrongHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.wrongMyPageTab, v -> goTo(MyPageActivity.class));

        if (retryQuizButton != null) {
            retryQuizButton.setOnClickListener(v -> {
                if (wrongIndices.isEmpty()) {
                    finish();
                    return;
                }
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
        if (wrongIndices.isEmpty() || quizList.isEmpty()) {
            if (tvWrongProgress != null) tvWrongProgress.setText("오답 0/0");
            if (tvWrongQuestion != null) tvWrongQuestion.setText("틀린 문제가 전혀 없습니다! 모든 문제를 마스터하셨습니다. 👍");
            if (tvMyAnswer != null) tvMyAnswer.setText("");
            if (tvRealAnswer != null) tvRealAnswer.setText("");
            if (tvExplanation != null) tvExplanation.setText("");
            if (retryQuizButton != null) retryQuizButton.setText("돌아가기");
            return;
        }

        int originalIdx = wrongIndices.get(currentWrongIndex);
        QuizModel currentQuiz = quizList.get(originalIdx);

        if (tvWrongProgress != null) {
            tvWrongProgress.setText("오답 " + (currentWrongIndex + 1) + "/" + wrongIndices.size());
        }

        if (tvWrongQuestion != null) {
            tvWrongQuestion.setText("Q" + (originalIdx + 1) + ". " + currentQuiz.getQuestion());
        }

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