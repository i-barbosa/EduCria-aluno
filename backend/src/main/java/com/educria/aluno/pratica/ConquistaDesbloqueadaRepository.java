package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Conquista;
import com.educria.aluno.pratica.ConquistaDesbloqueada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConquistaDesbloqueadaRepository extends JpaRepository<ConquistaDesbloqueada, Long> {
    boolean existsByAlunoAndConquista(Aluno aluno, Conquista conquista);
    List<ConquistaDesbloqueada> findByAluno(Aluno aluno);
}
