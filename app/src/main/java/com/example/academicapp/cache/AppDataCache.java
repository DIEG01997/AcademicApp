package com.example.academicapp.cache;

import com.example.academicapp.api.AsignaturaResponse;
import com.example.academicapp.api.InstrumentoEvaluacionResponse;
import com.example.academicapp.api.NotaListadoResponse;
import com.example.academicapp.api.PerfilAlumnoResponse;
import com.example.academicapp.api.UnidadDidacticaResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppDataCache {

    private static final long CACHE_TTL_MS = 60_000L;

    private static String ownerEmail;
    private static PerfilAlumnoResponse perfil;
    private static long perfilAt;
    private static List<AsignaturaResponse> asignaturas;
    private static long asignaturasAt;
    private static List<InstrumentoEvaluacionResponse> instrumentos;
    private static long instrumentosAt;
    private static List<NotaListadoResponse> notas;
    private static long notasAt;
    private static final Map<Long, CacheEntry<List<UnidadDidacticaResponse>>> unidadesPorAsignatura = new HashMap<>();

    private AppDataCache() {
    }

    public static void prepareFor(String email) {
        if (ownerEmail != null && ownerEmail.equals(email)) {
            return;
        }
        clearAll();
        ownerEmail = email;
    }

    public static PerfilAlumnoResponse getPerfil() {
        return isFresh(perfilAt) ? perfil : null;
    }

    public static void setPerfil(PerfilAlumnoResponse value) {
        perfil = value;
        perfilAt = System.currentTimeMillis();
    }

    public static List<AsignaturaResponse> getAsignaturas() {
        return isFresh(asignaturasAt) ? asignaturas : null;
    }

    public static void setAsignaturas(List<AsignaturaResponse> value) {
        asignaturas = value;
        asignaturasAt = System.currentTimeMillis();
    }

    public static List<InstrumentoEvaluacionResponse> getInstrumentos() {
        return isFresh(instrumentosAt) ? instrumentos : null;
    }

    public static void setInstrumentos(List<InstrumentoEvaluacionResponse> value) {
        instrumentos = value;
        instrumentosAt = System.currentTimeMillis();
    }

    public static List<NotaListadoResponse> getNotas() {
        return isFresh(notasAt) ? notas : null;
    }

    public static void setNotas(List<NotaListadoResponse> value) {
        notas = value;
        notasAt = System.currentTimeMillis();
    }

    public static List<UnidadDidacticaResponse> getUnidades(Long idAsignatura) {
        if (idAsignatura == null) {
            return null;
        }
        CacheEntry<List<UnidadDidacticaResponse>> entry = unidadesPorAsignatura.get(idAsignatura);
        return entry != null && entry.isFresh() ? entry.value : null;
    }

    public static void setUnidades(Long idAsignatura, List<UnidadDidacticaResponse> value) {
        if (idAsignatura != null) {
            unidadesPorAsignatura.put(idAsignatura, new CacheEntry<>(value));
        }
    }

    public static void invalidateAcademicData() {
        notas = null;
        notasAt = 0L;
        unidadesPorAsignatura.clear();
    }

    public static void clearAll() {
        ownerEmail = null;
        perfil = null;
        perfilAt = 0L;
        asignaturas = null;
        asignaturasAt = 0L;
        instrumentos = null;
        instrumentosAt = 0L;
        notas = null;
        notasAt = 0L;
        unidadesPorAsignatura.clear();
    }

    private static boolean isFresh(long timestamp) {
        return timestamp > 0 && System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
    }

    private static class CacheEntry<T> {
        private final T value;
        private final long timestamp;

        private CacheEntry(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        private boolean isFresh() {
            return AppDataCache.isFresh(timestamp);
        }
    }
}
