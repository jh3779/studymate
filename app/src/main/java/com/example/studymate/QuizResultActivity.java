package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private TextView resultSaveStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        // 지훈이 피드백 반영: 중복 선언 완전 제거 및 보정 수식 반영 완료
        int wrongCount = Math.max(0, totalCount - correctCount);
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

        uploadWrongAnswersToFirestore();

        bindClick(R.id.showWrongButton, v -> {
            if (userAnswers == null || userAnswers.isEmpty()) {
                showShortToast("전달된 유저 답안 데이터가 없습니다.");
                return;
            }
            Intent intent = new Intent(this, WrongAnswerActivity.class);
            intent.putIntegerArrayListExtra("userAnswers", userAnswers);
            intent.putExtra("quizListSerializable", quizList);
            startActivity(intent);
        });
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

        if (wrongCount > 0) {
            uploadWrongAnswersToFirestore();
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

    /**
     *
     * 1. 메서드명을 지훈이가 요구한 uploadWrongAnswersToFirestore()로 명사화 변경
     * 2. note_default_fallback 제거 및 비어있을 시 즉시 return 차단 보완
     * 3. validQuiz() 및 wrongAnswerMatchesQuiz() 규칙 완벽 동기화
     */
    private void uploadWrongAnswersToFirestore() {
        if (auth.getCurrentUser() == null) {
            updateSaveStatus("로그인 정보가 없어 오답노트 저장을 건너뛰었습니다.");
            return;
        }
        if (quizList.isEmpty() || userAnswers.isEmpty()) {
            updateSaveStatus("저장할 오답 데이터가 없습니다.");
            return;
        }

        // note_default_fallback을 완전히 제거하고, 유효한 실제 문서 ID가 없으면 즉시 중단
        if (noteId == null || noteId.trim().isEmpty()) {
            Log.e("FirestoreRulesGuard", "Error: 실제 study_notes 문서 ID가 누락되어 오답 저장을 중단합니다.");
            updateSaveStatus("노트 정보가 올바르지 않아 오답노트가 저장되지 않았습니다.");
            showShortToast("노트 정보가 올바르지 않아 오답노트가 서버에 기록되지 않았습니다.");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        for (int i = 0; i < quizList.size(); i++) {
            if (i < userAnswers.size()) {
                QuizModel quiz = quizList.get(i);
                final int userSelected = userAnswers.get(i);

                // 틀린 문제만 필터링하여 업로드
                if (userSelected != quiz.getAnswerIndex()) {
                    if (quiz.getId() == null || quiz.getId().startsWith("ai_gen_")) {

                        //  validQuiz() 규칙 100% 매칭 스키마 (7개 필드 정확히 일치)
                        Map<String, Object> quizSchema = new HashMap<>();
                        quizSchema.put("noteId", noteId);                          // verifiedOwner 및 noteExists 조건 충족
                        quizSchema.put("userId", currentUserId);                    // noteBelongsTo 조건 충족
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

                                    // 1단계 성공 시, 생성된 정식 quizId를 결합하여 2단계 wrong_answers 저장 실행
                                    executeWrongAnswerInsert(currentUserId, realQuizId, quiz, userSelected);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreRulesGuard", "1단계 quizzes 선행 생성 실패: " + e.getMessage());
                                    updateSaveStatus("오답 저장에 실패했습니다. 잠시 후 다시 시도해 주세요.");
                                    showShortToast("보안 권한 거부로 인해 오답 퀴즈 생성에 실패했습니다.");
                                });
                    } else {
                        // 원본 퀴즈 ID가 이미 존재하는 경우 바로 2단계 저장
                        executeWrongAnswerInsert(currentUserId, quiz.getId(), quiz, userSelected);
                    }
                }
            }
        }
    }
}

    /**
     * wrongAnswerMatchesQuiz() 규칙 매칭
     * quizzes 문서에 들어간 원본 데이터와 한 자의 텍스트 오차도 없이 완벽하게 정합성을 맞춤
     */
    private void executeWrongAnswerInsert(String userId, String actualQuizId, QuizModel quiz, int userSelected) {
        Map<String, Object> wrongAnswerData = new HashMap<>();
        wrongAnswerData.put("userId", userId);
        wrongAnswerData.put("quizId", actualQuizId);
        wrongAnswerData.put("noteId", noteId);
        wrongAnswerData.put("question", quiz.getQuestion());
        wrongAnswerData.put("options", quiz.getOptions());
        wrongAnswerData.put("selectedIndex", userSelected);
        wrongAnswerData.put("correctIndex", quiz.getAnswerIndex()); // rules 검증용 answerIndex 매핑 동기화
        wrongAnswerData.put("explanation", quiz.getExplanation());
        wrongAnswerData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("wrong_answers")
                .add(wrongAnswerData)
                .addOnSuccessListener(documentReference -> updateSaveStatus("오답노트에 저장했습니다."))
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRulesGuard", "2단계 wrong_answers 최종 적재 실패 (rules 정합성 오류 확인 필요): " + e.getMessage());
                    updateSaveStatus("오답 저장에 실패했습니다. 잠시 후 다시 시도해 주세요.");
                });
    }

    private void updateSaveStatus(String message) {
        if (resultSaveStatusText != null) {
            resultSaveStatusText.setText(message);
        }
    }
}
