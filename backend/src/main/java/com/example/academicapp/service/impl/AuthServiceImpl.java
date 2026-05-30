package com.example.academicapp.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.academicapp.dto.AuthResponse;
import com.example.academicapp.dto.LoginRequest;
import com.example.academicapp.dto.LoginResponse;
import com.example.academicapp.dto.RegisterRequest;
import com.example.academicapp.entity.Alumno;
import com.example.academicapp.entity.Curso;
import com.example.academicapp.repository.AlumnoRepository;
import com.example.academicapp.repository.CursoRepository;
import com.example.academicapp.security.JwtService;
import com.example.academicapp.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AlumnoRepository alumnoRepository,
                           CursoRepository cursoRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (alumnoRepository.existsByCorreoEducativo(request.getCorreoEducativo())) {
            throw new RuntimeException("Ya existe un alumno con ese correo educativo");
        }

        Curso curso = cursoRepository.findById(request.getIdCurso())
                .orElseThrow(() -> new RuntimeException("El curso no existe"));

        Alumno alumno = new Alumno();
        alumno.setNombre(request.getNombre());
        alumno.setApellidos(request.getApellidos());
        alumno.setCorreoEducativo(request.getCorreoEducativo());
        alumno.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        alumno.setFechaNacimiento(request.getFechaNacimiento());
        alumno.setTelefono(normalizarTelefono(request.getTelefono()));
        alumno.setFechaRegistro(LocalDateTime.now());
        alumno.setCurso(curso);

        alumnoRepository.save(alumno);

        return new AuthResponse("Alumno registrado correctamente");
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return null;
        }

        String telefonoNormalizado = telefono.trim();
        if (!telefonoNormalizado.matches("^[0-9]{9}$")) {
            throw new RuntimeException("El telefono debe estar vacio o contener exactamente 9 digitos");
        }

        return telefonoNormalizado;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Alumno alumno = alumnoRepository.findByCorreoEducativo(request.getCorreoEducativo())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), alumno.getPasswordHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(alumno.getCorreoEducativo());

        return new LoginResponse(token, "Bearer");
    }
}
