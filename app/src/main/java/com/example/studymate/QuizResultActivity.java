package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.WrongAnswerModel;
import com.example.studymate.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class QuizResultActivity extends BaseActivity {
    private static final String STATE_OUTCOME_SAVE_STARTED = "outcomeSaveStarted";

    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private FirebaseAuth auth;
    private final FirestoreService firestoreService = new FirestoreService();
    private String noteId = "";
    private TextView resultSaveStatusText;
    private boolean outcomeSaveStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        auth = FirebaseAuth.getInstance();
        if (savedInstanceState != null) {
            outcomeSaveStarted = savedInstanceState.getBoolean(STATE_OUTCOME_SAVE_STARTED, false);
        }

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 0);

        // SummaryResultActivity -> QuizActivity -> 여기로 온 실제 study_notes/{noteId} 문서 ID 수신
        noteId = getIntent().getStringExtra("noteId");

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        int wrongCount = totalCount - correctCount;
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);
        resultSaveStatusText = findViewById(R.id.resultSaveStatusText);
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

        // 오답 상세 버튼 비활성화 및 가드 처리
        if (showWrongButton != null) {
            if (wrongCount == 0) {
                showWrongButton.setEnabled(false);
                showWrongButton.setAlpha(0.5f);
                showWrongButton.setText("틀린 문제가 없습니다");
                updateSaveStatus("저장할 오답이 없습니다.");
            } else {
                showWrongButton.setEnabled(true);
                showWrongButton.setAlpha(1.0f);
                showWrongButton.setText("오답노트 확인하기");
                updateSaveStatus("오답을 오답노트에 저장하는 중입니다.");
            }
        }

        if (!outcomeSaveStarted) {
            outcomeSaveStarted = true;
            saveOutcomeToFirestore(totalCount, correctCount, score, wrongCount);
        }

        if (showWrongButton != null) {
            showWrongButton.setOnClickListener(v -> {
                if (wrongCount == 0) {
                    showShortToast("틀린 문제가 없습니다.");
                    return;
                }
                if (userAnswers == null || userAnswers.isEmpty()) {
                    showShortToast("전달된 유저 답안 데이터가 없습니다.");
                    return;
                }
                Intent intent = new Intent(this, WrongAnswerActivity.class);
                intent.putIntegerArrayListExtra("userAnswers", userAnswers);
                intent.putExtra("quizListSerializable", quizList);
                startActivity(intent);
            });
        }

        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_OUTCOME_SAVE_STARTED, outcomeSaveStarted);
    }

    private void saveOutcomeToFirestore(
            int totalCount,
            int correctCount,
            int score,
            int wrongCount
    ) {
        if (auth.getCurrentUser() == null || noteId == null || noteId.trim().isEmpty()) {
            updateSaveStatus("로그인 또는 학습 기록 정보가 없어 저장을 건너뛰었습니다.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        QuizResultModel result = new QuizResultModel(
                null,
                userId,
                noteId,
                totalCount,
                correctCount,
                score,
                null
        );

        List<WrongAnswerModel> wrongAnswers = buildWrongAnswers(userId);
        firestoreService.saveQuizOutcome(result, wrongAnswers, new FirestoreService.SaveCallback() {
            @Override
            public void onSuccess(String documentId) {
                if (wrongCount == 0) {
                    updateSaveStatus("퀴즈 결과를 저장했습니다.");
                } else {
                    updateSaveStatus("퀴즈 결과와 오답노트를 저장했습니다.");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("FirestoreRulesGuard", "퀴즈 결과/오답 batch 저장 실패: " + errorMessage);
                updateSaveStatus("저장에 실패했습니다. 잠시 후 홈에서 기록을 확인해주세요.");
            }
        });
    }

    private List<WrongAnswerModel> buildWrongAnswers(String userId) {
        List<WrongAnswerModel> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < quizList.size(); i++) {
            if (i < userAnswers.size()) {
                QuizModel quiz = quizList.get(i);
                int userSelected = userAnswers.get(i);
                if (userSelected != quiz.getAnswerIndex()) {
                    if (quiz.getId() == null || quiz.getId().trim().isEmpty()) {
                        Log.e("FirestoreRulesGuard", "quizId 누락으로 오답 저장 제외: index=" + i);
                        continue;
                    }
                    wrongAnswers.add(new WrongAnswerModel(
                            null,
                            userId,
                            quiz.getId(),
                            noteId,
                            userSelected,
                            quiz.getAnswerIndex(),
                            quiz.getQuestion(),
                            quiz.getOptions(),
                            quiz.getExplanation(),
                            null
                    ));
                }
            }
        }
        return wrongAnswers;
    }

    private void updateSaveStatus(String message) {
        if (resultSaveStatusText != null) {
            resultSaveStatusText.setText(message);
        }
    }
}
