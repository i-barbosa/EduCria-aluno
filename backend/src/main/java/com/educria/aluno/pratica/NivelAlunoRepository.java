package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Assunto;
import com.educria.aluno.pratica.NivelAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NivelAlunoRepository extends JpaRepository<NivelAluno, Long> {
    Optional<NivelAluno> findByAlunoAndAssunto(Aluno aluno, Assunto assunto);
    List<NivelAluno> findByAluno(Aluno aluno);
}
