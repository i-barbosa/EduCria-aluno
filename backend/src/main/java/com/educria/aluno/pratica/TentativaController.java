package com.educria.aluno.pratica;

import com.educria.aluno.pratica.TentativaRequest;
import com.educria.aluno.pratica.TentativaResponse;
import com.educria.aluno.security.AlunoUserDetails;
import com.educria.aluno.pratica.TentativaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alunos/{alunoId}/tentativas")
@Tag(name = "Tentativas")
public class TentativaController {

    private final TentativaService tentativaService;
    private final AcessoAlunoGuard acessoAlunoGuard;

    public TentativaController(TentativaService tentativaService, AcessoAlunoGuard acessoAlunoGuard) {
        this.tentativaService = tentativaService;
        this.acessoAlunoGuard = acessoAlunoGuard;
    }

    @PostMapping
    @Operation(summary = "Registra tentativa de resposta e roda motor adaptativo, repeticao espacada e gamificacao")
    public ResponseEntity<TentativaResponse> registrar(@PathVariable Long alunoId,
                                                         @Valid @RequestBody TentativaRequest request,
                                                         @AuthenticationPrincipal AlunoUserDetails principal) {
        acessoAlunoGuard.verificar(principal, alunoId);
        return ResponseEntity.ok(tentativaService.registrarTentativa(alunoId, request));
    }
}
