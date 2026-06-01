package com.example.academicapp.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.AsignaturaResponse;
import com.example.academicapp.api.DashboardResponse;
import com.example.academicapp.api.DashboardTrimestreResponse;
import com.example.academicapp.api.NotaListadoResponse;
import com.example.academicapp.api.PercentilGlobalResponse;
import com.example.academicapp.api.PerfilAlumnoResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de la app Android encargada de gestionar la pantalla NotasActivity y coordinar su interfaz con la API.
 */
public class NotasActivity extends AppCompatActivity {

    private static final int EVALUACION_FINAL = 4;
    private static final int PERCENTILE_COLOR = Color.rgb(124, 58, 237);
    private static final String PROFILE_PREFS = "academic_app_profile";
    private static final String KEY_PROFILE_PHOTO = "profile_photo_path";

    private TextView txtEmptyEvaluaciones;
    private LinearLayout layoutEvaluaciones;
    private ProgressBar progressBar;
    private Button btnDescargarBoletin;
    private BottomNavigationView bottomNavigation;
    private MaterialButtonToggleGroup toggleEvaluacion;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<AsignaturaResponse> asignaturasCargadas = new ArrayList<>();
    private List<NotaListadoResponse> notasCargadas = new ArrayList<>();
    private int evaluacionSeleccionada = 1;
    private boolean firstLoad = true;
    private boolean tourMostrado;
    private boolean actualizandoToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        txtEmptyEvaluaciones = findViewById(R.id.txtEmptyEvaluaciones);
        layoutEvaluaciones = findViewById(R.id.layoutEvaluaciones);
        progressBar = findViewById(R.id.progressBarNotas);
        btnDescargarBoletin = findViewById(R.id.btnDescargarBoletin);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toggleEvaluacion = findViewById(R.id.toggleEvaluacion);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        NavigationHelper.setup(this, bottomNavigation, R.id.nav_notes);
        toggleEvaluacion.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || actualizandoToggle) {
                return;
            }

            if (checkedId == R.id.btnEvaluacion1) {
                evaluacionSeleccionada = 1;
            } else if (checkedId == R.id.btnEvaluacion2) {
                evaluacionSeleccionada = 2;
            } else if (checkedId == R.id.btnEvaluacion3) {
                evaluacionSeleccionada = 3;
            } else if (checkedId == R.id.btnEvaluacionFinal) {
                evaluacionSeleccionada = EVALUACION_FINAL;
            }

            mostrarEvaluaciones();
        });
        btnDescargarBoletin.setOnClickListener(v -> descargarBoletinPdf());

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
        setLoading(true);
        final boolean[] asignaturasListas = {false};
        final boolean[] notasListas = {false};
        final boolean[] hayError = {false};

        List<AsignaturaResponse> cachedAsignaturas = AppDataCache.getAsignaturas();
        if (cachedAsignaturas != null) {
            asignaturasCargadas = cachedAsignaturas;
            asignaturasListas[0] = true;
        }

        List<NotaListadoResponse> cachedNotas = AppDataCache.getNotas();
        if (cachedNotas != null) {
            notasCargadas = cachedNotas;
            notasListas[0] = true;
        }

        finalizarCargaEvaluacionSiProcede(asignaturasListas, notasListas, hayError);

        if (!asignaturasListas[0]) {
        apiService.getAsignaturas().enqueue(new Callback<List<AsignaturaResponse>>() {
            @Override
            public void onResponse(Call<List<AsignaturaResponse>> call, Response<List<AsignaturaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(NotasActivity.this);
                    asignaturasCargadas = response.body();
                    AppDataCache.setAsignaturas(asignaturasCargadas);
                    asignaturasListas[0] = true;
                    finalizarCargaEvaluacionSiProcede(asignaturasListas, notasListas, hayError);
                    return;
                }

                hayError[0] = true;
                setLoading(false);
                if (!gestionarErrorAutenticacion(response.code())) {
                    Toast.makeText(NotasActivity.this, "No se pudieron cargar las asignaturas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AsignaturaResponse>> call, Throwable t) {
                hayError[0] = true;
                setLoading(false);
                ConnectionErrorViewHelper.show(NotasActivity.this, () -> cargarNotas());
            }
        });
        }

        if (!notasListas[0]) {
        apiService.getNotas().enqueue(new Callback<List<NotaListadoResponse>>() {
            @Override
            public void onResponse(Call<List<NotaListadoResponse>> call, Response<List<NotaListadoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(NotasActivity.this);
                    notasCargadas = response.body();
                    AppDataCache.setNotas(notasCargadas);
                    notasListas[0] = true;
                    finalizarCargaEvaluacionSiProcede(asignaturasListas, notasListas, hayError);
                    return;
                }

                hayError[0] = true;
                setLoading(false);
                if (!gestionarErrorAutenticacion(response.code())) {
                    Toast.makeText(NotasActivity.this, "No se pudieron cargar las notas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<NotaListadoResponse>> call, Throwable t) {
                hayError[0] = true;
                setLoading(false);
                ConnectionErrorViewHelper.show(NotasActivity.this, () -> cargarNotas());
            }
        });
        }
    }

    private void finalizarCargaEvaluacionSiProcede(boolean[] asignaturasListas, boolean[] notasListas, boolean[] hayError) {
        if (hayError[0] || !asignaturasListas[0] || !notasListas[0]) {
            return;
        }

        setLoading(false);
        mostrarEvaluaciones();
    }

    private void mostrarEvaluaciones() {
        limpiarEvaluaciones();

        actualizarVisibilidadEvaluacionFinal();
        List<EvaluacionAsignatura> evaluaciones = calcularEvaluaciones();
        if (evaluaciones.isEmpty()) {
            txtEmptyEvaluaciones.setVisibility(View.VISIBLE);
            return;
        }

        txtEmptyEvaluaciones.setVisibility(View.GONE);
        for (EvaluacionAsignatura evaluacion : evaluaciones) {
            layoutEvaluaciones.addView(crearVistaEvaluacion(evaluacion), layoutEvaluaciones.getChildCount() - 1);
        }
        mostrarTourSiCorresponde();
    }

    private void actualizarVisibilidadEvaluacionFinal() {
        View btnFinal = findViewById(R.id.btnEvaluacionFinal);
        boolean finalDisponible = tieneEvaluacionFinalDisponible();
        btnFinal.setVisibility(finalDisponible ? View.VISIBLE : View.GONE);

        if (!finalDisponible && evaluacionSeleccionada == EVALUACION_FINAL) {
            evaluacionSeleccionada = 1;
            actualizandoToggle = true;
            toggleEvaluacion.check(R.id.btnEvaluacion1);
            actualizandoToggle = false;
        }
    }

    private void limpiarEvaluaciones() {
        while (layoutEvaluaciones.getChildCount() > 4) {
            layoutEvaluaciones.removeViewAt(3);
        }
    }

    private View crearVistaEvaluacion(EvaluacionAsignatura evaluacion) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(12), 0, 0);

        TextView txtAsignatura = new TextView(this);
        txtAsignatura.setText(evaluacion.nombreAsignatura);
        txtAsignatura.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        txtAsignatura.setTextSize(15);
        txtAsignatura.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView txtNota = new TextView(this);
        Integer notaRedondeada = evaluacion.tieneNotas ? Math.round((float) evaluacion.notaFinal) : null;
        txtNota.setText(notaRedondeada == null ? "" : String.valueOf(notaRedondeada));
        txtNota.setGravity(android.view.Gravity.CENTER);
        txtNota.setTextColor(getColorTextoBadge(notaRedondeada));
        txtNota.setTextSize(16);
        txtNota.setTypeface(null, Typeface.BOLD);
        txtNota.setBackground(crearFondoBadge(notaRedondeada));

        int badgeSize = dpToPx(44);
        txtNota.setLayoutParams(new LinearLayout.LayoutParams(badgeSize, badgeSize));

        row.addView(txtAsignatura);
        row.addView(txtNota);
        txtNota.setScaleX(0f);
        txtNota.setScaleY(0f);
        txtNota.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(320)
                .setStartDelay(80)
                .start();
        return row;
    }

    private List<EvaluacionAsignatura> calcularEvaluaciones() {
        if (evaluacionSeleccionada == EVALUACION_FINAL) {
            return calcularEvaluacionesFinales();
        }
        return calcularEvaluacionesPorNumero(evaluacionSeleccionada);
    }

    private List<EvaluacionAsignatura> calcularEvaluacionesPorNumero(int numeroEvaluacionSeleccionada) {
        Map<Long, EvaluacionAsignatura> evaluacionesPorAsignatura = new LinkedHashMap<>();

        if (asignaturasCargadas == null || asignaturasCargadas.isEmpty()) {
            return new ArrayList<>();
        }

        for (AsignaturaResponse asignatura : asignaturasCargadas) {
            if (asignatura.getIdAsignatura() != null) {
                evaluacionesPorAsignatura.put(
                        asignatura.getIdAsignatura(),
                        new EvaluacionAsignatura(safeText(asignatura.getNombreAsignatura()))
                );
            }
        }

        if (notasCargadas == null) {
            return new ArrayList<>(evaluacionesPorAsignatura.values());
        }

        for (NotaListadoResponse nota : notasCargadas) {
            Long idAsignatura = nota.getIdAsignatura();
            Long idUnidad = nota.getIdUnidad();
            Double valor = parseDecimal(safeText(nota.getValorCalificacion(), ""));
            Double ponderacion = parseDecimal(safeText(nota.getPonderacion(), ""));
            Integer numeroEvaluacion = nota.getNumeroEvaluacion();

            if (idAsignatura == null || idUnidad == null || valor == null || ponderacion == null
                    || numeroEvaluacion == null || numeroEvaluacion != numeroEvaluacionSeleccionada) {
                continue;
            }

            EvaluacionAsignatura evaluacion = evaluacionesPorAsignatura.get(idAsignatura);
            if (evaluacion == null) {
                evaluacion = new EvaluacionAsignatura(safeText(nota.getNombreAsignatura()));
                evaluacionesPorAsignatura.put(idAsignatura, evaluacion);
            }

            EvaluacionUnidad unidad = evaluacion.unidades.get(idUnidad);
            if (unidad == null) {
                unidad = new EvaluacionUnidad();
                evaluacion.unidades.put(idUnidad, unidad);
            }

            unidad.tieneNotas = true;
            unidad.notaAcumulada += valor * (ponderacion / 100);
        }

        List<EvaluacionAsignatura> evaluaciones = new ArrayList<>();
        for (EvaluacionAsignatura evaluacion : evaluacionesPorAsignatura.values()) {
            double total = 0;
            int unidadesConNotas = 0;
            for (EvaluacionUnidad unidad : evaluacion.unidades.values()) {
                if (unidad.tieneNotas) {
                    total += unidad.notaAcumulada;
                    unidadesConNotas++;
                }
            }

            evaluacion.tieneNotas = unidadesConNotas > 0;
            if (evaluacion.tieneNotas) {
                evaluacion.notaFinal = total / unidadesConNotas;
            }

            evaluaciones.add(evaluacion);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            evaluaciones.sort(Comparator
                    .comparing((EvaluacionAsignatura evaluacion) -> !evaluacion.tieneNotas)
                    .thenComparing(
                            evaluacion -> evaluacion.tieneNotas ? evaluacion.notaFinal : Double.NEGATIVE_INFINITY,
                            Comparator.reverseOrder()
                    ));
        }

        return evaluaciones;
    }

    private List<EvaluacionAsignatura> calcularEvaluacionesFinales() {
        if (asignaturasCargadas == null || asignaturasCargadas.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, EvaluacionAsignatura> finalesPorAsignatura = new LinkedHashMap<>();
        List<EvaluacionAsignatura> evaluacion1 = calcularEvaluacionesPorNumero(1);
        List<EvaluacionAsignatura> evaluacion2 = calcularEvaluacionesPorNumero(2);
        List<EvaluacionAsignatura> evaluacion3 = calcularEvaluacionesPorNumero(3);

        for (AsignaturaResponse asignatura : asignaturasCargadas) {
            String nombre = safeText(asignatura.getNombreAsignatura());
            EvaluacionAsignatura primera = buscarEvaluacionPorNombre(evaluacion1, nombre);
            EvaluacionAsignatura segunda = buscarEvaluacionPorNombre(evaluacion2, nombre);
            EvaluacionAsignatura tercera = buscarEvaluacionPorNombre(evaluacion3, nombre);

            if (primera == null || segunda == null || tercera == null
                    || !primera.tieneNotas || !segunda.tieneNotas || !tercera.tieneNotas) {
                continue;
            }

            EvaluacionAsignatura finalAsignatura = new EvaluacionAsignatura(nombre);
            finalAsignatura.tieneNotas = true;
            finalAsignatura.notaFinal = (primera.notaFinal + segunda.notaFinal + tercera.notaFinal) / 3;
            finalesPorAsignatura.put(nombre, finalAsignatura);
        }

        List<EvaluacionAsignatura> finales = new ArrayList<>(finalesPorAsignatura.values());
        ordenarEvaluaciones(finales);
        return finales;
    }

    private boolean tieneEvaluacionFinalDisponible() {
        if (asignaturasCargadas == null || asignaturasCargadas.isEmpty()) {
            return false;
        }

        List<EvaluacionAsignatura> evaluacion1 = calcularEvaluacionesPorNumero(1);
        List<EvaluacionAsignatura> evaluacion2 = calcularEvaluacionesPorNumero(2);
        List<EvaluacionAsignatura> evaluacion3 = calcularEvaluacionesPorNumero(3);
        for (AsignaturaResponse asignatura : asignaturasCargadas) {
            String nombre = safeText(asignatura.getNombreAsignatura());
            EvaluacionAsignatura primera = buscarEvaluacionPorNombre(evaluacion1, nombre);
            EvaluacionAsignatura segunda = buscarEvaluacionPorNombre(evaluacion2, nombre);
            EvaluacionAsignatura tercera = buscarEvaluacionPorNombre(evaluacion3, nombre);

            if (primera == null || segunda == null || tercera == null
                    || !primera.tieneNotas || !segunda.tieneNotas || !tercera.tieneNotas) {
                return false;
            }
        }

        return true;
    }

    private EvaluacionAsignatura buscarEvaluacionPorNombre(List<EvaluacionAsignatura> evaluaciones, String nombreAsignatura) {
        for (EvaluacionAsignatura evaluacion : evaluaciones) {
            if (evaluacion.nombreAsignatura.equals(nombreAsignatura)) {
                return evaluacion;
            }
        }
        return null;
    }

    private void ordenarEvaluaciones(List<EvaluacionAsignatura> evaluaciones) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            evaluaciones.sort(Comparator
                    .comparing((EvaluacionAsignatura evaluacion) -> !evaluacion.tieneNotas)
                    .thenComparing(
                            evaluacion -> evaluacion.tieneNotas ? evaluacion.notaFinal : Double.NEGATIVE_INFINITY,
                            Comparator.reverseOrder()
                    ));
        }
    }

    private GradientDrawable crearFondoBadge(Integer nota) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setStroke(dpToPx(2), ContextCompat.getColor(this, R.color.surfaceTintColor));
        drawable.setColor(getColorBadge(nota));
        return drawable;
    }

    private int getColorBadge(Integer nota) {
        if (nota == null) {
            return Color.TRANSPARENT;
        }
        if (nota < 5) {
            return Color.parseColor("#FCA5A5");
        }
        if (nota < 7) {
            return Color.parseColor("#FDE68A");
        }
        if (nota < 9) {
            return Color.parseColor("#93C5FD");
        }
        return Color.parseColor("#86EFAC");
    }

    private int getColorTextoBadge(Integer nota) {
        if (nota == null) {
            return ContextCompat.getColor(this, R.color.textColorPrimary);
        }
        if (nota < 5) {
            return Color.parseColor("#7F1D1D");
        }
        if (nota < 7) {
            return Color.parseColor("#78350F");
        }
        if (nota < 9) {
            return Color.parseColor("#1E3A8A");
        }
        return Color.parseColor("#14532D");
    }

    private void descargarBoletinPdf() {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        setLoading(true);
        apiService.getPerfil().enqueue(new Callback<PerfilAlumnoResponse>() {
            @Override
            public void onResponse(Call<PerfilAlumnoResponse> call, Response<PerfilAlumnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cargarDatosVisualesBoletin(response.body());
                    return;
                }

                setLoading(false);

                if (!gestionarErrorAutenticacion(response.code())) {
                    Toast.makeText(NotasActivity.this, "No se pudo cargar el perfil para el boletín.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilAlumnoResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(NotasActivity.this, () -> descargarBoletinPdf());
            }
        });
    }

    private void cargarDatosVisualesBoletin(PerfilAlumnoResponse perfil) {
        apiService.getDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                DashboardResponse dashboard = response.isSuccessful() ? response.body() : null;
                cargarPercentilBoletin(perfil, dashboard);
            }

            @Override
            public void onFailure(Call<DashboardResponse> call, Throwable t) {
                cargarPercentilBoletin(perfil, null);
            }
        });
    }

    private void cargarPercentilBoletin(PerfilAlumnoResponse perfil, DashboardResponse dashboard) {
        apiService.getPercentilGlobal().enqueue(new Callback<PercentilGlobalResponse>() {
            @Override
            public void onResponse(Call<PercentilGlobalResponse> call, Response<PercentilGlobalResponse> response) {
                setLoading(false);
                PercentilGlobalResponse percentil = response.isSuccessful() ? response.body() : null;
                generarBoletinPdf(perfil, dashboard, percentil);
            }

            @Override
            public void onFailure(Call<PercentilGlobalResponse> call, Throwable t) {
                setLoading(false);
                generarBoletinPdf(perfil, dashboard, null);
            }
        });
    }

    private void generarBoletinPdf(PerfilAlumnoResponse perfil, DashboardResponse dashboard, PercentilGlobalResponse percentilGlobal) {
        List<EvaluacionAsignatura> evaluaciones = calcularEvaluaciones();
        EstadisticasEvaluacion estadisticas = calcularEstadisticas(evaluaciones);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        titlePaint.setTextSize(22);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint sectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionPaint.setColor(ContextCompat.getColor(this, R.color.primaryColor));
        sectionPaint.setTextSize(14);
        sectionPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        bodyPaint.setTextSize(11);

        Paint mutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mutedPaint.setColor(ContextCompat.getColor(this, R.color.textColorSecondary));
        mutedPaint.setTextSize(10);

        Bitmap photo = cargarFotoPerfilBitmap(perfil);
        String photoInitials = obtenerInicialesAlumno(perfil);
        String detalleEvaluacion = buildDetalleEvaluacionPdf(dashboard);
        PdfDocument document = new PdfDocument();

        class PdfFlow {
            private PdfDocument.Page page;
            private Canvas canvas;
            private int pageNumber = 0;
            private int y;
            private int cardBottom;

            void nuevaPagina() {
                if (page != null) {
                    cerrarPaginaActual();
                }

                pageNumber++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = iniciarPaginaBoletinPdf(canvas, paint, titlePaint, mutedPaint, bodyPaint, photo, photoInitials, detalleEvaluacion, pageNumber);
            }

            void asegurarEspacio(int cardHeight) {
                if (y + cardHeight > 780) {
                    nuevaPagina();
                }
            }

            void dibujarTarjeta(int cardHeight) {
                cardBottom = y + cardHeight;
                dibujarTarjetaPdf(canvas, y, cardBottom, paint);
                y += 24;
            }

            void cerrarTarjeta() {
                y = Math.max(y + 10, cardBottom + 24);
            }

            void cerrarPaginaActual() {
                dibujarFooterBoletinPdf(canvas, mutedPaint, pageNumber);
                document.finishPage(page);
                page = null;
            }
        }

        PdfFlow flow = new PdfFlow();
        flow.nuevaPagina();

        flow.asegurarEspacio(154);
        flow.dibujarTarjeta(154);
        flow.y = dibujarSeccion(flow.canvas, "Datos del alumno", flow.y, sectionPaint);
        flow.y = dibujarLinea(flow.canvas, "Nombre", safeText(perfil.getNombre()) + " " + safeText(perfil.getApellidos(), ""), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Correo", safeText(perfil.getCorreoEducativo()), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Curso", buildCursoText(perfil), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Fecha de nacimiento", formatFechaNatural(perfil.getFechaNacimiento()), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Teléfono", safeText(perfil.getTelefono(), "No indicado"), flow.y, bodyPaint, mutedPaint);

        flow.cerrarTarjeta();
        flow.asegurarEspacio(178);
        flow.dibujarTarjeta(178);
        flow.y = dibujarResumenVisualGlobalPdf(flow.canvas, dashboard, percentilGlobal, flow.y, paint, sectionPaint, bodyPaint, mutedPaint);

        flow.cerrarTarjeta();
        if (evaluacionSeleccionada == EVALUACION_FINAL) {
            flow.asegurarEspacio(150);
            flow.dibujarTarjeta(150);
            flow.y = dibujarEvolucionTrimestresPdf(flow.canvas, dashboard, flow.y, paint, sectionPaint, bodyPaint, mutedPaint);
            flow.cerrarTarjeta();
        }

        int index = 0;
        while (index < evaluaciones.size()) {
            int rowsThatFit = Math.max(1, Math.min(evaluaciones.size() - index, (780 - flow.y - 88) / 30));
            int cardHeight = 70 + rowsThatFit * 30;
            flow.asegurarEspacio(cardHeight);
            rowsThatFit = Math.max(1, Math.min(evaluaciones.size() - index, (780 - flow.y - 88) / 30));
            cardHeight = 70 + rowsThatFit * 30;
            flow.dibujarTarjeta(cardHeight);
            String tituloAsignaturas = evaluacionSeleccionada == EVALUACION_FINAL ? "Asignaturas finales" : "Asignaturas evaluadas";
            flow.y = dibujarSeccion(flow.canvas, index == 0 ? tituloAsignaturas : tituloAsignaturas + " (continuación)", flow.y, sectionPaint);

            for (int i = 0; i < rowsThatFit; i++) {
                EvaluacionAsignatura evaluacion = evaluaciones.get(index + i);
                Integer nota = evaluacion.tieneNotas ? Math.round((float) evaluacion.notaFinal) : null;
                dibujarBadgePdf(flow.canvas, nota, 492, flow.y - 13, 22, paint, bodyPaint);
                flow.canvas.drawText(acortarTexto(evaluacion.nombreAsignatura, 34), 52, flow.y, bodyPaint);
                flow.canvas.drawText(evaluacion.tieneNotas ? formatDecimal(evaluacion.notaFinal) : "Sin notas registradas", 330, flow.y, mutedPaint);
                flow.y += 30;
            }

            index += rowsThatFit;
            flow.cerrarTarjeta();
        }

        flow.asegurarEspacio(172);
        flow.dibujarTarjeta(172);
        flow.y = dibujarSeccion(flow.canvas, evaluacionSeleccionada == EVALUACION_FINAL ? "Estadísticas finales" : "Estadísticas de la evaluación", flow.y, sectionPaint);
        flow.y = dibujarLinea(flow.canvas, "Media", estadisticas.asignaturasConNota == 0 ? "-" : formatDecimal(estadisticas.media), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Asignaturas con nota", String.valueOf(estadisticas.asignaturasConNota), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Aprobadas", String.valueOf(estadisticas.aprobadas), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Suspensas", String.valueOf(estadisticas.suspensas), flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Mejor resultado", estadisticas.mejorAsignatura, flow.y, bodyPaint, mutedPaint);
        flow.y = dibujarLinea(flow.canvas, "Resultado más bajo", estadisticas.peorAsignatura, flow.y, bodyPaint, mutedPaint);

        flow.cerrarPaginaActual();

        try {
            guardarPdf(document, getNombreArchivoBoletin(perfil));
            Toast.makeText(this, "Boletín PDF guardado en Descargas.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo guardar el boletín PDF.", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private int dibujarSeccion(Canvas canvas, String title, int y, Paint sectionPaint) {
        canvas.drawText(title, 52, y, sectionPaint);
        return y + 20;
    }

    private int iniciarPaginaBoletinPdf(Canvas canvas, Paint paint, Paint titlePaint, Paint mutedPaint,
                                        Paint bodyPaint, Bitmap photo, String photoInitials,
                                        String detalleEvaluacion, int pageNumber) {
        dibujarFondoBoletinPdf(canvas, paint);
        dibujarLogoPdf(canvas, 36, 22, 140, 125);
        canvas.drawText(pageNumber == 1 ? "Boletín oficial de notas" : "Boletín oficial de notas - continuación", 188, 52, titlePaint);
        canvas.drawText(detalleEvaluacion, 188, 80, mutedPaint);

        if (pageNumber == 1) {
            dibujarFotoPerfilPdf(canvas, photo, photoInitials, 484, 48, 58, paint, bodyPaint);
            return 170;
        }

        return 170;
    }

    private void dibujarTarjetaPdf(Canvas canvas, int top, int bottom, Paint paint) {
        RectF card = new RectF(32, top, 563, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(238);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(card, 18, 18, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f);
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#DBEAFE"));
        canvas.drawRoundRect(card, 18, 18, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }

    private void dibujarFooterBoletinPdf(Canvas canvas, Paint mutedPaint, int pageNumber) {
        int originalColor = mutedPaint.getColor();
        float originalSize = mutedPaint.getTextSize();
        Paint.Align originalAlign = mutedPaint.getTextAlign();

        mutedPaint.setTextSize(8.5f);
        mutedPaint.setColor(Color.WHITE);
        mutedPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Documento generado por AcademicApp. Calificaciones según los datos disponibles al descargar.", 40, 807, mutedPaint);
        mutedPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Pagina " + pageNumber, 555, 807, mutedPaint);

        mutedPaint.setColor(originalColor);
        mutedPaint.setTextSize(originalSize);
        mutedPaint.setTextAlign(originalAlign);
    }

    private void dibujarFondoBoletinPdf(Canvas canvas, Paint paint) {
        canvas.drawColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#EAF2FF"));
        paint.setAlpha(190);
        canvas.drawCircle(90, 38, 92, paint);
        canvas.drawCircle(510, 116, 74, paint);

        Path waveLight = new Path();
        waveLight.moveTo(0, 716);
        waveLight.cubicTo(120, 660, 210, 735, 326, 692);
        waveLight.cubicTo(420, 660, 496, 682, 595, 640);
        waveLight.lineTo(595, 842);
        waveLight.lineTo(0, 842);
        waveLight.close();
        paint.setAlpha(95);
        paint.setColor(Color.parseColor("#60A5FA"));
        canvas.drawPath(waveLight, paint);

        Path wave = new Path();
        wave.moveTo(0, 765);
        wave.cubicTo(122, 716, 214, 790, 330, 742);
        wave.cubicTo(424, 704, 506, 724, 595, 690);
        wave.lineTo(595, 842);
        wave.lineTo(0, 842);
        wave.close();
        paint.setAlpha(220);
        paint.setColor(Color.parseColor("#2563EB"));
        canvas.drawPath(wave, paint);
        paint.setAlpha(255);
    }

    private int dibujarLinea(Canvas canvas, String label, String value, int y, Paint bodyPaint, Paint mutedPaint) {
        canvas.drawText(label + ":", 52, y, mutedPaint);
        canvas.drawText(safeText(value), 190, y, bodyPaint);
        return y + 17;
    }

    private int dibujarResumenVisualGlobalPdf(Canvas canvas, DashboardResponse dashboard,
                                              PercentilGlobalResponse percentilGlobal, int y,
                                              Paint paint, Paint sectionPaint, Paint bodyPaint, Paint mutedPaint) {
        y = dibujarSeccion(canvas, "Resumen visual global", y, sectionPaint);

        double mediaGlobal = dashboard != null && dashboard.getMediaGlobal() != null
                ? parseDecimalValue(safeText(dashboard.getMediaGlobal().getMediaGlobal(), "-1"))
                : -1;
        double percentil = percentilGlobal != null && percentilGlobal.isCalculable()
                ? parseDecimalValue(safeText(percentilGlobal.getPercentil(), "-1"))
                : -1;

        int centerX = 298;
        int circleSize = 74;
        int circleLeft = centerX - 120;
        int circleTop = y + 2;
        RectF arc = new RectF(circleLeft, circleTop, circleLeft + circleSize, circleTop + circleSize);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawArc(arc, 135, 270, false, paint);

        if (mediaGlobal >= 0) {
            paint.setColor(getColorResumenGlobalPdf(mediaGlobal));
            canvas.drawArc(arc, 135, (float) (270 * Math.min(mediaGlobal, 10) / 10), false, paint);
        }

        bodyPaint.setTextAlign(Paint.Align.CENTER);
        bodyPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        bodyPaint.setTextSize(15);
        canvas.drawText(mediaGlobal >= 0 ? formatDecimal(mediaGlobal) : "-", circleLeft + circleSize / 2f, circleTop + 44, bodyPaint);
        bodyPaint.setTextSize(8);
        bodyPaint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("media", circleLeft + circleSize / 2f, circleTop + 58, bodyPaint);
        bodyPaint.setTextAlign(Paint.Align.LEFT);

        int legendX = centerX - 25;
        int legendY = y + 18;
        dibujarPuntoLeyendaPdf(canvas, legendX, legendY, Color.parseColor("#DC2626"), "Riesgo", paint, mutedPaint);
        dibujarPuntoLeyendaPdf(canvas, legendX, legendY + 16, Color.parseColor("#FACC15"), "Medio", paint, mutedPaint);
        dibujarPuntoLeyendaPdf(canvas, legendX, legendY + 32, Color.parseColor("#2563EB"), "Sólido", paint, mutedPaint);
        dibujarPuntoLeyendaPdf(canvas, legendX, legendY + 48, Color.parseColor("#16A34A"), "Excelente", paint, mutedPaint);

        int barLeft = centerX - 120;
        int barTop = y + 94;
        int barWidth = 240;
        mutedPaint.setTextSize(9);
        canvas.drawText("Percentil global", barLeft, barTop - 8, mutedPaint);
        dibujarBarraPdf(canvas, barLeft, barTop, barWidth, 9, percentil, 100, getColorPercentilPdf(percentil), paint);
        bodyPaint.setTextSize(10);
        canvas.drawText(percentil >= 0 ? "Percentil " + formatDecimal(percentil) : "No disponible", barLeft + barWidth + 10, barTop + 8, bodyPaint);
        mutedPaint.setTextSize(8.5f);
        canvas.drawText(buildPercentileExplanation(percentil), barLeft, barTop + 27, mutedPaint);

        return y + 140;
    }

    private int dibujarEvolucionTrimestresPdf(Canvas canvas, DashboardResponse dashboard, int y,
                                             Paint paint, Paint sectionPaint, Paint bodyPaint, Paint mutedPaint) {
        y = dibujarSeccion(canvas, "Evolución media-trimestre", y, sectionPaint);

        List<DashboardTrimestreResponse> trimestres = dashboard != null ? dashboard.getEvolucionTrimestres() : null;
        if (trimestres == null || trimestres.isEmpty()) {
            canvas.drawText("No hay datos suficientes para mostrar la evolución.", 52, y, mutedPaint);
            return y + 34;
        }

        List<DashboardTrimestreResponse> validos = new ArrayList<>();
        for (DashboardTrimestreResponse trimestre : trimestres) {
            if (parseDecimalValue(trimestre.getMedia()) >= 0) {
                validos.add(trimestre);
            }
        }

        if (validos.isEmpty()) {
            canvas.drawText("No hay datos suficientes para mostrar la evolución.", 52, y, mutedPaint);
            return y + 34;
        }

        int chartLeft = 66;
        int chartTop = y + 12;
        int chartWidth = 440;
        int chartHeight = 62;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f);
        paint.setColor(Color.parseColor("#CBD5E1"));
        canvas.drawLine(chartLeft, chartTop + chartHeight, chartLeft + chartWidth, chartTop + chartHeight, paint);
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartTop + chartHeight, paint);

        Path path = new Path();
        for (int i = 0; i < validos.size(); i++) {
            DashboardTrimestreResponse trimestre = validos.get(i);
            double media = parseDecimalValue(trimestre.getMedia());
            float px = validos.size() == 1 ? chartLeft + chartWidth / 2f : chartLeft + chartWidth * i / (float) (validos.size() - 1);
            float py = chartTop + chartHeight - (float) (chartHeight * Math.min(Math.max(media, 0), 10) / 10);
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getColorNotaPdf(media));
            canvas.drawCircle(px, py, 4.5f, paint);

            bodyPaint.setTextAlign(Paint.Align.CENTER);
            bodyPaint.setTextSize(8.5f);
            canvas.drawText(formatDecimal(media), px, py - 8, bodyPaint);

            mutedPaint.setTextAlign(Paint.Align.CENTER);
            String label = trimestre.getNumeroEvaluacion() == null ? "-" : trimestre.getNumeroEvaluacion() + "ª";
            canvas.drawText(label, px, chartTop + chartHeight + 16, mutedPaint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.4f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(ContextCompat.getColor(this, R.color.primaryColor));
        canvas.drawPath(path, paint);
        bodyPaint.setTextAlign(Paint.Align.LEFT);
        mutedPaint.setTextAlign(Paint.Align.LEFT);
        return y + 106;
    }

    private void dibujarPuntoLeyendaPdf(Canvas canvas, int x, int y, int color, String label, Paint paint, Paint textPaint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y - 4, 4, paint);
        textPaint.setTextSize(9);
        canvas.drawText(label, x + 10, y, textPaint);
    }

    private int dibujarResumenVisualPdf(Canvas canvas, List<EvaluacionAsignatura> evaluaciones,
                                        EstadisticasEvaluacion estadisticas, int y,
                                        Paint paint, Paint sectionPaint, Paint bodyPaint, Paint mutedPaint) {
        y = dibujarSeccion(canvas, "Resumen visual", y, sectionPaint);

        int barLeft = 40;
        int barWidth = 210;
        int barHeight = 9;
        bodyPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Media de la evaluación", barLeft, y, bodyPaint);
        bodyPaint.setTypeface(Typeface.DEFAULT);
        dibujarBarraPdf(canvas, barLeft, y + 8, barWidth, barHeight, estadisticas.media, 10, getColorNotaPdf(estadisticas.media), paint);
        canvas.drawText(estadisticas.asignaturasConNota == 0 ? "-" : formatDecimal(estadisticas.media), barLeft + barWidth + 12, y + 17, bodyPaint);

        int total = estadisticas.aprobadas + estadisticas.suspensas;
        double aprobadasRatio = total == 0 ? 0 : estadisticas.aprobadas;
        y += 38;
        bodyPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Balance aprobadas/suspensas", barLeft, y, bodyPaint);
        bodyPaint.setTypeface(Typeface.DEFAULT);
        dibujarBarraPdf(canvas, barLeft, y + 8, barWidth, barHeight, aprobadasRatio, Math.max(1, total), Color.parseColor("#16A34A"), paint);
        canvas.drawText(estadisticas.aprobadas + " / " + estadisticas.suspensas, barLeft + barWidth + 12, y + 17, bodyPaint);

        int xAsignaturas = 315;
        int yAsignaturas = y - 38;
        bodyPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Asignaturas", xAsignaturas, yAsignaturas, bodyPaint);
        bodyPaint.setTypeface(Typeface.DEFAULT);
        yAsignaturas += 18;

        int dibujadas = 0;
        for (EvaluacionAsignatura evaluacion : evaluaciones) {
            if (!evaluacion.tieneNotas || dibujadas >= 5) {
                continue;
            }

            mutedPaint.setTextSize(8);
            canvas.drawText(acortarTexto(evaluacion.nombreAsignatura, 22), xAsignaturas, yAsignaturas, mutedPaint);
            dibujarBarraPdf(canvas, xAsignaturas, yAsignaturas + 5, 150, 7, evaluacion.notaFinal, 10, getColorNotaPdf(evaluacion.notaFinal), paint);
            canvas.drawText(formatDecimal(evaluacion.notaFinal), xAsignaturas + 158, yAsignaturas + 12, bodyPaint);
            yAsignaturas += 22;
            dibujadas++;
        }

        return Math.max(y + 56, yAsignaturas + 4);
    }

    private void dibujarBarraPdf(Canvas canvas, int left, int top, int width, int height,
                                double value, double max, int color, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRoundRect(new RectF(left, top, left + width, top + height), height / 2f, height / 2f, paint);

        if (value <= 0) {
            return;
        }

        float filled = (float) (width * Math.min(value, max) / max);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(left, top, left + filled, top + height), height / 2f, height / 2f, paint);
    }

    private int getColorNotaPdf(double nota) {
        if (nota < 5) {
            return Color.parseColor("#DC2626");
        }
        if (nota < 7) {
            return Color.parseColor("#FACC15");
        }
        if (nota < 9) {
            return Color.parseColor("#2563EB");
        }
        return Color.parseColor("#16A34A");
    }

    private int getColorResumenGlobalPdf(double media) {
        if (media < 5) {
            return Color.parseColor("#DC2626");
        }
        if (media < 7) {
            return Color.parseColor("#FACC15");
        }
        if (media < 9) {
            return Color.parseColor("#2563EB");
        }
        return Color.parseColor("#16A34A");
    }

    private int getColorPercentilPdf(double percentil) {
        if (percentil < 0) {
            return Color.parseColor("#94A3B8");
        }
        return PERCENTILE_COLOR;
    }

    private String acortarTexto(String value, int maxLength) {
        String text = safeText(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "...";
    }

    private void dibujarBadgePdf(Canvas canvas, Integer nota, int left, int top, int size, Paint paint, Paint textPaint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getColorBadge(nota));
        canvas.drawOval(new RectF(left, top, left + size, top + size), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(ContextCompat.getColor(this, R.color.surfaceTintColor));
        canvas.drawOval(new RectF(left, top, left + size, top + size), paint);

        if (nota != null) {
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setTextSize(10);
            textPaint.setColor(getColorTextoBadge(nota));
            String value = String.valueOf(nota);
            float textWidth = textPaint.measureText(value);
            canvas.drawText(value, left + (size - textWidth) / 2, top + 15, textPaint);
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setTextSize(11);
            textPaint.setColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        }
    }

    private void dibujarFotoPerfilPdf(Canvas canvas, Bitmap bitmap, String initials, int left, int top, int size, Paint paint, Paint textPaint) {
        float centerX = left + size / 2f;
        float centerY = top + size / 2f;

        if (bitmap != null) {
            Bitmap circularPhoto = crearBitmapCircular(bitmap, size * 3);
            Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            photoPaint.setFilterBitmap(true);
            canvas.drawBitmap(circularPhoto, null, new RectF(left, top, left + size, top + size), photoPaint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(ContextCompat.getColor(this, R.color.primaryColor));
            canvas.drawCircle(centerX, centerY, size / 2f, paint);

            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setTextSize(16);
            textPaint.setColor(Color.WHITE);
            String safeInitials = safeText(initials, "AA");
            float textWidth = textPaint.measureText(safeInitials);
            canvas.drawText(safeInitials, centerX - textWidth / 2f, centerY + 6, textPaint);
            textPaint.setTypeface(Typeface.DEFAULT);
            textPaint.setTextSize(11);
            textPaint.setColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, size / 2f - 1, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private String obtenerInicialesAlumno(PerfilAlumnoResponse perfil) {
        if (perfil == null) {
            return "AA";
        }

        String nombre = safeText(perfil.getNombre(), "").trim();
        String apellidos = safeText(perfil.getApellidos(), "").trim();
        StringBuilder initials = new StringBuilder();
        if (!nombre.isEmpty()) {
            initials.append(nombre.substring(0, 1));
        }
        if (!apellidos.isEmpty()) {
            initials.append(apellidos.substring(0, 1));
        }
        return initials.length() == 0 ? "AA" : initials.toString().toUpperCase(Locale.ROOT);
    }

    private void dibujarLogoPdf(Canvas canvas, int left, int top, int width, int height) {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_academic_logo);
        if (logo == null) {
            return;
        }

        Paint logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        logoPaint.setFilterBitmap(true);
        canvas.drawBitmap(logo, null, new RectF(left, top, left + width, top + height), logoPaint);
    }

    private Bitmap recortarCentroCuadrado(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int left = (bitmap.getWidth() - size) / 2;
        int top = (bitmap.getHeight() - size) / 2;
        return Bitmap.createBitmap(bitmap, left, top, size, size);
    }

    private Bitmap crearBitmapCircular(Bitmap bitmap, int outputSize) {
        Bitmap square = recortarCentroCuadrado(bitmap);
        Bitmap scaled = Bitmap.createScaledBitmap(square, outputSize, outputSize, true);
        Bitmap output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888);
        Canvas outputCanvas = new Canvas(output);
        Path clipPath = new Path();
        clipPath.addCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f, Path.Direction.CW);
        outputCanvas.clipPath(clipPath);
        Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        photoPaint.setFilterBitmap(true);
        outputCanvas.drawBitmap(scaled, 0, 0, photoPaint);
        return output;
    }

    private Bitmap cargarFotoPerfilBitmap(PerfilAlumnoResponse perfil) {
        Bitmap fotoApi = decodeFotoPerfilBase64(perfil.getFotoPerfilBase64());
        if (fotoApi != null) {
            return fotoApi;
        }

        SharedPreferences prefs = getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE);
        String path = prefs.getString(getProfilePhotoPreferenceKey(), null);
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        try {
            if (path.startsWith("content://")) {
                try (InputStream inputStream = getContentResolver().openInputStream(Uri.parse(path))) {
                    return inputStream != null ? BitmapFactory.decodeStream(inputStream) : null;
                }
            }

            File file = new File(path);
            return file.exists() ? decodeBitmapFileConOrientacion(file) : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    private Bitmap decodeFotoPerfilBase64(String fotoPerfilBase64) {
        if (fotoPerfilBase64 == null || fotoPerfilBase64.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(fotoPerfilBase64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getProfilePhotoPreferenceKey() {
        return KEY_PROFILE_PHOTO + "_" + getCurrentUserKey();
    }

    private String getCurrentUserKey() {
        String email = sessionManager != null ? sessionManager.getEmail() : null;
        if (email == null || email.trim().isEmpty()) {
            return "anonymous";
        }

        return email.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "_");
    }

    private Bitmap decodeBitmapFileConOrientacion(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            return null;
        }

        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return aplicarOrientacionExif(bitmap, orientation);
        } catch (IOException e) {
            return bitmap;
        }
    }

    private Bitmap aplicarOrientacionExif(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.postRotate(270);
        } else {
            return bitmap;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String buildDetalleEvaluacionPdf(DashboardResponse dashboard) {
        if (evaluacionSeleccionada == EVALUACION_FINAL) {
            return "AcademicApp - Evaluación final" + buildPeriodoFinalPdf(dashboard);
        }

        DashboardTrimestreResponse evaluacion = getTrimestreSeleccionado(dashboard);
        String nombre = evaluacion != null && evaluacion.getNombreEvaluacion() != null
                ? evaluacion.getNombreEvaluacion()
                : evaluacionSeleccionada + " evaluación";
        String periodo = "";
        if (evaluacion != null && evaluacion.getFechaInicio() != null && evaluacion.getFechaFin() != null) {
            periodo = " - " + formatFechaNatural(evaluacion.getFechaInicio()) + " a " + formatFechaNatural(evaluacion.getFechaFin());
        }

        return "AcademicApp - " + nombre + periodo;
    }

    private String buildPeriodoFinalPdf(DashboardResponse dashboard) {
        if (dashboard == null || dashboard.getEvolucionTrimestres() == null || dashboard.getEvolucionTrimestres().isEmpty()) {
            return "";
        }

        String inicio = null;
        String fin = null;
        for (DashboardTrimestreResponse trimestre : dashboard.getEvolucionTrimestres()) {
            if (trimestre.getFechaInicio() != null && (inicio == null || trimestre.getFechaInicio().compareTo(inicio) < 0)) {
                inicio = trimestre.getFechaInicio();
            }
            if (trimestre.getFechaFin() != null && (fin == null || trimestre.getFechaFin().compareTo(fin) > 0)) {
                fin = trimestre.getFechaFin();
            }
        }

        if (inicio == null || fin == null) {
            return "";
        }
        return " - " + formatFechaNatural(inicio) + " a " + formatFechaNatural(fin);
    }

    private DashboardTrimestreResponse getTrimestreSeleccionado(DashboardResponse dashboard) {
        if (dashboard == null || dashboard.getEvolucionTrimestres() == null) {
            return null;
        }

        for (DashboardTrimestreResponse trimestre : dashboard.getEvolucionTrimestres()) {
            if (trimestre.getNumeroEvaluacion() != null && trimestre.getNumeroEvaluacion() == evaluacionSeleccionada) {
                return trimestre;
            }
        }

        return null;
    }

    private void guardarPdf(PdfDocument document, String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }
        if (uri == null) {
            throw new IOException("No se pudo crear el archivo");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IOException("No se pudo abrir el archivo");
            }
            document.writeTo(outputStream);
        }
    }

    private String getNombreArchivoBoletin(PerfilAlumnoResponse perfil) {
        String alumno = (safeText(perfil.getNombre(), "Alumno") + "_" + safeText(perfil.getApellidos(), ""))
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^A-Za-z0-9_]", "");
        if (evaluacionSeleccionada == EVALUACION_FINAL) {
            return "boletin_" + alumno + "_evaluacion_final.pdf";
        }
        return "boletin_" + alumno + "_evaluacion_" + evaluacionSeleccionada + ".pdf";
    }

    private EstadisticasEvaluacion calcularEstadisticas(List<EvaluacionAsignatura> evaluaciones) {
        EstadisticasEvaluacion estadisticas = new EstadisticasEvaluacion();
        estadisticas.mejorAsignatura = "-";
        estadisticas.peorAsignatura = "-";

        double total = 0;
        double mejor = Double.NEGATIVE_INFINITY;
        double peor = Double.POSITIVE_INFINITY;

        for (EvaluacionAsignatura evaluacion : evaluaciones) {
            if (!evaluacion.tieneNotas) {
                continue;
            }

            estadisticas.asignaturasConNota++;
            total += evaluacion.notaFinal;

            if (evaluacion.notaFinal >= 5) {
                estadisticas.aprobadas++;
            } else {
                estadisticas.suspensas++;
            }

            if (evaluacion.notaFinal > mejor) {
                mejor = evaluacion.notaFinal;
                estadisticas.mejorAsignatura = evaluacion.nombreAsignatura + " (" + formatDecimal(evaluacion.notaFinal) + ")";
            }

            if (evaluacion.notaFinal < peor) {
                peor = evaluacion.notaFinal;
                estadisticas.peorAsignatura = evaluacion.nombreAsignatura + " (" + formatDecimal(evaluacion.notaFinal) + ")";
            }
        }

        if (estadisticas.asignaturasConNota > 0) {
            estadisticas.media = total / estadisticas.asignaturasConNota;
        }

        return estadisticas;
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
        return safeText(value, "-");
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String safeText(Object value) {
        if (value == null) {
            return "-";
        }

        String text = String.valueOf(value);
        return text.trim().isEmpty() ? "-" : text;
    }

    private String buildCursoText(PerfilAlumnoResponse perfil) {
        String numeroCurso = perfil.getNumeroCurso() != null ? perfil.getNumeroCurso().toString() : "";
        String siglas = perfil.getSiglasCiclo() != null ? perfil.getSiglasCiclo() : "";
        String nombre = perfil.getNombreCiclo() != null ? " - " + perfil.getNombreCiclo() : "";

        String curso = (numeroCurso + " " + siglas + nombre).trim();
        return curso.isEmpty() ? "-" : curso;
    }

    private String formatFechaNatural(String fechaIso) {
        if (fechaIso == null || fechaIso.trim().isEmpty() || !fechaIso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return safeText(fechaIso);
        }

        String[] partes = fechaIso.split("-");
        return partes[2] + "-" + partes[1] + "-" + partes[0];
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

    private String formatDecimal(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private String buildPercentileExplanation(double percentil) {
        if (percentil < 0) {
            return "Se calculará cuando haya suficientes notas comparables.";
        }
        int rounded = Math.round((float) Math.max(0, Math.min(100, percentil)));
        return "Tu nota media está por encima del " + rounded + "% de tus compañeros.";
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnDescargarBoletin.setEnabled(!isLoading);
    }

    private void volverALogin() {
        Intent intent = new Intent(NotasActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarTourSiCorresponde() {
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 10) {
            return;
        }
        tourMostrado = true;
        btnDescargarBoletin.postDelayed(() -> mostrarPasoTour(10), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View target;
        String title;
        String message;

        if (index == 10) {
            target = toggleEvaluacion;
            title = "Evaluaciones";
            message = "Elige 1ª, 2ª o 3ª evaluación. Las notas se recalculan con las unidades didácticas de ese trimestre.";
        } else if (index == 11) {
            target = layoutEvaluaciones;
            title = "Boletín vivo";
            message = "Cada asignatura muestra una nota redondeada cuando ya existe al menos una unidad evaluada.";
        } else {
            target = btnDescargarBoletin;
            title = "PDF oficial";
            message = "Desde aquí descargas el boletín con perfil, notas de la evaluación y resumen visual.";
        }
        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> mostrarPasoTour(Math.max(10, index - 1)),
                () -> {
                    if (index < 12) {
                        mostrarPasoTour(index + 1);
                    } else {
                        Intent intent = new Intent(NotasActivity.this, EstadisticasActivity.class);
                        intent.putExtra("tour_step", 13);
                        startActivity(intent);
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }

    private static class EvaluacionAsignatura {
        private final String nombreAsignatura;
        private final Map<Long, EvaluacionUnidad> unidades = new LinkedHashMap<>();
        private boolean tieneNotas;
        private double notaFinal;

        EvaluacionAsignatura(String nombreAsignatura) {
            this.nombreAsignatura = nombreAsignatura;
        }
    }

    private static class EvaluacionUnidad {
        private boolean tieneNotas;
        private double notaAcumulada;
    }

    private static class EstadisticasEvaluacion {
        private int asignaturasConNota;
        private int aprobadas;
        private int suspensas;
        private double media;
        private String mejorAsignatura;
        private String peorAsignatura;
    }
}

