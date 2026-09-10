package com.educria.aluno.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CadastroRequest(
        @NotBlank String nome,
        @NotBlank @Email String email
) {
}
