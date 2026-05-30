package com.example.academicapp.activities;

import android.content.res.ColorStateList;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.BundleCompat;

import com.example.academicapp.BuildConfig;
import com.example.academicapp.R;
import com.example.academicapp.api.ActualizarPerfilRequest;
import com.example.academicapp.api.AlumnoCursoResponse;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.PerfilAlumnoResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.cache.AppDataCache;
import com.example.academicapp.session.SessionManager;
import com.example.academicapp.views.TutorialOverlayView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private static final String PROFILE_PREFS = "academic_app_profile";
    private static final String KEY_PROFILE_PHOTO = "profile_photo_path";

    private ImageView imgFotoPerfil;
    private TextView txtNombrePerfil;
    private TextView txtApellidosPerfil;
    private TextView txtCorreoPerfil;
    private TextView txtCursoPerfil;
    private TextView txtTelefonoPerfil;
    private TextView txtFechaNacimientoPerfil;
    private TextView txtEmptyCompaneros;
    private TextView txtVersionApp;
    private LinearLayout layoutCompanerosCurso;

    private ProgressBar progressBarPerfil;

    private ImageButton btnEditarFotoPerfil;
    private ImageButton btnMenuPerfil;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private ApiService apiService;
    private PerfilAlumnoResponse perfilActual;
    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Intent> camaraLauncher;
    private boolean tourMostrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        txtNombrePerfil = findViewById(R.id.txtNombrePerfil);
        txtApellidosPerfil = findViewById(R.id.txtApellidosPerfil);
        txtCorreoPerfil = findViewById(R.id.txtCorreoPerfil);
        txtCursoPerfil = findViewById(R.id.txtCursoPerfil);
        txtTelefonoPerfil = findViewById(R.id.txtTelefonoPerfil);
        txtFechaNacimientoPerfil = findViewById(R.id.txtFechaNacimientoPerfil);
        txtEmptyCompaneros = findViewById(R.id.txtEmptyCompaneros);
        txtVersionApp = findViewById(R.id.txtVersionApp);
        layoutCompanerosCurso = findViewById(R.id.layoutCompanerosCurso);

        progressBarPerfil = findViewById(R.id.progressBarPerfil);

        btnEditarFotoPerfil = findViewById(R.id.btnEditarFotoPerfil);
        btnMenuPerfil = findViewById(R.id.btnMenuPerfil);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        txtVersionApp.setText("AcademicApp v" + BuildConfig.VERSION_NAME + " - Demo");
        configurarLaunchersFotoPerfil();

        btnEditarFotoPerfil.setOnClickListener(v -> mostrarOpcionesFotoPerfil());
        btnMenuPerfil.setOnClickListener(this::mostrarMenuPerfil);
        NavigationHelper.setup(this, bottomNavigation, R.id.nav_profile);

        cargarFotoPerfilLocal();
        cargarPerfil();
    }

    private void cargarPerfil() {
        ConnectionErrorViewHelper.hide(this);
        if (!sessionManager.hasValidSession()) {
            volverALogin();
            return;
        }

        AppDataCache.prepareFor(sessionManager.getEmail());
        PerfilAlumnoResponse cachedPerfil = AppDataCache.getPerfil();
        if (cachedPerfil != null) {
            mostrarPerfil(cachedPerfil);
            cargarCompanerosCurso();
            setLoading(false);
            return;
        }

        setLoading(true);

        apiService.getPerfil().enqueue(new Callback<PerfilAlumnoResponse>() {
            @Override
            public void onResponse(Call<PerfilAlumnoResponse> call, Response<PerfilAlumnoResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(PerfilActivity.this);
                    AppDataCache.setPerfil(response.body());
                    mostrarPerfil(response.body());
                    cargarCompanerosCurso();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(
                        PerfilActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo cargar el perfil."),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<PerfilAlumnoResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(PerfilActivity.this, () -> cargarPerfil());
            }
        });
    }

    private void mostrarPerfil(PerfilAlumnoResponse perfil) {
        perfilActual = perfil;

        txtNombrePerfil.setText("Nombre: " + safeText(perfil.getNombre()));
        txtApellidosPerfil.setText("Apellidos: " + safeText(perfil.getApellidos()));
        txtCorreoPerfil.setText("Correo educativo: " + safeText(perfil.getCorreoEducativo()));
        txtCursoPerfil.setText("Curso: " + safeText(buildCursoText(perfil), "No indicado"));
        txtTelefonoPerfil.setText("Teléfono: " + safeText(perfil.getTelefono(), "No indicado"));
        txtFechaNacimientoPerfil.setText("Fecha de nacimiento: " + safeText(formatFechaNatural(perfil.getFechaNacimiento()), "No indicada"));
        mostrarFotoPerfilBase64(perfil.getFotoPerfilBase64());
        mostrarTourSiCorresponde();
    }

    private void cargarCompanerosCurso() {
        apiService.getCompanerosCurso().enqueue(new Callback<List<AlumnoCursoResponse>>() {
            @Override
            public void onResponse(Call<List<AlumnoCursoResponse>> call, Response<List<AlumnoCursoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarCompaneros(response.body());
                } else {
                    txtEmptyCompaneros.setVisibility(View.VISIBLE);
                    txtEmptyCompaneros.setText(ApiErrorUtils.getErrorMessage(
                            response,
                            "No se pudieron cargar los compañeros. Código " + response.code()
                    ));
                }
            }

            @Override
            public void onFailure(Call<List<AlumnoCursoResponse>> call, Throwable t) {
                txtEmptyCompaneros.setVisibility(View.VISIBLE);
                txtEmptyCompaneros.setText("No se pudo conectar con la API: " + t.getMessage());
            }
        });
    }

    private void mostrarCompaneros(List<AlumnoCursoResponse> companeros) {
        while (layoutCompanerosCurso.getChildCount() > 2) {
            layoutCompanerosCurso.removeViewAt(2);
        }

        if (companeros.isEmpty()) {
            txtEmptyCompaneros.setText("Todavía no hay compañeros registrados en tu curso.\n\nCuando otros alumnos se unan, aparecerán aquí con su foto y nombre.");
            txtEmptyCompaneros.setVisibility(View.VISIBLE);
            return;
        }

        txtEmptyCompaneros.setVisibility(View.GONE);
        for (AlumnoCursoResponse companero : companeros) {
            layoutCompanerosCurso.addView(crearFilaCompanero(companero));
        }
    }

    private View crearFilaCompanero(AlumnoCursoResponse companero) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(12), 0, 0);

        ShapeableImageView avatar = new ShapeableImageView(this);
        aplicarAvatar(avatar, companero.getFotoPerfilBase64());
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setShapeAppearanceModel(avatar.getShapeAppearanceModel()
                .toBuilder()
                .setAllCornerSizes(dp(21))
                .build());
        avatar.setStrokeColor(ColorStateList.valueOf(getColor(R.color.surfaceTintColor)));
        avatar.setStrokeWidth(dp(1));
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        row.addView(avatar, avatarParams);

        TextView name = new TextView(this);
        name.setText(safeText(companero.getNombre(), "") + " " + safeText(companero.getApellidos(), ""));
        name.setTextColor(getColor(R.color.textColorPrimary));
        name.setTextSize(15);
        name.setPadding(dp(12), 0, 0, 0);
        row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private void mostrarMenuPerfil(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Editar perfil");
        menu.getMenu().add("Cerrar sesión");
        menu.getMenu().add("Eliminar cuenta");
        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Editar perfil".equals(title)) {
                mostrarDialogoEditarPerfil();
                return true;
            }
            if ("Cerrar sesión".equals(title)) {
                cerrarSesion();
                return true;
            }
            if ("Eliminar cuenta".equals(title)) {
                confirmarEliminacionCuenta();
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void mostrarOpcionesFotoPerfil() {
        String[] opciones = {"Elegir de galería", "Sacar foto ahora"};

        new AlertDialog.Builder(this)
                .setTitle("Foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirGaleria();
                    } else {
                        abrirCamara();
                    }
                })
                .show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            camaraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No hay una cámara disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarLaunchersFotoPerfil() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) {
                        return;
                    }

                    try {
                        guardarFotoDesdeGaleria(result.getData().getData());
                    } catch (IOException e) {
                        Toast.makeText(this, "No se pudo guardar la foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() != RESULT_OK || data == null || data.getExtras() == null) {
                        return;
                    }

                    Bitmap bitmap = BundleCompat.getParcelable(data.getExtras(), "data", Bitmap.class);
                    if (bitmap == null) {
                        return;
                    }

                    try {
                        guardarFotoDesdeBitmap(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "No se pudo guardar la foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void guardarFotoDesdeGaleria(Uri uri) throws IOException {
        File destino = getProfilePhotoFile();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(destino)) {
            if (inputStream == null) {
                throw new IOException("No se pudo abrir la imagen");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        guardarRutaFoto(destino);
    }

    private void guardarFotoDesdeBitmap(Bitmap bitmap) throws IOException {
        File destino = getProfilePhotoFile();
        try (FileOutputStream outputStream = new FileOutputStream(destino)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream);
        }

        guardarRutaFoto(destino);
    }

    private File getProfilePhotoFile() {
        return new File(getFilesDir(), "profile_photo_" + getCurrentUserKey() + ".jpg");
    }

    private void guardarRutaFoto(File file) {
        getProfilePrefs().edit()
                .putString(getProfilePhotoPreferenceKey(), file.getAbsolutePath())
                .apply();
        mostrarFotoPerfil(file);
        subirFotoPerfil(file);
    }

    private void cargarFotoPerfilLocal() {
        imgFotoPerfil.setImageResource(R.drawable.ic_academic_icon);

        String path = getProfilePrefs().getString(getProfilePhotoPreferenceKey(), null);
        if (path == null || path.trim().isEmpty()) {
            return;
        }

        File file = new File(path);
        if (file.exists()) {
            mostrarFotoPerfil(file);
        }
    }

    private void mostrarFotoPerfil(File file) {
        Bitmap bitmap = decodeBitmapFileConOrientacion(file);
        if (bitmap != null) {
            imgFotoPerfil.setImageBitmap(bitmap);
        }
    }

    private void mostrarFotoPerfilBase64(String fotoPerfilBase64) {
        Bitmap bitmap = decodeFotoPerfil(fotoPerfilBase64);
        if (bitmap != null) {
            imgFotoPerfil.setImageBitmap(bitmap);
            return;
        }

        imgFotoPerfil.setImageResource(R.drawable.ic_academic_icon);
    }

    private void aplicarAvatar(ImageView avatar, String fotoPerfilBase64) {
        Bitmap bitmap = decodeFotoPerfil(fotoPerfilBase64);
        if (bitmap != null) {
            avatar.setImageBitmap(bitmap);
            return;
        }
        avatar.setImageResource(R.drawable.ic_academic_icon);
    }

    private Bitmap decodeFotoPerfil(String fotoPerfilBase64) {
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

    private void subirFotoPerfil(File file) {
        if (perfilActual == null) {
            return;
        }

        String fotoPerfilBase64 = encodeFotoPerfil(file);
        if (fotoPerfilBase64 == null) {
            return;
        }

        ActualizarPerfilRequest request = new ActualizarPerfilRequest(
                perfilActual.getNombre(),
                perfilActual.getApellidos(),
                perfilActual.getTelefono(),
                perfilActual.getFechaNacimiento()
        );
        request.setFotoPerfilBase64(fotoPerfilBase64);

        apiService.actualizarPerfil(request).enqueue(new Callback<PerfilAlumnoResponse>() {
            @Override
            public void onResponse(Call<PerfilAlumnoResponse> call, Response<PerfilAlumnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    perfilActual = response.body();
                    AppDataCache.setPerfil(perfilActual);
                    cargarCompanerosCurso();
                    return;
                }

                Toast.makeText(PerfilActivity.this, "La foto se guardó en este dispositivo, pero no se pudo sincronizar.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<PerfilAlumnoResponse> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "La foto se guardó en este dispositivo, pero no se pudo sincronizar.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String encodeFotoPerfil(File file) {
        Bitmap bitmap = decodeBitmapFileConOrientacion(file);
        if (bitmap == null) {
            return null;
        }

        int maxSide = 360;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min(1f, maxSide / (float) Math.max(width, height));
        Bitmap output = bitmap;
        if (scale < 1f) {
            output = Bitmap.createScaledBitmap(bitmap, Math.round(width * scale), Math.round(height * scale), true);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        output.compress(Bitmap.CompressFormat.JPEG, 78, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
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

    private SharedPreferences getProfilePrefs() {
        return getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE);
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

    private void mostrarDialogoEditarPerfil() {
        if (perfilActual == null) {
            Toast.makeText(this, "El perfil todavía no está cargado.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);

        EditText edtNombre = dialogView.findViewById(R.id.edtNombrePerfil);
        EditText edtApellidos = dialogView.findViewById(R.id.edtApellidosPerfil);
        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefonoPerfil);
        EditText edtFechaNacimiento = dialogView.findViewById(R.id.edtFechaNacimientoPerfil);

        edtNombre.setText(safeText(perfilActual.getNombre(), ""));
        edtApellidos.setText(safeText(perfilActual.getApellidos(), ""));
        edtTelefono.setText(safeText(perfilActual.getTelefono(), ""));
        edtFechaNacimiento.setText(safeText(formatFechaNatural(perfilActual.getFechaNacimiento()), ""));

        configurarSelectorFechaPerfil(edtFechaNacimiento);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar perfil")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {
                if (actualizarPerfil(edtNombre, edtApellidos, edtTelefono, edtFechaNacimiento)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private boolean actualizarPerfil(
            EditText edtNombre,
            EditText edtApellidos,
            EditText edtTelefono,
            EditText edtFechaNacimiento
    ) {
        String nombre = edtNombre.getText().toString().trim();
        String apellidos = edtApellidos.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String fechaNacimiento = edtFechaNacimiento.getText().toString().trim();
        String fechaNacimientoApi = fechaNacimiento.isEmpty() ? null : formatFechaApi(fechaNacimiento);

        if (nombre.isEmpty()) {
            edtNombre.setError("El nombre no puede estar vacío.");
            edtNombre.requestFocus();
            return false;
        }

        if (apellidos.isEmpty()) {
            edtApellidos.setError("Los apellidos no pueden estar vacíos.");
            edtApellidos.requestFocus();
            return false;
        }

        if (!telefono.isEmpty() && !telefono.matches("^[0-9]{9}$")) {
            edtTelefono.setError("Debe estar vacío o contener exactamente 9 dígitos.");
            edtTelefono.requestFocus();
            return false;
        }

        if (!fechaNacimiento.isEmpty() && fechaNacimientoApi == null) {
            edtFechaNacimiento.setError("Usa el formato DD-MM-AAAA.");
            edtFechaNacimiento.requestFocus();
            return false;
        }

        if (fechaNacimientoApi != null && !isValidBirthDate(fechaNacimientoApi)) {
            edtFechaNacimiento.setError("La fecha debe estar entre 1900 y 2020.");
            edtFechaNacimiento.requestFocus();
            return false;
        }

        ActualizarPerfilRequest request = new ActualizarPerfilRequest(
                nombre,
                apellidos,
                telefono,
                fechaNacimientoApi
        );

        setLoading(true);

        apiService.actualizarPerfil(request).enqueue(new Callback<PerfilAlumnoResponse>() {
            @Override
            public void onResponse(Call<PerfilAlumnoResponse> call, Response<PerfilAlumnoResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AppDataCache.setPerfil(response.body());
                    mostrarPerfil(response.body());
                    Toast.makeText(PerfilActivity.this, "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(
                        PerfilActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo actualizar el perfil."),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<PerfilAlumnoResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(
                        PerfilActivity.this,
                        () -> actualizarPerfil(edtNombre, edtApellidos, edtTelefono, edtFechaNacimiento)
                );
            }
        });

        return true;
    }

    private void confirmarEliminacionCuenta() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmar_eliminar_cuenta, null);
        EditText edtConfirmarEliminarCuenta = dialogView.findViewById(R.id.edtConfirmarEliminarCuenta);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setView(dialogView)
                .setPositiveButton("Eliminar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {
                String confirmacion = edtConfirmarEliminarCuenta.getText().toString().trim();

                if (!"ELIMINAR".equals(confirmacion)) {
                    edtConfirmarEliminarCuenta.setError("Debes escribir ELIMINAR para confirmar.");
                    edtConfirmarEliminarCuenta.requestFocus();
                    return;
                }

                dialog.dismiss();
                eliminarCuenta();
            });
        });

        dialog.show();
    }

    private void eliminarCuenta() {
        setLoading(true);

        apiService.eliminarPerfil().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(PerfilActivity.this, "Cuenta eliminada correctamente.", Toast.LENGTH_SHORT).show();
                    completarLogoutLocal();
                    return;
                }

                if (gestionarErrorAutenticacion(response.code())) {
                    return;
                }

                Toast.makeText(
                        PerfilActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo eliminar la cuenta."),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(PerfilActivity.this, () -> eliminarCuenta());
            }
        });
    }

    private void cerrarSesion() {
        setLoading(true);

        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                completarLogoutLocal();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                completarLogoutLocal();
            }
        });
    }

    private void configurarSelectorFechaPerfil(EditText edtFechaNacimiento) {
        edtFechaNacimiento.setFocusable(false);
        edtFechaNacimiento.setClickable(true);
        edtFechaNacimiento.setOnClickListener(v -> mostrarDatePickerPerfil(edtFechaNacimiento));
    }

    private void mostrarDatePickerPerfil(EditText edtFechaNacimiento) {
        Calendar selectedDate = Calendar.getInstance();
        String fechaActual = edtFechaNacimiento.getText().toString().trim();

        String fechaActualApi = formatFechaApi(fechaActual);
        if (fechaActualApi != null) {
            String[] dateParts = fechaActualApi.split("-");

            selectedDate.set(
                    Integer.parseInt(dateParts[0]),
                    Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2])
            );
        } else {
            selectedDate.set(2000, Calendar.JANUARY, 1);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PerfilActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fechaSeleccionada = String.format(
                            Locale.getDefault(),
                            "%02d-%02d-%04d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                    );

                    edtFechaNacimiento.setText(fechaSeleccionada);
                    edtFechaNacimiento.setError(null);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(1900, Calendar.JANUARY, 1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2020, Calendar.DECEMBER, 31);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private boolean isValidBirthDate(String birthDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(birthDate);

            int year = Integer.parseInt(birthDate.split("-")[0]);
            return year >= 1900 && year <= 2020;
        } catch (ParseException | NumberFormatException e) {
            return false;
        }
    }

    private String formatFechaNatural(String fechaIso) {
        if (fechaIso == null || fechaIso.trim().isEmpty() || !fechaIso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return fechaIso;
        }

        String[] partes = fechaIso.split("-");
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    private String formatFechaApi(String fechaNatural) {
        if (fechaNatural == null || fechaNatural.trim().isEmpty()) {
            return null;
        }

        if (fechaNatural.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return fechaNatural;
        }

        if (!fechaNatural.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return null;
        }

        String[] partes = fechaNatural.split("-");
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    private boolean gestionarErrorAutenticacion(int code) {
        if (code == 401 || code == 403) {
            completarLogoutLocal();
            return true;
        }

        return false;
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
        progressBarPerfil.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        btnEditarFotoPerfil.setEnabled(!isLoading);
        btnMenuPerfil.setEnabled(!isLoading);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void completarLogoutLocal() {
        AppDataCache.clearAll();
        EstadisticasActivity.invalidateCache();
        sessionManager.clearSession();
        volverALogin();
    }

    private void volverALogin() {
        Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarTourSiCorresponde() {
        if (tourMostrado || getIntent().getIntExtra("tour_step", -1) != 15) {
            return;
        }
        tourMostrado = true;
        btnEditarFotoPerfil.postDelayed(() -> mostrarPasoTour(15), 300);
    }

    private void mostrarPasoTour(int index) {
        SharedPreferences prefs = getSharedPreferences("academic_app_onboarding", MODE_PRIVATE);
        View target = index == 15 ? btnEditarFotoPerfil : btnMenuPerfil;
        String title = index == 15 ? "Foto de perfil" : "Opciones del perfil";
        String message = index == 15
                ? "El lápiz permite elegir una imagen de galería o sacar una foto en el momento."
                : "Desde el menú superior puedes editar tus datos, cerrar sesión o eliminar la cuenta.";

        TutorialOverlayView.show(
                this,
                target,
                title,
                message,
                index,
                17,
                () -> mostrarPasoTour(Math.max(15, index - 1)),
                () -> {
                    if (index == 15) {
                        mostrarPasoTour(16);
                    } else {
                        OnboardingStateHelper.markCompleted(this);
                        Toast.makeText(this, "Tutorial completado. Ya puedes moverte por AcademicApp.", Toast.LENGTH_LONG).show();
                    }
                },
                () -> OnboardingStateHelper.markCompleted(this)
        );
    }
}
