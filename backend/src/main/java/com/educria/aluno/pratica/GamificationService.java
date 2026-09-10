package com.educria.aluno.pratica;

import com.educria.aluno.config.MotorProperties;
import com.educria.aluno.aluno.*;
import com.educria.aluno.pratica.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gamificacao e sempre individual: XP, badges, streak e conquistas nunca sao comparados
 * entre alunos nem expostos como ranking. Servem so para o proprio aluno acompanhar progresso.
 */
@Service
public class GamificationService {

    private final XpRepository xpRepository;
    private final BadgeRepository badgeRepository;
    private final BadgeDesbloqueadoRepository badgeDesbloqueadoRepository;
    private final StreakRepository streakRepository;
    private final ConquistaRepository conquistaRepository;
    private final ConquistaDesbloqueadaRepository conquistaDesbloqueadaRepository;
    private final MotorProperties motorProperties;

    public GamificationService(XpRepository xpRepository,
                                BadgeRepository badgeRepository,
                                BadgeDesbloqueadoRepository badgeDesbloqueadoRepository,
                                StreakRepository streakRepository,
                                ConquistaRepository conquistaRepository,
                                ConquistaDesbloqueadaRepository conquistaDesbloqueadaRepository,
                                MotorProperties motorProperties) {
        this.xpRepository = xpRepository;
        this.badgeRepository = badgeRepository;
        this.badgeDesbloqueadoRepository = badgeDesbloqueadoRepository;
        this.streakRepository = streakRepository;
        this.conquistaRepository = conquistaRepository;
        this.conquistaDesbloqueadaRepository = conquistaDesbloqueadaRepository;
        this.motorProperties = motorProperties;
    }

    public int registrarXpPorAcerto(Aluno aluno, Assunto assunto) {
        Xp xp = xpRepository.findByAlunoAndAssunto(aluno, assunto)
                .orElseGet(() -> Xp.builder().aluno(aluno).assunto(assunto).pontosTotal(0).build());

        int ganho = motorProperties.getXpPorAcerto();
        xp.setPontosTotal(xp.getPontosTotal() + ganho);
        xpRepository.save(xp);
        return ganho;
    }

    public List<String> desbloquearBadgeSeSubiuNivel(Aluno aluno, Assunto assunto, int novoNivel, boolean subiuNivel) {
        List<String> desbloqueados = new ArrayList<>();
        if (!subiuNivel) {
            return desbloqueados;
        }

        badgeRepository.findByAssuntoAndNivelRequerido(assunto, novoNivel).ifPresent(badge -> {
            if (!badgeDesbloqueadoRepository.existsByAlunoAndBadge(aluno, badge)) {
                badgeDesbloqueadoRepository.save(BadgeDesbloqueado.builder()
                        .aluno(aluno)
                        .badge(badge)
                        .data(LocalDateTime.now())
                        .build());
                desbloqueados.add(badge.getNome());
            }
        });

        return desbloqueados;
    }

    public void atualizarStreak(Aluno aluno) {
        Streak streak = streakRepository.findByAluno(aluno)
                .orElseGet(() -> Streak.builder().aluno(aluno).diasSeguidos(0).ultimaDataPratica(null).build());

        LocalDate hoje = LocalDate.now();
        LocalDate ultima = streak.getUltimaDataPratica();

        if (ultima == null || ultima.isBefore(hoje.minusDays(1))) {
            streak.setDiasSeguidos(1);
        } else if (ultima.equals(hoje.minusDays(1))) {
            streak.setDiasSeguidos(streak.getDiasSeguidos() + 1);
        }
        // se ultima == hoje, ja praticou hoje: mantem contagem

        streak.setUltimaDataPratica(hoje);
        streakRepository.save(streak);
    }

    public List<String> verificarConquistas(Aluno aluno, NivelAluno nivelAluno, boolean subiuNivel, boolean cicloRepeticaoCompleto) {
        List<String> desbloqueadas = new ArrayList<>();

        if (nivelAluno.getSequenciaAcertosGlobal() >= 5) {
            desbloquearConquista(aluno, CondicaoConquista.CINCO_ACERTOS_SEGUIDOS).ifPresent(desbloqueadas::add);
        }

        if (subiuNivel) {
            desbloquearConquista(aluno, CondicaoConquista.SUBIU_NIVEL).ifPresent(desbloqueadas::add);
        }

        if (cicloRepeticaoCompleto) {
            desbloquearConquista(aluno, CondicaoConquista.CICLO_REPETICAO_COMPLETO).ifPresent(desbloqueadas::add);
        }

        streakRepository.findByAluno(aluno)
                .filter(streak -> streak.getDiasSeguidos() >= 7)
                .ifPresent(streak -> desbloquearConquista(aluno, CondicaoConquista.STREAK_SETE_DIAS).ifPresent(desbloqueadas::add));

        return desbloqueadas;
    }

    private java.util.Optional<String> desbloquearConquista(Aluno aluno, CondicaoConquista condicao) {
        return conquistaRepository.findByCondicao(condicao).flatMap(conquista -> {
            if (conquistaDesbloqueadaRepository.existsByAlunoAndConquista(aluno, conquista)) {
                return java.util.Optional.empty();
            }
            conquistaDesbloqueadaRepository.save(ConquistaDesbloqueada.builder()
                    .aluno(aluno)
                    .conquista(conquista)
                    .data(LocalDateTime.now())
                    .build());
            return java.util.Optional.of(conquista.getNome());
        });
    }
}
