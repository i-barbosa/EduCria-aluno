package com.educria.aluno.pratica;

import com.educria.aluno.config.MotorProperties;
import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Questao;
import com.educria.aluno.pratica.RepeticaoAgendada;
import com.educria.aluno.pratica.RepeticaoAgendadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpacedRepetitionServiceTest {

    private RepeticaoAgendadaRepository repository;
    private SpacedRepetitionService service;
    private Aluno aluno;
    private Questao questao;

    private static final List<Integer> INTERVALOS = List.of(1, 3, 7, 16, 35, 70);

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RepeticaoAgendadaRepository.class);
        MotorProperties motorProperties = new MotorProperties(1, 10, 10, "1,3,7,16,35,70");

        service = new SpacedRepetitionService(repository, motorProperties);
        aluno = Aluno.builder().id(1L).build();
        questao = Questao.builder().id(1L).build();

        when(repository.save(any(RepeticaoAgendada.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void primeiroAcertoAgendaParaUmDia() {
        when(repository.findByAlunoAndQuestao(aluno, questao)).thenReturn(Optional.empty());

        SpacedRepetitionService.ResultadoAgendamento resultado = service.registrarResultado(aluno, questao, true);

        assertThat(resultado.indiceAtual()).isEqualTo(1);
        assertThat(resultado.proximaData()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(resultado.cicloCompleto()).isFalse();
    }

    @Test
    void acertosSucessivosAvancamPelaSequenciaCompleta() {
        RepeticaoAgendada agendamento = RepeticaoAgendada.builder()
                .aluno(aluno).questao(questao).indiceSequencia(0).proximaData(LocalDate.now())
                .build();
        when(repository.findByAlunoAndQuestao(aluno, questao)).thenReturn(Optional.of(agendamento));

        // indice 0 -> acerto avanca pra indice 1 (3 dias)
        SpacedRepetitionService.ResultadoAgendamento r1 = service.registrarResultado(aluno, questao, true);
        assertThat(r1.indiceAtual()).isEqualTo(1);
        assertThat(r1.proximaData()).isEqualTo(LocalDate.now().plusDays(3));
    }

    @Test
    void erroNaRevisaoReiniciaIndiceParaZero() {
        RepeticaoAgendada agendamento = RepeticaoAgendada.builder()
                .aluno(aluno).questao(questao).indiceSequencia(3).proximaData(LocalDate.now())
                .build();
        when(repository.findByAlunoAndQuestao(aluno, questao)).thenReturn(Optional.of(agendamento));

        SpacedRepetitionService.ResultadoAgendamento resultado = service.registrarResultado(aluno, questao, false);

        assertThat(resultado.indiceAtual()).isZero();
        assertThat(resultado.proximaData()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(resultado.cicloCompleto()).isFalse();
    }

    @Test
    void acertoNoUltimoIndiceCompletaCicloEPermaneceNoUltimoIntervalo() {
        int ultimoIndice = INTERVALOS.size() - 1;
        RepeticaoAgendada agendamento = RepeticaoAgendada.builder()
                .aluno(aluno).questao(questao).indiceSequencia(ultimoIndice).proximaData(LocalDate.now())
                .build();
        when(repository.findByAlunoAndQuestao(aluno, questao)).thenReturn(Optional.of(agendamento));

        SpacedRepetitionService.ResultadoAgendamento resultado = service.registrarResultado(aluno, questao, true);

        assertThat(resultado.cicloCompleto()).isTrue();
        assertThat(resultado.indiceAtual()).isEqualTo(ultimoIndice);
        assertThat(resultado.proximaData()).isEqualTo(LocalDate.now().plusDays(70));
    }

    @Test
    void salvaAgendamentoAtualizadoNoRepositorio() {
        when(repository.findByAlunoAndQuestao(aluno, questao)).thenReturn(Optional.empty());

        service.registrarResultado(aluno, questao, true);

        ArgumentCaptor<RepeticaoAgendada> captor = ArgumentCaptor.forClass(RepeticaoAgendada.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAluno()).isEqualTo(aluno);
        assertThat(captor.getValue().getQuestao()).isEqualTo(questao);
    }
}
