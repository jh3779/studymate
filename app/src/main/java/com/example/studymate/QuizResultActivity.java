package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;

public class QuizResultActivity extends BaseActivity {
    // 유저가 고른 실제 답안지 인덱스 리스트를 담을 그릇
    private ArrayList<Integer> userAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // 1. QuizActivity가 넘겨준 데이터 수신 (기본값 오차 원천 차단하기 위해 0으로 수정)
        int correctCount = getIntent().getIntExtra("correctCount", 0);
        int totalCount = getIntent().getIntExtra("totalCount", 3);

        // 인텐트로부터 유저 실제 답안지를 꺼내 안전하게 할당합니다.
        ArrayList<Integer> receivedAnswers = (ArrayList<Integer>) getIntent().getSerializableExtra("userAnswers");
        if (receivedAnswers != null) {
            userAnswers = receivedAnswers;
        }

        // [안전장치] 만약 유저 답안지가 비어서 넘어온 시연 상황이라면 강제로 3개 다 틀린 더미 답안지 생성
        if (userAnswers == null || userAnswers.isEmpty()) {
            userAnswers = new ArrayList<>();
            userAnswers.add(0); // 1번 틀림
            userAnswers.add(0); // 2번 틀림
            userAnswers.add(0); // 3번 틀림
        }

        // 2. 오답 개수 실시간 동적 계산 (3개 틀리면 정확히 3개라고 뜸)
        int wrongCount = totalCount - correctCount;
        int score = totalCount == 0 ? 0 : Math.round((correctCount * 100f) / totalCount);

        // 3. UI 컴포넌트 텍스트 바인딩
        TextView scoreCircle = findViewById(R.id.scoreCircle);
        TextView summaryText = findViewById(R.id.resultSummaryText);
        TextView wrongSummaryBox = findViewById(R.id.wrongSummaryBox);

        if (scoreCircle != null) scoreCircle.setText(score + "%\n정답률");
        if (summaryText != null) summaryText.setText(totalCount + "문제 중 " + correctCount + "문제 정답");

        if (wrongSummaryBox != null) {
            if (wrongCount == 0) {
                wrongSummaryBox.setText("틀린 문제가 없어요. 모두 정답이에요!");
            } else {
                // 하드코딩 흔적을 지우고 진짜 계산된 wrongCount를 실시간 매핑합니다.
                wrongSummaryBox.setText("틀린 문제가 " + wrongCount + "개 있습니다. 오답노트에서 확인해 보세요.");
            }
        }

        // 4. ★ 오답 상세보기 버튼 먹통 해결 구역
        // 클릭 시 유저의 답안지(userAnswers)를 인텐트에 꽉 묶어서 WrongAnswerActivity로 정상 토스합니다!
        bindClick(R.id.showWrongButton, v -> {
            Intent intent = new Intent(this, WrongAnswerActivity.class);
            intent.putExtra("userAnswers", userAnswers);
            startActivity(intent);
        });

        // 홈으로 가기 버튼 바인딩
        bindClick(R.id.resultHomeButton, v -> goToAndClear(HomeActivity.class));
    }
}