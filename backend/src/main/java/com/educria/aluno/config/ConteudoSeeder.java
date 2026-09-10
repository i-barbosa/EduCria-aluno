package com.educria.aluno.config;

import com.educria.aluno.pratica.*;
import com.educria.aluno.pratica.seed.SeedArquivo;
import com.educria.aluno.pratica.seed.SeedAssunto;
import com.educria.aluno.pratica.seed.SeedQuestao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * Popula materias, questoes, badges e conquistas a partir de src/main/resources/seed/questoes.json.
 * Roda em QUALQUER ambiente (H2 local ou Postgres em producao) - e conteudo de verdade da
 * plataforma, nao dado de teste. Edite o questoes.json para adicionar/mudar materias e questoes,
 * sem mexer em codigo Java. So popula se ainda nao existir nenhum assunto (idempotente).
 */
@Component
@Order(1)
public class ConteudoSeeder implements CommandLineRunner {

    private static final String ARQUIVO_SEED = "seed/questoes.json";

    private final AssuntoRepository assuntoRepository;
    private final QuestaoRepository questaoRepository;
    private final BadgeRepository badgeRepository;
    private final ConquistaRepository conquistaRepository;
    private final ObjectMapper objectMapper;

    public ConteudoSeeder(AssuntoRepository assuntoRepository,
                           QuestaoRepository questaoRepository,
                           BadgeRepository badgeRepository,
                           ConquistaRepository conquistaRepository,
                           ObjectMapper objectMapper) {
        this.assuntoRepository = assuntoRepository;
        this.questaoRepository = questaoRepository;
        this.badgeRepository = badgeRepository;
        this.conquistaRepository = conquistaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws IOException {
        if (assuntoRepository.count() > 0) {
            return;
        }

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

        System.out.println("=== EDUCRIA conteudo criado ===");
        System.out.println("Assuntos: " + resumo);
    }

    private SeedArquivo carregarSeed() throws IOException {
        try (InputStream input = new ClassPathResource(ARQUIVO_SEED).getInputStream()) {
            return objectMapper.readValue(input, SeedArquivo.class);
        }
    }
}
