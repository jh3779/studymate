package com.example.studymate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.studymate.service.AuthService;

public class SplashActivity extends BaseActivity {
    private final AuthService authService = new AuthService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authService.isSignedIn()) {
                if (authService.isCurrentUserEmailVerified()) {
                    goToAndClear(HomeActivity.class);
                } else {
                    goToAndClear(EmailVerificationActivity.class);
                }
            } else {
                goToAndClear(LoginActivity.class);
            }
        }, 900);
    }
}
