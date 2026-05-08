package com.example.studymate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isLoggedIn()) {
                goToAndClear(HomeActivity.class);
            } else {
                goToAndClear(LoginActivity.class);
            }
        }, 900);
    }
}
