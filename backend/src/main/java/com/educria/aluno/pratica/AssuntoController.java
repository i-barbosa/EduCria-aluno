package com.educria.aluno.pratica;

import com.educria.aluno.pratica.AssuntoResponse;
import com.educria.aluno.pratica.AssuntoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assuntos")
@Tag(name = "Assuntos")
public class AssuntoController {

    private final AssuntoRepository assuntoRepository;

    public AssuntoController(AssuntoRepository assuntoRepository) {
        this.assuntoRepository = assuntoRepository;
    }

    @GetMapping
    @Operation(summary = "Lista os assuntos disponiveis para pratica")
    public ResponseEntity<List<AssuntoResponse>> listar() {
        List<AssuntoResponse> resposta = assuntoRepository.findAll().stream()
                .map(a -> new AssuntoResponse(a.getId(), a.getNome(), a.getDisciplina()))
                .toList();
        return ResponseEntity.ok(resposta);
    }
}
