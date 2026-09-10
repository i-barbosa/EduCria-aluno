package com.educria.aluno.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${educria.jwt.secret}") String secret,
                       @Value("${educria.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(UserDetails userDetails, Long alunoId) {
        return construirToken(userDetails.getUsername(), Map.of("alunoId", alunoId, "role", "ALUNO"));
    }

    /** Token de coordenacao: nao corresponde a nenhum registro no banco, so a senha configurada. */
    public String gerarTokenAdmin() {
        return construirToken("admin", Map.of("role", "ADMIN"));
    }

    private String construirToken(String subject, Map<String, Object> claims) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extrairSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extrairRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    public Long extrairAlunoId(String token) {
        Object alunoId = parseClaims(token).get("alunoId");
        return alunoId == null ? null : Long.valueOf(alunoId.toString());
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        String subject = extrairSubject(token);
        return subject.equals(userDetails.getUsername()) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
