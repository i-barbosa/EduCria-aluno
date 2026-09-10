package com.educria.aluno.pratica;

import jakarta.validation.constraints.NotNull;

public record TentativaRequest(
        @NotNull Long questaoId,
        @NotNull Long alternativaEscolhidaId
) {
}
