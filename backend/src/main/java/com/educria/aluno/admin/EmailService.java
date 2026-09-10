package com.educria.aluno.admin;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Manda as credenciais por e-mail quando um cadastro e aprovado, usando a API HTTP do Brevo.
 * Se ninguem configurar educria.email.brevo-api-key/remetente, o backend continua funcionando
 * normalmente - so nao envia e-mail, e a coordenacao usa as credenciais mostradas na tela.
 */
@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String remetente;

    public EmailService(@Value("${educria.email.brevo-api-key:}") String apiKey,
                         @Value("${educria.email.remetente:}") String remetente) {
        this.remetente = remetente;
        this.restClient = (apiKey.isBlank() || remetente.isBlank())
                ? null
                : RestClient.builder()
                        .baseUrl("https://api.brevo.com/v3")
                        .defaultHeader("api-key", apiKey)
                        .defaultHeader("Content-Type", "application/json")
                        .defaultHeader("Accept", "application/json")
                        .build();
    }

    public boolean enviarCredenciais(String destinatario, String nomeAluno, String matricula, String senha, String turma) {
        if (restClient == null) {
            LOG.warn("Envio de e-mail nao configurado (educria.email.brevo-api-key ausente). "
                    + "Credenciais de {} nao foram enviadas por e-mail.", destinatario);
            return false;
        }

        String texto = "Oi, " + nomeAluno + "!\n\n"
                + "Seu cadastro no EDUCRIA foi aprovado. Voce ja pode entrar com:\n\n"
                + "Turma: " + turma + "\n"
                + "Matricula: " + matricula + "\n"
                + "Senha: " + senha + "\n\n"
                + "Guarde essas informacoes - elas sao a sua identificacao no sistema.";

        BrevoEmailRequest corpo = new BrevoEmailRequest(
                new BrevoContato(remetente, null),
                List.of(new BrevoContato(destinatario, nomeAluno)),
                "Suas credenciais de acesso - EDUCRIA",
                texto
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .body(corpo)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException erro) {
            LOG.error("Falha ao enviar e-mail para {}", destinatario, erro);
            return false;
        }
    }

    private record BrevoContato(String email, String name) {
    }

    private record BrevoEmailRequest(BrevoContato sender, List<BrevoContato> to, String subject, String textContent) {
    }
}
