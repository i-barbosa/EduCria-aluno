package com.educria.aluno.pratica;

import com.educria.aluno.pratica.CondicaoConquista;
import com.educria.aluno.pratica.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
    Optional<Conquista> findByCondicao(CondicaoConquista condicao);
}
