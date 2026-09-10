package com.educria.aluno.config;

import com.educria.aluno.aluno.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um aluno de demonstracao (matricula/senha conhecidas) so para testar login sem passar
 * pelo fluxo de cadastro/aprovacao. So roda com H2 (perfil default) - nunca em producao com
 * Postgres, onde alunos entram pelo fluxo real (/auth/cadastro -> aprovacao pela coordenacao).
 */
@Component
@Order(2)
@Profile("!postgres")
public class DevDataSeeder implements CommandLineRunner {

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(AlunoRepository alunoRepository,
                          TurmaRepository turmaRepository,
                          PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (alunoRepository.count() > 0) {
            return;
        }

        Turma turmaDemo = turmaRepository.save(Turma.builder().nome("DEMO").build());

        Aluno aluno = alunoRepository.save(Aluno.builder()
                .nome("Aluno Teste")
                .email("aluno@educria.com")
                .senhaHash(passwordEncoder.encode("123456"))
                .matricula("DEMO0001")
                .turma(turmaDemo)
                .role(Role.ALUNO)
                .build());

        System.out.println("=== EDUCRIA aluno demo criado ===");
        System.out.println("Login: matricula=" + aluno.getMatricula() + " / senha=123456 | alunoId=" + aluno.getId());
        System.out.println("Admin: POST /admin/login com a senha de educria.admin.senha (default admin123)");
    }
}
