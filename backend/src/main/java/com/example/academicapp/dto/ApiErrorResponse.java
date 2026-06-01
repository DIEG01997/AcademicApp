package com.example.academicapp.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida que desacopla las entidades JPA del contrato publico de la API.
 */
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
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