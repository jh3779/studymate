package com.example.studymate;

import android.os.Bundle;

public class SummaryResultActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_result);

        bindClick(R.id.backInput, v -> finish());
        bindClick(R.id.createQuizButton, v -> goTo(QuizActivity.class));
    }
}
