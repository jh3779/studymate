package com.example.studymate;

import android.os.Bundle;

public class WrongAnswerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_answer);

        bindClick(R.id.backResult, v -> finish());
        bindClick(R.id.retryQuizButton, v -> goTo(QuizActivity.class));
        bindClick(R.id.wrongHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.wrongMyPageTab, v -> goTo(MyPageActivity.class));
    }
}
