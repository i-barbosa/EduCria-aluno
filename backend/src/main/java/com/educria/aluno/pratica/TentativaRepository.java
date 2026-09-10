package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Questao;
import com.educria.aluno.pratica.Tentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaRepository extends JpaRepository<Tentativa, Long> {
    List<Tentativa> findByAlunoAndQuestao(Aluno aluno, Questao questao);
    List<Tentativa> findByAlunoOrderByDataHoraDesc(Aluno aluno);
}
