package com.example.academicapp.activities;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.session.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class OnboardingStateHelper {

    public static final String PREFS = "academic_app_onboarding";
    public static final String KEY_DONE = "guided_tour_v2_done";

    private OnboardingStateHelper() {
    }

    public static boolean isLocalDone(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(getUserScopedKey(context), false);
    }

    public static void markCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(getUserScopedKey(context), true).apply();

        ApiService apiService = RetrofitClient.getRetrofitInstance(context).create(ApiService.class);
        apiService.marcarTutorialCompletado().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private static String getUserScopedKey(Context context) {
        String email = new SessionManager(context.getApplicationContext()).getEmail();
        if (email == null || email.trim().isEmpty()) {
            return KEY_DONE + "_anonymous";
        }

        String userKey = email.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "_");
        return KEY_DONE + "_" + userKey;
    }
}
