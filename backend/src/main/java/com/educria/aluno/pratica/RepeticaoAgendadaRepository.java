package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Questao;
import com.educria.aluno.pratica.RepeticaoAgendada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepeticaoAgendadaRepository extends JpaRepository<RepeticaoAgendada, Long> {

    Optional<RepeticaoAgendada> findByAlunoAndQuestao(Aluno aluno, Questao questao);

    List<RepeticaoAgendada> findByAlunoAndProximaDataLessThanEqualOrderByProximaDataAsc(Aluno aluno, LocalDate data);
}
