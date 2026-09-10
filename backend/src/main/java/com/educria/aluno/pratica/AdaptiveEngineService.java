package com.educria.aluno.pratica;

import com.educria.aluno.config.MotorProperties;
import com.educria.aluno.pratica.NivelAluno;
import org.springframework.stereotype.Service;

/**
 * Motor adaptativo: ajusta o nivel do aluno por assunto.
 * Regra: 2 acertos seguidos no mesmo nivel sobe 1 nivel; nivel so desce no 2o erro seguido.
 * Nunca zera XP nem penaliza o aluno, apenas ajusta a dificuldade das proximas questoes.
 */
@Service
public class AdaptiveEngineService {

    private static final int ACERTOS_PARA_SUBIR = 2;
    private static final int ERROS_PARA_DESCER = 2;

    private final MotorProperties motorProperties;

    public AdaptiveEngineService(MotorProperties motorProperties) {
        this.motorProperties = motorProperties;
    }

    public record ResultadoAjuste(boolean subiuNivel, boolean desceuNivel) {
    }

    public ResultadoAjuste registrarResposta(NivelAluno nivelAluno, boolean acertou) {
        if (acertou) {
            return registrarAcerto(nivelAluno);
        }
        return registrarErro(nivelAluno);
    }

    private ResultadoAjuste registrarAcerto(NivelAluno nivelAluno) {
        nivelAluno.setErrosSeguidos(0);
        nivelAluno.setSequenciaAcertosGlobal(nivelAluno.getSequenciaAcertosGlobal() + 1);

        int acertosSeguidos = nivelAluno.getAcertosSeguidos() + 1;

        if (acertosSeguidos >= ACERTOS_PARA_SUBIR) {
            nivelAluno.setAcertosSeguidos(0);

            int nivelMaximo = motorProperties.getNivelMaximo();
            if (nivelAluno.getNivelAtual() < nivelMaximo) {
                nivelAluno.setNivelAtual(nivelAluno.getNivelAtual() + 1);
                return new ResultadoAjuste(true, false);
            }
            return new ResultadoAjuste(false, false);
        }

        nivelAluno.setAcertosSeguidos(acertosSeguidos);
        return new ResultadoAjuste(false, false);
    }

    private ResultadoAjuste registrarErro(NivelAluno nivelAluno) {
        nivelAluno.setAcertosSeguidos(0);
        nivelAluno.setSequenciaAcertosGlobal(0);

        int errosSeguidos = nivelAluno.getErrosSeguidos() + 1;

        if (errosSeguidos >= ERROS_PARA_DESCER) {
            nivelAluno.setErrosSeguidos(0);

            int nivelMinimo = motorProperties.getNivelMinimo();
            if (nivelAluno.getNivelAtual() > nivelMinimo) {
                nivelAluno.setNivelAtual(nivelAluno.getNivelAtual() - 1);
                return new ResultadoAjuste(false, true);
            }
            return new ResultadoAjuste(false, false);
        }

        nivelAluno.setErrosSeguidos(errosSeguidos);
        return new ResultadoAjuste(false, false);
    }
}
