package com.example.studymate;

import android.os.Bundle;
import android.widget.TextView;

import com.example.studymate.model.UserStatsModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;

public class MyPageActivity extends BaseActivity {
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();
    private TextView studyCountText;
    private TextView quizCountText;
    private TextView averageScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        TextView accountText = findViewById(R.id.accountText);
        studyCountText = findViewById(R.id.myStudyCountText);
        quizCountText = findViewById(R.id.myQuizCountText);
        averageScoreText = findViewById(R.id.myAverageScoreText);
        String email = authService.getCurrentUserEmail();
        accountText.setText((email == null ? "로그인 사용자" : email) + "\n회원");

        bindClick(R.id.logoutButton, v -> {
            authService.signOut();
            goToAndClear(LoginActivity.class);
        });
        bindClick(R.id.myHomeTab, v -> switchTopLevel(HomeActivity.class));
        bindClick(R.id.myWrongTab, v -> switchTopLevel(WrongAnswerActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        String userId = authService.getCurrentUserId();
        if (userId == null) {
            studyCountText.setText("0\n학습 기록");
            quizCountText.setText("0\n퀴즈 풀이");
            averageScoreText.setText("0%\n평균 정답률");
            return;
        }

        firestoreService.getUserStats(userId, new FirestoreService.StatsCallback() {
            @Override
            public void onSuccess(UserStatsModel stats) {
                studyCountText.setText(stats.getStudyNoteCount() + "\n학습 기록");
                quizCountText.setText(stats.getQuizResultCount() + "\n퀴즈 풀이");
                averageScoreText.setText(stats.getAverageScore() + "%\n평균 정답률");
            }

            @Override
            public void onFailure(String errorMessage) {
                showShortToast(errorMessage);
            }
        });
    }
}
