package com.educria.aluno.pratica;

import com.educria.aluno.security.AlunoUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Garante que o aluno autenticado so acesse os proprios dados (id do path == id do token).
 */
@Component
public class AcessoAlunoGuard {

    public void verificar(AlunoUserDetails principal, Long alunoIdPath) {
        if (!principal.getAluno().getId().equals(alunoIdPath)) {
            throw new AccessDeniedException("Aluno nao pode acessar dados de outro aluno");
        }
    }
}
