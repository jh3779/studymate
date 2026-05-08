package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BaseActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.loginErrorText);

        bindClick(R.id.loginButton, v -> handleLogin());
        bindClick(R.id.signupLink, v -> goTo(SignUpActivity.class));
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            errorText.setText("⚠ 이메일과 비밀번호를 입력해주세요.");
            errorText.setVisibility(View.VISIBLE);
            return;
        }

        // TODO: 최백도 담당 AuthService 로그인 연동 시 이 더미 처리를 교체한다.
        setLoggedIn(true);
        goToAndClear(HomeActivity.class);
    }
}
