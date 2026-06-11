package com.example.studymate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.example.studymate.service.AuthService;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends BaseActivity {
    public static final String EXTRA_EMAIL = "com.example.studymate.extra.EMAIL";
    private static final String EXTRA_START_RESEND_COOLDOWN =
            "com.example.studymate.extra.START_RESEND_COOLDOWN";
    private static final String VERIFICATION_PREFERENCES = "email_verification";
    private static final String RESEND_AVAILABLE_AT_PREFIX = "resend_available_at_";
    private static final long RESEND_COOLDOWN_MILLIS = 60_000L;

    private TextView emailText;
    private TextView errorText;
    private Button verifyButton;
    private Button resendButton;
    private CountDownTimer resendCountdown;
    private String resendPreferenceKey;
    private long resendAvailableAtMillis;
    private boolean verificationInProgress;
    private boolean resendInProgress;
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
        initializeResendCooldown(email);

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
        return createIntent(context, email, false);
    }

    public static Intent createIntent(
            Context context,
            String email,
            boolean startResendCooldown
    ) {
        Intent intent = new Intent(context, EmailVerificationActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_START_RESEND_COOLDOWN, startResendCooldown);
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
        if (remainingCooldownMillis() > 0 || resendInProgress) {
            startResendCountdown();
            return;
        }

        errorText.setVisibility(View.GONE);
        setResending(true);

        authService.sendEmailVerification(new AuthService.ActionCallback() {
            @Override
            public void onSuccess() {
                setResending(false);
                beginResendCooldown();
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
        verificationInProgress = loading;
        verifyButton.setText(loading ? "확인 중..." : "인증 완료 확인");
        updateActionButtons();
    }

    private void setResending(boolean resending) {
        resendInProgress = resending;
        updateActionButtons();
    }

    private void initializeResendCooldown(String email) {
        String ownerKey = authService.getCurrentUserId();
        if (ownerKey == null || ownerKey.isEmpty()) {
            ownerKey = email == null || email.isEmpty() ? "unknown" : email;
        }
        resendPreferenceKey = RESEND_AVAILABLE_AT_PREFIX + ownerKey;
        resendAvailableAtMillis = getSharedPreferences(
                VERIFICATION_PREFERENCES,
                MODE_PRIVATE
        ).getLong(resendPreferenceKey, 0L);

        if (getIntent().getBooleanExtra(EXTRA_START_RESEND_COOLDOWN, false)
                && remainingCooldownMillis() <= 0) {
            saveResendAvailableAt(System.currentTimeMillis() + RESEND_COOLDOWN_MILLIS);
        }
        startResendCountdown();
    }

    private void beginResendCooldown() {
        saveResendAvailableAt(System.currentTimeMillis() + RESEND_COOLDOWN_MILLIS);
        startResendCountdown();
    }

    private void saveResendAvailableAt(long availableAtMillis) {
        resendAvailableAtMillis = availableAtMillis;
        getSharedPreferences(VERIFICATION_PREFERENCES, MODE_PRIVATE)
                .edit()
                .putLong(resendPreferenceKey, availableAtMillis)
                .apply();
    }

    private long remainingCooldownMillis() {
        return Math.max(0L, resendAvailableAtMillis - System.currentTimeMillis());
    }

    private void startResendCountdown() {
        if (resendCountdown != null) {
            resendCountdown.cancel();
            resendCountdown = null;
        }

        long remainingMillis = remainingCooldownMillis();
        if (remainingMillis <= 0) {
            clearExpiredCooldown();
            updateActionButtons();
            return;
        }

        resendCountdown = new CountDownTimer(remainingMillis, 250L) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateActionButtons();
            }

            @Override
            public void onFinish() {
                resendCountdown = null;
                clearExpiredCooldown();
                updateActionButtons();
            }
        }.start();
        updateActionButtons();
    }

    private void clearExpiredCooldown() {
        resendAvailableAtMillis = 0L;
        if (resendPreferenceKey != null) {
            getSharedPreferences(VERIFICATION_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .remove(resendPreferenceKey)
                    .apply();
        }
    }

    private void updateActionButtons() {
        verifyButton.setEnabled(!verificationInProgress && !resendInProgress);

        if (resendInProgress) {
            resendButton.setEnabled(false);
            resendButton.setText("발송 중...");
            return;
        }

        long remainingMillis = remainingCooldownMillis();
        if (remainingMillis > 0) {
            long remainingSeconds = Math.max(1L, (remainingMillis + 999L) / 1000L);
            resendButton.setEnabled(false);
            resendButton.setText(getString(
                    R.string.email_verification_resend_countdown,
                    remainingSeconds
            ));
            return;
        }

        resendButton.setEnabled(!verificationInProgress);
        resendButton.setText("인증 메일 재전송");
    }

    @Override
    protected void onDestroy() {
        if (resendCountdown != null) {
            resendCountdown.cancel();
            resendCountdown = null;
        }
        super.onDestroy();
    }

    private void showError(String message) {
        errorText.setText("⚠ " + message);
        errorText.setVisibility(View.VISIBLE);
    }
}
