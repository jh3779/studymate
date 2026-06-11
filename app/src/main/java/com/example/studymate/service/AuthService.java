package com.example.studymate.service;

import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {
    private static final String TAG = "AuthService";

    private final FirebaseAuth firebaseAuth;

    public AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);

        void onFailure(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public void signUp(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void signIn(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(result.getUser()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void sendEmailVerification(ActionCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("사용자 정보를 확인할 수 없습니다.");
            return;
        }

        user.sendEmailVerification()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void reloadCurrentUser(AuthCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("사용자 정보를 확인할 수 없습니다.");
            return;
        }

        user.reload()
                .addOnSuccessListener(unused -> callback.onSuccess(firebaseAuth.getCurrentUser()))
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public void deleteCurrentUser(ActionCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onSuccess();
            return;
        }

        user.delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onFailure(toUserMessage(error)));
    }

    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public boolean isCurrentUserEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user == null ? null : user.getUid();
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user == null ? null : user.getEmail();
    }

    private String toUserMessage(Exception error) {
        Log.e(TAG, "Firebase authentication failed", error);

        if (error instanceof FirebaseAuthUserCollisionException) {
            return "이미 가입된 이메일입니다.";
        }
        if (error instanceof FirebaseAuthInvalidUserException
                || error instanceof FirebaseAuthInvalidCredentialsException) {
            return "이메일 또는 비밀번호가 올바르지 않습니다.";
        }
        if (error instanceof FirebaseNetworkException) {
            return "네트워크 연결을 확인한 후 다시 시도해주세요.";
        }
        if (error instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) error).getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equals(errorCode)) {
                return "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
            }
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(errorCode)) {
                return "Firebase Console에서 이메일/비밀번호 로그인을 활성화해주세요.";
            }
            if ("ERROR_APP_NOT_AUTHORIZED".equals(errorCode)
                    || "ERROR_INVALID_API_KEY".equals(errorCode)) {
                return "이 앱의 Firebase API 키 또는 앱 서명 설정을 확인해주세요.";
            }
            return "인증 처리에 실패했습니다. 오류 코드: " + errorCode;
        }

        String detail = error.getMessage();
        if (detail != null) {
            String normalizedDetail = detail.toLowerCase();
            if (normalizedDetail.contains("api key")
                    || normalizedDetail.contains("blocked")
                    || normalizedDetail.contains("configuration_not_found")) {
                return "Firebase 프로젝트의 API 키, 앱 패키지명, SHA 인증서 설정을 확인해주세요.";
            }
        }
        return "인증 처리에 실패했습니다. 자세한 원인은 Logcat의 AuthService 로그를 확인해주세요.";
    }
}
