package com.example.academicapp.activities;

import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.academicapp.R;
import com.example.academicapp.api.ApiErrorUtils;
import com.example.academicapp.api.ApiService;
import com.example.academicapp.api.CrearMensajeChatRequest;
import com.example.academicapp.api.MensajeChatResponse;
import com.example.academicapp.api.RetrofitClient;
import com.example.academicapp.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatCursoActivity extends AppCompatActivity {

    private static final String CHAT_PREFS = "academic_app_chat";
    private static final String KEY_LAST_SEEN_CHAT_ID = "last_seen_chat_id";
    private static final long CHAT_REFRESH_INTERVAL_MS = 3_000L;

    private LinearLayout layoutMensajesChat;
    private EditText edtMensajeChat;
    private ImageButton btnEnviarChat;
    private ApiService apiService;
    private SessionManager sessionManager;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!chatActivo) {
                return;
            }
            cargarMensajesSilencioso();
            refreshHandler.postDelayed(this, CHAT_REFRESH_INTERVAL_MS);
        }
    };
    private boolean chatActivo;
    private boolean cargandoMensajes;
    private long ultimoMensajePintado = -1L;
    private int cantidadMensajesPintados = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_curso);

        layoutMensajesChat = findViewById(R.id.layoutMensajesChat);
        edtMensajeChat = findViewById(R.id.edtMensajeChat);
        btnEnviarChat = findViewById(R.id.btnEnviarChat);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        NavigationHelper.setup(this, bottomNavigation, R.id.nav_home);

        edtMensajeChat.setEnabled(true);
        btnEnviarChat.setEnabled(true);
        btnEnviarChat.setOnClickListener(v -> enviarMensaje());
        cargarMensajes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatActivo = true;
        iniciarActualizacionChat();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatActivo = false;
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void iniciarActualizacionChat() {
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, CHAT_REFRESH_INTERVAL_MS);
    }

    private void cargarMensajes() {
        ConnectionErrorViewHelper.hide(this);
        cargarMensajes(false);
    }

    private void cargarMensajesSilencioso() {
        cargarMensajes(true);
    }

    private void cargarMensajes(boolean silencioso) {
        if (cargandoMensajes) {
            return;
        }

        cargandoMensajes = true;
        apiService.getMensajesChatCurso().enqueue(new Callback<List<MensajeChatResponse>>() {
            @Override
            public void onResponse(Call<List<MensajeChatResponse>> call, Response<List<MensajeChatResponse>> response) {
                cargandoMensajes = false;
                if (response.isSuccessful() && response.body() != null) {
                    ConnectionErrorViewHelper.hide(ChatCursoActivity.this);
                    pintarMensajesSiHanCambiado(response.body());
                    marcarMensajesComoVistos(response.body());
                    return;
                }
                if (silencioso) {
                    return;
                }
                Toast.makeText(
                        ChatCursoActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo cargar el chat. Código " + response.code()),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<List<MensajeChatResponse>> call, Throwable t) {
                cargandoMensajes = false;
                if (!silencioso) {
                    ConnectionErrorViewHelper.show(ChatCursoActivity.this, () -> cargarMensajes());
                }
            }
        });
    }

    private void pintarMensajesSiHanCambiado(List<MensajeChatResponse> mensajes) {
        long ultimoId = obtenerUltimoId(mensajes);
        if (ultimoId == ultimoMensajePintado && mensajes.size() == cantidadMensajesPintados) {
            return;
        }

        boolean debeBajar = ultimoId > ultimoMensajePintado;
        pintarMensajes(mensajes);
        ultimoMensajePintado = ultimoId;
        cantidadMensajesPintados = mensajes.size();
        if (debeBajar) {
            desplazarAlFinal();
        }
    }

    private void enviarMensaje() {
        String contenido = edtMensajeChat.getText().toString().trim();
        if (contenido.isEmpty()) {
            return;
        }

        btnEnviarChat.setEnabled(false);
        apiService.enviarMensajeChatCurso(new CrearMensajeChatRequest(contenido)).enqueue(new Callback<MensajeChatResponse>() {
            @Override
            public void onResponse(Call<MensajeChatResponse> call, Response<MensajeChatResponse> response) {
                btnEnviarChat.setEnabled(true);
                if (response.isSuccessful()) {
                    edtMensajeChat.setText("");
                    cargarMensajes();
                    iniciarActualizacionChat();
                    return;
                }
                Toast.makeText(
                        ChatCursoActivity.this,
                        ApiErrorUtils.getErrorMessage(response, "No se pudo enviar el mensaje. Código " + response.code()),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<MensajeChatResponse> call, Throwable t) {
                btnEnviarChat.setEnabled(true);
                ConnectionErrorViewHelper.show(ChatCursoActivity.this, () -> enviarMensaje());
            }
        });
    }

    private long obtenerUltimoId(List<MensajeChatResponse> mensajes) {
        long maxId = 0L;
        for (MensajeChatResponse mensaje : mensajes) {
            if (mensaje.getIdMensaje() != null && mensaje.getIdMensaje() > maxId) {
                maxId = mensaje.getIdMensaje();
            }
        }
        return maxId;
    }

    private void desplazarAlFinal() {
        layoutMensajesChat.post(() -> {
            android.view.View parent = (android.view.View) layoutMensajesChat.getParent();
            if (parent instanceof android.widget.ScrollView) {
                ((android.widget.ScrollView) parent).fullScroll(android.view.View.FOCUS_DOWN);
            }
        });
    }

    private void pintarMensajes(List<MensajeChatResponse> mensajes) {
        layoutMensajesChat.removeAllViews();
        if (mensajes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Todavía no hay mensajes en tu curso.\n\nSé el primero en iniciar la conversación con tus compañeros.");
            empty.setTextColor(ContextCompat.getColor(this, R.color.textColorSecondary));
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setBackgroundResource(R.drawable.bg_empty_state_card);
            empty.setPadding(dp(18), dp(18), dp(18), dp(18));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dp(16), 0, dp(16));
            layoutMensajesChat.addView(empty, params);
            return;
        }

        for (MensajeChatResponse mensaje : mensajes) {
            layoutMensajesChat.addView(crearBurbujaMensaje(mensaje));
        }
    }

    private void marcarMensajesComoVistos(List<MensajeChatResponse> mensajes) {
        long maxId = 0L;
        for (MensajeChatResponse mensaje : mensajes) {
            if (mensaje.getIdMensaje() != null && mensaje.getIdMensaje() > maxId) {
                maxId = mensaje.getIdMensaje();
            }
        }

        if (maxId > 0) {
            SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
            long current = prefs.getLong(getLastSeenChatKey(), 0L);
            if (maxId > current) {
                prefs.edit().putLong(getLastSeenChatKey(), maxId).apply();
            }
        }

        apiService.marcarMensajesChatCursoComoVistos().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private String getLastSeenChatKey() {
        return KEY_LAST_SEEN_CHAT_ID + "_" + getCurrentUserKey();
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

    private LinearLayout crearBurbujaMensaje(MensajeChatResponse mensaje) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(mensaje.isPropio() ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowParams);

        ShapeableImageView avatar = crearAvatarMensaje(mensaje.getFotoPerfilBase64());

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(mensaje.isPropio() ? 0 : dp(8), 0, mensaje.isPropio() ? dp(8) : 0, 0);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(mensaje.isPropio()
                ? ContextCompat.getColor(this, R.color.primaryColor)
                : ContextCompat.getColor(this, R.color.cardBackgroundColor));
        card.setRadius(dp(8));
        card.setCardElevation(dp(1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(10), dp(14), dp(10));
        TextView autor = new TextView(this);
        autor.setText(mensaje.isPropio() ? "Tú" : safeText(mensaje.getNombreAlumno()));
        autor.setTextSize(11);
        autor.setTextColor(mensaje.isPropio() ? Color.WHITE : ContextCompat.getColor(this, R.color.textColorSecondary));

        TextView cuerpo = new TextView(this);
        cuerpo.setText(safeText(mensaje.getContenido()));
        cuerpo.setTextSize(15);
        cuerpo.setTextColor(mensaje.isPropio() ? Color.WHITE : ContextCompat.getColor(this, R.color.textColorPrimary));
        cuerpo.setPadding(0, dp(3), 0, 0);

        content.addView(autor);
        content.addView(cuerpo);
        card.addView(content);

        if (mensaje.isPropio()) {
            row.addView(card);
            row.addView(avatar);
        } else {
            row.addView(avatar);
            row.addView(card);
        }
        return row;
    }

    private ShapeableImageView crearAvatarMensaje(String fotoPerfilBase64) {
        ShapeableImageView avatar = new ShapeableImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setShapeAppearanceModel(avatar.getShapeAppearanceModel()
                .toBuilder()
                .setAllCornerSizes(dp(18))
                .build());
        avatar.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surfaceTintColor)));
        avatar.setStrokeWidth(dp(1));
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));

        Bitmap bitmap = decodeFotoPerfil(fotoPerfilBase64);
        if (bitmap != null) {
            avatar.setImageBitmap(bitmap);
        } else {
            avatar.setImageResource(R.drawable.ic_academic_icon);
        }

        return avatar;
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

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
