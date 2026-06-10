package com.example.studymate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.example.studymate.service.AuthService;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends BaseActivity {
    public static final String EXTRA_EMAIL = "com.example.studymate.extra.EMAIL";

    private TextView emailText;
    private TextView errorText;
    private Button verifyButton;
    private Button resendButton;
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        emailText = findViewById(R.id.verificationEmailText);
        errorText = findViewById(R.id.verificationErrorText);
        verifyButton = findViewById(R.id.verifyEmailButton);
        resendButton = findViewById(R.id.resendVerificationButton);

        String email = authService.getCurrentUserEmail();
        if (email == null || email.isEmpty()) {
            email = getIntent().getStringExtra(EXTRA_EMAIL);
        }
        emailText.setText(email == null || email.isEmpty() ? "이메일 확인 필요" : email);

        bindClick(R.id.backToLoginFromVerification, v -> leaveToLogin());
        verifyButton.setOnClickListener(v -> handleVerify());
        resendButton.setOnClickListener(v -> handleResend());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leaveToLogin();
            }
        });
    }

    public static Intent createIntent(Context context, String email) {
        Intent intent = new Intent(context, EmailVerificationActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    private void leaveToLogin() {
        authService.signOut();
        goToAndClear(LoginActivity.class);
    }

    private void handleVerify() {
        errorText.setVisibility(View.GONE);
        setLoading(true);
        reloadAndFinishIfVerified();
    }

    private void reloadAndFinishIfVerified() {
        authService.reloadCurrentUser(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoading(false);
                if (user != null && user.isEmailVerified()) {
                    authService.signOut();
                    showShortToast("이메일 인증이 완료되었습니다. 로그인해주세요.");
                    goToAndClear(LoginActivity.class);
                    return;
                }
                showError("이메일 인증이 아직 완료되지 않았습니다.");
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError(errorMessage);
            }
        });
    }

    private void handleResend() {
        errorText.setVisibility(View.GONE);
        setResending(true);

        authService.sendEmailVerification(new AuthService.ActionCallback() {
            @Override
            public void onSuccess() {
                setResending(false);
                showShortToast("인증 메일을 다시 보냈습니다.");
            }

            @Override
            public void onFailure(String errorMessage) {
                setResending(false);
                showError(errorMessage);
            }
        });
    }

    private void setLoading(boolean loading) {
        verifyButton.setEnabled(!loading);
        verifyButton.setText(loading ? "확인 중..." : "인증 완료 확인");
        resendButton.setEnabled(!loading);
    }

    private void setResending(boolean resending) {
        resendButton.setEnabled(!resending);
        resendButton.setText(resending ? "발송 중..." : "인증 메일 재전송");
        verifyButton.setEnabled(!resending);
    }

    private void showError(String message) {
        errorText.setText("⚠ " + message);
        errorText.setVisibility(View.VISIBLE);
    }
}
