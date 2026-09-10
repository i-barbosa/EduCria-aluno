package com.educria.aluno.admin;

public record AlunoAtribuidoDto(
        Long id,
        String nome,
        String email,
        String matricula,
        String turma,
        String senha,
        boolean emailEnviado
) {
}
