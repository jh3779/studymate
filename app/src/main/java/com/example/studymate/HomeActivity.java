package com.example.studymate;

import android.os.Bundle;

public class HomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindClick(R.id.startStudyButton, v -> goTo(StudyInputActivity.class));
        bindClick(R.id.wrongTab, v -> goTo(WrongAnswerActivity.class));
        bindClick(R.id.myPageTab, v -> goTo(MyPageActivity.class));
    }
}
