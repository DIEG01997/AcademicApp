package com.example.academicapp.api;

/**
 * DTO de respuesta recibido desde la API y usado por las pantallas Android.
 */
public class ApiErrorResponse {

    private String timestamp;
    private int status;
    private String error;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
