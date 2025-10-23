package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PreferenceManager preferencesManager = new PreferenceManager(this);

            Intent intent;
            if (preferencesManager.isUserLoggedIn()) {
                // Si ya hay sesión, ir directo al Home
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                // Si no hay sesión, ir al Login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}