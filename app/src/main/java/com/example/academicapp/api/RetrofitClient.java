package com.example.academicapp.api;

import android.content.Context;

import com.example.academicapp.BuildConfig;
import com.example.academicapp.session.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = normalizeBaseUrl(BuildConfig.API_BASE_URL);
    private static Retrofit retrofit;

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "http://10.58.202.54:8080/";
        }

        String trimmedBaseUrl = baseUrl.trim();
        return trimmedBaseUrl.endsWith("/") ? trimmedBaseUrl : trimmedBaseUrl + "/";
    }

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            Interceptor authInterceptor = chain -> {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder();

                String token = sessionManager.getToken();
                String tokenType = sessionManager.getTokenType();

                if (token != null && !token.trim().isEmpty()) {
                    String authType = tokenType != null && !tokenType.trim().isEmpty() ? tokenType : "Bearer";
                    requestBuilder.header("Authorization", authType + " " + token);
                }

                return chain.proceed(requestBuilder.build());
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())  // Usamos Gson para convertir los objetos a JSON
                    .build();
        }
        return retrofit;
    }
}
