package com.example.academicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.LoginRequest;
import com.example.academicapp.api.LoginResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de la app Android encargada de gestionar la pantalla LoginActivity y coordinar su interfaz con la API.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText edtCorreo, edtPassword;
    private Button btnLogin, btnGoToRegister;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtCorreo = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        String registeredEmail = getIntent().getStringExtra("registered_email");
        if (registeredEmail != null && !registeredEmail.isEmpty()) {
            edtCorreo.setText(registeredEmail);
        }

        btnLogin.setOnClickListener(v -> intentarLogin());

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void intentarLogin() {
        ConnectionErrorViewHelper.hide(this);
        limpiarErrores();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!validarCamposObligatorios(correo, password)) {
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(correo, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                ConnectionErrorViewHelper.hide(LoginActivity.this);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Al cambiar de usuario se invalida cualquier dato cacheado
                    // antes de guardar el nuevo token.
                    AppDataCache.clearAll();
                    EstadisticasActivity.invalidateCache();
                    sessionManager.saveSession(loginResponse.getToken(), loginResponse.getTipo(), correo);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("play_login_animation", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }

                Toast.makeText(
                        LoginActivity.this,
                        ApiErrorUtils.getErrorMessage(response, getLoginErrorMessage(response.code())),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "No se pudo conectar con la API", t);
                // onFailure representa un problema de red o transporte, no una
                // respuesta HTTP con credenciales incorrectas.
                ConnectionErrorViewHelper.show(LoginActivity.this, () -> intentarLogin());
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnGoToRegister.setEnabled(!isLoading);
    }

    private boolean validarCamposObligatorios(String correo, String password) {
        boolean isValid = true;
        if (correo.isEmpty()) {
            edtCorreo.setError("Introduce tu correo educativo.");
            isValid = false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Introduce tu contraseña.");
            isValid = false;
        }
        if (!isValid) {
            if (correo.isEmpty()) {
                edtCorreo.requestFocus();
            } else {
                edtPassword.requestFocus();
            }
        }
        return isValid;
    }

    private void limpiarErrores() {
        edtCorreo.setError(null);
        edtPassword.setError(null);
    }

    private String getLoginErrorMessage(int code) {
        if (code == 401 || code == 403) {
            return "Correo o contraseña incorrectos.";
        }
        if (code >= 500) {
            return "Error del servidor. Inténtalo de nuevo.";
        }
        return "No se pudo iniciar sesión.";
    }
}
