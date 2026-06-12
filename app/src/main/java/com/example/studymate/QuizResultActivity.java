package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.example.studymate.model.QuizModel;
import com.example.studymate.model.QuizResultModel;
import com.example.studymate.model.WrongAnswerModel;
import com.example.studymate.service.FirestoreService;
import com.example.studymate.util.QuizAttemptEvaluator;
import com.example.studymate.util.QuizScoring;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuizResultActivity extends BaseActivity {
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private FirebaseAuth auth;
    private final FirestoreService firestoreService = new FirestoreService();
    private String noteId = "";
    private String attemptId = "";
    private TextView resultSaveStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        auth = FirebaseAuth.getInstance();

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 0);

        // SummaryResultActivity -> QuizActivity -> 여기로 온 실제 study_notes/{noteId} 문서 ID 수신
        noteId = getIntent().getStringExtra("noteId");
        attemptId = getIntent().getStringExtra("attemptId");
        if (attemptId == null || attemptId.trim().isEmpty()) {
            attemptId = UUID.randomUUID().toString();
            getIntent().putExtra("attemptId", attemptId);
        }

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        if (!quizList.isEmpty()) {
            totalCount = quizList.size();
            correctCount = countCorrectAnswers();
        }

        int wrongCount = QuizScoring.wrongCount(correctCount, totalCount);
        int score = QuizScoring.scorePercent(correctCount, totalCount);

        TextView scorePercentText = findViewById(R.id.scorePercentText);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);
        resultSaveStatusText = findViewById(R.id.resultSaveStatusText);
        Button showWrongButton = findViewById(R.id.showWrongButton);

        if (scorePercentText != null) scorePercentText.setText(score + "%");
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

        QuizResultViewModel viewModel =
                new ViewModelProvider(this).get(QuizResultViewModel.class);
        viewModel.getSaveStatus().observe(this, this::updateSaveStatus);
        saveOutcomeToFirestore(viewModel, totalCount, correctCount, score, wrongCount);

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
                intent.putExtra("noteId", noteId);
                intent.putIntegerArrayListExtra("userAnswers", userAnswers);
                intent.putExtra("quizListSerializable", quizList);
                startActivity(intent);
            });
        }

        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }

    private int countCorrectAnswers() {
        return QuizAttemptEvaluator.countCorrect(quizList, userAnswers);
    }

    private void saveOutcomeToFirestore(
            QuizResultViewModel viewModel,
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
                attemptId,
                userId,
                noteId,
                totalCount,
                correctCount,
                score,
                null
        );

        List<WrongAnswerModel> wrongAnswers = buildWrongAnswers(userId);
        viewModel.saveOutcome(firestoreService, result, wrongAnswers, wrongCount);
    }

    private List<WrongAnswerModel> buildWrongAnswers(String userId) {
        List<WrongAnswerModel> wrongAnswers = new ArrayList<>();
        for (int index : QuizAttemptEvaluator.wrongQuestionIndices(quizList, userAnswers)) {
            QuizModel quiz = quizList.get(index);
            if (quiz.getId() == null || quiz.getId().trim().isEmpty()) {
                continue;
            }
            wrongAnswers.add(new WrongAnswerModel(
                    null,
                    userId,
                    quiz.getId(),
                    noteId,
                    userAnswers.get(index),
                    quiz.getAnswerIndex(),
                    quiz.getQuestion(),
                    quiz.getOptions(),
                    quiz.getExplanation(),
                    null
            ));
        }
        return wrongAnswers;
    }

    private void updateSaveStatus(String message) {
        if (resultSaveStatusText != null) {
            resultSaveStatusText.setText(message);
        }
    }
}
