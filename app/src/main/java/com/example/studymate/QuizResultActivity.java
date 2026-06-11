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

        ArrayList<Integer> receivedAnswers = (ArrayList<Integer>) getIntent().getSerializableExtra("userAnswers");
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

                // 실제 채점 결과 오답 판정일 때만 적재 처리 수행
                if (userAnswers.get(i) != quiz.getAnswerIndex()) {
                    quiz.setUserId(currentUserId);
                    quiz.setUserSelectedIndex(userAnswers.get(i));

                    // 원본 모델의 맵 변환 기능을 100% 활용
                    Map<String, Object> uploadMap = quiz.toMap();

                    // 지훈이가 요구한 Firestore 스키마 검증 전용 필드 강제 보완 매핑
                    uploadMap.put("userSelectedIndex", quiz.getUserSelectedIndex());
                    uploadMap.put("isCorrect", quiz.isCorrect());

                    db.collection("wrong_answers")
                            .add(uploadMap)
                            .addOnSuccessListener(documentReference -> {
                                quiz.setId(documentReference.getId());
                            })
                            .addOnFailureListener(Throwable::printStackTrace);
                }
            }
        }
    }
}
