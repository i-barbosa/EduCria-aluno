package com.educria.aluno.pratica;

import com.educria.aluno.pratica.Assunto;
import com.educria.aluno.pratica.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByAssuntoAndNivelRequerido(Assunto assunto, Integer nivelRequerido);
}
