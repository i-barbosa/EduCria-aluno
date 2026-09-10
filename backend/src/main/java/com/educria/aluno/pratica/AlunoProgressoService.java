package com.educria.aluno.pratica;

import com.educria.aluno.aluno.Aluno;
import com.educria.aluno.aluno.AlunoRepository;
import com.educria.aluno.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoProgressoService {

    private final AlunoRepository alunoRepository;
    private final NivelAlunoRepository nivelAlunoRepository;
    private final XpRepository xpRepository;
    private final BadgeDesbloqueadoRepository badgeDesbloqueadoRepository;
    private final StreakRepository streakRepository;
    private final ConquistaDesbloqueadaRepository conquistaDesbloqueadaRepository;

    public AlunoProgressoService(AlunoRepository alunoRepository,
                                  NivelAlunoRepository nivelAlunoRepository,
                                  XpRepository xpRepository,
                                  BadgeDesbloqueadoRepository badgeDesbloqueadoRepository,
                                  StreakRepository streakRepository,
                                  ConquistaDesbloqueadaRepository conquistaDesbloqueadaRepository) {
        this.alunoRepository = alunoRepository;
        this.nivelAlunoRepository = nivelAlunoRepository;
        this.xpRepository = xpRepository;
        this.badgeDesbloqueadoRepository = badgeDesbloqueadoRepository;
        this.streakRepository = streakRepository;
        this.conquistaDesbloqueadaRepository = conquistaDesbloqueadaRepository;
    }

    public ProgressoResponse progresso(Long alunoId) {
        Aluno aluno = buscarAluno(alunoId);

        List<NivelAluno> niveis = nivelAlunoRepository.findByAluno(aluno);
        List<BadgeDesbloqueado> badgesDoAluno = badgeDesbloqueadoRepository.findByAluno(aluno);

        List<ProgressoAssuntoResponse> assuntos = niveis.stream()
                .map(nivel -> {
                    int xp = xpRepository.findByAlunoAndAssunto(aluno, nivel.getAssunto())
                            .map(x -> x.getPontosTotal())
                            .orElse(0);
                    long badgesDoAssunto = badgesDoAluno.stream()
                            .filter(bd -> bd.getBadge().getAssunto().getId().equals(nivel.getAssunto().getId()))
                            .count();
                    return new ProgressoAssuntoResponse(
                            nivel.getAssunto().getId(),
                            nivel.getAssunto().getNome(),
                            nivel.getNivelAtual(),
                            xp,
                            (int) badgesDoAssunto);
                })
                .toList();

        int streakDias = streakRepository.findByAluno(aluno)
                .map(s -> s.getDiasSeguidos())
                .orElse(0);

        return new ProgressoResponse(aluno.getId(), aluno.getNome(), streakDias, assuntos);
    }

    public List<ConquistaResponse> conquistas(Long alunoId) {
        Aluno aluno = buscarAluno(alunoId);

        return conquistaDesbloqueadaRepository.findByAluno(aluno).stream()
                .map(cd -> new ConquistaResponse(cd.getConquista().getId(), cd.getConquista().getNome(), cd.getData()))
                .toList();
    }

    private Aluno buscarAluno(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado: " + alunoId));
    }
}
