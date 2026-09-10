package com.educria.aluno.pratica;

import com.educria.aluno.pratica.Assunto;
import com.educria.aluno.pratica.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    List<Questao> findByAssuntoAndNivel(Assunto assunto, Integer nivel);

    List<Questao> findByAssuntoAndNivelAndIdNotIn(Assunto assunto, Integer nivel, List<Long> idsExcluidos);
}
