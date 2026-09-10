package com.educria.aluno.pratica;

import com.educria.aluno.config.MotorProperties;
import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Questao;
import com.educria.aluno.pratica.RepeticaoAgendada;
import com.educria.aluno.pratica.RepeticaoAgendadaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repeticao espacada: questao acertada reaparece nos intervalos configurados (1,3,7,16,35,70 dias).
 * Cada acerto na revisao avanca o indice da sequencia; um erro na revisao reinicia o indice para 0.
 */
@Service
public class SpacedRepetitionService {

    private final RepeticaoAgendadaRepository repeticaoAgendadaRepository;
    private final MotorProperties motorProperties;

    public SpacedRepetitionService(RepeticaoAgendadaRepository repeticaoAgendadaRepository,
                                    MotorProperties motorProperties) {
        this.repeticaoAgendadaRepository = repeticaoAgendadaRepository;
        this.motorProperties = motorProperties;
    }

    public record ResultadoAgendamento(int indiceAtual, LocalDate proximaData, boolean cicloCompleto) {
    }

    public ResultadoAgendamento registrarResultado(Aluno aluno, Questao questao, boolean acertou) {
        RepeticaoAgendada agendamento = repeticaoAgendadaRepository.findByAlunoAndQuestao(aluno, questao)
                .orElseGet(() -> RepeticaoAgendada.builder()
                        .aluno(aluno)
                        .questao(questao)
                        .indiceSequencia(0)
                        .proximaData(LocalDate.now())
                        .build());

        List<Integer> intervalos = motorProperties.getIntervalosRepeticao();

        if (!acertou) {
            agendamento.setIndiceSequencia(0);
            agendamento.setProximaData(LocalDate.now().plusDays(intervalos.get(0)));
            repeticaoAgendadaRepository.save(agendamento);
            return new ResultadoAgendamento(0, agendamento.getProximaData(), false);
        }

        int indiceUsado = agendamento.getIndiceSequencia();
        boolean cicloCompleto = indiceUsado >= intervalos.size() - 1;

        int proximoIndice = cicloCompleto ? intervalos.size() - 1 : indiceUsado + 1;
        int diasAteProxima = intervalos.get(proximoIndice);

        agendamento.setIndiceSequencia(proximoIndice);
        agendamento.setProximaData(LocalDate.now().plusDays(diasAteProxima));
        repeticaoAgendadaRepository.save(agendamento);

        return new ResultadoAgendamento(proximoIndice, agendamento.getProximaData(), cicloCompleto);
    }

    public Optional<RepeticaoAgendada> proximaRevisaoDisponivel(Aluno aluno, Long assuntoId) {
        return repeticaoAgendadaRepository
                .findByAlunoAndProximaDataLessThanEqualOrderByProximaDataAsc(aluno, LocalDate.now())
                .stream()
                .filter(agenda -> agenda.getQuestao().getAssunto().getId().equals(assuntoId))
                .findFirst();
    }
}
