package com.example.academicapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.academicapp.dto.InstrumentoEvaluacionResponse;
import com.example.academicapp.service.NotaService;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    private final NotaService notaService;

    public InstrumentoController(NotaService notaService) {
        this.notaService = notaService;
    }

    @GetMapping
    public List<InstrumentoEvaluacionResponse> getInstrumentosEvaluacion() {
        return notaService.getInstrumentosEvaluacion();
    }
}