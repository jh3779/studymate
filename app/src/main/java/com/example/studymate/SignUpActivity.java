package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpActivity extends BaseActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailInput = findViewById(R.id.signupEmailInput);
        passwordInput = findViewById(R.id.signupPasswordInput);
        confirmInput = findViewById(R.id.signupConfirmInput);
        errorText = findViewById(R.id.signupErrorText);

        bindClick(R.id.backToLogin, v -> finish());
        bindClick(R.id.signupButton, v -> handleSignUp());
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

        // TODO: 최백도 담당 AuthService 회원가입 연동 시 이 더미 처리를 교체한다.
        showShortToast("회원가입이 완료되었습니다. 로그인해주세요.");
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
