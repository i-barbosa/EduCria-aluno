package com.educria.aluno.aluno;

import com.educria.aluno.aluno.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Optional<Aluno> findByEmail(String email);
    Optional<Aluno> findByMatricula(String matricula);
    List<Aluno> findByTurmaIsNull();
}
