package com.example.academicapp.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Response;

public final class ApiErrorUtils {

    private static final Gson GSON = new Gson();

    private ApiErrorUtils() {
    }

    public static String getErrorMessage(Response<?> response, String fallback) {
        if (response == null || response.errorBody() == null) {
            return fallback;
        }

        try {
            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) {
                return fallback;
            }

            ApiErrorResponse apiError = GSON.fromJson(raw, ApiErrorResponse.class);
            if (apiError != null && apiError.getError() != null && !apiError.getError().trim().isEmpty()) {
                return apiError.getError();
            }
        } catch (IOException | JsonSyntaxException ignored) {
        }

        return fallback;
    }
}
