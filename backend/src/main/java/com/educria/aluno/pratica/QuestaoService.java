package com.educria.aluno.pratica;

import com.educria.aluno.pratica.AlternativaResponse;
import com.educria.aluno.pratica.QuestaoResponse;
import com.educria.aluno.aluno.*;
import com.educria.aluno.pratica.*;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import com.educria.aluno.pratica.AssuntoRepository;
import com.educria.aluno.pratica.NivelAlunoRepository;
import com.educria.aluno.pratica.QuestaoRepository;
import com.educria.aluno.pratica.TentativaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Escolhe a proxima questao respeitando a fila de repeticao espacada (prioridade)
 * e, quando nao ha revisao devida, o nivel atual do aluno no assunto.
 */
@Service
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final AssuntoRepository assuntoRepository;
    private final NivelAlunoRepository nivelAlunoRepository;
    private final TentativaRepository tentativaRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    public QuestaoService(QuestaoRepository questaoRepository,
                           AssuntoRepository assuntoRepository,
                           NivelAlunoRepository nivelAlunoRepository,
                           TentativaRepository tentativaRepository,
                           SpacedRepetitionService spacedRepetitionService) {
        this.questaoRepository = questaoRepository;
        this.assuntoRepository = assuntoRepository;
        this.nivelAlunoRepository = nivelAlunoRepository;
        this.tentativaRepository = tentativaRepository;
        this.spacedRepetitionService = spacedRepetitionService;
    }

    public QuestaoResponse proximaQuestao(Aluno aluno, Long assuntoId) {
        Assunto assunto = assuntoRepository.findById(assuntoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Assunto nao encontrado: " + assuntoId));

        Optional<RepeticaoAgendada> revisaoDevida = spacedRepetitionService.proximaRevisaoDisponivel(aluno, assuntoId);

        if (revisaoDevida.isPresent()) {
            return paraResponse(revisaoDevida.get().getQuestao());
        }

        int nivelAtual = nivelAlunoRepository.findByAlunoAndAssunto(aluno, assunto)
                .map(NivelAluno::getNivelAtual)
                .orElse(1);

        List<Long> jaRespondidas = tentativaRepository.findByAlunoOrderByDataHoraDesc(aluno).stream()
                .map(t -> t.getQuestao().getId())
                .distinct()
                .toList();

        List<Questao> candidatas = jaRespondidas.isEmpty()
                ? questaoRepository.findByAssuntoAndNivel(assunto, nivelAtual)
                : questaoRepository.findByAssuntoAndNivelAndIdNotIn(assunto, nivelAtual, jaRespondidas);

        if (candidatas.isEmpty()) {
            candidatas = questaoRepository.findByAssuntoAndNivel(assunto, nivelAtual);
        }

        if (candidatas.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Nenhuma questao disponivel para o assunto " + assuntoId + " no nivel " + nivelAtual);
        }

        return paraResponse(candidatas.get(0));
    }

    private QuestaoResponse paraResponse(Questao questao) {
        List<AlternativaResponse> alternativas = questao.getAlternativas().stream()
                .map(a -> new AlternativaResponse(a.getId(), a.getTexto()))
                .toList();

        return new QuestaoResponse(
                questao.getId(),
                questao.getAssunto().getId(),
                questao.getNivel(),
                questao.getEnunciado(),
                alternativas.isEmpty() ? Collections.emptyList() : alternativas);
    }
}
