package com.educria.aluno.admin;

import jakarta.validation.constraints.NotNull;

public record AtribuirTurmaRequest(@NotNull Long turmaId) {
}
