package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studymate.model.UserModel;
import com.example.studymate.service.AuthService;
import com.example.studymate.service.FirestoreService;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends BaseActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private TextView errorText;
    private Button signupButton;
    private final AuthService authService = new AuthService();
    private final FirestoreService firestoreService = new FirestoreService();

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
                if (user == null) {
                    setLoading(false);
                    showError("⚠ 사용자 정보를 확인할 수 없습니다.");
                    return;
                }

                UserModel userModel = new UserModel(
                        user.getUid(),
                        user.getEmail() == null ? email : user.getEmail(),
                        "",
                        null
                );
                saveUserProfile(userModel);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showError("⚠ " + errorMessage);
            }
        });
    }

    private void saveUserProfile(UserModel user) {
        firestoreService.saveUser(user, new FirestoreService.SaveCallback() {
            @Override
            public void onSuccess(String documentId) {
                sendInitialVerificationEmail(user.getEmail());
            }

            @Override
            public void onFailure(String errorMessage) {
                rollbackCreatedAccount(errorMessage);
            }
        });
    }

    private void sendInitialVerificationEmail(String email) {
        authService.sendEmailVerification(new AuthService.ActionCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                showShortToast("인증 메일을 보냈습니다.");
                openEmailVerification(email);
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                showShortToast("인증 메일 발송에 실패했습니다. 다시 시도해주세요.");
                openEmailVerification(email);
            }
        });
    }

    private void openEmailVerification(String email) {
        startActivity(EmailVerificationActivity.createIntent(this, email));
        finish();
    }

    private void rollbackCreatedAccount(String originalErrorMessage) {
        authService.deleteCurrentUser(new AuthService.ActionCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                showError("⚠ " + originalErrorMessage);
            }

            @Override
            public void onFailure(String cleanupErrorMessage) {
                authService.signOut();
                setLoading(false);
                showError("⚠ 회원 정보 저장에 실패했습니다. 관리자에게 문의해주세요.");
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
