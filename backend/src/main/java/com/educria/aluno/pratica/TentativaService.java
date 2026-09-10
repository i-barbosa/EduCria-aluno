package com.educria.aluno.pratica;

import com.educria.aluno.pratica.TentativaRequest;
import com.educria.aluno.pratica.TentativaResponse;
import com.educria.aluno.aluno.*;
import com.educria.aluno.pratica.*;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import com.educria.aluno.exception.RespostaInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquestra o registro de uma tentativa: motor adaptativo, repeticao espacada
 * e gamificacao rodam em cascata dentro de uma unica transacao.
 */
@Service
public class TentativaService {

    private final AlunoRepository alunoRepository;
    private final QuestaoRepository questaoRepository;
    private final NivelAlunoRepository nivelAlunoRepository;
    private final TentativaRepository tentativaRepository;
    private final AdaptiveEngineService adaptiveEngineService;
    private final SpacedRepetitionService spacedRepetitionService;
    private final GamificationService gamificationService;

    public TentativaService(AlunoRepository alunoRepository,
                             QuestaoRepository questaoRepository,
                             NivelAlunoRepository nivelAlunoRepository,
                             TentativaRepository tentativaRepository,
                             AdaptiveEngineService adaptiveEngineService,
                             SpacedRepetitionService spacedRepetitionService,
                             GamificationService gamificationService) {
        this.alunoRepository = alunoRepository;
        this.questaoRepository = questaoRepository;
        this.nivelAlunoRepository = nivelAlunoRepository;
        this.tentativaRepository = tentativaRepository;
        this.adaptiveEngineService = adaptiveEngineService;
        this.spacedRepetitionService = spacedRepetitionService;
        this.gamificationService = gamificationService;
    }

    @Transactional
    public TentativaResponse registrarTentativa(Long alunoId, TentativaRequest request) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + alunoId));

        Questao questao = questaoRepository.findById(request.questaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Questao nao encontrada: " + request.questaoId()));

        Alternativa escolhida = questao.getAlternativas().stream()
                .filter(a -> a.getId().equals(request.alternativaEscolhidaId()))
                .findFirst()
                .orElseThrow(() -> new RespostaInvalidaException("Alternativa nao pertence a questao informada"));

        boolean acertou = escolhida.isCorreta();
        Assunto assunto = questao.getAssunto();

        NivelAluno nivelAluno = nivelAlunoRepository.findByAlunoAndAssunto(aluno, assunto)
                .orElseGet(() -> NivelAluno.builder().aluno(aluno).assunto(assunto).build());

        AdaptiveEngineService.ResultadoAjuste ajusteNivel = adaptiveEngineService.registrarResposta(nivelAluno, acertou);
        nivelAlunoRepository.save(nivelAluno);

        SpacedRepetitionService.ResultadoAgendamento agendamento =
                spacedRepetitionService.registrarResultado(aluno, questao, acertou);

        tentativaRepository.save(Tentativa.builder()
                .aluno(aluno)
                .questao(questao)
                .acertou(acertou)
                .dataHora(LocalDateTime.now())
                .indiceRepeticao(agendamento.indiceAtual())
                .build());

        int xpGanho = 0;
        List<String> badgesDesbloqueados = List.of();
        List<String> conquistasDesbloqueadas = List.of();

        if (acertou) {
            xpGanho = gamificationService.registrarXpPorAcerto(aluno, assunto);
            badgesDesbloqueados = gamificationService.desbloquearBadgeSeSubiuNivel(
                    aluno, assunto, nivelAluno.getNivelAtual(), ajusteNivel.subiuNivel());
        }

        gamificationService.atualizarStreak(aluno);
        conquistasDesbloqueadas = gamificationService.verificarConquistas(
                aluno, nivelAluno, ajusteNivel.subiuNivel(), agendamento.cicloCompleto());

        return new TentativaResponse(
                acertou,
                nivelAluno.getNivelAtual(),
                ajusteNivel.subiuNivel(),
                ajusteNivel.desceuNivel(),
                xpGanho,
                agendamento.proximaData(),
                badgesDesbloqueados,
                conquistasDesbloqueadas);
    }
}
