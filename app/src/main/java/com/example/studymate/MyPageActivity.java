package com.example.studymate;

import android.os.Bundle;
import android.widget.TextView;

import com.example.studymate.service.AuthService;

public class MyPageActivity extends BaseActivity {
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        TextView accountText = findViewById(R.id.accountText);
        String email = authService.getCurrentUserEmail();
        accountText.setText((email == null ? "로그인 사용자" : email) + "\n회원");

        bindClick(R.id.logoutButton, v -> {
            authService.signOut();
            goToAndClear(LoginActivity.class);
        });
        bindClick(R.id.myHomeTab, v -> goToAndClear(HomeActivity.class));
        bindClick(R.id.myWrongTab, v -> goTo(WrongAnswerActivity.class));
    }
}
