package com.example.academicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.ChatCursoEstadoResponse;
import com.example.academicapp.api.PerfilAlumnoResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int TOUR_TOTAL_STEPS = 17;
    private static final long CHAT_BADGE_REFRESH_INTERVAL_MS = 3_000L;

    private ImageView imgHomeLogo;
    private TextView txtWelcome;
    private TextView txtHomeCourse;
    private TextView txtChatUnreadBadge;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabNuevaNota;

    private SessionManager sessionManager;
    private ApiService apiService;
    private boolean firstLoad = true;
    private boolean homeActivo;
    private boolean cargandoAvisoChat;
    private long ultimoUnreadMostrado = -1L;
    private final Handler chatBadgeHandler = new Handler(Looper.getMainLooper());
    private final Runnable chatBadgeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!homeActivo) {
                return;
            }
            cargarAvisoChat();
            chatBadgeHandler.postDelayed(this, CHAT_BADGE_REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWelcome = findViewById(R.id.txtWelcome);
        imgHomeLogo = findViewById(R.id.imgHomeLogo);
        txtHomeCourse = findViewById(R.id.txtHomeCourse);
        txtChatUnreadBadge = findViewById(R.id.txtChatUnreadBadge);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabNuevaNota = findViewById(R.id.fabNuevaNota);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        NavigationHelper.setup(this, bottomNavigation, R.id.nav_home);
        fabNuevaNota.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChatCursoActivity.class)));
        txtChatUnreadBadge.bringToFront();
        txtChatUnreadBadge.setTranslationZ(dp(18));

        cargarDatosIniciales();
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeActivo = true;
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        if (firstLoad) {
            firstLoad = false;
            iniciarActualizacionAvisoChat();
            return;
        }
        cargarDatosIniciales();
        iniciarActualizacionAvisoChat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        homeActivo = false;
        chatBadgeHandler.removeCallbacks(chatBadgeRunnable);
    }

    private void iniciarActualizacionAvisoChat() {
        chatBadgeHandler.removeCallbacks(chatBadgeRunnable);
        chatBadgeHandler.postDelayed(chatBadgeRunnable, CHAT_BADGE_REFRESH_INTERVAL_MS);
    }

    private void cargarDatosIniciales() {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        PerfilAlumnoResponse perfilCache = AppDataCache.getPerfil();
        if (perfilCache != null) {
            mostrarBienvenida(perfilCache);
            cargarAvisoChat();
            setLoading(false);
            return;
        }

        setLoading(true);
        apiService.getPerfil().enqueue(new Callback<PerfilAlumnoResponse>() {
            @Override
            public void onResponse(Call<PerfilAlumnoResponse> call, Response<PerfilAlumnoResponse> response) {
                setLoading(false);
                ConnectionErrorViewHelper.hide(MainActivity.this);
                if (response.isSuccessful() && response.body() != null) {
                    AppDataCache.setPerfil(response.body());
                    mostrarBienvenida(response.body());
                    cargarAvisoChat();
                    return;
                }

                gestionarErrorAutenticacion(response.code());
            }

            @Override
            public void onFailure(Call<PerfilAlumnoResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(MainActivity.this, () -> cargarDatosIniciales());
            }
        });
    }

    private void mostrarBienvenida(PerfilAlumnoResponse perfil) {
        txtWelcome.setText("Hola, " + safeText(perfil.getNombre()));
        txtHomeCourse.setText("Curso actual: " + safeText(buildCursoText(perfil), "No indicado"));
        mostrarOnboardingSiCorresponde(perfil);
    }

    private void cargarAvisoChat() {
        if (cargandoAvisoChat || !sessionManager.hasValidSession()) {
            return;
        }

        cargandoAvisoChat = true;
        apiService.getEstadoChatCurso().enqueue(new Callback<ChatCursoEstadoResponse>() {
            @Override
            public void onResponse(Call<ChatCursoEstadoResponse> call, Response<ChatCursoEstadoResponse> response) {
                cargandoAvisoChat = false;
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                actualizarBadgeChat(response.body().getMensajesNoLeidos());
            }

            @Override
            public void onFailure(Call<ChatCursoEstadoResponse> call, Throwable t) {
                cargandoAvisoChat = false;
            }
        });
    }

    private void actualizarBadgeChat(long unread) {
        if (unread == ultimoUnreadMostrado) {
            return;
        }

        ultimoUnreadMostrado = unread;
        txtChatUnreadBadge.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
        txtChatUnreadBadge.setText(unread > 9 ? "9+" : String.valueOf(unread));
        if (unread > 0) {
            txtChatUnreadBadge.bringToFront();
            txtChatUnreadBadge.animate().scaleX(1.15f).scaleY(1.15f).setDuration(160)
                    .withEndAction(() -> txtChatUnreadBadge.animate().scaleX(1f).scaleY(1f).setDuration(160).start())
                    .start();
        }
    }

    private void mostrarOnboardingSiCorresponde(PerfilAlumnoResponse perfil) {
        if (perfil.isTutorialCompletado()) {
            return;
        }

        if (OnboardingStateHelper.isLocalDone(this)) {
            OnboardingStateHelper.markCompleted(this);
            return;
        }

        mostrarPasoOnboarding(0);
    }

    private void mostrarPasoOnboarding(int index) {
        OnboardingStep[] steps = {
                new OnboardingStep("Bienvenido a AcademicApp", "Este es tu punto de partida: una pantalla limpia para orientarte antes de entrar en notas, progreso o perfil.", findViewById(R.id.imgHomeLogo)),
                new OnboardingStep("Barra de navegación", "Abajo tienes las secciones principales. La guía señalará cada zona importante mientras avanzas.", bottomNavigation),
                new OnboardingStep("Chat del curso", "Este botón abre la conversación con compañeros de tu mismo curso o ciclo.", fabNuevaNota),
                new OnboardingStep("Asignaturas", "Entramos por aquí para ver la ruta real: asignatura, unidad didáctica y creación de nota.", bottomNavigation.findViewById(R.id.nav_subjects))
        };

        TutorialOverlayView.show(
                this,
                steps[index].targetView,
                steps[index].title,
                steps[index].message,
                index,
                TOUR_TOTAL_STEPS,
                () -> mostrarPasoOnboarding(Math.max(0, index - 1)),
                () -> {
                    if (index < steps.length - 1) {
                        mostrarPasoOnboarding(index + 1);
                    } else {
                        Intent intent = new Intent(MainActivity.this, AsignaturasActivity.class);
                        intent.putExtra("tour_step", 4);
                        startActivity(intent);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private void gestionarErrorAutenticacion(int code) {
        if (code == 401 || code == 403) {
            AppDataCache.clearAll();
            EstadisticasActivity.invalidateCache();
            sessionManager.clearSession();
            volverALogin();
        }
    }

    private String buildCursoText(PerfilAlumnoResponse perfil) {
        String numeroCurso = perfil.getNumeroCurso() != null ? perfil.getNumeroCurso().toString() : "";
        String siglas = perfil.getSiglasCiclo() != null ? perfil.getSiglasCiclo() : "";
        String nombre = perfil.getNombreCiclo() != null ? " - " + perfil.getNombreCiclo() : "";
        return (numeroCurso + " " + siglas + nombre).trim();
    }

    private String safeText(String value) {
        return safeText(value, "-");
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void volverALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static class OnboardingStep {
        private final String title;
        private final String message;
        private final View targetView;

        OnboardingStep(String title, String message, View targetView) {
            this.title = title;
            this.message = message;
            this.targetView = targetView;
        }
    }
}
