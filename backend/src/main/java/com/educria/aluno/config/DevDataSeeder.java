package com.educria.aluno.config;

import com.educria.aluno.pratica.seed.SeedArquivo;
import com.educria.aluno.pratica.seed.SeedAssunto;
import com.educria.aluno.pratica.seed.SeedQuestao;
import com.educria.aluno.aluno.*;
import com.educria.aluno.pratica.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Popula dado minimo em ambiente de desenvolvimento (perfil default, H2)
 * para permitir testar login e o fluxo de tentativa sem precisar de um front.
 * Temas e questoes vem de src/main/resources/seed/questoes.json - edite esse
 * arquivo para adicionar/mudar assuntos e questoes, sem mexer em codigo Java.
 * Nao roda com o perfil "postgres" (producao real usaria migrations, nao seed automatico).
 */
@Component
@Profile("!postgres")
public class DevDataSeeder implements CommandLineRunner {

    private static final String ARQUIVO_SEED = "seed/questoes.json";

    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final AssuntoRepository assuntoRepository;
    private final QuestaoRepository questaoRepository;
    private final BadgeRepository badgeRepository;
    private final ConquistaRepository conquistaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DevDataSeeder(AlunoRepository alunoRepository,
                          TurmaRepository turmaRepository,
                          AssuntoRepository assuntoRepository,
                          QuestaoRepository questaoRepository,
                          BadgeRepository badgeRepository,
                          ConquistaRepository conquistaRepository,
                          PasswordEncoder passwordEncoder,
                          ObjectMapper objectMapper) {
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.assuntoRepository = assuntoRepository;
        this.questaoRepository = questaoRepository;
        this.badgeRepository = badgeRepository;
        this.conquistaRepository = conquistaRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws IOException {
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

        SeedArquivo seed = carregarSeed();
        StringBuilder resumo = new StringBuilder();

        for (SeedAssunto seedAssunto : seed.assuntos()) {
            Assunto assunto = assuntoRepository.save(Assunto.builder()
                    .nome(seedAssunto.nome())
                    .disciplina(seedAssunto.disciplina())
                    .build());

            for (SeedQuestao seedQuestao : seedAssunto.questoes()) {
                Questao questao = Questao.builder()
                        .assunto(assunto)
                        .nivel(seedQuestao.nivel())
                        .enunciado(seedQuestao.enunciado())
                        .build();

                seedQuestao.alternativas().forEach(alt -> questao.getAlternativas().add(
                        Alternativa.builder().questao(questao).texto(alt.texto()).correta(alt.correta()).build()));

                questaoRepository.save(questao);
            }

            // um badge por nivel acima do primeiro que tiver questao cadastrada
            Set<Integer> niveis = new TreeSet<>();
            seedAssunto.questoes().forEach(q -> niveis.add(q.nivel()));
            niveis.stream().filter(n -> n > 1).forEach(nivel ->
                    badgeRepository.save(Badge.builder()
                            .nome(seedAssunto.nome() + " - Nivel " + nivel)
                            .assunto(assunto)
                            .nivelRequerido(nivel)
                            .build()));

            resumo.append(assunto.getNome()).append(" (id=").append(assunto.getId())
                    .append(", ").append(seedAssunto.questoes().size()).append(" questoes) ");
        }

        conquistaRepository.save(Conquista.builder().nome("5 acertos seguidos").condicao(CondicaoConquista.CINCO_ACERTOS_SEGUIDOS).build());
        conquistaRepository.save(Conquista.builder().nome("Completou um ciclo de repeticao espacada").condicao(CondicaoConquista.CICLO_REPETICAO_COMPLETO).build());
        conquistaRepository.save(Conquista.builder().nome("Subiu de nivel").condicao(CondicaoConquista.SUBIU_NIVEL).build());
        conquistaRepository.save(Conquista.builder().nome("7 dias seguidos praticando").condicao(CondicaoConquista.STREAK_SETE_DIAS).build());

        System.out.println("=== EDUCRIA seed criado ===");
        System.out.println("Login: matricula=" + aluno.getMatricula() + " / senha=123456 | alunoId=" + aluno.getId());
        System.out.println("Admin: POST /admin/login com a senha de educria.admin.senha (default admin123)");
        System.out.println("Assuntos: " + resumo);
    }

    private SeedArquivo carregarSeed() throws IOException {
        try (InputStream input = new ClassPathResource(ARQUIVO_SEED).getInputStream()) {
            return objectMapper.readValue(input, SeedArquivo.class);
        }
    }
}
