package com.example.academicapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ActualizarNotaRequest;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.NotaListadoResponse;
import com.example.academicapp.api.NotaResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de la app Android encargada de gestionar la pantalla UnidadNotasActivity y coordinar su interfaz con la API.
 */
public class UnidadNotasActivity extends AppCompatActivity {

    private TextView txtTituloUnidadNotas;
    private TextView txtSubtituloUnidadNotas;
    private TextView txtEmptyUnidadNotas;
    private ImageButton btnBackUnidadNotas;
    private ListView listUnidadNotas;
    private ProgressBar progressBar;
    private FloatingActionButton fabCrearNotaUnidad;
    private BottomNavigationView bottomNavigation;
    private ActivityResultLauncher<Intent> crearNotaLauncher;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<NotaListadoResponse> notasUnidad = new ArrayList<>();
    private long idAsignatura;
    private long idUnidad;
    private String nombreAsignatura;
    private String nombreUnidad;
    private int ordenUnidad;
    private boolean firstLoad = true;
    private boolean tourMostrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unidad_notas);

        txtTituloUnidadNotas = findViewById(R.id.txtTituloUnidadNotas);
        txtSubtituloUnidadNotas = findViewById(R.id.txtSubtituloUnidadNotas);
        txtEmptyUnidadNotas = findViewById(R.id.txtEmptyUnidadNotas);
        btnBackUnidadNotas = findViewById(R.id.btnBackUnidadNotas);
        listUnidadNotas = findViewById(R.id.listUnidadNotas);
        progressBar = findViewById(R.id.progressBarUnidadNotas);
        fabCrearNotaUnidad = findViewById(R.id.fabCrearNotaUnidad);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        idAsignatura = getIntent().getLongExtra("id_asignatura", -1L);
        idUnidad = getIntent().getLongExtra("id_unidad", -1L);
        nombreAsignatura = getIntent().getStringExtra("nombre_asignatura");
        nombreUnidad = getIntent().getStringExtra("nombre_unidad");
        ordenUnidad = getIntent().getIntExtra("orden_unidad", -1);

        txtTituloUnidadNotas.setText(getTitulo());
        txtSubtituloUnidadNotas.setText("Asignaturas > "
                + safeText(nombreAsignatura, "Asignatura")
                + " > "
                + getTitulo());
        btnBackUnidadNotas.setOnClickListener(v -> finish());
        NavigationHelper.setup(this, bottomNavigation, R.id.nav_subjects);

        crearNotaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarNotas();
                    }
                }
        );

        fabCrearNotaUnidad.setOnClickListener(v -> abrirCrearNota());
        listUnidadNotas.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarOpcionesNota(position);
            return true;
        });

        cargarNotas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstLoad) {
            firstLoad = false;
            return;
        }
        cargarNotas();
    }

    private void cargarNotas() {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        if (idUnidad <= 0) {
            Toast.makeText(this, "No se pudo identificar la unidad.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<NotaListadoResponse> cachedNotas = AppDataCache.getNotas();
        if (cachedNotas != null) {
            notasUnidad = filtrarNotasUnidad(cachedNotas);
            mostrarNotas();
            setLoading(false);
            return;
        }

        setLoading(true);
        apiService.getNotas().enqueue(new Callback<List<NotaListadoResponse>>() {
            @Override
            public void onResponse(Call<List<NotaListadoResponse>> call, Response<List<NotaListadoResponse>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(UnidadNotasActivity.this);
                    AppDataCache.setNotas(response.body());
                    notasUnidad = filtrarNotasUnidad(response.body());
                    mostrarNotas();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(UnidadNotasActivity.this, "No se pudieron cargar las notas.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<NotaListadoResponse>> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(UnidadNotasActivity.this, () -> cargarNotas());
            }
        });
    }

    private List<NotaListadoResponse> filtrarNotasUnidad(List<NotaListadoResponse> notas) {
        List<NotaListadoResponse> resultado = new ArrayList<>();
        for (NotaListadoResponse nota : notas) {
            if (nota != null && nota.getIdUnidad() != null && nota.getIdUnidad() == idUnidad) {
                resultado.add(nota);
            }
        }
        return resultado;
    }

    private void mostrarNotas() {
        if (notasUnidad.isEmpty()) {
            txtEmptyUnidadNotas.setVisibility(View.VISIBLE);
            listUnidadNotas.setVisibility(View.GONE);
            listUnidadNotas.setAdapter(null);
            mostrarTourSiCorresponde();
            return;
        }

        txtEmptyUnidadNotas.setVisibility(View.GONE);
        listUnidadNotas.setVisibility(View.VISIBLE);
        listUnidadNotas.setAdapter(new UnidadNotasAdapter());
        mostrarTourSiCorresponde();
    }

    private void abrirCrearNota() {
        Intent intent = new Intent(UnidadNotasActivity.this, CrearNotaActivity.class);
        intent.putExtra("id_asignatura", idAsignatura);
        intent.putExtra("id_unidad", idUnidad);
        intent.putExtra("nombre_unidad", nombreUnidad);
        intent.putExtra("orden_unidad", ordenUnidad);
        crearNotaLauncher.launch(intent);
    }

    private void mostrarTourSiCorresponde() {
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 8) {
            return;
        }
        tourMostrado = true;
        fabCrearNotaUnidad.postDelayed(() -> mostrarPasoTour(8), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View target = index == 8 ? (notasUnidad.isEmpty() ? txtEmptyUnidadNotas : listUnidadNotas) : fabCrearNotaUnidad;
        String title = index == 8 ? "Notas de la unidad" : "Añadir nota";
        String message = index == 8
                ? "Aquí se agrupan las notas de esta unidad. Puedes consultarlas y mantener pulsado para editar o eliminar."
                : "Este botón crea una nota nueva ya contextualizada en esta unidad didáctica.";

        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> finish(),
                () -> {
                    if (index == 8) {
                        mostrarPasoTour(9);
                    } else {
                        Intent intent = new Intent(UnidadNotasActivity.this, NotasActivity.class);
                        intent.putExtra("tour_step", 10);
                        startActivity(intent);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private void mostrarOpcionesNota(int position) {
        if (position < 0 || position >= notasUnidad.size()) {
            return;
        }

        NotaListadoResponse nota = notasUnidad.get(position);
        String[] opciones = {"Editar", "Eliminar"};

        new AlertDialog.Builder(this)
                .setTitle("Acciones sobre la nota")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarNota(nota);
                    } else {
                        confirmarEliminacion(nota);
                    }
                })
                .show();
    }

    private void mostrarDialogoEditarNota(NotaListadoResponse nota) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_nota, null);

        EditText edtNuevaCalificacion = dialogView.findViewById(R.id.edtNuevaCalificacion);
        EditText edtNuevaPonderacion = dialogView.findViewById(R.id.edtNuevaPonderacion);
        TextView txtContextoEditarNota = dialogView.findViewById(R.id.txtContextoEditarNota);

        edtNuevaCalificacion.setText(safeText(nota.getValorCalificacion(), ""));
        edtNuevaPonderacion.setText(safeText(nota.getPonderacion(), ""));
        txtContextoEditarNota.setText(getTextoContextoEditarNota(nota));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar nota")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (actualizarNota(nota, edtNuevaCalificacion, edtNuevaPonderacion)) {
                dialog.dismiss();
            }
        }));

        dialog.show();
    }

    private boolean actualizarNota(NotaListadoResponse nota, EditText edtNuevaCalificacion, EditText edtNuevaPonderacion) {
        String valorTexto = edtNuevaCalificacion.getText().toString().trim();
        String ponderacionTexto = edtNuevaPonderacion.getText().toString().trim();

        Double valorCalificacion = parseDecimal(valorTexto);
        Double ponderacion = parseDecimal(ponderacionTexto);

        if (valorCalificacion == null || valorCalificacion < 0 || valorCalificacion > 10) {
            edtNuevaCalificacion.setError("La nota debe estar entre 0 y 10.");
            edtNuevaCalificacion.requestFocus();
            return false;
        }

        if (ponderacion == null || ponderacion < 0 || ponderacion > getPonderacionDisponibleParaNota(nota) + 0.01) {
            edtNuevaPonderacion.setError("La ponderación máxima disponible es " + formatDecimal(getPonderacionDisponibleParaNota(nota)) + "%.");
            edtNuevaPonderacion.requestFocus();
            return false;
        }

        double ponderacionDisponible = getPonderacionDisponibleParaNota(nota);
        if (notasUnidad.size() >= 3 && Math.abs(ponderacion - ponderacionDisponible) > 0.01) {
            edtNuevaPonderacion.setError("Con 3 notas, esta ponderación debe ser " + formatDecimal(ponderacionDisponible) + "%.");
            edtNuevaPonderacion.requestFocus();
            return false;
        }

        setLoading(true);
        apiService.actualizarNota(nota.getIdNota(), new ActualizarNotaRequest(valorCalificacion, ponderacion))
                .enqueue(new Callback<NotaResponse>() {
                    @Override
                    public void onResponse(Call<NotaResponse> call, Response<NotaResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            AppDataCache.invalidateAcademicData();
                            EstadisticasActivity.invalidateCache();
                            Toast.makeText(UnidadNotasActivity.this, "Nota actualizada correctamente.", Toast.LENGTH_SHORT).show();
                            cargarNotas();
                            return;
                        }

                        if (gestionarErrorAutenticacion(response.code())) {
                            return;
                        }

                        Toast.makeText(UnidadNotasActivity.this, ApiErrorUtils.getErrorMessage(response, "No se pudo actualizar la nota."), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<NotaResponse> call, Throwable t) {
                        setLoading(false);
                        ConnectionErrorViewHelper.show(UnidadNotasActivity.this, () -> mostrarDialogoEditarNota(nota));
                    }
                });

        return true;
    }

    private String getTextoContextoEditarNota(NotaListadoResponse nota) {
        double usadaPorOtrasNotas = getPonderacionUsadaPorOtrasNotas(nota);
        double disponible = Math.max(0, 100 - usadaPorOtrasNotas);
        return "Las otras notas de esta unidad suman "
                + formatDecimal(usadaPorOtrasNotas)
                + "%. Disponible para esta nota: "
                + formatDecimal(disponible)
                + "%.";
    }

    private double getPonderacionDisponibleParaNota(NotaListadoResponse nota) {
        return Math.max(0, 100 - getPonderacionUsadaPorOtrasNotas(nota));
    }

    private double getPonderacionUsadaPorOtrasNotas(NotaListadoResponse notaActual) {
        double total = 0;
        for (NotaListadoResponse nota : notasUnidad) {
            if (notaActual.getIdNota() != null && notaActual.getIdNota().equals(nota.getIdNota())) {
                continue;
            }

            Double ponderacion = parseDecimal(safeText(nota.getPonderacion(), "0"));
            if (ponderacion != null) {
                total += ponderacion;
            }
        }
        return total;
    }

    private void confirmarEliminacion(NotaListadoResponse nota) {
        String mensaje = "Seguro que quieres eliminar esta nota?\n\n"
                + "Instrumento: " + safeText(nota.getNombreInstrumento()) + "\n"
                + "Calificación: " + safeText(nota.getValorCalificacion()) + "\n"
                + "Ponderación: " + safeText(nota.getPonderacion()) + "%\n\n"
                + "Esta acción no se puede deshacer.";

        new AlertDialog.Builder(this)
                .setTitle("Eliminar nota")
                .setMessage(mensaje)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarNota(nota))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarNota(NotaListadoResponse nota) {
        setLoading(true);
        apiService.eliminarNota(nota.getIdNota()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    AppDataCache.invalidateAcademicData();
                    EstadisticasActivity.invalidateCache();
                    Toast.makeText(UnidadNotasActivity.this, "Nota eliminada correctamente.", Toast.LENGTH_SHORT).show();
                    cargarNotas();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(UnidadNotasActivity.this, ApiErrorUtils.getErrorMessage(response, "No se pudo eliminar la nota."), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(UnidadNotasActivity.this, () -> eliminarNota(nota));
            }
        });
    }

    private boolean gestionarErrorAutenticacion(int code) {
        if (code == 401 || code == 403) {
            AppDataCache.clearAll();
            EstadisticasActivity.invalidateCache();
            sessionManager.clearSession();
            volverALogin();
            return true;
        }
        return false;
    }

    private String getTitulo() {
        String unidad = safeText(nombreUnidad, "Unidad didáctica");
        if (ordenUnidad > 0) {
            return "Unidad " + ordenUnidad + ": " + unidad;
        }
        return unidad;
    }

    private String safeText(String value) {
        return safeText(value, "-");
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String safeNumber(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private Double parseDecimal(String value) {
        String normalizedValue = value.replace(',', '.');
        if (!normalizedValue.matches("^\\d+(\\.\\d+)?$")) {
            return null;
        }

        try {
            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatDecimal(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        fabCrearNotaUnidad.setEnabled(!isLoading);
    }

    private void volverALogin() {
        Intent intent = new Intent(UnidadNotasActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class UnidadNotasAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return notasUnidad.size();
        }

        @Override
        public NotaListadoResponse getItem(int position) {
            return notasUnidad.get(position);
        }

        @Override
        public long getItemId(int position) {
            NotaListadoResponse nota = getItem(position);
            return nota.getIdNota() == null ? position : nota.getIdNota();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(UnidadNotasActivity.this).inflate(R.layout.list_item_nota, parent, false);
            }

            NotaListadoResponse nota = getItem(position);
            TextView txtNotaAsignatura = view.findViewById(R.id.txtNotaAsignatura);
            TextView txtNotaUnidad = view.findViewById(R.id.txtNotaUnidad);
            TextView txtNotaInstrumento = view.findViewById(R.id.txtNotaInstrumento);
            TextView txtNotaCalificacion = view.findViewById(R.id.txtNotaCalificacion);
            TextView txtNotaPonderacion = view.findViewById(R.id.txtNotaPonderacion);

            txtNotaAsignatura.setText(safeText(nombreAsignatura, safeText(nota.getNombreAsignatura())));
            txtNotaUnidad.setText("Unidad " + safeNumber(nota.getOrdenUnidad()) + ": " + safeText(nota.getNombreUnidad()));
            txtNotaInstrumento.setText("Instrumento: " + safeText(nota.getNombreInstrumento()));
            txtNotaCalificacion.setText("Nota: " + safeText(nota.getValorCalificacion()));
            txtNotaPonderacion.setText("Ponderación: " + safeText(nota.getPonderacion()) + "%");

            return view;
        }
    }
}
