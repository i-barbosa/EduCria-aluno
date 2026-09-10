package com.educria.aluno.aluno;

import com.educria.aluno.aluno.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
    Optional<Turma> findByNome(String nome);
}
