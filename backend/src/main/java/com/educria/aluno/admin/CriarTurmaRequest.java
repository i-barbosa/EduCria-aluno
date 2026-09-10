package com.educria.aluno.admin;

import jakarta.validation.constraints.NotBlank;

public record CriarTurmaRequest(@NotBlank String nome) {
}
