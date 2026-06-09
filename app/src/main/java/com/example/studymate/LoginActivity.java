package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.service.AuthService;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorText;
    private Button loginButton;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.loginErrorText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> handleLogin());
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

        errorText.setVisibility(View.GONE);
        setLoading(true);

        authService.signIn(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoading(false);
                if (user == null) {
                    showError("사용자 정보를 확인할 수 없습니다.");
                    return;
                }
                if (!user.isEmailVerified()) {
                    showShortToast("이메일 인증을 완료해주세요.");
                    startActivity(EmailVerificationActivity.createIntent(
                            LoginActivity.this,
                            user.getEmail()
                    ));
                    return;
                }
                goToAndClear(HomeActivity.class);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError(errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "로그인 중..." : "로그인");
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }

    private void showError(String message) {
        errorText.setText("⚠ " + message);
        errorText.setVisibility(View.VISIBLE);
    }
}
