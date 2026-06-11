package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.studymate.model.QuizModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * [이슈 #14 해결] 오답 데이터 Firestore wrong_answers 컬렉션 적재 파트
 */
public class QuizResultActivity extends BaseActivity {
    private ArrayList<Integer> userAnswers = new ArrayList<>();
    private ArrayList<QuizModel> quizList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 0);

        ArrayList<Integer> receivedAnswers = getIntent().getIntegerArrayListExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        // QuizActivity가 토스해준 원본 AI 퀴즈 객체 리스트 수신
        ArrayList<QuizModel> receivedQuizList = (ArrayList<QuizModel>) getIntent().getSerializableExtra("quizListSerializable");
        if (receivedQuizList != null) {
            quizList = receivedQuizList;
        }

        int wrongCount = totalCount - correctCount;
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);

        if (scoreCircle != null) scoreCircle.setText(score + "%\n정답률");
        if (summaryText != null) summaryText.setText(totalCount + "문제 중 " + correctCount + "문제 정답");

        if (wrongSummaryBox != null) {
            if (wrongCount == 0) {
                wrongSummaryBox.setText("틀린 문제가 없어요. 모두 정답이에요!");
            } else {
                wrongSummaryBox.setText("틀린 문제가 " + wrongCount + "개 있습니다. 오답노트에서 확인해 보세요.");
            }
        }

        // 유저가 실제 틀린 문제만 선별하여 파이어베이스 Cloud Firestore 서버에 영구 적재
        uploadWrongAnswersToFirestore();

        bindClick(R.id.showWrongButton, v -> {
            if (userAnswers == null || userAnswers.isEmpty()) {
                showShortToast("전달된 유저 답안 데이터가 없습니다.");
                return;
            }
            Intent intent = new Intent(this, WrongAnswerActivity.class);
            intent.putExtra("userAnswers", userAnswers);
            // 오답노트 화면이 더미 텍스트를 파괴하고 진짜 AI가 만든 문제를 그리도록 데이터 넘김
            intent.putExtra("quizListSerializable", quizList);
            startActivity(intent);
        });

        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }

    /**
     * 틀린 문항들을 Firestore wrong_answers 컬렉션에 Document로 밀어 넣는 함수
     */
    private void uploadWrongAnswersToFirestore() {
        if (auth.getCurrentUser() == null || quizList.isEmpty() || userAnswers.isEmpty()) return;

        String currentUserId = auth.getCurrentUser().getUid();

        for (int i = 0; i < quizList.size(); i++) {
            if (i < userAnswers.size()) {
                QuizModel quiz = quizList.get(i);

                if (userAnswers.get(i) != quiz.getAnswerIndex()) {
                    // wrong_answers 정식 명세 컬럼 동기화
                    Map<String, Object> wrongAnswerData = new HashMap<>();
                    wrongAnswerData.put("userId", currentUserId);
                    wrongAnswerData.put("quizId", quiz.getId() != null ? quiz.getId() : "ai_gen_" + i);
                    wrongAnswerData.put("noteId", "note_" + currentUserId); // 오답노트 맵핑 ID 생성
                    wrongAnswerData.put("question", quiz.getQuestion());
                    wrongAnswerData.put("options", quiz.getOptions());
                    wrongAnswerData.put("selectedIndex", userAnswers.get(i)); // 내가 고른 오답 인덱스
                    wrongAnswerData.put("correctIndex", quiz.getAnswerIndex()); // 진짜 정답 인덱스
                    wrongAnswerData.put("explanation", quiz.getExplanation());
                    wrongAnswerData.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("wrong_answers")
                            .add(wrongAnswerData)
                            .addOnSuccessListener(documentReference -> quiz.setId(documentReference.getId()))
                            .addOnFailureListener(Throwable::printStackTrace);
                }
            }
        }
    }
}
