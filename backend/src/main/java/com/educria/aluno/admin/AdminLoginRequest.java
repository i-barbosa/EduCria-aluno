package com.educria.aluno.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(@NotBlank String senha) {
}
