package com.educria.aluno.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MotorProperties {

    private final int nivelMinimo;
    private final int nivelMaximo;
    private final int xpPorAcerto;
    private final String intervalosRaw;

    public MotorProperties(@Value("${educria.motor.nivel-minimo}") int nivelMinimo,
                            @Value("${educria.motor.nivel-maximo}") int nivelMaximo,
                            @Value("${educria.gamificacao.xp-por-acerto}") int xpPorAcerto,
                            @Value("${educria.repeticao.intervalos}") String intervalosRaw) {
        this.nivelMinimo = nivelMinimo;
        this.nivelMaximo = nivelMaximo;
        this.xpPorAcerto = xpPorAcerto;
        this.intervalosRaw = intervalosRaw;
    }

    public int getNivelMinimo() {
        return nivelMinimo;
    }

    public int getNivelMaximo() {
        return nivelMaximo;
    }

    public int getXpPorAcerto() {
        return xpPorAcerto;
    }

    public List<Integer> getIntervalosRepeticao() {
        return Arrays.stream(intervalosRaw.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
    }
}
