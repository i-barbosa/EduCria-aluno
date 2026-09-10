package com.educria.aluno.pratica;

import com.educria.aluno.pratica.ConquistaResponse;
import com.educria.aluno.pratica.ProgressoResponse;
import com.educria.aluno.security.AlunoUserDetails;
import com.educria.aluno.pratica.AlunoProgressoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alunos/{alunoId}")
@Tag(name = "Progresso")
public class ProgressoController {

    private final AlunoProgressoService alunoProgressoService;
    private final AcessoAlunoGuard acessoAlunoGuard;

    public ProgressoController(AlunoProgressoService alunoProgressoService, AcessoAlunoGuard acessoAlunoGuard) {
        this.alunoProgressoService = alunoProgressoService;
        this.acessoAlunoGuard = acessoAlunoGuard;
    }

    @GetMapping("/progresso")
    @Operation(summary = "Nivel atual por assunto, XP, badges e streak do proprio aluno")
    public ResponseEntity<ProgressoResponse> progresso(@PathVariable Long alunoId,
                                                         @AuthenticationPrincipal AlunoUserDetails principal) {
        acessoAlunoGuard.verificar(principal, alunoId);
        return ResponseEntity.ok(alunoProgressoService.progresso(alunoId));
    }

    @GetMapping("/conquistas")
    @Operation(summary = "Conquistas desbloqueadas pelo proprio aluno")
    public ResponseEntity<List<ConquistaResponse>> conquistas(@PathVariable Long alunoId,
                                                                @AuthenticationPrincipal AlunoUserDetails principal) {
        acessoAlunoGuard.verificar(principal, alunoId);
        return ResponseEntity.ok(alunoProgressoService.conquistas(alunoId));
    }
}
