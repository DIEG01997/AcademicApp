package com.example.academicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.AsignaturaResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de la app Android encargada de gestionar la pantalla AsignaturasActivity y coordinar su interfaz con la API.
 */
public class AsignaturasActivity extends AppCompatActivity {

    private TextView txtEmpty;
    private ListView listAsignaturas;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<AsignaturaResponse> asignaturasCargadas = new ArrayList<>();
    private boolean tourMostrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignaturas);

        txtEmpty = findViewById(R.id.txtEmpty);
        listAsignaturas = findViewById(R.id.listAsignaturas);
        progressBar = findViewById(R.id.progressBarAsignaturas);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        NavigationHelper.setup(this, bottomNavigation, R.id.nav_subjects);
        listAsignaturas.setOnItemClickListener((parent, view, position, id) -> animarPulsado(view, () -> abrirUnidades(position)));

        cargarAsignaturas();
    }

    private void cargarAsignaturas() {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        List<AsignaturaResponse> cachedAsignaturas = AppDataCache.getAsignaturas();
        if (cachedAsignaturas != null) {
            asignaturasCargadas = cachedAsignaturas;
            mostrarAsignaturas(asignaturasCargadas);
            setLoading(false);
            return;
        }

        setLoading(true);
        apiService.getAsignaturas().enqueue(new Callback<List<AsignaturaResponse>>() {
            @Override
            public void onResponse(Call<List<AsignaturaResponse>> call, Response<List<AsignaturaResponse>> response) {
                setLoading(false);
                ConnectionErrorViewHelper.hide(AsignaturasActivity.this);

                if (response.isSuccessful() && response.body() != null) {
                    asignaturasCargadas = response.body();
                    AppDataCache.setAsignaturas(asignaturasCargadas);
                    mostrarAsignaturas(asignaturasCargadas);
                    return;
                }

                if (response.code() == 401 || response.code() == 403) {
                    AppDataCache.clearAll();
                    EstadisticasActivity.invalidateCache();
                    sessionManager.clearSession();
                    volverALogin();
                    return;
                }

                Toast.makeText(AsignaturasActivity.this, "No se pudieron cargar las asignaturas.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<AsignaturaResponse>> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(AsignaturasActivity.this, () -> cargarAsignaturas());
            }
        });
    }

    private void mostrarAsignaturas(List<AsignaturaResponse> asignaturas) {
        if (asignaturas.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            listAsignaturas.setVisibility(View.GONE);
            return;
        }

        txtEmpty.setVisibility(View.GONE);
        listAsignaturas.setVisibility(View.VISIBLE);
        listAsignaturas.setAdapter(new AsignaturasAdapter(asignaturas));
        mostrarTourSiCorresponde();
    }

    private void abrirUnidades(int position) {
        if (position < 0 || position >= asignaturasCargadas.size()) {
            return;
        }

        AsignaturaResponse asignatura = asignaturasCargadas.get(position);
        Intent intent = new Intent(AsignaturasActivity.this, UnidadesActivity.class);
        intent.putExtra("id_asignatura", asignatura.getIdAsignatura());
        intent.putExtra("nombre_asignatura", asignatura.getNombreAsignatura());
        if (getIntent().hasExtra("tour_step")) {
            intent.putExtra("tour_step", 6);
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
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 4) {
            return;
        }
        tourMostrado = true;
        listAsignaturas.postDelayed(() -> mostrarPasoTour(4), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View primerItem = listAsignaturas.getChildAt(0) != null ? listAsignaturas.getChildAt(0) : listAsignaturas;
        View target = index == 4 ? listAsignaturas : primerItem;
        String title = index == 4 ? "Tus asignaturas" : "Entra en una asignatura";
        String message = index == 4
                ? "Aquí aparece el listado completo de asignaturas. Esta sección no mezcla estadísticas: sirve para navegar por contenidos y notas."
                : "Toca una asignatura para ver sus unidades didácticas. El tour abrirá la primera para enseñarte el camino.";

        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> volverInicioTour(),
                () -> {
                    if (index == 4) {
                        mostrarPasoTour(5);
                    } else {
                        abrirUnidades(0);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private void volverInicioTour() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private String safeText(String value) {
        return safeText(value, "-");
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void volverALogin() {
        Intent intent = new Intent(AsignaturasActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class AsignaturasAdapter extends BaseAdapter {
        private final List<AsignaturaResponse> asignaturas;
        private final LayoutInflater inflater;

        AsignaturasAdapter(List<AsignaturaResponse> asignaturas) {
            this.asignaturas = asignaturas;
            this.inflater = LayoutInflater.from(AsignaturasActivity.this);
        }

        @Override
        public int getCount() {
            return asignaturas.size();
        }

        @Override
        public AsignaturaResponse getItem(int position) {
            return asignaturas.get(position);
        }

        @Override
        public long getItemId(int position) {
            AsignaturaResponse asignatura = getItem(position);
            return asignatura.getIdAsignatura() == null ? position : asignatura.getIdAsignatura();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_asignatura, parent, false);
            }

            AsignaturaResponse asignatura = getItem(position);
            TextView txtAsignaturaNombre = view.findViewById(R.id.txtAsignaturaNombre);
            TextView txtAsignaturaDescripcion = view.findViewById(R.id.txtAsignaturaDescripcion);

            txtAsignaturaNombre.setText(safeText(asignatura.getNombreAsignatura()));
            txtAsignaturaDescripcion.setText(safeText(asignatura.getDescripcion(), "Sin descripción"));

            return view;
        }
    }
}
