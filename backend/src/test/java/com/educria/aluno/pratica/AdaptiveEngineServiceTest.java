package com.educria.aluno.pratica;

import com.educria.aluno.config.MotorProperties;
import com.educria.aluno.pratica.NivelAluno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdaptiveEngineServiceTest {

    private AdaptiveEngineService service;

    @BeforeEach
    void setUp() {
        MotorProperties motorProperties = new MotorProperties(1, 10, 10, "1,3,7,16,35,70");
        service = new AdaptiveEngineService(motorProperties);
    }

    private NivelAluno nivelInicial(int nivel) {
        return NivelAluno.builder()
                .nivelAtual(nivel)
                .acertosSeguidos(0)
                .errosSeguidos(0)
                .sequenciaAcertosGlobal(0)
                .build();
    }

    @Test
    void primeiroAcertoNaoSobeNivel() {
        NivelAluno nivelAluno = nivelInicial(3);

        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, true);

        assertThat(resultado.subiuNivel()).isFalse();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(3);
        assertThat(nivelAluno.getAcertosSeguidos()).isEqualTo(1);
    }

    @Test
    void doisAcertosSeguidosSobeUmNivel() {
        NivelAluno nivelAluno = nivelInicial(3);

        service.registrarResposta(nivelAluno, true);
        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, true);

        assertThat(resultado.subiuNivel()).isTrue();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(4);
        assertThat(nivelAluno.getAcertosSeguidos()).isZero();
    }

    @Test
    void umErroIsoladoNaoDesceNivel() {
        NivelAluno nivelAluno = nivelInicial(3);
        nivelAluno.setAcertosSeguidos(1);

        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, false);

        assertThat(resultado.desceuNivel()).isFalse();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(3);
        assertThat(nivelAluno.getErrosSeguidos()).isEqualTo(1);
        assertThat(nivelAluno.getAcertosSeguidos()).isZero();
    }

    @Test
    void doisErrosSeguidosDesceUmNivel() {
        NivelAluno nivelAluno = nivelInicial(3);

        service.registrarResposta(nivelAluno, false);
        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, false);

        assertThat(resultado.desceuNivel()).isTrue();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(2);
        assertThat(nivelAluno.getErrosSeguidos()).isZero();
    }

    @Test
    void erroIntercaladoNaoAcumulaParaSubidaOuDescida() {
        NivelAluno nivelAluno = nivelInicial(3);

        service.registrarResposta(nivelAluno, true);
        service.registrarResposta(nivelAluno, false);
        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, true);

        assertThat(resultado.subiuNivel()).isFalse();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(3);
        assertThat(nivelAluno.getAcertosSeguidos()).isEqualTo(1);
    }

    @Test
    void naoDesceAbaixoDoNivelMinimo() {
        NivelAluno nivelAluno = nivelInicial(1);

        service.registrarResposta(nivelAluno, false);
        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, false);

        assertThat(resultado.desceuNivel()).isFalse();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(1);
    }

    @Test
    void naoSobeAcimaDoNivelMaximo() {
        NivelAluno nivelAluno = nivelInicial(10);

        service.registrarResposta(nivelAluno, true);
        AdaptiveEngineService.ResultadoAjuste resultado = service.registrarResposta(nivelAluno, true);

        assertThat(resultado.subiuNivel()).isFalse();
        assertThat(nivelAluno.getNivelAtual()).isEqualTo(10);
    }

    @Test
    void sequenciaGlobalDeAcertosCresceIndependenteDoNivel() {
        NivelAluno nivelAluno = nivelInicial(3);

        for (int i = 0; i < 5; i++) {
            service.registrarResposta(nivelAluno, true);
        }

        assertThat(nivelAluno.getSequenciaAcertosGlobal()).isEqualTo(5);
    }

    @Test
    void erroZeraSequenciaGlobalDeAcertos() {
        NivelAluno nivelAluno = nivelInicial(3);

        service.registrarResposta(nivelAluno, true);
        service.registrarResposta(nivelAluno, true);
        service.registrarResposta(nivelAluno, false);

        assertThat(nivelAluno.getSequenciaAcertosGlobal()).isZero();
    }
}
