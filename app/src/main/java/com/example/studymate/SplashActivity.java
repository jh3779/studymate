package com.example.studymate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.studymate.service.AuthService;

public class SplashActivity extends BaseActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AuthService authService = new AuthService();
    private final Runnable navigateAfterSplash = () -> {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (authService.isSignedIn()) {
            if (authService.isCurrentUserEmailVerified()) {
                goToAndClear(HomeActivity.class);
            } else {
                goToAndClear(EmailVerificationActivity.class);
            }
        } else {
            goToAndClear(LoginActivity.class);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(navigateAfterSplash, 900);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateAfterSplash);
        super.onDestroy();
    }
}
