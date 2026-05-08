package com.example.studymate;

import android.os.Bundle;

public class MyPageActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        bindClick(R.id.logoutButton, v -> {
            setLoggedIn(false);
            goToAndClear(LoginActivity.class);
        });
        bindClick(R.id.myHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.myWrongTab, v -> goTo(WrongAnswerActivity.class));
    }
}
