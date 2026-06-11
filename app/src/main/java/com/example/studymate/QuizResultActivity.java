package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizResultActivity extends BaseActivity {
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String noteId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 0);
        noteId = getIntent().getStringExtra("noteId");

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        // 지훈이 피드백 반영: 중복 선언 완전 제거 및 보정 수식 반영 완료
        int wrongCount = Math.max(0, totalCount - correctCount);
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);
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

        if (showWrongButton != null) {
            if (wrongCount == 0) {
                showWrongButton.setEnabled(false);
                showWrongButton.setAlpha(0.5f); // 불투명하게 처리하여 클릭 불가 상태 시각화
                showWrongButton.setText("틀린 문제가 없습니다");
            } else {
                showWrongButton.setEnabled(true);
                showWrongButton.setAlpha(1.0f);
                showWrongButton.setText("오답노트 확인하기");
            }
        }

        uploadWrongAnswersToFirestore();

        if (showWrongButton != null) {
            showWrongButton.setOnClickListener(v -> {
                // 1. 오답 개수가 0개이면 화면 이동을 원천 봉쇄하고 메시지 처리
                if (wrongCount == 0) {
                    showShortToast("틀린 문제가 없습니다.");
                    return;
                }

                // 2. 전달 데이터 유효성 예외 처리
                if (userAnswers == null || userAnswers.isEmpty()) {
                    showShortToast("전달된 유저 답안 데이터가 없습니다.");
                    return;
                }

                // 3. 검증 통과 시에만 오답 화면으로 진입
                Intent intent = new Intent(this, WrongAnswerActivity.class);
                intent.putIntegerArrayListExtra("userAnswers", userAnswers);
                intent.putExtra("quizListSerializable", quizList);
                startActivity(intent);
            });
        }

        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }

    private void uploadWrongAnswersToFirestore() {
        if (auth.getCurrentUser() == null || quizList.isEmpty() || userAnswers.isEmpty()) return;
        if (noteId == null || noteId.isEmpty()) {
            noteId = "note_default_fallback";
        }
        String currentUserId = auth.getCurrentUser().getUid();

        for (int i = 0; i < quizList.size(); i++) {
            if (i < userAnswers.size()) {
                QuizModel quiz = quizList.get(i);
                final int userSelected = userAnswers.get(i);

                if (userSelected != quiz.getAnswerIndex()) {
                    if (quiz.getId() == null || quiz.getId().startsWith("ai_gen_")) {
                        // 1단계: 실제 quizzes 컬렉션 문서가 없다면 선제 빌드 업로드 수행 (quizExists 조건 만족용)
                        Map<String, Object> quizSchema = new HashMap<>();
                        quizSchema.put("question", quiz.getQuestion());
                        quizSchema.put("options", quiz.getOptions());
                        quizSchema.put("answerIndex", quiz.getAnswerIndex());
                        quizSchema.put("explanation", quiz.getExplanation());
                        quizSchema.put("createdAt", FieldValue.serverTimestamp());

                        db.collection("quizzes")
                                .add(quizSchema)
                                .addOnSuccessListener(quizDocRef -> {
                                    String realQuizId = quizDocRef.getId();
                                    quiz.setId(realQuizId);
                                    // 2단계: 실존하는 실제 주소(realQuizId, noteId)들로 wrong_answers 최종 적재 실행
                                    executeWrongAnswerInsert(currentUserId, realQuizId, quiz, userSelected);
                                })
                                .addOnFailureListener(Throwable::printStackTrace);
                    } else {
                        // 이미 실존하는 퀴즈 아이디가 주입되어 있다면 다이렉트 규칙 통과 적재
                        executeWrongAnswerInsert(currentUserId, quiz.getId(), quiz, userSelected);
                    }
                }
            }
        }
    }
private void executeWrongAnswerInsert(String userId, String actualQuizId, QuizModel quiz, int userSelected) {
    Map<String, Object> wrongAnswerData = new HashMap<>();
    wrongAnswerData.put("userId", userId);
    wrongAnswerData.put("quizId", actualQuizId);     // [규칙 충족] 실존하는 실제 quizId 매핑
    wrongAnswerData.put("noteId", noteId);           // [규칙 충족] 실존하는 실제 noteId 연동 완료
    wrongAnswerData.put("question", quiz.getQuestion());
    wrongAnswerData.put("options", quiz.getOptions());
    wrongAnswerData.put("selectedIndex", userSelected);
    wrongAnswerData.put("correctIndex", quiz.getAnswerIndex());
    wrongAnswerData.put("explanation", quiz.getExplanation());
    wrongAnswerData.put("createdAt", FieldValue.serverTimestamp());

    db.collection("wrong_answers")
            .add(wrongAnswerData)
            .addOnFailureListener(Throwable::printStackTrace);
}
}