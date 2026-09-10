package com.educria.aluno.pratica;

import java.time.LocalDate;
import java.util.List;

public record TentativaResponse(
        boolean acertou,
        int nivelAtual,
        boolean subiuNivel,
        boolean desceuNivel,
        int xpGanho,
        LocalDate proximaRevisao,
        List<String> badgesDesbloqueados,
        List<String> conquistasDesbloqueadas
) {
}
