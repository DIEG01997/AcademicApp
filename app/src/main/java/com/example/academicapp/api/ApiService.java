package com.example.academicapp.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Contrato Retrofit con todos los endpoints REST que consume la aplicacion movil.
 */
public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);

    @POST("api/auth/logout")
    Call<Void> logout();

    @GET("api/alumnos/perfil")
    Call<PerfilAlumnoResponse> getPerfil();

    @GET("api/alumnos/curso/companeros")
    Call<List<AlumnoCursoResponse>> getCompanerosCurso();

    @PATCH("api/alumnos/perfil")
    Call<PerfilAlumnoResponse> actualizarPerfil(@Body ActualizarPerfilRequest actualizarPerfilRequest);

    @PATCH("api/alumnos/tutorial/completado")
    Call<Void> marcarTutorialCompletado();

    @DELETE("api/alumnos/perfil")
    Call<Void> eliminarPerfil();

    @GET("api/alumnos/dashboard")
    Call<DashboardResponse> getDashboard();

    @GET("api/alumnos/percentil/global")
    Call<PercentilGlobalResponse> getPercentilGlobal();

    @GET("api/alumnos/asignaturas/{idAsignatura}/percentil")
    Call<PercentilAsignaturaResponse> getPercentilAsignatura(@Path("idAsignatura") Long idAsignatura);

    @GET("api/alumnos/asignaturas")
    Call<List<AsignaturaResponse>> getAsignaturas();

    @GET("api/alumnos/asignaturas/{idAsignatura}/unidades")
    Call<List<UnidadDidacticaResponse>> getUnidades(@Path("idAsignatura") Long idAsignatura);

    @GET("api/alumnos/unidades/{idUnidad}/nota-final")
    Call<NotaFinalUnidadResponse> getNotaFinalUnidad(@Path("idUnidad") Long idUnidad);

    @GET("api/alumnos/notas")
    Call<List<NotaListadoResponse>> getNotas();

    @GET("api/instrumentos")
    Call<List<InstrumentoEvaluacionResponse>> getInstrumentos();

    @POST("api/alumnos/notas")
    Call<NotaResponse> crearNota(@Body CrearNotaRequest crearNotaRequest);

    @PUT("api/alumnos/notas/{idNota}")
    Call<NotaResponse> actualizarNota(@Path("idNota") Long idNota, @Body ActualizarNotaRequest actualizarNotaRequest);

    @DELETE("api/alumnos/notas/{idNota}")
    Call<Void> eliminarNota(@Path("idNota") Long idNota);

    @GET("api/chat/curso/mensajes")
    Call<List<MensajeChatResponse>> getMensajesChatCurso();

    @GET("api/chat/curso/estado")
    Call<ChatCursoEstadoResponse> getEstadoChatCurso();

    @PATCH("api/chat/curso/mensajes/vistos")
    Call<Void> marcarMensajesChatCursoComoVistos();

    @POST("api/chat/curso/mensajes")
    Call<MensajeChatResponse> enviarMensajeChatCurso(@Body CrearMensajeChatRequest request);
}
