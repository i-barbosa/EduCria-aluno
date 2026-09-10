package com.educria.aluno.admin;

import com.educria.aluno.aluno.Turma;
import com.educria.aluno.aluno.AlunoRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Gera a matricula de um aluno aprovado: prefixo da turma + 4 digitos sorteados (nao sequenciais). */
@Component
public class GeradorMatricula {

    private static final int TENTATIVAS_MAXIMAS = 30;

    private final AlunoRepository alunoRepository;
    private final SecureRandom aleatorio = new SecureRandom();

    public GeradorMatricula(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public String gerar(Turma turma) {
        for (int tentativa = 0; tentativa < TENTATIVAS_MAXIMAS; tentativa++) {
            String candidata = turma.getNome() + String.format("%04d", aleatorio.nextInt(10000));
            if (alunoRepository.findByMatricula(candidata).isEmpty()) {
                return candidata;
            }
        }
        throw new IllegalStateException("Nao foi possivel gerar uma matricula unica para a turma " + turma.getNome());
    }
}
