package com.example.academicapp.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.example.academicapp.entity.Alumno;
import com.example.academicapp.repository.AlumnoRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AlumnoRepository alumnoRepository;

    public CustomUserDetailsService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correoEducativo) throws UsernameNotFoundException {
        Alumno alumno = alumnoRepository.findByCorreoEducativo(correoEducativo)
                .orElseThrow(() -> new UsernameNotFoundException("Alumno no encontrado"));

        return new User(
                alumno.getCorreoEducativo(),
                alumno.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
