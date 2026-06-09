package com.example.studymate.service;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {
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

    public void applyEmailVerificationCode(String code, ActionCallback callback) {
        firebaseAuth.applyActionCode(code)
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
        return "인증 처리에 실패했습니다. 잠시 후 다시 시도해주세요.";
    }
}
