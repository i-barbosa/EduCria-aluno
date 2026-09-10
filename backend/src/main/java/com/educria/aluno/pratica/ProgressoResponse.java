package com.educria.aluno.pratica;

import java.util.List;

public record ProgressoResponse(
        Long alunoId,
        String nome,
        int streakDiasSeguidos,
        List<ProgressoAssuntoResponse> assuntos
) {
}
