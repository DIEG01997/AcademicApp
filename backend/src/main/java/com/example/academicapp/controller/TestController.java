package com.example.academicapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/protegido")
    public String protegido() {
        return "Acceso concedido con JWT válido";
    }
}