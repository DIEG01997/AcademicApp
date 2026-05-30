package com.example.academicapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.AuthResponse;
import com.example.academicapp.api.RegisterRequest;
import com.example.academicapp.api.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNombre, edtApellidos, edtCorreo, edtPassword, edtFechaNacimiento, edtTelefono;
    private Spinner spinnerCurso, spinnerCiclo;
    private CheckBox checkboxTerms;
    private Button btnRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtNombre = findViewById(R.id.edtNombre);
        edtApellidos = findViewById(R.id.edtApellidos);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtPassword = findViewById(R.id.edtPassword);
        edtFechaNacimiento = findViewById(R.id.edtFechaNacimiento);
        edtTelefono = findViewById(R.id.edtTelefono);
        checkboxTerms = findViewById(R.id.checkboxTerms);
        btnRegister = findViewById(R.id.btnRegister);

        spinnerCurso = findViewById(R.id.spinnerCurso);
        spinnerCiclo = findViewById(R.id.spinnerCiclo);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        ArrayAdapter<CharSequence> adapterCurso = ArrayAdapter.createFromResource(
                this,
                R.array.cursos_array,
                android.R.layout.simple_spinner_item
        );
        adapterCurso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurso.setAdapter(adapterCurso);

        ArrayAdapter<CharSequence> adapterCiclo = ArrayAdapter.createFromResource(
                this,
                R.array.ciclos_array,
                android.R.layout.simple_spinner_item
        );
        adapterCiclo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCiclo.setAdapter(adapterCiclo);

        configurarSelectorFecha();
        btnRegister.setOnClickListener(v -> intentarRegistro());
    }

    private void intentarRegistro() {
        limpiarErrores();

        String nombre = edtNombre.getText().toString().trim();
        String apellidos = edtApellidos.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String fechaNacimiento = edtFechaNacimiento.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();

        if (!validarCamposObligatorios(nombre, apellidos, correo, password, fechaNacimiento)) {
            return;
        }

        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(correo)) {
            edtCorreo.setError("El correo debe terminar con @educarex.es");
            edtCorreo.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            edtPassword.setError("La contraseña debe tener mayúscula, número y símbolo.");
            edtPassword.requestFocus();
            return;
        }

        if (!isValidBirthDate(fechaNacimiento)) {
            edtFechaNacimiento.setError("La fecha de nacimiento debe ser posterior a 1900 y anterior a 2020.");
            edtFechaNacimiento.requestFocus();
            return;
        }

        if (!telefono.isEmpty() && !isValidPhone(telefono)) {
            edtTelefono.setError("Introduce un teléfono válido de 9 dígitos.");
            edtTelefono.requestFocus();
            return;
        }

        String curso = spinnerCurso.getSelectedItem().toString();
        String ciclo = spinnerCiclo.getSelectedItem().toString();
        int idCurso = getIdCurso(curso, ciclo);

        if (idCurso == -1) {
            Toast.makeText(this, "Selecciona una combinación válida de curso y ciclo.", Toast.LENGTH_SHORT).show();
            return;
        }

        enviarRegistro(nombre, apellidos, correo, password, fechaNacimiento, telefono, idCurso);
    }

    private void limpiarErrores() {
        edtNombre.setError(null);
        edtApellidos.setError(null);
        edtCorreo.setError(null);
        edtPassword.setError(null);
        edtFechaNacimiento.setError(null);
        edtTelefono.setError(null);
    }

    private boolean validarCamposObligatorios(String nombre, String apellidos, String correo,
                                              String password, String fechaNacimiento) {
        boolean isValid = true;
        if (nombre.isEmpty()) {
            edtNombre.setError("Introduce tu nombre.");
            isValid = false;
        }
        if (apellidos.isEmpty()) {
            edtApellidos.setError("Introduce tus apellidos.");
            isValid = false;
        }
        if (correo.isEmpty()) {
            edtCorreo.setError("Introduce tu correo educativo.");
            isValid = false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Introduce una contraseña.");
            isValid = false;
        }
        if (fechaNacimiento.isEmpty()) {
            edtFechaNacimiento.setError("Selecciona tu fecha de nacimiento.");
            isValid = false;
        }

        if (!isValid) {
            if (nombre.isEmpty()) {
                edtNombre.requestFocus();
            } else if (apellidos.isEmpty()) {
                edtApellidos.requestFocus();
            } else if (correo.isEmpty()) {
                edtCorreo.requestFocus();
            } else if (password.isEmpty()) {
                edtPassword.requestFocus();
            } else {
                edtFechaNacimiento.requestFocus();
            }
        }
        return isValid;
    }

    private void configurarSelectorFecha() {
        edtFechaNacimiento.setFocusable(false);
        edtFechaNacimiento.setClickable(true);
        edtFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
    }

    private void enviarRegistro(String nombre, String apellidos, String correo, String password,
                                String fechaNacimiento, String telefono, int idCurso) {
        ConnectionErrorViewHelper.hide(this);
        setLoading(true);

        RegisterRequest request = new RegisterRequest(
                nombre,
                apellidos,
                correo,
                password,
                fechaNacimiento,
                telefono.isEmpty() ? null : telefono,
                (long) idCurso
        );

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                ConnectionErrorViewHelper.hide(RegisterActivity.this);

                if (response.isSuccessful()) {
                    String mensaje = "Alumno registrado correctamente.";
                    if (response.body() != null && response.body().getMensaje() != null) {
                        mensaje = response.body().getMensaje();
                    }

                    Toast.makeText(RegisterActivity.this, mensaje, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("registered_email", correo);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }

                Toast.makeText(
                        RegisterActivity.this,
                        ApiErrorUtils.getErrorMessage(response, getRegisterErrorMessage(response.code())),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                ConnectionErrorViewHelper.show(
                        RegisterActivity.this,
                        () -> enviarRegistro(nombre, apellidos, correo, password, fechaNacimiento, telefono, idCurso)
                );
            }
        });
    }

    private void mostrarDatePicker() {
        int year = 2000;
        int month = Calendar.JANUARY;
        int day = 1;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fechaSeleccionada = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                    );
                    edtFechaNacimiento.setText(fechaSeleccionada);
                },
                year,
                month,
                day
        );

        Calendar minDate = Calendar.getInstance();
        minDate.set(1900, Calendar.JANUARY, 1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2020, Calendar.DECEMBER, 31);

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private boolean isValidEmail(String correo) {
        return correo.endsWith("@educarex.es");
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*+=_?;:,.<>]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
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

    private boolean isValidPhone(String telefono) {
        return telefono.matches("^[0-9]{9}$");
    }

    private int getIdCurso(String curso, String ciclo) {
        if (curso.startsWith("1") && "DAM".equals(ciclo)) {
            return 1;
        } else if (curso.startsWith("2") && "DAM".equals(ciclo)) {
            return 2;
        } else if (curso.startsWith("1") && "DAW".equals(ciclo)) {
            return 3;
        } else if (curso.startsWith("2") && "DAW".equals(ciclo)) {
            return 4;
        }
        return -1;
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        checkboxTerms.setEnabled(!isLoading);
        spinnerCurso.setEnabled(!isLoading);
        spinnerCiclo.setEnabled(!isLoading);
        edtNombre.setEnabled(!isLoading);
        edtApellidos.setEnabled(!isLoading);
        edtCorreo.setEnabled(!isLoading);
        edtPassword.setEnabled(!isLoading);
        edtFechaNacimiento.setEnabled(!isLoading);
        edtTelefono.setEnabled(!isLoading);
    }

    private String getRegisterErrorMessage(int code) {
        if (code == 400) {
            return "Revisa los datos del registro.";
        }
        if (code == 409) {
            return "Ya existe una cuenta con ese correo.";
        }
        if (code >= 500) {
            return "Error del servidor. Inténtalo de nuevo.";
        }
        return "No se pudo completar el registro.";
    }
}
