package com.example.academicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.api.UnidadDidacticaResponse;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnidadesActivity extends AppCompatActivity {

    private TextView txtTituloUnidades;
    private TextView txtRutaUnidades;
    private TextView txtEmptyUnidades;
    private ImageButton btnBackUnidades;
    private ListView listUnidades;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<UnidadDidacticaResponse> unidadesCargadas = new ArrayList<>();
    private long idAsignatura;
    private boolean firstLoad = true;
    private boolean tourMostrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unidades);

        txtTituloUnidades = findViewById(R.id.txtTituloUnidades);
        txtRutaUnidades = findViewById(R.id.txtRutaUnidades);
        txtEmptyUnidades = findViewById(R.id.txtEmptyUnidades);
        btnBackUnidades = findViewById(R.id.btnBackUnidades);
        listUnidades = findViewById(R.id.listUnidades);
        progressBar = findViewById(R.id.progressBarUnidades);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        String nombreAsignatura = getIntent().getStringExtra("nombre_asignatura");
        idAsignatura = getIntent().getLongExtra("id_asignatura", -1L);

        txtTituloUnidades.setText(nombreAsignatura != null ? nombreAsignatura : "Unidades didácticas");
        txtRutaUnidades.setText("Asignaturas > " + safeText(nombreAsignatura, "Asignatura"));
        btnBackUnidades.setOnClickListener(v -> finish());
        NavigationHelper.setup(this, bottomNavigation, R.id.nav_subjects);
        listUnidades.setOnItemClickListener((parent, view, position, id) -> animarPulsado(view, () -> abrirNotasUnidad(position)));

        cargarUnidades(idAsignatura);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstLoad) {
            firstLoad = false;
            return;
        }
        cargarUnidades(idAsignatura);
    }

    private void cargarUnidades(long idAsignatura) {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        if (idAsignatura <= 0) {
            Toast.makeText(this, "No se pudo identificar la asignatura.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<UnidadDidacticaResponse> cachedUnidades = AppDataCache.getUnidades(idAsignatura);
        if (cachedUnidades != null) {
            unidadesCargadas = cachedUnidades;
            setLoading(false);
            mostrarUnidades(unidadesCargadas);
            return;
        }

        setLoading(true);
        apiService.getUnidades(idAsignatura).enqueue(new Callback<List<UnidadDidacticaResponse>>() {
            @Override
            public void onResponse(Call<List<UnidadDidacticaResponse>> call, Response<List<UnidadDidacticaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(UnidadesActivity.this);
                    unidadesCargadas = response.body();
                    AppDataCache.setUnidades(idAsignatura, unidadesCargadas);
                    setLoading(false);
                    mostrarUnidades(unidadesCargadas);
                    return;
                }

                setLoading(false);
                if (response.code() == 401 || response.code() == 403) {
                    AppDataCache.clearAll();
                    EstadisticasActivity.invalidateCache();
                    sessionManager.clearSession();
                    volverALogin();
                    return;
                }

                Toast.makeText(UnidadesActivity.this, "No se pudieron cargar las unidades.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<UnidadDidacticaResponse>> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(UnidadesActivity.this, () -> cargarUnidades(idAsignatura));
            }
        });
    }

    private void mostrarUnidades(List<UnidadDidacticaResponse> unidades) {
        if (unidades.isEmpty()) {
            txtEmptyUnidades.setVisibility(View.VISIBLE);
            listUnidades.setVisibility(View.GONE);
            return;
        }

        txtEmptyUnidades.setVisibility(View.GONE);
        listUnidades.setVisibility(View.VISIBLE);

        listUnidades.setAdapter(new UnidadesAdapter(unidades));
        mostrarTourSiCorresponde();
    }

    private void abrirNotasUnidad(int position) {
        if (position < 0 || position >= unidadesCargadas.size()) {
            return;
        }

        UnidadDidacticaResponse unidad = unidadesCargadas.get(position);

        Intent intent = new Intent(UnidadesActivity.this, UnidadNotasActivity.class);
        intent.putExtra("id_asignatura", idAsignatura);
        intent.putExtra("id_unidad", unidad.getIdUnidad());
        intent.putExtra("nombre_asignatura", txtTituloUnidades.getText().toString());
        intent.putExtra("nombre_unidad", unidad.getNombreUnidad());
        intent.putExtra("orden_unidad", unidad.getOrdenUnidad());
        if (getIntent().hasExtra("tour_step")) {
            intent.putExtra("tour_step", 8);
        }

        startActivity(intent);
    }

    private void animarPulsado(View view, Runnable onEnd) {
        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .alpha(0.82f)
                .setDuration(90)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(120)
                        .withEndAction(onEnd)
                        .start())
                .start();
    }

    private void mostrarTourSiCorresponde() {
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 6) {
            return;
        }
        tourMostrado = true;
        listUnidades.postDelayed(() -> mostrarPasoTour(6), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View primerItem = listUnidades.getChildAt(0) != null ? listUnidades.getChildAt(0) : listUnidades;
        View target = index == 6 ? txtRutaUnidades : primerItem;
        String title = index == 6 ? "Ruta de aprendizaje" : "Unidad didáctica";
        String message = index == 6
                ? "La ruta te recuerda dónde estás: Asignaturas > unidad. Así el alumno no se pierde al profundizar."
                : "Cada unidad te lleva al listado de notas registradas para consultar, editar o añadir nuevas calificaciones.";

        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> finish(),
                () -> {
                    if (index == 6) {
                        mostrarPasoTour(7);
                    } else {
                        abrirNotasUnidad(0);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void volverALogin() {
        Intent intent = new Intent(UnidadesActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String safeNumber(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private class UnidadesAdapter extends BaseAdapter {
        private final List<UnidadDidacticaResponse> unidades;
        private final LayoutInflater inflater;

        UnidadesAdapter(List<UnidadDidacticaResponse> unidades) {
            this.unidades = unidades;
            this.inflater = LayoutInflater.from(UnidadesActivity.this);
        }

        @Override
        public int getCount() {
            return unidades.size();
        }

        @Override
        public UnidadDidacticaResponse getItem(int position) {
            return unidades.get(position);
        }

        @Override
        public long getItemId(int position) {
            UnidadDidacticaResponse unidad = getItem(position);
            return unidad.getIdUnidad() == null ? position : unidad.getIdUnidad();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_unidad, parent, false);
            }

            UnidadDidacticaResponse unidad = getItem(position);
            TextView txtUnidadTitulo = view.findViewById(R.id.txtUnidadTitulo);
            TextView txtUnidadDescripcion = view.findViewById(R.id.txtUnidadDescripcion);

            txtUnidadTitulo.setText("Unidad " + safeNumber(unidad.getOrdenUnidad()) + ": " + safeText(unidad.getNombreUnidad(), "-"));
            txtUnidadDescripcion.setText(safeText(unidad.getDescripcion(), "Sin descripción"));

            return view;
        }
    }
}
