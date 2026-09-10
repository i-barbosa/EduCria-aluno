package com.educria.aluno.aluno;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String matricula,
        @NotBlank String senha
) {
}
