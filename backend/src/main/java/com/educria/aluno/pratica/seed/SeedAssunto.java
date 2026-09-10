package com.educria.aluno.pratica.seed;

import java.util.List;

public record SeedAssunto(String nome, String disciplina, List<SeedQuestao> questoes) {
}
