package com.educria.aluno.pratica;

import java.util.List;

public record QuestaoResponse(
        Long id,
        Long assuntoId,
        Integer nivel,
        String enunciado,
        List<AlternativaResponse> alternativas
) {
}
