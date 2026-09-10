package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByAluno(Aluno aluno);
}
