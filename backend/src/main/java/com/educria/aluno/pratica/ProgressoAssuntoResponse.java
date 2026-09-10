package com.educria.aluno.pratica;

public record ProgressoAssuntoResponse(
        Long assuntoId,
        String assuntoNome,
        int nivelAtual,
        int xp,
        int badgesDesbloqueados
) {
}
