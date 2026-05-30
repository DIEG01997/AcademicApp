package com.example.academicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.AsignaturaResponse;
import com.example.academicapp.api.CrearNotaRequest;
import com.example.academicapp.api.InstrumentoEvaluacionResponse;
import com.example.academicapp.api.NotaListadoResponse;
import com.example.academicapp.api.NotaResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.api.UnidadDidacticaResponse;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearNotaActivity extends AppCompatActivity {

    private TextView txtInfoCrearNota;
    private TextView txtAsignaturaContextoCrearNota;
    private TextView txtUnidadContextoCrearNota;
    private TextView txtLabelAsignaturaCrearNota;
    private TextView txtLabelUnidadCrearNota;
    private Spinner spinnerAsignatura;
    private Spinner spinnerUnidad;
    private Spinner spinnerInstrumento;
    private EditText edtCalificacion;
    private EditText edtPonderacion;
    private ProgressBar progressBarCrearNota;
    private Button btnGuardarNota;

    private SessionManager sessionManager;
    private ApiService apiService;

    private List<AsignaturaResponse> asignaturas = new ArrayList<>();
    private List<UnidadDidacticaResponse> unidades = new ArrayList<>();
    private List<InstrumentoEvaluacionResponse> instrumentos = new ArrayList<>();
    private List<InstrumentoEvaluacionResponse> instrumentosDisponibles = new ArrayList<>();
    private List<NotaListadoResponse> notasExistentes = new ArrayList<>();

    private long idAsignaturaPreseleccionada;
    private long idUnidadPreseleccionada;
    private boolean ignorarEventoAsignatura = false;
    private boolean preseleccionUnidadPendiente = true;
    private boolean ponderacionBloqueadaPorUnidad = false;
    private boolean modoUnidadPreseleccionada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_nota);

        txtInfoCrearNota = findViewById(R.id.txtInfoCrearNota);
        txtAsignaturaContextoCrearNota = findViewById(R.id.txtAsignaturaContextoCrearNota);
        txtUnidadContextoCrearNota = findViewById(R.id.txtUnidadContextoCrearNota);
        txtLabelAsignaturaCrearNota = findViewById(R.id.txtLabelAsignaturaCrearNota);
        txtLabelUnidadCrearNota = findViewById(R.id.txtLabelUnidadCrearNota);
        spinnerAsignatura = findViewById(R.id.spinnerAsignatura);
        spinnerUnidad = findViewById(R.id.spinnerUnidad);
        spinnerInstrumento = findViewById(R.id.spinnerInstrumento);
        edtCalificacion = findViewById(R.id.edtCalificacion);
        edtPonderacion = findViewById(R.id.edtPonderacion);
        progressBarCrearNota = findViewById(R.id.progressBarCrearNota);
        btnGuardarNota = findViewById(R.id.btnGuardarNota);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        idAsignaturaPreseleccionada = getIntent().getLongExtra("id_asignatura", -1L);
        idUnidadPreseleccionada = getIntent().getLongExtra("id_unidad", -1L);
        modoUnidadPreseleccionada = idAsignaturaPreseleccionada > 0 && idUnidadPreseleccionada > 0;
        configurarModoContextual();

        btnGuardarNota.setOnClickListener(v -> guardarNota());

        spinnerAsignatura.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignorarEventoAsignatura || asignaturas.isEmpty()) {
                    return;
                }

                boolean aplicarPreseleccion =
                        preseleccionUnidadPendiente && idUnidadPreseleccionada > 0;

                cargarUnidadesDeAsignaturaSeleccionada(aplicarPreseleccion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se necesita acción.
            }
        });

        spinnerUnidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarContextoUnidadSeleccionada();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se necesita accion.
            }
        });

        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        setLoading(true);
        txtInfoCrearNota.setText(modoUnidadPreseleccionada
                ? "Preparando la nota para esta unidad..."
                : "Cargando datos para crear la nota...");

        cargarAsignaturas();
        cargarInstrumentos();
        cargarNotasExistentes();
    }

    private void cargarAsignaturas() {
        ConnectionErrorViewHelper.hide(this);
        List<AsignaturaResponse> cachedAsignaturas = AppDataCache.getAsignaturas();
        if (cachedAsignaturas != null) {
            asignaturas = cachedAsignaturas;
            poblarSpinnerAsignaturas();
            return;
        }

        apiService.getAsignaturas().enqueue(new Callback<List<AsignaturaResponse>>() {
            @Override
            public void onResponse(Call<List<AsignaturaResponse>> call, Response<List<AsignaturaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(CrearNotaActivity.this);
                    asignaturas = response.body();
                    AppDataCache.setAsignaturas(asignaturas);
                    poblarSpinnerAsignaturas();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                setLoading(false);
                Toast.makeText(CrearNotaActivity.this, "No se pudieron cargar las asignaturas.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<AsignaturaResponse>> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(CrearNotaActivity.this, () -> cargarAsignaturas());
            }
        });
    }

    private void poblarSpinnerAsignaturas() {
        if (asignaturas.isEmpty()) {
            setLoading(false);
            txtInfoCrearNota.setText("No hay asignaturas disponibles para crear una nota.");
            Toast.makeText(this, "No hay asignaturas disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> nombres = new ArrayList<>();

        for (AsignaturaResponse asignatura : asignaturas) {
            nombres.add(safeText(asignatura.getNombreAsignatura()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ignorarEventoAsignatura = true;
        spinnerAsignatura.setAdapter(adapter);

        int posicionPreseleccionada = buscarPosicionAsignatura(idAsignaturaPreseleccionada);
        if (posicionPreseleccionada < 0) {
            posicionPreseleccionada = 0;
        }

        spinnerAsignatura.setSelection(posicionPreseleccionada);
        ignorarEventoAsignatura = false;

        cargarUnidadesDeAsignaturaSeleccionada(true);
    }

    private void cargarUnidadesDeAsignaturaSeleccionada(boolean aplicarPreseleccion) {
        int position = spinnerAsignatura.getSelectedItemPosition();

        if (position < 0 || position >= asignaturas.size()) {
            Toast.makeText(this, "Selecciona una asignatura válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        AsignaturaResponse asignaturaSeleccionada = asignaturas.get(position);

        if (asignaturaSeleccionada.getIdAsignatura() == null) {
            Toast.makeText(this, "No se pudo identificar la asignatura.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        txtInfoCrearNota.setText("Cargando unidades de " + safeText(asignaturaSeleccionada.getNombreAsignatura()) + "...");

        unidades.clear();
        poblarSpinnerUnidadConTexto("Cargando unidades...");

        List<UnidadDidacticaResponse> cachedUnidades = AppDataCache.getUnidades(asignaturaSeleccionada.getIdAsignatura());
        if (cachedUnidades != null) {
            setLoading(false);
            unidades = cachedUnidades;
            poblarSpinnerUnidades(aplicarPreseleccion);
            return;
        }

        apiService.getUnidades(asignaturaSeleccionada.getIdAsignatura()).enqueue(new Callback<List<UnidadDidacticaResponse>>() {
            @Override
            public void onResponse(Call<List<UnidadDidacticaResponse>> call, Response<List<UnidadDidacticaResponse>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    unidades = response.body();
                    AppDataCache.setUnidades(asignaturaSeleccionada.getIdAsignatura(), unidades);
                    poblarSpinnerUnidades(aplicarPreseleccion);
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(CrearNotaActivity.this, "No se pudieron cargar las unidades.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<UnidadDidacticaResponse>> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(CrearNotaActivity.this, () -> cargarUnidadesDeAsignaturaSeleccionada(aplicarPreseleccion));
            }
        });
    }

    private void poblarSpinnerUnidades(boolean aplicarPreseleccion) {
        if (unidades.isEmpty()) {
            poblarSpinnerUnidadConTexto("No hay unidades disponibles");
            txtInfoCrearNota.setText("La asignatura seleccionada no tiene unidades disponibles.");
            actualizarContextoUnidadSeleccionada();
            return;
        }

        List<String> nombres = new ArrayList<>();

        for (UnidadDidacticaResponse unidad : unidades) {
            String textoUnidad = "Unidad "
                    + safeNumber(unidad.getOrdenUnidad())
                    + ": "
                    + safeText(unidad.getNombreUnidad());

            nombres.add(textoUnidad);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidad.setAdapter(adapter);

        if (aplicarPreseleccion && idUnidadPreseleccionada > 0) {
            int posicionUnidad = buscarPosicionUnidad(idUnidadPreseleccionada);

            if (posicionUnidad >= 0) {
                spinnerUnidad.setSelection(posicionUnidad);
                preseleccionUnidadPendiente = false;
            }
        }

        actualizarContextoVisual();
        txtInfoCrearNota.setText(modoUnidadPreseleccionada
                ? "Completa la calificación, la ponderación y guarda la nota."
                : "Selecciona los datos de la nota y pulsa Guardar.");
        actualizarContextoUnidadSeleccionada();
    }

    private void configurarModoContextual() {
        int selectorVisibility = modoUnidadPreseleccionada ? View.GONE : View.VISIBLE;
        int contextVisibility = modoUnidadPreseleccionada ? View.VISIBLE : View.GONE;

        txtAsignaturaContextoCrearNota.setVisibility(contextVisibility);
        txtUnidadContextoCrearNota.setVisibility(contextVisibility);
        txtLabelAsignaturaCrearNota.setVisibility(selectorVisibility);
        spinnerAsignatura.setVisibility(selectorVisibility);
        txtLabelUnidadCrearNota.setVisibility(selectorVisibility);
        spinnerUnidad.setVisibility(selectorVisibility);
    }

    private void actualizarContextoVisual() {
        if (!modoUnidadPreseleccionada) {
            return;
        }

        int posicionAsignatura = spinnerAsignatura.getSelectedItemPosition();
        if (posicionAsignatura >= 0 && posicionAsignatura < asignaturas.size()) {
            txtAsignaturaContextoCrearNota.setText(safeText(asignaturas.get(posicionAsignatura).getNombreAsignatura()));
        }

        int posicionUnidad = spinnerUnidad.getSelectedItemPosition();
        if (posicionUnidad >= 0 && posicionUnidad < unidades.size()) {
            UnidadDidacticaResponse unidad = unidades.get(posicionUnidad);
            txtUnidadContextoCrearNota.setText(
                    "Unidad "
                            + safeNumber(unidad.getOrdenUnidad())
                            + ": "
                            + safeText(unidad.getNombreUnidad())
            );
        }
    }

    private void poblarSpinnerUnidadConTexto(String texto) {
        List<String> items = new ArrayList<>();
        items.add(texto);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidad.setAdapter(adapter);
    }

    private void cargarInstrumentos() {
        List<InstrumentoEvaluacionResponse> cachedInstrumentos = AppDataCache.getInstrumentos();
        if (cachedInstrumentos != null) {
            instrumentos = cachedInstrumentos;
            actualizarContextoUnidadSeleccionada();
            return;
        }

        apiService.getInstrumentos().enqueue(new Callback<List<InstrumentoEvaluacionResponse>>() {
            @Override
            public void onResponse(Call<List<InstrumentoEvaluacionResponse>> call, Response<List<InstrumentoEvaluacionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    instrumentos = response.body();
                    AppDataCache.setInstrumentos(instrumentos);
                    actualizarContextoUnidadSeleccionada();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(CrearNotaActivity.this, "No se pudieron cargar los instrumentos.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<InstrumentoEvaluacionResponse>> call, Throwable t) {
                ConnectionErrorViewHelper.show(CrearNotaActivity.this, () -> cargarInstrumentos());
            }
        });
    }

    private void cargarNotasExistentes() {
        List<NotaListadoResponse> cachedNotas = AppDataCache.getNotas();
        if (cachedNotas != null) {
            notasExistentes = cachedNotas;
            actualizarContextoUnidadSeleccionada();
            return;
        }

        apiService.getNotas().enqueue(new Callback<List<NotaListadoResponse>>() {
            @Override
            public void onResponse(Call<List<NotaListadoResponse>> call, Response<List<NotaListadoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notasExistentes = response.body();
                    AppDataCache.setNotas(notasExistentes);
                    actualizarContextoUnidadSeleccionada();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(CrearNotaActivity.this, "No se pudieron cargar las notas existentes.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<NotaListadoResponse>> call, Throwable t) {
                ConnectionErrorViewHelper.show(CrearNotaActivity.this, () -> cargarNotasExistentes());
            }
        });
    }

    private void poblarSpinnerInstrumentos(List<InstrumentoEvaluacionResponse> instrumentosParaMostrar) {
        if (instrumentosParaMostrar.isEmpty()) {
            List<String> vacio = new ArrayList<>();
            vacio.add("No hay instrumentos disponibles");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    vacio
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerInstrumento.setAdapter(adapter);
            return;
        }

        List<String> nombres = new ArrayList<>();

        for (InstrumentoEvaluacionResponse instrumento : instrumentosParaMostrar) {
            nombres.add(safeText(instrumento.getNombreInstrumento()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nombres
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstrumento.setAdapter(adapter);
    }

    private void actualizarContextoUnidadSeleccionada() {
        int posicionUnidad = spinnerUnidad.getSelectedItemPosition();

        if (posicionUnidad < 0 || posicionUnidad >= unidades.size()) {
            instrumentosDisponibles = new ArrayList<>(instrumentos);
            poblarSpinnerInstrumentos(instrumentosDisponibles);
            ponderacionBloqueadaPorUnidad = false;
            edtPonderacion.setEnabled(!progressBarCrearNota.isShown());
            btnGuardarNota.setEnabled(!progressBarCrearNota.isShown());
            return;
        }

        UnidadDidacticaResponse unidadSeleccionada = unidades.get(posicionUnidad);
        List<NotaListadoResponse> notasUnidad = getNotasDeUnidad(unidadSeleccionada.getIdUnidad());
        Double ponderacionUsada = calcularPonderacionUsada(notasUnidad);
        double restante = Math.max(0, 100 - ponderacionUsada);

        instrumentosDisponibles = filtrarInstrumentosDisponibles(notasUnidad);
        poblarSpinnerInstrumentos(instrumentosDisponibles);

        if (notasUnidad.size() >= 3 || instrumentosDisponibles.isEmpty()) {
            ponderacionBloqueadaPorUnidad = false;
            edtPonderacion.setEnabled(false);
            btnGuardarNota.setEnabled(false);
            txtInfoCrearNota.setText("Esta unidad ya tiene las tres notas registradas. Puedes editarlas desde el listado de la unidad.");
            return;
        }

        if (notasUnidad.size() == 2) {
            ponderacionBloqueadaPorUnidad = true;
            edtPonderacion.setText(formatDecimal(restante));
            edtPonderacion.setEnabled(false);
            txtInfoCrearNota.setText("Queda " + formatDecimal(restante) + "% de ponderación. La última nota usará ese porcentaje.");
        } else {
            ponderacionBloqueadaPorUnidad = false;
            edtPonderacion.setEnabled(true);
            txtInfoCrearNota.setText(
                    "Ponderación usada: "
                            + formatDecimal(ponderacionUsada)
                            + "%. Puedes repartir hasta "
                            + formatDecimal(restante)
                            + "%."
            );
        }

        btnGuardarNota.setEnabled(true);
    }

    private List<NotaListadoResponse> getNotasDeUnidad(Long idUnidad) {
        List<NotaListadoResponse> notasUnidad = new ArrayList<>();
        if (idUnidad == null) {
            return notasUnidad;
        }

        for (NotaListadoResponse nota : notasExistentes) {
            if (idUnidad.equals(nota.getIdUnidad())) {
                notasUnidad.add(nota);
            }
        }

        return notasUnidad;
    }

    private Double calcularPonderacionUsada(List<NotaListadoResponse> notasUnidad) {
        double total = 0;
        for (NotaListadoResponse nota : notasUnidad) {
            Double ponderacion = parseDecimal(safeText(nota.getPonderacion(), "0"));
            if (ponderacion != null) {
                total += ponderacion;
            }
        }
        return total;
    }

    private List<InstrumentoEvaluacionResponse> filtrarInstrumentosDisponibles(List<NotaListadoResponse> notasUnidad) {
        List<InstrumentoEvaluacionResponse> disponibles = new ArrayList<>();

        for (InstrumentoEvaluacionResponse instrumento : instrumentos) {
            boolean usado = false;
            for (NotaListadoResponse nota : notasUnidad) {
                if (instrumento.getIdInstrumento() != null
                        && instrumento.getIdInstrumento().equals(nota.getIdInstrumento())) {
                    usado = true;
                    break;
                }
            }

            if (!usado) {
                disponibles.add(instrumento);
            }
        }

        return disponibles;
    }

    private void guardarNota() {
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        if (asignaturas.isEmpty()) {
            Toast.makeText(this, "No hay asignaturas disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unidades.isEmpty()) {
            Toast.makeText(this, "Selecciona una unidad válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (instrumentosDisponibles.isEmpty()) {
            Toast.makeText(this, "No hay instrumentos disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }

        int posicionUnidad = spinnerUnidad.getSelectedItemPosition();
        int posicionInstrumento = spinnerInstrumento.getSelectedItemPosition();

        if (posicionUnidad < 0 || posicionUnidad >= unidades.size()) {
            Toast.makeText(this, "Selecciona una unidad válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (posicionInstrumento < 0 || posicionInstrumento >= instrumentosDisponibles.size()) {
            Toast.makeText(this, "Selecciona un instrumento válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String valorTexto = edtCalificacion.getText().toString().trim();
        String ponderacionTexto = edtPonderacion.getText().toString().trim();

        if (valorTexto.isEmpty() || ponderacionTexto.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Double valorCalificacion = parseDecimal(valorTexto);
        Double ponderacion = parseDecimal(ponderacionTexto);

        if (valorCalificacion == null) {
            edtCalificacion.setError("Introduce un valor numérico válido.");
            edtCalificacion.requestFocus();
            return;
        }

        if (ponderacion == null) {
            edtPonderacion.setError("Introduce un valor numérico válido.");
            edtPonderacion.requestFocus();
            return;
        }

        if (valorCalificacion < 0 || valorCalificacion > 10) {
            edtCalificacion.setError("La nota debe estar entre 0 y 10.");
            edtCalificacion.requestFocus();
            return;
        }

        if (ponderacion < 0 || ponderacion > 100) {
            edtPonderacion.setError("La ponderación debe estar entre 0 y 100.");
            edtPonderacion.requestFocus();
            return;
        }

        UnidadDidacticaResponse unidadSeleccionada = unidades.get(posicionUnidad);
        List<NotaListadoResponse> notasUnidad = getNotasDeUnidad(unidadSeleccionada.getIdUnidad());
        double ponderacionDisponible = Math.max(0, 100 - calcularPonderacionUsada(notasUnidad));

        if (ponderacion > ponderacionDisponible + 0.01) {
            edtPonderacion.setError("La ponderación máxima disponible es " + formatDecimal(ponderacionDisponible) + "%.");
            edtPonderacion.requestFocus();
            return;
        }

        InstrumentoEvaluacionResponse instrumentoSeleccionado = instrumentosDisponibles.get(posicionInstrumento);

        CrearNotaRequest request = new CrearNotaRequest(
                unidadSeleccionada.getIdUnidad(),
                instrumentoSeleccionado.getIdInstrumento(),
                valorCalificacion,
                ponderacion
        );

        setLoading(true);

        apiService.crearNota(request).enqueue(new Callback<NotaResponse>() {
            @Override
            public void onResponse(Call<NotaResponse> call, Response<NotaResponse> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    AppDataCache.invalidateAcademicData();
                    EstadisticasActivity.invalidateCache();
                    Toast.makeText(CrearNotaActivity.this, "Nota creada correctamente.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(
                        CrearNotaActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo crear la nota."),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<NotaResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(CrearNotaActivity.this, () -> guardarNota());
            }
        });
    }

    private int buscarPosicionAsignatura(long idAsignatura) {
        if (idAsignatura <= 0) {
            return -1;
        }

        for (int i = 0; i < asignaturas.size(); i++) {
            Long idActual = asignaturas.get(i).getIdAsignatura();
            if (idActual != null && idActual == idAsignatura) {
                return i;
            }
        }

        return -1;
    }

    private int buscarPosicionUnidad(long idUnidad) {
        if (idUnidad <= 0) {
            return -1;
        }

        for (int i = 0; i < unidades.size(); i++) {
            Long idActual = unidades.get(i).getIdUnidad();
            if (idActual != null && idActual == idUnidad) {
                return i;
            }
        }

        return -1;
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

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String safeNumber(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
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

    private void setLoading(boolean isLoading) {
        progressBarCrearNota.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        btnGuardarNota.setEnabled(!isLoading);
        spinnerAsignatura.setEnabled(!isLoading);
        spinnerUnidad.setEnabled(!isLoading);
        spinnerInstrumento.setEnabled(!isLoading);
        edtCalificacion.setEnabled(!isLoading);
        edtPonderacion.setEnabled(!isLoading && !ponderacionBloqueadaPorUnidad);
    }

    private void volverALogin() {
        Intent intent = new Intent(CrearNotaActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
