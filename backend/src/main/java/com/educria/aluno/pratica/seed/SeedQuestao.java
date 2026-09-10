package com.educria.aluno.pratica.seed;

import java.util.List;

public record SeedQuestao(int nivel, String enunciado, List<SeedAlternativa> alternativas) {
}
