package com.example.academicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.DashboardAsignaturaResponse;
import com.example.academicapp.api.DashboardResponse;
import com.example.academicapp.api.DashboardTrimestreResponse;
import com.example.academicapp.api.DashboardUnidadResponse;
import com.example.academicapp.api.MediaGlobalResponse;
import com.example.academicapp.api.PercentilAsignaturaResponse;
import com.example.academicapp.api.PercentilGlobalResponse;
import com.example.academicapp.api.PorcentajeSuspensosAsignaturasResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.example.academicapp.views.ProgressChartView;
import com.example.academicapp.views.SubjectProgressChartView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de la app Android encargada de gestionar la pantalla EstadisticasActivity y coordinar su interfaz con la API.
 */
public class EstadisticasActivity extends AppCompatActivity {

    private static final long CACHE_TTL_MS = 45_000L;
    private static String cacheOwnerEmail;
    private static DashboardResponse dashboardCache;
    private static long dashboardCacheAt;
    private static PercentileCache percentilGlobalCache;
    private static final Map<Long, PercentileCache> percentilesAsignaturaCache = new HashMap<>();

    public static void invalidateCache() {
        dashboardCache = null;
        dashboardCacheAt = 0L;
        percentilGlobalCache = null;
        percentilesAsignaturaCache.clear();
    }

    private TextView txtEmptyEstadisticas;
    private LinearLayout layoutAsignaturasEstadisticas;
    private ProgressBar progressBarEstadisticas;
    private ProgressChartView chartProgreso;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private ApiService apiService;
    private boolean primeraCargaRealizada = false;
    private boolean cargandoEstadisticas = false;
    private boolean tourMostrado;

    private double mediaGlobalActual = -1;
    private int aprobadasActual = 0;
    private int suspensasActual = 0;
    private double percentilGlobalActual = -1;
    private int alumnosComparadosActual = 0;
    private final List<ProgressChartView.TermValue> evolucionActual = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        txtEmptyEstadisticas = findViewById(R.id.txtEmptyEstadisticas);
        layoutAsignaturasEstadisticas = findViewById(R.id.layoutAsignaturasEstadisticas);
        progressBarEstadisticas = findViewById(R.id.progressBarEstadisticas);
        chartProgreso = findViewById(R.id.chartProgreso);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        NavigationHelper.setup(this, bottomNavigation, R.id.nav_stats);

        cargarEstadisticas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (primeraCargaRealizada && !cargandoEstadisticas && !tieneDashboardCacheValida()) {
            cargarEstadisticas();
        }
    }

    private void cargarEstadisticas() {
        ConnectionErrorViewHelper.hide(this);
        if (cargandoEstadisticas) {
            return;
        }

        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        prepararCacheSesionActual();
        primeraCargaRealizada = true;

        if (tieneDashboardCacheValida()) {
            cargandoEstadisticas = false;
            setLoading(false);
            aplicarPercentilGlobalDesdeCache();
            mostrarDashboard(dashboardCache);
            if (!tienePercentilGlobalCacheValida()) {
                cargarPercentilGlobal();
            }
            return;
        }

        cargandoEstadisticas = true;

        limpiarVista();
        setLoading(true);

        cargarDashboard();
        if (tienePercentilGlobalCacheValida()) {
            aplicarPercentilGlobalDesdeCache();
        } else {
            cargarPercentilGlobal();
        }
    }

    private void cargarDashboard() {
        apiService.getDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                cargandoEstadisticas = false;
                setLoading(false);
                ConnectionErrorViewHelper.hide(EstadisticasActivity.this);

                if (response.isSuccessful() && response.body() != null) {
                    dashboardCache = response.body();
                    dashboardCacheAt = System.currentTimeMillis();
                    mostrarDashboard(dashboardCache);
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(EstadisticasActivity.this, "No se pudo cargar el progreso.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                cargandoEstadisticas = false;
                setLoading(false);
                ConnectionErrorViewHelper.show(EstadisticasActivity.this, () -> cargarEstadisticas());
            }
        });
    }

    private void cargarPercentilGlobal() {
        apiService.getPercentilGlobal().enqueue(new Callback<PercentilGlobalResponse>() {
            @Override
            public void onResponse(Call<PercentilGlobalResponse> call, Response<PercentilGlobalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PercentilGlobalResponse percentil = response.body();
                    if (percentil.isCalculable()) {
                        percentilGlobalActual = parseDecimalValue(percentil.getPercentil());
                        alumnosComparadosActual = percentil.getTotalAlumnosComparados() == null
                                ? 0
                                : percentil.getTotalAlumnosComparados();
                        percentilGlobalCache = new PercentileCache(percentilGlobalActual, alumnosComparadosActual);
                        chartProgreso.setPercentile(percentilGlobalActual, alumnosComparadosActual);
                    }
                    return;
                }

                gestionarErrorAutenticacion(response.code());
            }

            @Override
            public void onFailure(Call<PercentilGlobalResponse> call, Throwable t) {
                chartProgreso.setPercentile(-1, 0);
            }
        });
    }

    private void limpiarVista() {
        mediaGlobalActual = -1;
        aprobadasActual = 0;
        suspensasActual = 0;
        percentilGlobalActual = -1;
        alumnosComparadosActual = 0;
        evolucionActual.clear();

        txtEmptyEstadisticas.setVisibility(View.GONE);
        layoutAsignaturasEstadisticas.removeAllViews();
        chartProgreso.clearData();
    }

    private void mostrarDashboard(DashboardResponse dashboard) {
        mediaGlobalActual = obtenerMediaGlobal(dashboard.getMediaGlobal());
        calcularBalanceAsignaturas(dashboard.getPorcentajeSuspensosAsignaturas());
        evolucionActual.clear();
        evolucionActual.addAll(obtenerEvolucion(dashboard.getEvolucionTrimestres()));

        chartProgreso.setData(
                mediaGlobalActual,
                aprobadasActual,
                suspensasActual,
                percentilGlobalActual,
                alumnosComparadosActual,
                evolucionActual
        );

        mostrarAsignaturas(dashboard.getAsignaturas());
        mostrarTourSiCorresponde();
    }

    private double obtenerMediaGlobal(MediaGlobalResponse mediaGlobal) {
        return mediaGlobal == null ? -1 : parseDecimalValue(mediaGlobal.getMediaGlobal());
    }

    private void calcularBalanceAsignaturas(PorcentajeSuspensosAsignaturasResponse suspensos) {
        int evaluadas = suspensos != null && suspensos.getAsignaturasEvaluadas() != null
                ? suspensos.getAsignaturasEvaluadas()
                : 0;
        suspensasActual = suspensos != null && suspensos.getAsignaturasSuspensas() != null
                ? suspensos.getAsignaturasSuspensas()
                : 0;
        aprobadasActual = Math.max(0, evaluadas - suspensasActual);
    }

    private List<ProgressChartView.TermValue> obtenerEvolucion(List<DashboardTrimestreResponse> trimestres) {
        List<ProgressChartView.TermValue> values = new ArrayList<>();
        if (trimestres == null) {
            return values;
        }

        for (DashboardTrimestreResponse trimestre : trimestres) {
            String label = trimestre.getNumeroEvaluacion() == null
                    ? "Eval."
                    : trimestre.getNumeroEvaluacion() + "ª";
            values.add(new ProgressChartView.TermValue(label, parseDecimalValue(trimestre.getMedia())));
        }

        return values;
    }

    private void mostrarAsignaturas(List<DashboardAsignaturaResponse> asignaturas) {
        layoutAsignaturasEstadisticas.removeAllViews();

        if (asignaturas == null || asignaturas.isEmpty()) {
            txtEmptyEstadisticas.setVisibility(View.VISIBLE);
            return;
        }

        txtEmptyEstadisticas.setVisibility(View.GONE);

        List<DashboardAsignaturaResponse> ordenadas = new ArrayList<>(asignaturas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ordenadas.sort(Comparator
                    .comparingDouble((DashboardAsignaturaResponse asignatura) -> parseDecimalValue(asignatura.getMediaAsignatura()))
                    .reversed());
        }

        for (DashboardAsignaturaResponse asignatura : ordenadas) {
            View card = crearTarjetaAsignatura(asignatura);
            layoutAsignaturasEstadisticas.addView(card);
        }
    }

    private View crearTarjetaAsignatura(DashboardAsignaturaResponse asignatura) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            card.setCardBackgroundColor(getColor(R.color.cardBackgroundColor));
        }
        card.setRadius(dp(8));
        card.setCardElevation(dp(1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            card.setStrokeColor(getColor(R.color.surfaceTintColor));
        }
        card.setStrokeWidth(dp(1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(16));

        TextView title = new TextView(this);
        title.setText(safeText(asignatura.getNombreAsignatura()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            title.setTextAppearance(R.style.CardTitleStyle);
        }
        content.addView(title);

        SubjectProgressChartView chart = new SubjectProgressChartView(this);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chartParams.setMargins(0, dp(12), 0, 0);
        chart.setLayoutParams(chartParams);
        chart.setData(parseDecimalValue(asignatura.getMediaAsignatura()), obtenerUnidades(asignatura.getUnidades()));
        content.addView(chart);

        card.addView(content);

        if (asignatura.getIdAsignatura() != null) {
            cargarPercentilAsignatura(asignatura.getIdAsignatura(), chart);
        }

        return card;
    }

    private List<SubjectProgressChartView.UnitValue> obtenerUnidades(List<DashboardUnidadResponse> unidades) {
        List<SubjectProgressChartView.UnitValue> values = new ArrayList<>();
        if (unidades == null) {
            return values;
        }

        for (DashboardUnidadResponse unidad : unidades) {
            String nombre = unidad.getOrdenUnidad() == null
                    ? safeText(unidad.getNombreUnidad())
                    : unidad.getOrdenUnidad() + ". " + safeText(unidad.getNombreUnidad());
            values.add(new SubjectProgressChartView.UnitValue(nombre, parseDecimalValue(unidad.getNotaFinal())));
        }

        return values;
    }

    private void cargarPercentilAsignatura(Long idAsignatura, SubjectProgressChartView chart) {
        PercentileCache cachedPercentile = percentilesAsignaturaCache.get(idAsignatura);
        if (cachedPercentile != null && cachedPercentile.isFresh()) {
            chart.setPercentile(cachedPercentile.percentil, cachedPercentile.alumnosComparados);
            return;
        }

        apiService.getPercentilAsignatura(idAsignatura).enqueue(new Callback<PercentilAsignaturaResponse>() {
            @Override
            public void onResponse(Call<PercentilAsignaturaResponse> call, Response<PercentilAsignaturaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PercentilAsignaturaResponse percentil = response.body();
                    if (percentil.isCalculable()) {
                        int alumnos = percentil.getTotalAlumnosComparados() == null
                                ? 0
                                : percentil.getTotalAlumnosComparados();
                        double valorPercentil = parseDecimalValue(percentil.getPercentil());
                        percentilesAsignaturaCache.put(idAsignatura, new PercentileCache(valorPercentil, alumnos));
                        chart.setPercentile(valorPercentil, alumnos);
                    }
                    return;
                }

                gestionarErrorAutenticacion(response.code());
            }

            @Override
            public void onFailure(Call<PercentilAsignaturaResponse> call, Throwable t) {
                chart.setPercentile(-1, 0);
            }
        });
    }

    private void prepararCacheSesionActual() {
        String emailActual = sessionManager.getEmail();
        if (cacheOwnerEmail != null && cacheOwnerEmail.equals(emailActual)) {
            return;
        }

        cacheOwnerEmail = emailActual;
        dashboardCache = null;
        dashboardCacheAt = 0L;
        percentilGlobalCache = null;
        percentilesAsignaturaCache.clear();
    }

    private boolean tieneDashboardCacheValida() {
        return dashboardCache != null && esCacheReciente(dashboardCacheAt);
    }

    private boolean tienePercentilGlobalCacheValida() {
        return percentilGlobalCache != null && percentilGlobalCache.isFresh();
    }

    private void aplicarPercentilGlobalDesdeCache() {
        if (!tienePercentilGlobalCacheValida()) {
            return;
        }

        percentilGlobalActual = percentilGlobalCache.percentil;
        alumnosComparadosActual = percentilGlobalCache.alumnosComparados;
        chartProgreso.setPercentile(percentilGlobalActual, alumnosComparadosActual);
    }

    private boolean esCacheReciente(long timestamp) {
        return timestamp > 0 && System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
    }

    private boolean gestionarErrorAutenticacion(int code) {
        if (code == 401 || code == 403) {
            AppDataCache.clearAll();
            invalidateCache();
            sessionManager.clearSession();
            volverALogin();
            return true;
        }

        return false;
    }

    private void setLoading(boolean isLoading) {
        progressBarEstadisticas.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private double parseDecimalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }

        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void volverALogin() {
        Intent intent = new Intent(EstadisticasActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarTourSiCorresponde() {
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 13) {
            return;
        }
        tourMostrado = true;
        chartProgreso.postDelayed(() -> mostrarPasoTour(13), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View target = index == 13 ? chartProgreso : layoutAsignaturasEstadisticas;
        String title = index == 13 ? "Progreso visual" : "Rendimiento por asignatura";
        String message = index == 13
                ? "Este panel resume media global, balance, percentil y evolución por evaluaciones con gráficas."
                : "Debajo se ordenan tus asignaturas de mejor a peor para detectar fortalezas y áreas de mejora.";

        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> mostrarPasoTour(Math.max(13, index - 1)),
                () -> {
                    if (index == 13) {
                        mostrarPasoTour(14);
                    } else {
                        Intent intent = new Intent(EstadisticasActivity.this, PerfilActivity.class);
                        intent.putExtra("tour_step", 15);
                        startActivity(intent);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private static class PercentileCache {
        private final double percentil;
        private final int alumnosComparados;
        private final long timestamp;

        private PercentileCache(double percentil, int alumnosComparados) {
            this.percentil = percentil;
            this.alumnosComparados = alumnosComparados;
            this.timestamp = System.currentTimeMillis();
        }

        private boolean isFresh() {
            return System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
        }
    }
}
