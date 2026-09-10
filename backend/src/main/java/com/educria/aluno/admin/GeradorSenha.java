package com.educria.aluno.admin;

import java.security.SecureRandom;

/** Gera a senha temporaria entregue ao aluno quando a coordenacao aprova o cadastro. */
public final class GeradorSenha {

    // sem caracteres faceis de confundir (0/O, 1/I/l)
    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    public static String gerar() {
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            senha.append(CARACTERES.charAt(ALEATORIO.nextInt(CARACTERES.length())));
        }
        return senha.toString();
    }

    private GeradorSenha() {
    }
}
