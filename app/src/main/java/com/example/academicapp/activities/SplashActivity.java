package com.example.academicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.BuildConfig;
import com.example.academicapp.R;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final String INSTALL_PREFS = "academic_app_install_state";
    private static final String KEY_LAST_VERSION_CODE = "last_version_code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);
        resetSessionIfFirstRunOfVersion(sessionManager);
        Intent intent;

        if (sessionManager.hasValidSession()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 1100);
    }

    private void resetSessionIfFirstRunOfVersion(SessionManager sessionManager) {
        SharedPreferences prefs = getSharedPreferences(INSTALL_PREFS, MODE_PRIVATE);
        int storedVersionCode = prefs.getInt(KEY_LAST_VERSION_CODE, -1);
        if (storedVersionCode != BuildConfig.VERSION_CODE) {
            AppDataCache.clearAll();
            EstadisticasActivity.invalidateCache();
            sessionManager.clearSession();
            prefs.edit().putInt(KEY_LAST_VERSION_CODE, BuildConfig.VERSION_CODE).commit();
        }
    }
}
