package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Assunto;
import com.educria.aluno.pratica.Xp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface XpRepository extends JpaRepository<Xp, Long> {
    Optional<Xp> findByAlunoAndAssunto(Aluno aluno, Assunto assunto);
    List<Xp> findByAluno(Aluno aluno);
}
