package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.service.AuthService;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends BaseActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private TextView errorText;
    private Button signupButton;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailInput = findViewById(R.id.signupEmailInput);
        passwordInput = findViewById(R.id.signupPasswordInput);
        confirmInput = findViewById(R.id.signupConfirmInput);
        errorText = findViewById(R.id.signupErrorText);
        signupButton = findViewById(R.id.signupButton);

        bindClick(R.id.backToLogin, v -> finish());
        signupButton.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmInput.getText().toString();

        if (email.isEmpty()) {
            showError("⚠ 이메일을 입력해주세요.");
            return;
        }
        if (password.length() < 6) {
            showError("⚠ 비밀번호는 6자 이상 입력해주세요.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("⚠ 비밀번호가 일치하지 않습니다.");
            return;
        }

        errorText.setVisibility(View.GONE);
        setLoading(true);

        authService.signUp(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoading(false);
                authService.signOut();
                showShortToast("회원가입이 완료되었습니다. 로그인해주세요.");
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ " + errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        signupButton.setEnabled(!loading);
        signupButton.setText(loading ? "가입 처리 중..." : "회원가입");
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        confirmInput.setEnabled(!loading);
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
