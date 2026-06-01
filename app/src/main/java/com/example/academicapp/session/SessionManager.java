package com.example.academicapp.session;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Gestor de sesion local que persiste token, tipo de autenticacion y correo del alumno.
 */
public class SessionManager {

    private static final String PREF_NAME = "academic_app_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda la sesion de forma sincrona para que el token este disponible
     * inmediatamente al navegar a la pantalla principal tras un login correcto.
     */
    public void saveSession(String token, String tokenType, String email) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_TOKEN_TYPE, tokenType)
                .putString(KEY_EMAIL, normalizeEmail(email))
                .commit();
    }

    public boolean hasValidSession() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getTokenType() {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    /**
     * Borra todo el estado local de autenticacion cuando el usuario cierra
     * sesion, elimina la cuenta o el backend devuelve una autenticacion invalida.
     */
    public void clearSession() {
        sharedPreferences.edit().clear().commit();
    }

    /*
     * El correo se usa como clave logica de cache; normalizarlo evita duplicar
     * datos si el usuario introduce mayusculas o espacios accidentales.
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
