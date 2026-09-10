package com.educria.aluno.aluno;

/** Nunca leva senha nem token - so sinaliza se ja pode ir para a tela de login. */
public record StatusCadastroResponse(String nome, boolean aprovado, String matricula, String turma) {
}
