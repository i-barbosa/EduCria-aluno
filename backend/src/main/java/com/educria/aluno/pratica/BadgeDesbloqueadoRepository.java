package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.pratica.Badge;
import com.educria.aluno.pratica.BadgeDesbloqueado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeDesbloqueadoRepository extends JpaRepository<BadgeDesbloqueado, Long> {
    boolean existsByAlunoAndBadge(Aluno aluno, Badge badge);
    List<BadgeDesbloqueado> findByAluno(Aluno aluno);
}
