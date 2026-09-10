package com.educria.aluno.pratica;

import com.educria.aluno.pratica.QuestaoResponse;
import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import com.educria.aluno.aluno.AlunoRepository;
import com.educria.aluno.security.AlunoUserDetails;
import com.educria.aluno.pratica.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alunos/{alunoId}/proxima-questao")
@Tag(name = "Questoes")
public class QuestaoController {

    private final QuestaoService questaoService;
    private final AlunoRepository alunoRepository;
    private final AcessoAlunoGuard acessoAlunoGuard;

    public QuestaoController(QuestaoService questaoService, AlunoRepository alunoRepository, AcessoAlunoGuard acessoAlunoGuard) {
        this.questaoService = questaoService;
        this.alunoRepository = alunoRepository;
        this.acessoAlunoGuard = acessoAlunoGuard;
    }

    @GetMapping
    @Operation(summary = "Retorna proxima questao respeitando nivel atual e fila de repeticao espacada")
    public ResponseEntity<QuestaoResponse> proximaQuestao(@PathVariable Long alunoId,
                                                           @RequestParam Long assunto,
                                                           @AuthenticationPrincipal AlunoUserDetails principal) {
        acessoAlunoGuard.verificar(principal, alunoId);

        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + alunoId));

        return ResponseEntity.ok(questaoService.proximaQuestao(aluno, assunto));
    }
}
