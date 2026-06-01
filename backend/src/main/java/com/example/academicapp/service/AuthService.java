package com.example.academicapp.service;

import com.example.academicapp.dto.AuthResponse;
import com.example.academicapp.dto.LoginRequest;
import com.example.academicapp.dto.LoginResponse;
import com.example.academicapp.dto.RegisterRequest;

/**
 * Interfaz de servicio que define las operaciones de negocio disponibles para los controladores.
 */
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}