package com.educria.aluno.pratica;

import java.time.LocalDateTime;

public record ConquistaResponse(
        Long conquistaId,
        String nome,
        LocalDateTime dataDesbloqueio
) {
}
