package com.educria.aluno.aluno;

public record LoginResponse(
        String token,
        Long alunoId,
        String nome
) {
}
